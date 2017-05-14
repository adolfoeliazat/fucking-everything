package vgrechka.spew

import org.jetbrains.kotlin.psi.*
import vgrechka.*
import vgrechka.BigPile.mangleUUID
import java.io.File
import java.util.*

annotation class GOptsBuilder

class OptsBuilderSpew : Spew {
    private var ktFile by notNullOnce<KtFile>()
    private var outputFilePath by notNullOnce<String>()
    private var out by notNullOnce<CodeShitter>()
    private var spewResults by notNullOnce<SpewResults>()
    private var packageName by notNullOnce<String>()
    private val fileClasses = mutableListOf<FileClass>()

    class FileClass(val klass: KtClass, val qualifier: String)

    class Fuck(val name: String,
               val typeInSource: String,
               val type: String,
               val defaultValue: String?)

    override fun ignite(ktFile: KtFile, outputFilePath: String, spewResults: SpewResults) {
        this.ktFile = ktFile
        this.outputFilePath = outputFilePath
        this.spewResults = spewResults
        val file = File(outputFilePath)
        out = CodeShitter(indent = 0)

        analyzeAndShit()

        FilePile.backUp().ifExists().ignite(file)
        file.writeText(CodeShitter.foldSpacers(out.reify()))
        clog("Written ${file.path}")
    }

    private fun noise(x: Any?) {
        if (true) clog(x)
    }

