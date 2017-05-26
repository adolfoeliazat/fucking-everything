package alraune.back

import alraune.back.AlRenderPile.t
import alraune.shared.AlDomID
import alraune.shared.AlPageID
import alraune.shared.AlPagePath
import alraune.shared.OrderFileFormPostData
import vgrechka.*

fun handleGet_orderFiles() {
    Algo1(object : Algo1Pedro {
        override fun makeSpitOrderTabPagePedro(algo1: Algo1): SpitOrderTabPagePedro {
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
                            order = algo1.order).toForm())
                        o- renderForm(dfctx = fields.dfctx,
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
    })
}


class OrderFileFields(data: OrderFileFormPostData) {
    val f = AlFields.orderFile
    val v = AlBackPile
    val dfctx = DeclareFieldContext()

    val title = declareField(dfctx, data::title, f.title.title, v::validateDocumentTitle)
    val details = declareField(dfctx, data::details, f.documentDetails.title, v::validateDocumentDetails, FieldType.TEXTAREA)
}










