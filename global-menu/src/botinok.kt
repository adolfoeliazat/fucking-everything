package vgrechka.botinok

import javafx.event.EventType
import javafx.geometry.Rectangle2D
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
    val vbox = VBox(8.0)
    override val rootControl = vbox
    private val tmpImgPath = "$tmpDirPath/d2185122-750e-432d-8d88-fad71b5021b5.png".replace("\\", "/")
    private var stackPane: StackPane
    private val boxes = mutableListOf<Box>()
    private var selectedBox: Box? = null
    private var selectedHandles = setOf<Handle>()
    private var operationStartMouseX = 0.0
    private var operationStartMouseY = 0.0
    private var operationStartBoxParams by notNull<Box>()
    val darkPaint = Color(0.5, 0.0, 0.0, 1.0)
    val brightPaint = Color(1.0, 0.0, 0.0, 1.0)
    private var gc by notNull<GraphicsContext>()
    private var image by notNull<Image>()


    data class Box(var x: Int = 0, var y: Int = 0, var w: Int = 0, var h: Int = 0) {
        fun isHit(e: MouseEvent) =
            e.x >= x && e.x <= x + w - 1 && e.y >= y && e.y <= y + h - 1
    }

    class BoxPoints(var minX: Int, var minY: Int, var maxX: Int, var maxY: Int)

    enum class DragMutator {
        TOP {override fun mutate(points: BoxPoints, dx: Int, dy: Int) {points.minY += dy}},
        RIGHT {override fun mutate(points: BoxPoints, dx: Int, dy: Int) {points.maxX += dx}},
        BOTTOM {override fun mutate(points: BoxPoints, dx: Int, dy: Int) {points.maxY += dy}},
        LEFT {override fun mutate(points: BoxPoints, dx: Int, dy: Int) {points.minX += dx}};

        abstract fun mutate(points: BoxPoints, dx: Int, dy: Int)
    }

    enum class Handle {
        TOP_LEFT {
            override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - obscureConst2 + obscureConst1 - handleSize + boxEdgeSize, box.y.toDouble() - obscureConst2 - obscureConst1, handleSize, handleSize)
            override val dragMutators = setOf(DragMutator.TOP, DragMutator.LEFT)
        },
        TOP {
            override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - obscureConst2 + obscureConst1 + boxEdgeSize + box.w / 2 - handleSize / 2, box.y.toDouble() - obscureConst2 - obscureConst1, handleSize, handleSize)
            override val dragMutators = setOf(DragMutator.TOP)
        },
        TOP_RIGHT {
            override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - obscureConst2 + obscureConst1 + boxEdgeSize + box.w, box.y.toDouble() - obscureConst2 - obscureConst1, handleSize, handleSize)
            override val dragMutators = setOf(DragMutator.TOP, DragMutator.RIGHT)
        },
        RIGHT {
            override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - obscureConst2 + obscureConst1 + boxEdgeSize + box.w, box.y.toDouble() - obscureConst2 - obscureConst1 + box.h / 2 + handleSize / 2, handleSize, handleSize)
            override val dragMutators = setOf(DragMutator.RIGHT)
        },
        BOTTOM_RIGHT {
            override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - obscureConst2 + obscureConst1 + boxEdgeSize + box.w, box.y.toDouble() - obscureConst2 - obscureConst1 + box.h + handleSize, handleSize, handleSize)
            override val dragMutators = setOf(DragMutator.BOTTOM, DragMutator.RIGHT)
        },
        BOTTOM {
            override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - obscureConst2 + obscureConst1 + boxEdgeSize + box.w / 2 - handleSize / 2, box.y.toDouble() - obscureConst2 - obscureConst1 + box.h + handleSize, handleSize, handleSize)
            override val dragMutators = setOf(DragMutator.BOTTOM)
        },
        BOTTOM_LEFT {
            override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - obscureConst2 + obscureConst1 - handleSize + boxEdgeSize, box.y.toDouble() - obscureConst2 - obscureConst1 + box.h + handleSize, handleSize, handleSize)
            override val dragMutators = setOf(DragMutator.BOTTOM, DragMutator.LEFT)
        },
        LEFT {
            override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - obscureConst2 + obscureConst1 - handleSize + boxEdgeSize, box.y.toDouble() - obscureConst2 - obscureConst1 + box.h / 2 + handleSize / 2, handleSize, handleSize)
            override val dragMutators = setOf(DragMutator.LEFT)
        };

        abstract fun rectForBox(box: Box): Rectangle2D
        abstract val dragMutators: Set<DragMutator>
    }

    companion object {
        val boxEdgeSize = 5.0
        val handleSize = boxEdgeSize * 2
        val obscureConst1 = (handleSize - boxEdgeSize) / 2
        val obscureConst2 = boxEdgeSize + obscureConst1
    }

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
        boxes.clear()

        run { // Initial boxes
            boxes += Box()-{o->
                o.x = 25; o.y = 25
                o.w = 200; o.h = 100
            }
            boxes += Box()-{o->
                o.x = 400; o.y = 150
                o.w = 50; o.h = 150
            }
        }

        image = Image("file:///$tmpImgPath")

        val canvas = Canvas(image.width, image.height)
        stackPane.children += canvas
        gc = canvas.graphicsContext2D
        drawShit()

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED) {e->
            if (e.button == MouseButton.PRIMARY) {
                selectedBox = null
                o@for (box in boxes) {
                    for (handle in Handle.values()) {
                        if (handle.rectForBox(box).contains(e.x, e.y)) {
                            selectedBox = box
                            selectedHandles = setOf(handle)
                            break@o
                        }
                    }
                    if (box.isHit(e)) {
                        selectedBox = box
                        selectedHandles = Handle.values().toSet()
                        break@o
                    }
                }
                selectedBox?.let {
                    operationStartBoxParams = it.copy()
                    operationStartMouseX = e.x
                    operationStartMouseY = e.y
                }
            }
            drawShit()
        }

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED) {e->
            drawShit()
        }

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED) {e->
            val selectedBox = this.selectedBox
            if (selectedBox != null) {
                val dx = Math.round(e.x - operationStartMouseX).toInt()
                val dy = Math.round(e.y - operationStartMouseY).toInt()
                val dragMutators = selectedHandles.flatMap{it.dragMutators}.toSet()
                val points = BoxPoints(operationStartBoxParams.x, operationStartBoxParams.y, operationStartBoxParams.x + operationStartBoxParams.w - 1, operationStartBoxParams.y + operationStartBoxParams.h - 1)
                dragMutators.forEach {it.mutate(points, dx, dy)}
                selectedBox.x = points.minX
                selectedBox.y = points.minY
                selectedBox.w = points.maxX - points.minX + 1
                selectedBox.h = points.maxY - points.minY + 1
                drawShit()
            }
        }
    }

    private fun drawShit() {
        printState()
        gc.drawImage(image, 0.0, 0.0)
        boxes.forEach {box->
//            run { // Area to be captured by the box
//                gc.fill = Color.BLUE
//                gc.fillRect(box.x.toDouble(), box.y.toDouble(), box.w.toDouble(), box.h.toDouble())
//            }

            gc.stroke = when {
                box === selectedBox -> darkPaint
                else -> brightPaint
            }
            gc.lineWidth = boxEdgeSize
            gc.strokeRect(box.x.toDouble() - boxEdgeSize / 2, box.y.toDouble() - boxEdgeSize / 2, box.w.toDouble() + boxEdgeSize, box.h.toDouble() + boxEdgeSize)

            if (box === selectedBox) {
                for (handle in Handle.values()) {
                    gc.fill = when {
                        handle in selectedHandles -> brightPaint
                        else -> darkPaint
                    }
                    val rect = handle.rectForBox(box)
                    gc.fillRect(rect.minX, rect.minY, rect.width, rect.height)
                }
            }
        }
    }

    fun printState() {
//        clog("selectedBox = ${if (selectedBox != null) "<something>" else "null"}"
//                 + "; selectedHandles = $selectedHandles"
//                 + "; selectionMode = $selectionMode")
    }
}











