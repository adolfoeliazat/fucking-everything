package alraune.back

import alraune.back.AlRenderPile.col
import alraune.back.AlRenderPile.pageHeader
import alraune.back.AlRenderPile.row
import alraune.back.AlRenderPile.t
import alraune.shared.AlUADocumentCategories
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
            val validationResults = mutableListOf<ValidationResult>()
            fun addVR(vr: ValidationResult) = run {validationResults.add(vr); vr}

            val email = addVR(AlBackPile.validateEmail(ftb.email))
            val contactName = addVR(AlBackPile.validateName(ftb.contactName))
            val phone = addVR(AlBackPile.validatePhone(ftb.phone))
            val documentTitle = addVR(AlBackPile.validateDocumentTitle(ftb.documentTitle))
            val documentDetails = addVR(AlBackPile.validateDocumentDetails(ftb.documentDetails))
            val numPages = addVR(AlBackPile.validateNumPages(ftb.numPages))
            val numSources = addVR(AlBackPile.validateNumSources(ftb.numSources))
            val documentType = AlUADocumentType.valueOf(ftb.documentType)
            val documentCategory = AlUADocumentCategories.findByIDOrBitch(ftb.documentCategory).id

            if (validationResults.any {it.error != null}) {
                val initCommands = mutableListOf<AlBackToFrontCommandPile>()
                val inputControlUUIDs = mutableListOf<String>()

                val htmlReplacement = kdiv(Attrs(domid = AlDomid.replaceableContent)) {o ->
                    o - pageHeader(t("TOTE", "Йобаный Заказ"))

                    o - kdiv {o ->
                        val cb = ControlBuilder2(initCommands = initCommands,
                                                inputControlUUIDs = inputControlUUIDs)

                        o - row(marginBottom = null) {o ->
                            o - col(4, cb.begin(title = t("TOTE", "Контактное имя"),
                                                prop = AlFrontToBackCommandPile::contactName,
                                                vr = contactName)
                                .text())

                            o - col(4, cb.begin(title = t("TOTE", "Почта"),
                                                prop = AlFrontToBackCommandPile::email,
                                                vr = email)
                                .text())

                            o - col(4, cb.begin(title = t("TOTE", "Телефон"),
                                                prop = AlFrontToBackCommandPile::phone,
                                                vr = phone)
                                .text())
                        }

                        o - row(marginBottom = null) {o ->
                            o - col(4, cb.begin(title = t("TOTE", "Тип документа"),
                                                prop = AlFrontToBackCommandPile::documentType,
                                                vr = ValidationResult(documentType.name, null))
                                .select(values = AlUADocumentType.values().map {
                                    TitledValue(value = it.name, title = it.title)
                                }))

                            o - col(8, cb.begin(title = t("TOTE", "Категория"),
                                                prop = AlFrontToBackCommandPile::documentCategory,
                                                vr = ValidationResult(documentCategory, null))
                                .documentCategoryPicker())
                        }

                        o - cb.begin(title = t("TOTE", "Тема работы (задание)"),
                                     prop = AlFrontToBackCommandPile::documentTitle,
                                     vr = documentTitle)
                            .text()

                        o - row(marginBottom = null) {o ->
                            o - col(6, cb.begin(title = t("TOTE", "Страниц"),
                                                prop = AlFrontToBackCommandPile::numPages,
                                                vr = numPages)
                                .text())

                            o - col(6, cb.begin(title = t("TOTE", "Источников"),
                                                prop = AlFrontToBackCommandPile::numSources,
                                                vr = numSources)
                                .text())
                        }

                        o - cb.begin(title = t("TOTE", "Детали"),
                                     prop = AlFrontToBackCommandPile::documentDetails,
                                     vr = documentDetails)
                            .textArea()

                        initCommands.add(AlBackToFrontCommandPile() - {o ->
                            o.opcode = AlBackToFrontCommandOpcode.FocusControl
                            o.ftbProp = AlFrontToBackCommandPile::contactName
                        })
                    }

                    o - run {
                        val domid = AlBackPile.uuid()
                        val buttonBarUUID = AlBackPile.uuid()
                        initCommands += AlBackToFrontCommandPile() - {o ->
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
                                        AlBackToFrontCommandPile() - {o ->
                                            o.opcode = AlBackToFrontCommandOpcode.SetTickerActive
                                            o.controlUUID = buttonBarUUID
                                            o.bool = true
                                        },
                                        AlBackToFrontCommandPile() - {o ->
                                            o.opcode = AlBackToFrontCommandOpcode.CallBackend
                                            o.postURL = "https://alraune.local/fuckingCall"
                                            o.backOpcode = AlFrontToBackCommandOpcode.SubmitOrderCreationForm
                                            o.readValuesOfControlsWithUUIDs = inputControlUUIDs
                                        }
                                    )))
                        }
                        kdiv(Attrs(id = domid))
                    }
                }

                commands += AlBackToFrontCommandPile()-{o->
                    o.opcode = AlBackToFrontCommandOpcode.ReplaceElement
                    o.domid = AlDomid.replaceableContent
                    o.html = htmlReplacement.render()
                    o.initCommands = initCommands
                }
            } else {
                val order = alUAOrderRepo.save(newAlUAOrder(
                    uuid = AlBackPile.uuid(), state = UAOrderState.CUSTOMER_DRAFT,
                    email = email.sanitizedString, contactName = contactName.sanitizedString, phone = phone.sanitizedString,
                    documentTitle = documentTitle.sanitizedString, documentDetails = documentDetails.sanitizedString,
                    documentType = documentType.name, documentCategory = documentCategory,
                    numPages = numPages.sanitizedString.toInt(), numSources = numSources.sanitizedString.toInt()))
            }
        }

        AlFrontToBackCommandOpcode.SubmitOrderParamsForm -> imf("592f03a8-5453-4c51-b3f5-2e182ebb6b05")
    }

    commands += AlBackToFrontCommandPile()-{o->
        o.opcode = AlBackToFrontCommandOpcode.SayFuckYou
    }

    rctx.res.contentType = "application/json; charset=utf-8"
    rctx.res.writer.print(ObjectMapper().writeValueAsString(response))
}

