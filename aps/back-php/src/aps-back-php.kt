package aps.back

import aps.*
import phizdetslib.*

object apsphp {
    var myFuckingDir by notNull<String>()
}

annotation class PHPDumpBodyToContainer

fun require_once(path: String) {
    imf("119e3083-946c-428e-b380-037bb6fcb537")
}

fun error_log(vararg xs: Any?) {
    imf("f258787a-ee6d-4b9f-b544-1653fc299bf1")
}

@PHPDumpBodyToContainer fun apsback_makeRequires() {
    apsphp.myFuckingDir = phiEval("return __DIR__;") as String
    // println("My fucking dir is " + aps.myFuckingDir)
    require_once(apsphp.myFuckingDir + "/aps-back-settings.php")
}

object phplog {
    fun info(vararg xs: Any?) {
        val shit = xs.joinToString(" ") {it.toString()}
        error_log("$shit\n", 3, apsphp.myFuckingDir + "/../log/all.log")
    }
}


fun main(args: Array<String>) {
    // dumpRequestShitAndFuckAway()
    serveShit(XHttpServletRequest(), XHttpServletResponse())
}

fun dumpRequestShitAndFuckAway() {
    phiEval("""
        echo 'QUERY_STRING = ';
        var_export(${'$'}_SERVER['QUERY_STRING']);
        exit();
    """)
}
















