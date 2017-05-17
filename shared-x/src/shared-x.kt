package vgrechka

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

fun main(args: Array<String>) {
}

fun StringBuilder.ln(x: Any?) {
    append(x)
    append("\n")
}

// TODO:vgrechka @unduplicate e0841d01-e09d-49e0-8d60-7f4f77f42ca8
class notNullOnce<T: Any> : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property `${property.name}` should be initialized before get.")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        check(this.value == null) {"Property `${property.name}` should be assigned only once"}
        this.value = value
    }
}

// TODO:vgrechka @unduplicate d405bf42-54e2-481e-8b31-457d53eb3293
inline operator fun <T, FRet> T.minus(f: (T) -> FRet): T { f(this); return this }










