package alraune.back

import vgrechka.*
import java.io.File

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
            tsFiles@for (tsFile in tsFiles) {
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
                            break@tsFiles
                        } else {
                            buf.append(src[i])
                            --i
                        }
                    }
                    // i is at the end of jsLine above
                }
            }

            val classDeclarationRe = Regex(".*?\\sclass (.*)\\{")
            val mr2 = classDeclarationRe.matchEntire(tsLineAbove) ?: bitch("34fe5ff3-0ddc-481f-9333-42a2754959e7")
            val s1 = mr2.groupValues[1]

            out.append("/* @generated $tag */")

            fun findJSLineAbove(predicate: (String) -> Boolean): String {
                for (i in (jsLineIndex - 1).downTo(0)) {
                    if (predicate(jsLines[i])) {
                        return jsLines[i]
                    }
                }
                wtf("018b19ce-c6c6-4a5d-8332-beee1fa2fe80")
            }

            val alreadyInsideCtor = run {
                val s = findJSLineAbove {it.trim().endsWith("{")}
                s.matches(Regex("\\s*constructor\\(.*"))
            }
            if (!alreadyInsideCtor)
                out.append("constructor() {")

            val className = run {
                val re = Regex(".*?\\s*class (.*?)\\s?\\{.*")
                val s = findJSLineAbove {it.matches(re)}
                val mr3 = re.matchEntire(s) ?: wtf("5a41fa91-6e0e-4a46-b69c-f1f5a2e72da9")
                mr3.groupValues[1]
            }
            if (className.isNotBlank()) { // Can be anonymous
                out.append(" this.__is$className = true;")
            }

            val implementsPrefix = when {
                className.isBlank() -> "implements "
                else -> "$className implements "
            }
            if (s1.startsWith(implementsPrefix)) {
                val s2 = s1.substring(implementsPrefix.length)
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

