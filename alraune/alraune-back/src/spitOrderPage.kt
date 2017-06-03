package alraune.back

import alraune.back.AlRenderPile.renderOrderParams
import alraune.back.AlRenderPile.renderOrderTitle
import alraune.back.AlRenderPile.t
import alraune.shared.AlGetParams
import alraune.shared.AlPagePath
import vgrechka.*

fun spitOrderPage() {
    val commands = mutableListOf<AlBackToFrontCommandPile>()
    val initialBackResponse = AlBackResponsePile()-{o->
        o.commands = commands
    }
    spitUsualPage(kdiv(Attrs(domid = AlDomid.replaceableContent))-{o->
        o- kdiv.className("container"){o->
            val orderUUID = rctx.req.getParameter("orderUUID")
            val order = alUAOrderRepo.findByUuid(orderUUID) ?: bitch("d65541de-d1cc-41b7-93b2-6f741c5a74eb")
            val canEdit = order.state == UAOrderState.CUSTOMER_DRAFT

            o- renderOrderTitle(order)
            o- kdiv.className(AlCSS.submitForReviewBanner){o->
                o- kdiv(Style(flexGrow = "1")).text(t("TOTE", "Убедись, что все верно. Подредактируй, если нужно. Возможно, добавь файлы. А затем..."))
                o- kbutton(Attrs(domid = AlDomid.submitOrderForReviewButton, className = "btn btn-primary"), t("TOTE", "Отправить на проверку"))
            }

            val activeTab = OrderTab.values().find {it.name == rctx.req.getParameter("tab")}
                ?: OrderTab.PARAMS

            o- kdiv(Style(position = "relative")){o->
                o- kdiv(Attrs(className = "nav nav-tabs", style = Style(marginBottom = "0.5rem"))){o->
                    fun maybeActive(tab: OrderTab) = if (activeTab == tab) "active" else ""

                    o- kli.className(maybeActive(OrderTab.PARAMS))
                        .add(ka(Attrs(href = makeURLPart(AlPagePath.orderParams, AlGetParams(orderUUID = order.uuid))))
                                 .add(t("Parameters", "Параметры")))

                    o- kli.className(maybeActive(OrderTab.FILES))
                        .add(ka(Attrs(href = makeURLPart(AlPagePath.orderFiles, AlGetParams(orderUUID = order.uuid))))
                                 .add(t("Files", "Файлы")))
                }

                if (canEdit) {
                    val topRightButtonIcon = when (activeTab) {
                        OrderTab.PARAMS -> fa.pencil
                        OrderTab.FILES -> fa.plus
                    }
                    o- kbutton(Attrs(domid = AlDomid.topRightButton, className = "btn btn-default",
                                     style = Style(position = "absolute", right = "0", top = "0")))
                        .add(ki.className(topRightButtonIcon))
                }
            }

            exhaustive=when (activeTab) {
                OrderTab.PARAMS -> {
                    o- renderOrderParams(order)
                }
                OrderTab.FILES -> {
                    imf("10a46306-0ce5-498d-94e1-e7c24efbb090")
                }
            }

//            if (canEdit) {
//                exhaustive=when (activeTab) {
//                    OrderTab.PARAMS -> {
//                        val initCommands = mutableListOf<AlBackToFrontCommandPile>()
//                        val inputControlUUIDs = mutableListOf<String>()
//                        val modalHtml = AlRenderPile.renderModal(ModalParams(
//                            width = "80rem",
//                            leftMarginColor = Color.BLUE_GRAY_300,
//                            title = t("TOTE", "Параметры"),
//                            body = AlRenderPile.renderOrderParamsForm(
//                                initCommands = initCommands,
//                                contactNameVirginValue = {rctx.order.contactName},
//                                emailVirginValue = {rctx.order.email},
//                                phoneVirginValue = {rctx.order.phone},
//                                documentTypeVirginValue = {rctx.order.documentType},
//                                documentCategoryVirginValue = {rctx.order.documentCategory},
//                                documentTitleVirginValue = {rctx.order.documentTitle},
//                                numPagesVirginValue = {rctx.order.numPages.toString()},
//                                numSourcesVirginValue = {rctx.order.numSources.toString()},
//                                detailsVirginValue = {rctx.order.documentDetails},
//                                inputControlUUIDs = inputControlUUIDs)
//                        )).render()
//
//
//                        shitToFront2("298dca01-eee5-49e4-8234-1002676f67ba") {
//                            it.commands += AlBackToFrontCommandPile().also {
//                                it.opcode = AlBackToFrontCommandOpcode.OpenModalOnElementClick
//                                it.domid = AlDomid.topRightButton
//                                it.html = modalHtml
//                                it.initCommands = initCommands
//                            }
//                        }
//                    }
//                    OrderTab.FILES -> {
//                        imf("f04ee25a-881d-4ca5-90e8-124cc42645ac")
//                    }
//                }
//            }
        }
    }, initialBackResponse)

}


