@file:GSpit(spew = DBEntitySpew::class, output = "%FE%/global-menu/gen/generated--botinok-entities.kt")
@file:GDBEntitySpewOptions(stuffObject = "BotinokGeneratedDBStuff")

package vgrechka.botinok

import javafx.collections.FXCollections
import vgrechka.*
import vgrechka.spew.*


@GEntity(table = "botinok_regions")
interface BotinokRegion : GCommonEntityFields {
    var name: String
    var x: Int
    var y: Int
    var w: Int
    var h: Int
    @GManyToOne var arena: BotinokArena
}

interface BotinokRegionRepository : GRepository<BotinokRegion> {
}

@GEntity(table = "botinok_arenas")
interface BotinokArena : GCommonEntityFields {
    var name: String
    var screenshot: ByteArray
    @GManyToOne var play: BotinokPlay
    @GOneToMany(mappedBy = "arena", fetch = GFetchType.EAGER) var regions: MutableList<BotinokRegion>
}

interface BotinokArenaRepository : GRepository<BotinokArena> {
}

@GEntity(table = "botinok_plays")
interface BotinokPlay : GCommonEntityFields {
    var name: String
    @GOneToMany(mappedBy = "play", fetch = GFetchType.EAGER) var arenas: MutableList<BotinokArena>
}

interface BotinokPlayRepository : GRepository<BotinokPlay> {
    fun findByName(x: String): BotinokPlay?
}


// ---------------------------------------------------------------------

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









