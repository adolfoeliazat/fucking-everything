package alraune.back

import alraune.back.AlRenderPile.t
import alraune.shared.*
import java.util.*

interface Ctx100 {
    val order: AlUAOrder
}

private fun  _makeSpitOrderTabPagePedro(ctx: Ctx100, fields: OrderFileFields): SpitOrderTabPagePedro {
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
                o- "piiiiiiiiiiiiiiiiiiizdaaaaaaaaaaa"
            }
        }
    }
}

fun handleGet_orderFiles() {
    // TODO:vgrechka Algo1 one should get (Algo1) -> Algo1Pedro parameter
    Algo1(object : Algo1Pedro {
        override fun makeSpitOrderTabPagePedro(ctx: Algo1): SpitOrderTabPagePedro {
            return _makeSpitOrderTabPagePedro(ctx, OrderFileFields(newAlUAOrderFile(
                uuid = "boobs",
                state = UAOrderFileState.UNKNOWN,
                name = "",
                title = "",
                details = "",
                order = ctx.order).toForm())
            )
        }
    })
}

fun handlePost_addOrderFile() {
    HandleOrderTabPagePost(
        postDataClass = OrderFileFormPostData::class,
        fieldsCtor = ::OrderFileFields,
        makePedro = {ctx-> object:HandleOrderTabPagePostPedro<OrderFileFormPostData, OrderFileFields> {
            override fun spitPage() {
                SpitOrderTabPage(ctx.order, _makeSpitOrderTabPagePedro(ctx, ctx.fields))
            }

            override fun validateDataAndUpdateDB() {
                alUAOrderFileRepo.save(newAlUAOrderFile(
                    uuid = UUID.randomUUID().toString(),
                    state = UAOrderFileState.UNKNOWN,
                    name = "todo",
                    title = ctx.fields.title.value,
                    details = ctx.fields.details.value,
                    order = ctx.order
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










