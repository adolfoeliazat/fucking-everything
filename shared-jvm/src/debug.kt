package vgrechka

object DebugGlobal {
    private var nextPUID = 1L
    val shitToDebugTag = mutableMapOf<Any, String>()
    val shitToAttachedShit = mutableMapOf<Any, MutableMap<String, Any?>>()

    fun nextPUID(): Long = nextPUID++
}

fun Any.debug_attachShit(label: String, shit: Any?) {
    val attachedShit = DebugGlobal.shitToAttachedShit.getOrPut(this) {mutableMapOf()}
    attachedShit[label] = shit
}

fun Any.debug_attachedShit(label: String): Any? {
    val attachedShit = DebugGlobal.shitToAttachedShit[this] ?: return null
    return attachedShit[label]
}

fun Any.debug_attachTag(tag: String? = null): String {
    val theTag = tag ?: ("dt" + DebugGlobal.nextPUID())
    DebugGlobal.shitToDebugTag[this] = theTag
    return theTag
}

val Any?.debug_tag: String? get() = DebugGlobal.shitToDebugTag[this]

