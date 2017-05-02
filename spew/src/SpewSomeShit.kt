package vgrechka.spew

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import vgrechka.*
import vgrechka.botinok.*
import vgrechka.db.*
import javax.sql.DataSource

object SpewSomeShit1 {
    @JvmStatic
    fun main(args: Array<String>) {
        spewForInputFiles(listOf("%FE%/spew-gen-tests/src/GeneratedEntitiesForAmazingWordsTest.kt"))
        clog("OK")
    }
}

object SpewSomeShit2 {
    @JvmStatic
    fun main(args: Array<String>) {
        val res = spewForInputFiles(listOf("%FE%/global-menu/src/botinok-entities.kt"))
        clog(res.ddl)
        backPlatform.springctx = AnnotationConfigApplicationContext(BotinokAppConfig::class.java)
        DBStuff.executeBunchOfSQLStatementsAndCloseConnection(res.ddl.toString())
        clog("OK")
    }
}



