/*
 * (C) Copyright 2017 Vladimir Grechka
 *
 * YOU DON'T MESS AROUND WITH THIS SHIT, IT WAS GENERATED BY A TOOL SMARTER THAN YOU
 */

//
// Generated on Sun Apr 30 16:00:36 EEST 2017
// Model: e:/fegh/global-menu/src/botinok-entities.kt
//

package vgrechka.botinok

import kotlin.reflect.KClass
import vgrechka.*
import vgrechka.spew.*
import vgrechka.db.*

// ------------------------------------------------------------------
// BotinokBox
// ------------------------------------------------------------------

fun newBotinokBox(x: Int,
                  y: Int,
                  w: Int,
                  h: Int,
                  arena: BotinokArena): BotinokBox {
    val backing = Generated_BotinokBox(
        Generated_BotinokBoxFields(x = x,
                                   y = y,
                                   w = w,
                                   h = h,
                                   arena = arena._backing))
    return backing.toManuallyDefinedInterface()
}

val BotinokBox._backing
    get() = (this as Generated_BotinokBoxBackingProvider)._backing

val botinokBoxRepo: BotinokBoxRepository by lazy {
    val generatedRepo = backPlatform.springctx.getBean(Generated_BotinokBoxRepository::class.java)!!

    object:BotinokBoxRepository {
        override fun findAll(): List<BotinokBox> {
            val shit = generatedRepo.findAll()
            return shit.map {it.toManuallyDefinedInterface()}
        }

        override fun save(x: BotinokBox): BotinokBox {
            val shit = generatedRepo.save(x._backing)
            return shit.toManuallyDefinedInterface()
        }

        override fun delete(id: Long) {
            generatedRepo.delete(id)
        }

        override fun delete(x : BotinokBox) {
            generatedRepo.delete(x._backing)
        }

    }
}

interface Generated_BotinokBoxRepository : XCrudRepository<Generated_BotinokBox, Long> {
}

interface Generated_BotinokBoxBackingProvider : DBCodeGenUtils.GeneratedBackingEntityProvider<Generated_BotinokBox> {
    override val _backing: Generated_BotinokBox
}

@XEntity @XTable(name = "botinok_boxes")
class Generated_BotinokBox(
    @XEmbedded var botinokBox: Generated_BotinokBoxFields
)
    : ClitoralEntity0(), DBCodeGenUtils.GeneratedEntity<BotinokBox>
{
    override fun toManuallyDefinedInterface(): BotinokBox {
        return object : BotinokBox, Generated_BotinokBoxBackingProvider {
            override val _backing: Generated_BotinokBox
                get() = this@Generated_BotinokBox

            override var id: Long
                get() = _backing.id!!
                set(value) {_backing.id = value}

            override var createdAt: XTimestamp
                get() = _backing.botinokBox.common.createdAt
                set(value) {_backing.botinokBox.common.createdAt = value}

            override var updatedAt: XTimestamp
                get() = _backing.botinokBox.common.updatedAt
                set(value) {_backing.botinokBox.common.updatedAt = value}

            override var deleted: Boolean
                get() = _backing.botinokBox.common.deleted
                set(value) {_backing.botinokBox.common.deleted = value}

            override var x: Int
                get() = _backing.botinokBox.x
                set(value) {_backing.botinokBox.x = value}

            override var y: Int
                get() = _backing.botinokBox.y
                set(value) {_backing.botinokBox.y = value}

            override var w: Int
                get() = _backing.botinokBox.w
                set(value) {_backing.botinokBox.w = value}

            override var h: Int
                get() = _backing.botinokBox.h
                set(value) {_backing.botinokBox.h = value}

            override var arena: BotinokArena
                get() = _backing.botinokBox.arena.toManuallyDefinedInterface()
                set(value) {_backing.botinokBox.arena = value._backing}

            override fun toString() = _backing.toString()

            override fun hashCode() = _backing.hashCode()

            override fun equals(other: Any?): Boolean {
                val otherShit = other as? Generated_BotinokBoxBackingProvider ?: return false
                return _backing == otherShit._backing
            }
        }
    }

    override fun toString(): String {
        return "BotinokBox(x=${botinokBox.x}, y=${botinokBox.y}, w=${botinokBox.w}, h=${botinokBox.h})"
    }
}

@XEmbeddable
class Generated_BotinokBoxFields(
    @XEmbedded var common: CommonFields = CommonFields(),
    @XColumn var x: Int,
    @XColumn var y: Int,
    @XColumn var w: Int,
    @XColumn var h: Int,
    @XManyToOne(fetch = XFetchType.LAZY) var arena: Generated_BotinokArena
)

// ------------------------------------------------------------------
// BotinokArena
// ------------------------------------------------------------------

fun newBotinokArena(name: String): BotinokArena {
    val backing = Generated_BotinokArena(
        Generated_BotinokArenaFields(name = name))
    return backing.toManuallyDefinedInterface()
}

val BotinokArena._backing
    get() = (this as Generated_BotinokArenaBackingProvider)._backing

val botinokArenaRepo: BotinokArenaRepository by lazy {
    val generatedRepo = backPlatform.springctx.getBean(Generated_BotinokArenaRepository::class.java)!!

    object:BotinokArenaRepository {
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

            override var boxes: MutableList<BotinokBox>
                by DBCodeGenUtils.FuckingList(getBackingList = {_backing.botinokArena.boxes})

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
    @XOneToMany(fetch = XFetchType.LAZY, mappedBy = "botinokBox.arena") var boxes: MutableList<Generated_BotinokBox> = mutableListOf()
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
    @XColumn(columnDefinition = "text") var name: String
)

object BotinokGeneratedDBStuff {
    object ddl {
        val dropCreateAllScript = """
drop table if exists `botinok_boxes`;
create table `botinok_boxes` (
    `id` integer primary key autoincrement,
    `botinokBox_common_createdAt` text not null,
    `botinokBox_common_updatedAt` text not null,
    `botinokBox_common_deleted` integer not null,
    `botinokBox_x` integer not null,
    `botinokBox_y` integer not null,
    `botinokBox_w` integer not null,
    `botinokBox_h` integer not null,
    `botinokBox_arena__id` bigint not null,
    foreign key (botinokBox_arena__id) references botinok_arenas(id)
);

drop table if exists `botinok_arenas`;
create table `botinok_arenas` (
    `id` integer primary key autoincrement,
    `botinokArena_common_createdAt` text not null,
    `botinokArena_common_updatedAt` text not null,
    `botinokArena_common_deleted` integer not null,
    `botinokArena_name` text not null
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

drop table if exists `botinok_boxes`;
create table `botinok_boxes` (
    `id` integer primary key autoincrement,
    `botinokBox_common_createdAt` text not null,
    `botinokBox_common_updatedAt` text not null,
    `botinokBox_common_deleted` integer not null,
    `botinokBox_x` integer not null,
    `botinokBox_y` integer not null,
    `botinokBox_w` integer not null,
    `botinokBox_h` integer not null,
    `botinokBox_arena__id` bigint not null,
    foreign key (botinokBox_arena__id) references botinok_arenas(id)
);

drop table if exists `botinok_arenas`;
create table `botinok_arenas` (
    `id` integer primary key autoincrement,
    `botinokArena_common_createdAt` text not null,
    `botinokArena_common_updatedAt` text not null,
    `botinokArena_common_deleted` integer not null,
    `botinokArena_name` text not null
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