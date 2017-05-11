package vgrechka.botinok

import javafx.scene.control.TreeItem
import javafx.scene.image.Image
import vgrechka.*
import kotlin.reflect.KMutableProperty0

object BotinokPile {
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

enum class BotinokNodeType {
    PLAY, ARENA, REGION, POINTER
}

class PussyNode(
    val type: BotinokNodeType,
    val parent: PussyNode?,
    var entity: Any
) {
    val treeItem = TreeItem<Any>(this)
    val childNodes = mutableListOf<PussyNode>()

    init {
        treeItem.value = this
        if (parent != null) {
            parent.treeItem.children += treeItem
            parent.childNodes += this
        }
    }

    override fun toString() = // Used by TreeView to display node title
        BotinokPile.entityNameProperty(entity).get()

    val iplay by lazy {IPlay()} // TODO:vgrechka Add check?
    inner class IPlay {
        val entity get() = this@PussyNode.entity as BotinokPlay
        val arenas get() = entity.arenas
    }

    val iarena by lazy {IArena()}
    inner class IArena {
        val node = this@PussyNode
        val entity get() = node.entity as BotinokArena
        val image by lazy {Image(entity.screenshot.inputStream())}

        val regionNodes: List<IRegion>
            get() = childNodes.collect {if (it.type == BotinokNodeType.REGION) yield(it.iregion)}

        val pointerNodes: List<IPointer>
            get() = childNodes.collect {if (it.type == BotinokNodeType.POINTER) yield(it.ipointer)}
    }

    val iregion by lazy {IRegion()}
    inner class IRegion {
        val node = this@PussyNode
        val entity get() = node.entity as BotinokRegion
    }

    val ipointer by lazy {IPointer()}
    inner class IPointer {
        val node = this@PussyNode
        val entity get() = node.entity as BotinokPointer
    }
}

fun newPlayNode(play: BotinokPlay): PussyNode {
    return PussyNode(type = BotinokNodeType.PLAY,
                     parent = null,
                     entity = play)
}

fun newArenaNode(arena: BotinokArena, playNode: PussyNode): PussyNode {
    check(playNode.type == BotinokNodeType.PLAY)
    return PussyNode(type = BotinokNodeType.ARENA,
                     parent = playNode,
                     entity = arena)
}

fun newRegionNode(region: BotinokRegion, arenaNode: PussyNode): PussyNode {
    check(arenaNode.type == BotinokNodeType.ARENA)
    return PussyNode(type = BotinokNodeType.REGION,
                     parent = arenaNode,
                     entity = region)
}

fun newPointerNode(pointer: BotinokPointer, arenaNode: PussyNode): PussyNode {
    check(arenaNode.type == BotinokNodeType.ARENA)
    return PussyNode(type = BotinokNodeType.POINTER,
                     parent = arenaNode,
                     entity = pointer)
}











