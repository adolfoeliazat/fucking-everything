package vgrechka.botinok

import javafx.geometry.Rectangle2D
import javafx.scene.control.TreeItem
import javafx.scene.image.Image
import vgrechka.*
import kotlin.reflect.KMutableProperty0

object BotinokPile {
    val boxEdgeSize = 5.0
    val handleSize = boxEdgeSize * 2
    val obscureConst1 = (handleSize - boxEdgeSize) / 2
    val obscureConst2 = boxEdgeSize + obscureConst1

    fun entityNameProperty(entity: Any): KMutableProperty0<String> {
        val nameProperty = when (entity) {
            // `let` here is a workaround for https://youtrack.jetbrains.com/issue/KT-17799
            is BotinokArena -> entity.let {it::name}
            is BotinokRegion -> entity.let {it::name}
            is BotinokPointer -> entity.let {it::name}
            else -> wtf("d980a3e1-ca51-43b1-aaa3-284a5ac277d1")
        }
        return nameProperty
    }
}

typealias BotinokTreeItem = TreeItem<Any>

class PussyNode(
    val type: Type,
    var entity: Any,
    val parent: PussyNode?
) {
    val treeItem = TreeItem<Any>(this)
    val childNodes = mutableListOf<PussyNode>()

    enum class Type {
        PLAY, ARENA, REGION, POINTER
    }

    init {
        treeItem.value = this
        if (parent != null) {
            parent.treeItem.children += treeItem
            parent.childNodes += this
        }
    }

    companion object {
        fun newPlay(play: BotinokPlay) = PussyNode(
            PussyNode.Type.PLAY, play, null)

        fun newArena(arena: BotinokArena, parent: PussyNode.IPlay) = PussyNode(
            PussyNode.Type.ARENA, arena, parent.node)

        fun newRegion(region: BotinokRegion, parent: PussyNode.IArena) = PussyNode(
            PussyNode.Type.REGION, region, parent.node)

        fun newPointer(pointer: BotinokPointer, parent: PussyNode.IArena) = PussyNode(
            PussyNode.Type.POINTER, pointer, parent.node)
    }

    override fun toString() = // Used by TreeView to display node title
        BotinokPile.entityNameProperty(entity).get()

    val iplay by lazy {check(type == Type.PLAY); IPlay()}
    inner class IPlay {
        val node = this@PussyNode
        val entity get() = node.entity as BotinokPlay
        val arenas get() = entity.arenas
    }

    val iarena by lazy {check(type == Type.ARENA); IArena()}
    inner class IArena {
        val node = this@PussyNode
        val entity get() = node.entity as BotinokArena
        val image by lazy {Image(entity.screenshot.inputStream())}

        val regionNodes: List<IRegion>
            get() = childNodes.collect {if (it.type == Type.REGION) yield(it.iregion)}

        val pointerNodes: List<IPointer>
            get() = childNodes.collect {if (it.type == Type.POINTER) yield(it.ipointer)}
    }

    val iregion by lazy {check(type == Type.REGION); IRegion()}
    inner class IRegion {
        val node = this@PussyNode
        val entity get() = node.entity as BotinokRegion
    }

    val ipointer by lazy {check(type == Type.POINTER); IPointer()}
    inner class IPointer {
        val node = this@PussyNode
        val entity get() = node.entity as BotinokPointer
    }
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
        private val b = BotinokPile
    }

    abstract fun rectForRegion(r: BotinokRegion): Rectangle2D
    abstract val dragMutators: Set<DragMutator>
}








