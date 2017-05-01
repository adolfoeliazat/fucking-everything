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

enum class RegionHandle {
    TOP_LEFT {
        override fun rectForRegion(box: Region) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 - b.handleSize + b.boxEdgeSize, box.y.toDouble() - b.obscureConst2 - b.obscureConst1, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.TOP, DragMutator.LEFT)
    },
    TOP {
        override fun rectForRegion(box: Region) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.w / 2 - b.handleSize / 2, box.y.toDouble() - b.obscureConst2 - b.obscureConst1, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.TOP)
    },
    TOP_RIGHT {
        override fun rectForRegion(box: Region) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.w, box.y.toDouble() - b.obscureConst2 - b.obscureConst1, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.TOP, DragMutator.RIGHT)
    },
    RIGHT {
        override fun rectForRegion(box: Region) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.w, box.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.h / 2 + b.handleSize / 2, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.RIGHT)
    },
    BOTTOM_RIGHT {
        override fun rectForRegion(box: Region) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.w, box.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.h + b.handleSize, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.BOTTOM, DragMutator.RIGHT)
    },
    BOTTOM {
        override fun rectForRegion(box: Region) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.w / 2 - b.handleSize / 2, box.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.h + b.handleSize, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.BOTTOM)
    },
    BOTTOM_LEFT {
        override fun rectForRegion(box: Region) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 - b.handleSize + b.boxEdgeSize, box.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.h + b.handleSize, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.BOTTOM, DragMutator.LEFT)
    },
    LEFT {
        override fun rectForRegion(box: Region) = Rectangle2D(box.x.toDouble() - b.obscureConst2 + b.obscureConst1 - b.handleSize + b.boxEdgeSize, box.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.h / 2 + b.handleSize / 2, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.LEFT)
    };

    companion object {
        private val b = BotinokStuff
    }

    abstract fun rectForRegion(box: Region): Rectangle2D
    abstract val dragMutators: Set<DragMutator>
}

sealed class FuckingNode {
    class Root : FuckingNode()

    class Arena(val treeItem: TreeItem<FuckingNode>,
                val name: String,
                val image: Image) : FuckingNode() {

        val regions: List<Region>
            get() {
                return treeItem.children.map {it.value as Region}
            }

        override fun toString() = name
    }

}

