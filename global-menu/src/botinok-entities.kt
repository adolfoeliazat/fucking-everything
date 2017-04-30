@file:GSpit(spew = DBEntitySpew::class, output = "%FE%/global-menu/gen/generated--botinok-entities.kt")
@file:GDBEntitySpewOptions(stuffObject = "BotinokGeneratedDBStuff")

package vgrechka.botinok

import javafx.collections.FXCollections
import vgrechka.*
import vgrechka.spew.*


@GEntity(table = "botinok_boxes")
interface BotinokBox : GCommonEntityFields {
    var x: Int
    var y: Int
    var w: Int
    var h: Int
    @GManyToOne var arena: BotinokArena
}

interface BotinokBoxRepository : GRepository<BotinokBox> {
}

@GEntity(table = "botinok_arenas")
interface BotinokArena : GCommonEntityFields {
    var name: String
    @GOneToMany(mappedBy = "arena") var boxes: MutableList<BotinokBox>
}

interface BotinokArenaRepository : GRepository<BotinokArena> {
}

@GEntity(table = "botinok_plays")
interface BotinokPlay : GCommonEntityFields {
    var name: String
}

interface BotinokPlayRepository : GRepository<BotinokPlay> {
    fun findByName(x: String): BotinokPlay?
}


class Play {
    val arenas = FXCollections.observableArrayList<Arena>(JFXPropertyObservableExtractor())
    @Transient val editing = PlayEditing()
}

class PlayEditing {
    var selectedArena by JFXProperty<Arena?>(null)
//        var selectedArena: Arena? = null
}

class Arena {
    var title by JFXProperty("Unfuckingtitled")

    val boxes = mutableListOf<Box>()
    @Transient val editing = ArenaEditing()

    override fun toString() = title
}

class ArenaEditing {
    var selectedBox: Box? = null
}

data class Box(var x: Int = 0, var y: Int = 0, var w: Int = 0, var h: Int = 0) {
    fun isHit(testX: Double, testY: Double) =
        testX >= x && testX <= x + w - 1 && testY >= y && testY <= y + h - 1
}









