package vgrechka.kjs

import org.w3c.dom.events.Event
import kotlin.browser.window

fun main(args: Array<String>) {
    clog("I am shared-kjs")
}

object KJSPile {
    fun getURLParam(name: String): String? {
        val shit = decodeURIComponent(window.location.search.substring(1))
        for (pairString in shit.split('&')) {
            val pair = pairString.split("=")
            if (pair[0] == name)
                return pair[1]
        }
        return null
    }
}

fun clog(vararg xs: Any?) {
    console.log(*xs)
}

external fun encodeURIComponent(s: String): String
external fun decodeURIComponent(s: String): String

fun Event.preventAndStop() {
    this.preventDefault()
    this.stopPropagation()
}

