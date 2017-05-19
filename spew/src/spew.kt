package vgrechka.spew

import org.jetbrains.kotlin.psi.*
import vgrechka.*
import java.util.*
import kotlin.reflect.KClass

interface Spew {
    fun ignite(ktFile: KtFile, outputFilePath: String, spewResults: SpewResults)
}

class SpewResults {
    val ddl = StringBuilder()
}

class SpewForInputFileOptions(val annotations: Boolean = false)

fun spewForInputFiles(paths: List<String>): SpewResults {
    val analysisResult = try {
        clog("Working like a dog, analyzing your crappy sources...")
        FuckedCLICompiler.doMain(FuckedK2JVMCompiler(), paths.map{it.substituteMyVars()}.toTypedArray())
        wtf("61ea9f24-7e40-45d8-a858-e357cccff2a0")
    } catch (e: EnoughFuckedCompiling) {
        e
    }

    val ktFiles = analysisResult.environment.getSourceFiles()
    val spewResults = SpewResults()
    for (ktFile in ktFiles) {
        for (ann in ktFile.freakingFindAnnotations(GSpit::class.simpleName!!)) {
            val spewClassName = ann.freakingGetStringAttribute(GSpit::spewClassName.name) ?: wtf("c301fe3f-a716-44c7-9931-70353676036b")
            val spewClass = Class.forName(spewClassName)

//            val spewAttributeText = ann.freakingGetClassAttributeText(GSpit::spew.name) ?: wtf("c301fe3f-a716-44c7-9931-70353676036b")
//            val colonColonIndex = spewAttributeText.indexOfOrNull("::") ?: wtf("e194e277-2f5f-40f8-9664-fd9b162f69b6")
//            val spewClass = Class.forName("vgrechka.spew.${spewAttributeText.substring(0, colonColonIndex)}")

            val spew = spewClass.newInstance() as Spew
            val outputFilePath = ann.freakingGetStringAttribute(GSpit::output.name) ?: wtf("3af6ca59-bae4-4659-8806-28e0b1395a0c")
            spew.ignite(ktFile, outputFilePath.substituteMyVars(), spewResults)
        }
    }

    return spewResults
}

object GlobalSpewContext {
    private var uid = 0L
    var opts = SpewForInputFileOptions()
    val uidToCapturedStack = mutableMapOf<Long, Exception>()

    fun nextUID() = ++uid

    fun maybeDebugInfo(): String {
        if (!opts.annotations) return ""
        return buildString {
            ln(); ln(); ln()
            ln("/*")
            for ((uid, exception) in uidToCapturedStack) {
                append(" *$uid <-- ")
                for (stackTraceElement in exception.stackTrace.drop(1)) {
                    append(stackTraceElement.fileName + ":" + stackTraceElement.lineNumber + "    ")
                }
                ln()
            }
            ln(" */")
        }
    }
}

