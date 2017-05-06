package vgrechka.botinok

import de.jensd.fx.glyphs.emojione.EmojiOne
import de.jensd.fx.glyphs.emojione.EmojiOneView
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.application.Application
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.geometry.Rectangle2D
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.ContextMenuEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
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

// delete from botinok_regions; delete from botinok_arenas; delete from botinok_plays;

object BotinokStuff {
    val boxEdgeSize = 5.0
    val handleSize = boxEdgeSize * 2
    val obscureConst1 = (handleSize - boxEdgeSize) / 2
    val obscureConst2 = boxEdgeSize + obscureConst1
}

data class Box(var x: Int = 0, var y: Int = 0, var w: Int = 0, var h: Int = 0)

class BoxPoints(var minX: Int, var minY: Int, var maxX: Int, var maxY: Int)

enum class DragMutator {
    TOP {override fun mutate(points: BoxPoints, dx: Int, dy: Int) {points.minY += dy}},
    RIGHT {override fun mutate(points: BoxPoints, dx: Int, dy: Int) {points.maxX += dx}},
    BOTTOM {override fun mutate(points: BoxPoints, dx: Int, dy: Int) {points.maxY += dy}},
    LEFT {override fun mutate(points: BoxPoints, dx: Int, dy: Int) {points.minX += dx}};

    abstract fun mutate(points: BoxPoints, dx: Int, dy: Int)
}

enum class RegionHandle {
    TOP_LEFT {
        override fun rectForRegion(box: RegionNode) = Rectangle2D(box.region.x.toDouble() - b.obscureConst2 + b.obscureConst1 - b.handleSize + b.boxEdgeSize, box.region.y.toDouble() - b.obscureConst2 - b.obscureConst1, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.TOP, DragMutator.LEFT)
    },
    TOP {
        override fun rectForRegion(box: RegionNode) = Rectangle2D(box.region.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.region.w / 2 - b.handleSize / 2, box.region.y.toDouble() - b.obscureConst2 - b.obscureConst1, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.TOP)
    },
    TOP_RIGHT {
        override fun rectForRegion(box: RegionNode) = Rectangle2D(box.region.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.region.w, box.region.y.toDouble() - b.obscureConst2 - b.obscureConst1, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.TOP, DragMutator.RIGHT)
    },
    RIGHT {
        override fun rectForRegion(box: RegionNode) = Rectangle2D(box.region.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.region.w, box.region.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.region.h / 2 + b.handleSize / 2, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.RIGHT)
    },
    BOTTOM_RIGHT {
        override fun rectForRegion(box: RegionNode) = Rectangle2D(box.region.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.region.w, box.region.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.region.h + b.handleSize, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.BOTTOM, DragMutator.RIGHT)
    },
    BOTTOM {
        override fun rectForRegion(box: RegionNode) = Rectangle2D(box.region.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.region.w / 2 - b.handleSize / 2, box.region.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.region.h + b.handleSize, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.BOTTOM)
    },
    BOTTOM_LEFT {
        override fun rectForRegion(box: RegionNode) = Rectangle2D(box.region.x.toDouble() - b.obscureConst2 + b.obscureConst1 - b.handleSize + b.boxEdgeSize, box.region.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.region.h + b.handleSize, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.BOTTOM, DragMutator.LEFT)
    },
    LEFT {
        override fun rectForRegion(box: RegionNode) = Rectangle2D(box.region.x.toDouble() - b.obscureConst2 + b.obscureConst1 - b.handleSize + b.boxEdgeSize, box.region.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.region.h / 2 + b.handleSize / 2, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.LEFT)
    };

    companion object {
        private val b = BotinokStuff
    }

    abstract fun rectForRegion(box: RegionNode): Rectangle2D
    abstract val dragMutators: Set<DragMutator>
}

sealed class FuckingNode {
    class Root : FuckingNode()
}

