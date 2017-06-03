package alraune.back

import com.fasterxml.jackson.databind.ObjectMapper
import vgrechka.*

fun handleFuckingCall() {
    val response = AlBackResponsePile()
    val commands = mutableListOf<AlBackToFrontCommandPile>()
    response.commands = commands

    exhaustive=when (rctx.ftb.opcode) {
        AlFrontToBackCommandOpcode.SubmitOrderCreationForm -> {
            val fields = OrderParamsFields(FieldSource.POST)
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

        AlFrontToBackCommandOpcode.SubmitOrderParamsForm -> {
            val fields = OrderParamsFields(FieldSource.POST)
            if (fields.validationResults.any {it.error != null}) {
                val initCommands = mutableListOf<AlBackToFrontCommandPile>()
                val inputControlUUIDs = mutableListOf<String>()
                commands += AlBackToFrontCommandPile()-{o->
                    o.opcode = AlBackToFrontCommandOpcode.ReplaceElement
                    o.domid = AlDomid.modalContent
                    o.initCommands = initCommands
                    o.html = renderOrderParamsModalContent(initCommands, inputControlUUIDs, fields).render()
                }
            } else {
                rctx.order.let {
                    it.email = fields.email.sanitizedString
                    it.contactName = fields.contactName.sanitizedString
                    it.phone = fields.phone.sanitizedString
                    it.documentTitle = fields.documentTitle.sanitizedString
                    it.documentDetails = fields.documentDetails.sanitizedString
                    it.documentType = fields.documentType.name
                    it.documentCategory = fields.documentCategory
                    it.numPages = fields.numPages.sanitizedString.toInt()
                    it.numSources = fields.numSources.sanitizedString.toInt()
                }

                commands += AlBackToFrontCommandPile()-{o->
                    o.opcode = AlBackToFrontCommandOpcode.CloseModal
                }

                commands += AlBackToFrontCommandPile()-{o->
                    o.opcode = AlBackToFrontCommandOpcode.ReplaceElement
                    o.domid = AlDomid.replaceableContent
                    val initCommands = mutableListOf<AlBackToFrontCommandPile>()
                    o.initCommands = initCommands
                    o.html = renderOrderPage(initCommands, RenderingParamsPile(
                        orderParamsClazz = AlCSS.fuckIn))
                        .render()
                }
            }
        }

        AlFrontToBackCommandOpcode.SubmitCreateOrderFileForm -> {
            val fields = FileFields(FieldSource.POST)
            if (fields.validationResults.any {it.error != null}) {
                val initCommands = mutableListOf<AlBackToFrontCommandPile>()
                val inputControlUUIDs = mutableListOf<String>()
                commands += AlBackToFrontCommandPile()-{o->
                    o.opcode = AlBackToFrontCommandOpcode.ReplaceElement
                    o.domid = AlDomid.modalContent
                    o.initCommands = initCommands
                    o.html = renderFileModalContent(initCommands, inputControlUUIDs, fields, AlFrontToBackCommandOpcode.SubmitCreateOrderFileForm).render()
                }
            } else {
                rctx.file.let {
                    it.title = fields.title.sanitizedString
                    it.details = fields.details.sanitizedString
                }

                commands += AlBackToFrontCommandPile()-{o->
                    o.opcode = AlBackToFrontCommandOpcode.CloseModal
                }

                imf("449c8727-29b1-4a18-bb07-8af7d9d9cb69")
                commands += AlBackToFrontCommandPile()-{o->
                    o.opcode = AlBackToFrontCommandOpcode.ReplaceElement
                    o.domid = AlDomid.replaceableContent
                    val initCommands = mutableListOf<AlBackToFrontCommandPile>()
                    o.initCommands = initCommands
                    o.html = renderOrderPage(initCommands, RenderingParamsPile(
                        orderParamsClazz = AlCSS.fuckIn))
                        .render()
                }
            }
        }

        AlFrontToBackCommandOpcode.SubmitUpdateOrderFileForm -> {
            imf("1de7677b-d975-484c-92ed-ee5831a8764f")
        }
    }

    rctx.res.contentType = "application/json; charset=utf-8"
    rctx.res.writer.print(ObjectMapper().writeValueAsString(response))
}