class CodeShitter(val indent: Int = 0,
                  val isOneForProducingDebugInfoInTheEnd: Boolean = false,
                  val beforeReification: (CodeShitter) -> Unit = {}) {
    val buf = StringBuilder()
    val tag = "{{" + UUID.randomUUID().toString() + "}}"
    val subPlaces = mutableListOf<CodeShitter>()

    fun deleteLastCommaBeforeNewLine() {
        val shit = listOf(",\r\n", ",\n").find {buf.endsWith(it)}
        if (shit != null) {
            buf.setLength(buf.length - shit.length)
            buf.append("\n")
        }
    }

    fun deleteLastCommaBeforeDoubleQuoteClosingParenAndNewLine() {
        val shit = listOf(",\")\r\n", ",\")\n").find {buf.endsWith(it)}
        if (shit != null) {
            buf.setLength(buf.length - shit.length)
            buf.append("\")\n")
        }
    }

    fun append(text: String) {
        if (GlobalSpewContext.opts.annotations) {
            val uid = GlobalSpewContext.nextUID()
            GlobalSpewContext.uidToCapturedStack[uid] = Exception()
            buf += "/*$uid*/"
        }
        buf += text
    }

    fun appendln(text: String = "") {
        append(" ".repeat(indent) + text + "\n")
    }

    fun append(place: CodeShitter) {
        subPlaces += place
        buf.append(place.tag)
    }

    fun appendBetweenNewlines(place: CodeShitter) {
        appendln(foldableSpacerMarker)
        append(place)
        appendln(foldableSpacerMarker)
    }

    fun reify(): String {
        beforeReification(this)
        var res = buf.toString()
        for (subPlace in subPlaces) {
            val reifiedSubPlace = subPlace.reify()
            res = res.replace(subPlace.tag, reifiedSubPlace)
        }

        return res
    }

    fun line(text: String, numNewlines: Int = 1) {
        var s = dedent(text)
        s = reindent(indent, s)
        if (!s.endsWith("\n")) s += "\n".repeat(numNewlines)
        buf += s
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

    fun headerComment() {
        line(numNewlines = 2, text = """
            /*
             * (C) Copyright 2017 Vladimir Grechka
             *
             * YOU DON'T MESS AROUND WITH THIS SHIT, IT WAS GENERATED BY A TOOL SMARTER THAN YOU
             */""")
    }

    companion object {
        val foldableSpacerMarker = "// @@foldable-spacer"

        enum class LineFoldingState {FIRST, SECOND}

        fun foldSpacers(res: String): String {
            val inLines = res.lines()
            val outLines = mutableListOf<String>()
            var state = LineFoldingState.FIRST
            for (line in inLines) {
                val isFoldableSpacer = line.trim() == foldableSpacerMarker
                exhaustive=when (state) {
                    LineFoldingState.FIRST -> {
                        if (isFoldableSpacer) {
                            outLines += ""
                            state = LineFoldingState.SECOND
                        } else {
                            outLines += line
                        }
                    }
                    LineFoldingState.SECOND -> {
                        if (isFoldableSpacer) {
                        } else {
                            outLines += line
                            state = LineFoldingState.FIRST
                        }
                    }
                }
            }

            return outLines.joinToString("\n")
        }
    }
}

fun KtAnnotationEntry.freakingGetStringAttribute(name: String): String? {
    return this.freakingGetAttribute(name) {it.freakingSimpleStringValue()}
}

fun KtAnnotationEntry.freakingGetClassAttributeText(name: String): String? {
    return this.freakingGetAttribute(name) {
        (it.getArgumentExpression() as KtClassLiteralExpression).text
    }
}

fun KtAnnotationEntry.freakingGetEnumAttributeText(name: String): String? {
    return this.freakingGetAttribute(name) {
        (it.getArgumentExpression() as KtDotQualifiedExpression).text
    }
}

fun <T> KtAnnotationEntry.freakingGetAttribute(name: String, convert: (KtValueArgument) -> T): T? {
    for (valueArgument in this.valueArguments) {
        val ktValueArgument = valueArgument as KtValueArgument
        val argName = ktValueArgument.getArgumentName()!!.text
        if (argName == name) {
            return convert(ktValueArgument)
        }
    }
    return null
}

fun KtValueArgument.freakingSimpleStringValue(): String =
    (getArgumentExpression() as KtStringTemplateExpression).entries[0].text

fun KtAnnotated.freakingFindAnnotation(type: String) =
    annotationEntries.find {it.typeReference!!.text == type}

fun KtAnnotated.freakingFindAnnotations(type: String) =
    annotationEntries.filter {it.typeReference!!.text == type}

fun KtElement.freakingVisitClasses(onClass: (KtClass) -> Unit) {
    accept(object : KtVisitor<Unit, Unit>() {
        override fun visitKtFile(file: KtFile, data: Unit?) {
            for (decl in file.declarations) {
                decl.accept(this)
            }
        }

        override fun visitClass(klass: KtClass, data: Unit?) {
            onClass(klass)
        }
    })
}


