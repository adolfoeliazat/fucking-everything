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

class RenderingParamsPile(
    val orderParamsClazz: AlCSS.Pack? = null
)

fun renderOrderPage(commands: MutableList<AlBackToFrontCommandPile>, rpp: RenderingParamsPile = RenderingParamsPile()): Tag {
    val shit = kdiv(Attrs(domid = AlDomid.replaceableContent)){o->
        o- kdiv.className("container"){o->
            val canEdit = rctx.order.state == UAOrderState.CUSTOMER_DRAFT

            o- renderOrderTitle(rctx.order)
            o- kdiv.className(AlCSS.submitForReviewBanner){o->
                o- kdiv(Style(flexGrow = "1")).text(t("TOTE", "Убедись, что все верно. Подредактируй, если нужно. Возможно, добавь файлы. А затем..."))
                o- kbutton(Attrs(domid = AlDomid.submitOrderForReviewButton, className = "btn btn-primary"), t("TOTE", "Отправить на проверку"))
            }

            val activeTab = AlOrderTab.values().find {it.name.toLowerCase() == rctx.getParams.tab?.toLowerCase()}
                ?: AlOrderTab.PARAMS

            o- kdiv(Style(position = "relative")){o->
                o- kdiv(Attrs(className = "nav nav-tabs", style = Style(marginBottom = "0.5rem"))){o->
                    fun maybeActive(tab: AlOrderTab) = if (activeTab == tab) "active" else ""

                    o- kli.className(maybeActive(AlOrderTab.PARAMS))
                        .add(ka(Attrs(href = makeURLPart(AlPagePath.order, AlGetParams(orderUUID = rctx.order.uuid, tab = AlOrderTab.PARAMS.name.toLowerCase()))))
                                 .add(t("Parameters", "Параметры")))

                    o- kli.className(maybeActive(AlOrderTab.FILES))
                        .add(ka(Attrs(href = makeURLPart(AlPagePath.order, AlGetParams(orderUUID = rctx.order.uuid, tab = AlOrderTab.FILES.name.toLowerCase()))))
                                 .add(t("Files", "Файлы")))
                }

                if (canEdit) {
                    val topRightButtonIcon = when (activeTab) {
                        AlOrderTab.PARAMS -> fa.pencil
                        AlOrderTab.FILES -> fa.plus
                    }
                    o- kbutton(Attrs(domid = AlDomid.topRightButton, dataDebugTag = AlDebugTag.topRightButton,
                                      className = "btn btn-default",
                                      style = Style(position = "absolute", right = "0", top = "0")))
                        .add(ki.className(topRightButtonIcon))
                }
            }

            exhaustive = when (activeTab) {
                AlOrderTab.PARAMS -> {
                    o- renderOrderParams(rctx.order, rpp)
                }
                AlOrderTab.FILES -> {
                    o- "pizda"
                }
            }

            if (canEdit) {
                exhaustive = when (activeTab) {
                    AlOrderTab.PARAMS -> {
                        val initCommands = mutableListOf<AlBackToFrontCommandPile>()
                        val inputControlUUIDs = mutableListOf<String>()
                        val fields = OrderParamsFields(FieldSource.DB)

                        commands += AlBackToFrontCommandPile()-{o->
                            o.opcode = AlBackToFrontCommandOpcode.OpenModalOnElementClick
                            o.domid = AlDomid.topRightButton
                            o.html = renderModal(renderOrderParamsModalContent(initCommands, inputControlUUIDs, fields)).render()
                            o.initCommands = initCommands
                        }
                    }
                    AlOrderTab.FILES -> {
                        val initCommands = mutableListOf<AlBackToFrontCommandPile>()
                        val inputControlUUIDs = mutableListOf<String>()
                        val fields = FileFields(FieldSource.INITIAL)

                        commands += AlBackToFrontCommandPile()-{o->
                            o.opcode = AlBackToFrontCommandOpcode.OpenModalOnElementClick
                            o.domid = AlDomid.topRightButton
                            o.html = renderModal(renderFileModalContent(initCommands, inputControlUUIDs, fields, AlFrontToBackCommandOpcode.SubmitCreateOrderFileForm)).render()
                            o.initCommands = initCommands
                        }
                    }
                }
            }
        }
    }
    return shit
}


fun renderOrderParamsModalContent(initCommands: MutableList<AlBackToFrontCommandPile>, inputControlUUIDs: MutableList<String>, fields: OrderParamsFields) =
    renderBlueModalContent(title = t("TOTE", "Параметры"),
                           bodyContent = renderOrderParamsFormBody(initCommands, inputControlUUIDs, fields),
                           footerContent = renderOrderParamsFormButtons(initCommands, inputControlUUIDs, tickerFloat = "left", ftbOpcode = AlFrontToBackCommandOpcode.SubmitOrderParamsForm))

fun renderFileModalContent(initCommands: MutableList<AlBackToFrontCommandPile>, inputControlUUIDs: MutableList<String>, fields: FileFields, ftbOpcode: AlFrontToBackCommandOpcode) =
    renderBlueModalContent(title = t("TOTE", "Файл"),
                           bodyContent = renderFileFormBody(initCommands, inputControlUUIDs, fields),
                           footerContent = renderFileFormButtons(initCommands, inputControlUUIDs, tickerFloat = "left", ftbOpcode = ftbOpcode))

private fun renderBlueModalContent(title: String, bodyContent: Tag, footerContent: Tag): Tag {
    return renderModalContent(ModalParams(
        domid = AlDomid.modalContent.name,
        width = "80rem",
        leftMarginColor = Color.BLUE_GRAY_300,
        title = title,
        body = kdiv {o ->
            o - bodyContent
        },
        footer = kdiv {o ->
            o - footerContent
        }
    ))
}






