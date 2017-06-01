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
    var initCommands: MutableList<AlBackToFrontCommandPile>? = null
    var putInFormGroup: Boolean? = null
    var controlType: AlControlType? = null

    @JsonSerialize(using = PropertyNameSerializer::class)
    var ftbProp: KProperty1<AlFrontToBackCommandPile, *>? = null
}

enum class AlBackToFrontCommandOpcode {
    CreateControl, OpenModalOnElementClick, FocusControl
}

enum class AlControlType {
    Text, TextArea, Select, DocumentCategoryPicker
}

class TitledValue(val value: String, val title: String)

@Ser data class AlFrontToBackCommandPile(
    val opcode: AlFrontToBackCommandOpcode,
    val orderUUID: String,
    val itemUUID: String,
    val email: String,
    val name: String,
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
    SubmitOrderParamsForm
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
    itemShit
}



