package vgrechka.botinok

import de.jensd.fx.glyphs.emojione.EmojiOne
import de.jensd.fx.glyphs.emojione.EmojiOneView
import javafx.application.Application
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Rectangle2D
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyAdapter
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.mouse.NativeMouseEvent
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import vgrechka.*
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

object BotinokStuff {
    val boxEdgeSize = 5.0
    val handleSize = boxEdgeSize * 2
    val obscureConst1 = (handleSize - boxEdgeSize) / 2
    val obscureConst2 = boxEdgeSize + obscureConst1
}

private class BoxPoints(var minX: Int, var minY: Int, var maxX: Int, var maxY: Int)

private enum class DragMutator {
    TOP {override fun mutate(points: BoxPoints, dx: Int, dy: Int) {points.minY += dy}},
    RIGHT {override fun mutate(points: BoxPoints, dx: Int, dy: Int) {points.maxX += dx}},
    BOTTOM {override fun mutate(points: BoxPoints, dx: Int, dy: Int) {points.maxY += dy}},
    LEFT {override fun mutate(points: BoxPoints, dx: Int, dy: Int) {points.minX += dx}};

    abstract fun mutate(points: BoxPoints, dx: Int, dy: Int)
}

private enum class Handle {
    TOP_LEFT {
        override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 - b.handleSize + b.boxEdgeSize, box.y.toDouble() - b.obscureConst2 - b.obscureConst1, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.TOP, DragMutator.LEFT)
    },
    TOP {
        override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.w / 2 - b.handleSize / 2, box.y.toDouble() - b.obscureConst2 - b.obscureConst1, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.TOP)
    },
    TOP_RIGHT {
        override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.w, box.y.toDouble() - b.obscureConst2 - b.obscureConst1, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.TOP, DragMutator.RIGHT)
    },
    RIGHT {
        override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.w, box.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.h / 2 + b.handleSize / 2, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.RIGHT)
    },
    BOTTOM_RIGHT {
        override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.w, box.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.h + b.handleSize, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.BOTTOM, DragMutator.RIGHT)
    },
    BOTTOM {
        override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.w / 2 - b.handleSize / 2, box.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.h + b.handleSize, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.BOTTOM)
    },
    BOTTOM_LEFT {
        override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 - b.handleSize + b.boxEdgeSize, box.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.h + b.handleSize, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.BOTTOM, DragMutator.LEFT)
    },
    LEFT {
        override fun rectForBox(box: Box) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 - b.handleSize + b.boxEdgeSize, box.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.h / 2 + b.handleSize / 2, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.LEFT)
    };

    companion object {
        private val b = BotinokStuff
    }

    abstract fun rectForBox(box: Box): Rectangle2D
    abstract val dragMutators: Set<DragMutator>
}

sealed class FuckingNode {
    class Root : FuckingNode()

    class Arena(val name: String, val image: Image) : FuckingNode() {
        override fun toString() = name
    }
}

class StartBotinok : Application() {
    private var primaryStage by notNullOnce<Stage>()
    private var bananas by notNullOnce<goBananas2>()
    private var co by notNullOnce<BotinokBrowserSceneController>()

