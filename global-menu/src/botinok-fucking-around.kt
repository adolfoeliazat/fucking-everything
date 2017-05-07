package vgrechka.botinok

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import vgrechka.*
import vgrechka.db.*

object BotinokFuckingAround {
    @JvmStatic
    fun main(args: Array<String>) {
        backPlatform.springctx = AnnotationConfigApplicationContext(BotinokTestAppConfig::class.java)
        DBPile.executeBunchOfSQLStatementsAndCloseConnection(BotinokGeneratedDBPile.ddl.dropCreateAllScript)

        val f =
//            this::fuck_txSavesShitAutomatically
//            this::fuck_shitIsNotSavedAutomatically_1
//            this::fuck_shitIsNotSavedAutomatically_2
//            this::fuck_explicitSave
//            this::fuck_bug_arenaSavedTwice
            this::fuck_bug_arenaSavedTwice

        clog("====================================================")
        clog(f.name)
        clog("====================================================")
        clog()
        f()
        clog("OK")
    }

    fun fuck_txSavesShitAutomatically() {
        run {
            val play = newBotinokPlay("The Fucking Play")
            botinokPlayRepo.save(play)
        }
        run {
            backPlatform.tx {
                val play = botinokPlayRepo.findByName("The Fucking Play")!!
                val arena = newBotinokArena(name = "Arena 1", screenshot = byteArrayOf(1, 2, 3), play = play)
                play.arenas.add(arena)
            }
        }
        dumpShit()
    }

    fun fuck_shitIsNotSavedAutomatically_1() {
        run {
            val play = newBotinokPlay("The Fucking Play")
            botinokPlayRepo.save(play)
        }
        run {
            val play = botinokPlayRepo.findByName("The Fucking Play")!!
            backPlatform.tx {
                val arena = newBotinokArena(name = "Arena 1", screenshot = byteArrayOf(1, 2, 3), play = play)
                play.arenas.add(arena)
            }
        }
        dumpShit()
    }

    fun fuck_shitIsNotSavedAutomatically_2() {
        run {
            val play = newBotinokPlay("The Fucking Play")
            botinokPlayRepo.save(play)
        }
        run {
            val play = botinokPlayRepo.findByName("The Fucking Play")!!
            val arena = newBotinokArena(name = "Arena 1", screenshot = byteArrayOf(1, 2, 3), play = play)
            play.arenas.add(arena)
        }
        dumpShit()
    }

    fun fuck_explicitSave() {
        run {
            val play = newBotinokPlay("The Fucking Play")
            botinokPlayRepo.save(play)
        }
        run {
            val play = botinokPlayRepo.findByName("The Fucking Play")!!
            val arena = newBotinokArena(name = "Arena 1", screenshot = byteArrayOf(1, 2, 3), play = play)
            play.arenas.add(arena)
            botinokPlayRepo.save(play)
        }
        dumpShit()
    }

    fun fuck_bug_arenaSavedTwice() {
        run {
            val play = newBotinokPlay("The Fucking Play")
            botinokPlayRepo.save(play)
        }
        run {
            var play = botinokPlayRepo.findByName("The Fucking Play")!!
            val arena = newBotinokArena(name = "Arena 1", screenshot = byteArrayOf(1, 2, 3), play = play)
            play.arenas.add(arena)
            play = botinokPlayRepo.save(play)

            play.name = "The Fucking Play (Amended)"
            botinokPlayRepo.save(play)
        }
        dumpShit()
    }

    fun dumpShit() {
        clog("Plays")
        clog("-----")
        clog(ExecuteAndFormatResultForPrinting().sql("select * from botinok_plays").linePerRow().ignite())
        clog("Arenas")
        clog("------")
        clog(ExecuteAndFormatResultForPrinting().sql("select * from botinok_arenas").linePerRow().ignite())
    }
}



