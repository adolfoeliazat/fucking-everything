package alraune.front

import kotlin.reflect.KProperty

interface LocalStorageShim {
    val length: Int
    fun clear()
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
    fun key(index: Int): String?
}

object RealLocalStorage : LocalStorageShim {
    override val length get() = kotlin.browser.localStorage.length
    override fun key(index: Int) = kotlin.browser.localStorage.key(index)
    override fun clear() = kotlin.browser.localStorage.clear()
    override fun getItem(key: String): String? = kotlin.browser.localStorage.getItem(key)
    override fun setItem(key: String, value: String) = kotlin.browser.localStorage.setItem(key, value)
    override fun removeItem(key: String) = kotlin.browser.localStorage.removeItem(key)
}

class AlLocalStorage(val store: LocalStorageShim) {
    var signedIn by BooleanValue()

    inner class StringValue {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String? =
            store.getItem(property.name)

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
            if (value == null) store.removeItem(property.name)
            else store.setItem(property.name, value)
        }
    }

    inner class IntValue {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Int? {
            return store.getItem(property.name)?.let {it.toInt()}
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int?) {
            if (value == null) store.removeItem(property.name)
            else store.setItem(property.name, value.toString())
        }
    }

    inner class BooleanValue {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean =
            store.getItem(property.name).let {it == "true" || it == "yes"}

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            store.setItem(property.name, if (value) "true" else "false")
        }
    }
}



