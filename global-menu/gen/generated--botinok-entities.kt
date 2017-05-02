/*
 * (C) Copyright 2017 Vladimir Grechka
 *
 * YOU DON'T MESS AROUND WITH THIS SHIT, IT WAS GENERATED BY A TOOL SMARTER THAN YOU
 */

//
// Generated on Tue May 02 16:56:23 EEST 2017
// Model: e:/fegh/global-menu/src/botinok-entities.kt
//

package vgrechka.botinok

import kotlin.reflect.KClass
import vgrechka.*
import vgrechka.spew.*
import vgrechka.db.*

// ------------------------------------------------------------------
// BotinokRegion
// ------------------------------------------------------------------

fun newBotinokRegion(name: String,
                     x: Int,
                     y: Int,
                     w: Int,
                     h: Int,
                     arena: BotinokArena): BotinokRegion {
    val backing = Generated_BotinokRegion(
        Generated_BotinokRegionFields(name = name,
                                      x = x,
                                      y = y,
                                      w = w,
                                      h = h,
                                      arena = arena._backing))
    return backing.toManuallyDefinedInterface()
}

val BotinokRegion._backing
    get() = (this as Generated_BotinokRegionBackingProvider)._backing

val botinokRegionRepo: BotinokRegionRepository by lazy {
    val generatedRepo = backPlatform.springctx.getBean(Generated_BotinokRegionRepository::class.java)!!

    object:BotinokRegionRepository {
        override fun findOne(id: Long): BotinokRegion? {
            val shit = generatedRepo.findOne(id)
            return shit?.toManuallyDefinedInterface()
        }

        override fun findAll(): List<BotinokRegion> {
            val shit = generatedRepo.findAll()
            return shit.map {it.toManuallyDefinedInterface()}
        }

        override fun save(x: BotinokRegion): BotinokRegion {
            val shit = generatedRepo.save(x._backing)
            return shit.toManuallyDefinedInterface()
        }

        override fun delete(id: Long) {
            generatedRepo.delete(id)
        }

        override fun delete(x : BotinokRegion) {
            generatedRepo.delete(x._backing)
        }

    }
}

interface Generated_BotinokRegionRepository : XCrudRepository<Generated_BotinokRegion, Long> {
}

interface Generated_BotinokRegionBackingProvider : DBCodeGenUtils.GeneratedBackingEntityProvider<Generated_BotinokRegion> {
    override val _backing: Generated_BotinokRegion
}

@XEntity @XTable(name = "botinok_regions")
class Generated_BotinokRegion(
    @XEmbedded var botinokRegion: Generated_BotinokRegionFields
)
    : ClitoralEntity0(), DBCodeGenUtils.GeneratedEntity<BotinokRegion>
{
    override fun toManuallyDefinedInterface(): BotinokRegion {
        return object : BotinokRegion, Generated_BotinokRegionBackingProvider {
            override val _backing: Generated_BotinokRegion
                get() = this@Generated_BotinokRegion

            override var id: Long
                get() = _backing.id!!
                set(value) {_backing.id = value}

            override var createdAt: XTimestamp
                get() = _backing.botinokRegion.common.createdAt
                set(value) {_backing.botinokRegion.common.createdAt = value}

            override var updatedAt: XTimestamp
                get() = _backing.botinokRegion.common.updatedAt
                set(value) {_backing.botinokRegion.common.updatedAt = value}

            override var deleted: Boolean
                get() = _backing.botinokRegion.common.deleted
                set(value) {_backing.botinokRegion.common.deleted = value}

            override var name: String
                get() = _backing.botinokRegion.name
                set(value) {_backing.botinokRegion.name = value}

            override var x: Int
                get() = _backing.botinokRegion.x
                set(value) {_backing.botinokRegion.x = value}

            override var y: Int
                get() = _backing.botinokRegion.y
                set(value) {_backing.botinokRegion.y = value}

            override var w: Int
                get() = _backing.botinokRegion.w
                set(value) {_backing.botinokRegion.w = value}

            override var h: Int
                get() = _backing.botinokRegion.h
                set(value) {_backing.botinokRegion.h = value}

            override var arena: BotinokArena
                get() = _backing.botinokRegion.arena.toManuallyDefinedInterface()
                set(value) {_backing.botinokRegion.arena = value._backing}

            override fun toString() = _backing.toString()

            override fun hashCode() = _backing.hashCode()

            override fun equals(other: Any?): Boolean {
                val otherShit = other as? Generated_BotinokRegionBackingProvider ?: return false
                return _backing == otherShit._backing
            }
        }
    }

    override fun toString(): String {
        return "BotinokRegion(name=${botinokRegion.name}, x=${botinokRegion.x}, y=${botinokRegion.y}, w=${botinokRegion.w}, h=${botinokRegion.h})"
    }
}

