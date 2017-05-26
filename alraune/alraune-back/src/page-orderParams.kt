package alraune.back

import alraune.back.AlRenderPile.col
import alraune.back.AlRenderPile.renderOrderTitle
import alraune.back.AlRenderPile.row
import alraune.back.AlRenderPile.t
import alraune.shared.*
import vgrechka.*

fun handleGet_orderParams() {
    val a = Algo1(object : Algo1Pedro {
        private var fields by notNullOnce<OrderParamsFields>()

        override fun jerk1(ctx: Algo1) {
            fields = OrderParamsFields(ctx.order.toForm())
        }

        override fun makeSpitOrderTabPagePedro() = object : SpitOrderTabPagePedro {
            override fun jerk1(ctx: SpitOrderTabPage) {
                val o = ctx.o
                if (ctx.canEdit) {
                    o- AlRenderPile.renderModal(ModalParams(
                        width = "80rem",
                        leftMarginColor = Color.BLUE_GRAY_300,
                        title = t("Parameters", "Параметры"),
                        body = insideMarkers(id = AlDomID.modalContent, content = renderOrderParamsForm(fields))
                    ))
                }
            }
        }
    })
}

interface Algo1Pedro {
    fun jerk1(ctx: Algo1)
    fun makeSpitOrderTabPagePedro(): SpitOrderTabPagePedro
}

class Algo1(pedro: Algo1Pedro) {
    val orderUUID = AlRequestContext.the.getParams.orderUUID ?: bitch("0fe1dd78-8afd-4511-b743-7fc3b5ac78ce")
    val order = alUAOrderRepo.findByUuid(orderUUID) ?: bitch("bcfc6c38-585c-43f9-8984-c26d9c113e4e")

    init {
        shitToFront("954a5058-5ae6-40c7-bb45-06b0eeae8bc7") {
            it.hasErrors = false
        }
        pedro.jerk1(this)

        SpitOrderTabPage(order, pedro.makeSpitOrderTabPagePedro())
    }
}

fun handlePost_setOrderParams() {
    shitToFront("b4e2fd47-3a65-41a2-be93-959118883938") {
        it.hasErrors = true
    }
    val data = readPostData(OrderCreationFormPostData::class)
    val uuid = data.orderUUID ?: bitch("4c7f82b3-6347-4f25-8949-2f96e5af4713")
    val order = alUAOrderRepo.findByUuid(uuid) ?: bitch("0ef3a079-e1c6-41bf-bfa9-8540ae9d0082")
    val fields = OrderParamsFields(data)
    if (fields.dfctx.hasErrors) {
        shitToFront("030a3b7c-7f4d-4d69-8473-88396049630f") {
            it.replacement_id = AlDomID.modalContent
        }
        spitOrderParamsPage(order, fields)
    } else {
        validateOrderParamsFields(fields)

        order.email = fields.email.value
        order.contactName = fields.contactName.value
        order.phone = fields.phone.value
        order.documentTitle = fields.documentTitle.value
        order.documentDetails = fields.documentDetails.value
        order.documentTypeID = fields.data.documentTypeID
        order.documentCategoryID = fields.data.documentCategoryID
        order.numPages = fields.numPages.value.toInt()
        order.numSources = fields.numSources.value.toInt()
        alUAOrderRepo.save(order)

        shitToFront("9b4f1a3e-c2ca-4bfb-a567-4a612caa7fc9") {
            it.historyPushState = makeURLPart(AlPagePath.orderParams, AlGetParams(orderUUID = order.uuid))
            it.hasErrors = false
            it.replacement_id = AlDomID.replaceableContent
        }
        spitOrderParamsPage(order, fields)
    }
}

interface SpitOrderTabPagePedro {
    fun jerk1(ctx: SpitOrderTabPage)
}

class SpitOrderTabPage(order: AlUAOrder, pedro: SpitOrderTabPagePedro) {
    var o by notNullOnce<Tag>()
    var canEdit by notNullOnce<Boolean>()

