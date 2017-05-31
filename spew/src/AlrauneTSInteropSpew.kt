package vgrechka.spew

import alraune.shared.AlUADocumentCategories
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtFile
import vgrechka.*
import java.io.File
import java.util.*

class AlrauneTSInteropSpew : Spew {
    override fun ignite(ktFile: KtFile, outputFilePath: String, spewResults: SpewResults) {
        GlobalSpewContext.opts = SpewForInputFileOptions(annotations = true)
        object {
            val out = CodeShitter()
            val ln = out::ln

            init {
                val file = File(outputFilePath)

                out.headerComment()
                ln("//")
                ln("// Generated on ${Date()}")
                ln("// Model: ${ktFile.virtualFile.path}")
                ln("//")
                ln("")
                ln("namespace alraune {")

                val nameToKlass = mutableMapOf<String, KtClass>()
                ktFile.freakingVisitClasses {
                    nameToKlass[it.name!!] = it
                }

                for (klass in nameToKlass.values) {
                    ln("")
                    if (klass.isEnum()) {
                        val gen = BeginLiteralUnionType(klass.name!!)
                        for (declaration in klass.declarations) {
                            if (declaration is KtEnumEntry) {
                                gen.member(declaration.name!!)
                            }
                        }
                    } else {
                        ln("    export interface ${klass.name} {")
                        for (property in klass.getProperties()) {
                            ln("        ${property.name}: ${ktTypeToTSType(property.typeReference!!.text)}")
                        }
                        klass.primaryConstructor?.valueParameters?.let {
                            for (param in it) {
                                ln("        ${param.name}: ${ktTypeToTSType(param.typeReference!!.text)}")
                            }
                        }
                        ln("    }")
                    }
                }

                run {
                    val forClass = "AlFrontToBackCommandPile"
                    ln("")
                    val gen = BeginLiteralUnionType("${forClass}Prop")
                    val ktClass = nameToKlass[forClass]!!
                    val params = ktClass.primaryConstructor!!.valueParameters
                    for (param in params) {
                        gen.member(param.name!!)
                    }
                }

                run {
                    ln("")
                    ln("    export interface AlUADocumentCategory {")
                    ln("        id: string")
                    ln("        title: string")
                    ln("        children: AlUADocumentCategory[]")
                    ln("    }")
                    ln("")

                    ln("    export class AlUADocumentCategories {")
                    ln("        static miscID = \"102\"")
                    ln("        static humanitiesID = \"226\"")
                    ln("        static linguisticsID = \"238\"")
                    ln("        static technicalID = \"174\"")
                    ln("        static programmingID = \"186\"")
                    ln("")
                    ln("        static root: AlUADocumentCategory = ")
                    fun descend(cat: AlUADocumentCategories.Category, indent: Int) {
                        out.append(" ".repeat(indent))
                        out.append("{id: \"${cat.id}\", title: \"${cat.title}\", children: [")
                        if (cat.children.isNotEmpty())
                            ln("")
                        for (child in cat.children) {
                            descend(child, indent + 4)
                        }
                        if (cat.children.isNotEmpty())
                            out.append(" ".repeat(indent))
                        ln("]},")
                    }
                    descend(AlUADocumentCategories.root, indent = 12)
                    out.deleteLastCommaBeforeNewLine()
                    ln("    }")

                }

                ln("}")


                FilePile.backUp().ifExists().ignite(file)
                file.writeText(out.reify() + GlobalSpewContext.maybeDebugInfo())
                clog("Written ${file.path}")
            }

            private fun ktTypeToTSType(_text: String): String {
                var text = _text
                if (text.endsWith("?"))
                    text = text.dropLast(1)
                if (text == "String")
                    return "string"
                if (text == "Boolean")
                    return "boolean"
                if (text == "Int")
                    return "number"
                run {
                    val re = Regex("(?:Mutable)?List<(.*?)>")
                    re.matchEntire(text)?.let {
                        return ktTypeToTSType(it.groupValues[1]) + "[]"
                    }
                }
                return text
            }

            inner class BeginLiteralUnionType(name: String) {
                var first = true

                init {
                    ln("    export type $name =")
                }

                fun member(name: String) {
                    val prefix = when {
                        first -> {
                            first = false
                            "  "
                        }
                        else -> "| "
                    }
                    ln("        $prefix\"$name\"")
                }
            }
        }

    }
}



