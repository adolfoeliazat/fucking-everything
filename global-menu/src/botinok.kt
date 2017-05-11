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

// TODO:vgrechka Sanity checks during model loading (e.g. positions are correct)
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

fun getNodeType(node: Any): BotinokNodeType {
    return (node as PussyNode<*, *, *>).type as BotinokNodeType
}

class NodeShit<Entity : Any>(val node: Any, val parentNodeShit: NodeShit<*>?) {
    val treeItem = FuckingTreeItem(node)
    val childNodes = mutableListOf<Any>()
    var vagina by notNull<Entity>()
    var parentNode by notNullOnce<Any>()


    init {
        treeItem.value = node
        if (parentNodeShit != null) {
            parentNodeShit.treeItem.children += treeItem
            parentNodeShit.childNodes += node
        }
    }

    val entity get() = vagina

    inline fun <reified T> childrenOfType(type: BotinokNodeType): List<T> {
        return childNodes.collect<Any, Any> {if (getNodeType(it) == type) yield(it)} as List<T>
    }
}


class PussyNode<Type: Enum<Type>, Entity : Any, Stuff : Any>(
    val type: Type,
    val nodeTitle: (PussyNode<Type, Entity, Stuff>) -> String
) : FuckingNode {
    override var shit by notNullOnce<NodeShit<Entity>>()
    var stuff by notNullOnce<Stuff>()
    override fun toString() = nodeTitle(this)
}

fun <Type: Enum<Type>, Entity : Any, Stuff : Any>
    nodeFactory(type: Type,
                makeStuff: (NodeShit<Entity>) -> Stuff,
                getNodeTitle: (PussyNode<Type, Entity, Stuff>) -> String)
    : (Entity, parentNode: PussyNode<*, *, *>?) -> PussyNode<Type, Entity, Stuff>
{
    return {entity, parentNode ->
        val node = PussyNode(type, nodeTitle = getNodeTitle)
        node.shit = NodeShit<Entity>(node, parentNodeShit = parentNode?.shit)
        node.shit.vagina = entity
        if (parentNode != null)
            node.shit.parentNode = parentNode
        node.stuff = makeStuff(node.shit)
        node
    }
}

enum class BotinokNodeType {
    PLAY, ARENA, REGION, POINTER
}

typealias PlayNode = PussyNode<BotinokNodeType, BotinokPlay, Unit>
val newPlayNode = nodeFactory<BotinokNodeType, BotinokPlay, Unit>(
    BotinokNodeType.PLAY,
    makeStuff = {Unit},
    getNodeTitle = {it.shit.vagina.name})

typealias ArenaNode = PussyNode<BotinokNodeType, BotinokArena, ArenaNodeStuff>
val newArenaNode = nodeFactory<BotinokNodeType, BotinokArena, ArenaNodeStuff>(
    BotinokNodeType.ARENA,
    makeStuff = {ArenaNodeStuff(it)},
    getNodeTitle = {it.shit.vagina.name})

class ArenaNodeStuff(val shit: NodeShit<BotinokArena>) {
    val regionNodes get() = shit.childrenOfType<RegionNode>(BotinokNodeType.REGION)
    val pointerNodes get() = shit.childrenOfType<PointerNode>(BotinokNodeType.POINTER)

    val image by lazy {
        Image(shit.vagina.screenshot.inputStream())
    }
}

typealias RegionNode = PussyNode<BotinokNodeType, BotinokRegion, Unit>
val newRegionNode = nodeFactory<BotinokNodeType, BotinokRegion, Unit>(
    BotinokNodeType.REGION,
    makeStuff = {Unit},
    getNodeTitle = {it.shit.vagina.name})

typealias PointerNode = PussyNode<BotinokNodeType, BotinokPointer, Unit>
val newPointerNode = nodeFactory<BotinokNodeType, BotinokPointer, Unit>(
    BotinokNodeType.POINTER,
    makeStuff = {Unit},
    getNodeTitle = {it.shit.vagina.name})


