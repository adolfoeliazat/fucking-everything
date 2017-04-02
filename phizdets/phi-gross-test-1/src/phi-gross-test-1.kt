package phi.gross.test.one

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun main(args: Array<String>) {
    sayShit(Shit("Archibald", "Fuck you"))
}

external fun phiPrint(x: String)

class Shit(val name: String, val text: String) {
    fun sayIt() {
        phiPrint("<b>$text, $name</b>")
    }
}

fun qwe() {
    Shit("Archibald", "fuck you").sayIt()
}


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













