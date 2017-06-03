package alraune.back

import alraune.back.AlRenderPile.renderOrderParams
import alraune.back.AlRenderPile.renderOrderTitle
import alraune.back.AlRenderPile.t
import alraune.shared.AlGetParams
import alraune.shared.AlPagePath
import alraune.shared.Color
import vgrechka.*

fun spitOrderPage() {
    val commands = mutableListOf<AlBackToFrontCommandPile>()
    val initialBackResponse = AlBackResponsePile()-{o->
        o.commands = commands
    }
    spitUsualPage(kdiv{o->
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
                    o- kbutton(Attrs(domid = AlDomid.topRightButton, dataDebugTag = AlDebugTag.topRightButton,
                                     className = "btn btn-default",
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

            if (canEdit) {
                exhaustive=when (activeTab) {
                    OrderTab.PARAMS -> {
                        val modalInitCommands = mutableListOf<AlBackToFrontCommandPile>()
                        emitCommandsForRenderingOrderCreationFormPage(modalInitCommands, OrderParamsFields(null))
                        val modalHtml = AlRenderPile.renderModal(ModalParams(
                            width = "80rem",
                            leftMarginColor = Color.BLUE_GRAY_300,
                            title = t("TOTE", "Параметры"),
                            body = kdiv(Attrs(domid = AlDomid.replaceableContent)){o->
                                o- "fuck you"
                            }
                        )).render()

                        commands += AlBackToFrontCommandPile()-{o->
                            o.opcode = AlBackToFrontCommandOpcode.OpenModalOnElementClick
                            o.domid = AlDomid.topRightButton
                            o.html = modalHtml
                            o.initCommands = modalInitCommands
                        }
                    }
                    OrderTab.FILES -> {
                        imf("f04ee25a-881d-4ca5-90e8-124cc42645ac")
                    }
                }
            }
        }
    }, initialBackResponse)

}


