package alraune.back

import alraune.back.AlRenderPile.col
import alraune.back.AlRenderPile.renderOrderTitle
import alraune.back.AlRenderPile.row
import alraune.back.AlRenderPile.t
import alraune.shared.*
import vgrechka.*

fun handleGet_orderParams() {
    Algo1(object : Algo1Pedro {
        override fun makeSpitOrderTabPagePedro(ctx: Algo1): SpitOrderTabPagePedro {
            val fields = OrderParamsFields(rctx.order.toForm())
            fields.fieldCtx.noValidation()
            return makePedroForParamsTag(fields)
        }
    })
}

interface Algo1Pedro {
    fun makeSpitOrderTabPagePedro(ctx: Algo1): SpitOrderTabPagePedro
}

/**
 * This is, basically, a way to call SpitOrderTabPage, bringing order from database into context beforehand.
 */
class Algo1(pedro: Algo1Pedro) {
    init {
        shitToFront("954a5058-5ae6-40c7-bb45-06b0eeae8bc7") {
            it.hasErrors = false
        }
        SpitOrderTabPage(pedro.makeSpitOrderTabPagePedro(this))
    }
}

fun handlePost_setOrderParams() {
    HandleOrderTabPagePost(
        getPostData = {rctx.postData.orderParams},
        fieldsCtor = ::OrderParamsFields,
        makePedro = {host-> object:HandleOrderTabPagePostPedro<OrderParamsFormPostData, OrderParamsFields> {
            override fun spitPage() {
                spitOrderParamsPage(host.fields)
            }

            override fun validateDataAndUpdateDB() {
                validateOrderParamsFields(host.fields)
                // TODO:vgrechka Show error to user instead of just dying

                rctx.order.let {
                    it.email = host.fields.email.value
                    it.contactName = host.fields.contactName.value
                    it.phone = host.fields.phone.value
                    it.documentTitle = host.fields.documentTitle.value
                    it.documentDetails = host.fields.documentDetails.value
                    it.documentTypeID = host.fields.data.documentTypeID
                    it.documentCategoryID = host.fields.data.documentCategoryID
                    it.numPages = host.fields.numPages.value.toInt()
                    it.numSources = host.fields.numSources.value.toInt()
                }

                if (AlDebugServerFiddling.fuckDatabaseForPost.getAndReset() == true) {
                    bitch("Our database is fucked up, man. No fucking service today. Yeah, and fuck you too...")
                } else {
                    alUAOrderRepo.save(rctx.order)
                }
            }

            override fun additionallyShitToFrontOnSuccess(shit: PieceOfShitFromBack) {
                shit.historyPushState = makeURLPart(AlPagePath.orderParams, AlGetParams(orderUUID = rctx.order.uuid))
            }
        }}
    )
}

interface HandleOrderTabPagePostPedro<PostData, Fields>
where PostData : Any, Fields : WithFieldContext
{
    fun spitPage()
    fun validateDataAndUpdateDB()
    fun additionallyShitToFrontOnSuccess(shit: PieceOfShitFromBack)
}

class HandleOrderTabPagePost<PostData, Fields>(
    getPostData: () -> PostData,
    fieldsCtor: (PostData) -> Fields,
    makePedro: (HandleOrderTabPagePost<PostData, Fields>) -> HandleOrderTabPagePostPedro<PostData, Fields>)
    where PostData : Any, Fields : WithFieldContext
{
    init {
        shitToFront("b4e2fd47-3a65-41a2-be93-959118883938") {
            it.hasErrors = true
        }
    }

    val data = getPostData()
    val fields = fieldsCtor(data)
    val pedro = makePedro(this)

    init {
        fields.fieldCtx.validate()
        if (fields.fieldCtx.hasErrors) {
            shitToFront("030a3b7c-7f4d-4d69-8473-88396049630f") {
                it.replacement_id = AlDomID.modalContent
            }
            pedro.spitPage()
        } else {
            pedro.validateDataAndUpdateDB()
            shitToFront("9b4f1a3e-c2ca-4bfb-a567-4a612caa7fc9") {
                pedro.additionallyShitToFrontOnSuccess(it)
                it.hasErrors = false
                it.replacement_id = AlDomID.replaceableContent
            }
            pedro.spitPage()
        }
    }
}

interface SpitOrderTabPagePedro {
    val activeTab: SpitOrderTabPage.Tab
    fun renderContent(): Renderable
    val topRightButtonIcon: IconClass
    fun renderTopRightModalContent(): Renderable
    val pageID: String
    val postPath: String
    val topRightButtonModalTitle: String
}

class SpitOrderTabPage(pedro: SpitOrderTabPagePedro) {
    var o by notNullOnce<Tag>()
    var canEdit by notNullOnce<Boolean>()

    enum class Tab {PARAMS, FILES}

