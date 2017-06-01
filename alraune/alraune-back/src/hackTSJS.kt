package alraune.back

import vgrechka.*
import java.io.File
import java.io.FilenameFilter

fun hackTSJS(): String {
    val jsCode = File(AlBackPile0.tsJSOutputFile).readText()
    val tsFiles = File(AlBackPile0.tsSrcRoot).listFiles {_, name -> name.endsWith(".ts")}

    val out = StringBuilder()
    val augmentRe = Regex("\\s*/// @augment ([^\\s]*)\\s*")
    val jsLines = jsCode.lines()
    for ((jsLineIndex, jsLine) in jsLines.withIndex()) {
        val mr = augmentRe.matchEntire(jsLine)
        if (mr != null) {
            val tag = mr.groupValues[1]

            var tsLineAbove by notNullOnce<String>()
            for (tsFile in tsFiles) {
                val src = tsFile.readText()
                var i = src.indexOf(tag)
                if (i != -1) {
                    while (true) {
                        if (src[i] == '\n') {
                            --i
                            if (src[i] == '\r')
                                --i
                            break
                        } else {
                            --i
                        }
                    }
                    val buf = StringBuilder()
                    while (true) {
                        if (src[i] == '\n') {
                            tsLineAbove = buf.reversed().toString()
                            break
                        } else {
                            buf.append(src[i])
                            --i
                        }
                    }
                    // i is at the end of jsLine above
                }
                break
            }

            val classDeclarationRe = Regex(".*?\\sclass (.*)\\{")
            val mr2 = classDeclarationRe.matchEntire(tsLineAbove) ?: bitch("34fe5ff3-0ddc-481f-9333-42a2754959e7")
            val s1 = mr2.groupValues[1]

            out.append("/* @generated $tag */")
            val alreadyInsideCtor = jsLines[jsLineIndex - 1].matches(Regex("\\s*constructor\\(.*"))
            if (!alreadyInsideCtor)
                out.append("constructor() {")

            if (s1.startsWith("implements ")) {
                val s2 = s1.substring("implements ".length)
                val interfaceNames = s2.split(",").map {it.trim()}
                for (interfaceName in interfaceNames) {
                    out.append(" this.__is$interfaceName = true;")
                }
            }

            out.append(" this.__stackAtCreation = new Error(\"Capturing stack\");")
            if (!alreadyInsideCtor)
                out.append("}")
            out.appendln()
        } else {
            out.appendln(jsLine)
        }
    }
    return out.toString()
}

