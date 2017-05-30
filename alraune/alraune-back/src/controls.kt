package alraune.back

import java.util.*

fun renderTextControl(onShowCommands: MutableList<AlBackToFrontCommand>, propName: String, value: String): Renderable {
    val placeholderDomid = UUID.randomUUID().toString()
    onShowCommands += CreateTextControlCommand(placeholderDomid, propName, value)
    return kdiv(Attrs(id = placeholderDomid))
}


