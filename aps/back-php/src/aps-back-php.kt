package aps.back

import aps.*
import org.w3c.fetch.RequestDestination
import phizdetslib.*

object apsphp {
    var myFuckingDir by notNull<String>()
}

fun require_once(path: String) {
    phiEval("require_once('$path');")
}


object phplog {
    fun println(vararg xs: Any?) {
        val shit = xs.joinToString(" ") {it.toString()}
        val logFile = phiEval("return MY_FUCKING_LOG;") as String
        error_log("$shit\n", 3, logFile)
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
















