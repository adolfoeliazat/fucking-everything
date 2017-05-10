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
        override fun rectForRegion(box: EntityNode<BotinokRegion>) = Rectangle2D(box.entity.x.toDouble() - b.obscureConst2 + b.obscureConst1 - b.handleSize + b.boxEdgeSize, box.entity.y.toDouble() - b.obscureConst2 - b.obscureConst1, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.TOP, DragMutator.LEFT)
    },
    TOP {
        override fun rectForRegion(box: EntityNode<BotinokRegion>) = Rectangle2D(box.entity.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.entity.w / 2 - b.handleSize / 2, box.entity.y.toDouble() - b.obscureConst2 - b.obscureConst1, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.TOP)
    },
    TOP_RIGHT {
        override fun rectForRegion(box: EntityNode<BotinokRegion>) = Rectangle2D(box.entity.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.entity.w, box.entity.y.toDouble() - b.obscureConst2 - b.obscureConst1, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.TOP, DragMutator.RIGHT)
    },
    RIGHT {
        override fun rectForRegion(box: EntityNode<BotinokRegion>) = Rectangle2D(box.entity.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.entity.w, box.entity.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.entity.h / 2 + b.handleSize / 2, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.RIGHT)
    },
    BOTTOM_RIGHT {
        override fun rectForRegion(box: EntityNode<BotinokRegion>) = Rectangle2D(box.entity.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.entity.w, box.entity.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.entity.h + b.handleSize, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.BOTTOM, DragMutator.RIGHT)
    },
    BOTTOM {
        override fun rectForRegion(box: EntityNode<BotinokRegion>) = Rectangle2D(box.entity.x.toDouble() - b.obscureConst2 + b.obscureConst1 + b.boxEdgeSize + box.entity.w / 2 - b.handleSize / 2, box.entity.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.entity.h + b.handleSize, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.BOTTOM)
    },
    BOTTOM_LEFT {
        override fun rectForRegion(box: EntityNode<BotinokRegion>) = Rectangle2D(box.entity.x.toDouble() - b.obscureConst2 + b.obscureConst1 - b.handleSize + b.boxEdgeSize, box.entity.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.entity.h + b.handleSize, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.BOTTOM, DragMutator.LEFT)
    },
    LEFT {
        override fun rectForRegion(box: EntityNode<BotinokRegion>) = Rectangle2D(box.entity.x.toDouble() - b.obscureConst2 + b.obscureConst1 - b.handleSize + b.boxEdgeSize, box.entity.y.toDouble() - b.obscureConst2 - b.obscureConst1 + box.entity.h / 2 + b.handleSize / 2, b.handleSize, b.handleSize)
        override val dragMutators = setOf(DragMutator.LEFT)
    };

    companion object {
        private val b = BotinokStuff
    }

    abstract fun rectForRegion(box: EntityNode<BotinokRegion>): Rectangle2D
    abstract val dragMutators: Set<DragMutator>
}

sealed class FuckingNode

class RootNode(var play: BotinokPlay) : FuckingNode()

class EntityNode<T : Any>(
    val treeItem: TreeItem<FuckingNode>,
    var entity: T
)
    : FuckingNode()
{
    inline fun <reified U : Any> cast(): EntityNode<U> {
        check(entity is U) {"5bb21908-affd-4cad-95de-97b1e330fdc7"}
        @Suppress("UNCHECKED_CAST") return this as EntityNode<U>
    }

    override fun toString() = entityNameProperty(entity).get()
}

val EntityNode<BotinokArena>.image by AttachedComputedShit<EntityNode<BotinokArena>, Image> {
    Image(it.entity.screenshot.inputStream())
}

inline fun <reified T> Any?.ifIs(block: (T) -> Unit) {
    if (this is T)
        block(this)
}

