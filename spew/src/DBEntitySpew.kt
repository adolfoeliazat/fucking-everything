package vgrechka.spew

import org.jetbrains.kotlin.psi.*
import org.jgrapht.alg.CycleDetector
import vgrechka.*
import vgrechka.spew.GDBEntitySpewDatabaseDialect.*
import java.io.File
import java.util.*
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.traverse.TopologicalOrderIterator

sealed class FieldKind {
    class Simple : FieldKind()
    class One(val fetchType: GFetchType) : FieldKind()
    data class Many(val mappedBy: String, val fetchType: GFetchType) : FieldKind()
}

data class FieldSpec(
    val name: String,
    val type: String,
    val isEntity: Boolean,
    val isInToString: Boolean,
    val isOrderColumn: Boolean,
    val kind: FieldKind)
{
    fun typeWithoutQuestion() = when {
        type.endsWith("?") -> type.dropLast(1)
        else -> type
    }

    fun isNullable() = type.endsWith("?")
}

data class FinderParamSpec(
    val name: String,
    val type: String)

data class FinderSpec(
    val definedFinderName: String,
    val generatedFinderName: String,
    val params: List<FinderParamSpec>,
    val returnsList: Boolean,
    val returnsNullable: Boolean)

data class EntitySpec(
    val name: String,
    val tableName: String,
    val fields: List<FieldSpec>,
    val finders: List<FinderSpec>)


class CommonDBEntitySpew(val ktFile: KtFile, val outputFilePath: String, val spewResults: SpewResults, makePedro: (CommonDBEntitySpew) -> Pedro) {
    val out = CodeShitter()
    val entities = mutableListOf<EntitySpec>()
    var pileObjectName: String? = null
    var databaseDialect by notNullOnce<GDBEntitySpewDatabaseDialect>()
    val tableDependsOnTable = mutableListOf<Pair<String, String>>()
    val drops = mutableListOf<PieceOfDDL>()
    val creates = mutableListOf<PieceOfDDL>()
    val pedro = makePedro(this)

    class PieceOfDDL(val table: String, val ddl: String)

    interface Juan {
        fun spitEntityCtor()
        fun spitEntityClass()
        fun spitVariousShit()
        fun spitRepo()
        fun spitDDLForSpecialColumns(buf: StringBuilder)
        fun columnDDL(field: FieldSpec, sqlType: String): String
    }

    interface Pedro {
        fun makeJuan(juanCtx: spitShitForEntity): Juan
        fun spitImports()
        fun createTableSuffix(): String
    }

    companion object {
        fun maybeQuestion(finder: FinderSpec) =
            finder.returnsNullable.thenElseEmpty {"?"}

        fun maybeWrapShitIntoPlatformType(typed: FieldSpec, shit: String): String {
            val platformType = platformType(typed)
            return when {
                platformType != typed.type -> "$platformType($shit)"
                else -> shit
            }
        }

        fun platformType(fieldSpec: FieldSpec): String {
            val platformFieldType = when (fieldSpec.type) {
                "Int" -> "java.lang.Integer"
                "Long" -> "java.lang.Long"
                "Boolean" -> "java.lang.Boolean"
                else -> fieldSpec.type
            }
            return platformFieldType
        }

        fun maybeUnwrapShitFromPlatformType(typed: FieldSpec, shit: String): String {
            val platformType = platformType(typed)
            return when {
                platformType != typed.type -> when (typed.type) {
                    "Int" -> "$shit.toInt()"
                    "Long" -> "$shit.toLong()"
                    "Boolean" -> "$shit.booleanValue()"
                    else -> wtf("f8ef6767-4bfd-4bad-8ee3-9b5fbd768990")
                }
                else -> shit
            }
        }

    }

    init {
        val file = File(outputFilePath)

        analyzeSources()

        val packageName = (ktFile.packageDirective
            ?: wtf("512f3d89-eb0d-4653-b04f-686c6100a900"))
            .qualifiedName

        shitHeader(packageName)

        for (_entity in entities) {
            spitShitForEntity(_entity)
        }

        sortAndCombineDDLPieces()
        spitStuffClass()
        spitDDLComment()

        FilePile.backUp().ifExists().ignite(file)
        file.writeText(out.reify() + GlobalSpewContext.maybeDebugInfo())
        clog("Written ${file.path}")
    }

