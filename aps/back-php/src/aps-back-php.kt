package aps.back

import aps.*

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

fun <T> eval(code: String): T {
    imf("5b4295a4-9059-47af-96fc-736e5921dc09")
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


fun main(args: Array<String>) {
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
