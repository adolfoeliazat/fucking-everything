package alraune.back

import alraune.back.AlRenderPile.t
import alraune.shared.*

interface Ctx100 {
    val order: AlUAOrder
}

private fun  _makeSpitOrderTabPagePedro(ctx: Ctx100): SpitOrderTabPagePedro {
    return object : SpitOrderTabPagePedro {
        override val topRightButtonModalTitle = t("New File", "Новый файл")
        override val pageID = AlPageID.orderFiles
        override val postPath = AlPagePath.post_addOrderFile

        override fun renderTopRightModalContent(): Renderable {
            return kdiv{o->
                val fields = OrderFileFields(newAlUAOrderFile(
                    uuid = "boobs",
                    state = UAOrderFileState.UNKNOWN,
                    name = "",
                    title = "",
                    details = "",
                    order = ctx.order).toForm())
                o- renderForm(dfctx = fields.fieldCtx,
                              shitDataNecessaryForControlsToFront = {},
                              renderFormBody = {kdiv{o->
                                  o- fields.title.render()
                                  o- fields.details.render()
                              }})
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
        override fun makeSpitOrderTabPagePedro(algo1: Algo1): SpitOrderTabPagePedro {
            return _makeSpitOrderTabPagePedro(algo1)
        }
    })
}

fun handlePost_addOrderFile() {
    // TODO:vgrechka Whole this thing should be in transaction
    HandleOrderTabPagePost(
        postDataClass = OrderFileFormPostData::class,
        fieldsCtor = ::OrderFileFields,
        makePedro = {ctx-> object:HandleOrderTabPagePostPedro<OrderFileFormPostData, OrderFileFields> {
            override fun spitPage() {
                SpitOrderTabPage(ctx.order, _makeSpitOrderTabPagePedro(ctx))
            }

            override fun validateDataAndUpdateDB() {
                // TODO:vgrechka Show error to user instead of just dying
                AlBackPile0.log.info("-----> title = ${ctx.fields.title}")
                AlBackPile0.log.info("-----> details = ${ctx.fields.details}")

//                ctx.order.email = ctx.fields.email.value
//                ctx.order.contactName = ctx.fields.contactName.value
//                ctx.order.phone = ctx.fields.phone.value
//                ctx.order.documentTitle = ctx.fields.documentTitle.value
//                ctx.order.documentDetails = ctx.fields.documentDetails.value
//                ctx.order.documentTypeID = ctx.fields.data.documentTypeID
//                ctx.order.documentCategoryID = ctx.fields.data.documentCategoryID
//                ctx.order.numPages = ctx.fields.numPages.value.toInt()
//                ctx.order.numSources = ctx.fields.numSources.value.toInt()
//                alUAOrderRepo.save(ctx.order)
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










