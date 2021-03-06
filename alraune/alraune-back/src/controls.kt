package alraune.back

import vgrechka.*
import kotlin.reflect.KProperty1

//class ControlBuilder(val initCommands: MutableList<AlBackToFrontCommandPile>, val inputControlUUIDs: MutableList<String>) {
//
//    inner class begin(
//        val title: String,
//        val prop: KProperty1<AlFrontToBackCommandPile, String>,
//        val validate: (String?) -> ValidationResult,
//        val virginValue: () -> String
//    ) {
//        val cmd = AlBackToFrontCommandPile()
//        val vr = when {
//            rctx.isPost -> validate(prop.get(rctx.postData.pile))
//            else -> ValidationResult(virginValue(), null)
//        }
//
//        init {
//            cmd.opcode = AlBackToFrontCommandOpcode.CreateControl
//            cmd.rawDomid = AlBackPile.uuid()
//            cmd.controlUUID = AlBackPile.uuid()
//            cmd.name = prop.name
//            cmd.error = vr.error
////            cmd.error = "pizdets"
//            cmd.putInFormGroup = true
//            cmd.title = title
//            cmd.ftbProp = prop
//
//            inputControlUUIDs += cmd.controlUUID!!
//            initCommands += cmd
//        }
//
//        private fun simple(type: AlControlType): Renderable {
//            cmd.controlType = type
//            cmd.stringValue = vr.sanitizedString
//            return render()
//        }
//
//        fun text() = simple(AlControlType.Text)
//        fun textArea() = simple(AlControlType.TextArea)
//        fun documentCategoryPicker() = simple(AlControlType.DocumentCategoryPicker)
//
//        fun select(values: List<TitledValue>): Renderable {
//            cmd.titledValues = values
//            return simple(AlControlType.Select)
//        }
//
//        private fun render(): Renderable {
//            clog("Control creation command: controlUUID = ${cmd.controlUUID}; controlType = ${cmd.controlType}")
//            // clog(ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(cmd))
//            return kdiv(Attrs(id = cmd.rawDomid))
//        }
//    }
//
//
//}

class ControlBuilder2(val commands: MutableList<AlBackToFrontCommandPile>, val inputControlUUIDs: MutableList<String>) {

    inner class begin(
        val title: String,
        val prop: KProperty1<AlFrontToBackCommandPile, String>,
        val vr: ValidationResult
    ) {
        val cmd = AlBackToFrontCommandPile()

        init {
            cmd.opcode = AlBackToFrontCommandOpcode.CreateControl
            cmd.rawDomid = AlBackPile.uuid()
            cmd.controlUUID = AlBackPile.uuid()
//            cmd.name = prop.name
            cmd.error = vr.error
            cmd.putInFormGroup = true
            cmd.title = title
            cmd.ftbProp = prop

            inputControlUUIDs += cmd.controlUUID!!
            commands += cmd
        }

        private fun simple(type: AlControlType): Renderable {
            cmd.controlType = type
            cmd.stringValue = vr.sanitizedString
            return render()
        }

        fun text() = simple(AlControlType.Text)
        fun textArea() = simple(AlControlType.TextArea)
        fun documentCategoryPicker() = simple(AlControlType.DocumentCategoryPicker)

        fun select(values: List<TitledValue>): Renderable {
            cmd.titledValues = values
            return simple(AlControlType.Select)
        }

        private fun render(): Renderable {
            clog("Control creation command: controlUUID = ${cmd.controlUUID}; controlType = ${cmd.controlType}")
            // clog(ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(cmd))
            return kdiv(Attrs(id = cmd.rawDomid))
        }
    }


}


//fun renderTextControl(initCommands: MutableList<AlBackToFrontCommand>,
//                      validateFromPostData: () -> ValidationResult,
//                      prop: KProperty1<*, String>,
//                      title: String,
//                      fieldType: FieldType,
//                      initialValue: String
//): Renderable {
//    val vr = when {
//        rctx.isPost -> validateFromPostData()
//        else -> ValidationResult(sanitizedString = initialValue, error = null)
//    }
//
//    return kdiv.className("form-group"){o->
//        if (vr.error != null)
//            o.amend(Style(marginBottom = "0"))
//        o- klabel(text = title)
//        val control = when (fieldType) {
//            FieldType.TEXT -> {
//                renderCommandPlaceholder(initCommands) {placeholderDomid->
//                    CreateTextControlCommand(
//                        placeHolderDomid = placeholderDomid,
//                        propName = prop.name,
//                        value = vr.sanitizedString)
//                }
//            }
//            FieldType.TEXTAREA -> {
//                // ktextarea(Attrs(id = domid, rows = 5, className = "form-control"), text = vr.sanitizedString)
//                imf("af9b81c5-acc2-4536-a744-b2862e054515")
//            }
//        }
//        o- kdiv(Style(position = "relative")){o->
//            o- control
//            if (vr.error != null) {
//                o- kdiv(Style(marginTop = "5px", marginRight = "9px", textAlign = "right", color = "${Color.RED_700}"))
//                    .text(vr.error)
//                // TODO:vgrechka Shift red circle if control has scrollbar
//                o- kdiv(Style(width = "15px", height = "15px", backgroundColor = "${Color.RED_300}",
//                              borderRadius = "10px", position = "absolute", top = "10px", right = "8px"))
//            }
//        }
//    }
//}
//
//fun renderSelectControl(initCommands: MutableList<AlBackToFrontCommand>,
//                        validateFromPostData: () -> ValidationResult,
//                        prop: KProperty1<*, String>,
//                        title: String,
//                        items: SelectControlItem,
//                        initialValueForGetRequest
//): Renderable {
//    val vr = when {
//        rctx.isPost -> validateFromPostData()
//        else -> ValidationResult(sanitizedString = initialValue, error = null)
//    }
//
//    return kdiv.className("form-group"){o->
//        o- klabel(text = t("TOTE", "Тип документа"))
//        o- renderCommandPlaceholder(initCommands) {placeholderDomid->
//            CreateSelectControlCommand(
//                placeHolderDomid = placeholderDomid,
//                propName = prop.name,
//                value = vr.sanitizedString)
//        }
//    }
//
//    return kselect(Attrs(id = AlSharedPile.fieldDOMID(name = OrderParamsFormPostData::documentType.name),
//                         className = "form-control")) {o->
//        val selectedDocumentType = when {
//            rctx.isPost -> myPostData().documentType
//            else -> AlDocumentType.ABSTRACT.name
//        }
//        for (value in AlDocumentType.values()) {
//            o- koption(Attrs(value = value.name,
//                             selected = selectedDocumentType == value.name),
//                       value.title)
//        }
//    }
//}



