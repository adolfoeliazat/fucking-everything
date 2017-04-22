package vgrechka

import com.google.common.collect.MapMaker
import javafx.beans.Observable
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Alert
import javafx.util.Callback
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

object JFXStuff {
    fun infoAlert(headerText: String) {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "Fucking Information"
        alert.headerText = headerText
        alert.showAndWait()
    }

    fun errorAlert(e: Throwable) {
        errorAlert(e.message ?: "Obscure exception", e.stackTraceString, width = 1000)
    }

    fun errorAlert(headerText: String, contentText: String? = null, width: Int? = null) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Bloody Error"
        alert.headerText = headerText
        alert.contentText = contentText
        alert.isResizable = true
        width?.let {alert.dialogPane.prefWidth = it.toDouble()}
        alert.showAndWait()
    }
}

private val Any.observables by AttachedComputedShit<Any, MutableList<Observable>>(weak = true) {
    Collections.synchronizedList(mutableListOf())
}

class JFXProperty<T>(val initialValue: T) {
    val simpleObjectProperty = SimpleObjectProperty(initialValue)

    operator fun provideDelegate(thisRef: Any, prop: KProperty<*>): ReadWriteProperty<Any, T> {
        thisRef.observables += simpleObjectProperty

        return object:ReadWriteProperty<Any, T> {
            override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
                simpleObjectProperty.value = value
            }

            override fun getValue(thisRef: Any, property: KProperty<*>): T {
                return simpleObjectProperty.value
            }
        }
    }
}

class JFXPropertyObservableExtractor<T> : Callback<T, Array<Observable>> {
    override fun call(obj: T): Array<Observable> {
        return obj?.observables?.toTypedArray() ?: arrayOf()
    }
}






















