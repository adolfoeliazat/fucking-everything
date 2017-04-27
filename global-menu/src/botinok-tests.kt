package vgrechka.botinok

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import vgrechka.*
import vgrechka.spew.*

@RunWith(Suite::class) @Suite.SuiteClasses(
    BotinokTest1::class
)
class BotinokTests

class BotinokTest1 {
    @Test fun test1() {
        backPlatform.springctx = AnnotationConfigApplicationContext(BotinokAppConfig::class.java)
        backPlatform.tx {
            val arena = botinokArenaRepo.save(newBotinokArena("Pizdarena"))

            botinokBoxRepo.save(newBotinokBox(x = 10, y = 20, w = 100, h = 200, arena = arena))
            botinokBoxRepo.save(newBotinokBox(x = 15, y = 23, w = 350, h = 60, arena = arena))
            botinokBoxRepo.findAll().forEach {
                clog(it)
            }
        }
    }
}



