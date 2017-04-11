package aps.back

import aps.*
import phizdetslib.*

object apsphp {
    var myFuckingDir by notNull<String>()
}

fun require_once(path: String) {
    phiEval("require_once('$path');")
}

fun error_log(vararg xs: Any?) {
    imf("f258787a-ee6d-4b9f-b544-1653fc299bf1")
}


object phplog {
    fun info(vararg xs: Any?) {
        val shit = xs.joinToString(" ") {it.toString()}
        error_log("$shit\n", 3, apsphp.myFuckingDir + "/../log/all.log")
    }
}


fun main(args: Array<String>) {
    apsphp.myFuckingDir = phiEval("return \$GLOBALS['myFuckingDir'];") as String
    // println("My fucking dir is " + aps.myFuckingDir)
    require_once(apsphp.myFuckingDir + "/aps-back-settings.php")

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
