@XEmbeddable
class Generated_BotinokRegionFields(
    @XEmbedded var common: CommonFields = CommonFields(),
    @XColumn(columnDefinition = "text") var name: String,
    @XColumn var x: Int,
    @XColumn var y: Int,
    @XColumn var w: Int,
    @XColumn var h: Int,
    @XManyToOne(fetch = XFetchType.EAGER, cascade = arrayOf(XCascadeType.ALL)) var arena: Generated_BotinokArena
)

// ------------------------------------------------------------------
// BotinokArena
// ------------------------------------------------------------------

fun newBotinokArena(name: String,
                    play: BotinokPlay): BotinokArena {
    val backing = Generated_BotinokArena(
        Generated_BotinokArenaFields(name = name,
                                     play = play._backing))
    return backing.toManuallyDefinedInterface()
}

val BotinokArena._backing
    get() = (this as Generated_BotinokArenaBackingProvider)._backing

val botinokArenaRepo: BotinokArenaRepository by lazy {
    val generatedRepo = backPlatform.springctx.getBean(Generated_BotinokArenaRepository::class.java)!!

    object:BotinokArenaRepository {
        override fun findOne(id: Long): BotinokArena? {
            val shit = generatedRepo.findOne(id)
            return shit?.toManuallyDefinedInterface()
        }

        override fun findAll(): List<BotinokArena> {
            val shit = generatedRepo.findAll()
            return shit.map {it.toManuallyDefinedInterface()}
        }

        override fun save(x: BotinokArena): BotinokArena {
            val shit = generatedRepo.save(x._backing)
            return shit.toManuallyDefinedInterface()
        }

        override fun delete(id: Long) {
            generatedRepo.delete(id)
        }

        override fun delete(x : BotinokArena) {
            generatedRepo.delete(x._backing)
        }

    }
}

interface Generated_BotinokArenaRepository : XCrudRepository<Generated_BotinokArena, Long> {
}

interface Generated_BotinokArenaBackingProvider : DBCodeGenUtils.GeneratedBackingEntityProvider<Generated_BotinokArena> {
    override val _backing: Generated_BotinokArena
}

@XEntity @XTable(name = "botinok_arenas")
class Generated_BotinokArena(
    @XEmbedded var botinokArena: Generated_BotinokArenaFields
)
    : ClitoralEntity0(), DBCodeGenUtils.GeneratedEntity<BotinokArena>
{
    override fun toManuallyDefinedInterface(): BotinokArena {
        return object : BotinokArena, Generated_BotinokArenaBackingProvider {
            override val _backing: Generated_BotinokArena
                get() = this@Generated_BotinokArena

            override var id: Long
                get() = _backing.id!!
                set(value) {_backing.id = value}

            override var createdAt: XTimestamp
                get() = _backing.botinokArena.common.createdAt
                set(value) {_backing.botinokArena.common.createdAt = value}

            override var updatedAt: XTimestamp
                get() = _backing.botinokArena.common.updatedAt
                set(value) {_backing.botinokArena.common.updatedAt = value}

            override var deleted: Boolean
                get() = _backing.botinokArena.common.deleted
                set(value) {_backing.botinokArena.common.deleted = value}

            override var name: String
                get() = _backing.botinokArena.name
                set(value) {_backing.botinokArena.name = value}

            override var play: BotinokPlay
                get() = _backing.botinokArena.play.toManuallyDefinedInterface()
                set(value) {_backing.botinokArena.play = value._backing}

            override var regions: MutableList<BotinokRegion>
                by DBCodeGenUtils.FuckingList(getBackingList = {_backing.botinokArena.regions})

            override fun toString() = _backing.toString()

            override fun hashCode() = _backing.hashCode()

            override fun equals(other: Any?): Boolean {
                val otherShit = other as? Generated_BotinokArenaBackingProvider ?: return false
                return _backing == otherShit._backing
            }
        }
    }

    override fun toString(): String {
        return "BotinokArena(name=${botinokArena.name})"
    }
}

