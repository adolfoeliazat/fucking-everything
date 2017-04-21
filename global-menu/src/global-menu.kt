package vgrechka.globalmenu

import com.sun.javafx.fxml.builder.JavaFXSceneBuilder
import com.sun.jna.platform.win32.User32
import javafx.application.Application
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.concurrent.Task
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.input.*
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import org.jnativehook.GlobalScreen
import org.jnativehook.mouse.NativeMouseAdapter
import org.jnativehook.mouse.NativeMouseEvent
import vgrechka.*
import vgrechka.idea.hripos.*
import vgrechka.ideabackdoor.*
import java.awt.Robot
import java.util.concurrent.ExecutorService
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.properties.Delegates.notNull
import kotlin.system.exitProcess
import java.lang.reflect.Array.setShort
import java.lang.reflect.AccessibleObject.setAccessible
import org.jnativehook.NativeInputEvent
import org.jnativehook.keyboard.NativeKeyAdapter
import org.jnativehook.keyboard.NativeKeyEvent
import vgrechka.botinok.*
import java.awt.MouseInfo
import java.util.concurrent.TimeUnit
import java.util.ArrayList
import java.util.concurrent.AbstractExecutorService
import kotlin.concurrent.thread
import kotlin.reflect.KFunction
import kotlin.reflect.KFunction0
import kotlin.reflect.jvm.isAccessible


object JFXStuff {
    fun infoAlert(headerText: String) {
        val alert = Alert(AlertType.INFORMATION)
        alert.title = "Fucking Information"
        alert.headerText = headerText
        alert.showAndWait()
    }

    fun errorAlert(e: Throwable) {
        errorAlert(e.message ?: "Obscure exception", e.stackTraceString, width = 1000)
    }

    fun errorAlert(headerText: String, contentText: String? = null, width: Int? = null) {
        val alert = Alert(AlertType.ERROR)
        alert.title = "Bloody Error"
        alert.headerText = headerText
        alert.contentText = contentText
        alert.isResizable = true
        width?.let {alert.dialogPane.prefWidth = it.toDouble()}
        alert.showAndWait()
    }
}

abstract class GlobalMenuItem {
    abstract fun run()
    open fun makeDetailsControl(): Control? = null
}

object FuckItem : GlobalMenuItem() {
    override fun toString() = "Fuck"
    override fun makeDetailsControl() = Label("I am the Fuck item")

    override fun run() {
        JFXStuff.infoAlert("Fuck, fuck, fuck...")
    }
}

object ShitItem : GlobalMenuItem() {
    override fun toString() = "Shit"
    override fun makeDetailsControl() = null

    override fun run() {
        JFXStuff.errorAlert("Shit? What shit?", "No shit for you today...")
    }
}

object BitchItem : GlobalMenuItem() {
    override fun toString() = "Bitch"
    override fun makeDetailsControl() = Label("I am the Bitch item")

    override fun run() {
        JFXStuff.errorAlert("Fuck you. Just fuck you")
    }
}

object GlobalMenuItem_Phizdets_MakeSenseOfPHPSpew : GlobalMenuItem() {
    var spew by notNull<String>()

    override fun toString() = "Phizdets: Make sense of PHP spew"

    override fun makeDetailsControl() = TextArea()-{o->
        o.minHeight = 100.0
        o.text = "Looking at clipboard..."

        thread {
            Thread.sleep(250)
            Platform.runLater {
                spew = Clipboard.getSystemClipboard().getContent(DataFormat.PLAIN_TEXT)?.toString()
                    ?: "Nothing interesting in clipboard"
                o.text = spew
            }
        }
    }

    override fun run() {
        try {
            GlobalMenuPile.primaryStage.isIconified = true
            sendCommandToIDEABackdoor(Command_PhiMakeSenseOfPHPSpew(spew))
            GlobalMenuPile.primaryStage.isIconified = true
        } catch(e: Exception) {
            JFXStuff.errorAlert(e)
        }
    }
}

internal object GlobalMenuPile {
    var primaryStage by notNull<Stage>()

    fun resizePrimaryStage(width: Int, height: Int) {
        primaryStage.width = width.toDouble()
        primaryStage.height = height.toDouble()
    }

    fun resizePrimaryStageToDefault() {
        resizePrimaryStage(300, 250)
    }