class ArenaNode(val treeItem: TreeItem<FuckingNode>,
                val arena: BotinokArena) : FuckingNode() {

    val image by lazy {Image(arena.screenshot.inputStream())}
    val regions: List<RegionNode> get() = treeItem.children.map {it.value as? RegionNode}.filterNotNull()
    val pointers: List<PointerNode> get() = treeItem.children.map {it.value as? PointerNode}.filterNotNull()

    override fun toString() = arena.name
}

data class RegionNode(val treeItem: TreeItem<FuckingNode>,
                      val region: BotinokRegion,
                      val arenaNode: ArenaNode) : FuckingNode() {

    override fun toString() = region.name

    fun isHit(testX: Double, testY: Double) =
        testX >= region.x && testX <= region.x + region.w - 1 && testY >= region.y && testY <= region.y + region.h - 1
}

data class PointerNode(val treeItem: TreeItem<FuckingNode>,
                       val pointer: BotinokPointer,
                       val arenaNode: ArenaNode) : FuckingNode() {

    override fun toString() = pointer.name

//    fun isHit(testX: Double, testY: Double) =
//        testX >= region.x && testX <= region.x + region.w - 1 && testY >= region.y && testY <= region.y + region.h - 1
}

class StartBotinok : Application() {
    private var primaryStage by notNullOnce<Stage>()
    private var bananas by notNullOnce<goBananas2>()
    private var handleEnterKeyInPlaySelector by notNull<() -> Unit>()
    private var play by notNull<BotinokPlay>()
    private var dirty = false