    init {
        shitToFront("054bb78d-238e-4313-9b75-820c5a37097c") {
            it.pageID = pedro.pageID
            it.postPath = pedro.postPath
            it.orderUUID = rctx.order.uuid
        }

        spitUsualPage(replaceableContent(kdiv{o->
            this.o = o
            canEdit = rctx.order.state == UAOrderState.CUSTOMER_DRAFT

            o- renderOrderTitle(rctx.order)
//            o- kdiv.className(AlCSS.successBanner).text(t("TOTE", "Все круто, заказ создан. Мы с тобой скоро свяжемся"))
            o- kdiv.className(AlCSS.submitForReviewBanner){o->
                o- kdiv(Style(flexGrow = "1")).text(t("TOTE", "Убедись, что все верно. Подредактируй, если нужно. Возможно, добавь файлы. А затем..."))
                o- kbutton(Attrs(id = AlDomID.submitOrderForReviewButton, className = "btn btn-primary"), t("TOTE", "Отправить на проверку"))
            }

            o- kdiv(Style(position = "relative")){o->
                o- kdiv(Attrs(className = "nav nav-tabs", style = Style(marginBottom = "0.5rem"))){o->
                    fun maybeActive(tab: Tab) = if (pedro.activeTab == tab) "active" else ""

                    o- kli.className(maybeActive(Tab.PARAMS))
                        .add(ka(Attrs(href = makeURLPart(AlPagePath.orderParams, AlGetParams(orderUUID = rctx.order.uuid))))
                                 .add(t("Parameters", "Параметры")))

                    o- kli.className(maybeActive(Tab.FILES))
                        .add(ka(Attrs(href = makeURLPart(AlPagePath.orderFiles, AlGetParams(orderUUID = rctx.order.uuid))))
                                 .add(t("Files", "Файлы")))
                }

                if (canEdit) {
                    o- kbutton(Attrs(id = AlDomID.topRightButton, className = "btn btn-default",
                                     style = Style(position = "absolute", right = "0", top = "0")))
                        .add(ki.className(pedro.topRightButtonIcon))
                }
            }

            o- pedro.renderContent()

            if (canEdit) {
                o- AlRenderPile.renderModal(ModalParams(
                    width = "80rem",
                    leftMarginColor = Color.BLUE_GRAY_300,
                    title = pedro.topRightButtonModalTitle,
                    body = insideMarkers(id = AlDomID.modalContent, content = pedro.renderTopRightModalContent())
                ))
            }

        }))
    }
}

fun spitOrderParamsPage(fields: OrderParamsFields) {
    SpitOrderTabPage(makePedroForParamsTag(fields))
}

private fun makePedroForParamsTag(fields: OrderParamsFields): SpitOrderTabPagePedro {
    return object : SpitOrderTabPagePedro {
        override val topRightButtonModalTitle = t("Parameters", "Параметры")
        override val pageID = AlPageID.orderParams
        override val postPath = AlPagePath.post_setOrderParams
        override fun renderTopRightModalContent() = renderOrderParamsForm(fields)

        override val topRightButtonIcon = fa.pencil
        override val activeTab = SpitOrderTabPage.Tab.PARAMS

        override fun renderContent(): Renderable {
            return AlRenderPile.renderOrderParams(rctx.order)
        }
    }
}

fun validateOrderParamsFields(fields: OrderParamsFields) {
    AlDocumentCategories.findByIDOrBitch(fields.data.documentCategoryID)
    AlDocumentType.values().find {it.name == fields.data.documentTypeID} ?: bitch("e63b006c-3cda-4db8-b7e0-e2413e980dbc")
}

fun renderForm(dfctx: FieldContext,
               shitDataNecessaryForControlsToFront: () -> Unit,
               renderFormBody: () -> Renderable,
               submitButtonTitle: String): Renderable {
    shitDataNecessaryForControlsToFront()
    return kdiv{o->
        o- kdiv.id(AlDomID.formBannerArea) {o->
            o- kdiv(Attrs(id = AlDomID.serviceFuckedUpBanner, className = AlCSS.errorBanner.className, style = Style(display = "none")))
                .text(t("Service is temporarily fucked up, sorry...", "Сервис временно в жопе, просим прощения..."))

            if (dfctx.hasErrors)
                o- kdiv.className(AlCSS.errorBanner)
                    .text(t("TOTE", "Кое-что нужно исправить..."))
        }

        o- renderFormBody()

        o- kdiv.id(AlDomID.formFooterArea){o->
            o- kbutton(Attrs(id = AlDomID.submitButton, className = "btn btn-primary"), submitButtonTitle)
            o- kdiv.id(AlDomID.ticker){}
        }
    }
}

fun renderOrderParamsForm(fields: OrderParamsFields): Renderable {
    return renderForm(
        dfctx = fields.fieldCtx,
        shitDataNecessaryForControlsToFront = {
            shitToFront("b822b894-0b67-4821-8aa5-d49dccab6e09") {
                it.documentCategoryID = fields.data.documentCategoryID
            }
        },
        renderFormBody = {
            kdiv{o->
                val f = AlFields.order
                o- row(marginBottom = null){o->
                    o- col(4, fields.contactName.render())
                    o- col(4, fields.email.render())
                    o- col(4, fields.phone.render())
                }
                o- row(marginBottom = null){o->
                    o- col(4, kdiv.className("form-group"){o->
                        o- klabel(text = f.documentType.title)
                        o- kselect(Attrs(id = AlSharedPile.fieldDOMID(name = OrderParamsFormPostData::documentTypeID.name),
                                         className = "form-control")) {o->
                            for (value in AlDocumentType.values()) {
                                o- koption(Attrs(value = value.name,
                                                 selected = fields.data.documentTypeID == value.name),
                                           value.title)
                            }
                        }
                    })
                    o- col(8, kdiv.className("form-group"){o->
                        o- klabel(text = f.documentCategory.title)
                        o- kdiv(Attrs(id = AlDomID.documentCategoryPickerContainer))
                    })
                }
                o- fields.documentTitle.render()
                o- row(marginBottom = null){o->
                    o- col(6, fields.numPages.render())
                    o- col(6, fields.numSources.render())
                }
                o- fields.documentDetails.render()
                o- kdiv(Attrs(id = AlDomID.filePickerContainer))
            }
        },
        submitButtonTitle = t("TOTE", "Продолжить")
    )
}







