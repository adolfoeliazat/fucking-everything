package vgrechka.globalmenu

import javafx.application.Application
import javafx.collections.FXCollections
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.input.Clipboard
import javafx.scene.input.DataFormat
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import javafx.stage.Stage
import vgrechka.*
import vgrechka.idea.hripos.*
import vgrechka.ideabackdoor.*
import kotlin.system.exitProcess

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

interface MenuItem {
    val control: Control?
    fun onEnterKey()
}

object FuckItem : MenuItem {
    override fun toString() = "Fuck"
    override val control = Label("I am the Fuck item")

    override fun onEnterKey() {
        JFXStuff.infoAlert("Fuck, fuck, fuck...")
    }
}

object ShitItem : MenuItem {
    override fun toString() = "Shit"
    override val control = null

    override fun onEnterKey() {
        JFXStuff.errorAlert("Shit? What shit?", "No shit for you today...")
    }
}

object BitchItem : MenuItem {
    override fun toString() = "Bitch"
    override val control = Label("I am the Bitch item")

    override fun onEnterKey() {
        JFXStuff.errorAlert("Fuck you. Just fuck you")
    }
}

object GlobalMenuItem_Phizdets_MakeSenseOfPHPSpew : MenuItem {
    val spew = run {
        Clipboard.getSystemClipboard().getContent(DataFormat.PLAIN_TEXT)?.toString()
            ?: "Nothing interesting in clipboard"
    }

    override fun toString() = "Phizdets: Make sense of PHP spew"

    override val control = TextArea()-{o->
        o.minHeight = 100.0
        o.text = spew
    }

    override fun onEnterKey() {
        try {
            sendCommandToIDEABackdoor(Command_PhiMakeSenseOfPHPSpew(spew))
            exitProcess(0)
        } catch(e: Exception) {
            JFXStuff.errorAlert(e)
        }
    }
}

class StartGlobalMenu : Application() {
    override fun start(primaryStage: Stage) {
        val vbox = VBox()
        val listView = ListView<MenuItem>(FXCollections.observableArrayList(
            GlobalMenuItem_Phizdets_MakeSenseOfPHPSpew,
            FuckItem,
            ShitItem,
            BitchItem))
        vbox.children += listView
        listView.selectionModel.selectedItemProperty().addListener {observable, oldValue, newValue ->
            vbox.children.remove(1, vbox.children.size)
            newValue.control?.let {vbox.children += it}

        }
        listView.selectionModel.selectFirst()
        listView.addEventHandler(KeyEvent.KEY_PRESSED) {e->
            if (e.code == KeyCode.ENTER) {
                val item = listView.selectionModel.selectedItem
                item.onEnterKey()
            }
            else if (e.code == KeyCode.ESCAPE) {
                primaryStage.close()
            }
        }

        val scene = Scene(vbox, 300.0, 250.0)

        primaryStage.modality
        primaryStage.title = "Global Menu"
        primaryStage.scene = scene
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(StartGlobalMenu::class.java, *args)
        }
    }
}


