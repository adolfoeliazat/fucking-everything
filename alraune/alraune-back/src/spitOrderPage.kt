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
    spitUsualPage(renderOrderPage(commands), initialBackResponse)
}

fun renderOrderPage(commands: MutableList<AlBackToFrontCommandPile>): Tag {
    val shit = kdiv(Attrs(domid = AlDomid.replaceableContent)) {o ->
        o - kdiv.className("container") {o ->
            val canEdit = rctx.order.state == UAOrderState.CUSTOMER_DRAFT

            o - renderOrderTitle(rctx.order)
            o - kdiv.className(AlCSS.submitForReviewBanner) {o ->
                o - kdiv(Style(flexGrow = "1")).text(t("TOTE", "Убедись, что все верно. Подредактируй, если нужно. Возможно, добавь файлы. А затем..."))
                o - kbutton(Attrs(domid = AlDomid.submitOrderForReviewButton, className = "btn btn-primary"), t("TOTE", "Отправить на проверку"))
            }

            val activeTab = OrderTab.values().find {it.name == rctx.req.getParameter("tab")}
                ?: OrderTab.PARAMS

            o - kdiv(Style(position = "relative")) {o ->
                o - kdiv(Attrs(className = "nav nav-tabs", style = Style(marginBottom = "0.5rem"))) {o ->
                    fun maybeActive(tab: OrderTab) = if (activeTab == tab) "active" else ""

                    o - kli.className(maybeActive(OrderTab.PARAMS))
                        .add(ka(Attrs(href = makeURLPart(AlPagePath.orderParams, AlGetParams(orderUUID = rctx.order.uuid))))
                                 .add(t("Parameters", "Параметры")))

                    o - kli.className(maybeActive(OrderTab.FILES))
                        .add(ka(Attrs(href = makeURLPart(AlPagePath.orderFiles, AlGetParams(orderUUID = rctx.order.uuid))))
                                 .add(t("Files", "Файлы")))
                }

                if (canEdit) {
                    val topRightButtonIcon = when (activeTab) {
                        OrderTab.PARAMS -> fa.pencil
                        OrderTab.FILES -> fa.plus
                    }
                    o - kbutton(Attrs(domid = AlDomid.topRightButton, dataDebugTag = AlDebugTag.topRightButton,
                                      className = "btn btn-default",
                                      style = Style(position = "absolute", right = "0", top = "0")))
                        .add(ki.className(topRightButtonIcon))
                }
            }

            exhaustive = when (activeTab) {
                OrderTab.PARAMS -> {
                    o - renderOrderParams(rctx.order)
                }
                OrderTab.FILES -> {
                    imf("10a46306-0ce5-498d-94e1-e7c24efbb090")
                }
            }

            if (canEdit) {
                exhaustive = when (activeTab) {
                    OrderTab.PARAMS -> {
                        val initCommands = mutableListOf<AlBackToFrontCommandPile>()
                        val inputControlUUIDs = mutableListOf<String>()
                        val fields = OrderParamsFields(order = rctx.order)

                        commands += AlBackToFrontCommandPile() - {o ->
                            o.opcode = AlBackToFrontCommandOpcode.OpenModalOnElementClick
                            o.domid = AlDomid.topRightButton
                            o.html = renderModal(renderFuckingModalContent(initCommands, inputControlUUIDs, fields)).render()
                            o.initCommands = initCommands
                        }
                    }
                    OrderTab.FILES -> {
                        imf("f04ee25a-881d-4ca5-90e8-124cc42645ac")
                    }
                }
            }
        }
    }
    return shit
}


fun renderFuckingModalContent(initCommands: MutableList<AlBackToFrontCommandPile>, inputControlUUIDs: MutableList<String>, fields: OrderParamsFields) = renderModalContent(ModalParams(
    domid = AlDomid.modalContent.name,
    width = "80rem",
    leftMarginColor = Color.BLUE_GRAY_300,
    title = t("TOTE", "Параметры"),
    body = kdiv{o->
        o- fuck10(initCommands, inputControlUUIDs, fields)
    },
    footer = kdiv{o->
        o- fuck20(initCommands, inputControlUUIDs, tickerFloat = "left", ftbOpcode = AlFrontToBackCommandOpcode.SubmitOrderParamsForm)
    }
))






