@file:GSpit(spew = DBEntitySpew::class, output = "%FE%/global-menu/gen/generated--botinok-entities.kt")
@file:GDBEntitySpewOptions(pileObject = "BotinokGeneratedDBPile")

package vgrechka.botinok

import vgrechka.spew.*

@GEntity(table = "botinok_regions")
interface BotinokRegion : GCommonEntityFields {
    @GIsOrderColumn var position: Int?
    var name: String
    var x: Int
    var y: Int
    var w: Int
    var h: Int
    @GManyToOne var arena: BotinokArena
}

interface BotinokRegionRepository : GRepository<BotinokRegion> {
}

@GEntity(table = "botinok_pointers")
interface BotinokPointer : GCommonEntityFields {
    @GIsOrderColumn var position: Int?
    var name: String
    var x: Int
    var y: Int
    var pile: String
    var language: String
    var script: String
    @GManyToOne var arena: BotinokArena
}

interface BotinokPointerRepository : GRepository<BotinokPointer> {
}

@GEntity(table = "botinok_arenas")
interface BotinokArena : GCommonEntityFields {
    @GIsOrderColumn var position: Int?
    var name: String
    var screenshot: ByteArray
    @GManyToOne var play: BotinokPlay

    @GOneToMany(mappedBy = "arena", fetch = GFetchType.EAGER)
    var regions: MutableList<BotinokRegion>

    @GOneToMany(mappedBy = "arena", fetch = GFetchType.EAGER)
    var pointers: MutableList<BotinokPointer>
}

interface BotinokArenaRepository : GRepository<BotinokArena> {
}

@GEntity(table = "botinok_plays")
interface BotinokPlay : GCommonEntityFields {
    var name: String

    @GOneToMany(mappedBy = "play", fetch = GFetchType.EAGER)
    var arenas: MutableList<BotinokArena>
}

interface BotinokPlayRepository : GRepository<BotinokPlay> {
    fun findByName(x: String): BotinokPlay?
}