@XEmbeddable
class Generated_BotinokArenaFields(
    @XEmbedded var common: CommonFields = CommonFields(),
    @XColumn(columnDefinition = "text") var name: String,
    @XManyToOne(fetch = XFetchType.EAGER, cascade = arrayOf(XCascadeType.ALL)) var play: Generated_BotinokPlay,
    @XOneToMany(fetch = XFetchType.EAGER, mappedBy = "botinokRegion.arena", cascade = arrayOf(XCascadeType.ALL), orphanRemoval = true) var regions: MutableList<Generated_BotinokRegion> = mutableListOf()
)

// ------------------------------------------------------------------
// BotinokPlay
// ------------------------------------------------------------------

fun newBotinokPlay(name: String): BotinokPlay {
    val backing = Generated_BotinokPlay(
        Generated_BotinokPlayFields(name = name))
    return backing.toManuallyDefinedInterface()
}

val BotinokPlay._backing
    get() = (this as Generated_BotinokPlayBackingProvider)._backing

val botinokPlayRepo: BotinokPlayRepository by lazy {
    val generatedRepo = backPlatform.springctx.getBean(Generated_BotinokPlayRepository::class.java)!!

    object:BotinokPlayRepository {
        override fun findOne(id: Long): BotinokPlay? {
            val shit = generatedRepo.findOne(id)
            return shit?.toManuallyDefinedInterface()
        }

        override fun findAll(): List<BotinokPlay> {
            val shit = generatedRepo.findAll()
            return shit.map {it.toManuallyDefinedInterface()}
        }

        override fun save(x: BotinokPlay): BotinokPlay {
            val shit = generatedRepo.save(x._backing)
            return shit.toManuallyDefinedInterface()
        }

        override fun delete(id: Long) {
            generatedRepo.delete(id)
        }

        override fun delete(x : BotinokPlay) {
            generatedRepo.delete(x._backing)
        }

        override fun findByName(x: String): BotinokPlay? {
            val shit = generatedRepo.findByBotinokPlay_Name(x)
            return shit?.toManuallyDefinedInterface()
        }
    }
}

interface Generated_BotinokPlayRepository : XCrudRepository<Generated_BotinokPlay, Long> {
    fun findByBotinokPlay_Name(x: String): Generated_BotinokPlay?
}

interface Generated_BotinokPlayBackingProvider : DBCodeGenUtils.GeneratedBackingEntityProvider<Generated_BotinokPlay> {
    override val _backing: Generated_BotinokPlay
}

@XEntity @XTable(name = "botinok_plays")
class Generated_BotinokPlay(
    @XEmbedded var botinokPlay: Generated_BotinokPlayFields
)
    : ClitoralEntity0(), DBCodeGenUtils.GeneratedEntity<BotinokPlay>
{
    override fun toManuallyDefinedInterface(): BotinokPlay {
        return object : BotinokPlay, Generated_BotinokPlayBackingProvider {
            override val _backing: Generated_BotinokPlay
                get() = this@Generated_BotinokPlay

            override var id: Long
                get() = _backing.id!!
                set(value) {_backing.id = value}

            override var createdAt: XTimestamp
                get() = _backing.botinokPlay.common.createdAt
                set(value) {_backing.botinokPlay.common.createdAt = value}

            override var updatedAt: XTimestamp
                get() = _backing.botinokPlay.common.updatedAt
                set(value) {_backing.botinokPlay.common.updatedAt = value}

            override var deleted: Boolean
                get() = _backing.botinokPlay.common.deleted
                set(value) {_backing.botinokPlay.common.deleted = value}

            override var name: String
                get() = _backing.botinokPlay.name
                set(value) {_backing.botinokPlay.name = value}

            override var arenas: MutableList<BotinokArena>
                by DBCodeGenUtils.FuckingList(getBackingList = {_backing.botinokPlay.arenas})

            override fun toString() = _backing.toString()

            override fun hashCode() = _backing.hashCode()

            override fun equals(other: Any?): Boolean {
                val otherShit = other as? Generated_BotinokPlayBackingProvider ?: return false
                return _backing == otherShit._backing
            }
        }
    }

    override fun toString(): String {
        return "BotinokPlay(name=${botinokPlay.name})"
    }
}