inline fun <reified T : Any> EntityNode<BotinokArena>.entityNodesOfType(): List<EntityNode<T>> =
    treeItem.children.collect {treeItem->
        treeItem.value.ifIs<EntityNode<*>> {node->
            node.entity.ifIs<T> {
                yield(node.cast())
            }
        }
    }

inline fun <reified Entity : Any> TreeItem<FuckingNode>.entityNodeOfType(): EntityNode<Entity>? {
    val entityNode = this.value as? EntityNode<*> ?: return null
    return when {
        entityNode.entity is Entity ->
            @Suppress("UNCHECKED_CAST")
            (entityNode as EntityNode<Entity>)
        else -> null
    }
}


val EntityNode<BotinokArena>.regions: List<EntityNode<BotinokRegion>>
    get() = entityNodesOfType()

val EntityNode<BotinokArena>.pointers: List<EntityNode<BotinokPointer>>
    get() = entityNodesOfType()


fun entityNameProperty(x: Any) = when (x) {
    // XXX Explicit casts are to work around Kotlin bug
    is BotinokArena -> (x as BotinokArena)::name
    is BotinokRegion -> (x as BotinokRegion)::name
    is BotinokPointer -> (x as BotinokPointer)::name
    else -> wtf("48e1ea9b-d0da-43fa-a4f0-2ea9cf1c5c36")
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

    val play get() = (bananas.rootTreeItem.value as RootNode).play

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

            val arenas = play.arenas //.toMutableList()
            arenas.sortBy {it.position}
            for (arena in arenas) {
                val arenaTreeItem = addTreeItemForArena(arena)
                val regions = arena.regions //.toMutableList()
                regions.sortBy {it.position}
                for (region in regions) {
                    addTreeItemForEntity(region, (arenaTreeItem.value as EntityNode<*>))
                }
                val pointers = arena.pointers //.toMutableList()
                pointers.sortBy {it.position}
                for (pointer in pointers) {
                    addTreeItemForEntity(pointer, (arenaTreeItem.value as EntityNode<*>))
                }
            }

            val rootItems = bananas.rootTreeItem.children
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

    inner class goBananas2(_play: BotinokPlay) {
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
        val rootTreeItem: TreeItem<FuckingNode> = TreeItem(RootNode(_play))
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

        fun isHit(node: EntityNode<*>, testX: Double, testY: Double): Boolean {
            val e = node.entity
            return when (e) {
                is BotinokRegion -> testX >= e.x && testX <= e.x + e.w - 1 && testY >= e.y && testY <= e.y + e.h - 1
                is BotinokPointer -> testX >= e.x && testX <= e.x + pointerWidth - 1 && testY >= e.y && testY <= e.y + pointerWidth - 1
                else -> wtf("1236858b-b0ae-4b7e-a8ff-dceb04997f71")
            }
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
                    val hitRegionNode: EntityNode<BotinokRegion>? = run {
                        o@for (region in displayedArenaNode().regions) {
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
                        operationStartRegionParams = Box(hitRegionNode.entity.x, hitRegionNode.entity.y, hitRegionNode.entity.w, hitRegionNode.entity.h)
                        operationStartMouseX = e.x
                        operationStartMouseY = e.y
                        // noise("operationStartMouseX = $operationStartMouseX; operationStartMouseY = $operationStartMouseY")
                    } else {
                        val hitPointerNode: EntityNode<BotinokPointer>? = displayedArenaNode().pointers.find {
                            val p = it.entity
                            val pointerRect = Rectangle2D(p.x.toDouble(), p.y.toDouble(), pointerWidth.toDouble(), pointerHeight.toDouble())
                            pointerRect.contains(e.x, e.y)
                        }
                        if (hitPointerNode != null) {
                            selectTreeItem(hitPointerNode.treeItem)
                            operationStartRegionParams = Box(hitPointerNode.entity.x, hitPointerNode.entity.y, -123, -123)
                            operationStartMouseX = e.x
                            operationStartMouseY = e.y
                        } else {
                            selectTreeItem(displayedArenaNode().treeItem)
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
                        selectedRegion.entity.x = points.minX
                        selectedRegion.entity.y = points.minY
                        selectedRegion.entity.w = points.maxX - points.minX + 1
                        selectedRegion.entity.h = points.maxY - points.minY + 1
                        updateShit()
                    } else {
                        val selectedPointer = selectedPointerNode()
                        if (selectedPointer != null) {
                            selectedPointer.entity.x = operationStartRegionParams.x + dx
                            selectedPointer.entity.y = operationStartRegionParams.y + dy
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

            navigationTreeView.root = rootTreeItem
            navigationTreeView.root.isExpanded = true

            primaryStage.scene = Scene(vbox)
        }

        fun resetStatusLabel() {
            statusLabel.text = "Fucking around? :)"
        }


        private fun makeTreeContextMenu(e: ContextMenuEvent) {
            val treeItem = navigationTreeView.selectionModel.selectedItem
            val menu = ContextMenu()

            val entity = treeItem.maybeEntity
            when (entity) {
                is BotinokArena -> {
                    addMenuItem(menu, "Rename") {action_renameShit(treeItem)}
                    if (treeItem !== rootTreeItem.children.first())
                        addMenuItem(menu, "Move Up") {action_moveArenaIndexDelta(treeItem, -1)}
                    if (treeItem !== rootTreeItem.children.last())
                        addMenuItem(menu, "Move Down") {action_moveArenaIndexDelta(treeItem, +1)}
                    addMenuItem(menu, "New Region", this::action_newRegion)
                    addMenuItem(menu, "New Pointer", this::action_newPointer)
                    menu.items += SeparatorMenuItem()

                    addMenuItem(menu, "Delete") {
                        action_deleteShit<RootNode, BotinokArena>(
                            treeItem = treeItem,
                            getCollectionFromParentNode = {it.play.arenas},
                            getPositionProperty = {it::position})
                    }
                }

                is BotinokRegion -> {
                    addMenuItem(menu, "Delete") {
                        action_deleteShit<EntityNode<BotinokArena>, BotinokRegion>(
                            treeItem = treeItem,
                            getCollectionFromParentNode = {it.entity.regions},
                            getPositionProperty = {it::position})
                    }
                }

                is BotinokPointer -> {
                    addMenuItem(menu, "Delete") {
                        action_deleteShit<EntityNode<BotinokArena>, BotinokPointer>(
                            treeItem = treeItem,
                            getCollectionFromParentNode = {it.entity.pointers},
                            getPositionProperty = {it::position})
                    }
                }
            }

            if (menu.items.isNotEmpty()) {
                menu.show(navigationTreeView, e.screenX, e.screenY)
                currentContextMenu = menu
            }
        }

        inline fun <reified ParentNode: FuckingNode, reified Entity>
            action_deleteShit(treeItem: TreeItem<FuckingNode>,
                              getCollectionFromParentNode: (ParentNode) -> MutableList<Entity>,
                              getPositionProperty: (Entity) -> KMutableProperty0<Int>)
        {
            val parentNode = treeItem.parent.value as ParentNode
            val collection = getCollectionFromParentNode(parentNode)
            val indexToDelete = collection.indexOf(treeItem.entity as Entity)
            collection.removeAt(indexToDelete)
            for (index in indexToDelete..collection.lastIndex) {
                val position = getPositionProperty(collection[index])
                position.set(position.get() - 1)
            }
            treeItem.parent.children -= treeItem
            dirty = true
        }

        private fun action_moveArenaIndexDelta(treeItem: TreeItem<FuckingNode>, delta: Int) {
            run { // Entities
                val arena = treeItem.entity
                val index = play.arenas.indexOfOrNull(arena) ?: wtf("db8f6365-cf88-494f-8f6a-7a07b11c01f5")

                val a = play.arenas[index]
                play.arenas[index] = play.arenas[index + delta]
                play.arenas[index + delta] = a

                val p = play.arenas[index].position
                play.arenas[index].position = play.arenas[index + delta].position
                play.arenas[index + delta].position = p
            }

            run { // Tree
                val index = rootTreeItem.children.indexOfOrNull(treeItem) ?: wtf("5df53fb4-534a-49df-832d-ee31043c7f19")
                val tmp = rootTreeItem.children[index]
                rootTreeItem.children[index] = rootTreeItem.children[index + delta]
                rootTreeItem.children[index + delta] = tmp
                navigationTreeView.selectionModel.clearSelection()
                selectTreeItem(treeItem)
            }

            dirty = true
        }

        private fun action_renameShit(treeItem: TreeItem<FuckingNode>) {
            val entity = treeItem.entity
            JFXStuff.inputBox("So, name?", entityNameProperty(entity).get())?.let {
                entityNameProperty(entity).set(it)
                treeItem.value = EntityNode(treeItem, entity)
                dirty = true
            }
        }

        private fun hideContextMenus() {
            // TODO:vgrechka ...
//            canvasContextMenu.hide()
//            boxContextMenu.hide()
        }

        fun selectTreeItem(treeItem: TreeItem<out FuckingNode>) {
            @Suppress("UNCHECKED_CAST")
            navigationTreeView.selectionModel.select(treeItem as TreeItem<FuckingNode>)
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
                pileTextArea.text = pointerNode.entity.pile
                pileTextArea.textProperty().addListener {_, _, newValue->
                    pointerNode.entity.pile = newValue
                    dirty = true
                }
            }

            drawArena()
        }

        fun drawArena() {
            val gc = canvas.graphicsContext2D
            gc.drawImage(displayedArenaNode().image, 0.0, 0.0)

            for (regionNode in displayedArenaNode().regions) {
                val darkPaint = Color(0.5, 0.0, 0.0, 1.0)
                val brightPaint = Color(1.0, 0.0, 0.0, 1.0)

                val isFocused = selectedRegionNode() === regionNode

                gc.stroke = when {
                    isFocused -> darkPaint
                    else -> brightPaint
                }
                val b = BotinokStuff
                gc.lineWidth = b.boxEdgeSize
                gc.strokeRect(regionNode.entity.x.toDouble() - b.boxEdgeSize / 2, regionNode.entity.y.toDouble() - b.boxEdgeSize / 2, regionNode.entity.w.toDouble() + b.boxEdgeSize, regionNode.entity.h.toDouble() + b.boxEdgeSize)

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

            for (pointerNode in displayedArenaNode().pointers) {
                val isFocused = selectedPointerNode() === pointerNode

                val w = pointerWidth.toDouble()
                val h = pointerHeight.toDouble()
                val x0 = pointerNode.entity.x.toDouble()
                val y0 = pointerNode.entity.y.toDouble()
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
                val newRegion = newBotinokRegion(name = "Region ${arenaNode.regions.size + 1}",
                                                 x = xywh.x, y = xywh.y,
                                                 w = xywh.w, h = xywh.h,
                                                 arena = arenaNode.entity,
                                                 position = arenaNode.entity.regions.size)
                arenaNode.entity.regions.add(newRegion)
                dirty = true
                val regionTreeItem = addTreeItemForEntity(newRegion, arenaNode)
                arenaNode.treeItem.expandedProperty().set(true)

                navigationTreeView.selectionModel.clearSelection()
                navigationTreeView.selectionModel.select(regionTreeItem)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun action_newPointer() {
            // TODO:vgrechka Dedupe
            try {
                val arenaNode = displayedArenaNode()
                val newPointer = newBotinokPointer(name = "Pointer ${arenaNode.pointers.size + 1}",
                                                   x = 50, y = 50,
                                                   pile = "{}",
                                                   language = "JavaScript",
                                                   script = "// Fuck you",
                                                   arena = arenaNode.entity,
                                                   position = arenaNode.entity.pointers.size)
                arenaNode.entity.pointers.add(newPointer)
                dirty = true
                val pointerTreeItem = addTreeItemForEntity(newPointer, arenaNode)
                arenaNode.treeItem.expandedProperty().set(true)

                navigationTreeView.selectionModel.clearSelection()
                navigationTreeView.selectionModel.select(pointerTreeItem)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun displayedArenaNode(): EntityNode<BotinokArena> {
            val treeItem = selectedTreeItem()!!
            treeItem.entityNodeOfType<BotinokArena>()?.let {return it}
            treeItem.parent.entityNodeOfType<BotinokArena>()?.let {return it}
            wtf("09eb5e83-448b-4b27-98fd-0ba002db4275")
        }

        fun selectedRegionNode(): EntityNode<BotinokRegion>? {
            return selectedTreeItem()?.regionNode
        }

        fun selectedPointerNode(): EntityNode<BotinokPointer>? {
            return selectedTreeItem()?.pointerNode
        }

        val TreeItem<FuckingNode>.regionNode: EntityNode<BotinokRegion>? get() {
            return this.entityNodeOfType()
        }

        val TreeItem<FuckingNode>.pointerNode: EntityNode<BotinokPointer>? get() {
            return this.entityNodeOfType()
        }

        fun selectedTreeItem(): TreeItem<FuckingNode>? =
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

    private fun <Entity : Any> addTreeItemForEntity(entity: Entity, parentNode: EntityNode<*>): TreeItem<FuckingNode> {
        val newTreeItem = TreeItem<FuckingNode>()
        val newNode = EntityNode(newTreeItem, entity)
        newTreeItem.value = newNode
        parentNode.treeItem.children += newTreeItem
        return newTreeItem
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
//                bananas.navigationTreeView.selectionModel.select(pointerTreeItem as TreeItem<FuckingNode>)
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

            val arena = newBotinokArena(name = "Arena ${getArenaCount() + 1}",
                                        screenshot = File(tmpImgPath).readBytes(),
                                        play = play,
                                        position = play.arenas.size)
            play.arenas.add(arena)
            dirty = true
            addTreeItemForArena(arena)
            bananas.navigationTreeView.scrollTo(bananas.rootTreeItem.children.lastIndex)
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
        val arenaNode = EntityNode(treeItem, arena)
        treeItem.value = arenaNode
        bananas.rootTreeItem.children.add(treeItem)
        return treeItem
    }

    private fun selectLastTreeItem() {
        bananas.navigationTreeView.selectionModel.clearSelection()
        bananas.navigationTreeView.selectionModel.selectLast()
    }


    fun getArenaCount(): Int {
        return bananas.rootTreeItem.children.size
    }

    fun action_save() {
        backPlatform.tx {
            (bananas.rootTreeItem.value as RootNode).play = botinokPlayRepo.save(play)

            var arenaIndex = 0
            for (arenaTreeItem in bananas.rootTreeItem.children) {
                val arenaNode = arenaTreeItem.entityNodeOfType<BotinokArena>() ?: wtf("58560832-c585-443b-a8d5-e5d948f6bb85")
                arenaNode.entity = play.arenas[arenaIndex++]
                var regionIndex = 0
                var pointerIndex = 0
                for (child in arenaTreeItem.children) {
                    child.entityNodeOfType<BotinokRegion>()?.let {it.entity = arenaNode.entity.regions[regionIndex++]}
                    child.entityNodeOfType<BotinokPointer>()?.let {it.entity = arenaNode.entity.pointers[pointerIndex++]}
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


    val TreeItem<FuckingNode>.entity: Any get() {
        return (this.value as EntityNode<*>).entity
    }

    val TreeItem<FuckingNode>?.maybeEntity: Any? get() {
        return (this?.value as? EntityNode<*>)?.entity
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










