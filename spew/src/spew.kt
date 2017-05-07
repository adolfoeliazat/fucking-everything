package vgrechka.spew

import org.jetbrains.kotlin.psi.*
import vgrechka.*
import kotlin.reflect.KClass

@Target(AnnotationTarget.FILE)
annotation class GSpit(val spew: KClass<out Spew>, val output: String)

interface Spew {
    fun ignite(ktFile: KtFile, outputFilePath: String, spewResults: SpewResults)
}

class SpewResults {
    val ddl = StringBuilder()
}

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
            val spewAttributeText = ann.freakingGetClassAttributeText(GSpit::spew.name) ?: wtf("c301fe3f-a716-44c7-9931-70353676036b")
            val colonColonIndex = spewAttributeText.indexOfOrNull("::") ?: wtf("e194e277-2f5f-40f8-9664-fd9b162f69b6")
            val spewClass = Class.forName("vgrechka.spew.${spewAttributeText.substring(0, colonColonIndex)}")
            val spew = spewClass.newInstance() as Spew
            val outputFilePath = ann.freakingGetStringAttribute(GSpit::output.name) ?: wtf("3af6ca59-bae4-4659-8806-28e0b1395a0c")
            spew.ignite(ktFile, outputFilePath.substituteMyVars(), spewResults)
        }
    }

    return spewResults
}

class CodeShitter(val output: StringBuilder, val indent: Int) {
    fun append(text: String) {
        output += text
    }

    fun appendln(text: String = "") {
        append(text + "\n")
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

    fun headerComment() {
        line(numNewlines = 2, text = """
            /*
             * (C) Copyright 2017 Vladimir Grechka
             *
             * YOU DON'T MESS AROUND WITH THIS SHIT, IT WAS GENERATED BY A TOOL SMARTER THAN YOU
             */""")
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

fun KtFile.freakingVisitClasses(onClass: (KtClass) -> Unit) {
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


