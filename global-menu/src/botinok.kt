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
import java.awt.event.InputEvent
import java.io.File
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import kotlin.concurrent.thread
import kotlin.properties.Delegates.notNull
import kotlin.reflect.KMutableProperty0
import kotlin.system.exitProcess

// delete from botinok_regions; delete from botinok_pointers; delete from botinok_arenas; delete from botinok_plays;

// TODO:vgrechka Sanity checks during model loading (e.g. that positions are correct)
//               Show a message if something is messed up

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
        override fun rectForRegion(r: BotinokRegion) = Rectangle2D(
            r.x.toDouble() - b.obscureConst2 + b.obscureConst1 - b.handleSize + b.boxEdgeSize,
            r.y.toDouble() - b.obscureConst2 - b.obscureConst1,
            b.handleSize,
            b.handleSize)
        override val dragMutators = setOf(DragMutator.TOP, DragMutator.LEFT)
    },
    TOP {
        override fun rectForRegion(r: BotinokRegion) = Rectangle2D(
            r.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + r.w / 2 - b.handleSize / 2,
            r.y.toDouble() - b.obscureConst2 - b.obscureConst1,
            b.handleSize,
            b.handleSize)
        override val dragMutators = setOf(DragMutator.TOP)
    },
    TOP_RIGHT {
        override fun rectForRegion(r: BotinokRegion) = Rectangle2D(
            r.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + r.w,
            r.y.toDouble() - b.obscureConst2 - b.obscureConst1,
            b.handleSize,
            b.handleSize)
        override val dragMutators = setOf(DragMutator.TOP, DragMutator.RIGHT)
    },
    RIGHT {
        override fun rectForRegion(r: BotinokRegion) = Rectangle2D(
            r.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + r.w,
            r.y.toDouble() - b.obscureConst2 - b.obscureConst1 + r.h / 2 + b.handleSize / 2,
            b.handleSize,
            b.handleSize)
        override val dragMutators = setOf(DragMutator.RIGHT)
    },
    BOTTOM_RIGHT {
        override fun rectForRegion(r: BotinokRegion) = Rectangle2D(
            r.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + r.w,
            r.y.toDouble() - b.obscureConst2 - b.obscureConst1 + r.h + b.handleSize,
            b.handleSize,
            b.handleSize)
        override val dragMutators = setOf(DragMutator.BOTTOM, DragMutator.RIGHT)
    },
    BOTTOM {
        override fun rectForRegion(r: BotinokRegion) = Rectangle2D(
            r.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + r.w / 2 - b.handleSize / 2,
            r.y.toDouble() - b.obscureConst2 - b.obscureConst1 + r.h + b.handleSize,
            b.handleSize,
            b.handleSize)
        override val dragMutators = setOf(DragMutator.BOTTOM)
    },
    BOTTOM_LEFT {
        override fun rectForRegion(r: BotinokRegion) = Rectangle2D(
            r.x.toDouble() - b.obscureConst2 + b.obscureConst1 - b.handleSize + b.boxEdgeSize,
            r.y.toDouble() - b.obscureConst2 - b.obscureConst1 + r.h + b.handleSize,
            b.handleSize,
            b.handleSize)
        override val dragMutators = setOf(DragMutator.BOTTOM, DragMutator.LEFT)
    },
    LEFT {
        override fun rectForRegion(r: BotinokRegion) = Rectangle2D(
            r.x.toDouble() - b.obscureConst2 + b.obscureConst1 - b.handleSize + b.boxEdgeSize,
            r.y.toDouble() - b.obscureConst2 - b.obscureConst1 + r.h / 2 + b.handleSize / 2,
            b.handleSize,
            b.handleSize)
        override val dragMutators = setOf(DragMutator.LEFT)
    };

    companion object {
        private val b = BotinokStuff
    }

    abstract fun rectForRegion(r: BotinokRegion): Rectangle2D
    abstract val dragMutators: Set<DragMutator>
}

