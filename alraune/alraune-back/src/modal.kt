package alraune.back

import alraune.shared.*

fun AlRenderPile.renderModal(domid: String, p: ModalParams): Renderable {
    return kdiv(Attrs(className = "modal fade", id = domid, tabIndex = -1)){o->
        o- kdiv(Attrs(className = "modal-dialog", style = Style(width = p.width))){o->
            val borderLeft = p.leftMarginColor?.let {"0.5em solid $it"}
            o- kdiv(Attrs(className = "modal-content", style = Style(borderLeft = borderLeft))){o->
                o- kdiv(Attrs(className = "modal-header", style = Style(borderTopLeftRadius = "6px", borderTopRightRadius = "6px"))){o->
                    o- kbutton(Attrs(id = AlDomID.closeModalButton, className = "close", dataDismiss = "modal"))
                        .add(AlSharedPile.text.times)
                    o- kh4(Attrs(className = "modal-title"))
                        .add(p.title)
                }
                o- kdiv(Attrs(className = "modal-body")){o->
                    o- p.body
                    val debug_makeItTall = false
                    if (debug_makeItTall) {
                        for (i in 1..50) {
                            o- kdiv("pizda $i")
                        }
                    }
                }
                if (p.footer != null) {
                    o- kdiv(Attrs(className = "modal-footer")){o->
                        o- p.footer
                    }
                }
            }
        }
    }
}

class ModalParams(
    val leftMarginColor: Color? = null,
    val title: String,
    val body: Renderable,
    val width: String? = null,
    val footer: Renderable? = null
)