@XEmbeddable
class Generated_BotinokPlayFields(
    @XEmbedded var common: CommonFields = CommonFields(),
    @XColumn(columnDefinition = "text") var name: String,
    @XOneToMany(fetch = XFetchType.EAGER, mappedBy = "botinokArena.play", cascade = arrayOf(XCascadeType.ALL), orphanRemoval = true) var arenas: MutableList<Generated_BotinokArena> = mutableListOf()
)

object BotinokGeneratedDBStuff {
    object ddl {
        val dropCreateAllScript = """
drop table if exists `botinok_regions`;
create table `botinok_regions` (
    `id` integer primary key autoincrement,
    `botinokRegion_common_createdAt` text not null,
    `botinokRegion_common_updatedAt` text not null,
    `botinokRegion_common_deleted` integer not null,
    `botinokRegion_name` text not null,
    `botinokRegion_x` integer not null,
    `botinokRegion_y` integer not null,
    `botinokRegion_w` integer not null,
    `botinokRegion_h` integer not null,
    `botinokRegion_arena__id` bigint not null,
    foreign key (botinokRegion_arena__id) references botinok_arenas(id)
);

drop table if exists `botinok_arenas`;
create table `botinok_arenas` (
    `id` integer primary key autoincrement,
    `botinokArena_common_createdAt` text not null,
    `botinokArena_common_updatedAt` text not null,
    `botinokArena_common_deleted` integer not null,
    `botinokArena_name` text not null,
    `botinokArena_play__id` bigint not null,
    foreign key (botinokArena_play__id) references botinok_plays(id)
);

drop table if exists `botinok_plays`;
create table `botinok_plays` (
    `id` integer primary key autoincrement,
    `botinokPlay_common_createdAt` text not null,
    `botinokPlay_common_updatedAt` text not null,
    `botinokPlay_common_deleted` integer not null,
    `botinokPlay_name` text not null
);

        """
    }
}


/*
DDL
===

drop table if exists `botinok_regions`;
create table `botinok_regions` (
    `id` integer primary key autoincrement,
    `botinokRegion_common_createdAt` text not null,
    `botinokRegion_common_updatedAt` text not null,
    `botinokRegion_common_deleted` integer not null,
    `botinokRegion_name` text not null,
    `botinokRegion_x` integer not null,
    `botinokRegion_y` integer not null,
    `botinokRegion_w` integer not null,
    `botinokRegion_h` integer not null,
    `botinokRegion_arena__id` bigint not null,
    foreign key (botinokRegion_arena__id) references botinok_arenas(id)
);

drop table if exists `botinok_arenas`;
create table `botinok_arenas` (
    `id` integer primary key autoincrement,
    `botinokArena_common_createdAt` text not null,
    `botinokArena_common_updatedAt` text not null,
    `botinokArena_common_deleted` integer not null,
    `botinokArena_name` text not null,
    `botinokArena_play__id` bigint not null,
    foreign key (botinokArena_play__id) references botinok_plays(id)
);

drop table if exists `botinok_plays`;
create table `botinok_plays` (
    `id` integer primary key autoincrement,
    `botinokPlay_common_createdAt` text not null,
    `botinokPlay_common_updatedAt` text not null,
    `botinokPlay_common_deleted` integer not null,
    `botinokPlay_name` text not null
);

*/