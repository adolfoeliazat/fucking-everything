package vgrechka.botinok

import de.jensd.fx.glyphs.emojione.EmojiOne
import de.jensd.fx.glyphs.emojione.EmojiOneView
import javafx.application.Application
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Rectangle2D
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.WindowEvent
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyAdapter
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.mouse.NativeMouseEvent
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import vgrechka.*
import vgrechka.db.*
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit.getDefaultToolkit
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import kotlin.concurrent.thread
import kotlin.properties.Delegates.notNull
import kotlin.system.exitProcess

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
            this::fuck_bug_arenaSavedTwice_2

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
            val play = botinokPlayRepo.findByName("The Fucking Play")!!
            val arena = newBotinokArena(name = "Arena 1", screenshot = byteArrayOf(1, 2, 3), play = play)
            play.arenas.add(arena)
            botinokPlayRepo.save(play)

            play.name = "The Fucking Play (Amended)"
            botinokPlayRepo.save(play)
        }
        dumpShit()
    }

    fun fuck_bug_arenaSavedTwice_2() {
        run {
            val play = newBotinokPlay("The Fucking Play")
            botinokPlayRepo.save(play)
        }
        run {
            var play = botinokPlayRepo.findByName("The Fucking Play")!!
            val arena = newBotinokArena(name = "Arena 1", screenshot = byteArrayOf(1, 2, 3), play = play)
            play.arenas.add(arena)
            clog("aaaaaa " + play.arenas.first()._backing.botinokArena.regions)

            play = botinokPlayRepo.save(play)
            clog("ID assigned to arena: " + play.arenas.first().id)
            clog("rrrrrr " + play.arenas.first()._backing.botinokArena.regions)
//            play.arenas.first().regions.size

//            play.name = "The Fucking Play (Amended)"
//            botinokPlayRepo.save(play)
        }
        dumpShit()
    }

    fun dumpShit() {
        clog("Plays")
        clog("-----")
        clog(DBPile.executeAndFormatResultForPrinting("select * from botinok_plays"))
        clog("Arenas")
        clog("------")
        clog(DBPile.executeAndFormatResultForPrinting("select * from botinok_arenas"))
    }
}



