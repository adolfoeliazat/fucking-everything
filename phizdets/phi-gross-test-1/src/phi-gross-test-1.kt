package phi.gross.test.one

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun main(args: Array<String>) {
    sayShit(Shit("Archibald", "Fuck you"))
    runTest(TestCheck())
    runTest(TestNotNullOnce())
}

external fun phiBreakDebugger()

fun assertTrue(b: Boolean, assertionID: String) {
    if (!b) {
        fail("assertTrue failed: $assertionID")
    }
}

fun assertEquals(expected: Any?, actual: Any?, assertionID: String) {
    if (expected != actual) {
        fail("assertEquals failed: $assertionID\nExpected: $expected\nActual: $actual")
    }
}

private fun fail(msg: String) {
    throw AssertionError(insideLines(msg))
}

private fun insideLines(s: String): String {
    val line = "----------------------------------------------------------\n"
    return "\n$line" + s + "\n$line"
}

//fun assertVarExportEquals(expected: String, actual: Any?, assertionID: String, log: Boolean = false) {
//    val shit0 = var_export(actual, returnString = true)
//    val shit = shit0.replace("\n  ", " ").replace("\n", "")
//    if (log) {
//        println("shit = $shit")
//    }
//    assertEquals(expected, shit, assertionID)
//}

fun assertException(testType: (Throwable) -> Boolean, expectedMessage: String, assertionID: String, block: () -> Unit) {
    try {
        block()
    } catch (e: Throwable) {
        val qwe = testType(e)
        if (!qwe)
            fail("assertException failed: $assertionID\nActual exception: ${e::class.simpleName}")
        assertEquals(expectedMessage, e.message, "message-$assertionID")
    }
}

interface Test {
    fun runTest()
}

fun runTest(test: Test) {
    test.runTest()
    phiPrintln("${test::class.simpleName}: PASSED")
}

class TestCheck : Test {
    override fun runTest() {
        assertException({it is IllegalStateException}, "vagina", "7f2510ff-4753-4ea3-8d39-dcfe63ea910a") {
            check(false) {"vagina"}
        }
    }
}

class TestNotNullOnce : Test {
    object Q {
        var a by notNullOnce<String>()
    }

    override fun runTest() {
        assertException({it is IllegalStateException}, "Property `a` should be initialized before get.", "81060a82-59e8-4bbc-b733-501d71fdb169") {
            phiPrintln("Q.a = " + Q.a)
        }

        Q.a = "pizda"
        phiPrintln("Q.a = " + Q.a)
        assertEquals("pizda", Q.a, "9181db8b-07ec-45e1-87fb-d738c2d235e3")

        assertException({it is IllegalStateException}, "Property `a` should be assigned only once", "2be3d76a-cab1-4ae7-ad8e-3ac0a601bdd2") {
            Q.a = "pizda again"
        }
        phiPrintln("Q.a = " + Q.a)
        assertEquals("pizda", Q.a, "b55ae4d8-9ae2-40a5-b0a7-6dd71bf98bf1")
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













