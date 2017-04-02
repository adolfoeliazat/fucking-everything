package phi.gross.test.one

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun main(args: Array<String>) {
    sayShit(Shit("Archibald", "Fuck you"))
}

external fun phiPrint(x: String)

fun aaaaaaaaaaa() {}

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

fun qwe() {
    Shit("Archibald", "fuck you").sayIt("!!!!!")
}

fun bbbbbbbbbbb() {}


private fun sayShit(shit: Shit) {
    phiPrint("<b>${shit.text}, ${shit.name}</b>")
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













