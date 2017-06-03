package alraune.back

import com.fasterxml.jackson.databind.ObjectMapper
import vgrechka.*

fun handleFuckingCall() {
    val response = AlBackResponsePile()
    val commands = mutableListOf<AlBackToFrontCommandPile>()
    response.commands = commands

    val requestText = rctx.req.reader.readText()
    val ftb = ObjectMapper().readValue(requestText, AlFrontToBackCommandPile::class.java)
    clog("ftb", ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(ftb))
//    rctx.ftb = ftb

    exhaustive=when (ftb.opcode) {
        AlFrontToBackCommandOpcode.SubmitOrderCreationForm -> {
            val fields = OrderParamsFields(ftb)
            if (fields.validationResults.any {it.error != null}) {
                emitCommandsForRenderingOrderCreationFormPage(commands, fields)
            } else {
                val order = alUAOrderRepo.save(newAlUAOrder(
                    uuid = AlBackPile.uuid(), state = UAOrderState.CUSTOMER_DRAFT,
                    email = fields.email.sanitizedString, contactName = fields.contactName.sanitizedString, phone = fields.phone.sanitizedString,
                    documentTitle = fields.documentTitle.sanitizedString, documentDetails = fields.documentDetails.sanitizedString,
                    documentType = fields.documentType.name, documentCategory = fields.documentCategory,
                    numPages = fields.numPages.sanitizedString.toInt(), numSources = fields.numSources.sanitizedString.toInt()))

                commands += AlBackToFrontCommandPile()-{o->
                    o.opcode = AlBackToFrontCommandOpcode.SetLocationHref
                    o.href = AlBackPile0.baseURL + "/order?orderUUID=${order.uuid}"
                }
            }
        }

        AlFrontToBackCommandOpcode.SubmitOrderParamsForm -> imf("592f03a8-5453-4c51-b3f5-2e182ebb6b05")
    }

    rctx.res.contentType = "application/json; charset=utf-8"
    rctx.res.writer.print(ObjectMapper().writeValueAsString(response))
}


