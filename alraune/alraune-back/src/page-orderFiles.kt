package alraune.back

import alraune.back.AlRenderPile.t
import alraune.shared.*
import vgrechka.*

//private fun  _makeSpitOrderTabPagePedro(fields: OrderFileFields): SpitOrderTabPagePedro {
//    return object : SpitOrderTabPagePedro {
//        override val topRightButtonModalTitle = t("New File", "Новый файл")
//        override val pageID = AlPageID.orderFiles
//        override val postPath = AlPagePath.post_createOrderFile
//
//        override fun renderTopRightModalContent(): Renderable {
//            return kdiv{o->
//                o- renderForm(dfctx = fields.fieldCtx,
//                              shitDataNecessaryForControlsToFront = {},
//                              renderFormBody = {kdiv{o->
//                                  o- fields.title.render()
//                                  o- fields.details.render()
//                              }},
//                              submitButtonTitle = t("TOTE", "Создать"))
//            }
//        }
//
//        override val topRightButtonIcon = fa.plus
//        override val activeTab = SpitOrderTabPage.Tab.FILES
//
//        override fun renderContent(): Renderable {
//            return kdiv{o->
//                val files = rctx.order.files
//                if (files.isEmpty()) {
//                    o- t("TOTE", "Савсэм ничего нэт здесь, да...")
//                } else {
//                    for (item in files) {
//                        o- kdiv.id("${AlDomID.itemShit}-${item.uuid}"){o->
//                            val canDelete = true
//                            val c = AlCSS.carla
//                            o- kdiv.className(c.title){o->
//                                o- ki.className(fa.file, c.titleIcon)
//                                o- item.title
//                                o- kspan.className(c.tinySubtitle).text(t("TOTE", "№${item.id}"))
//                                o- kdiv.className(c.titleRightIcons){o->
//                                    fun rightIcon(icon: IconClass, hint: String, style: Style = Style(), action: String, idBase: String) =
//                                        ki(Attrs(id = "$idBase-${item.uuid}",
//                                                 className = "$icon ${c.titleRightIcon}",
//                                                 style = style))
//
//                                    o- rightIcon(fa.cloudDownload, t("Download", "Скачать"), Style(marginTop = "3px", marginRight = "1px"), action = AlSharedPile.action.download, idBase = AlDomID.downloadItemIcon)
//                                    o- rightIcon(fa.pencil, t("Edit", "Изменить"), action = AlSharedPile.action.edit, idBase = AlDomID.editItemIcon)
//                                    if (canDelete)
//                                        o- rightIcon(fa.trash, t("Delete", "Удалить"), action = AlSharedPile.action.delete, idBase = AlDomID.deleteItemIcon)
//                                }
//                            }
//                            o- kdiv.className(c.body){o->
//                                o- item.details
//                            }
//
//                            if (canDelete) {
//                                o- AlRenderPile.renderModal("${AlDomID.deleteItemModal}-${item.uuid}", ModalParams(
//                                    width = "80rem",
//                                    leftMarginColor = Color.RED_300,
//                                    title = t("TOTE", "Удалить файл №${item.id}?"),
//                                    body = insideMarkers(
//                                        id = "${AlDomID.deleteItemModal}-content-${item.uuid}",
//                                        content = kdiv{o->
//                                            o- AlRenderPile.renderFormBannerArea(hasErrors = false, idSuffix = "-${item.uuid}")
//                                            o- kdiv.className(AlCSS.deleteItemModalBodySubtitle).add(item.title)
//                                            o- t("TOTE", "Без приколов, последний раз спрашиваю...")
//                                        }),
//                                    footer = kdiv{o->
//                                        o- kbutton(Attrs(id = "${AlDomID.deleteItemSubmitButton}-${item.uuid}", className = "btn btn-danger"), t("TOTE", "Мочи!"))
//                                        o- kbutton(Attrs(id = "${AlDomID.deleteItemCancelButton}-${item.uuid}", className = "btn btn-default"), t("TOTE", "Я очкую"))
//                                        o- kdiv.id("${AlDomID.deleteItemTicker}-${item.uuid}").className(AlCSS.ticker).amend(Style(float = "left"))
//                                    }
//                                ))
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

fun handleGet_orderFiles() {
    imf("a40baaf9-06d5-4b60-b12d-5d1aa06f3106")
//    shitToFront("954a5058-5ae6-40c7-bb45-06b0eeae8bc7") {
//        it.hasErrors = false
//    }
//    val fields = makeBlankFields()
//    SpitOrderTabPage(_makeSpitOrderTabPagePedro(fields))
}

//private fun makeBlankFields(): OrderFileFields {
//    val orderFileFields = OrderFileFields(newAlUAOrderFile(
//        uuid = "boobs",
//        state = UAOrderFileState.UNKNOWN,
//        name = "",
//        title = "",
//        details = "",
//        order = rctx.order).toForm())
//    orderFileFields.fieldCtx.noValidation()
//    return orderFileFields
//}

fun handlePost_createOrderFile() {
    imf("2644bcdb-6ee7-4f56-b302-c1bc4fe8c40a")
//    shitToFront("b4e2fd47-3a65-41a2-be93-959118883938") {
//        it.hasErrors = true
//    }
//
//    val data = rctx.postData.orderFile
//    val fields = OrderFileFields(data)
//
//    fields.fieldCtx.validate()
//    if (fields.fieldCtx.hasErrors) {
//        shitToFront("030a3b7c-7f4d-4d69-8473-88396049630f") {
//            it.replacement_id = AlDomID.modalContent
//        }
//        val fields = when {
//            fields.fieldCtx.hasErrors -> fields
//            else -> makeBlankFields()
//        }
//        spitUsualPage(kdiv{o->
//
//        })
//    } else {
//        alUAOrderFileRepo.save(newAlUAOrderFile(
//            uuid = AlBackPile.uuid(),
//            state = UAOrderFileState.UNKNOWN,
//            name = "todo",
//            title = fields.title.value,
//            details = fields.details.value,
//            order = rctx.order
//        ))
//        shitToFront("9b4f1a3e-c2ca-4bfb-a567-4a612caa7fc9") {
//            it.hasErrors = false
//            it.replacement_id = AlDomID.replaceableContent
//        }
//        SpitOrderTabPage(_makeSpitOrderTabPagePedro(fields))
//    }
}

//class OrderFileFields(data: OrderFileFormPostData) : WithFieldContext {
//    val f = AlFields.orderFile
//    val v = AlBackPile
//    override val fieldCtx = FieldContext()
//
//    val title = declareField(fieldCtx, data::title, f.title.title, v::validateDocumentTitle)
//    val details = declareField(fieldCtx, data::details, f.documentDetails.title, v::validateDocumentDetails, FieldType.TEXTAREA)
//}

fun handlePost_deleteOrderFile() {
    imf("1885c43f-5245-43a3-a70a-6cb53587a22a")
//    shitToFront("05d379d0-ec4d-4838-be87-79e4aa0067ad") {
//        it.hasErrors = true
//    }
//    val file = alUAOrderFileRepo.findByUuid(rctx.postData.deleteItemPostData.itemUUID) ?: bitch("c68d5abf-05dc-4ad6-88eb-e2a85181e18a")
//    alUAOrderFileRepo.delete(file)
//    shitToFront("b07efec1-70e7-4924-9782-157ecb52075f") {
//        it.hasErrors = false
//    }
//    spitUsualPage(kdiv())
}