    private fun analyzeAndShit() {
        packageName = (ktFile.packageDirective
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

        fun descend(el: KtDeclarationContainer, qualifier: String) {
            for (decl in el.declarations) {
                if (decl is KtClass) {
                    fileClasses += FileClass(decl, qualifier)
                    descend(decl, qualifier = qualifier + "." + decl.name)
                }
            }
        }
        descend(ktFile, qualifier = packageName)

        for (fc in fileClasses) {
            out.appendln("import ${fc.qualifier}.${fc.klass.name}.*")
        }

        out.line("")

        val nameToKlass = mutableMapOf<String, KtClass>()
        ktFile.freakingVisitClasses {
            nameToKlass[it.name!!] = it
        }

        for (klass in nameToKlass.values) {
            val optsBuilderAnn = klass.freakingFindAnnotation("GOptsBuilder") ?: return

            val fucks = gatherFucks(
                optsClass = klass.declarations.find {
                    it is KtClass && it.name == "Opts"
                } as KtClass,
                typeToQualified = {ident ->
                    if (hasInnerClass(klass, ident))
                        "${klass.name}.$ident"
                    else
                        ident
                })

            generateBuilderClass(g = out,
                                 klass = klass,
                                 generatedClassName = "Generated_BaseFor_${klass.name}",
                                 fucks = fucks,
                                 typeReturnedFromMutators = klass.name!!,
                                 typeReturnedFromHardenOpts = klass.name!! + ".Opts",
                                 generateIgnite = true,
                                 isAbstract = true,
                                 castReturnThis = true)
        }
    }

    private fun generateBuilderClass(g: CodeShitter, klass: KtClass, generatedClassName: String, fucks: MutableList<Fuck>,
                                     typeReturnedFromMutators: String, typeReturnedFromHardenOpts: String, generateIgnite: Boolean,
                                     isAbstract: Boolean, castReturnThis: Boolean)
    {
        g.appendln(CodeShitter.foldableSpacerMarker)
        g.appendln("// Generated at ${mangleUUID("c90a18a2-0dbd-4513-adfa-8165932eedc7")}")
        g.appendln("${isAbstract.thenElseEmpty{"abstract "}}class $generatedClassName {")

        generateOptsMaking(g, fucks)
        g.appendln(CodeShitter.foldableSpacerMarker)

        val generatedNestedClassesPlace = CodeShitter(indent = g.indent + 4)
        g.append(generatedNestedClassesPlace)

        g.appendln(CodeShitter.foldableSpacerMarker)
        for (fuck in fucks) {
            var generated = false

            // clog(fuck.type + " -- " + fuck.typeInSource)
            if (fuck.type.endsWith("Opts") || fuck.type.endsWith("Opts?")) {
                val fc = fileClasses.find {it.klass.name == fuck.typeInSource.replace("?", "")}
                if (fc != null) {
                    val nestedOptsKlass = fc.klass
                    if (nestedOptsKlass.isSealed()) {

                        for (decl in nestedOptsKlass.declarations) {
                            if (decl is KtClass) {
                                val concreteClass = decl

                                val nestedFucks = gatherFucks(
                                    concreteClass,
                                    typeToQualified = {it}
                                )

                                val builderClassName = "Generated_BuilderFor_${fuck.typeInSource.replace("?", "")}_${concreteClass.name}"

                                generatedNestedClassesPlace.let {g ->
                                    generateBuilderClass(g = generatedNestedClassesPlace,
                                                         klass = concreteClass,
                                                         generatedClassName = builderClassName,
                                                         fucks = nestedFucks,
                                                         typeReturnedFromMutators = builderClassName,
                                                         typeReturnedFromHardenOpts = concreteClass.name!!,
                                                         generateIgnite = false,
                                                         isAbstract = false,
                                                         castReturnThis = false)
                                }

                                val variant = concreteClass.name!!.decapitalize()
                                g.appendBetweenNewlines(CodeShitter(indent = g.indent + 4) - {g ->
                                    g.appendln("fun ${fuck.name}_$variant(block: ($builderClassName) -> Unit = {}): $typeReturnedFromMutators {")
                                    g.appendln("    // TODO:vgrechka Option to prevent calling this (or related _*s) more than once")
                                    g.appendln("    //               Both kinds of behavior (preventing vs. not) can be desired")
                                    g.appendln("    val builder = $builderClassName()")
                                    g.appendln("    block(builder)")
                                    g.appendln("    optsMaking.${fuck.name} = builder.hardenOpts()")
                                    g.appendln("    return this ${castReturnThis.thenElseEmpty{"as $typeReturnedFromMutators"}}")
                                    g.appendln("}")
                                })
                                generated = true
                            }
                        }
                    }
                }
            }

            if (!generated) {
                g.appendln("    fun ${fuck.name}(x: ${fuck.type}): $typeReturnedFromMutators {optsMaking.${fuck.name} = x; return this ${castReturnThis.thenElseEmpty{"as $typeReturnedFromMutators"}}}")
            }
        }

        generateHardenOpts(g, ret = typeReturnedFromHardenOpts, fucks = fucks)

        if (generateIgnite) {
            g.appendln(CodeShitter.foldableSpacerMarker)
            g.appendln("    fun ignite() = ${klass.name}.Ignition(hardenOpts()).ignite()")
        }

        g.appendln("}")
    }

    private fun gatherFucks(optsClass: KtClass, typeToQualified: (String) -> String): MutableList<Fuck> {
        val fucks = mutableListOf<Fuck>()

        val primaryConstructor = optsClass.primaryConstructor ?: return fucks
        for (param in primaryConstructor.valueParameters) {
    //                if (param.name == "title")
    //                    "break on me"

            val typeInSource = param.typeReference!!.text
            val type = run {
                val qualifiedType = StringBuilder()
                var identBuf = StringBuilder()
                fun identFinished() {
                    val ident = identBuf.toString()
                    qualifiedType.append(typeToQualified(ident))
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
                          typeInSource = typeInSource,
                          type = type,
                          defaultValue = param.defaultValue?.text)
        }
        return fucks
    }

    fun generateOptsMaking(g: CodeShitter, fucks: List<Fuck>) {
        g.append(CodeShitter(indent = g.indent + 4)-{g->
            g.appendln("val optsMaking = Generated_OptsMaking()")
            g.appendln("class Generated_OptsMaking {")

            for (fuck in fucks) {
                g.append(" ".repeat(g.indent + 4) + "var ${fuck.name}: ${fuck.type}")
                if (fuck.defaultValue == null) {
                    g.append("? = null")
                } else {
                    g.append(" = ")
                    g.append("${fuck.defaultValue}")
                }
                g.appendln()
            }

            g.appendln("}")
        })
    }

    private fun generateHardenOpts(g: CodeShitter, ret: String, fucks: List<Fuck>) {
        g.append(CodeShitter(indent = g.indent + 4)-{g->
            g.appendln("fun hardenOpts(): $ret {")
            g.appendln("    return $ret(")
            for ((i, fuck) in fucks.withIndex()) {
                g.append(" ".repeat(g.indent + 8) + "${fuck.name} = optsMaking.${fuck.name}")
                if (fuck.defaultValue == null)
                    g.append(" ?: wtf(\"I want `${fuck.name}`    ${UUID.randomUUID()}\")")
                if (i < fucks.lastIndex)
                    g.append(",")
                g.appendln()
            }
            g.appendln("    )")
            g.appendln("}")
        })
    }
}

private fun hasInnerClass(ktClass: KtClass, name: String) =
    ktClass.declarations.any {it is KtClass && it.name == name}