    fun spitStuffClass() {
        if (pileObjectName != null) {
            out.append("object $pileObjectName {\n")
            out.append("    object ddl {\n")
            out.append("        val dropCreateAllScript = \"\"\"\n")
            out.append(spewResults.ddl.toString())
            out.append("        \"\"\"\n")
            out.append("    }\n")
            out.append("}\n")
        }
    }

    fun spitDDLComment() {
        out.append("\n\n")
        out.append("/*\n")
        out.append("DDL\n")
        out.append("===\n")
        out.append("\n")
        out.append(spewResults.ddl.toString())
        out.append("*/")
    }

    fun sortAndCombineDDLPieces() {
        // https://github.com/jgrapht/jgrapht/wiki/DependencyDemo

        val g = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)
        for (pair in tableDependsOnTable) {
            if (!g.containsVertex(pair.first))
                g.addVertex(pair.first)
            if (!g.containsVertex(pair.second))
                g.addVertex(pair.second)
            g.addEdge(pair.first, pair.second)
        }

        if (CycleDetector<String, DefaultEdge>(g).detectCycles())
            bitch("Found some table dependency cycles. So, fuck you")

        val connectedTables = Iterable {TopologicalOrderIterator(g)}.toList()
        val allTables = mutableListOf<String>()
        allTables.addAll(connectedTables)
        for (entity in entities) {
            if (!allTables.contains(entity.tableName))
                allTables += entity.tableName
        }

