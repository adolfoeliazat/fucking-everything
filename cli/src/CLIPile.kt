package vgrechka

import kotlin.system.exitProcess

object CLIPile {
    val regexpTag = "@@regexp "

    class printAndCheckProcessResult(res: RunProcessResult, expectedLines: List<String>) {

        init {
            clog(res.stdout)

            val stderr = res.stderr
            if (stderr.isNotEmpty()) {
                clog("---------- STDERR ----------\n")
                clog(stderr)
                sayError("I didn't expect any stderr from TF")
                bitchAboutObscureOutput()
            }

            if (false) {
                clog("Expected output:")
                clog(expectedLines.joinToString("\n"))
                exitProcess(0)
            }

            val actualLines = res.stdout.lines().map {it.trim()}.toMutableList()

            for ((index, expected) in expectedLines.withIndex()) {
                val actual = takeActualLine(actualLines)
                if (actual == null) {
                    sayError("Expecting at least ${index + 1} lines of output from TF")
                    bitchAboutObscureOutput()
                }

                val expectedRegex = when {
                    expected.startsWith(regexpTag) -> Regex(expected.substring(regexpTag.length))
                    else -> Regex.fromLiteral(expected)
                }

                if (!actual.matches(expectedRegex)) {
                    sayError("Shitty TF output at line $index (0-based)")
                    sayError("Expected: [$expected]")
                    sayError("Actual:   [$actual]")
                    bitchAboutObscureOutput()
                }
            }

            if (actualLines.isNotEmpty()) {
                clog("\n---------- UNEXPECTED EXTRA LINES { ----------")
                clog(actualLines.joinToString("\n"))
                clog("---------- UNEXPECTED EXTRA LINES } ----------\n")
                sayError("Got unexpected extra ${actualLines.size} lines from TF")
                bitchAboutObscureOutput()
            }
        }

        fun takeActualLine(actualLines: MutableList<String>): String? {
            if (actualLines.isEmpty()) {
                return null
            } else {
                val s = actualLines.first()
                actualLines.removeAt(0)
                return s
            }
        }

        fun sayError(msg: String) {
            clog("[ERROR] $msg")
        }

        fun bitchAboutObscureOutput(): Nothing =
            bitch("TF says to us some obscure bullshit")
    }

}