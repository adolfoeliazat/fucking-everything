package vgrechka.botinok

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import vgrechka.*
import vgrechka.db.*
import vgrechka.spew.*

@RunWith(Suite::class) @Suite.SuiteClasses(
    BotinokTest1::class
)
class BotinokTests

class BotinokTest1 {
    val log = TestLogger()

    @Test fun test1() {
        backPlatform.springctx = AnnotationConfigApplicationContext(BotinokTestAppConfig::class.java)

        DBStuff.executeBunchOfSQLStatementsAndCloseConnection(BotinokGeneratedDBStuff.ddl.dropCreateAllScript)

        backPlatform.tx {
            botinokPlayRepo.save(newBotinokPlay("Pizdaplay")).also {play->
                botinokArenaRepo.save(newBotinokArena(play = play, name = "Pizdarena")).also {arena->
                    botinokRegionRepo.save(newBotinokRegion(arena = arena, name = "Fucking region", x = 10, y = 20, w = 100, h = 200))
                    botinokRegionRepo.save(newBotinokRegion(arena = arena, name = "Shitty region", x = 15, y = 23, w = 350, h = 60))
                }
                botinokArenaRepo.save(newBotinokArena(play = play, name = "Mandarena")).also {arena->
                    botinokRegionRepo.save(newBotinokRegion(arena = arena, name = "Bitchy region", x = 43, y = 32, w = 784, h = 46))
                }
            }
            botinokPlayRepo.save(newBotinokPlay("Hamlet")).also {play->
                botinokArenaRepo.save(newBotinokArena(play = play, name = "the fucking")).also {arena->
                    botinokRegionRepo.save(newBotinokRegion(arena = arena, name = "prince", x = 453, y = 858, w = 74, h = 500))
                    botinokRegionRepo.save(newBotinokRegion(arena = arena, name = "of Denmark", x = 424, y = 483, w = 22, h = 33))
                }
            }
        }

        backPlatform.tx {
            log.forEach(botinokPlayRepo.findAll(), "5ac827cf-d160-439e-8bce-ef41e51ec69a") {play, itemLog ->
                itemLog.println(play)
                log.forEach(play.arenas, "6c363edb-8cd5-4b87-8bf0-f899024482f8") {arena, itemLog ->
                    itemLog.println("    $arena")
                    log.forEach(arena.regions, "84c184eb-91b9-41e4-8bd4-f42dfd5b268a") {region, itemLog ->
                        itemLog.println("        $region")
                    }
                }
            }
        }

        fun <T : GCommonEntityFields> checkForeignKeyPreventsDeletion(deleteFromRepo: GRepository<T>) {
            assertThrownExceptionOrOneOfItsCausesMessageContains("SQLITE_CONSTRAINT_FOREIGNKEY") {
                backPlatform.tx {
                    val shit = deleteFromRepo.findAll().first()
                    deleteFromRepo.delete(shit)
                }
            }
        }

        checkForeignKeyPreventsDeletion(deleteFromRepo = botinokPlayRepo)
        checkForeignKeyPreventsDeletion(deleteFromRepo = botinokArenaRepo)

        log.assertEquals("""
BotinokPlay(name=Pizdaplay)                                             0--5ac827cf-d160-439e-8bce-ef41e51ec69a
    BotinokArena(name=Pizdarena)                                        0-0--6c363edb-8cd5-4b87-8bf0-f899024482f8
        BotinokRegion(name=Fucking region, x=10, y=20, w=100, h=200)    0-0-0--84c184eb-91b9-41e4-8bd4-f42dfd5b268a
        BotinokRegion(name=Shitty region, x=15, y=23, w=350, h=60)      0-0-1--84c184eb-91b9-41e4-8bd4-f42dfd5b268a
    BotinokArena(name=Mandarena)                                        0-1--6c363edb-8cd5-4b87-8bf0-f899024482f8
        BotinokRegion(name=Bitchy region, x=43, y=32, w=784, h=46)      0-1-0--84c184eb-91b9-41e4-8bd4-f42dfd5b268a
BotinokPlay(name=Hamlet)                                                1--5ac827cf-d160-439e-8bce-ef41e51ec69a
    BotinokArena(name=the fucking)                                      1-0--6c363edb-8cd5-4b87-8bf0-f899024482f8
        BotinokRegion(name=prince, x=453, y=858, w=74, h=500)           1-0-0--84c184eb-91b9-41e4-8bd4-f42dfd5b268a
        BotinokRegion(name=of Denmark, x=424, y=483, w=22, h=33)        1-0-1--84c184eb-91b9-41e4-8bd4-f42dfd5b268a
        """)
    }
}








