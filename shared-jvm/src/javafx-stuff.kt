package vgrechka

import com.google.common.collect.MapMaker
import javafx.application.Platform
import javafx.beans.Observable
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TextInputDialog
import javafx.util.Callback
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

object JFXStuff {
    fun later(block: () -> Unit) {
        Platform.runLater(block)
    }

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

    fun inputBox(prompt: String, initialValue: String): String? {
        val dialog = TextInputDialog(initialValue)
        dialog.title = "Type Into Me"
        dialog.headerText = prompt
        return dialog.showAndWait().orElse(null)
    }

    fun confirm(prompt: String): Boolean {
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = "Confirm Me"
        alert.headerText = "Are you fucking sure?"
        alert.contentText = prompt
        return alert.showAndWait().get() == ButtonType.OK
    }

    enum class YesNoCancelResult {YES, NO, CANCEL}

    fun yesNoCancel(prompt: String): YesNoCancelResult {
        val alert = Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
        alert.title = "Hey, Hoser"
        alert.headerText = prompt
        return when (alert.showAndWait().get()) {
            ButtonType.YES -> YesNoCancelResult.YES
            ButtonType.NO -> YesNoCancelResult.NO
            ButtonType.CANCEL -> YesNoCancelResult.CANCEL
            else -> wtf("b29d5532-14cd-4dfe-af1e-6b00aa225028")
        }
    }
}

private val Any.observables by AttachedComputedShit<Any, MutableList<Observable>>(weak = true) {
    Collections.synchronizedList(mutableListOf())
}

fun <T> jfxProperty(p: KMutableProperty0<T>): SimpleObjectProperty<T> {
    p.isAccessible = true
    val delegate = p.getDelegate()
    return (delegate as JFXProperty<T>.Delegate).simpleObjectProperty
}


class JFXProperty<T>(val initialValue: T) {
    inner class Delegate(thisRef: Any) : ReadWriteProperty<Any, T> {
        val simpleObjectProperty = SimpleObjectProperty(initialValue)

        init {
            thisRef.observables += simpleObjectProperty
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            simpleObjectProperty.value = value
        }

        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            return simpleObjectProperty.value
        }
    }

    operator fun provideDelegate(thisRef: Any, prop: KProperty<*>) =
        Delegate(thisRef)
}

class JFXPropertyObservableExtractor<T> : Callback<T, Array<Observable>> {
    override fun call(obj: T): Array<Observable> {
        return obj?.observables?.toTypedArray() ?: arrayOf()
    }
}






