    fun switchToFace(face: GlobalMenuFace) {
        if (face.shouldCtrlCWhenInvoked) {
            val robot = Robot()
            try {
                robot.keyPress('C'.toInt())
            } finally {
                robot.keyRelease('C'.toInt())
            }
        }

        Platform.runLater {
            try {
                primaryStage.isIconified = true
                primaryStage.scene.root = face.rootControl
                face.onBeforeDeiconified()
                primaryStage.isIconified = false
                GlobalMenuPile.resizePrimaryStageToDefault()
                face.onDeiconified()
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

//internal class VoidDispatchService : AbstractExecutorService() {
//    internal var running = false
//
//    init {
//        running = true
//    }
//
//    override fun shutdown() {
//        running = false
//    }
//
//    override fun shutdownNow(): List<Runnable> {
//        running = false
//        return ArrayList(0)
//    }
//
//    override fun isShutdown(): Boolean {
//        return !running
//    }
//
//    override fun isTerminated(): Boolean {
//        return !running
//    }
//
//    @Throws(InterruptedException::class)
//    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
//        return true
//    }
//
//    override fun execute(r: Runnable) {
//        r.run()
//    }
//}

class StartGlobalMenu : Application() {
    private val config: GlobalMenuConfig = BotinokGlobalMenuConfig()
    private var vbox by notNullOnce<VBox>()
    private var listView by notNullOnce<ListView<GlobalMenuItem>>()
    private var scene by notNullOnce<Scene>()

    override fun start(primaryStage: Stage) {
        GlobalMenuPile.primaryStage = primaryStage
        val robot = Robot()

        run { // Install hook
            val logger = Logger.getLogger(GlobalScreen::class.java.`package`.name)
            logger.level = Level.WARNING
            logger.useParentHandlers = false

//            GlobalScreen.setEventDispatcher(VoidDispatchService())
            GlobalScreen.registerNativeHook()

            GlobalScreen.addNativeKeyListener(object:NativeKeyAdapter() {
                override fun nativeKeyPressed(e: NativeKeyEvent) {
                    try {
                        if (e.modifiers.and(NativeMouseEvent.CTRL_L_MASK) == NativeMouseEvent.CTRL_L_MASK) {
                            for (face in config.faces) {
                                if (e.keyCode == face.keyCode) {
                                    GlobalMenuPile.switchToFace(face)
                                }
                            }
                        }
                    } catch(e: Throwable) {
                        e.printStackTrace()
                    }
                }
            })
        }

        primaryStage.addEventHandler(WindowEvent.WINDOW_HIDDEN) {e->
            exitProcess(0)
        }

//        initFaces()
        // scene = Scene(config.faces.first().rootControl) // , 300.0, 250.0)
        scene = Scene(Label(""))

        primaryStage.title = "Global Menu"
        primaryStage.scene = scene
        primaryStage.initStyle(StageStyle.DECORATED)
        GlobalMenuPile.resizePrimaryStageToDefault()
        primaryStage.show()

        GlobalMenuPile.switchToFace(config.faces[config.initialFaceIndex])
    }


//    private fun initFaces() {
//        for (face in config.faces) {
//        }
//    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(StartGlobalMenu::class.java, *args)
        }
    }
}




internal abstract class GlobalMenuConfig {
    open val initialFaceIndex = 0
    abstract val faces: List<GlobalMenuFace>
}

internal abstract class GlobalMenuFace {
    open val shouldCtrlCWhenInvoked: Boolean = false
    abstract val keyCode: Int // NativeKeyEvent.VC_*
    abstract val rootControl: Parent
    open fun onDeiconified() {}
    open fun onBeforeDeiconified() {}
}

internal fun makeListFace(keyCode: Int,
                          items: List<GlobalMenuItem>,
                          shouldCtrlCWhenInvoked: Boolean = false): GlobalMenuFace {
    return object : GlobalMenuFace() {
        val vbox = VBox()
        val listView = ListView<GlobalMenuItem>(FXCollections.observableArrayList(items))

        init {
            vbox.children += listView

            listView.selectionModel.selectedItemProperty().addListener {observable, oldValue, newValue ->
                syncSelectedItem()
            }

            listView.selectionModel.selectFirst()
            listView.addEventHandler(KeyEvent.KEY_PRESSED) {e->
                if (e.code == KeyCode.ENTER) {
                    listView.selectionModel.selectedItem.run()
                }
                else if (e.code == KeyCode.ESCAPE) {
                    GlobalMenuPile.primaryStage.isIconified = true
                }
            }
            listView.addEventHandler(MouseEvent.MOUSE_CLICKED) {e->
                if (e.clickCount == 2) {
                    listView.selectionModel.selectedItem.run()
                }
            }
        }

        override val keyCode = keyCode
        override val shouldCtrlCWhenInvoked = shouldCtrlCWhenInvoked

        override fun onDeiconified() {
            syncSelectedItem()
        }

        override val rootControl = vbox

        fun syncSelectedItem() {
            vbox.children.remove(1, vbox.children.size)
            val item = listView.selectionModel.selectedItem
            if (item != null) {
                item.makeDetailsControl()?.let {vbox.children += it}
            }
        }
    }
}

internal class PhizdetsGlobalMenuConfig : GlobalMenuConfig() {
    override val faces = listOf(
        makeListFace(
            keyCode = NativeKeyEvent.VC_1,
            shouldCtrlCWhenInvoked = true,
            items = listOf(
                GlobalMenuItem_Phizdets_MakeSenseOfPHPSpew,
                FuckItem,
                ShitItem,
                BitchItem
            )
        )
    )
}


internal fun fuckingSimpleMenuItem(function: KFunction0<Unit>): GlobalMenuItem {
    return object: GlobalMenuItem() {
        override fun toString() = function.name

        override fun run() {
            function.isAccessible = true
            function.call()
        }
    }
}

















