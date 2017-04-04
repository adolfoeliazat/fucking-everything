package phi.gross.test.one

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun main(args: Array<String>) {
    sayShit(Shit("Archibald", "Fuck you"))
    testCheck()
    test1()
}

external fun phiBreakDebugger()

fun assertEquals(expected: Any?, actual: Any?, assertionID: String) {
    if (expected != actual) {
        val line = "----------------------------------------------------------\n"
        throw AssertionError("\n${line}assertEquals failed: $assertionID\nExpected: $expected\nActual: $actual\n$line")
    }
}

//fun assertVarExportEquals(expected: String, actual: Any?, assertionID: String, log: Boolean = false) {
//    val shit0 = var_export(actual, returnString = true)
//    val shit = shit0.replace("\n  ", " ").replace("\n", "")
//    if (log) {
//        println("shit = $shit")
//    }
//    assertEquals(expected, shit, assertionID)
//}

fun testCheck() {
    assertEquals(6, 2 + 3, "69c46fb9-8dd6-4459-b3d3-96ac460c26ab")

    try {
        check(false) {"pizda"}
    } catch (e: Throwable) {
        phiPrintln(e.message) // TODO:vgrechka Assert...
    }

    try {
        check(false) {"pizda"}
    } catch (e: IllegalStateException) {
        phiPrintln("Well again...") // TODO:vgrechka Assert...
    }
}

class test1 {
    init {
        Q.a = "pizda"
        phiPrintln(Q.a)
        Q.a = "pizda again"
        phiPrintln(Q.a)
    }

    object Q {
        var a by notNullOnce<String>()
    }
}


external fun phiPrint(x: String?)

open class ShitParent(val a: String, val b: String) {
    var prelude = "Now I'm really gonna say it..."

    open fun sayIt(c: String) {
        phiPrint("a = $a\n")
        phiPrint("b = $b\n")
        phiPrint("c = $c\n")
        phiPrint("$prelude\n")
    }
}

class Shit(val name: String, val text: String) : ShitParent("fucking-a", "fucking-b") {
    override fun sayIt(c: String) {
        super.sayIt("fucking-c")
        phiPrint("<b>$name, $text$c</b>")
    }
}

fun phiPrintln(x: String?) {
    phiPrint(x + "\n")
}

private fun sayShit(shit: Shit) {
    phiPrintln("<b>${shit.text}, ${shit.name}</b>")
}

fun <T: Any> notNullOnce(): ReadWriteProperty<Any?, T> = NotNullOnceVar()

private class NotNullOnceVar<T: Any> : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property `${property.name}` should be initialized before get.")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        check(this.value == null) {"Property `${property.name}` should be assigned only once"}
        this.value = value
    }
}













