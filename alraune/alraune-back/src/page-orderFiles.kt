package alraune.back

import vgrechka.*

fun handleGet_orderFiles() {
    val uuid = AlRequestContext.the.getParams.orderUUID ?: bitch("0fe1dd78-8afd-4511-b743-7fc3b5ac78ce")
    val order = alUAOrderRepo.findByUuid(uuid) ?: bitch("bcfc6c38-585c-43f9-8984-c26d9c113e4e")
    val fields = OrderParamsFields(order.toForm())
    shitBigReplacementToFront("37636e9d-5060-43b8-a50d-34a95fe5bce1")
    shitToFront("954a5058-5ae6-40c7-bb45-06b0eeae8bc7") {
        it.hasErrors = false
    }
    spitOrderParamsPage(order, fields)
}
