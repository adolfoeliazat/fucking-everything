package vgrechka.spew

import org.jetbrains.kotlin.psi.*
import org.jgrapht.alg.CycleDetector
import vgrechka.*
import vgrechka.BigPile.mangleUUID
import vgrechka.spew.GDBEntitySpewDatabaseDialect.*
import java.io.File
import java.util.*
import kotlin.Comparator
import kotlin.properties.Delegates.notNull
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.traverse.TopologicalOrderIterator


@Target(AnnotationTarget.FILE)
annotation class GDBEntitySpewOptions(
    val pileObject: String = "",
    val databaseDialect: GDBEntitySpewDatabaseDialect)

enum class GDBEntitySpewDatabaseDialect {
    SQLITE, POSTGRESQL
}

annotation class GEntity(val table: String)
annotation class GOneToMany(val mappedBy: String, val fetch: GFetchType = GFetchType.LAZY)
annotation class GManyToOne(val fetch: GFetchType = GFetchType.EAGER)
annotation class GIsOrderColumn

enum class GFetchType {LAZY, EAGER}

interface GCommonEntityFields {
    var id: Long
    var createdAt: XTimestamp
    var updatedAt: XTimestamp
    var deleted: Boolean
}

interface GRepository<Entity : GCommonEntityFields> {
    fun findOne(id: Long): Entity?
    fun findAll(): List<Entity>
    fun save(x: Entity): Entity
    fun delete(id: Long)
    fun delete(x: Entity)
}

private sealed class FieldKind {
    class Simple : FieldKind()
    class One(val fetchType: GFetchType) : FieldKind()
    data class Many(val mappedBy: String, val fetchType: GFetchType) : FieldKind()
}

