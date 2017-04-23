package vgrechka.botinok

import javafx.application.Platform
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.geometry.Rectangle2D
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.*
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
import vgrechka.globalmenu.GlobalMenuItem
import java.awt.MouseInfo
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import kotlin.properties.Delegates.notNull
import javax.imageio.ImageIO
import java.awt.Toolkit.getDefaultToolkit
import java.awt.image.BufferedImage
import java.io.File
import kotlin.concurrent.thread
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible


internal class BotinokGlobalMenuConfig : GlobalMenuConfig() {
    override val initialFaceIndex = 1

    override val faces = listOf(
        makeListFace(
            keyCode = NativeKeyEvent.VC_1,
            items = listOf(
                object: GlobalMenuItem() {
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
    private val vbox = VBox(8.0)
    override val rootControl = vbox
    private val tmpImgPath = "$tmpDirPath/d2185122-750e-432d-8d88-fad71b5021b5.png".replace("\\", "/")
    private var stackPane: StackPane
    private var selectedHandles = setOf<Handle>()
    private var operationStartMouseX = 0.0
    private var operationStartMouseY = 0.0
    private var operationStartBoxParams by notNull<Box>()
    private val darkPaint = Color(0.5, 0.0, 0.0, 1.0)
    private val brightPaint = Color(1.0, 0.0, 0.0, 1.0)
//    private var gc by notNull<GraphicsContext>()
    private var image by notNull<Image>()
    private var newBoxX by notNull<Int>()
    private var newBoxY by notNull<Int>()
    private var canvasContextMenu by notNull<ContextMenu>()
    private var boxContextMenu by notNull<ContextMenu>()
    private var play by notNull<Play>()
    private var canvas by notNull<Canvas>()

    class Play {
        val arenas = FXCollections.observableArrayList<Arena>(JFXPropertyObservableExtractor())
        @Transient val editing = PlayEditing()
    }

    class PlayEditing {
        var selectedArena by JFXProperty<Arena?>(null)
//        var selectedArena: Arena? = null
    }

    class Arena {
        var title by JFXProperty("Unfuckingtitled")

        val boxes = mutableListOf<Box>()
        @Transient val editing = ArenaEditing()

        override fun toString() = title
    }

    class ArenaEditing {
        var selectedBox: Box? = null
    }

    data class Box(var x: Int = 0, var y: Int = 0, var w: Int = 0, var h: Int = 0) {
        fun isHit(testX: Double, testY: Double) =
            testX >= x && testX <= x + w - 1 && testY >= y && testY <= y + h - 1
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

    private var arenaListView: ListView<Arena>

    init {
        // clog("tmpImgPath = $tmpImgPath")
        val buttonBox = HBox(8.0)
        vbox.children += buttonBox
        buttonBox.children += Button("Fuck Around")-{o->
            o.onAction = EventHandler {
                play.arenas.first().title = "Fuck"
            }
        }
        buttonBox.children += Button("Save")
        stackPane = StackPane()
        val scrollPane = ScrollPane()
        scrollPane.content = stackPane
        val splitPane = SplitPane()

        arenaListView = ListView<Arena>()
        arenaListView.selectionModel.selectedItems.addListener(ListChangeListener {e->
            check(e.list.size == 1) {"029fd503-751a-4d02-889a-0eedbf68b468"}
            clog("Aaaaa")
        })

        splitPane.items += arenaListView
        splitPane.items += scrollPane
        splitPane.setDividerPosition(0, 0.2)
        vbox.children += splitPane
    }

    class ArenaListItem

    override fun onBeforeDeiconified() {
        val image = Robot().createScreenCapture(Rectangle(getDefaultToolkit().screenSize))
        ImageIO.write(image, "png", File(tmpImgPath))
    }

    override fun onDeiconified() {
        GlobalMenuPile.resizePrimaryStage(1000, 500)
        stackPane.children.clear()

        play = Play()
        jfxProperty(play.editing::selectedArena).addListener(ChangeListener {_, oldValue, newValue ->
            clog("selectedArena changed: $oldValue --> $newValue")
            arenaListView.selectionModel.select(newValue)
        })

        arenaListView.items = play.arenas

        run { // Test initial shit
            addNewArena()
            play.arenas.last()-{o->
                o.boxes-{a->
                    a += Box()-{o->
                        o.x = 25; o.y = 25
                        o.w = 200; o.h = 100
                    }
                    a += Box()-{o->
                        o.x = 400; o.y = 150
                        o.w = 50; o.h = 150
                    }
                }
            }

            addNewArena()
            play.arenas.last()-{o->
                o.boxes-{a->
                    a += Box()-{o->
                        o.x = 200; o.y = 100
                        o.w = 150; o.h = 250
                    }
                }
            }

//            startFuckingAroundThread()
        }

        image = Image("file:///$tmpImgPath")

        canvas = Canvas(image.width, image.height)
        stackPane.children += canvas

        canvasContextMenu = ContextMenu()
        canvasContextMenu.items += makeMenuItem {
            selectedArenaBang().boxes += Box()-{o->
                o.x = newBoxX; o.y = newBoxY
                o.w = 100; o.h = 100
            }
            drawShit()
        }

        boxContextMenu = ContextMenu()
        boxContextMenu.items += MenuItem("Delete")-{o->
            o.onAction = EventHandler {e->
                selectedArenaBang().boxes -= selectedArenaBang().editing.selectedBox!!
                drawShit()
            }
        }

        canvas.setOnContextMenuRequested {e->
            val p = canvas.screenToLocal(e.screenX, e.screenY)
            val hitBox = selectedArenaBang().boxes.find {it.isHit(p.x, p.y)}
            val menuToShow = when {
                hitBox != null -> {
                    selectedArenaBang().editing.selectedBox = hitBox
                    drawShit()
                    boxContextMenu
                }
                else -> {
                    newBoxX = Math.round(p.x).toInt()
                    newBoxY = Math.round(p.y).toInt()
                    canvasContextMenu
                }
            }

            hideContextMenus()
            menuToShow.show(canvas, e.screenX, e.screenY)
        }

        drawShit()

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED) {e->
            if (e.button == MouseButton.PRIMARY) {
                hideContextMenus()
                selectedArenaBang().editing.selectedBox = null
                o@for (box in selectedArenaBang().boxes) {
                    for (handle in Handle.values()) {
                        if (handle.rectForBox(box).contains(e.x, e.y)) {
                            selectedArenaBang().editing.selectedBox = box
                            selectedHandles = setOf(handle)
                            break@o
                        }
                    }
                    if (box.isHit(e.x, e.y)) {
                        selectedArenaBang().editing.selectedBox = box
                        selectedHandles = Handle.values().toSet()
                        break@o
                    }
                }
                selectedArenaBang().editing.selectedBox?.let {
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
            if (e.button == MouseButton.PRIMARY) {
                val selectedBox = selectedArenaBang().editing.selectedBox
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
    }

    private fun startFuckingAroundThread(): Thread {
        return thread {
            var index = 0
            while (true) {
                Thread.sleep(1000)
                Platform.runLater {
                    play.editing.selectedArena = play.arenas[index]
                    if (++index > play.arenas.lastIndex)
                        index = 0
                }
            }
        }
    }

    private fun addNewArena() {
        val arena = Arena() - {o ->
            o.title = "Unfuckingtitled arena ${play.arenas.size + 1}"
        }
        play.arenas += arena
        play.editing.selectedArena = arena
    }

    private fun hideContextMenus() {
        canvasContextMenu.hide()
        boxContextMenu.hide()
    }

    private fun makeMenuItem(onAction: () -> Unit): MenuItem {
        return MenuItem("Create Box")-{o->
            o.onAction = EventHandler {e->
                onAction()
            }
        }
    }

    private fun drawShit() {
        printState()
        val gc = canvas.graphicsContext2D
        gc.drawImage(image, 0.0, 0.0)
        selectedArenaBang().boxes.forEach {box->
//            run { // Area to be captured by the box
//                gc.fill = Color.BLUE
//                gc.fillRect(box.x.toDouble(), box.y.toDouble(), box.w.toDouble(), box.h.toDouble())
//            }

            gc.stroke = when {
                box === selectedArenaBang().editing.selectedBox -> darkPaint
                else -> brightPaint
            }
            gc.lineWidth = boxEdgeSize
            gc.strokeRect(box.x.toDouble() - boxEdgeSize / 2, box.y.toDouble() - boxEdgeSize / 2, box.w.toDouble() + boxEdgeSize, box.h.toDouble() + boxEdgeSize)

            if (box === selectedArenaBang().editing.selectedBox) {
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

    private fun selectedArenaBang() = play.editing.selectedArena!!

    fun printState() {
//        clog("selectedBox = ${if (selectedBox != null) "<something>" else "null"}"
//                 + "; selectedHandles = $selectedHandles"
//                 + "; selectionMode = $selectionMode")
    }
}











