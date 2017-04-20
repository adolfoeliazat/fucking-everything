package vgrechka.botinok

import javafx.event.EventType
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
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
    data class Box(var x: Int = 0, var y: Int = 0, var w: Int = 0, var h: Int = 0) {
        fun isOnInterior(px: Double, py: Double): Boolean {
            return px >= x && px <= x + w - 1 && py >= y && py <= y + h - 1
        }
    }

    enum class SelectionMode(val edgePaint: Paint, val handlePaint: Paint) {
        NORMAL(edgePaint = Color(1.0, 0.0, 0.0, 0.5),
               handlePaint = Color(1.0, 0.0, 0.0, 0.5)),

        OPERATING(edgePaint = Color(0.0, 0.0, 1.0, 0.8),
                  handlePaint = Color(0.0, 0.0, 1.0, 0.8))
    }

    enum class OperatingOn {ALL, TOP, RIGHT, BOTTOM, LEFT}

    val vbox = VBox(8.0)
    override val rootControl = vbox
    private val tmpImgPath = "$tmpDirPath/d2185122-750e-432d-8d88-fad71b5021b5.png".replace("\\", "/")
    private var stackPane: StackPane
    private val boxes = mutableListOf<Box>()
    private val frectLineWidth = 5.0
    private var selectedBox: Box? = null
    private var selectionMode = SelectionMode.NORMAL
    private var operatingOn = OperatingOn.ALL
    private var operationStartMouseX = 0.0
    private var operationStartMouseY = 0.0
    private var operationStartBoxParams by notNull<Box>()

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

    private var image by notNull<Image>()

    override fun onDeiconified() {
        GlobalMenuPile.resizePrimaryStage(1000, 500)
        stackPane.children.clear()
        boxes.clear()
        boxes += Box()-{o->
            o.x = 25; o.y = 25
            o.w = 200; o.h = 100
        }

        image = Image("file:///$tmpImgPath")

        val canvas = Canvas(image.width, image.height)
        stackPane.children += canvas
        gc = canvas.graphicsContext2D
        drawShit()

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED) {e->
            if (e.button == MouseButton.PRIMARY) {
                selectedBox = null
                for (box in boxes) {
                    if (box.isOnInterior(e.x, e.y)) {
                        selectedBox = box
                        operationStartBoxParams = box.copy()
                        operationStartMouseX = e.x
                        operationStartMouseY = e.y
                        selectionMode = SelectionMode.OPERATING
                        operatingOn = OperatingOn.ALL
                        break
                    }
                }
            }
            drawShit()
        }

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED) {e->
            selectedBox = null
            selectionMode = SelectionMode.NORMAL
            drawShit()
        }

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED) {e->
            val selectedBox = this.selectedBox
            if (selectedBox != null && selectionMode == SelectionMode.OPERATING) {
                val dx = e.x - operationStartMouseX
                val dy = e.y - operationStartMouseY
                when (operatingOn) {
                    BotinokScreenshotFace.OperatingOn.ALL -> {
                        selectedBox.x = Math.round(operationStartBoxParams.x + dx).toInt()
                        selectedBox.y = Math.round(operationStartBoxParams.y + dy).toInt()
                    }
                    BotinokScreenshotFace.OperatingOn.TOP -> TODO()
                    BotinokScreenshotFace.OperatingOn.RIGHT -> TODO()
                    BotinokScreenshotFace.OperatingOn.BOTTOM -> TODO()
                    BotinokScreenshotFace.OperatingOn.LEFT -> TODO()
                }
                drawShit()
            }
        }
    }

    private fun drawShit() {
        printState()
        gc.drawImage(image, 0.0, 0.0)
        boxes.forEach {r->
            // gc.fill = Color.BLUE
            // gc.fillRect(r.x.toDouble(), r.y.toDouble(), r.w.toDouble(), r.h.toDouble())

            gc.stroke = selectionMode.edgePaint
            gc.lineWidth = frectLineWidth
            gc.strokeRect(r.x.toDouble() - frectLineWidth / 2, r.y.toDouble() - frectLineWidth / 2, r.w.toDouble() + frectLineWidth, r.h.toDouble() + frectLineWidth)

            if (r === selectedBox) {
                gc.fill = selectionMode.handlePaint
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

    fun printState() {
        clog("selectedBox = ${if (selectedBox != null) "<something>" else "null"}"
             + "; selectionMode = $selectionMode"
             + "; operatingOn = $operatingOn")
    }
}











