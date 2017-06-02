package alraune.back

import alraune.back.AlRenderPile.pageHeader
import alraune.back.AlRenderPile.renderOrderParamsForm
import alraune.back.AlRenderPile.t
import alraune.shared.AlUADocumentCategories
import vgrechka.*

fun handleGet_orderCreationForm() {
    spitUsualPage(renderOrderCreationFormPage())
//    val fields = OrderParamsFields(
//        newAlUAOrder(
//            uuid = "boobs", state = UAOrderState.CUSTOMER_DRAFT, email = "",
//            contactName = "", phone = "", documentTypeID = AlDocumentType.ABSTRACT.name, documentTitle = "",
//            documentDetails = "", documentCategoryID = AlDocumentCategories.miscID, numPages = -1, numSources = -1)
//            .toForm())
//    fields.fieldCtx.noValidation()
//    spitOrderCreationFormPage(fields)
}

private fun renderOrderCreationFormPage(): Tag {
    return kdiv(Attrs(domid = AlDomid.replaceableContent)){o->
        o- pageHeader(t("TOTE", "Йобаный Заказ"))

        val initCommands = mutableListOf<AlBackToFrontCommandPile>()
        val inputControlUUIDs = mutableListOf<String>()

        o- renderOrderParamsForm(initCommands = initCommands,
                                 inputControlUUIDs = inputControlUUIDs,
                                 contactNameVirginValue = {""},
                                 emailVirginValue = {""},
                                 phoneVirginValue = {""},
                                 documentTypeVirginValue = {AlUADocumentType.ABSTRACT.name},
                                 documentCategoryVirginValue = {AlUADocumentCategories.miscID},
                                 documentTitleVirginValue = {""},
                                 numPagesVirginValue = {""},
                                 numSourcesVirginValue = {""},
                                 detailsVirginValue = {""})

        o- run {
            val domid = AlBackPile.uuid()
            val buttonBarUUID = AlBackPile.uuid()
            initCommands += AlBackToFrontCommandPile()-{o->
                o.opcode = AlBackToFrontCommandOpcode.CreateControl
                o.controlType = AlControlType.ButtonBarWithTicker
                o.controlUUID = buttonBarUUID
                o.rawDomid = domid
                o.buttons = listOf(
                    AlButtonParams(
                        debugTag = AlDebugTag.submitButton,
                        title = t("TOTE", "Продолжить"),
                        level = AlButtonLevel.Primary,
                        onClick = listOf(
                            AlBackToFrontCommandPile()-{o->
                                o.opcode = AlBackToFrontCommandOpcode.SetTickerActive
                                o.controlUUID = buttonBarUUID
                                o.bool = true
                            },
                            AlBackToFrontCommandPile()-{o->
                                o.opcode = AlBackToFrontCommandOpcode.CallBackend
                                o.postURL = "https://alraune.local/fuckingCall"
                                o.backOpcode = AlFrontToBackCommandOpcode.SubmitOrderCreationForm
                                o.readValuesOfControlsWithUUIDs = inputControlUUIDs
                            }
                        )))
            }
            kdiv(Attrs(id = domid))
        }
        shitToFront2("b690958f-f5d8-40c6-997d-39f5f4debf49") {
            it.commands += initCommands
        }
    }
}

fun handlePost_createOrder() {
    imf("0335d449-9666-466e-9823-ab55c5a644b2")
//    val fields = OrderParamsFields(rctx.postData.orderParams)
//    fields.fieldCtx.validate()
//    shitToFront("d2039b9e-7c7e-4487-b230-78203c35fdf7") {
//        it.replacement_id = AlDomID.replaceableContent
//    }
//    if (fields.fieldCtx.hasErrors) {
//        spitOrderCreationFormPage(fields)
//    } else {
//        validateOrderParamsFields(fields)
//        rctx.createdOrder = alUAOrderRepo.save(newAlUAOrder(
//            uuid = AlBackPile.uuid(), state = UAOrderState.CUSTOMER_DRAFT,
//            email = fields.email.value, contactName = fields.contactName.value, phone = fields.phone.value,
//            documentTitle = fields.documentTitle.value, documentDetails = fields.documentDetails.value,
//            documentTypeID = fields.data.documentTypeID, documentCategoryID = fields.data.documentCategoryID,
//            numPages = fields.numPages.value.toInt(), numSources = fields.numSources.value.toInt()))
//
//        shitToFront("cce77e9c-e7f2-4f17-9554-0e27ee982ed2") {
//            it.hasErrors = false
//            it.historyPushState = makeURLPart(AlPagePath.orderParams, AlGetParams(orderUUID = rctx.order.uuid))
//        }
//        spitOrderParamsPage(fields)
//    }
}

//private fun spitOrderCreationFormPage(fields: OrderParamsFields) {
//    shitToFront("cc6b96c4-d89b-41e9-b4db-85c6d985366e") {
//        it.pageID = AlPageID.orderCreationForm
//        it.postPath = makeURLPart(AlPagePath.post_createOrder)
//    }
//    spitUsualPage(replaceableContent(
//        kdiv()
//            .add(pageTitle(t("TOTE", "Заказ")))
//            .add(renderOrderParamsForm(fields))))
//}

