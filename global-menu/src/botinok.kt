package vgrechka.botinok

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
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

private class FuckingRectangle {
    var x = 0
    var y = 0
    var w = 0
    var h = 0
}

internal class BotinokScreenshotFace(override val keyCode: Int) : GlobalMenuFace() {
    val vbox = VBox(8.0)
    override val rootControl = vbox
    private val tmpImgPath = "$tmpDirPath/d2185122-750e-432d-8d88-fad71b5021b5.png".replace("\\", "/")
    private var stackPane: StackPane
    private val fuckingRectangles = mutableListOf<FuckingRectangle>()
    private var activeFuckingRectangle: FuckingRectangle? = null
    private val frectLineWidth = 5.0

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

    private var gc by notNull<GraphicsContext>()

    override fun onDeiconified() {
        GlobalMenuPile.resizePrimaryStage(1000, 500)
        stackPane.children.clear()
        fuckingRectangles.clear()
        fuckingRectangles += FuckingRectangle()-{o->
            o.x = 25; o.y = 25
            o.w = 200; o.h = 100
            activeFuckingRectangle = o
        }

        val image = Image("file:///$tmpImgPath")

        val canvas = Canvas(image.width, image.height)
        stackPane.children += canvas
        gc = canvas.graphicsContext2D
        drawShit(image)
    }

    private fun drawShit(image: Image) {
        gc.drawImage(image, 0.0, 0.0)
        fuckingRectangles.forEach {r->
            // gc.fill = Color.BLUE
            // gc.fillRect(r.x.toDouble(), r.y.toDouble(), r.w.toDouble(), r.h.toDouble())

            gc.stroke = Color(1.0, 0.0, 0.0, 0.5)
            gc.lineWidth = frectLineWidth
            gc.strokeRect(r.x.toDouble() - frectLineWidth / 2, r.y.toDouble() - frectLineWidth / 2, r.w.toDouble() + frectLineWidth, r.h.toDouble() + frectLineWidth)

            if (r === activeFuckingRectangle) {
                gc.fill = Color(0.5, 0.0, 0.0, 0.8)
                val handleSize = frectLineWidth * 1.5
                val q = (handleSize - frectLineWidth) / 2
                val d = frectLineWidth + q
                // top
                gc.fillRect(r.x.toDouble() - d + q + frectLineWidth + r.w / 2 - handleSize / 2, r.y.toDouble() - d - q, handleSize, handleSize)
                // left
                gc.fillRect(r.x.toDouble() - d + q - handleSize + frectLineWidth, r.y.toDouble() - d - q + r.h / 2 + handleSize / 2, handleSize, handleSize)
                // right
                gc.fillRect(r.x.toDouble() - d + q + frectLineWidth + r.w, r.y.toDouble() - d - q + r.h / 2 + handleSize / 2, handleSize, handleSize)
                // bottom
                gc.fillRect(r.x.toDouble() - d + q + frectLineWidth + r.w / 2 - handleSize / 2, r.y.toDouble() - d - q + r.h + handleSize, handleSize, handleSize)
            }
        }
    }
}











