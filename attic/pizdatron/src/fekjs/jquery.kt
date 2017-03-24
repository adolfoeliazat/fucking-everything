@file:Suppress("UnsafeCastFromDynamic")
package fekjs

import org.w3c.dom.*
import jquery.*
import kotlin.browser.*

external interface JQueryPosition {
    val left: Double
    val top: Double
}

operator fun JQuery.get(index: Int): HTMLElement? = this.asDynamic()[index]
fun JQuery.scrollTop(value: Double): Unit = this.asDynamic().scrollTop(value)
fun JQuery.scrollTop(value: Int): Unit = this.asDynamic().scrollTop(value)
fun JQuery.scrollTop(): Double = this.asDynamic().scrollTop()
fun JQuery.offset(): JQueryPosition = this.asDynamic().offset()
fun JQuery.text(): String = this.asDynamic().text()
fun JQuery.remove(): String = this.asDynamic().remove()
val JQuery.length: Int get() = this.asDynamic().length
fun JQuery.css(prop: String, value: Any?): JQuery = this.asDynamic().css(prop, value)
fun JQuery.setVal(value: String?): JQuery = this.asDynamic().`val`(value)
fun JQuery.outerWidth(includeMargin: Boolean = false): Double = this.asDynamic().outerWidth(includeMargin)
fun JQuery.outerHeight(includeMargin: Boolean = false): Double = this.asDynamic().outerHeight(includeMargin)
fun JQuery.each(block: (index: Int, element: HTMLElement) -> Unit): Unit = this.asDynamic().each(block)

val jqbody: JQuery get() = jq(document.body!!)

fun byid(id: String): JQuery {
    val selector = "#$id".replace(Regex("\\."), "\\.")
    return jq(selector)
}

fun byid0(id: String): HTMLElement? {
    val selector = "#$id".replace(Regex("\\."), "\\.")
    return jq(selector)[0]
}

fun byid0ForSure(id: String): HTMLElement {
    return requireNotNull(byid0(id)) {"I want fucking element #$id"}
}

fun JQuery.not(selector: String): JQuery = this.asDynamic().not(selector)

fun JQuery.scrollBodyToShit(dy: Int = 0) {
    jqbody.scrollTop(this.offset().top - 50 + dy)
}

