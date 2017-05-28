package alraune.back

import alraune.back.AlRenderPile.t
import alraune.shared.*
import java.util.*

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
                for (file in rctx.order.files) {
                    val c = AlCSS.carla
                    o- kdiv.className(c.pizda){o->
                        o- file.title
                    }
                    o- kdiv.className(c.cunt){o->
                        o- file.details
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










