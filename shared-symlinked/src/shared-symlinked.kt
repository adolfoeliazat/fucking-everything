package vgrechka

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

annotation class Ser
annotation class AllOpen

var exhaustive: Any? = null

fun wtf(msg: String = "...WTF didn't you describe this WTF?"): Nothing = throw Exception("WTF: $msg")
fun die(msg: String = "You've just killed me, motherfucker!"): Nothing = throw Exception("Aarrgghh... $msg")
fun imf(what: String = "me"): Nothing = throw Exception("Implement $what, please, fuck you")
fun bitch(msg: String = "Just bitching..."): Nothing = throw Exception(msg)

class notNullOnce<T: Any> : ReadWriteProperty<Any?, T> {
    var _value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return _value ?: throw IllegalStateException("Property `${property.name}` should be initialized before get.")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        check(this._value == null) {"Property `${property.name}` should be assigned only once"}
        this._value = value
    }
}

fun <T> named(make: (ident: String) -> T) = object : ReadOnlyProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value == null)
            value = make(property.name)
        return bang(value)
    }
}

fun myName() = named {it}
fun slashMyName() = named {"/" + it}

/**
 * Until KJS source mapping for !! is fixed
 */
fun <T> bang(x: T?): T {
    if (x == null) bitch("Banging on null")
    return x
}

fun StringBuilder.ln(x: Any? = "") {
    append(x)
    append("\n")
}

fun String.indexOfOrNull(needle: String, startIndex: Int = 0, ignoreCase: Boolean = false): Int? {
    val index = indexOf(needle, startIndex, ignoreCase)
    return if (index >= 0) index else null
}

inline fun <T> List<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
    val index = this.indexOfFirst(predicate)
    return when (index) {
        -1 -> null
        else -> index
    }
}

fun <T> List<T>.indexOfOrNull(element: T): Int? {
    val index = this.indexOf(element)
    return when (index) {
        -1 -> null
        else -> index
    }
}

// "Create parameter" refactoring aids
fun <R> call(f: () -> R): R = f()
fun <R, P0> call(f: (P0) -> R, p0: P0): R = f(p0)
fun <R, P0, P1> call(f: (P0, P1) -> R, p0: P0, p1: P1): R = f(p0, p1)










