package alraune.back

import alraune.back.AlRenderPile.pageTitle
import alraune.back.AlRenderPile.t
import alraune.shared.*
import java.util.*

fun handleGet_orderCreationForm() {
    val fields = OrderParamsFields(
        newAlUAOrder(
            uuid = "boobs", state = UAOrderState.CUSTOMER_DRAFT, email = "",
            contactName = "", phone = "", documentTypeID = AlDocumentType.ABSTRACT.name, documentTitle = "",
            documentDetails = "", documentCategoryID = AlDocumentCategories.miscID, numPages = -1, numSources = -1)
            .toForm())
    fields.fieldCtx.noValidation()
    spitOrderCreationFormPage(fields)
}

fun handlePost_createOrder() {
    val fields = OrderParamsFields(rctx.orderCreationFormPostData)
    fields.fieldCtx.validate()
    shitToFront("d2039b9e-7c7e-4487-b230-78203c35fdf7") {
        it.replacement_id = AlDomID.replaceableContent
    }
    if (fields.fieldCtx.hasErrors) {
        spitOrderCreationFormPage(fields)
    } else {
        validateOrderParamsFields(fields)
        rctx.createdOrder = alUAOrderRepo.save(newAlUAOrder(
            uuid = UUID.randomUUID().toString(), state = UAOrderState.CUSTOMER_DRAFT,
            email = fields.email.value, contactName = fields.contactName.value, phone = fields.phone.value,
            documentTitle = fields.documentTitle.value, documentDetails = fields.documentDetails.value,
            documentTypeID = fields.data.documentTypeID, documentCategoryID = fields.data.documentCategoryID,
            numPages = fields.numPages.value.toInt(), numSources = fields.numSources.value.toInt()))

        shitToFront("cce77e9c-e7f2-4f17-9554-0e27ee982ed2") {
            it.hasErrors = false
            it.historyPushState = makeURLPart(AlPagePath.orderParams, AlGetParams(orderUUID = rctx.order.uuid))
        }
        spitOrderParamsPage(fields)
    }
}

private fun spitOrderCreationFormPage(fields: OrderParamsFields) {
    shitToFront("cc6b96c4-d89b-41e9-b4db-85c6d985366e") {
        it.pageID = AlPageID.orderCreationForm
        it.postPath = makeURLPart(AlPagePath.post_createOrder)
    }
    spitUsualPage(replaceableContent(
        kdiv()
            .add(pageTitle(t("TOTE", "Заказ")))
            .add(renderOrderParamsForm(fields))))
}

