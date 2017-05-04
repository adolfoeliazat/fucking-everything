package vgrechka

object DebugPile {
    private var nextPUID = 1L
    val shitToDebugTag = mutableMapOf<Any, String>()
    val shitToAttachedShit = mutableMapOf<Any, MutableMap<String, Any?>>()

    fun nextPUID(): Long = nextPUID++
}

fun Any.debug_attachAllShitFrom(src: Any) {
    val shitAttachedToSource = DebugPile.shitToAttachedShit[src] ?: return
    val shitAttachedToMe = DebugPile.shitToAttachedShit.getOrPut(this) {mutableMapOf()}
    for ((k, v) in shitAttachedToSource) {
        shitAttachedToMe[k] = v
    }
}

fun Any.debug_attachShit(label: String, shit: Any?) {
    val attachedShit = DebugPile.shitToAttachedShit.getOrPut(this) {mutableMapOf()}
    attachedShit[label] = shit
}

fun Any.debug_attachedShit(label: String): Any? {
    val attachedShit = DebugPile.shitToAttachedShit[this] ?: return null
    return attachedShit[label]
}

fun Any.debug_attachTag(tag: String? = null): String {
    val theTag = tag ?: ("dt" + DebugPile.nextPUID())
    DebugPile.shitToDebugTag[this] = theTag
    return theTag
}

val Any?.debug_tag: String? get() = DebugPile.shitToDebugTag[this]

