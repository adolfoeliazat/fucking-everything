package vgrechka

import org.springframework.context.ApplicationContext
import org.springframework.transaction.TransactionStatus

interface XBackPlatform {
    var springctx: ApplicationContext
    fun tx(block: (TransactionStatus) -> Unit)
}


