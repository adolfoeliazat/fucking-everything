@file:GSpit(spewClassName = "vgrechka.spew.AlrauneTSInteropSpew", output = "%FE%/alraune/alraune-back/ts/src/generated--ts-interop.ts")
@file:Suppress("unused")
package alraune.back

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import vgrechka.*
import vgrechka.spew.*
import kotlin.reflect.KProperty1

class AlBackToFrontCommandPile {
    lateinit var opcode: AlBackToFrontCommandOpcode
    var name: String? = null
    var title: String? = null
    var stringValue: String? = null
    var error: String? = null
    var titledValues: List<TitledValue>? = null
    var rawDomid: String? = null
    var domid: AlDomid? = null
    var domidSuffix: String? = null
    var html: String? = null
    var initCommands: List<AlBackToFrontCommandPile>? = null
    var commands: List<AlBackToFrontCommandPile>? = null
    var putInFormGroup: Boolean? = null
    var controlType: AlControlType? = null
    var controlUUID: String? = null
    var readValuesOfControlsWithUUIDs: List<String>? = null
    var buttons: List<AlButtonParams>? = null
    var bool: Boolean? = null
    var ftbOpcode: AlFrontToBackCommandOpcode? = null
    var postURL: String? = null
    var errorBannerPlaceholderDomidForLowLevelPostFailure: String? = null
    var href: String? = null
    var tickerFloat: String? = null
    var ftbOrderUUID: String? = null
    var ftbItemUUID: String? = null

    @JsonSerialize(using = PropertyNameSerializer::class)
    var ftbProp: KProperty1<AlFrontToBackCommandPile, *>? = null
}

class AlBackResponsePile {
    var commands: List<AlBackToFrontCommandPile>? = null
}

class AlButtonParams(
    val debugTag: AlDebugTag? = null,
    val title: String,
    val level: AlButtonLevel,
    val onClick: List<AlBackToFrontCommandPile>
)

enum class AlButtonLevel {
    Default, Primary, Success, Info, Warning, Danger
}

enum class AlBackToFrontCommandOpcode {
    CreateControl, OpenModalOnElementClick, FocusControl, SayFuckYou, SetTickerActive, CallBackend,
    ReplaceElement, SetLocationHref, OnClick, CloseModal, FuckElementOut
}

enum class AlControlType {
    Text, TextArea, Select, DocumentCategoryPicker, ButtonBarWithTicker
}

class TitledValue(val value: String, val title: String)

@Ser data class AlFrontToBackCommandPile(
    val opcode: AlFrontToBackCommandOpcode,
    val orderUUID: String?,
    val itemUUID: String?,
    val email: String,
    val contactName: String,
    val phone: String,
    val documentType: String,
    val documentTitle: String,
    val documentDetails: String,
    val documentCategory: String,
    val numPages: String,
    val numSources: String,
    val fileUUID: String?,
    val title: String,
    val details: String
)

enum class AlFrontToBackCommandOpcode {
    SubmitOrderCreationForm, SubmitOrderParamsForm
}

enum class AlDebugTag {
    submitButton,
    topRightButton
}

enum class AlDomid {
    shitPassedFromBackToFront,
    shitPassedFromBackToFront2,
    submitButton,
    ticker,
    replaceableContent,
    documentCategoryPickerContainer,
    filePickerContainer,
    submitOrderForReviewButton,
    topRightButton,
    orderParamsModal,
    closeModalButton,
    modalContent,
    formBannerArea,
    formFooterArea,
    serviceFuckedUpBanner,
    deleteItemModal,
    deleteItemTicker,
    deleteItemSubmitButton,
    deleteItemCancelButton,
    downloadItemIcon,
    editItemIcon,
    deleteItemIcon,
    deleteItemModalContent,
    itemShit,
    orderParams
}



