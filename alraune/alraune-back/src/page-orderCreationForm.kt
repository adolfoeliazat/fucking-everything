package alraune.back

import vgrechka.*

fun handleGet_orderCreationForm() {
    val commands = mutableListOf<AlBackToFrontCommandPile>()
    val fields = OrderParamsFields(null)
    emitCommandsForRenderingOrderParamsForm(commands, fields)
    val initialBackResponse = AlBackResponsePile()-{o->
        o.commands = commands
    }
    spitUsualPage(kdiv(Attrs(domid = AlDomid.replaceableContent)), initialBackResponse)
}



