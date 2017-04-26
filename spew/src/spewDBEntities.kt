package vgrechka.spew

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtVisitor
import vgrechka.*
import java.io.File
import java.time.LocalDateTime
import java.util.*

sealed class FieldKind {
    class Simple : FieldKind()
    class One : FieldKind()
    class Many(val mappedBy: String) : FieldKind()
}

class FieldSpec(val name: String,
                val type: String,
                val isEntity: Boolean,
                val isInCtorParams: Boolean,
                val isInToString: Boolean,
                val kind: FieldKind)

class FinderParamSpec(val name: String,
                      val type: String)

class FinderSpec(val definedFinderName: String,
                 val generatedFinderName: String,
                 val params: List<FinderParamSpec>,
                 val returnsList: Boolean)

class EntitySpec(val name: String,
                 val tableName: String,
                 val fields: List<FieldSpec>,
                 val finders: List<FinderSpec>)

class spewDBEntities {
    init {
        val file = File(BigPile.fuckingEverythingRoot + "/spew-gen-tests/gen/generated--for-GeneratedEntitiesForAmazingWordsTest.kt")
        val code = StringBuilder()
        val out = Shitter(code, indent = 0)

        val analysisResult = try {
            clog("Working like a dog, analyzing your crappy sources...")
            FuckedCLICompiler.doMain(FuckedK2JVMCompiler(), arrayOf(
                BigPile.fuckingEverythingRoot + "/spew-gen-tests/src/GeneratedEntitiesForAmazingWordsTest.kt"
            ))
            wtf("61ea9f24-7e40-45d8-a858-e357cccff2a0")
        } catch (e: EnoughFuckedCompiling) {
            e
        }

//        val filesLeft = analysisResult.environment.getSourceFiles().toMutableSet()
//        val ktFile = filesLeft.first()

        shitHeaderComment(out, "vgrechka.spewgentests")

        val ktFile = analysisResult.environment.getSourceFiles().first()

        ktFile.accept(object:KtVisitor<Unit, Unit>() {
            override fun visitKtFile(file: KtFile, data: Unit?) {
                for (decl in file.declarations) {
                    decl.accept(this)
                }
            }

            override fun visitClass(klass: KtClass, data: Unit?) {
                if (klass.isInterface()) {
                    for (annotationEntry in klass.annotationEntries) {
                        val chars = annotationEntry.node.chars
                        // clog("Annotation:", chars)
                        if (chars == "@PersistentShit") {
                            val fuckingName = klass.name
                        }
                    }
                }
            }

//            override fun visitNamedFunction(function: KtNamedFunction, data: Unit?) {
//                // clog("Function:", function.name)
//                for (annotationEntry in function.annotationEntries) {
//                    val chars = annotationEntry.node.chars
//                    // clog("Annotation:", chars)
//                    if (chars == "@Remote")
//                        processRemoteFunction(function)
//                }
//            }
        })

//        check(filesLeft.isEmpty()){"489985a4-eeeb-4b83-bc0d-d704a77ed308"}

        out.linen("// Fuck you    ${Date()}")


        val entities = listOf(
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

        for (entity in entities) {
            val en = entity.name
            val end = en.decapitalize()
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

            /*
             */
        }


        backUpAndWrite(file, code.toString())
    }

    fun shitHeaderComment(shit: Shitter, packageName: String) {
        shit.line("""
                /*
                 * (C) Copyright 2017 Vladimir Grechka
                 *
                 * YOU DON'T MESS AROUND WITH THIS SHIT, IT WAS GENERATED BY A TOOL SMARTER THAN YOU
                 */

                package $packageName

                import kotlin.reflect.KClass
                import vgrechka.*
                import vgrechka.spew.*
                import vgrechka.db.*
            """)
        shit.line("")
    }

    private fun backUpAndWrite(file: File, newCode: String) {
        backUpIfExists(file)
        file.writeText(newCode)
        clog("Written ${file.path}")
    }


    private fun backUpIfExists(file: File) {
        if (!file.exists()) return

        check(file.path.replace("\\", "/").startsWith(BigPile.fuckingEverythingRoot + "/")) {"9911cfc6-6435-4a54-aa74-ad492162181a"}

        val stamp = LocalDateTime.now().format(PG_LOCAL_DATE_TIME).replace(Regex("[ :\\.]"), "-")
        val outPath = (
            BigPile.spewBak + "/" +
                file.path
                    .substring(BigPile.fuckingEverythingRoot.length)
                    .replace("\\", "/")
                    .replace(Regex("^/"), "")
                    .replace("/", "--")
                + "----$stamp"
            )

        // clog("Backing up: $outPath")
        File(outPath).writeText(file.readText())
    }
}

class Shitter(val output: StringBuilder, val indent: Int) {
    fun append(text: String) {
        output += text
    }

    fun line(text: String, numNewlines: Int = 1) {
        var s = dedent(text)
        s = reindent(indent, s)
        if (!s.endsWith("\n")) s += "\n".repeat(numNewlines)
        output += s
    }

    fun linen(text: String) {
        line(text, numNewlines = 2)
    }

    fun bigSection(title: String) {
        line("")
        line("// ==================================================================")
        line("// $title")
        line("// ==================================================================")
        line("")
    }

    fun smallSection(title: String) {
        line("// ------------------------------------------------------------------")
        line("// $title")
        line("// ------------------------------------------------------------------")
        line("")
    }
}


fun dedent(it: String): String {
    var lines = it.split(Regex("\\r?\\n"))
    if (lines.size > 0 && lines[0].isBlank()) {
        lines = lines.drop(1)
    }
    if (lines.size > 0 && lines.last().isBlank()) {
        lines = lines.dropLast(1)
    }

    var minIndent = 9999 // TODO:vgrechka Platform-specific max integer (for JS: Number.MAX_SAFE_INTEGER)
    for (line in lines) {
        if (!line.isBlank()) {
            val lineIndent = line.length - line.trimStart().length
            if (lineIndent < minIndent) {
                minIndent = lineIndent
            }
        }
    }

    return lines.map {line ->
        if (line.trim().isBlank()) ""
        else line.substring(minIndent)
    }.joinToString("\n")
}

fun reindent(newIndent: Int, it: String): String {
    return dedent(it).split("\n").joinToString("\n") {" ".repeat(newIndent) + it}
}


