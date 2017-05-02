package vgrechka

import org.springframework.context.ApplicationContext
import org.springframework.transaction.TransactionStatus

interface XBackPlatform {
    var springctx: ApplicationContext
    fun <T> tx(block: (TransactionStatus) -> T): T
}