    override fun start(primaryStage: Stage) {
        run {
            backPlatform.springctx = AnnotationConfigApplicationContext(BotinokProdAppConfig::class.java)
            seed()
        }

        Thread.setDefaultUncaughtExceptionHandler {thread, exception ->
            try {
                Platform.runLater {
                    exception.printStackTrace()
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

        this.primaryStage = primaryStage
        primaryStage.width = 1500.0
        primaryStage.height = 750.0
        openPlaySelector()

        primaryStage.setOnShown {
            simulateSomeUserActions()
        }
        primaryStage.setOnCloseRequest {
            if (maybeAskAndSave().cancelled) {
                it.consume()
            }
        }
        primaryStage.show()
    }

    class MaybeAskAndSaveResult(val cancelled: Boolean)
    private fun maybeAskAndSave(): MaybeAskAndSaveResult {
        if (dirty) {
            exhaustive = when (JFXStuff.yesNoCancel("Should I save your shit?")) {
                JFXStuff.YesNoCancelResult.YES -> {
                    action_save()
                }
                JFXStuff.YesNoCancelResult.NO -> {
                }
                JFXStuff.YesNoCancelResult.CANCEL -> {
                    return MaybeAskAndSaveResult(cancelled = true)
                }
            }
        }
        return MaybeAskAndSaveResult(cancelled = false)
    }

    fun setDirty(b: Boolean) {
        if (dirty != b) {
            dirty = b
            updateStageTitle()
        }
    }

    fun openPlaySelector() {
        primaryStage.title = "Botinok"
        class Item(val play: BotinokPlay) {
            override fun toString() = play.name
        }
        val listView = ListView<Item>()
        listView.items = FXCollections.observableArrayList(botinokPlayRepo.findAll().map {Item(it)})
        handleEnterKeyInPlaySelector = {
            listView.selectionModel.selectedItem?.play?.let {
                action_openPlayEditor(it)
            }
        }
        listView.setOnKeyPressed {
            if (it.code == KeyCode.ENTER) {
                handleEnterKeyInPlaySelector()
            }
        }
        listView.setOnMouseClicked {
            if (it.button == MouseButton.PRIMARY && it.clickCount == 2) {
                handleEnterKeyInPlaySelector()
            }
        }
        JFXStuff.later {
            listView.selectionModel.select(0)
            listView.requestFocus()
        }
        primaryStage.scene = Scene(listView)
    }

    fun action_openPlayEditor(play: BotinokPlay) {
        this.play = play
        updateStageTitle()
        bananas = goBananas2()

        for (arena in play.arenas) {
            val arenaTreeItem = addTreeItemForArena(arena)
            for (region in arena.regions) {
                addTreeItemForRegion(region, (arenaTreeItem.value as ArenaNode))
            }
            for (pointer in arena.pointers) {
                addTreeItemForPointer(pointer, (arenaTreeItem.value as ArenaNode))
            }
        }

        val rootItems = bananas.rootNode.children
        if (rootItems.isNotEmpty()) {
            val arenaItem = rootItems.first()
            bananas.navigationTreeView.selectionModel.select(arenaItem)
            if (arenaItem.children.isNotEmpty()) {
                arenaItem.isExpanded = true
            }
        }
    }

    private fun updateStageTitle() {
        primaryStage.title = "Botinok - ${play.name}${dirty.thenElseEmpty{" *"}}"
    }

    private fun seed() {
        backPlatform.tx {
            run {
                val playName = "Hamlet"
                if (botinokPlayRepo.findByName(playName) == null) {
                    botinokPlayRepo.save(newBotinokPlay(name = playName))
                }
            }
            run {
                val playName = "Macbeth"
                if (botinokPlayRepo.findByName(playName) == null) {
                    botinokPlayRepo.save(newBotinokPlay(name = playName))
                }
            }
        }
    }

    inner class goBananas2 {
        var someTextField by notNull<TextField>()
        var navigationTreeView by notNull<TreeView<FuckingNode>>()
        var splitPane by notNull<SplitPane>()
        var detailsPane by notNull<AnchorPane>()
        var currentContextMenu: ContextMenu? = null
        var selectedRegionHandles = setOf<RegionHandle>()
        var nextInitialRegionLocationIndex = 0
        var canvasContextMenu by notNull<ContextMenu>()
        var boxContextMenu by notNull<ContextMenu>()
        var operationStartRegionParams by notNull<Box>()
        var operationStartMouseX = 0.0
        var operationStartMouseY = 0.0
        val rootNode: TreeItem<FuckingNode> = TreeItem(FuckingNode.Root())
        var drawingAreaStackPane by notNull<StackPane>()

        inner class RegionLocation(val x: Int, val y: Int, val w: Int, val h: Int)

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

            drawingAreaStackPane = StackPane()
            drawingAreaStackPane.children += canvas
            scrollPane.content = drawingAreaStackPane

            canvas.addEventHandler(MouseEvent.MOUSE_PRESSED) {e->
                if (e.button == MouseButton.PRIMARY) {
                    // noise("MOUSE_PRESSED: e.x = ${e.x}; e.y = ${e.y}")
                    hideContextMenus()
                    val hitRegion: RegionNode? = run {
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
                        operationStartRegionParams = Box(hitRegion.region.x, hitRegion.region.y, hitRegion.region.w, hitRegion.region.h)
                        operationStartMouseX = e.x
                        operationStartMouseY = e.y
                        // noise("operationStartMouseX = $operationStartMouseX; operationStartMouseY = $operationStartMouseY")
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
                        // noise("dx = $dx; dy = $dy")
                        val dragMutators = selectedRegionHandles.flatMap{it.dragMutators}.toSet()
                        val points = BoxPoints(operationStartRegionParams.x, operationStartRegionParams.y, operationStartRegionParams.x + operationStartRegionParams.w - 1, operationStartRegionParams.y + operationStartRegionParams.h - 1)
                        dragMutators.forEach {it.mutate(points, dx, dy)}
                        selectedRegion.region.x = points.minX
                        selectedRegion.region.y = points.minY
                        selectedRegion.region.w = points.maxX - points.minX + 1
                        selectedRegion.region.h = points.maxY - points.minY + 1
                        setDirty(true)
                        drawArena()
                    }
                }
            }

            canvas
        }

        init {
            val vbox = VBox()

            vbox.children += MenuBar()-{o->
                o.menus += Menu("_File")-{o->
                    o.items += MenuItem("_Save")-{o->
                        o.setOnAction {action_save()}
                    }

                    o.items += MenuItem("_Quit")-{o->
                        o.setOnAction {
                            action_quit()
                        }
                    }
                }
            }

            splitPane = SplitPane()
            vbox.children += splitPane
            VBox.setVgrow(splitPane, Priority.ALWAYS)
            splitPane.setDividerPosition(0, 0.2)

            navigationTreeView = TreeView()
            splitPane.items += navigationTreeView

            detailsPane = AnchorPane()
            splitPane.items += detailsPane

            navigationTreeView.setOnMouseClicked {
                currentContextMenu?.hide()
                currentContextMenu = null
            }

            navigationTreeView.setOnContextMenuRequested {e->
                makeTreeContextMenu(e)
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

            navigationTreeView.root = rootNode
            navigationTreeView.root.isExpanded = true

            primaryStage.scene = Scene(vbox)
        }

        private fun makeTreeContextMenu(e: ContextMenuEvent) {
            val item = navigationTreeView.selectionModel.selectedItem
            val value = item?.value

            val menu = ContextMenu()
            when (value) {
                is ArenaNode -> {
                    addMenuItem(menu, "Rename", {action_renameArena(item, value)})
                    if (item !== rootNode.children.first())
                        addMenuItem(menu, "Move Up", {moveArenaIndexDelta(item, value, -1)})
                    if (item !== rootNode.children.last())
                        addMenuItem(menu, "Move Down", {moveArenaIndexDelta(item, value, +1)})
                    addMenuItem(menu, "New Region", this::action_newRegion)
                    addMenuItem(menu, "New Pointer", this::action_newPointer)
                }
                is RegionNode -> {

                }
            }

            if (menu.items.isNotEmpty()) {
                menu.show(navigationTreeView, e.screenX, e.screenY)
                currentContextMenu = menu
            }
        }

        private fun moveArenaIndexDelta(item: TreeItem<FuckingNode>, arenaNode: ArenaNode, i: Int) {
            run {
                val arena = arenaNode.arena
                val index = play.arenas.indexOfOrNull(arena) ?: wtf("db8f6365-cf88-494f-8f6a-7a07b11c01f5")
                val tmp = play.arenas[index + i]
                play.arenas[index + i] = arena
                play.arenas[index] = tmp
            }
            run {
                val index = rootNode.children.indexOfOrNull(item) ?: wtf("5df53fb4-534a-49df-832d-ee31043c7f19")
                rootNode.children.removeAt(index)
                rootNode.children.add(index + i, item)
            }
            setDirty(true)
        }

        private fun action_renameArena(treeItem: TreeItem<FuckingNode>, arenaNode: ArenaNode) {
            val arena = arenaNode.arena
            JFXStuff.inputBox("So, name?", arena.name)?.let {
                arena.name = it
                treeItem.value = ArenaNode(treeItem, arena)
                setDirty(true)
            }
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

            val blackPaint = Color(0.0, 0.0, 0.0, 1.0)
            val darkRedPaint = Color(0.5, 0.0, 0.0, 1.0)
            val brightRedPaint = Color(1.0, 0.0, 0.0, 1.0)

            for (regionNode in selectedArena().regions) {
                val isFocused = selectedRegion() === regionNode

                gc.stroke = when {
                    isFocused -> darkRedPaint
                    else -> brightRedPaint
                }
                val b = BotinokStuff
                gc.lineWidth = b.boxEdgeSize
                gc.strokeRect(regionNode.region.x.toDouble() - b.boxEdgeSize / 2, regionNode.region.y.toDouble() - b.boxEdgeSize / 2, regionNode.region.w.toDouble() + b.boxEdgeSize, regionNode.region.h.toDouble() + b.boxEdgeSize)

                if (isFocused) {
                    for (handle in RegionHandle.values()) {
                        gc.fill = when {
                            handle in selectedRegionHandles -> brightRedPaint
                            else -> darkRedPaint
                        }
                        val rect = handle.rectForRegion(regionNode)
                        gc.fillRect(rect.minX, rect.minY, rect.width, rect.height)
                    }
                }
            }

            val stackedShit = drawingAreaStackPane.children.subList(1, drawingAreaStackPane.children.size)
            stackedShit.clear()

            for (pointerNode in selectedArena().pointers) {
                val isFocused = selectedPointer() === pointerNode

//                run { // Draw exact point
//                    val darkPaint = Color(0.5, 0.0, 0.0, 1.0)
//                    val brightPaint = Color(1.0, 0.0, 0.0, 1.0)
//                    gc.fill = when {
//                        isFocused -> darkPaint
//                        else -> brightPaint
//                    }
//                    gc.fillRect(pointerNode.pointer.x.toDouble(), pointerNode.pointer.y.toDouble(),
//                                1.0, 1.0)
//                }

                val icon = FontAwesomeIconView(FontAwesomeIcon.MOUSE_POINTER)
                val scale = 1.0
                icon.translateX = pointerNode.pointer.x.toDouble() + (icon.glyphSize.toDouble() * scale - icon.glyphSize.toDouble()) / 2
                icon.translateY = pointerNode.pointer.y.toDouble() + (icon.glyphSize.toDouble() * scale - icon.glyphSize.toDouble()) / 2
                icon.scaleX = scale
                icon.scaleY = scale
                icon.opacity = 0.5
                icon.glyphSize = 32
                icon.fill = when {
                    isFocused -> brightRedPaint
                    else -> blackPaint
                }
                StackPane.setAlignment(icon, Pos.TOP_LEFT)
                stackedShit.add(icon)
            }
        }

        fun action_newRegion() {
            try {
                val arena = selectedArena()
                val xywh = initialRegionLocations[nextInitialRegionLocationIndex]
                if (++nextInitialRegionLocationIndex > initialRegionLocations.lastIndex)
                    nextInitialRegionLocationIndex = 0
                val newRegion = newBotinokRegion(name = "Region ${arena.regions.size + 1}",
                                                 x = xywh.x, y = xywh.y,
                                                 w = xywh.w, h = xywh.h,
                                                 arena = arena.arena)
                arena.arena.regions.add(newRegion)
                setDirty(true)
                val regionTreeItem = addTreeItemForRegion(newRegion, arena)
                arena.treeItem.expandedProperty().set(true)

                navigationTreeView.selectionModel.clearSelection()
                navigationTreeView.selectionModel.select(regionTreeItem)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun action_newPointer() {
            try {
                val arenaNode = selectedArena()
                val newPointer = newBotinokPointer(name = "Pointer ${arenaNode.pointers.size + 1}",
                                                  x = 50, y = 50,
                                                  pile = "{}",
                                                  language = "JavaScript",
                                                  script = "// Fuck you",
                                                  arena = arenaNode.arena)
                arenaNode.arena.pointers.add(newPointer)
                setDirty(true)
                val pointerTreeItem = addTreeItemForPointer(newPointer, arenaNode)
                arenaNode.treeItem.expandedProperty().set(true)

                navigationTreeView.selectionModel.clearSelection()
                navigationTreeView.selectionModel.select(pointerTreeItem)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        private fun selectedArena(): ArenaNode {
            return selectedTreeItem()!!.arena
        }

        private fun selectedRegion(): RegionNode? {
            return selectedTreeItem()?.region
        }

        private fun selectedPointer(): PointerNode? {
            return selectedTreeItem()?.pointer
        }

        private val TreeItem<FuckingNode>.region: RegionNode? get() {
            return value as? RegionNode
        }

        private val TreeItem<FuckingNode>.pointer: PointerNode? get() {
            return value as? PointerNode
        }

        private fun selectedTreeItem(): TreeItem<FuckingNode>? =
            navigationTreeView.selectionModel.selectedItem

        private val TreeItem<FuckingNode>.arena: ArenaNode get() {
            val value = value!!
            val arena: ArenaNode = when (value) {
                is FuckingNode.Root -> wtf("c420c2f1-7c89-45aa-89b6-0aee7faa4446")
                is ArenaNode -> value
                is RegionNode -> value.arenaNode
                is PointerNode -> value.arenaNode
            }
            return arena
        }

        @Suppress("unused")
        fun initialize() {

        }
    }

    private fun action_quit() {
        if (!maybeAskAndSave().cancelled) {
            primaryStage.close()
        }
    }

    private fun addTreeItemForRegion(region: BotinokRegion, arenaNode: ArenaNode): TreeItem<FuckingNode> {
        val regionTreeItem = TreeItem<FuckingNode>()
        val newRegionNode = RegionNode(regionTreeItem, region, arenaNode)
        regionTreeItem.value = newRegionNode
        arenaNode.treeItem.children += regionTreeItem
        return regionTreeItem
    }

    private fun addTreeItemForPointer(pointer: BotinokPointer, arenaNode: ArenaNode): TreeItem<FuckingNode> {
        val pointerTreeItem = TreeItem<FuckingNode>()
        val newPointerNode = PointerNode(pointerTreeItem, pointer, arenaNode)
        pointerTreeItem.value = newPointerNode
        arenaNode.treeItem.children += pointerTreeItem
        return pointerTreeItem
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
                            action_takeScreenshot()
                        }
                    }
                } catch(e: Throwable) {
                    e.printStackTrace()
                }
            }
        })
    }

    private inner class simulateSomeUserActions {
        init {thread {fuck1()}}

        fun fuck1() {
            Thread.sleep(500)
            JFXStuff.later {handleEnterKeyInPlaySelector()}
        }

        fun fuck2() {
            Thread.sleep(500)
            JFXStuff.later {handleEnterKeyInPlaySelector()}
            Thread.sleep(500)
            action_takeScreenshot()
            Thread.sleep(500)
            JFXStuff.later {bananas.action_newRegion()}
            Thread.sleep(500)
            JFXStuff.later {bananas.action_newRegion()}
        }
    }

    val tmpImgPath = "${FilePile.tmpDirPath}/d2185122-750e-432d-8d88-fad71b5021b5.png".replace("\\", "/")

    private fun action_takeScreenshot() {
        JFXStuff.later {

            run {
                val image = Robot().createScreenCapture(Rectangle(getDefaultToolkit().screenSize))
                ImageIO.write(image, "png", File(tmpImgPath))
                noise("Saved screenshot")
            }

            val arena = newBotinokArena(name = "Arena ${getArenaCount() + 1}", screenshot = File(tmpImgPath).readBytes(), play = play)
            play.arenas.add(arena)
            setDirty(true)
            addTreeItemForArena(arena)
            bananas.navigationTreeView.scrollTo(bananas.rootNode.children.lastIndex)
            selectLastTreeItem()

            JFXStuff.later {
                primaryStage.isIconified = true
                JFXStuff.later {
                    primaryStage.isIconified = false
                }
            }
        }
    }

    private fun addTreeItemForArena(arena: BotinokArena): TreeItem<FuckingNode> {
        val treeItem = TreeItem<FuckingNode>()
        val arenaNode = ArenaNode(treeItem, arena)
//        val arenaNode = FuckingNode.Arena(treeItem, arena, Image("file:///$tmpImgPath"))
        treeItem.value = arenaNode
        bananas.rootNode.children.add(treeItem)
        return treeItem
    }

    private fun selectLastTreeItem() {
        bananas.navigationTreeView.selectionModel.clearSelection()
        bananas.navigationTreeView.selectionModel.selectLast()
    }


    fun getArenaCount(): Int {
        return bananas.rootNode.children.size
    }

    fun action_save() {
        play = botinokPlayRepo.save(play)
        setDirty(false)
        JFXStuff.infoAlert("Your shit was saved OK")
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












