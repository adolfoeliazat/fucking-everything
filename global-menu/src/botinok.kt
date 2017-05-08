package vgrechka.botinok

import de.jensd.fx.glyphs.emojione.EmojiOne
import de.jensd.fx.glyphs.emojione.EmojiOneView
import javafx.application.Application
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Rectangle2D
import javafx.scene.Node
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
import org.springframework.data.jpa.repository.JpaContext
import vgrechka.*
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit.getDefaultToolkit
import java.awt.event.InputEvent
import java.io.File
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import kotlin.concurrent.thread
import kotlin.properties.Delegates.notNull
import kotlin.system.exitProcess

// TODO:vgrechka Shift positions after deleting shit

// delete from botinok_regions; delete from botinok_pointers; delete from botinok_arenas; delete from botinok_plays;

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

sealed class FuckingNode

class RootNode : FuckingNode()

class ArenaNode(val treeItem: TreeItem<FuckingNode>,
                var arena: BotinokArena) : FuckingNode() {

    val image by lazy {Image(arena.screenshot.inputStream())}
    val regions: List<RegionNode> get() = treeItem.children.map {it.value as? RegionNode}.filterNotNull()
    val pointers: List<PointerNode> get() = treeItem.children.map {it.value as? PointerNode}.filterNotNull()

    override fun toString() = arena.name
}

data class RegionNode(val treeItem: TreeItem<FuckingNode>,
                      var region: BotinokRegion,
                      val arenaNode: ArenaNode) : FuckingNode() {

    override fun toString() = region.name

}

data class PointerNode(val treeItem: TreeItem<FuckingNode>,
                       var pointer: BotinokPointer,
                       val arenaNode: ArenaNode) : FuckingNode() {

    override fun toString() = pointer.name

//    fun isHit(testX: Double, testY: Double) =
//        testX >= region.x && testX <= region.x + region.w - 1 && testY >= region.y && testY <= region.y + region.h - 1
}

class StartBotinok : Application() {
    var primaryStage by notNullOnce<Stage>()
    var bananas by notNullOnce<goBananas2>()
    var handleEnterKeyInPlaySelector by notNull<() -> Unit>()
    var afterPlayEditorOpened = {}
    var play by notNull<BotinokPlay>()
    @Volatile var running = false
    @Volatile var stopRequested = false
    val robot = Robot()
    val tmpImgPath = "${FilePile.tmpDirPath}/d2185122-750e-432d-8d88-fad71b5021b5.png".replace("\\", "/")

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

