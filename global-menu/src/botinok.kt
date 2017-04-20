package vgrechka.botinok

import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import org.jnativehook.keyboard.NativeKeyEvent
import vgrechka.*
import vgrechka.globalmenu.*
import java.awt.MouseInfo
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import kotlin.properties.Delegates.notNull
import javax.imageio.ImageIO
import java.awt.Toolkit.getDefaultToolkit
import java.awt.image.BufferedImage
import java.io.File


internal class BotinokGlobalMenuConfig : GlobalMenuConfig() {
    override val initialFaceIndex = 1

    override val faces = listOf(
        makeListFace(
            keyCode = NativeKeyEvent.VC_1,
            items = listOf(
                object:MenuItem() {
                    private var textArea by notNull<TextArea>()

                    override fun toString() = "Get mouse location"

                    override fun run() {
                        showDamnLocation()
                    }

                    override fun makeDetailsControl(): TextArea {
                        textArea = TextArea()
                        textArea.minHeight = 100.0
                        showDamnLocation()
                        return textArea
                    }

                    private fun showDamnLocation() {
                        val location = MouseInfo.getPointerInfo().location
                        textArea.text = "${location.x}, ${location.y}"
                    }
                }
            )
        ),

        BotinokScreenshotFace(NativeKeyEvent.VC_2)

//        makeFuckingFace(
//            keyCode = NativeKeyEvent.VC_3,
//            items = listOf(
//                fuckingSimpleMenuItem(this::playScenario1)
//            )
//        )
    )

    fun playScenario1() {
        JFXStuff.errorAlert("Not now, hoser")
    }

}

internal class BotinokScreenshotFace(override val keyCode: Int) : GlobalMenuFace() {
    val vbox = VBox(8.0)
    override val rootControl = vbox
    private val tmpImgPath = "$tmpDirPath/d2185122-750e-432d-8d88-fad71b5021b5.png".replace("\\", "/")
    private var stackPane: StackPane

    init {
        // clog("tmpImgPath = $tmpImgPath")
        val buttonBox = HBox(8.0)
        vbox.children += buttonBox
        buttonBox.children += Button("Save")
        stackPane = StackPane()
        val scrollPane = ScrollPane()
        scrollPane.content = stackPane
        vbox.children += scrollPane
    }

    override fun onBeforeDeiconified() {
        val image = Robot().createScreenCapture(Rectangle(getDefaultToolkit().screenSize))
        ImageIO.write(image, "png", File(tmpImgPath))
    }

    override fun onDeiconified() {
        GlobalMenuPile.resizePrimaryStage(1000, 500)
        stackPane.children.clear()

        val image = Image("file:///$tmpImgPath")

        val canvas = Canvas(image.width, image.height)
        stackPane.children += canvas
        val gc = canvas.graphicsContext2D
        gc.drawImage(image, 0.0, 0.0)
        gc.stroke = Color(1.0, 0.0, 0.0, 0.5)
        gc.lineWidth = 5.0
        gc.strokeRect(10.0, 10.0, 50.0, 30.0)
    }
}











