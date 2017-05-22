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

/**
 * Until KJS source mapping for !! is fixed
 */
fun <T> bang(x: T?): T {
    if (x == null) bitch("Banging on null")
    return x
}