    var dirty = false
        set(value) {
            if (field != value) {
                field = value
                updateStageTitle()
            }
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

    fun openPlaySelector() {
        backPlatform.tx {
            primaryStage.title = "Botinok"
            class Item(val play: BotinokPlay) {
                override fun toString() = play.name
            }

            val listView = ListView<Item>()
            val plays = botinokPlayRepo.findAll()
            listView.items = FXCollections.observableArrayList(plays.map {Item(it)})
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
    }

    fun action_openPlayEditor(_play: BotinokPlay) {
        backPlatform.tx {
            this.play = botinokPlayRepo.findOne(_play.id)!!

            updateStageTitle()
            bananas = goBananas2()

            val arenas = play.arenas.toMutableList()
            arenas.sortBy {it.position}
            for (arena in arenas) {
                val arenaTreeItem = addTreeItemForArena(arena)
                val regions = arena.regions.toMutableList()
                regions.sortBy {it.position}
                for (region in regions) {
                    addTreeItemForRegion(region, (arenaTreeItem.value as ArenaNode))
                }
                val pointers = arena.pointers.toMutableList()
                pointers.sortBy {it.position}
                for (pointer in pointers) {
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

            afterPlayEditorOpened()
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
        var mainSplitPane by notNull<SplitPane>()
        var detailsPane by notNull<AnchorPane>()
        var currentContextMenu: ContextMenu? = null
        var selectedRegionHandles = setOf<RegionHandle>()
        var nextInitialRegionLocationIndex = 0
        var canvasContextMenu by notNull<ContextMenu>()
        var boxContextMenu by notNull<ContextMenu>()
        var operationStartRegionParams by notNull<Box>()
        var operationStartMouseX = 0.0
        var operationStartMouseY = 0.0
        val rootNode: TreeItem<FuckingNode> = TreeItem(RootNode())
        var drawingAreaStackPane by notNull<StackPane>()
        val pointerWidth = 17
        val pointerHeight = 25
        var leftSplitPane: SplitPane
        val statusLabel: Label
        var runMenuItem by notNullOnce<MenuItem>()
        var stopMenuItem by notNullOnce<MenuItem>()

        inner class RegionLocation(val x: Int, val y: Int, val w: Int, val h: Int)

        val initialRegionLocations = listOf(
            RegionLocation(x = 20, y = 20, w = 150, h = 50),
            RegionLocation(x = 100, y = 100, w = 50, h = 150)
        )

        fun isHit(regionNode: RegionNode, testX: Double, testY: Double): Boolean {
            val r = regionNode.region
            return testX >= r.x && testX <= r.x + r.w - 1 && testY >= r.y && testY <= r.y + r.h - 1
        }

        fun isHit(pointerNode: PointerNode, testX: Double, testY: Double): Boolean {
            val p = pointerNode.pointer
            return testX >= p.x && testX <= p.x + pointerWidth - 1 && testY >= p.y && testY <= p.y + pointerWidth - 1
        }

        val canvas by relazy {
            detailsPane.children.clear()
            val scrollPane = ScrollPane()
            stretch(scrollPane)
            detailsPane.children += scrollPane
            val canvas = Canvas(selectedArenaNode().image.width, selectedArenaNode().image.height)

            drawingAreaStackPane = StackPane()
            drawingAreaStackPane.children += canvas
            scrollPane.content = drawingAreaStackPane

            canvas.addEventHandler(MouseEvent.MOUSE_PRESSED) {e->
                if (e.button == MouseButton.PRIMARY) {
                    // noise("MOUSE_PRESSED: e.x = ${e.x}; e.y = ${e.y}")
                    hideContextMenus()
                    val hitRegionNode: RegionNode? = run {
                        o@for (region in selectedArenaNode().regions) {
                            for (handle in RegionHandle.values()) {
                                if (handle.rectForRegion(region).contains(e.x, e.y)) {
                                    selectedRegionHandles = setOf(handle)
                                    return@run region
                                }
                            }
                            if (isHit(region, e.x, e.y)) {
                                selectedRegionHandles = RegionHandle.values().toSet()
                                return@run region
                            }
                        }
                        null
                    }
                    if (hitRegionNode != null) {
                        selectTreeItem(hitRegionNode.treeItem)
                        operationStartRegionParams = Box(hitRegionNode.region.x, hitRegionNode.region.y, hitRegionNode.region.w, hitRegionNode.region.h)
                        operationStartMouseX = e.x
                        operationStartMouseY = e.y
                        // noise("operationStartMouseX = $operationStartMouseX; operationStartMouseY = $operationStartMouseY")
                    } else {
                        val hitPointerNode: PointerNode? = selectedArenaNode().pointers.find {
                            val p = it.pointer
                            val pointerRect = Rectangle2D(p.x.toDouble(), p.y.toDouble(), pointerWidth.toDouble(), pointerHeight.toDouble())
                            pointerRect.contains(e.x, e.y)
                        }
                        if (hitPointerNode != null) {
                            selectTreeItem(hitPointerNode.treeItem)
                            operationStartRegionParams = Box(hitPointerNode.pointer.x, hitPointerNode.pointer.y, -123, -123)
                            operationStartMouseX = e.x
                            operationStartMouseY = e.y
                        } else {
                            selectTreeItem(selectedArenaNode().treeItem)
                        }
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

                    fun updateShit() {
                        dirty = true
                        drawArena()
                    }

                    val dx = Math.round(e.x - operationStartMouseX).toInt()
                    val dy = Math.round(e.y - operationStartMouseY).toInt()

                    val selectedRegion = selectedRegionNode()
                    if (selectedRegion != null) {
                        // noise("dx = $dx; dy = $dy")
                        val dragMutators = selectedRegionHandles.flatMap{it.dragMutators}.toSet()
                        val points = BoxPoints(operationStartRegionParams.x, operationStartRegionParams.y, operationStartRegionParams.x + operationStartRegionParams.w - 1, operationStartRegionParams.y + operationStartRegionParams.h - 1)
                        dragMutators.forEach {it.mutate(points, dx, dy)}
                        selectedRegion.region.x = points.minX
                        selectedRegion.region.y = points.minY
                        selectedRegion.region.w = points.maxX - points.minX + 1
                        selectedRegion.region.h = points.maxY - points.minY + 1
                        updateShit()
                    } else {
                        val selectedPointer = selectedPointerNode()
                        if (selectedPointer != null) {
                            selectedPointer.pointer.x = operationStartRegionParams.x + dx
                            selectedPointer.pointer.y = operationStartRegionParams.y + dy
                            updateShit()
                        }
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
                        o.setOnAction {action_quit()}
                    }
                }

                o.menus += Menu("_Play")-{o->
                    runMenuItem = MenuItem("_Run")-{o->
                        o.setOnAction {action_run()}
                    }
                    o.items += runMenuItem

                    stopMenuItem = MenuItem("_Stop")-{o->
                        o.setOnAction {action_stop()}
                        o.isDisable = true
                    }
                    o.items += stopMenuItem
                }

                o.menus += Menu("_Tools")-{o->
                    o.items += MenuItem("_Mess Around")-{o->
                        o.setOnAction {action_messAround()}
                    }
                }
            }

            mainSplitPane = SplitPane()
            vbox.children += mainSplitPane
            statusLabel = Label()
            statusLabel.padding = Insets(5.0)
            resetStatusLabel()
            vbox.children += statusLabel
            VBox.setVgrow(mainSplitPane, Priority.ALWAYS)
            mainSplitPane.setDividerPosition(0, 0.2)

            leftSplitPane = SplitPane()
            leftSplitPane.orientation = Orientation.VERTICAL
            navigationTreeView = TreeView()
            leftSplitPane.items += navigationTreeView

            mainSplitPane.items += leftSplitPane

            detailsPane = AnchorPane()
            mainSplitPane.items += detailsPane

            navigationTreeView.setOnMouseClicked {
                currentContextMenu?.hide()
                currentContextMenu = null
            }

            navigationTreeView.setOnContextMenuRequested {e->
                makeTreeContextMenu(e)
            }

            navigationTreeView.selectionModel.selectedItemProperty().addListener {_, oldValue, newValue ->
                handleTreeSelectionChanged(oldValue, newValue)
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

        fun resetStatusLabel() {
            statusLabel.text = "Fucking around? :)"
        }

        private fun makeTreeContextMenu(e: ContextMenuEvent) {
            val item = navigationTreeView.selectionModel.selectedItem
            val value = item?.value

            val menu = ContextMenu()
            when (value) {
                is ArenaNode -> {
                    addMenuItem(menu, "Rename") {action_renameArena(item, value)}
                    if (item !== rootNode.children.first())
                        addMenuItem(menu, "Move Up") {action_moveArenaIndexDelta(item, value, -1)}
                    if (item !== rootNode.children.last())
                        addMenuItem(menu, "Move Down") {action_moveArenaIndexDelta(item, value, +1)}
                    addMenuItem(menu, "New Region", this::action_newRegion)
                    addMenuItem(menu, "New Pointer", this::action_newPointer)
                    menu.items += SeparatorMenuItem()
                    addMenuItem(menu, "Delete") {action_deleteArena(item, value)}
                }
                is RegionNode -> {

                }
            }

            if (menu.items.isNotEmpty()) {
                menu.show(navigationTreeView, e.screenX, e.screenY)
                currentContextMenu = menu
            }
        }

        private fun action_deleteArena(item: TreeItem<FuckingNode>, arenaNode: ArenaNode) {
            check(item.parent.value is RootNode) {"6c32dc0c-3ffa-4f4e-875f-d63d587f6132"}
            play.arenas.remove(arenaNode.arena)
            item.parent.children -= item
        }

        private fun action_moveArenaIndexDelta(item: TreeItem<FuckingNode>, arenaNode: ArenaNode, i: Int) {
            run { // Entities
                val arena = arenaNode.arena
                val index = play.arenas.indexOfOrNull(arena) ?: wtf("db8f6365-cf88-494f-8f6a-7a07b11c01f5")

                val a = play.arenas[index + i]
                play.arenas[index + i] = play.arenas[index]
                play.arenas[index] = a

                val p = play.arenas[index + i].position
                play.arenas[index + i].position = play.arenas[index].position
                play.arenas[index].position = p
            }

            run { // Tree
                val index = rootNode.children.indexOfOrNull(item) ?: wtf("5df53fb4-534a-49df-832d-ee31043c7f19")
                rootNode.children.removeAt(index)
                rootNode.children.add(index + i, item)
            }
            dirty = true
        }

        private fun action_renameArena(treeItem: TreeItem<FuckingNode>, arenaNode: ArenaNode) {
            val arena = arenaNode.arena
            JFXStuff.inputBox("So, name?", arena.name)?.let {
                arena.name = it
                treeItem.value = ArenaNode(treeItem, arena)
                dirty = true
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

        fun handleTreeSelectionChanged(oldItem: TreeItem<FuckingNode>?, newItem: TreeItem<FuckingNode>?) {
            if (leftSplitPane.items.size > 1)
                leftSplitPane.items.subList(1, leftSplitPane.items.size).clear()

            if (newItem == null) {
                detailsPane.children.clear()
                relazy.reset(this::canvas)
                return
            }

            if (oldItem?.regionNode != selectedRegionNode()) {
                selectedRegionHandles = RegionHandle.values().toSet()
            }

            newItem.pointerNode?.let {pointerNode ->
                val pileTextArea = TextArea()
                leftSplitPane.items += pileTextArea
                leftSplitPane.setDividerPosition(0, 0.8)
                pileTextArea.text = pointerNode.pointer.pile
                pileTextArea.textProperty().addListener {_, _, newValue->
                    pointerNode.pointer.pile = newValue
                    dirty = true
                }
            }

            drawArena()
        }

        fun drawArena() {
            val gc = canvas.graphicsContext2D
            gc.drawImage(selectedArenaNode().image, 0.0, 0.0)

            for (regionNode in selectedArenaNode().regions) {
                val darkPaint = Color(0.5, 0.0, 0.0, 1.0)
                val brightPaint = Color(1.0, 0.0, 0.0, 1.0)

                val isFocused = selectedRegionNode() === regionNode

                gc.stroke = when {
                    isFocused -> darkPaint
                    else -> brightPaint
                }
                val b = BotinokStuff
                gc.lineWidth = b.boxEdgeSize
                gc.strokeRect(regionNode.region.x.toDouble() - b.boxEdgeSize / 2, regionNode.region.y.toDouble() - b.boxEdgeSize / 2, regionNode.region.w.toDouble() + b.boxEdgeSize, regionNode.region.h.toDouble() + b.boxEdgeSize)

                if (isFocused) {
                    for (handle in RegionHandle.values()) {
                        gc.fill = when {
                            handle in selectedRegionHandles -> brightPaint
                            else -> darkPaint
                        }
                        val rect = handle.rectForRegion(regionNode)
                        gc.fillRect(rect.minX, rect.minY, rect.width, rect.height)
                    }
                }
            }

            for (pointerNode in selectedArenaNode().pointers) {
                val isFocused = selectedPointerNode() === pointerNode

                val w = pointerWidth.toDouble()
                val h = pointerHeight.toDouble()
                val x0 = pointerNode.pointer.x.toDouble()
                val y0 = pointerNode.pointer.y.toDouble()
                val x1 = x0 + w - 1
                val y1 = y0 + h - 1

//                gc.fill = Color(0.0, 0.0, 1.0, 0.2)
//                gc.fillRect(x0, y0, w, h)

                val paint = when {
                    isFocused -> Color(1.0, 0.0, 0.0, 1.0)
                    else -> Color(1.0, 0.0, 0.0, 1.0)
                }
                gc.fill = paint

                gc.beginPath()
                gc.moveTo(x0, y0)
                gc.lineTo(x1, y0 + (y1 - y0) * 0.70)
                gc.lineTo(x0 + (x1 - x0) * 0.35, y0 + (y1 - y0) * 0.7)
                gc.lineTo(x0, y1)
                gc.closePath()
                gc.fill()

                gc.lineWidth = 4.0
                gc.stroke = paint
                gc.beginPath()
                gc.lineTo(x0 + (x1 - x0) * 0.40, y0 + (y1 - y0) * 0.7)
                gc.lineTo(x0 + (x1 - x0) * 0.60, y1)
                gc.closePath()
                gc.fill()
                gc.stroke()
            }
        }

        fun action_newRegion() {
            try {
                val arenaNode = selectedArenaNode()
                val xywh = initialRegionLocations[nextInitialRegionLocationIndex]
                if (++nextInitialRegionLocationIndex > initialRegionLocations.lastIndex)
                    nextInitialRegionLocationIndex = 0
                val newRegion = newBotinokRegion(name = "Region ${arenaNode.regions.size + 1}",
                                                 x = xywh.x, y = xywh.y,
                                                 w = xywh.w, h = xywh.h,
                                                 arena = arenaNode.arena,
                                                 position = arenaNode.arena.regions.size)
                arenaNode.arena.regions.add(newRegion)
                dirty = true
                val regionTreeItem = addTreeItemForRegion(newRegion, arenaNode)
                arenaNode.treeItem.expandedProperty().set(true)

                navigationTreeView.selectionModel.clearSelection()
                navigationTreeView.selectionModel.select(regionTreeItem)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun action_newPointer() {
            try {
                val arenaNode = selectedArenaNode()
                val newPointer = newBotinokPointer(name = "Pointer ${arenaNode.pointers.size + 1}",
                                                   x = 50, y = 50,
                                                   pile = "{}",
                                                   language = "JavaScript",
                                                   script = "// Fuck you",
                                                   arena = arenaNode.arena,
                                                   position = arenaNode.arena.pointers.size)
                arenaNode.arena.pointers.add(newPointer)
                dirty = true
                val pointerTreeItem = addTreeItemForPointer(newPointer, arenaNode)
                arenaNode.treeItem.expandedProperty().set(true)

                navigationTreeView.selectionModel.clearSelection()
                navigationTreeView.selectionModel.select(pointerTreeItem)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun selectedArenaNode(): ArenaNode {
            return selectedTreeItem()!!.arena
        }

        fun selectedRegionNode(): RegionNode? {
            return selectedTreeItem()?.regionNode
        }

        fun selectedPointerNode(): PointerNode? {
            return selectedTreeItem()?.pointerNode
        }

        val TreeItem<FuckingNode>.regionNode: RegionNode? get() {
            return value as? RegionNode
        }

        val TreeItem<FuckingNode>.pointerNode: PointerNode? get() {
            return value as? PointerNode
        }

        fun selectedTreeItem(): TreeItem<FuckingNode>? =
            navigationTreeView.selectionModel.selectedItem

        val TreeItem<FuckingNode>.arena: ArenaNode get() {
            val value = value!!
            val arena: ArenaNode = when (value) {
                is RootNode -> wtf("c420c2f1-7c89-45aa-89b6-0aee7faa4446")
                is ArenaNode -> value
                is RegionNode -> value.arenaNode
                is PointerNode -> value.arenaNode
            }
            return arena
        }
    }

    private fun stretch(node: Node) {
        AnchorPane.setTopAnchor(node, 0.0)
        AnchorPane.setRightAnchor(node, 0.0)
        AnchorPane.setBottomAnchor(node, 0.0)
        AnchorPane.setLeftAnchor(node, 0.0)
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

        fun fuck3() {
            afterPlayEditorOpened = {
                val pointerTreeItem = bananas.selectedArenaNode().treeItem.children.find {it.value is PointerNode}
                    ?: wtf("228ade45-63d1-4aab-966d-2a8ddc0fedec")
                bananas.navigationTreeView.selectionModel.select(pointerTreeItem)
            }
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

    private fun action_takeScreenshot() {
        stopRequested = true
        JFXStuff.later {
            run {
                val image = robot.createScreenCapture(Rectangle(getDefaultToolkit().screenSize))
                ImageIO.write(image, "png", File(tmpImgPath))
                noise("Saved screenshot")
            }

            val arena = newBotinokArena(name = "Arena ${getArenaCount() + 1}",
                                        screenshot = File(tmpImgPath).readBytes(),
                                        play = play,
                                        position = play.arenas.size)
            play.arenas.add(arena)
            dirty = true
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
        backPlatform.tx {
            play = botinokPlayRepo.save(play)

            var arenaIndex = 0
            for (arenaTreeItem in bananas.rootNode.children) {
                val arenaNode = arenaTreeItem.value as ArenaNode
                arenaNode.arena = play.arenas[arenaIndex++]
                var regionIndex = 0
                var pointerIndex = 0
                for (child in arenaTreeItem.children) {
                    val value = child.value
                    exhaustive=when (value) {
                        is RegionNode -> value.region = arenaNode.arena.regions[regionIndex++]
                        is PointerNode -> value.pointer = arenaNode.arena.pointers[pointerIndex++]
                        else -> wtf("9774d118-0b21-4ff3-953d-a371fcd6ce64")
                    }
                }
            }
        }

        dirty = false
        JFXStuff.infoAlert("Your shit was saved OK")
    }

    fun action_run() {
        if (running) return
        bananas.runMenuItem.isDisable = true
        bananas.stopMenuItem.isDisable = false
        running = true
        bananas.statusLabel.text = "Running..."
        stopRequested = false
        thread {
            arenas@for (arena in play.arenas) {
                if (arena.regions.isEmpty()) {
                    noise("Arena ${arena.name}: no regions -- skipping")
                    continue@arenas
                }
                if (arena.pointers.isEmpty()) {
                    noise("Arena ${arena.name}: no pointers -- skipping")
                    continue@arenas
                }
                if (arena.pointers.size > 1) {
                    noise("Arena ${arena.name}: multiple pointers are not yet supported -- skipping")
                    continue@arenas
                }

                noise("Arena ${arena.name}: waiting for match")
                val arenaImage = ImageIO.read(arena.screenshot.inputStream())
                check(arenaImage.raster.numBands == 3) {"9332a09a-417e-4c3b-8867-9bf76af6b47f"}
                while (true) {
                    val time0 = System.currentTimeMillis()
                    val screenImage = robot.createScreenCapture(Rectangle(getDefaultToolkit().screenSize))
                    check(screenImage.raster.numBands == 3) {"99e06055-4be8-4c29-8ac9-6437663e5d8e"}
                    var allRegionsMatched = true
                    regions@for (region in arena.regions) {
                        if (stopRequested) break@arenas

                        for (x in region.x until region.x + region.w) {
                            for (y in region.y until region.y + region.h) {
                                val screenPixel = intArrayOf(-1, -1, -1)
                                screenImage.raster.getPixel(x, y, screenPixel)
                                // noise("Screen pixel: ${screenPixel[0]}, ${screenPixel[1]}, ${screenPixel[2]}")

                                val arenaPixel = intArrayOf(-1, -1, -1)
                                arenaImage.raster.getPixel(x, y, arenaPixel)
                                // noise("Arena pixel: ${arenaPixel[0]}, ${arenaPixel[1]}, ${arenaPixel[2]}")

                                if (!Arrays.equals(screenPixel, arenaPixel)) {
                                    allRegionsMatched = false
                                    break@regions
                                }
                            }
                        }
                    }

                    if (allRegionsMatched) {
                        noise("Arena ${arena.name}: matched")
                        val pointer = arena.pointers.first()
                        robot.mouseMove(pointer.x, pointer.y)
                        Thread.sleep(250)
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)

                        // TODO:vgrechka Think about...
                        // In many cases this time will be enough for UI to respond,
                        // so next iteration will match right away, and we avoid longer waiting. Or not?
                        Thread.sleep(250)

                        continue@arenas
                    }

                    noise("Tick: ${System.currentTimeMillis() - time0}ms")
                    Thread.sleep(1000)
                }
            }

            running = false
            JFXStuff.later {
                bananas.runMenuItem.isDisable = false
                bananas.stopMenuItem.isDisable = true
                bananas.resetStatusLabel()
                noise("Stopped")
            }
        }
    }

    fun action_stop() {
        if (!running) return
        stopRequested = true
        bananas.statusLabel.text = "Stopping..."
    }

    fun action_messAround() {
        val pointer = findPointer(arenaIndex = 0)
        thread {
            robot.mouseMove(pointer.x, pointer.y)
            Thread.sleep(250)
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        }
    }

    fun findPointer(arenaIndex: Int, pointerIndex: Int = 0): BotinokPointer {
        return play.arenas[arenaIndex].pointers[pointerIndex]
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










