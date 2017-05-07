package vgrechka.spew

import org.jetbrains.kotlin.psi.*
import vgrechka.*
import vgrechka.BigPile.mangleUUID
import java.io.File
import java.util.*
import kotlin.properties.Delegates.notNull

annotation class GOptsBuilder

class OptsBuilderSpew : Spew {
    private var ktFile by notNullOnce<KtFile>()
    private var outputFilePath by notNullOnce<String>()
    private var out by notNullOnce<CodeShitter>()
    private var spewResults by notNullOnce<SpewResults>()

    override fun ignite(ktFile: KtFile, outputFilePath: String, spewResults: SpewResults) {
        this.ktFile = ktFile
        this.outputFilePath = outputFilePath
        this.spewResults = spewResults
        val file = File(outputFilePath)
        val code = StringBuilder()
        out = CodeShitter(code, indent = 0)

        analyzeAndShit()

        FilePile.backUp().ifExists().ignite(file)
        file.writeText(code.toString())
        clog("Written ${file.path}")
    }

    private fun noise(x: Any?) {
        if (true) clog(x)
    }

    private fun analyzeAndShit() {
        val packageName = (ktFile.packageDirective
            ?: wtf("512f3d89-eb0d-4653-b04f-686c6100a900"))
            .qualifiedName

        out.headerComment()
        out.line("""
                //
                // Generated on ${Date()}
                // Model: ${ktFile.virtualFile.path}
                //

                package $packageName

                import vgrechka.*
            """)
        out.line("")

        val nameToKlass = mutableMapOf<String, KtClass>()
        ktFile.freakingVisitClasses {
            nameToKlass[it.name!!] = it
        }

        for (klass in nameToKlass.values) {
            fun hasInnerClass(name: String) =
                klass.declarations.any {it is KtClass && it.name == name}

            val optsBuilderAnn = klass.freakingFindAnnotation("GOptsBuilder") ?: continue
            out.appendln("// Generated at ${mangleUUID("c90a18a2-0dbd-4513-adfa-8165932eedc7")}")
            out.appendln("abstract class Generated_BaseFor_${klass.name} {")
            out.appendln("    val optsMaking = Generated_OptsMaking()")
            out.appendln("    class Generated_OptsMaking {")

            class Fuck(val name: String, val type: String, val defaultValue: String?)
            val fucks = mutableListOf<Fuck>()

            val optsKlass = klass.declarations.find {it is KtClass && it.name == "Opts"} as KtClass
            for (param in optsKlass.primaryConstructor!!.valueParameters) {
//                if (param.name == "title")
//                    "break on me"

                val type = run {
                    val typeInSource = param.typeReference!!.text
                    val qualifiedType = StringBuilder()
                    var identBuf = StringBuilder()
                    fun identFinished() {
                        val ident = identBuf.toString()
                        qualifiedType.append(
                            if (hasInnerClass(ident))
                                "${klass.name}.$ident"
                            else
                                ident)
                        identBuf = StringBuilder()
                    }
                    for (c in typeInSource) {
                        if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9' || c == '_' || c == '$') {
                            identBuf.append(c)
                        } else {
                            identFinished()
                            qualifiedType.append(c)
                        }
                    }
                    identFinished()
                    var res = qualifiedType.toString()

                    if (res.contains("->"))
                        res = "($res)"

                    res
                }

                fucks += Fuck(name = param.name!!,
                              type = type,
                              defaultValue = param.defaultValue?.text)
            }

            for (fuck in fucks) {
                out.append("        var ${fuck.name}: ${fuck.type}")
                if (fuck.defaultValue == null) {
                    out.append("? = null")
                } else {
                    out.append(" = ")
                    val dot = fuck.defaultValue.indexOf(".")
                    if (dot != -1) {
                        val shitBeforeDot = fuck.defaultValue.substring(0, dot)
                        if (hasInnerClass(shitBeforeDot)) {
                            out.append("${klass.name}.")
                        }
                    }
                    out.append("${fuck.defaultValue}")
                }
                out.appendln()
            }

            out.appendln("    }")

            out.appendln()
            for (fuck in fucks) {
                out.appendln("    fun ${fuck.name}(x: ${fuck.type}): ${klass.name} {optsMaking.${fuck.name} = x; return this as ${klass.name}}")
            }

            out.appendln()
            out.appendln("    fun hardenOpts(): ${klass.name}.Opts {")
            out.appendln("        return ${klass.name}.Opts(")
            for ((i, fuck) in fucks.withIndex()) {
                out.append("            ${fuck.name} = optsMaking.${fuck.name}")
                if (fuck.defaultValue == null)
                    out.append("!!")
                if (i < fucks.lastIndex)
                    out.append(",")
                out.appendln()
            }
            out.appendln("        )")
            out.appendln("    }")

            out.appendln()
            out.appendln("    fun ignite() = ${klass.name}.Ignition(hardenOpts()).ignite()")

            out.appendln("}")
        }

        out.appendln()
    }
}














