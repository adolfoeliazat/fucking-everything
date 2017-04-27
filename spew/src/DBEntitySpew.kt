package vgrechka.spew

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtProperty
import vgrechka.*
import java.io.File
import java.util.*
import kotlin.properties.Delegates.notNull

annotation class GEntity(val table: String)
annotation class GOneToMany(val mappedBy: String)
annotation class GManyToOne

interface GCommonEntityFields {
    var id: Long
    var createdAt: XTimestamp
    var updatedAt: XTimestamp
    var deleted: Boolean
}


private sealed class FieldKind {
    class Simple : FieldKind()
    class One : FieldKind()
    data class Many(val mappedBy: String) : FieldKind()
}

private data class FieldSpec(
    val name: String,
    val type: String,
    val isEntity: Boolean,
    val isInCtorParams: Boolean,
    val isInToString: Boolean,
    val kind: FieldKind)

private data class FinderParamSpec(
    val name: String,
    val type: String)

private data class FinderSpec(
    val definedFinderName: String,
    val generatedFinderName: String,
    val params: List<FinderParamSpec>,
    val returnsList: Boolean)

private data class EntitySpec(
    val name: String,
    val tableName: String,
    val fields: List<FieldSpec>,
    val finders: List<FinderSpec>)



class DBEntitySpew : Spew {
    private var ktFile by notNullOnce<KtFile>()
    private var outputFilePath by notNullOnce<String>()
    private var out by notNullOnce<CodeShitter>()
    private var entity by notNull<EntitySpec>()
    private var en by notNullOnce<String>()
    private var end by notNullOnce<String>()
    private var spewResults by notNullOnce<SpewResults>()

    override fun ignite(ktFile: KtFile, outputFilePath: String, spewResults: SpewResults) {
        this.ktFile = ktFile
        this.outputFilePath = outputFilePath
        this.spewResults = spewResults
        val file = File(outputFilePath)
        val code = StringBuilder()
        out = CodeShitter(code, indent = 0)

//        val entities = fakeEntities()
        val entities = analyzeSources()

        val packageName = (ktFile.packageDirective
            ?: wtf("512f3d89-eb0d-4653-b04f-686c6100a900"))
            .qualifiedName

        shitHeader(out, packageName)
        out.linen("// Fuck you    ${Date()}")

        for (_entity in entities) {
            entity = _entity
            spitShitForEntity()
        }


        file.backUpAndWrite(code.toString())
    }

    fun spitShitForEntity() {
        en = entity.name
        end = en.decapitalize()
        out.smallSection(en)
        val fuck10 = "fun new$en("
        out.append(fuck10)
        val ctorParams = entity.fields.filter {it.isInCtorParams}
        for ((ctorParamIndex, ctorParam) in ctorParams.withIndex()) {
            out.append("${ctorParam.name}: ${ctorParam.type}")
            if (ctorParamIndex < ctorParams.lastIndex)
                out.append(",\n" + " ".repeat(fuck10.length))
        }
        out.append("): $en {\n")

        out.append(  "    val backing = Generated_$en(\n")
        val fuck20 = "        Generated_${en}Fields("
        out.append(fuck20)
        for ((ctorParamIndex, ctorParam) in ctorParams.withIndex()) {
            out.append("${ctorParam.name} = ${ctorParam.name}")
            if (ctorParam.isEntity)
                out.append("._backing")
            if (ctorParamIndex < ctorParams.lastIndex)
                out.append(",\n" + " ".repeat(fuck20.length))
        }
        out.append("))\n")
        out.append("    return backing.toManuallyDefinedInterface()\n")
        out.append("}\n\n")

        out.append("val $en._backing\n")
        out.append("    get() = (this as Generated_${en}BackingProvider)._backing\n\n")

        out.append("val ${end}Repo: ${en}Repository by lazy {\n")
        out.append("    val generatedRepo = backPlatform.springctx.getBean(Generated_${en}Repository::class.java)!!\n")
        out.append("\n")
        out.append("    object:${en}Repository {\n")
        out.append("        override fun save(x: $en): $en {\n")
        out.append("            val shit = generatedRepo.save(x._backing)\n")
        out.append("            return shit.toManuallyDefinedInterface()\n")
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

            out.append("        override fun ${finder.definedFinderName}($paramsCode): $returnTypeCode {\n")
            out.append("            val shit = generatedRepo.${finder.generatedFinderName}($generatedFinderArgsCode)\n")
            if (finder.returnsList) {
                out.append("            return shit.map {it.toManuallyDefinedInterface()}\n")
            } else {
                out.append("            return shit.toManuallyDefinedInterface()\n")
            }
            out.append("        }\n")
            if (finderIndex < entity.finders.lastIndex)
                out.append("\n")
        }

