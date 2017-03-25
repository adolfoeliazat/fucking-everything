package aps.back

import photlin.*
import aps.*

object apsphp {
    var myFuckingDir by notNull<String>()
}

@PHPDumpBodyToContainer fun apsback_makeRequires() {
    apsphp.myFuckingDir = eval<String>("return __DIR__;")
    // println("My fucking dir is " + aps.myFuckingDir)
    require_once(apsphp.myFuckingDir + "/aps-back-settings.php")
}

object phplog {
    fun info(vararg xs: Any?) {
        val shit = xs.joinToString(" ") {it.toString()}
        error_log("$shit\n", 3, apsphp.myFuckingDir + "/../log/all.log")
    }
}


fun apsback_main(args: Array<String>) {
    serveShit(XHttpServletRequest(), XHttpServletResponse())
}

fun pizdunchik() {
    val shit = PHP_XmlAccessType.values()
}

fun accessClitoris() {
    val fuck = const.text.numberSign
    val x = PHP_HttpServletResponse.SC_OK
    val target = Pizda.Clitoris()
}