private data class FieldSpec(
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

private data class FinderParamSpec(
    val name: String,
    val type: String)

private data class FinderSpec(
    val definedFinderName: String,
    val generatedFinderName: String,
    val params: List<FinderParamSpec>,
    val returnsList: Boolean,
    val returnsNullable: Boolean)

private data class EntitySpec(
    val name: String,
    val tableName: String,
    val fields: List<FieldSpec>,
    val finders: List<FinderSpec>)



class DBEntitySpew : Spew {
    // TODO:vgrechka Extract logic into one-off class constructed in `ignite` (to avoid all these `notNull`s)

    private var ktFile by notNullOnce<KtFile>()
    private var outputFilePath by notNullOnce<String>()
    private var out by notNullOnce<CodeShitter>()
    private var entity by notNull<EntitySpec>()
    private var en by notNull<String>()
    private var end by notNull<String>()
    private var spewResults by notNullOnce<SpewResults>()
    private var entities by notNull<List<EntitySpec>>()
    private var pileObjectName: String? = null
    private var databaseDialect by notNullOnce<GDBEntitySpewDatabaseDialect>()
    private val tableDependsOnTable = mutableListOf<Pair<String, String>>()

    override fun ignite(ktFile: KtFile, outputFilePath: String, spewResults: SpewResults) {
        this.ktFile = ktFile
        this.outputFilePath = outputFilePath
        this.spewResults = spewResults
        val file = File(outputFilePath)
        out = CodeShitter()

//        val entities = fakeEntities()
        analyzeSources()

        val packageName = (ktFile.packageDirective
            ?: wtf("512f3d89-eb0d-4653-b04f-686c6100a900"))
            .qualifiedName

        shitHeader(out, packageName)

        for (_entity in entities) {
            entity = _entity
            spitShitForEntity()
        }

        sortDDLStatementGeneratorsAndRunThem()
        spitStuffClass()
        spitDDLComment()

        FilePile.backUp().ifExists().ignite(file)
        file.writeText(out.reify())
        clog("Written ${file.path}")
    }

    fun spitShitForEntity() {
        en = entity.name
        end = en.decapitalize()
        out.smallSection(en)
        out.append("// Generated at ${mangleUUID("7470173f-49ef-43cb-adf7-1c395f07518c")}\n")
        val fuck10 = "fun new$en("
        out.append(fuck10)
        val ctorParams = entity.fields.filter {it.kind !is FieldKind.Many && !it.isOrderColumn}
        for ((ctorParamIndex, ctorParam) in ctorParams.withIndex()) {
            out.append("${ctorParam.name}: ${ctorParam.type}")
            if (ctorParamIndex < ctorParams.lastIndex)
                out.append(",\n" + " ".repeat(fuck10.length))
        }
        out.append("): $en {\n")

        out.append(  "    val backing = Generated_$en(\n")
        val fuck20 = "        Generated_${en}Fields().also {"
        out.append(fuck20)
        for ((ctorParamIndex, ctorParam) in ctorParams.withIndex()) {
            out.append("it.${ctorParam.name} = ${maybeWrapShitIntoPlatformType(ctorParam, ctorParam.name)}")
            if (ctorParam.isEntity)
                out.append("._backing")
            if (ctorParamIndex < ctorParams.lastIndex)
                out.append("\n" + " ".repeat(fuck20.length))
        }
        out.append("})\n")
        out.append("    return backing.toManuallyDefinedInterface()\n")
        out.append("}\n\n")

        out.append("val $en._backing\n")
        out.append("    get() = (this as Generated_${en}BackingProvider)._backing\n\n")

        spitRepo()
        spitGeneratedRepo()

        out.append("interface Generated_${en}BackingProvider : DBCodeGenUtils.GeneratedBackingEntityProvider<Generated_$en> {\n")
        out.append("    override val _backing: Generated_$en\n")
        out.append("}\n\n")

        out.append("@XEntity @XTable(name = \"${entity.tableName}\")\n")
        out.append("class Generated_$en( // Generated at ${mangleUUID("f21265f2-3d69-4ab8-a07c-5595106a9e6b")}\n")
        out.append("    @XEmbedded var $end: Generated_${en}Fields\n")
        out.append(")\n")
        out.append("    : ClitoralEntity0(), DBCodeGenUtils.GeneratedEntity<$en>\n")
        out.append("{\n")
        out.append("    override fun toManuallyDefinedInterface(): $en {\n")
        out.append("        return object : $en, Generated_${en}BackingProvider {\n")
        out.append("            override val _backing: Generated_$en\n")
        out.append("                get() = this@Generated_$en\n")
        out.append("\n")
        out.append("            override var id: Long\n")
        out.append("                get() = _backing.id!!\n")
        out.append("                set(value) {_backing.id = value}\n")
        out.append("\n")
        out.append("            override var createdAt: XTimestamp\n")
        out.append("                get() = _backing.$end.common.createdAt\n")
        out.append("                set(value) {_backing.$end.common.createdAt = value}\n")
        out.append("\n")
        out.append("            override var updatedAt: XTimestamp\n")
        out.append("                get() = _backing.$end.common.updatedAt\n")
        out.append("                set(value) {_backing.$end.common.updatedAt = value}\n")
        out.append("\n")
        out.append("            override var deleted: Boolean\n")
        out.append("                get() = _backing.$end.common.deleted\n")
        out.append("                set(value) {_backing.$end.common.deleted = value}\n")
        out.append("\n")

        for (field in entity.fields) {
            exhaustive=when (field.kind) {
                is FieldKind.Simple -> {
                    out.append("            override var ${field.name}: ${field.type}\n")
                    out.append("                get() = ${maybeUnwrapShitFromPlatformType(field, "_backing.$end.${field.name}")}\n")
                    out.append("                set(value) {_backing.$end.${field.name} = ${maybeWrapShitIntoPlatformType(field, "value")}}\n")
                    out.append("\n")
                }
                is FieldKind.One -> {
                    out.append("            override var ${field.name}: ${field.type}\n")
                    out.append("                get() = _backing.$end.${field.name}.toManuallyDefinedInterface()\n")
                    out.append("                set(value) {_backing.$end.${field.name} = value._backing}\n")
                    out.append("\n")
                }
                is FieldKind.Many -> {
                    out.append("            override var ${field.name}: MutableList<${field.type}>\n")
                    out.append("                by DBCodeGenUtils.FuckingList(getBackingList = {_backing.$end.${field.name}})\n")
                    out.append("\n")
                }
            }
        }

        out.append("            override fun toString() = _backing.toString()\n")
        out.append("\n")
        out.append("            override fun hashCode() = _backing.hashCode()\n")
        out.append("\n")
        out.append("            override fun equals(other: Any?): Boolean {\n")
        out.append("                val otherShit = other as? Generated_${en}BackingProvider ?: return false\n")
        out.append("                return _backing == otherShit._backing\n")
        out.append("            }\n")
        out.append("        }\n")
        out.append("    }\n")
        out.append("\n")
        out.append("    override fun toString(): String {\n")


        out.append("        return \"$en(")
        val toStringFields = entity.fields.filter {it.isInToString}
        for ((index, field) in toStringFields.withIndex()) {
            out.append("${field.name}=\${$end.${field.name}}")
            if (index < toStringFields.lastIndex)
                out.append(", ")
        }
        out.append(")\"\n")

        out.append("    }\n")
        out.append("}\n")
        out.append("\n")

        out.append("@XEmbeddable\n")
        out.append("class Generated_${en}Fields { // Generated at ${mangleUUID("2e91acff-5613-4b14-b71e-5edee254d029")}\n")
        out.append("    @XEmbedded var common: CommonFields = CommonFields()\n")
        for ((index, field) in entity.fields.withIndex()) {
            exhaustive=when (field.kind) {
                is FieldKind.Simple -> {
                    out.append("    @XColumn")
                    if (field.type == "String")
                        out.append("(columnDefinition = \"text\")")
                    if (field.type.endsWith("?"))
                        out.append(" var ${field.name}: ${field.type} = null")
                    else
                        out.append(" lateinit var ${field.name}: ${field.platformType()}")
                }
                is FieldKind.One -> {
                    out.append("    @XManyToOne(fetch = XFetchType.${field.kind.fetchType.name}/*, cascade = arrayOf(XCascadeType.ALL)*/)")
                    out.append(" lateinit var ${field.name}: Generated_${field.type}")
                }
                is FieldKind.Many -> {
                    out.append("    @XOneToMany(fetch = XFetchType.${field.kind.fetchType.name}, mappedBy = \"${field.kind.mappedBy}\", cascade = arrayOf(XCascadeType.ALL), orphanRemoval = true)")
                    val entity = entities.find {it.name == field.type} ?: wtf("68c50078-c088-476b-9a52-7c1657412fc3")
                    val orderColumnField = entity.fields.find {it.isOrderColumn}
                    if (orderColumnField != null) {
                        out.append(" @XOrderColumn(name = \"${field.type.decapitalize()}_${orderColumnField.name}\")")
                    }
                    out.append(" var ${field.name}: MutableList<Generated_${field.type}> = mutableListOf()")
                }
            }
//            if (index < entity.fields.lastIndex)
//                out.append(",")
            out.append("\n")
        }
        out.append("}\n")
        out.append("\n")

        generateDDLForEntity()
    }

    private fun maybeUnwrapShitFromPlatformType(typed: FieldSpec, shit: String): String {
        val platformType = typed.platformType()
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

    private fun maybeWrapShitIntoPlatformType(typed: FieldSpec, shit: String): String {
        val platformType = typed.platformType()
        return when {
            platformType != typed.type -> "$platformType($shit)"
            else -> shit
        }
    }

    private fun FieldSpec.platformType(): String {
        val platformFieldType = when (type) {
            "Int" -> "java.lang.Integer"
            "Long" -> "java.lang.Long"
            "Boolean" -> "java.lang.Boolean"
            else -> type
        }
        return platformFieldType
    }

    private fun spitGeneratedRepo() {
        out.append("interface Generated_${en}Repository : XCrudRepository<Generated_$en, Long> {\n")
        for (finder in entity.finders) {
            if (finder.definedFinderName in setOf("findAll")) continue

            val paramsCode = StringBuilder()
            for ((paramIndex, param) in finder.params.withIndex()) {
                paramsCode += "${param.name}: ${param.type}"
                if (paramIndex < finder.params.lastIndex) {
                    paramsCode += ", "
                }
            }
            val returnTypeCode = when {
                finder.returnsList -> "List<Generated_$en>"
                else -> "Generated_$en${maybeQuestion(finder)}"
            }
            out.append("    fun ${finder.generatedFinderName}($paramsCode): $returnTypeCode\n")
        }
        out.append("}\n\n")
    }

    private fun spitRepo() {
        out.append("val ${end}Repo: ${en}Repository by lazy {\n")
        out.append("    val generatedRepo = backPlatform.springctx.getBean(Generated_${en}Repository::class.java)!!\n")
        out.append("\n")
        out.append("    object:${en}Repository {\n")
        out.append("        override fun findOne(id: Long): $en? {\n")
        out.append("            val shit = generatedRepo.findOne(id)\n")
        out.append("            return shit?.toManuallyDefinedInterface()\n")
        out.append("        }\n")
        out.append("\n")
        out.append("        override fun findAll(): List<$en> {\n")
        out.append("            val shit = generatedRepo.findAll()\n")
        out.append("            return shit.map {it.toManuallyDefinedInterface()}\n")
        out.append("        }\n")
        out.append("\n")
        out.append("        override fun save(x: $en): $en {\n")
        out.append("            val shit = generatedRepo.save(x._backing)\n")
        out.append("            return shit.toManuallyDefinedInterface()\n")
        out.append("        }\n")
        out.append("\n")
        out.append("        override fun delete(id: Long) {\n")
        out.append("            generatedRepo.delete(id)\n")
        out.append("        }\n")
        out.append("\n")
        out.append("        override fun delete(x : $en) {\n")
        out.append("            generatedRepo.delete(x._backing)\n")
        out.append("        }\n")
        out.append("\n")

        for ((finderIndex, finder) in entity.finders.withIndex()) {
            val paramsCode = StringBuilder()
            val generatedFinderArgsCode = StringBuilder()
            for ((paramIndex, param) in finder.params.withIndex()) {
                paramsCode += "${param.name}: ${param.type}"
                generatedFinderArgsCode += param.name
                if (paramIndex < finder.params.lastIndex) {
                    paramsCode += ", "
                    generatedFinderArgsCode += ", "
                }
            }
            val returnTypeCode = when {
                finder.returnsList -> "List<$en>"
                else -> en
            }

            out.append("        override fun ${finder.definedFinderName}($paramsCode): $returnTypeCode${maybeQuestion(finder)} {\n")
            out.append("            val shit = generatedRepo.${finder.generatedFinderName}($generatedFinderArgsCode)\n")
            if (finder.returnsList) {
                out.append("            return shit${maybeQuestion(finder)}.map {it.toManuallyDefinedInterface()}\n")
            } else {
                out.append("            return shit${maybeQuestion(finder)}.toManuallyDefinedInterface()\n")
            }
            out.append("        }\n")
            if (finderIndex < entity.finders.lastIndex)
                out.append("\n")
        }

        out.append("    }\n")
        out.append("}\n\n")
    }

    private fun maybeQuestion(finder: FinderSpec) = finder.returnsNullable.thenElseEmpty {"?"}

    fun sortDDLStatementGeneratorsAndRunThem() {
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

        val tables = Iterable {TopologicalOrderIterator(g)}.toList()
        for (table in tables)
            drops.find {it.table == table}!!.doAppend()
        for (table in tables.reversed())
            creates.find {it.table == table}!!.doAppend()
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

    class DDLGenerator(val table: String, val doAppend: () -> Unit)
    val drops = mutableListOf<DDLGenerator>()
    val creates = mutableListOf<DDLGenerator>()

    fun generateDDLForEntity() {
        val entity = entity.copy()
        val end = end

        fun append(x: String) = spewResults.ddl.append(x)

        fun quote(s: String) = when (databaseDialect) {
            SQLITE -> "`$s`"
            POSTGRESQL -> '"' + s + '"'
        }

        val idColumnDefinition = when (databaseDialect) {
            SQLITE -> "id integer primary key autoincrement"
            POSTGRESQL -> "id bigserial primary key"
        }

        val quotedTableName = quote(entity.tableName)

        val foreignKeyFields = entity.fields.filter {it.kind is FieldKind.One}
        for (field in foreignKeyFields) {
            val oneEntity = entities.find {it.name == field.typeWithoutQuestion()} ?: wtf("d8beb50d-dc3f-42ab-8a64-16fb61ecfc42")
            tableDependsOnTable += entity.tableName to oneEntity.tableName
        }

        drops += DDLGenerator(
            table = entity.tableName,
            doAppend = {
                append("drop table if exists $quotedTableName;\n")
            }
        )

        val timestampType = when (databaseDialect) {
            GDBEntitySpewDatabaseDialect.SQLITE -> "text"
            GDBEntitySpewDatabaseDialect.POSTGRESQL -> "timestamp"
        }
        val booleanType = when (databaseDialect) {
            GDBEntitySpewDatabaseDialect.SQLITE -> "integer"
            GDBEntitySpewDatabaseDialect.POSTGRESQL -> "boolean"
        }

        creates += DDLGenerator(
            table = entity.tableName,
            doAppend = {
                append("create table $quotedTableName (\n")
                append("    $idColumnDefinition,\n")
                append("    ${end}_common_createdAt $timestampType not null,\n")
                append("    ${end}_common_updatedAt $timestampType not null,\n")
                append("    ${end}_common_deleted $booleanType not null,\n")
                val fields = entity.fields.filter {it.kind !is FieldKind.Many}

                for ((index, field) in fields.withIndex()) {
                    val sqlType = when (field.kind) {
                        is FieldKind.Simple -> {
                            when (field.type) {
                                "Int" -> when (databaseDialect) {
                                    SQLITE -> "integer not null"
                                    POSTGRESQL -> "integer not null"
                                }
                                "Int?" -> when (databaseDialect) {
                                    SQLITE -> "integer"
                                    POSTGRESQL -> "integer"
                                }
                                "Long" -> when (databaseDialect) {
                                    SQLITE -> "bigint not null"
                                    POSTGRESQL -> "bigint not null"
                                }
                                "Long?" -> when (databaseDialect) {
                                    SQLITE -> "bigint"
                                    POSTGRESQL -> "bigint"
                                }
                                "String" -> when (databaseDialect) {
                                    SQLITE -> "text not null"
                                    POSTGRESQL -> "text not null"
                                }
                                "String?" -> when (databaseDialect) {
                                    SQLITE -> "text"
                                    POSTGRESQL -> "text"
                                }
                                "ByteArray" -> when (databaseDialect) {
                                    SQLITE -> "blob not null"
                                    POSTGRESQL -> "bytea not null"
                                }
                                "ByteArray?" -> when (databaseDialect) {
                                    SQLITE -> "blob"
                                    POSTGRESQL -> "bytea"
                                }
                                else -> wtf("field.type = ${field.type}    5e84c6fb-b523-43cc-aa45-bdef1dca7ff2")
                            }
                        }
                        is FieldKind.One -> {
                            val s = when (databaseDialect) {
                                SQLITE -> "bigint"
                                POSTGRESQL -> "bigint"
                            }
                            s + (!field.isNullable()).thenElseEmpty{" not null"}
                        }
                        is FieldKind.Many -> wtf("0f79f787-be13-49ba-ac1d-6855476605da")
                    }

                    append("    ${end}_${field.name}${(field.kind is FieldKind.One).thenElseEmpty{"__id"}} $sqlType")
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

                append("\n);\n")

                if (databaseDialect == POSTGRESQL) {
                    foreignKeyFields.forEach {field->
                        append("create index on $quotedTableName (${end}_${field.name}__id);\n")
                    }
                }

                append("\n")
            }
        )
    }

    private fun noise(x: Any?) {
        if (false) clog(x)
    }

    private fun analyzeSources() {
        entities = mutableListOf<EntitySpec>()
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


//    private fun fakeEntities(): List<EntitySpec> {
//        return listOf(
//            EntitySpec(name = "AmazingWord",
//                       tableName = "amazing_words",
//                       fields = listOf(
//                           FieldSpec(name = "word", type = "String", isEntity = false, isInCtorParams = true, isInToString = true, kind = FieldKind.Simple()),
//                           FieldSpec(name = "rank", type = "Int", isEntity = false, isInCtorParams = true, isInToString = true, kind = FieldKind.Simple()),
//                           FieldSpec(name = "comments", type = "AmazingComment", isEntity = true, isInCtorParams = false, isInToString = false, kind = FieldKind.Many(mappedBy = "amazingComment.word", fetchType = GFetchType.LAZY))),
//                       finders = listOf(
//                           FinderSpec(definedFinderName = "findAll",
//                                      generatedFinderName = "findAll",
//                                      returnsList = true,
//                                      returnsNullable = false,
//                                      params = listOf()),
//                           FinderSpec(definedFinderName = "findByWordLikeIgnoreCase",
//                                      generatedFinderName = "findByAmazingWord_WordLikeIgnoreCase",
//                                      returnsList = true,
//                                      returnsNullable = false,
//                                      params = listOf(
//                                          FinderParamSpec(name = "x", type = "String")
//                                      )))),
//            EntitySpec(name = "AmazingComment",
//                       tableName = "amazing_comments",
//                       fields = listOf(
//                           FieldSpec(name = "author", type = "String", isEntity = false, isInCtorParams = true, isInToString = true, kind = FieldKind.Simple()),
//                           FieldSpec(name = "content", type = "String", isEntity = false, isInCtorParams = true, isInToString = true, kind = FieldKind.Simple()),
//                           FieldSpec(name = "word", type = "AmazingWord", isEntity = true, isInCtorParams = true, isInToString = false, kind = FieldKind.One(fetchType = GFetchType.EAGER))),
//                       finders = listOf(
//                           FinderSpec(definedFinderName = "findAll",
//                                      generatedFinderName = "findAll",
//                                      returnsList = true,
//                                      returnsNullable = false,
//                                      params = listOf()))))
//    }

    fun shitHeader(out: CodeShitter, packageName: String) {
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
                import vgrechka.db.*
            """)
        out.line("")
    }
}




