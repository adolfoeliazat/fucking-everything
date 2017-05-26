package alraune.back

import alraune.back.AlRenderPile.t
import alraune.shared.AlDomID
import alraune.shared.AlPageID
import alraune.shared.AlPagePath
import vgrechka.*

fun handleGet_orderFiles() {
    Algo1(object : Algo1Pedro {
        override fun makeSpitOrderTabPagePedro(algo1: Algo1): SpitOrderTabPagePedro {
            return object : SpitOrderTabPagePedro {
                override val topRightButtonModalTitle = t("New File", "Новый файл")
                override val pageID = AlPageID.orderFiles
                override val postPath = AlPagePath.post_addOrderFile

                override fun renderModalContent(): Renderable {
                    return kdiv{o->
                        o- "fucking modal"
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