data class Region(val treeItem: TreeItem<FuckingNode>,
             val name: String,
             val arena: Arena,
             var x: Int,
             var y: Int,
             var w: Int,
             var h: Int) : FuckingNode() {

    override fun toString() = name

    fun isHit(testX: Double, testY: Double) =
        testX >= x && testX <= x + w - 1 && testY >= y && testY <= y + h - 1
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
            simulateSomeUserActions()
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
        @FXML lateinit var someTextField: TextField
        @FXML lateinit var fuckAroundButton1: Button
        @FXML lateinit var fuckAroundButton2: Button
        @FXML lateinit var navigationTreeView: TreeView<FuckingNode>
        @FXML lateinit var splitPane: SplitPane
        @FXML lateinit var detailsPane: AnchorPane
        var stuff by notNullOnce<goBananas2>()
        var currentContextMenu: ContextMenu? = null
        var selectedRegionHandles = setOf<RegionHandle>()
        var nextInitialRegionLocationIndex = 0
        var canvasContextMenu by notNull<ContextMenu>()
        var boxContextMenu by notNull<ContextMenu>()
        var operationStartRegionParams by notNull<Region>()
        var operationStartMouseX = 0.0
        var operationStartMouseY = 0.0

        class RegionLocation(val x: Int, val y: Int, val w: Int, val h: Int)

        val initialRegionLocations = listOf(
            RegionLocation(x = 20, y = 20, w = 150, h = 50),
            RegionLocation(x = 100, y = 100, w = 50, h = 150)
        )

        val canvas by relazy {
            detailsPane.children.clear()
            val scrollPane = ScrollPane()
            AnchorPane.setTopAnchor(scrollPane, 0.0)
            AnchorPane.setRightAnchor(scrollPane, 0.0)
            AnchorPane.setBottomAnchor(scrollPane, 0.0)
            AnchorPane.setLeftAnchor(scrollPane, 0.0)
            detailsPane.children += scrollPane
            val canvas = Canvas(selectedArena().image.width, selectedArena().image.height)
            scrollPane.content = canvas

            canvas.addEventHandler(MouseEvent.MOUSE_PRESSED) {e->
                if (e.button == MouseButton.PRIMARY) {
                    // noise("MOUSE_PRESSED: e.x = ${e.x}; e.y = ${e.y}")
                    hideContextMenus()
                    val hitRegion: Region? = run {
                        o@for (region in selectedArena().regions) {
                            for (handle in RegionHandle.values()) {
                                if (handle.rectForRegion(region).contains(e.x, e.y)) {
                                    selectedRegionHandles = setOf(handle)
                                    return@run region
                                }
                            }
                            if (region.isHit(e.x, e.y)) {
                                selectedRegionHandles = RegionHandle.values().toSet()
                                return@run region
                            }
                        }
                        null
                    }
                    if (hitRegion == null) {
                        selectTreeItem(selectedArena().treeItem)
                    } else {
                        selectTreeItem(hitRegion.treeItem)
                        operationStartRegionParams = hitRegion.copy()
                        operationStartMouseX = e.x
                        operationStartMouseY = e.y
                        noise("operationStartMouseX = $operationStartMouseX; operationStartMouseY = $operationStartMouseY")
                    }
                }
                drawArena()
            }

            canvas.addEventHandler(MouseEvent.MOUSE_RELEASED) {e->
                drawArena()
            }

            canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED) {e->
                if (e.button == MouseButton.PRIMARY) {
                    // noise("MOUSE_DRAGGED: e.x = ${e.x}; e.y = ${e.y}")
                    val selectedRegion = selectedRegion()
                    if (selectedRegion != null) {
                        val dx = Math.round(e.x - operationStartMouseX).toInt()
                        val dy = Math.round(e.y - operationStartMouseY).toInt()
                        noise("dx = $dx; dy = $dy")
                        val dragMutators = selectedRegionHandles.flatMap{it.dragMutators}.toSet()
                        val points = BoxPoints(operationStartRegionParams.x, operationStartRegionParams.y, operationStartRegionParams.x + operationStartRegionParams.w - 1, operationStartRegionParams.y + operationStartRegionParams.h - 1)
                        dragMutators.forEach {it.mutate(points, dx, dy)}
                        selectedRegion.x = points.minX
                        selectedRegion.y = points.minY
                        selectedRegion.w = points.maxX - points.minX + 1
                        selectedRegion.h = points.maxY - points.minY + 1
                        drawArena()
                    }
                }
            }

            canvas
        }

        private fun hideContextMenus() {
            // TODO:vgrechka ...
//            canvasContextMenu.hide()
//            boxContextMenu.hide()
        }

        fun selectTreeItem(treeItem: TreeItem<FuckingNode>) {
            navigationTreeView.selectionModel.select(treeItem)
        }

        fun onTreeSelectionChanged(oldItem: TreeItem<FuckingNode>?, newItem: TreeItem<FuckingNode>?) {
            if (newItem == null) {
                detailsPane.children.clear()
                relazy.reset(this::canvas)
                return
            }

            if (oldItem?.region != selectedRegion()) {
                selectedRegionHandles = RegionHandle.values().toSet()
            }

            drawArena()
        }

        fun drawArena() {
            val gc = canvas.graphicsContext2D
            gc.drawImage(selectedArena().image, 0.0, 0.0)
            selectedArena().regions.forEach {region->
//                run { // Area to be captured by the region
//                    gc.fill = Color.BLUE
//                    gc.fillRect(region.x.toDouble(), region.y.toDouble(), region.w.toDouble(), region.h.toDouble())
//                }

                val isFocused = selectedRegion() === region

                val darkPaint = Color(0.5, 0.0, 0.0, 1.0)
                val brightPaint = Color(1.0, 0.0, 0.0, 1.0)
                gc.stroke = when {
                    isFocused -> darkPaint
                    else -> brightPaint
                }
                val b = BotinokStuff
                gc.lineWidth = b.boxEdgeSize
                gc.strokeRect(region.x.toDouble() - b.boxEdgeSize / 2, region.y.toDouble() - b.boxEdgeSize / 2, region.w.toDouble() + b.boxEdgeSize, region.h.toDouble() + b.boxEdgeSize)

                if (isFocused) {
                    for (handle in RegionHandle.values()) {
                        gc.fill = when {
                            handle in selectedRegionHandles -> brightPaint
                            else -> darkPaint
                        }
                        val rect = handle.rectForRegion(region)
                        gc.fillRect(rect.minX, rect.minY, rect.width, rect.height)
                    }
                }
            }
        }

        fun action_newRegion() {
            try {
                val arena = selectedArena()
                val xywh = initialRegionLocations[nextInitialRegionLocationIndex]
                if (++nextInitialRegionLocationIndex > initialRegionLocations.lastIndex)
                    nextInitialRegionLocationIndex = 0
                val regionTreeItem = TreeItem<FuckingNode>()
                val newRegion = Region(regionTreeItem, "Region ${arena.regions.size + 1}", arena,
                                       x = xywh.x, y = xywh.y,
                                       w = xywh.w, h = xywh.h)
                regionTreeItem.value = newRegion
                arena.treeItem.children += regionTreeItem
                arena.treeItem.expandedProperty().set(true)

                navigationTreeView.selectionModel.clearSelection()
                navigationTreeView.selectionModel.select(regionTreeItem)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        private fun selectedArena(): FuckingNode.Arena {
            return selectedTreeItem()!!.arena
        }

        private fun selectedRegion(): Region? {
            return selectedTreeItem()?.region
        }

        private val TreeItem<FuckingNode>.region: Region? get() {
            return value as? Region
        }

        private fun selectedTreeItem(): TreeItem<FuckingNode>? =
            navigationTreeView.selectionModel.selectedItem

        private val TreeItem<FuckingNode>.arena: FuckingNode.Arena get() {
            val value = value!!
            val arena: FuckingNode.Arena = when (value) {
                is FuckingNode.Root -> wtf("c420c2f1-7c89-45aa-89b6-0aee7faa4446")
                is FuckingNode.Arena -> value
                is Region -> value.arena
            }
            return arena
        }

        fun initController() {
            navigationTreeView.setOnMouseClicked {
                currentContextMenu?.hide()
                currentContextMenu = null
            }

            navigationTreeView.setOnContextMenuRequested {e->
                val item = navigationTreeView.selectionModel.selectedItem?.value

                val menu = ContextMenu()
                if (item is FuckingNode.Arena) {
                    addMenuItem(menu, "New Region", this::action_newRegion)
                }

                if (menu.items.isNotEmpty()) {
                    menu.show(navigationTreeView, e.screenX, e.screenY)
                    currentContextMenu = menu
                }
            }

            navigationTreeView.selectionModel.selectedItemProperty().addListener {_, oldValue, newValue ->
                onTreeSelectionChanged(oldValue, newValue)
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
            co.initController()
            primaryStage.scene = Scene(root)
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

    private fun simulateSomeUserActions() {
        handleKey2()
        thread {
            Thread.sleep(500)
            JFXStuff.later {co.action_newRegion()}
            Thread.sleep(500)
            JFXStuff.later {co.action_newRegion()}
        }
    }

    private fun handleKey2() {
        JFXStuff.later {
            val tmpImgPath = "$tmpDirPath/d2185122-750e-432d-8d88-fad71b5021b5.png".replace("\\", "/")

            run {
                val image = Robot().createScreenCapture(Rectangle(getDefaultToolkit().screenSize))
                ImageIO.write(image, "png", File(tmpImgPath))
                noise("Saved screenshot")
            }

            val treeItem = TreeItem<FuckingNode>()
            val arena = FuckingNode.Arena(treeItem, "Arena ${getArenaCount() + 1}", Image("file:///$tmpImgPath"))
            treeItem.value = arena
            bananas.rootNode.children.add(treeItem)
            co.navigationTreeView.scrollTo(bananas.rootNode.children.lastIndex)
            selectLastTreeItem()


            JFXStuff.later {
                primaryStage.isIconified = true
                JFXStuff.later {
                    primaryStage.isIconified = false
                }
            }
        }
    }

    private fun selectLastTreeItem() {
        co.navigationTreeView.selectionModel.clearSelection()
        co.navigationTreeView.selectionModel.selectLast()
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

private fun addMenuItem(menu: ContextMenu, title: String, handler: () -> Unit) {
    menu.items += MenuItem(title)-{o->
        o.onAction = EventHandler {e->
            handler()
        }
    }
}













