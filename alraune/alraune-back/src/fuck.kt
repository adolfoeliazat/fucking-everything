package alraune.back

import vgrechka.*
import alraune.back.AlRenderPile.col
import alraune.back.AlRenderPile.pageHeader
import alraune.back.AlRenderPile.row
import alraune.back.AlRenderPile.t
import alraune.shared.AlUADocumentCategories

class OrderParamsFields(ftb: AlFrontToBackCommandPile?) {
    val validationResults = mutableListOf<ValidationResult>()
    fun addVR(vr: ValidationResult) = run {validationResults.add(vr); vr}

    val email = addVR(if (ftb != null) AlBackPile.validateEmail(ftb.email) else ValidationResult("", null))
    val contactName = addVR(if (ftb != null) AlBackPile.validateName(ftb.contactName) else ValidationResult("", null))
    val phone = addVR(if (ftb != null) AlBackPile.validatePhone(ftb.phone) else ValidationResult("", null))
    val documentTitle = addVR(if (ftb != null) AlBackPile.validateDocumentTitle(ftb.documentTitle) else ValidationResult("", null))
    val documentDetails = addVR(if (ftb != null) AlBackPile.validateDocumentDetails(ftb.documentDetails) else ValidationResult("", null))
    val numPages = addVR(if (ftb != null) AlBackPile.validateNumPages(ftb.numPages) else ValidationResult("", null))
    val numSources = addVR(if (ftb != null) AlBackPile.validateNumSources(ftb.numSources) else ValidationResult("", null))
    val documentType = if (ftb != null) AlUADocumentType.valueOf(ftb.documentType) else AlUADocumentType.ABSTRACT
    val documentCategory = if (ftb != null) AlUADocumentCategories.findByIDOrBitch(ftb.documentCategory).id else AlUADocumentCategories.miscID
}

fun emitCommandsForRenderingOrderCreationFormPage(commands: MutableList<AlBackToFrontCommandPile>, fields: OrderParamsFields) {
    val initCommands = mutableListOf<AlBackToFrontCommandPile>()
    val inputControlUUIDs = mutableListOf<String>()

    commands += AlBackToFrontCommandPile()-{o->
        o.opcode = AlBackToFrontCommandOpcode.ReplaceElement
        o.domid = AlDomid.replaceableContent
        o.initCommands = initCommands
        o.html = kdiv(Attrs(domid = AlDomid.replaceableContent, className = "container")){o->
            o- pageHeader(t("TOTE", "Йобаный Заказ"))

            o- kdiv{o->
                val cb = ControlBuilder2(commands = initCommands,
                                         inputControlUUIDs = inputControlUUIDs)

                o- row(marginBottom = null){o->
                    o- col(4, cb.begin(title = t("TOTE", "Контактное имя"),
                                       prop = AlFrontToBackCommandPile::contactName,
                                       vr = fields.contactName)
                        .text())

                    o- col(4, cb.begin(title = t("TOTE", "Почта"),
                                       prop = AlFrontToBackCommandPile::email,
                                       vr = fields.email)
                        .text())

                    o- col(4, cb.begin(title = t("TOTE", "Телефон"),
                                       prop = AlFrontToBackCommandPile::phone,
                                       vr = fields.phone)
                        .text())
                }

                o- row(marginBottom = null){o->
                    o- col(4, cb.begin(title = t("TOTE", "Тип документа"),
                                       prop = AlFrontToBackCommandPile::documentType,
                                       vr = ValidationResult(fields.documentType.name, null))
                        .select(values = AlUADocumentType.values().map {
                            TitledValue(value = it.name, title = it.title)
                        }))

                    o- col(8, cb.begin(title = t("TOTE", "Категория"),
                                       prop = AlFrontToBackCommandPile::documentCategory,
                                       vr = ValidationResult(fields.documentCategory, null))
                        .documentCategoryPicker())
                }

                o- cb.begin(title = t("TOTE", "Тема работы (задание)"),
                            prop = AlFrontToBackCommandPile::documentTitle,
                            vr = fields.documentTitle)
                    .text()

                o- row(marginBottom = null){o->
                    o- col(6, cb.begin(title = t("TOTE", "Страниц"),
                                       prop = AlFrontToBackCommandPile::numPages,
                                       vr = fields.numPages)
                        .text())

                    o- col(6, cb.begin(title = t("TOTE", "Источников"),
                                       prop = AlFrontToBackCommandPile::numSources,
                                       vr = fields.numSources)
                        .text())
                }

                o- cb.begin(title = t("TOTE", "Детали"),
                            prop = AlFrontToBackCommandPile::documentDetails,
                            vr = fields.documentDetails)
                    .textArea()

                initCommands.add(AlBackToFrontCommandPile()-{o->
                    o.opcode = AlBackToFrontCommandOpcode.FocusControl
                    o.ftbProp = AlFrontToBackCommandPile::contactName
                })
            }

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
                                    o.postURL = "${AlBackPile0.baseURL}/fuckingCall"
                                    o.backOpcode = AlFrontToBackCommandOpcode.SubmitOrderCreationForm
                                    o.readValuesOfControlsWithUUIDs = inputControlUUIDs
                                }
                            )))
                }
                kdiv(Attrs(id = domid))
            }
        }.render()
    }
}


