package alraune.back

import java.util.*

fun renderTextControl(initCommands: MutableList<AlBackToFrontCommand>, propName: String, value: String): Renderable {
    val placeholderDomid = UUID.randomUUID().toString()
    initCommands += CreateTextControlCommand(placeholderDomid, propName, value)
    return kdiv(Attrs(id = placeholderDomid))
}