typealias FuckingTreeItem = TreeItem<Any>

class StartBotinok : Application() {
    var primaryStage by notNullOnce<Stage>()
    var bananas by notNullOnce<goBananas2>()
    var handleEnterKeyInPlaySelector by notNull<() -> Unit>()
    var afterPlayEditorOpened = {}
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
            bananas = goBananas2(botinokPlayRepo.findOne(_play.id)!!)
            updateStageTitle()

            val arenas = bananas.playNode.iplay.arenas
            arenas.sortBy {it.position}
            for (arena in arenas) {
                val arenaNode = newArenaNode(arena, bananas.playNode)
                val regions = arena.regions
                regions.sortBy {it.position}
                for (region in regions) {
                    newRegionNode(region, arenaNode)
                }
                val pointers = arena.pointers
                pointers.sortBy {it.position}
                for (pointer in pointers) {
                    newPointerNode(pointer, arenaNode)
                }
            }

            val rootItems = bananas.playNode.treeItem.children
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
        primaryStage.title = "Botinok - ${bananas.playNode.iplay.entity.name}${dirty.thenElseEmpty{" *"}}"
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

    inner class goBananas2(_play: BotinokPlay) {
        var someTextField by notNull<TextField>()
        var navigationTreeView by notNull<TreeView<Any>>()
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
//        val rootTreeItem: FuckingTreeItem = TreeItem()
        var drawingAreaStackPane by notNull<StackPane>()
        val pointerWidth = 17
        val pointerHeight = 25
        var leftSplitPane: SplitPane
        val statusLabel: Label
        var runMenuItem by notNullOnce<MenuItem>()
        var stopMenuItem by notNullOnce<MenuItem>()
        val playNode: PussyNode

        inner class RegionLocation(val x: Int, val y: Int, val w: Int, val h: Int)

        val initialRegionLocations = listOf(
            RegionLocation(x = 20, y = 20, w = 150, h = 50),
            RegionLocation(x = 100, y = 100, w = 50, h = 150)
        )

        fun isHit(r: BotinokRegion, testX: Double, testY: Double): Boolean {
            return testX >= r.x && testX <= r.x + r.w - 1 && testY >= r.y && testY <= r.y + r.h - 1
        }

        fun isHit(p: BotinokPointer, testX: Double, testY: Double): Boolean {
            return testX >= p.x && testX <= p.x + pointerWidth - 1 && testY >= p.y && testY <= p.y + pointerWidth - 1
        }

        val canvas by relazy {
            detailsPane.children.clear()
            val scrollPane = ScrollPane()
            stretch(scrollPane)
            detailsPane.children += scrollPane
            val canvas = Canvas(displayedArenaNode().image.width, displayedArenaNode().image.height)

            drawingAreaStackPane = StackPane()
            drawingAreaStackPane.children += canvas
            scrollPane.content = drawingAreaStackPane

            canvas.addEventHandler(MouseEvent.MOUSE_PRESSED) {e->
                if (e.button == MouseButton.PRIMARY) {
                    // noise("MOUSE_PRESSED: e.x = ${e.x}; e.y = ${e.y}")
                    hideContextMenus()
                    val hitRegionNode: PussyNode.IRegion? = run {
                        o@for (iregion in displayedArenaNode().regionNodes) {
                            for (handle in RegionHandle.values()) {
                                if (handle.rectForRegion(iregion.entity).contains(e.x, e.y)) {
                                    selectedRegionHandles = setOf(handle)
                                    return@run iregion
                                }
                            }
                            if (isHit(iregion.entity, e.x, e.y)) {
                                selectedRegionHandles = RegionHandle.values().toSet()
                                return@run iregion
                            }
                        }
                        null
                    }
                    if (hitRegionNode != null) {
                        selectTreeItem(hitRegionNode.node.treeItem)
                        operationStartRegionParams = Box(hitRegionNode.entity.x, hitRegionNode.entity.y, hitRegionNode.entity.w, hitRegionNode.entity.h)
                        operationStartMouseX = e.x
                        operationStartMouseY = e.y
                        // noise("operationStartMouseX = $operationStartMouseX; operationStartMouseY = $operationStartMouseY")
                    } else {
                        val hitPointerNode: PussyNode.IPointer? = displayedArenaNode().pointerNodes.find {
                            val p = it.entity
                            val pointerRect = Rectangle2D(p.x.toDouble(), p.y.toDouble(), pointerWidth.toDouble(), pointerHeight.toDouble())
                            pointerRect.contains(e.x, e.y)
                        }
                        if (hitPointerNode != null) {
                            selectTreeItem(hitPointerNode.node.treeItem)
                            operationStartRegionParams = Box(hitPointerNode.entity.x, hitPointerNode.entity.y, -123, -123)
                            operationStartMouseX = e.x
                            operationStartMouseY = e.y
                        } else {
                            selectTreeItem(displayedArenaNode().node.treeItem)
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
                        val en = selectedRegion.entity
                        en.x = points.minX
                        en.y = points.minY
                        en.w = points.maxX - points.minX + 1
                        en.h = points.maxY - points.minY + 1
                        updateShit()
                    } else {
                        val selectedPointer = selectedPointerNode()
                        if (selectedPointer != null) {
                            val en = selectedPointer.entity
                            en.x = operationStartRegionParams.x + dx
                            en.y = operationStartRegionParams.y + dy
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
                object : TreeCell<Any>() {
                    override fun updateItem(item: Any?, empty: Boolean) {
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

            playNode = newPlayNode(_play)
            navigationTreeView.root = playNode.treeItem
            navigationTreeView.root.isExpanded = true

            primaryStage.scene = Scene(vbox)
        }

        fun resetStatusLabel() {
            statusLabel.text = "Fucking around? :)"
        }


        private fun makeTreeContextMenu(e: ContextMenuEvent) {
            val treeItem = navigationTreeView.selectionModel.selectedItem
            val menu = ContextMenu()

            val node = treeItem.value as PussyNode
            exhaustive=when (node.type) {
                BotinokNodeType.ARENA -> {
                    addRenameMenuItem(menu, node)
                    addMoveMenuItems(menu, node)
                    addMenuItem(menu, "New Region", this::action_newRegion)
                    addMenuItem(menu, "New Pointer", this::action_newPointer)
                    menu.items += SeparatorMenuItem()
                    addDeleteMenuItem(menu, node)
                }

                BotinokNodeType.REGION -> {
                    addRenameMenuItem(menu, node)
                    addDeleteMenuItem(menu, node)
                }

                BotinokNodeType.POINTER -> {
                    addRenameMenuItem(menu, node)
                    addDeleteMenuItem(menu, node)
                }

                BotinokNodeType.PLAY -> wtf("cff6704f-dee7-4ddb-92fd-fe0c0de0de50")
            }

            if (menu.items.isNotEmpty()) {
                menu.show(navigationTreeView, e.screenX, e.screenY)
                currentContextMenu = menu
            }
        }

        private fun addMoveMenuItems(menu: ContextMenu, node: PussyNode) {
            // TODO:vgrechka Generalize. Currently works only for arenas
            val iarena = node.iarena
            val treeItem = node.treeItem

            fun move(delta: Int) {
                run { // Entities
                    val arena = iarena.entity
                    val play = bananas.playNode.iplay.entity
                    val index = play.arenas.indexOfOrNull(arena) ?: wtf("db8f6365-cf88-494f-8f6a-7a07b11c01f5")

                    val a = play.arenas[index]
                    play.arenas[index] = play.arenas[index + delta]
                    play.arenas[index + delta] = a

                    val p = play.arenas[index].position
                    play.arenas[index].position = play.arenas[index + delta].position
                    play.arenas[index + delta].position = p

                    val n = playNode.childNodes[index]
                    playNode.childNodes[index] = playNode.childNodes[index + delta]
                    playNode.childNodes[index + delta] = n
                }

                run { // Tree
                    val rootTreeItem = playNode.treeItem
                    val index = rootTreeItem.children.indexOfOrNull(treeItem) ?: wtf("5df53fb4-534a-49df-832d-ee31043c7f19")
                    val tmp = rootTreeItem.children[index]
                    rootTreeItem.children[index] = rootTreeItem.children[index + delta]
                    rootTreeItem.children[index + delta] = tmp
                    navigationTreeView.selectionModel.clearSelection()
                    selectTreeItem(treeItem)
                }

                dirty = true
            }

            if (treeItem !== treeItem.parent.children.first())
                addMenuItem(menu, "Move Up") {move(-1)}
            if (treeItem !== treeItem.parent.children.last())
                addMenuItem(menu, "Move Down") {move(+1)}
        }

        private fun addDeleteMenuItem(menu: ContextMenu, node: PussyNode) {
            addMenuItem(menu, "Delete") {
                val entity = node.entity
                val collection = when (entity) {
                    is BotinokArena -> entity.play.arenas
                    is BotinokRegion -> entity.arena.regions
                    is BotinokPointer -> entity.arena.pointers
                    else -> wtf("4e674ed4-423b-4bc5-9431-bc62692f968a")
                }
                val indexToDelete = collection.indexOf(entity)
                collection.removeAt(indexToDelete)

                for (index in indexToDelete..collection.lastIndex) {
                    val x = collection[index]
                    val positionProperty = when (x) {
                        // `let` here is a workaround for https://youtrack.jetbrains.com/issue/KT-17799
                        is BotinokArena -> x.let {it::position}
                        is BotinokRegion -> x.let {it::position}
                        is BotinokPointer -> x.let {it::position}
                        else -> wtf("22a2f24c-2395-4ad8-806e-70fc3f10c7ec")
                    }
                    positionProperty.set(positionProperty.get() - 1)
                }

                node.parent!!.childNodes.remove(node)
                node.treeItem.parent.children -= node.treeItem
                dirty = true
            }
        }

        fun addRenameMenuItem(menu: ContextMenu, node: PussyNode) {
            addMenuItem(menu, "Rename") {
                val entity = node.entity
                val nameProperty = BotinokPile.entityNameProperty(entity)
                JFXStuff.inputBox("So, name?", nameProperty.get())?.let {
                    nameProperty.set(it)
                    val parentChildren = node.treeItem.parent.children
                    val index = parentChildren.indexOf(node.treeItem)
                    parentChildren.removeAt(index)
                    parentChildren.add(index, node.treeItem)
                    selectTreeItem(node.treeItem)
                    dirty = true
                }
            }
        }

        private fun hideContextMenus() {
            // TODO:vgrechka ...
//            canvasContextMenu.hide()
//            boxContextMenu.hide()
        }

        fun selectTreeItem(treeItem: TreeItem<out Any>) {
            @Suppress("UNCHECKED_CAST")
            navigationTreeView.selectionModel.select(treeItem as FuckingTreeItem)
        }

        fun handleTreeSelectionChanged(oldItem: FuckingTreeItem?, newItem: FuckingTreeItem?) {
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

            val node = newItem.value as PussyNode
            val type = node.type
            if (type == BotinokNodeType.POINTER) {
                val pileTextArea = TextArea()
                leftSplitPane.items += pileTextArea
                leftSplitPane.setDividerPosition(0, 0.8)
                pileTextArea.text = node.ipointer.entity.pile
                pileTextArea.textProperty().addListener {_, _, newValue->
                    node.ipointer.entity.pile = newValue
                    dirty = true
                }
            }

            drawArena()
        }

        fun drawArena() {
            val gc = canvas.graphicsContext2D
            gc.drawImage(displayedArenaNode().image, 0.0, 0.0)

            for (iregion in displayedArenaNode().regionNodes) {
                val darkPaint = Color(0.5, 0.0, 0.0, 1.0)
                val brightPaint = Color(1.0, 0.0, 0.0, 1.0)

                val isFocused = selectedRegionNode() === iregion

                gc.stroke = when {
                    isFocused -> darkPaint
                    else -> brightPaint
                }
                val b = BotinokStuff
                gc.lineWidth = b.boxEdgeSize
                gc.strokeRect(iregion.entity.x.toDouble() - b.boxEdgeSize / 2,
                              iregion.entity.y.toDouble() - b.boxEdgeSize / 2,
                              iregion.entity.w.toDouble() + b.boxEdgeSize,
                              iregion.entity.h.toDouble() + b.boxEdgeSize)

                if (isFocused) {
                    for (handle in RegionHandle.values()) {
                        gc.fill = when {
                            handle in selectedRegionHandles -> brightPaint
                            else -> darkPaint
                        }
                        val rect = handle.rectForRegion(iregion.entity)
                        gc.fillRect(rect.minX, rect.minY, rect.width, rect.height)
                    }
                }
            }

            for (ipointer in displayedArenaNode().pointerNodes) {
                val isFocused = selectedPointerNode() === ipointer

                val w = pointerWidth.toDouble()
                val h = pointerHeight.toDouble()
                val x0 = ipointer.entity.x.toDouble()
                val y0 = ipointer.entity.y.toDouble()
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
                val arenaNode = displayedArenaNode()
                val xywh = initialRegionLocations[nextInitialRegionLocationIndex]
                if (++nextInitialRegionLocationIndex > initialRegionLocations.lastIndex)
                    nextInitialRegionLocationIndex = 0
                val newRegion = newBotinokRegion(name = "Region ${arenaNode.entity.regions.size + 1}",
                                                 x = xywh.x, y = xywh.y,
                                                 w = xywh.w, h = xywh.h,
                                                 arena = arenaNode.entity,
                                                 position = arenaNode.entity.regions.size)
                arenaNode.entity.regions.add(newRegion)
                dirty = true
                val regionNode = newRegionNode(newRegion, arenaNode.node)
                arenaNode.node.treeItem.expandedProperty().set(true)

                navigationTreeView.selectionModel.clearSelection()
                navigationTreeView.selectionModel.select(regionNode.treeItem)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun action_newPointer() {
            // TODO:vgrechka Dedupe
            try {
                val arenaNode = displayedArenaNode()
                val newPointer = newBotinokPointer(name = "Pointer ${arenaNode.entity.pointers.size + 1}",
                                                   x = 50, y = 50,
                                                   pile = "{}",
                                                   language = "JavaScript",
                                                   script = "// Fuck you",
                                                   arena = arenaNode.entity,
                                                   position = arenaNode.entity.pointers.size)
                arenaNode.entity.pointers.add(newPointer)
                dirty = true
                val pointerNode = newPointerNode(newPointer, arenaNode.node)
                arenaNode.node.treeItem.expandedProperty().set(true)

                navigationTreeView.selectionModel.clearSelection()
                navigationTreeView.selectionModel.select(pointerNode.treeItem)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun displayedArenaNode(): PussyNode.IArena {
            val treeItem = selectedTreeItem()!!
            val node = treeItem.value as PussyNode
            return when (node.type) {
                BotinokNodeType.ARENA -> node.iarena
                BotinokNodeType.REGION -> node.parent!!.iarena
                BotinokNodeType.POINTER -> node.parent!!.iarena
                BotinokNodeType.PLAY -> wtf("09eb5e83-448b-4b27-98fd-0ba002db4275")
            }
        }

        fun selectedRegionNode(): PussyNode.IRegion? {
            return selectedTreeItem()?.regionNode
        }

        fun selectedPointerNode(): PussyNode.IPointer? {
            return selectedTreeItem()?.pointerNode
        }

        val FuckingTreeItem.regionNode: PussyNode.IRegion? get() {
            val node = this.value as PussyNode?
            if (node != null)
                if (node.type == BotinokNodeType.REGION)
                    return node.iregion
            return null
        }

        val FuckingTreeItem.pointerNode: PussyNode.IPointer? get() {
            val node = this.value as PussyNode?
            if (node != null)
                if (node.type == BotinokNodeType.POINTER)
                    return node.ipointer
            return null
        }

        fun selectedTreeItem(): FuckingTreeItem? =
            navigationTreeView.selectionModel.selectedItem
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

//        fun fuck3() {
//            afterPlayEditorOpened = {
//                val pointerTreeItem = bananas.selectedArenaNode().treeItem.children
//                    .find {it.asOfEntityType<BotinokPointer>() != null}
//                    ?: wtf("228ade45-63d1-4aab-966d-2a8ddc0fedec")
//                bananas.navigationTreeView.selectionModel.select(pointerTreeItem as FuckingTreeItem)
//            }
//            Thread.sleep(500)
//            JFXStuff.later {handleEnterKeyInPlaySelector()}
//        }

//        fun fuck2() {
//            Thread.sleep(500)
//            JFXStuff.later {handleEnterKeyInPlaySelector()}
//            Thread.sleep(500)
//            action_takeScreenshot()
//            Thread.sleep(500)
//            JFXStuff.later {bananas.action_newRegion()}
//            Thread.sleep(500)
//            JFXStuff.later {bananas.action_newRegion()}
//        }
    }

    private fun action_takeScreenshot() {
        stopRequested = true
        JFXStuff.later {
            run {
                val image = robot.createScreenCapture(Rectangle(getDefaultToolkit().screenSize))
                ImageIO.write(image, "png", File(tmpImgPath))
                noise("Saved screenshot")
            }

            val play = bananas.playNode.iplay.entity
            val arena = newBotinokArena(name = "Arena ${getArenaCount() + 1}",
                                        screenshot = File(tmpImgPath).readBytes(),
                                        play = play,
                                        position = play.arenas.size)
            play.arenas.add(arena)
            dirty = true
            newArenaNode(arena, bananas.playNode)
            bananas.navigationTreeView.scrollTo(bananas.playNode.treeItem.children.lastIndex)
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
        bananas.navigationTreeView.selectionModel.clearSelection()
        bananas.navigationTreeView.selectionModel.selectLast()
    }

    fun getArenaCount(): Int {
        return bananas.playNode.treeItem.children.size
    }

    fun action_save() {
        backPlatform.tx {
            val playNode = bananas.playNode
            var swappedEntities = 0

            playNode.entity = botinokPlayRepo.save(playNode.iplay.entity)
            ++swappedEntities

            var arenaIndex = 0
            for (arenaNode in playNode.childNodes) {
                check(arenaNode.type == BotinokNodeType.ARENA)

                arenaNode.entity = playNode.iplay.entity.arenas[arenaIndex++]
                ++swappedEntities
                for ((index, iregion) in arenaNode.iarena.regionNodes.withIndex()) {
                    iregion.node.entity = arenaNode.iarena.entity.regions[index]
                    ++swappedEntities
                }
                for ((index, ipointer) in arenaNode.iarena.pointerNodes.withIndex()) {
                    ipointer.node.entity = arenaNode.iarena.entity.pointers[index]
                    ++swappedEntities
                }
            }

            noise("swappedEntities = $swappedEntities")
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
            arenas@for (arena in bananas.playNode.iplay.entity.arenas) {
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
        return bananas.playNode.iplay.entity.arenas[arenaIndex].pointers[pointerIndex]
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










