package vgrechka

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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