        for (table in allTables)
            spewResults.ddl.append(drops.find {it.table == table}!!.ddl)
        for (table in allTables.reversed())
            spewResults.ddl.append(creates.find {it.table == table}!!.ddl)
    }


    inner class spitShitForEntity(val entity: EntitySpec) {
        val en = entity.name
        val end = en.decapitalize()
        val out get() = this@CommonDBEntitySpew.out
        val juan = pedro.makeJuan(this)

        init {
            out.smallSection(en)
            juan.spitEntityCtor()

            juan.spitRepo()
            juan.spitVariousShit()

            juan.spitEntityClass()

            generateDDLForEntity()
        }

        val timestampType get() = when (databaseDialect) {
            SQLITE -> "text"
            POSTGRESQL -> "timestamp"
            MYSQL -> "datetime"
        }

        val booleanType get() = when (databaseDialect) {
            SQLITE -> "integer"
            POSTGRESQL -> "boolean"
            MYSQL -> "boolean"
        }


        inner class generateDDLForEntity {
            val entity = this@spitShitForEntity.entity.copy()
//            val end = end

//            fun append(out: CodeShitter?, x: String) {
//                if (out != null)
//                    out.append(x)
//                else
//                    spewResults.ddl.append(x)
//            }

            fun quote(s: String) = when (databaseDialect) {
                SQLITE -> "`$s`"
                POSTGRESQL -> '"' + s + '"'
                MYSQL -> "`$s`"
            }

            init {
                val quotedTableName = quote(entity.tableName)

                val foreignKeyFields = entity.fields.filter {it.kind is FieldKind.One}
                for (field in foreignKeyFields) {
                    val oneEntity = entities.find {it.name == field.typeWithoutQuestion()} ?: wtf("d8beb50d-dc3f-42ab-8a64-16fb61ecfc42")
                    tableDependsOnTable += entity.tableName to oneEntity.tableName
                }

                drops += PieceOfDDL(
                    table = entity.tableName,
                    ddl = "drop table if exists $quotedTableName;\n"
                )

                creates += PieceOfDDL(
                    table = entity.tableName,
                    ddl = buildString {
                        append("create table $quotedTableName (\n")
                        juan.spitDDLForSpecialColumns(this)
                        val fields = entity.fields.filter {
                            it.kind !is FieldKind.Many
                                && it.name !in setOf("id", "createdAt", "updatedAt", "deleted")
                        }

                        for ((index, field) in fields.withIndex()) {
                            val sqlType = when (field.kind) {
                                is FieldKind.Simple -> {
                                    val fieldTypeWithoutQuestion = field.type.replace(Regex("\\?$"), "")
                                    val sql = when (fieldTypeWithoutQuestion) {
                                        "Int" -> when (databaseDialect) {
                                            SQLITE -> "integer"
                                            POSTGRESQL -> "integer"
                                            MYSQL -> "integer"
                                        }
                                        "Long" -> when (databaseDialect) {
                                            SQLITE -> "bigint"
                                            POSTGRESQL -> "bigint"
                                            MYSQL -> "bigint"
                                        }
                                        "Boolean" -> when (databaseDialect) {
                                            SQLITE -> "int"
                                            POSTGRESQL -> "boolean"
                                            MYSQL -> "boolean"
                                        }
                                        "String" -> when (databaseDialect) {
                                            SQLITE -> "text"
                                            POSTGRESQL -> "text"
                                            MYSQL -> "longtext"
                                        }
                                        "ByteArray" -> when (databaseDialect) {
                                            SQLITE -> "blob"
                                            POSTGRESQL -> "bytea"
                                            MYSQL -> "longblob"
                                        }
                                        "PHPTimestamp" -> when (databaseDialect) {
                                            SQLITE -> imf("696a10c7-83f1-4562-8b71-2a1d6e48a08c")
                                            POSTGRESQL -> imf("6d372efa-62d0-4573-a98f-f8134f91b4ae")
                                            MYSQL -> "datetime"
                                        }
                                        else -> wtf("field.type = ${field.type}    5e84c6fb-b523-43cc-aa45-bdef1dca7ff2")
                                    }
                                    if (field.type.endsWith("?"))
                                        sql
                                    else
                                        sql + " not null"
                                }
                                is FieldKind.One -> {
                                    val s = when (databaseDialect) {
                                        SQLITE -> "bigint"
                                        POSTGRESQL -> "bigint"
                                        MYSQL -> "bigint"
                                    }
                                    s + (!field.isNullable()).thenElseEmpty{" not null"}
                                }
                                is FieldKind.Many -> wtf("0f79f787-be13-49ba-ac1d-6855476605da")
                            }

                            append("    " + juan.columnDDL(field, sqlType))
                            if (index < fields.lastIndex) {
                                append(",\n")
                            }
                        }

                        val foreignKeyLines = foreignKeyFields.map {field->
                            val oneEntity = entities.find {it.name == field.typeWithoutQuestion()} ?: wtf("aa058895-cbce-453a-b0f0-e551abaf95e1")
                            "    foreign key (${end}_${field.name}__id) references ${oneEntity.tableName}(id)"
                        }
                        if (foreignKeyLines.isNotEmpty())
                            append(",\n")
                        append(foreignKeyLines.joinToString(",\n"))

                        append("\n) ${pedro.createTableSuffix()};\n")

                        if (databaseDialect == POSTGRESQL) {
                            foreignKeyFields.forEach {field->
                                append("create index on $quotedTableName (${end}_${field.name}__id);\n")
                            }
                        }

                        append("\n")
                    }
                )
            }

        }

    }


    fun shitHeader(packageName: String) {
        out.headerComment()
        out.line("""
                //
                // Generated on ${Date()}
                // Model: ${ktFile.virtualFile.path}
                //

                package $packageName

                import kotlin.reflect.KClass
                import vgrechka.*
                import vgrechka.spew.*
            """)
        pedro.spitImports()
        out.line("")
    }

    fun noise(x: Any?) {
        if (false) clog(x)
    }

    fun analyzeSources() {
        val nameToKlass = mutableMapOf<String, KtClass>()

        object {
            init {
                for (ann in ktFile.freakingFindAnnotations(GDBEntitySpewOptions::class.simpleName!!)) {
                    ann.freakingGetStringAttribute(GDBEntitySpewOptions::pileObject.name)?.let {
                        if (it.isNotBlank())
                            pileObjectName = it
                    }
                    val dialectText = ann.freakingGetEnumAttributeText(GDBEntitySpewOptions::databaseDialect.name)!!
                    databaseDialect = GDBEntitySpewDatabaseDialect.valueOf(dialectText.substringAfterLast("."))
                }

                ktFile.freakingVisitClasses {
                    nameToKlass[it.name!!] = it
                }

                for (klass in nameToKlass.values) {
                    if (klass.isInterface()) {
                        for (annotationEntry in klass.annotationEntries) {
                            val annotationType = annotationEntry.typeReference!!.text
                            if (annotationType == "GEntity") {
                                noise("")
                                val entityName = klass.name!!
                                noise("entityName = $entityName")
                                val tableName = annotationEntry.freakingGetStringAttribute("table") ?: wtf("bf5e88ea-a756-4512-ad16-aeda4ec0e29a")
                                noise("tableName = $tableName")
                                val fields = mutableListOf<FieldSpec>()
                                val finders = mutableListOf<FinderSpec>()

                                for (decl in klass.declarations) {
                                    if (decl is KtProperty) {
                                        val prop = decl
                                        val name = prop.name!!
                                        var kind: FieldKind = FieldKind.Simple()
                                        var type = prop.typeReference!!.text

                                        fun findFetchType(ann: KtAnnotationEntry, default: GFetchType): GFetchType {
                                            val prefix = GFetchType::class.simpleName + "."
                                            val fetchTypeString = ann.freakingGetEnumAttributeText("fetch")
                                                ?: "$prefix$default"
                                            check(fetchTypeString.startsWith(prefix)) {"3e673b6f-939a-4a72-a4cf-a9fe20416ae5"}
                                            return GFetchType.valueOf(fetchTypeString.substring(prefix.length))
                                        }

                                        if (type.startsWith("MutableList<")) {
                                            type = type.substring("MutableList<".length, type.lastIndexOf(">"))
                                            val oneToManyAnnotationEntry = prop.freakingFindAnnotation("GOneToMany") ?: wtf("556650b2-91b5-45de-887f-81f87f701c49")
                                            val mappedBy = oneToManyAnnotationEntry.freakingGetStringAttribute("mappedBy") ?: wtf("9aafe9de-9a02-49c9-b4db-2300055b0026")
                                            val fetchType = findFetchType(oneToManyAnnotationEntry, default = GFetchType.LAZY)
                                            kind = FieldKind.Many(mappedBy = type.decapitalize() + "." + mappedBy,
                                                                  fetchType = fetchType)
                                        } else {
                                            val manyToOneAnnotationEntry = prop.freakingFindAnnotation("GManyToOne")
                                            if (manyToOneAnnotationEntry != null) {
                                                val fetchType = findFetchType(manyToOneAnnotationEntry, default = GFetchType.EAGER)
                                                kind = FieldKind.One(fetchType = fetchType)
                                            }
                                        }

                                        val isEntity = type !in setOf("Int", "Long", "Boolean", "String", "XTimestamp", "ByteArray")
                                        val isInToString = !isEntity && type !in setOf("ByteArray")

                                        fields += FieldSpec(name = name,
                                                            type = type,
                                                            isEntity = isEntity,
                                                            isInToString = isInToString,
                                                            isOrderColumn = prop.freakingFindAnnotation("GIsOrderColumn") != null,
                                                            kind = kind)
                                            .also {noise(it.toVerticalString())}
                                    }
                                }

                                val repositoryClassName = entityName + "Repository"
                                val repoKlass = nameToKlass[repositoryClassName] ?: wtf("No class $repositoryClassName found    3d2c6366-163a-448b-a4fb-9e231321107c")
                                for (decl in repoKlass.declarations) {
                                    if (decl is KtFunction) {
                                        val func = decl
                                        val definedFinderName = func.name!!

                                        if (definedFinderName in setOf("save", "findAll", "delete")) continue

                                        var generatedFinderName by notNullOnce<String>()

                                        /*if (definedFinderName == "findAll") {
                                            generatedFinderName = definedFinderName
                                        }
                                        else*/ if (definedFinderName.startsWith("findBy")) {
                                            if (definedFinderName.contains("And")) imf("6653efd2-35ae-483a-b49d-0a6af6afac44")
                                            var shit = definedFinderName.substring("findBy".length)
                                            val operator = listOf(
                                                "LikeIgnoreCase",
                                                "Like").find {shit.endsWith(it)} ?: ""
                                            shit = shit.dropLast(operator.length)
                                            generatedFinderName = "findBy${entityName}_$shit$operator"
                                        }
                                        else {
                                            wtf("52cd36a0-9de1-436f-be73-86b166f202cf")
                                        }

                                        val params = mutableListOf<FinderParamSpec>()
                                        for (valueParameter in func.valueParameters) {
                                            params += FinderParamSpec(name = valueParameter.name!!,
                                                                      type = valueParameter.typeReference!!.text)
                                        }

                                        val typeText = func.typeReference!!.text
                                        val returnsList = typeText.startsWith("List<")
                                        val returnsNullable = typeText.endsWith("?")

                                        finders += FinderSpec(definedFinderName = definedFinderName,
                                                              generatedFinderName = generatedFinderName,
                                                              params = params,
                                                              returnsList = returnsList,
                                                              returnsNullable = returnsNullable)
                                            .also {noise(it.toVerticalString())}
                                    }
                                }

                                entities += EntitySpec(entityName, tableName, fields, finders)
                            }
                        }
                    }
                }
            }

        }
    }
}