        out.append("    }\n")
        out.append("}\n\n")

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
                else -> "Generated_$en"
            }
            out.append("    fun ${finder.generatedFinderName}($paramsCode): $returnTypeCode\n")
        }
        out.append("}\n\n")

        out.append("interface Generated_${en}BackingProvider : DBCodeGenUtils.GeneratedBackingEntityProvider<Generated_$en> {\n")
        out.append("    override val _backing: Generated_$en\n")
        out.append("}\n\n")

        out.append("@XEntity @XTable(name = \"${entity.tableName}\")\n")
        out.append("class Generated_$en(\n")
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
                    out.append("                get() = _backing.$end.${field.name}\n")
                    out.append("                set(value) {_backing.$end.${field.name} = value}\n")
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
        out.append("class Generated_${en}Fields(\n")
        out.append("    @XEmbedded var common: CommonFields = CommonFields(),\n")
        for ((index, field) in entity.fields.withIndex()) {
            exhaustive=when (field.kind) {
                is FieldKind.Simple -> {
                    out.append("    @XColumn")
                    if (field.type == "String")
                        out.append("(columnDefinition = \"text\")")
                    out.append(" var ${field.name}: ${field.type}")
                }
                is FieldKind.One -> {
                    out.append("    @XManyToOne(fetch = XFetchType.LAZY) var ${field.name}: Generated_${field.type}")
                }
                is FieldKind.Many -> {
                    out.append("    @XOneToMany(fetch = XFetchType.LAZY, mappedBy = \"${field.kind.mappedBy}\") var ${field.name}: MutableList<Generated_${field.type}> = mutableListOf()")
                }
            }
            if (index < entity.fields.lastIndex)
                out.append(",")
            out.append("\n")
        }
        out.append(")\n")
        out.append("\n")

        spitDDLComment()

        /*
         */
    }

    fun spitDDLComment() {
        fun append(x: String) {
            out.append(x)
            spewResults.ddl.append(x)
        }

        out.append("\n\n")
        out.append("/*\n")
        out.append("DDL\n")
        out.append("===\n")
        out.append("\n")
        append("drop table if exists `${entity.tableName}`;\n")
        append("create table `${entity.tableName}` (\n")
        append("    `id` integer primary key autoincrement,\n")
        append("    `${end}_common_createdAt` text not null,\n")
        append("    `${end}_common_updatedAt` text not null,\n")
        append("    `${end}_common_deleted` integer not null,\n")
        for ((index, field) in entity.fields.withIndex()) {
            append("    `${end}_${field.name}` text not null")
            if (index < entity.fields.lastIndex)
                append(",")
            append("\n")
        }
        append(");\n")
        out.append("*/")

        /*
         */
    }

    private fun noise(x: Any?) {
        if (false) clog(x)
    }

    private fun analyzeSources(): List<EntitySpec> {
        val entities = mutableListOf<EntitySpec>()
        val nameToKlass = mutableMapOf<String, KtClass>()

        object {
            init {
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
                                        var isInCtorParams = true
                                        var kind: FieldKind = FieldKind.Simple()
                                        var type = prop.typeReference!!.text

                                        if (type.startsWith("MutableList<")) {
                                            type = type.substring("MutableList<".length, type.lastIndexOf(">"))
                                            val oneToManyAnnotationEntry = prop.freakingFindAnnotation("GOneToMany") ?: wtf("556650b2-91b5-45de-887f-81f87f701c49")
                                            val mappedBy = oneToManyAnnotationEntry.freakingGetStringAttribute("mappedBy") ?: wtf("9aafe9de-9a02-49c9-b4db-2300055b0026")
                                            kind = FieldKind.Many(mappedBy = type.decapitalize() + "." + mappedBy)
                                            isInCtorParams = false
                                        } else {
                                            if (prop.freakingFindAnnotation("GManyToOne") != null) {
                                                kind = FieldKind.One()
                                            }
                                        }

                                        val isEntity = type !in setOf("Int", "Long", "Boolean", "String", "XTimestamp")
                                        val isInToString = !isEntity

                                        fields += FieldSpec(name = name,
                                                            type = type,
                                                            isEntity = isEntity,
                                                            isInCtorParams = isInCtorParams,
                                                            isInToString = isInToString,
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

                                        if (definedFinderName == "save") continue

                                        var generatedFinderName by notNullOnce<String>()

                                        if (definedFinderName == "findAll") {
                                            generatedFinderName = definedFinderName
                                        }
                                        else if (definedFinderName.startsWith("findBy")) {
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

                                        val returnsList = func.typeReference!!.text.startsWith("List<")

                                        finders += FinderSpec(definedFinderName = definedFinderName,
                                                              generatedFinderName = generatedFinderName,
                                                              params = params,
                                                              returnsList = returnsList)
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


        return entities
    }


    private fun fakeEntities(): List<EntitySpec> {
        return listOf(
            EntitySpec(name = "AmazingWord",
                       tableName = "amazing_words",
                       fields = listOf(
                           FieldSpec(name = "word", type = "String", isEntity = false, isInCtorParams = true, isInToString = true, kind = FieldKind.Simple()),
                           FieldSpec(name = "rank", type = "Int", isEntity = false, isInCtorParams = true, isInToString = true, kind = FieldKind.Simple()),
                           FieldSpec(name = "comments", type = "AmazingComment", isEntity = true, isInCtorParams = false, isInToString = false, kind = FieldKind.Many(mappedBy = "amazingComment.word"))),
                       finders = listOf(
                           FinderSpec(definedFinderName = "findAll",
                                      generatedFinderName = "findAll",
                                      returnsList = true,
                                      params = listOf()),
                           FinderSpec(definedFinderName = "findByWordLikeIgnoreCase",
                                      generatedFinderName = "findByAmazingWord_WordLikeIgnoreCase",
                                      returnsList = true,
                                      params = listOf(
                                          FinderParamSpec(name = "x", type = "String")
                                      )))),
            EntitySpec(name = "AmazingComment",
                       tableName = "amazing_comments",
                       fields = listOf(
                           FieldSpec(name = "author", type = "String", isEntity = false, isInCtorParams = true, isInToString = true, kind = FieldKind.Simple()),
                           FieldSpec(name = "content", type = "String", isEntity = false, isInCtorParams = true, isInToString = true, kind = FieldKind.Simple()),
                           FieldSpec(name = "word", type = "AmazingWord", isEntity = true, isInCtorParams = true, isInToString = false, kind = FieldKind.One())),
                       finders = listOf(
                           FinderSpec(definedFinderName = "findAll",
                                      generatedFinderName = "findAll",
                                      returnsList = true,
                                      params = listOf()))))
    }

    fun shitHeader(out: CodeShitter, packageName: String) {
        out.headerComment()
        out.line("""
                package $packageName

                import kotlin.reflect.KClass
                import vgrechka.*
                import vgrechka.spew.*
                import vgrechka.db.*
            """)
        out.line("")
    }
}




