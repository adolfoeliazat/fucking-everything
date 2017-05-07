package vgrechka.botinok

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import vgrechka.*
import vgrechka.db.*
import vgrechka.db.ExecuteAndFormatResultForPrinting.Title.*

object DumpBotinokTables {
    @JvmStatic
    fun main(args: Array<String>) {
        backPlatform.springctx = AnnotationConfigApplicationContext(BotinokProdAppConfig::class.java)
        clog(ExecuteAndFormatResultForPrinting()
                 .sql("select * from botinok_arenas")
                 .title(SQL())
                 .skipColumn {col->
                     listOf("createdAt", "updatedAt", "deleted", "screenshot")
                         .any {col.name.contains(it)}}
                 .linePerColumn()
                 .ignite())
    }
}