    override fun start(primaryStage: Stage) {
        run {
            backPlatform.springctx = AnnotationConfigApplicationContext(BotinokAppConfig::class.java)
            seed()
        }

        Thread.setDefaultUncaughtExceptionHandler {thread, exception ->
            try {
                Platform.runLater {
                    JFXStuff.errorAlert(exception)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        installKeyboardHook()

        primaryStage.addEventHandler(WindowEvent.WINDOW_HIDDEN) {e->
            exitProcess(0)
        }

//        scene = Scene(Label(""))

        primaryStage.width = 1000.0
        primaryStage.height = 500.0
        primaryStage.title = "Botinok"
//        primaryStage.scene = scene
        primaryStage.initStyle(StageStyle.DECORATED)

        this.primaryStage = primaryStage
        bananas = goBananas2()

        primaryStage.setOnShown {
            handleKey2()
        }
        primaryStage.show()
    }

    private fun seed() {
        backPlatform.tx {
            run {
                val playName = "Fucking Play"
                if (botinokPlayRepo.findByName(playName) == null) {
                    botinokPlayRepo.save(newBotinokPlay(name = playName))
                }
            }
            run {
                val playName = "Shitty Play"
                if (botinokPlayRepo.findByName(playName) == null) {
                    botinokPlayRepo.save(newBotinokPlay(name = playName))
                }
            }
        }
    }

    class NavigationTreeNode(val title: String) {
        override fun toString(): String {
            return title
        }
    }

    class BotinokBrowserSceneController {
        var stuff by notNullOnce<goBananas2>()
        @FXML lateinit var someTextField: TextField
        @FXML lateinit var fuckAroundButton1: Button
        @FXML lateinit var fuckAroundButton2: Button
        @FXML lateinit var navigationTreeView: TreeView<FuckingNode>
        @FXML lateinit var splitPane: SplitPane
        @FXML lateinit var detailsPane: AnchorPane

        fun onTreeSelectionChanged(item: TreeItem<FuckingNode>?) {
            detailsPane.children.clear()
            if (item == null) return

            val value = item.value
            exhaustive=when (value) {
                is FuckingNode.Root -> {}

                is FuckingNode.Arena -> {
                    val image = value.image
                    val scrollPane = ScrollPane()
                    AnchorPane.setTopAnchor(scrollPane, 0.0)
                    AnchorPane.setRightAnchor(scrollPane, 0.0)
                    AnchorPane.setBottomAnchor(scrollPane, 0.0)
                    AnchorPane.setLeftAnchor(scrollPane, 0.0)
                    detailsPane.children += scrollPane

                    val canvas = Canvas(image.width, image.height)
                    scrollPane.content = canvas
                    val gc = canvas.graphicsContext2D
                    gc.drawImage(image, 0.0, 0.0)
                }
            }
        }

        fun init() {
            navigationTreeView.selectionModel.selectedItemProperty().addListener {_, _, newValue ->
                onTreeSelectionChanged(newValue)
            }

            navigationTreeView.isShowRoot = false
            navigationTreeView.setCellFactory {
                object : TreeCell<FuckingNode>() {
                    override fun updateItem(item: FuckingNode?, empty: Boolean) {
                        super.updateItem(item, empty)
                        if (item == null) {
                            text = ""
                            graphic = null
                            return
                        }

                        // this.style = "-fx-text-fill: red; -fx-font-weight: bold;"
                        text = item.toString()
                        graphic = EmojiOneView(EmojiOne.AIRPLANE)
                    }
                }
            }

            navigationTreeView.root = stuff.rootNode

            navigationTreeView.root.isExpanded = true

            fuckAroundButton1.onAction = EventHandler {
                JFXStuff.infoAlert("Fuck you, ${someTextField.text}")
            }
        }

        @Suppress("unused")
        fun initialize() {

        }
    }

    inner class goBananas2 {
        val rootNode: TreeItem<FuckingNode> = TreeItem(FuckingNode.Root())

        init {
            val loader = FXMLLoader(this::class.java.getResource("BotinokBrowserScene.fxml"))
            val root = loader.load<Parent>()
            co = loader.getController<BotinokBrowserSceneController>()
            co.stuff = this
            co.init()
            primaryStage.scene = Scene(root)
        }
    }

    inner class goBananas {
        private val vbox: VBox
        private val tmpImgPath = "$tmpDirPath/d2185122-750e-432d-8d88-fad71b5021b5.png".replace("\\", "/")
        private var stackPane: StackPane
        private var selectedHandles = setOf<Handle>()
        private var operationStartMouseX = 0.0
        private var operationStartMouseY = 0.0
        private var operationStartBoxParams by notNull<Box>()
        private val darkPaint = Color(0.5, 0.0, 0.0, 1.0)
        private val brightPaint = Color(1.0, 0.0, 0.0, 1.0)
        private var image by notNull<Image>()
        private var newBoxX by notNull<Int>()
        private var newBoxY by notNull<Int>()
        private var canvasContextMenu by notNull<ContextMenu>()
        private var boxContextMenu by notNull<ContextMenu>()
        private var play by notNull<Play>()
        private var canvas by notNull<Canvas>()
        private var arenaListView by notNull<ListView<Arena>>()

        init {
            vbox = VBox(8.0)
            primaryStage.scene = Scene(vbox)

            // noise("tmpImgPath = $tmpImgPath")
            val buttonBox = HBox(8.0)
            vbox.children += buttonBox

            fun addButton(title: String, handler: () -> Unit) {
                buttonBox.children += Button(title)-{o->
                    o.onAction = EventHandler {
                        handler()
                    }
                }
            }

            addButton("Save", this::action_save)
            addButton("Fuck Around 1", this::action_fuckAround1)
            addButton("Fuck Around 2", this::action_fuckAround2)

            val splitPane = SplitPane()
            vbox.children += splitPane

            val navigationTreeView = TreeView<NavigationTreeNode>()
            splitPane.items += navigationTreeView
            navigationTreeView.prefWidth = 50.0
            splitPane.setDividerPosition(0, 0.1)

            splitPane.items += ScrollPane()

            stackPane = StackPane()
        }

        fun initArenaListView() {
            arenaListView = ListView<Arena>()
            arenaListView.selectionModel.selectedItems.addListener(ListChangeListener {e->
                if (e.list.size == 0) {
                    play.editing.selectedArena = null
                }
                else if (e.list.size == 1) {
                    val item = e.list.first()
                    noise("arenaListView selection changed: $item")
                    play.editing.selectedArena = item
                }
                else {
                    wtf("19a359d5-77c2-4f2e-bb8f-2d36cc89d605")
                }
            })


            fun addItem(menu: ContextMenu, title: String, handler: () -> Unit) {
                menu.items += MenuItem(title)-{o->
                    o.onAction = EventHandler {e->
                        handler()
                    }
                }
            }

            arenaListView.setOnContextMenuRequested {e->
                val menu = ContextMenu()
                addItem(menu, "New", this::action_newArena)

                if (arenaListView.selectionModel.selectedItems.isNotEmpty()) {
                    addItem(menu, "Rename", this::action_renameArena)
                    addItem(menu, "Delete", this::action_deleteArena)
                }

                menu.show(canvas, e.screenX, e.screenY)
            }
        }

        private fun addNewArena() {
            val arena = Arena()-{o->
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
            val b = BotinokStuff

            printState()
            val gc = canvas.graphicsContext2D

            val arena = play.editing.selectedArena
            if (arena == null) {
                gc.fill = Color.WHITE
                gc.fillRect(0.0, 0.0, canvas.width, canvas.height)
                return
            }

            gc.drawImage(image, 0.0, 0.0)
            arena.boxes.forEach {box->
//            run { // Area to be captured by the box
//                gc.fill = Color.BLUE
//                gc.fillRect(box.x.toDouble(), box.y.toDouble(), box.w.toDouble(), box.h.toDouble())
//            }

                gc.stroke = when {
                    box === arena.editing.selectedBox -> darkPaint
                    else -> brightPaint
                }
                gc.lineWidth = b.boxEdgeSize
                gc.strokeRect(box.x.toDouble() - b.boxEdgeSize / 2, box.y.toDouble() - b.boxEdgeSize / 2, box.w.toDouble() + b.boxEdgeSize, box.h.toDouble() + b.boxEdgeSize)

                if (box === arena.editing.selectedBox) {
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

        private fun action_newArena() {
            addNewArena()
        }

        private fun action_renameArena() {
            val arena = selectedArenaBang()
            JFXStuff.inputBox("So, how it should be named?", arena.title)?.let {
                arena.title = it
            }
        }

        private fun action_deleteArena() {
            val arena = selectedArenaBang()
            if (JFXStuff.confirm("Arena will be deleted: ${arena.title}")) {
                play.arenas -= arena
            }
        }

        private fun action_save() {
            JFXStuff.infoAlert("Fuck you")
        }

        private fun action_fuckAround1() {
            action_deleteArena()
        }

        private fun action_fuckAround2() {
            thread {
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

        fun takeScreenshot() {
            val image = Robot().createScreenCapture(Rectangle(getDefaultToolkit().screenSize))
            ImageIO.write(image, "png", File(tmpImgPath))
        }

        fun fuckingShit1() {
            stackPane.children.clear()

            initCanvas()

            play = Play()
            jfxProperty(play.editing::selectedArena).addListener {_, oldValue, newValue ->
                // noise("selectedArena changed: $oldValue --> $newValue")
                noise("selectedArena changed: $newValue")
                arenaListView.selectionModel.select(newValue)
                drawShit()
            }

            arenaListView.items = play.arenas

            run { // Test initial shit
                addNewArena()
                play.arenas.last()-{o->
                    o.title = "Fucking arena"
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
                    o.title = "Shitty arena"
                    o.boxes-{a->
                        a += Box()-{o->
                            o.x = 200; o.y = 100
                            o.w = 150; o.h = 250
                        }
                    }
                }

                drawShit()
            }

        }

        fun initCanvas() {
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

        private fun selectedArenaBang() = play.editing.selectedArena!!

        fun printState() {
//        noise("selectedBox = ${if (selectedBox != null) "<something>" else "null"}"
//                 + "; selectedHandles = $selectedHandles"
//                 + "; selectionMode = $selectionMode")
        }
    }

    private fun installKeyboardHook() {
        val logger = Logger.getLogger(GlobalScreen::class.java.`package`.name)
        logger.level = Level.WARNING
        logger.useParentHandlers = false

        GlobalScreen.registerNativeHook()

        GlobalScreen.addNativeKeyListener(object : NativeKeyAdapter() {
            override fun nativeKeyPressed(e: NativeKeyEvent) {
                try {
                    if (e.modifiers.and(NativeMouseEvent.CTRL_L_MASK) == NativeMouseEvent.CTRL_L_MASK) {
                        if (e.keyCode == NativeKeyEvent.VC_2) {
                            handleKey2()
                        }
                    }
                } catch(e: Throwable) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun handleKey2() {
        JFXStuff.later {
            val tmpImgPath = "$tmpDirPath/d2185122-750e-432d-8d88-fad71b5021b5.png".replace("\\", "/")

            run {
                val image = Robot().createScreenCapture(Rectangle(getDefaultToolkit().screenSize))
                ImageIO.write(image, "png", File(tmpImgPath))
                noise("Saved screenshot")
            }

            val newTreeNode = TreeItem<FuckingNode>(FuckingNode.Arena("Arena ${getArenaCount() + 1}", Image("file:///$tmpImgPath")))
            bananas.rootNode.children.add(newTreeNode)
            co.navigationTreeView.scrollTo(bananas.rootNode.children.lastIndex)
            co.navigationTreeView.selectionModel.clearSelection()
            co.navigationTreeView.selectionModel.selectLast()


            JFXStuff.later {
                primaryStage.isIconified = true
                JFXStuff.later {
                    primaryStage.isIconified = false
                }
            }
        }
    }


    fun getArenaCount(): Int {
        return bananas.rootNode.children.size
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(StartBotinok::class.java, *args)
        }
    }
}


private fun noise(s: String) {
    if (true) {
        clog(s)
    }
}













