package vgrechka.spew

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import vgrechka.*
import vgrechka.botinok.*
import vgrechka.db.*
import java.io.File

object SpewSomeShit1 {
    @JvmStatic
    fun main(args: Array<String>) {
        spewForInputFiles(listOf("%FE%/spew-gen-tests/src/GeneratedEntitiesForAmazingWordsTest.kt"))
        clog("OK")
    }
}

object SpewBotinokEntitiesAndRecreateTestDatabaseSchema {
    @JvmStatic
    fun main(args: Array<String>) {
        val res = spewForInputFiles(listOf("%FE%/global-menu/src/botinok-entities.kt"))
        clog(res.ddl)
        backPlatform.springctx = AnnotationConfigApplicationContext(BotinokTestAppConfig::class.java)
        DBPile.executeBunchOfSQLStatementsAndCloseConnection(res.ddl.toString())
        clog("OK")
    }
}

object SpewBotinokEntitiesAndRecreateProdDatabaseSchema {
    @JvmStatic
    fun main(args: Array<String>) {
        val res = spewForInputFiles(listOf("%FE%/global-menu/src/botinok-entities.kt"))
        clog(res.ddl)
        val backupPath = FilePile.backUp().orBitchIfDoesntExist()
            .ignite(File(BigPile.localSQLiteShebangDBFilePath))
        clog("Backed shit up to $backupPath")
        backPlatform.springctx = AnnotationConfigApplicationContext(BotinokProdAppConfig::class.java)
        DBPile.executeBunchOfSQLStatementsAndCloseConnection(res.ddl.toString())
        clog("OK")
    }
}














