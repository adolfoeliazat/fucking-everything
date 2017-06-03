package alraune.back

import vgrechka.*
import alraune.back.AlRenderPile.col
import alraune.back.AlRenderPile.pageHeader
import alraune.back.AlRenderPile.row
import alraune.back.AlRenderPile.t
import alraune.back.FieldSource.*
import alraune.shared.AlUADocumentCategories

enum class FieldSource {INITIAL, POST, DB}

class OrderParamsFields(source: FieldSource) {
    val validationResults = mutableListOf<ValidationResult>()
    fun addVR(vr: ValidationResult) = run {validationResults.add(vr); vr}

    val email = addVR(when (source) {
        INITIAL -> ValidationResult("", null)
        DB -> ValidationResult(rctx.order.email, null)
        POST -> AlBackPile.validateEmail(rctx.ftb.email)})

    val contactName = addVR(when (source) {
        INITIAL -> ValidationResult("", null)
        DB -> ValidationResult(rctx.order.contactName, null)
        POST -> AlBackPile.validateName(rctx.ftb.contactName)})

    val phone = addVR(when (source) {
        INITIAL -> ValidationResult("", null)
        DB -> ValidationResult(rctx.order.phone, null)
        POST -> AlBackPile.validatePhone(rctx.ftb.phone)})

    val documentTitle = addVR(when (source) {
        INITIAL -> ValidationResult("", null)
        DB -> ValidationResult(rctx.order.documentTitle, null)
        POST -> AlBackPile.validateDocumentTitle(rctx.ftb.documentTitle)})

    val documentDetails = addVR(when (source) {
        INITIAL -> ValidationResult("", null)
        DB -> ValidationResult(rctx.order.documentDetails, null)
        POST -> AlBackPile.validateDocumentDetails(rctx.ftb.documentDetails)})

    val numPages = addVR(when (source) {
        INITIAL -> ValidationResult("", null)
        DB -> ValidationResult(rctx.order.numPages.toString(), null)
        POST -> AlBackPile.validateNumPages(rctx.ftb.numPages)})

    val numSources = addVR(when (source) {
        INITIAL -> ValidationResult("", null)
        DB -> ValidationResult(rctx.order.numSources.toString(), null)
        POST -> AlBackPile.validateNumSources(rctx.ftb.numSources)})

    val documentType = when (source) {
        INITIAL -> AlUADocumentType.ABSTRACT
        DB -> rctx.order.documentType.let {AlUADocumentType.valueOf(it)}
        POST -> AlUADocumentType.valueOf(rctx.ftb.documentType)}

    val documentCategory = when (source) {
        INITIAL -> AlUADocumentCategories.miscID
        DB -> rctx.order.documentCategory
        POST -> AlUADocumentCategories.findByIDOrBitch(rctx.ftb.documentCategory).id}
}


fun emitCommandsForRenderingOrderCreationFormPage(commands: MutableList<AlBackToFrontCommandPile>, fields: OrderParamsFields) {
    val initCommands = mutableListOf<AlBackToFrontCommandPile>()
    val inputControlUUIDs = mutableListOf<String>()
    commands += AlBackToFrontCommandPile()-{o->
        o.opcode = AlBackToFrontCommandOpcode.ReplaceElement
        o.domid = AlDomid.replaceableContent
        o.initCommands = initCommands
        o.html = kdiv(Attrs(domid = AlDomid.replaceableContent)){o->
            o- kdiv.className("container"){o->
                o- pageHeader(t("TOTE", "Йобаный Заказ"))
                o- renderOrderParamsFormBody(initCommands, inputControlUUIDs, fields)
                o- renderOrderParamsFormButtons(initCommands, inputControlUUIDs, tickerFloat = "right", ftbOpcode = AlFrontToBackCommandOpcode.SubmitOrderCreationForm)
            }
        }.render()
    }
}

fun renderOrderParamsFormButtons(initCommands: MutableList<AlBackToFrontCommandPile>, inputControlUUIDs: MutableList<String>, tickerFloat: String, ftbOpcode: AlFrontToBackCommandOpcode): Tag {
    val domid = AlBackPile.uuid()
    val buttonBarUUID = AlBackPile.uuid()
    initCommands += AlBackToFrontCommandPile()-{o->
        o.opcode = AlBackToFrontCommandOpcode.CreateControl
        o.controlType = AlControlType.ButtonBarWithTicker
        o.tickerFloat = tickerFloat
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
                        o.ftbOpcode = ftbOpcode
                        o.readValuesOfControlsWithUUIDs = inputControlUUIDs
                        rctx.maybeOrderUUID?.let {
                            o.ftbOrderUUID = it
                        }
                    }
                )))
    }
    return kdiv(Attrs(id = domid))
}

fun renderOrderParamsFormBody(initCommands: MutableList<AlBackToFrontCommandPile>, inputControlUUIDs: MutableList<String>, fields: OrderParamsFields): Tag {
    return kdiv{o->
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
}


