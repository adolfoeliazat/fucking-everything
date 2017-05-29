package alraune.back

import alraune.back.AlRenderPile.t
import alraune.shared.*
import vgrechka.*

private fun  _makeSpitOrderTabPagePedro(fields: OrderFileFields): SpitOrderTabPagePedro {
    return object : SpitOrderTabPagePedro {
        override val topRightButtonModalTitle = t("New File", "Новый файл")
        override val pageID = AlPageID.orderFiles
        override val postPath = AlPagePath.post_addOrderFile

        override fun renderTopRightModalContent(): Renderable {
            return kdiv{o->
                o- renderForm(dfctx = fields.fieldCtx,
                              shitDataNecessaryForControlsToFront = {},
                              renderFormBody = {kdiv{o->
                                  o- fields.title.render()
                                  o- fields.details.render()
                              }},
                              submitButtonTitle = t("TOTE", "Создать"))
            }
        }

        override val topRightButtonIcon = fa.plus
        override val activeTab = SpitOrderTabPage.Tab.FILES

        override fun renderContent(): Renderable {
            return kdiv{o->
                val files = rctx.order.files
                if (files.isEmpty()) {
                    o- t("TOTE", "Савсэм ничего нэт здесь, да...")
                } else {
                    for (item in files) {
                        o- kdiv.id("${AlDomID.itemShit}-${item.uuid}"){o->
                            val canDelete = true
                            val c = AlCSS.carla
                            o- kdiv.className(c.title){o->
                                o- ki.className(fa.file, c.titleIcon)
                                o- item.title
                                o- kspan.className(c.tinySubtitle).text(t("TOTE", "№${item.id}"))
                                o- kdiv.className(c.titleRightIcons){o->
                                    fun rightIcon(icon: IconClass, hint: String, style: Style = Style(), action: String, idBase: String) =
                                        ki(Attrs(id = "$idBase-${item.uuid}",
                                                 className = "$icon ${c.titleRightIcon}",
                                                 style = style))

                                    o- rightIcon(fa.cloudDownload, t("Download", "Скачать"), Style(marginTop = "3px", marginRight = "1px"), action = AlSharedPile.action.download, idBase = AlDomID.downloadItemIcon)
                                    o- rightIcon(fa.pencil, t("Edit", "Изменить"), action = AlSharedPile.action.edit, idBase = AlDomID.editItemIcon)
                                    if (canDelete)
                                        o- rightIcon(fa.trash, t("Delete", "Удалить"), action = AlSharedPile.action.delete, idBase = AlDomID.deleteItemIcon)
                                }
                            }
                            o- kdiv.className(c.body){o->
                                o- item.details
                            }

                            if (canDelete) {
                                o- AlRenderPile.renderModal("${AlDomID.deleteItemModal}-${item.uuid}", ModalParams(
                                    width = "80rem",
                                    leftMarginColor = Color.RED_300,
                                    title = t("TOTE", "Удалить файл №${item.id}?"),
                                    body = insideMarkers(
                                        id = "${AlDomID.deleteItemModal}-content-${item.uuid}",
                                        content = kdiv{o->
                                            o- AlRenderPile.renderFormBannerArea(hasErrors = false, idSuffix = "-${item.uuid}")
                                            o- kdiv.className(AlCSS.deleteItemModalBodySubtitle).add(item.title)
                                            o- t("TOTE", "Без приколов, последний раз спрашиваю...")
                                        }),
                                    footer = kdiv{o->
                                        o- kbutton(Attrs(id = "${AlDomID.deleteItemSubmitButton}-${item.uuid}", className = "btn btn-danger"), t("TOTE", "Мочи!"))
                                        o- kbutton(Attrs(id = "${AlDomID.deleteItemCancelButton}-${item.uuid}", className = "btn btn-default"), t("TOTE", "Я очкую"))
                                        o- kdiv.id("${AlDomID.deleteItemTicker}-${item.uuid}").className(AlCSS.ticker).amend(Style(float = "left"))
                                    }
                                ))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun handleGet_orderFiles() {
    // TODO:vgrechka Algo1 one should get (Algo1) -> Algo1Pedro parameter
    Algo1(object : Algo1Pedro {
        override fun makeSpitOrderTabPagePedro(ctx: Algo1): SpitOrderTabPagePedro {
            val fields = makeBlankFields()
            return _makeSpitOrderTabPagePedro(fields)
        }
    })
}

private fun makeBlankFields(): OrderFileFields {
    val orderFileFields = OrderFileFields(newAlUAOrderFile(
        uuid = "boobs",
        state = UAOrderFileState.UNKNOWN,
        name = "",
        title = "",
        details = "",
        order = rctx.order).toForm())
    orderFileFields.fieldCtx.noValidation()
    return orderFileFields
}

fun handlePost_addOrderFile() {
    HandleOrderTabPagePost(
        getPostData = {rctx.postData.orderFile},
        fieldsCtor = ::OrderFileFields,
        makePedro = {ctx-> object:HandleOrderTabPagePostPedro<OrderFileFormPostData, OrderFileFields> {
            override fun spitPage() {
                val fields = when {
                    ctx.fields.fieldCtx.hasErrors -> ctx.fields
                    else -> makeBlankFields()
                }
                SpitOrderTabPage(_makeSpitOrderTabPagePedro(fields))
            }

            override fun validateDataAndUpdateDB() {
                alUAOrderFileRepo.save(newAlUAOrderFile(
                    uuid = AlBackPile.uuid(),
                    state = UAOrderFileState.UNKNOWN,
                    name = "todo",
                    title = ctx.fields.title.value,
                    details = ctx.fields.details.value,
                    order = rctx.order
                ))
            }

            override fun additionallyShitToFrontOnSuccess(shit: PieceOfShitFromBack) {}
        }}
    )
}

class OrderFileFields(data: OrderFileFormPostData) : WithFieldContext {
    val f = AlFields.orderFile
    val v = AlBackPile
    override val fieldCtx = FieldContext()

    val title = declareField(fieldCtx, data::title, f.title.title, v::validateDocumentTitle)
    val details = declareField(fieldCtx, data::details, f.documentDetails.title, v::validateDocumentDetails, FieldType.TEXTAREA)
}

fun handlePost_deleteOrderFile() {
    shitToFront("05d379d0-ec4d-4838-be87-79e4aa0067ad") {
        it.hasErrors = true
    }
    val file = alUAOrderFileRepo.findByUuid(rctx.postData.deleteItemPostData.itemUUID) ?: bitch("c68d5abf-05dc-4ad6-88eb-e2a85181e18a")
    alUAOrderFileRepo.delete(file)
    shitToFront("b07efec1-70e7-4924-9782-157ecb52075f") {
        it.hasErrors = false
    }
    spitUsualPage(kdiv())
}









