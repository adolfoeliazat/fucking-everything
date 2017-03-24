@file:Suppress("UnsafeCastFromDynamic")

package fekjs

import fekjs.node.*
import fekjs.pizdatron.*

val global: dynamic get() =
    if (js("typeof global") == "object") js("global")
    else js("window")

// Rapier operator: x-{o->
inline operator fun <T, R> T.minus(block: (T) -> R): T {
    block(this)
    return this
}

@Suppress("Unused")
fun commandLineRun() {
    val qualifiedName = process.argv[2]
    var shit = js("kotlin.modules.fekjs")
    for (name in qualifiedName.split(".")) {
        shit = shit[name]
    }
    js("new shit()")
}


