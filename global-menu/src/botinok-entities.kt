package vgrechka.botinok

import javafx.collections.FXCollections
import vgrechka.*
import vgrechka.db.*
import vgrechka.db.tests.*


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










////============================== BotinokBox ==============================
//
//interface BotinokBoxRepository : XCrudRepository<BotinokBox, Long>
//
//@XEntity @XTable(name = "botinok_box")
//data class BotinokBox(
//    @XEmbedded var common: CommonFields = CommonFields(),
//    @XColumn var x: Int = 0,
//    @XColumn var y: Int = 0,
//    @XColumn var w: Int = 0,
//    @XColumn var h: Int = 0
//) {
//    fun isHit(testX: Double, testY: Double) =
//        testX >= x && testX <= x + w - 1 && testY >= y && testY <= y + h - 1
//}
//
//
////============================== BotinokArena ==============================
//
//class Arena {
//    var title by JFXProperty("Unfuckingtitled")
//
//    val boxes = mutableListOf<BotinokBox>()
//    @Transient val editing = ArenaEditing()
//
//    override fun toString() = title
//}
//
//class ArenaEditing {
//    var selectedBox: BotinokBox? = null
//}
//
//
//
////class Arena {
////    var title by JFXProperty("Unfuckingtitled")
////
////    val boxes = mutableListOf<Box>()
////    @Transient val editing = ArenaEditing()
////
////    override fun toString() = title
////}
////
////class ArenaEditing {
////    var selectedBox: Box? = null
////}
////
//
//
////============================== BotinokPlay ==============================
//
//interface BotinokPlayRepository : XCrudRepository<BotinokPlay, Long>
//
//@XEntity @XTable(name = "botinok_plays")
//data class BotinokPlay(
//    @XEmbedded var botinokPlay: BotinokPlayFields
//) : ClitoralEntity0()
//
//@XEmbeddable
//data class BotinokPlayFields(
//    @XEmbedded var common: CommonFields = CommonFields(),
//    @XColumn(columnDefinition = "text") var word: String,
//    @XColumn var rank: Integer
//)
//
//
//
//
////class Play {
////    val arenas = FXCollections.observableArrayList<Arena>(JFXPropertyObservableExtractor())
////    @Transient val editing = PlayEditing()
////}
////
////class PlayEditing {
////    var selectedArena by JFXProperty<Arena?>(null)
//////        var selectedArena: Arena? = null
////}
////
//
//

