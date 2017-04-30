package vgrechka.botinok

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import vgrechka.*
import vgrechka.db.*

@RunWith(Suite::class) @Suite.SuiteClasses(
    BotinokTest1::class
)
class BotinokTests

class BotinokTest1 {

    @Test fun test1() {
        backPlatform.springctx = AnnotationConfigApplicationContext(BotinokAppConfig::class.java)

        DBStuff.executeBunchOfSQLStatementsAndCloseConnection(BotinokGeneratedDBStuff.ddl.dropCreateAllScript)

        backPlatform.tx {
            val arena = botinokArenaRepo.save(newBotinokArena("Pizdarena"))
            botinokBoxRepo.save(newBotinokBox(x = 10, y = 20, w = 100, h = 200, arena = arena))
            botinokBoxRepo.save(newBotinokBox(x = 15, y = 23, w = 350, h = 60, arena = arena))
        }
        backPlatform.tx {
            botinokBoxRepo.findAll().forEach {
                clog(it)
            }
        }
        assertThrownExceptionOrOneOfItsCausesMessageContains("SQLITE_CONSTRAINT_FOREIGNKEY") {
            backPlatform.tx {
                val arena = botinokArenaRepo.findAll().first()
                botinokArenaRepo.delete(arena)
            }
        }
    }
}