    init {
        shitToFront("054bb78d-238e-4313-9b75-820c5a37097c") {
            it.pageID = AlPageID.orderParams
            it.postPath = makeURLPart(AlPagePath.post_setOrderParams)
            it.orderUUID = order.uuid
        }

        spitUsualPage(replaceableContent(kdiv{o->
            this.o = o
            canEdit = order.state == UAOrderState.CUSTOMER_DRAFT

            o- renderOrderTitle(order)
//            o- kdiv.className(AlCSS.successBanner).text(t("TOTE", "Все круто, заказ создан. Мы с тобой скоро свяжемся"))
            o- kdiv.className(AlCSS.submitForReviewBanner){o->
                o- kdiv(Style(flexGrow = "1")).text(t("TOTE", "Убедись, что все верно. Подредактируй, если нужно. Возможно, добавь файлы. А затем..."))
                o- kbutton(Attrs(id = AlDomID.submitOrderForReviewButton, className = "btn btn-primary"), t("TOTE", "Отправить на проверку"))
            }

            o- kdiv(Style(position = "relative")){o->
                o- kdiv(Attrs(className = "nav nav-tabs", style = Style(marginBottom = "0.5rem"))){o->
                    o- kli.className("active")
                        .add(ka(Attrs(href = makeURLPart(AlPagePath.orderParams, AlGetParams(orderUUID = order.uuid))))
                                 .add(t("Parameters", "Параметры")))

                    o- kli.className("")
                        .add(ka(Attrs(href = makeURLPart(AlPagePath.orderFiles, AlGetParams(orderUUID = order.uuid))))
                                 .add(t("Files", "Файлы")))
                }

                if (canEdit) {
                    o- kbutton(Attrs(id = AlDomID.editOrderParamsButton, className = "btn btn-default",
                                     style = Style(position = "absolute", right = "0", top = "0")))
                        .add(ki.className(fa.pencil))
                }
            }

            o- AlRenderPile.renderOrderParams(order)

            pedro.jerk1(this)
        }))
    }
}

fun spitOrderParamsPage(order: AlUAOrder, fields: OrderParamsFields) {
    SpitOrderTabPage(order, object : SpitOrderTabPagePedro {
        override fun jerk1(ctx: SpitOrderTabPage) {
            val o = ctx.o
            if (ctx.canEdit) {
                o- AlRenderPile.renderModal(ModalParams(
                    width = "80rem",
                    leftMarginColor = Color.BLUE_GRAY_300,
                    title = t("Parameters", "Параметры"),
                    body = insideMarkers(id = AlDomID.modalContent, content = renderOrderParamsForm(fields))
                ))
            }
        }
    })
}

fun validateOrderParamsFields(fields: OrderParamsFields) {
    AlDocumentCategories.findByIDOrBitch(fields.data.documentCategoryID)
    AlDocumentType.values().find {it.name == fields.data.documentTypeID} ?: bitch("e63b006c-3cda-4db8-b7e0-e2413e980dbc")
}

fun renderOrderParamsForm(fields: OrderParamsFields): Renderable {
    val f = AlFields.order
    AlRequestContext.the.shitPassedFromBackToFront.documentCategoryID = fields.data.documentCategoryID
    return kdiv{o->
        if (fields.dfctx.hasErrors)
            o- kdiv.className(AlCSS.errorBanner).text(t("TOTE", "Кое-что нужно исправить..."))

        o- row(marginBottom = null){o->
            o- col(4, fields.contactName.render())
            o- col(4, fields.email.render())
            o- col(4, fields.phone.render())
        }
        o- row(marginBottom = null){o->
            o- col(4, kdiv.className("form-group"){o->
                o- klabel(text = f.documentType.title)
                o- kselect(Attrs(id = AlSharedPile.fieldDOMID(name = OrderCreationFormPostData::documentTypeID.name),
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
        o- kdiv{o->
            o- kbutton(Attrs(id = AlDomID.submitButton, className = "btn btn-primary"), t("TOTE", "Продолжить"))
            o- kdiv.id(AlDomID.ticker){}
        }
    }
}







