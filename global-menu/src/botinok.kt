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
import org.hibernate.cfg.Environment
import org.hibernate.dialect.SQLiteDialect
import org.jnativehook.keyboard.NativeKeyEvent
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.support.TransactionTemplate
import org.sqlite.javax.SQLiteConnectionPoolDataSource
import vgrechka.*
import vgrechka.db.*
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
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource
import kotlin.concurrent.thread
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible


internal class BotinokGlobalMenuConfig : GlobalMenuConfig() {
    override val initialFaceIndex = 1

    init {
        backPlatform.springctx = AnnotationConfigApplicationContext(BotinokAppConfig::class.java)
    }

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
    private var image by notNull<Image>()
    private var newBoxX by notNull<Int>()
    private var newBoxY by notNull<Int>()
    private var canvasContextMenu by notNull<ContextMenu>()
    private var boxContextMenu by notNull<ContextMenu>()
    private var play by notNull<Play>()
    private var canvas by notNull<Canvas>()
    private var arenaListView by notNull<ListView<Arena>>()


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

        stackPane = StackPane()
        val scrollPane = ScrollPane()
        scrollPane.content = stackPane
        val splitPane = SplitPane()

        initArenaListView()

        splitPane.items += arenaListView
        splitPane.items += scrollPane
        splitPane.setDividerPosition(0, 0.2)
        vbox.children += splitPane
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

    private fun action_save() {
        JFXStuff.infoAlert("Fuck you")
    }

    private fun action_fuckAround1() {
        action_deleteArena()
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

    override fun onBeforeDeiconified() {
        val image = Robot().createScreenCapture(Rectangle(getDefaultToolkit().screenSize))
        ImageIO.write(image, "png", File(tmpImgPath))
    }

    fun noise(s: String) {
        if (false) {
            clog(s)
        }
    }

    override fun onDeiconified() {
        GlobalMenuPile.resizePrimaryStage(1000, 500)
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
            gc.lineWidth = boxEdgeSize
            gc.strokeRect(box.x.toDouble() - boxEdgeSize / 2, box.y.toDouble() - boxEdgeSize / 2, box.w.toDouble() + boxEdgeSize, box.h.toDouble() + boxEdgeSize)

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

    private fun selectedArenaBang() = play.editing.selectedArena!!

    fun printState() {
//        noise("selectedBox = ${if (selectedBox != null) "<something>" else "null"}"
//                 + "; selectedHandles = $selectedHandles"
//                 + "; selectionMode = $selectionMode")
    }
}

@Suppress("unused")
@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
@ComponentScan(basePackages = arrayOf("vgrechka.botinok"))
open class BotinokAppConfig {
    @Bean open fun entityManagerFactory(dataSource: DataSource) = LocalContainerEntityManagerFactoryBean()-{o->
        o.jpaVendorAdapter = HibernateJpaVendorAdapter()-{o->
            o.setShowSql(true)
        }
//        o.jpaPropertyMap.put(Environment.HBM2DDL_AUTO, "create-drop")
        o.jpaPropertyMap.put(Environment.DIALECT, SQLiteDialect::class.qualifiedName)
        o.jpaPropertyMap.put(Environment.IMPLICIT_NAMING_STRATEGY, NiceHibernateNamingStrategy::class.qualifiedName)
        o.setPackagesToScan("vgrechka.botinok")
        o.dataSource = dataSource
    }

    @Bean open fun dataSource(): DataSource {
        return SQLiteConnectionPoolDataSource()-{o->
            o.url = BigPile.localSQLiteShebangDBURL
        }
    }

    @Bean open fun transactionManager(emf: EntityManagerFactory) = JpaTransactionManager()-{o->
        o.entityManagerFactory = emf
    }
}






















