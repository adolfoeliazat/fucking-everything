package alraune.back

import alraune.back.AlBackPile.validateInt
import alraune.back.AlBackPile.validateString
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
    private val entity by lazy {rctx.order}

    private fun addVR(vr: ValidationResult) = run {validationResults.add(vr); vr}

    val email = addVR(when (source) {
        INITIAL -> blankVR()
        DB -> ValidationResult(entity.email)
        POST -> AlBackPile.validateEmail(rctx.ftb.email)})

    val contactName = addVR(when (source) {
        INITIAL -> blankVR()
        DB -> ValidationResult(entity.contactName)
        POST -> validateString(rctx.ftb.contactName, minLen = 3, maxLen = 50)})

    val phone = addVR(when (source) {
        INITIAL -> blankVR()
        DB -> ValidationResult(entity.phone)
        POST -> AlBackPile.validatePhone(rctx.ftb.phone)})

    val documentTitle = addVR(when (source) {
        INITIAL -> blankVR()
        DB -> ValidationResult(entity.documentTitle)
        POST -> AlBackPile.validateGenericTitle(rctx.ftb.title)})

    val documentDetails = addVR(when (source) {
        INITIAL -> blankVR()
        DB -> ValidationResult(entity.documentDetails)
        POST -> AlBackPile.validateGenericDetails(rctx.ftb.details)})

    val numPages = addVR(when (source) {
        INITIAL -> blankVR()
        DB -> ValidationResult(entity.numPages.toString())
        POST -> validateInt(rctx.ftb.numPages, min = 3, max = 500)})

    val numSources = addVR(when (source) {
        INITIAL -> blankVR()
        DB -> ValidationResult(entity.numSources.toString())
        POST -> validateInt(rctx.ftb.numSources, min = 0, max = 50)})

    val documentType = when (source) {
        INITIAL -> AlUADocumentType.ABSTRACT
        DB -> entity.documentType.let {AlUADocumentType.valueOf(it)}
        POST -> AlUADocumentType.valueOf(rctx.ftb.documentType)}

    val documentCategory = when (source) {
        INITIAL -> AlUADocumentCategories.miscID
        DB -> entity.documentCategory
        POST -> AlUADocumentCategories.findByIDOrBitch(rctx.ftb.documentCategory).id}
}

private fun blankVR() = ValidationResult("")

class FileFields(source: FieldSource) {
    val validationResults = mutableListOf<ValidationResult>()
    private val entity by lazy {rctx.file}

    private fun addVR(vr: ValidationResult) = run {validationResults.add(vr); vr}

    val title = addVR(when (source) {
        INITIAL -> blankVR()
        DB -> ValidationResult(entity.title)
        POST -> AlBackPile.validateGenericTitle(rctx.ftb.title)
    })

    val details = addVR(when (source) {
        INITIAL -> blankVR()
        DB -> ValidationResult(entity.details)
        POST -> AlBackPile.validateGenericDetails(rctx.ftb.details)
    })
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
    return renderFuckingButtons(initCommands, tickerFloat, ftbOpcode, inputControlUUIDs, t("TOTE", "Продолжить"))
}

private fun renderFuckingButtons(initCommands: MutableList<AlBackToFrontCommandPile>, tickerFloat: String, ftbOpcode: AlFrontToBackCommandOpcode, inputControlUUIDs: MutableList<String>, submitButtonTitle: String): Tag {
    val domid = AlBackPile.uuid()
    val buttonBarUUID = AlBackPile.uuid()
    initCommands += AlBackToFrontCommandPile() - {o ->
        o.opcode = AlBackToFrontCommandOpcode.CreateControl
        o.controlType = AlControlType.ButtonBarWithTicker
        o.tickerFloat = tickerFloat
        o.controlUUID = buttonBarUUID
        o.rawDomid = domid
        o.buttons = listOf(
            AlButtonParams(
                debugTag = AlDebugTag.submitButton,
                title = submitButtonTitle,
                level = AlButtonLevel.Primary,
                onClick = listOf(
                    AlBackToFrontCommandPile() - {o ->
                        o.opcode = AlBackToFrontCommandOpcode.SetTickerActive
                        o.controlUUID = buttonBarUUID
                        o.bool = true
                    },
                    AlBackToFrontCommandPile() - {o ->
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

fun renderFileFormButtons(initCommands: MutableList<AlBackToFrontCommandPile>, inputControlUUIDs: MutableList<String>, tickerFloat: String, ftbOpcode: AlFrontToBackCommandOpcode): Tag {
    return renderFuckingButtons(initCommands, tickerFloat, ftbOpcode, inputControlUUIDs, t("TOTE", "Создать"))
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
                    prop = AlFrontToBackCommandPile::title,
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
                    prop = AlFrontToBackCommandPile::details,
                    vr = fields.documentDetails)
            .textArea()

        initCommands.add(AlBackToFrontCommandPile()-{o->
            o.opcode = AlBackToFrontCommandOpcode.FocusControl
            o.ftbProp = AlFrontToBackCommandPile::contactName
        })
    }
}

fun renderFileFormBody(initCommands: MutableList<AlBackToFrontCommandPile>, inputControlUUIDs: MutableList<String>, fields: FileFields): Tag {
    return kdiv{o->
        val cb = ControlBuilder2(commands = initCommands,
                                 inputControlUUIDs = inputControlUUIDs)

        o- cb.begin(title = t("TOTE", "Название"),
                    prop = AlFrontToBackCommandPile::title,
                    vr = fields.title)
            .text()

        o- cb.begin(title = t("TOTE", "Детали"),
                    prop = AlFrontToBackCommandPile::details,
                    vr = fields.details)
            .textArea()
    }
}


