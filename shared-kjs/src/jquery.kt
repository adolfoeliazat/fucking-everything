@file:Suppress("UnsafeCastFromDynamic")

package vgrechka.kjs

import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import vgrechka.*
import kotlin.browser.*

external class JQuery {
    fun addClass(className: String): JQuery
//    fun addClass(f: Element.(Int, String) -> String): JQuery

    fun attr(attrName: String): String
    fun attr(attrName: String, value: dynamic): JQuery

    fun html(): String
    fun html(s: String): JQuery
//    fun html(f: Element.(Int, String) -> String): JQuery


    fun hasClass(className: String): Boolean
    fun removeClass(className: String): JQuery
    fun height(): Number
    fun width(): Number

    fun click(): JQuery

//    fun mousedown(handler: Element.(MouseEvent) -> Unit): JQuery
//    fun mouseup(handler: Element.(MouseEvent) -> Unit): JQuery
//    fun mousemove(handler: Element.(MouseEvent) -> Unit): JQuery

//    fun dblclick(handler: Element.(MouseClickEvent) -> Unit): JQuery
//    fun click(handler: Element.(MouseClickEvent) -> Unit): JQuery

//    fun load(handler: Element.() -> Unit): JQuery
//    fun change(handler: Element.() -> Unit): JQuery

    fun append(str: String): JQuery
    fun append(x: JQuery): JQuery
    fun ready(handler: () -> Unit): JQuery
    fun text(text: String): JQuery
    fun slideUp(): JQuery
//    fun hover(handlerInOut: Element.() -> Unit): JQuery
//    fun hover(handlerIn: Element.() -> Unit, handlerOut: Element.() -> Unit): JQuery
    fun next(): JQuery
    fun parent(): JQuery

    @JsName("val")
    fun getVal(): String?

    @JsName("val")
    fun setVal(x: String)

    fun on(event: String, handler: (Event) -> Unit)
    fun off(event: String = definedExternally)

    fun find(selector: String): JQuery
    fun children(selector: String = definedExternally): JQuery

    fun modal()
}

fun JQuery.onClick(handler: (MouseEvent) -> Unit) {
    this.off("click")
    this.on("click") {
        it.preventAndStop()
        handler(it.asDynamic())
    }
}

//open external class MouseEvent() {
//    val pageX: Double
//    val pageY: Double
//    fun preventDefault()
//    fun isDefaultPrevented(): Boolean
//}

//external class MouseClickEvent() : MouseEvent {
//    val which: Int
//}

@JsName("$")
external fun jq(selector: String): JQuery
@JsName("$")
external fun jq(selector: String, context: Element): JQuery
@JsName("$")
external fun jqDocumentReady(callback: () -> Unit): JQuery
@JsName("$")
external fun jq(obj: JQuery): JQuery
@JsName("$")
external fun jq(el: Element): JQuery
@JsName("$")
external fun jq(): JQuery



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
fun JQuery.hide(): String = this.asDynamic().hide()
fun JQuery.show(): String = this.asDynamic().show()

object JQueryPile {
    fun byIDSingle(id: String, errorTag: String): JQuery {
        val j = byID(id)
        if (j.length != 1)
            bitch("I want one element with ID `$id`, got ${j.length}    $errorTag")
        return j
    }

    fun byIDNoneOrSingle(id: String, errorTag: String): JQuery? {
        val j = byID(id)
        return when(j.length) {
            0 -> null
            1 -> j
            else -> bitch("I want either none or single element with ID `$id`, got ${j.length}    $errorTag")
        }
    }


    val jqbody: JQuery get() = jq(document.body!!)
    val jqwindow: JQuery get() = js("$(window)")

    fun byID(id: String): JQuery {
        val selector = "#$id".replace(Regex("\\."), "\\.")
        return jq(selector)
    }

    fun byID0(id: String): HTMLElement? {
        val selector = "#$id".replace(Regex("\\."), "\\.")
        return jq(selector)[0]
    }

    fun byID0ForSure(id: String): HTMLElement {
        return requireNotNull(byID0(id)) {"I want fucking element #$id"}
    }
}


fun JQuery.not(selector: String): JQuery = this.asDynamic().not(selector)

fun HTMLElement.hasScrollbar() = scrollHeight > clientHeight
