interface FuckingNode {
    val shit: NodeShit<*>
}

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

            val arenas = bananas.playNode.shit.entity.arenas
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

            val rootItems = bananas.playNode.shit.treeItem.children
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
        primaryStage.title = "Botinok - ${bananas.playNode.shit.entity.name}${dirty.thenElseEmpty{" *"}}"
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
        val playNode: PlayNode

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
            val canvas = Canvas(displayedArenaNode().stuff.image.width, displayedArenaNode().stuff.image.height)

            drawingAreaStackPane = StackPane()
            drawingAreaStackPane.children += canvas
            scrollPane.content = drawingAreaStackPane

            canvas.addEventHandler(MouseEvent.MOUSE_PRESSED) {e->
                if (e.button == MouseButton.PRIMARY) {
                    // noise("MOUSE_PRESSED: e.x = ${e.x}; e.y = ${e.y}")
                    hideContextMenus()
                    val hitRegionNode: RegionNode? = run {
                        o@for (regionNode in displayedArenaNode().stuff.regionNodes) {
                            for (handle in RegionHandle.values()) {
                                if (handle.rectForRegion(regionNode.shit.vagina).contains(e.x, e.y)) {
                                    selectedRegionHandles = setOf(handle)
                                    return@run regionNode
                                }
                            }
                            if (isHit(regionNode.shit.vagina, e.x, e.y)) {
                                selectedRegionHandles = RegionHandle.values().toSet()
                                return@run regionNode
                            }
                        }
                        null
                    }
                    if (hitRegionNode != null) {
                        selectTreeItem(hitRegionNode.shit.treeItem)
                        operationStartRegionParams = Box(hitRegionNode.shit.vagina.x, hitRegionNode.shit.vagina.y, hitRegionNode.shit.vagina.w, hitRegionNode.shit.vagina.h)
                        operationStartMouseX = e.x
                        operationStartMouseY = e.y
                        // noise("operationStartMouseX = $operationStartMouseX; operationStartMouseY = $operationStartMouseY")
                    } else {
                        val hitPointerNode: PointerNode? = displayedArenaNode().stuff.pointerNodes.find {
                            val p = it.shit.vagina
                            val pointerRect = Rectangle2D(p.x.toDouble(), p.y.toDouble(), pointerWidth.toDouble(), pointerHeight.toDouble())
                            pointerRect.contains(e.x, e.y)
                        }
                        if (hitPointerNode != null) {
                            selectTreeItem(hitPointerNode.shit.treeItem)
                            operationStartRegionParams = Box(hitPointerNode.shit.vagina.x, hitPointerNode.shit.vagina.y, -123, -123)
                            operationStartMouseX = e.x
                            operationStartMouseY = e.y
                        } else {
                            selectTreeItem(displayedArenaNode().shit.treeItem)
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
                        selectedRegion.shit.vagina.x = points.minX
                        selectedRegion.shit.vagina.y = points.minY
                        selectedRegion.shit.vagina.w = points.maxX - points.minX + 1
                        selectedRegion.shit.vagina.h = points.maxY - points.minY + 1
                        updateShit()
                    } else {
                        val selectedPointer = selectedPointerNode()
                        if (selectedPointer != null) {
                            selectedPointer.shit.vagina.x = operationStartRegionParams.x + dx
                            selectedPointer.shit.vagina.y = operationStartRegionParams.y + dy
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

            playNode = newPlayNode(_play, null)
            navigationTreeView.root = playNode.shit.treeItem
            navigationTreeView.root.isExpanded = true

            primaryStage.scene = Scene(vbox)
        }

        fun resetStatusLabel() {
            statusLabel.text = "Fucking around? :)"
        }


        private fun makeTreeContextMenu(e: ContextMenuEvent) {
            val treeItem = navigationTreeView.selectionModel.selectedItem
            val menu = ContextMenu()

            val node = treeItem.value as PussyNode<*, *, *>
            exhaustive=when (node.type as BotinokNodeType) {
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

        private fun addMoveMenuItems(menu: ContextMenu, node: FuckingNode) {
            // TODO:vgrechka Generalize. Currently works only for arenas

            val treeItem = node.shit.treeItem

            fun move(delta: Int) {
                run { // Entities
                    val arena = node.shit.entity as BotinokArena
                    val play = bananas.playNode.shit.entity
                    val index = play.arenas.indexOfOrNull(arena) ?: wtf("db8f6365-cf88-494f-8f6a-7a07b11c01f5")

                    val a = play.arenas[index]
                    play.arenas[index] = play.arenas[index + delta]
                    play.arenas[index + delta] = a

                    val p = play.arenas[index].position
                    play.arenas[index].position = play.arenas[index + delta].position
                    play.arenas[index + delta].position = p

                    val n = playNode.shit.childNodes[index]
                    playNode.shit.childNodes[index] = playNode.shit.childNodes[index + delta]
                    playNode.shit.childNodes[index + delta] = n
                }

                run { // Tree
                    val rootTreeItem = playNode.shit.treeItem
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

        private fun addDeleteMenuItem(menu: ContextMenu, node: FuckingNode) {
            addMenuItem(menu, "Delete") {
                val entity = node.shit.entity
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
                        is BotinokArena -> x.let<BotinokArena, KMutableProperty0<Int>> {it::position}
                        is BotinokRegion -> x.let<BotinokRegion, KMutableProperty0<Int>> {it::position}
                        is BotinokPointer -> x.let<BotinokPointer, KMutableProperty0<Int>> {it::position}
                        else -> wtf("22a2f24c-2395-4ad8-806e-70fc3f10c7ec")
                    }
                    positionProperty.set(positionProperty.get() - 1)
                }

                exhaustive=when (getNodeType(node)) {
                    BotinokNodeType.ARENA -> getChildNodes(node).remove(node)
                    BotinokNodeType.REGION -> getChildNodes(node).remove(node)
                    BotinokNodeType.POINTER -> getChildNodes(node).remove(node)
                    else -> wtf("7a057f02-5004-4eb2-b7b7-46fb331f9eae")
                }

                node.shit.treeItem.parent.children -= node.shit.treeItem

                dirty = true
            }
        }

        fun addRenameMenuItem(menu: ContextMenu, node: FuckingNode) {
            addMenuItem(menu, "Rename") {
                val entity = node.shit.entity
                val nameProperty = when (entity) {
                    // `let` here is a workaround for https://youtrack.jetbrains.com/issue/KT-17799
                    is BotinokArena -> entity.let {it::name}
                    is BotinokRegion -> entity.let {it::name}
                    is BotinokPointer -> entity.let {it::name}
                    else -> wtf("d980a3e1-ca51-43b1-aaa3-284a5ac277d1")
                }
                JFXStuff.inputBox("So, name?", nameProperty.get())?.let {
                    nameProperty.set(it)
                    val parentChildren = node.shit.treeItem.parent.children
                    val index = parentChildren.indexOf(node.shit.treeItem)
                    parentChildren.removeAt(index)
                    parentChildren.add(index, node.shit.treeItem)
                    selectTreeItem(node.shit.treeItem)
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

            val node = newItem.value
            val type = getNodeType(node)
            if (type == BotinokNodeType.POINTER) {
                val pileTextArea = TextArea()
                leftSplitPane.items += pileTextArea
                leftSplitPane.setDividerPosition(0, 0.8)
                pileTextArea.text = node.getPropChain("shit.vagina.pile") as String
                pileTextArea.textProperty().addListener {_, _, newValue->
                    node.setPropChain("shit.vagina.pile", newValue)
                    dirty = true
                }
            }

            drawArena()
        }

        fun drawArena() {
            val gc = canvas.graphicsContext2D
            gc.drawImage(displayedArenaNode().stuff.image, 0.0, 0.0)

            for (regionNode in displayedArenaNode().stuff.regionNodes) {
                val darkPaint = Color(0.5, 0.0, 0.0, 1.0)
                val brightPaint = Color(1.0, 0.0, 0.0, 1.0)

                val isFocused = selectedRegionNode() === regionNode

                gc.stroke = when {
                    isFocused -> darkPaint
                    else -> brightPaint
                }
                val b = BotinokStuff
                gc.lineWidth = b.boxEdgeSize
                gc.strokeRect(regionNode.shit.vagina.x.toDouble() - b.boxEdgeSize / 2, regionNode.shit.vagina.y.toDouble() - b.boxEdgeSize / 2, regionNode.shit.vagina.w.toDouble() + b.boxEdgeSize, regionNode.shit.vagina.h.toDouble() + b.boxEdgeSize)

                if (isFocused) {
                    for (handle in RegionHandle.values()) {
                        gc.fill = when {
                            handle in selectedRegionHandles -> brightPaint
                            else -> darkPaint
                        }
                        val rect = handle.rectForRegion(regionNode.shit.vagina)
                        gc.fillRect(rect.minX, rect.minY, rect.width, rect.height)
                    }
                }
            }

            for (pointerNode in displayedArenaNode().stuff.pointerNodes) {
                val isFocused = selectedPointerNode() === pointerNode

                val w = pointerWidth.toDouble()
                val h = pointerHeight.toDouble()
                val x0 = pointerNode.shit.vagina.x.toDouble()
                val y0 = pointerNode.shit.vagina.y.toDouble()
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
                val newRegion = newBotinokRegion(name = "Region ${arenaNode.shit.vagina.regions.size + 1}",
                                                 x = xywh.x, y = xywh.y,
                                                 w = xywh.w, h = xywh.h,
                                                 arena = arenaNode.shit.vagina,
                                                 position = arenaNode.shit.vagina.regions.size)
                arenaNode.shit.vagina.regions.add(newRegion)
                dirty = true
                val regionNode = newRegionNode(newRegion, arenaNode)
                arenaNode.shit.treeItem.expandedProperty().set(true)

                navigationTreeView.selectionModel.clearSelection()
                navigationTreeView.selectionModel.select(regionNode.shit.treeItem)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun action_newPointer() {
            // TODO:vgrechka Dedupe
            try {
                val arenaNode = displayedArenaNode()
                val newPointer = newBotinokPointer(name = "Pointer ${arenaNode.shit.vagina.pointers.size + 1}",
                                                   x = 50, y = 50,
                                                   pile = "{}",
                                                   language = "JavaScript",
                                                   script = "// Fuck you",
                                                   arena = arenaNode.shit.vagina,
                                                   position = arenaNode.shit.vagina.pointers.size)
                arenaNode.shit.vagina.pointers.add(newPointer)
                dirty = true
                val pointerNode = newPointerNode(newPointer, arenaNode)
                arenaNode.shit.treeItem.expandedProperty().set(true)

                navigationTreeView.selectionModel.clearSelection()
                navigationTreeView.selectionModel.select(pointerNode.shit.treeItem)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun displayedArenaNode(): ArenaNode {
            val treeItem = selectedTreeItem()!!
            val node = treeItem.value
            @Suppress("UNCHECKED_CAST")
            return when (getNodeType(node)) {
                BotinokNodeType.ARENA -> node as ArenaNode
                BotinokNodeType.REGION -> getParentNode(node) as ArenaNode
                BotinokNodeType.POINTER -> getParentNode(node) as ArenaNode
                BotinokNodeType.PLAY -> wtf("09eb5e83-448b-4b27-98fd-0ba002db4275")
            }
        }

        fun selectedRegionNode(): RegionNode? {
            return selectedTreeItem()?.regionNode
        }

        fun selectedPointerNode(): PointerNode? {
            return selectedTreeItem()?.pointerNode
        }

        val FuckingTreeItem.regionNode: RegionNode? get() {
            val node = this.value
            if (node != null)
                if (getNodeType(node) == BotinokNodeType.REGION)
                    @Suppress("UNCHECKED_CAST") return node as RegionNode
            return null
        }

        val FuckingTreeItem.pointerNode: PointerNode? get() {
            val node = this.value
            if (node != null)
                if (getNodeType(node) == BotinokNodeType.POINTER)
                    @Suppress("UNCHECKED_CAST") return node as PointerNode
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

            val play = bananas.playNode.shit.entity
            val arena = newBotinokArena(name = "Arena ${getArenaCount() + 1}",
                                        screenshot = File(tmpImgPath).readBytes(),
                                        play = play,
                                        position = play.arenas.size)
            play.arenas.add(arena)
            dirty = true
            newArenaNode(arena, bananas.playNode)
            bananas.navigationTreeView.scrollTo(bananas.playNode.shit.treeItem.children.lastIndex)
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
        return bananas.playNode.shit.treeItem.children.size
    }

    fun action_save() {
        backPlatform.tx {
            val playNode = bananas.playNode
            var swappedEntities = 0

            playNode.shit.vagina = botinokPlayRepo.save(playNode.shit.entity)
            ++swappedEntities

            var arenaIndex = 0
            for (arenaNode in playNode.shit.childNodes) {
                check(getNodeType(arenaNode) == BotinokNodeType.ARENA) {"1835ecc4-32e7-4603-b382-7296aa2b5b38"}
                @Suppress("UNCHECKED_CAST") (arenaNode as ArenaNode)

                arenaNode.shit.vagina = playNode.shit.entity.arenas[arenaIndex++]
                ++swappedEntities
                for ((index, regionNode) in arenaNode.stuff.regionNodes.withIndex()) {
                    regionNode.shit.vagina = arenaNode.shit.vagina.regions[index]
                    ++swappedEntities
                }
                for ((index, pointerNode) in arenaNode.stuff.pointerNodes.withIndex()) {
                    pointerNode.shit.vagina = arenaNode.shit.vagina.pointers[index]
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
            arenas@for (arena in bananas.playNode.shit.entity.arenas) {
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
        return bananas.playNode.shit.entity.arenas[arenaIndex].pointers[pointerIndex]
    }

    fun Any.getPropChain(chain: String): Any? {
        val links = chain.split(".")
        var obj = this
        for (link in links) {
            obj = obj::class.java
                .getMethod("get" + link.capitalize())
                .invoke(obj)
        }
        return obj
    }

    inline fun <reified Value> Any.setPropChain(chain: String, value: Value) {
        val links = chain.split(".")
        var obj = this
        for (link in links.dropLast(1)) {
            obj = obj::class.java
                .getMethod("get" + link.capitalize())
                .invoke(obj)
        }
        obj::class.java
            .getMethod("set" + links.last().capitalize(), Value::class.java)
            .invoke(value)
    }

    fun getChildNodes(node: Any): MutableList<*> {
        val parentNode = getParentNode(node)
        val parentNodeShit = parentNode!!::class.java.getMethod("getShit").invoke(parentNode)
        val parentNodeChildNodes = parentNodeShit::class.java.getMethod("getChildNodes").invoke(parentNodeShit)
        return parentNodeChildNodes as MutableList<*>
    }

    fun getParentNode(node: Any): Any? {
        val shit = node::class.java.getMethod("getShit").invoke(node)
        val parentNode = shit::class.java.getMethod("getParentNode").invoke(shit)
        return parentNode
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










