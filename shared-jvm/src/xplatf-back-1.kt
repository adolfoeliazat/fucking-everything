package vgrechka

import org.springframework.context.ApplicationContext

interface XBackPlatform {
    var springctx: ApplicationContext
    fun tx(block: () -> Unit)
}


