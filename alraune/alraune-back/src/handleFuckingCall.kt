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

    exhaustive=when (ftb.opcode) {
        AlFrontToBackCommandOpcode.SubmitOrderCreationForm -> {
            val email = AlBackPile.validateEmail(ftb.email)
            val contactName = AlBackPile.validateName(ftb.contactName)
            val phone = AlBackPile.validatePhone(ftb.phone)
            val documentTitle = AlBackPile.validateDocumentTitle(ftb.documentTitle)
            val documentDetails = AlBackPile.validateDocumentDetails(ftb.documentDetails)
            val documentType = AlUADocumentType.valueOf(ftb.documentType)
            val documentCategory = ftb.documentCategory
            val numPages = ftb.numPages.toInt()
            val numSources = ftb.numSources.toInt()

            val order = alUAOrderRepo.save(newAlUAOrder(
                uuid = AlBackPile.uuid(), state = UAOrderState.CUSTOMER_DRAFT,
                email = email.sanitizedString, contactName = contactName.sanitizedString, phone = phone.sanitizedString,
                documentTitle = documentTitle.sanitizedString, documentDetails = documentDetails.sanitizedString,
                documentType = documentType.name, documentCategory = documentCategory,
                numPages = numPages, numSources = numSources))
        }

        AlFrontToBackCommandOpcode.SubmitOrderParamsForm -> imf("592f03a8-5453-4c51-b3f5-2e182ebb6b05")
    }

    commands += AlBackToFrontCommandPile()-{o->
        o.opcode = AlBackToFrontCommandOpcode.SayFuckYou
    }

    rctx.res.contentType = "application/json; charset=utf-8"
    rctx.res.writer.print(ObjectMapper().writeValueAsString(response))
}