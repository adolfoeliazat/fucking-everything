/*
 * (C) Copyright 2017 Vladimir Grechka
 *
 * YOU DON'T MESS AROUND WITH THIS SHIT, IT WAS GENERATED BY A TOOL SMARTER THAN YOU
 */

//
// Generated on Fri May 12 09:33:02 EEST 2017
// Model: e:/fegh/global-menu/src/botinok-entities.kt
//

package vgrechka.botinok

import kotlin.reflect.KClass
import vgrechka.*
import vgrechka.spew.*
import vgrechka.db.*

// ------------------------------------------------------------------
// BotinokPlay
// ------------------------------------------------------------------

// Generated at 7-470173f-49ef-43cb-adf7-1c395f07518c
fun newBotinokPlay(name: String,
                   pile: String): BotinokPlay {
    val backing = Generated_BotinokPlay(
        Generated_BotinokPlayFields().also {it.name = name
                                            it.pile = pile})
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
class Generated_BotinokPlay( // Generated at f-21265f2-3d69-4ab8-a07c-5595106a9e6b
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

            override var pile: String
                get() = _backing.botinokPlay.pile
                set(value) {_backing.botinokPlay.pile = value}

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
        return "BotinokPlay(name=${botinokPlay.name}, pile=${botinokPlay.pile})"
    }
}

@XEmbeddable
class Generated_BotinokPlayFields { // Generated at 2-e91acff-5613-4b14-b71e-5edee254d029
    @XEmbedded var common: CommonFields = CommonFields()
    @XColumn(columnDefinition = "text") lateinit var name: String
    @XColumn(columnDefinition = "text") lateinit var pile: String
    @XOneToMany(fetch = XFetchType.LAZY, mappedBy = "botinokArena.play", cascade = arrayOf(XCascadeType.ALL), orphanRemoval = true) var arenas: MutableList<Generated_BotinokArena> = mutableListOf()
}

// ------------------------------------------------------------------
// BotinokArena
// ------------------------------------------------------------------

// Generated at 7-470173f-49ef-43cb-adf7-1c395f07518c
fun newBotinokArena(position: Int,
                    name: String,
                    screenshot: ByteArray,
                    pile: String,
                    play: BotinokPlay): BotinokArena {
    val backing = Generated_BotinokArena(
        Generated_BotinokArenaFields().also {it.position = java.lang.Integer(position)
                                             it.name = name
                                             it.screenshot = screenshot
                                             it.pile = pile
                                             it.play = play._backing})
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
class Generated_BotinokArena( // Generated at f-21265f2-3d69-4ab8-a07c-5595106a9e6b
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

            override var position: Int
                get() = _backing.botinokArena.position.toInt()
                set(value) {_backing.botinokArena.position = java.lang.Integer(value)}

            override var name: String
                get() = _backing.botinokArena.name
                set(value) {_backing.botinokArena.name = value}

            override var screenshot: ByteArray
                get() = _backing.botinokArena.screenshot
                set(value) {_backing.botinokArena.screenshot = value}

            override var pile: String
                get() = _backing.botinokArena.pile
                set(value) {_backing.botinokArena.pile = value}

            override var play: BotinokPlay
                get() = _backing.botinokArena.play.toManuallyDefinedInterface()
                set(value) {_backing.botinokArena.play = value._backing}

            override var regions: MutableList<BotinokRegion>
                by DBCodeGenUtils.FuckingList(getBackingList = {_backing.botinokArena.regions})

            override var pointers: MutableList<BotinokPointer>
                by DBCodeGenUtils.FuckingList(getBackingList = {_backing.botinokArena.pointers})

            override fun toString() = _backing.toString()

            override fun hashCode() = _backing.hashCode()

            override fun equals(other: Any?): Boolean {
                val otherShit = other as? Generated_BotinokArenaBackingProvider ?: return false
                return _backing == otherShit._backing
            }
        }
    }

    override fun toString(): String {
        return "BotinokArena(position=${botinokArena.position}, name=${botinokArena.name}, pile=${botinokArena.pile})"
    }
}

@XEmbeddable
class Generated_BotinokArenaFields { // Generated at 2-e91acff-5613-4b14-b71e-5edee254d029
    @XEmbedded var common: CommonFields = CommonFields()
    @XColumn lateinit var position: java.lang.Integer
    @XColumn(columnDefinition = "text") lateinit var name: String
    @XColumn lateinit var screenshot: ByteArray
    @XColumn(columnDefinition = "text") lateinit var pile: String
    @XManyToOne(fetch = XFetchType.EAGER/*, cascade = arrayOf(XCascadeType.ALL)*/) lateinit var play: Generated_BotinokPlay
    @XOneToMany(fetch = XFetchType.LAZY, mappedBy = "botinokRegion.arena", cascade = arrayOf(XCascadeType.ALL), orphanRemoval = true) var regions: MutableList<Generated_BotinokRegion> = mutableListOf()
    @XOneToMany(fetch = XFetchType.LAZY, mappedBy = "botinokPointer.arena", cascade = arrayOf(XCascadeType.ALL), orphanRemoval = true) var pointers: MutableList<Generated_BotinokPointer> = mutableListOf()
}

// ------------------------------------------------------------------
// BotinokRegion
// ------------------------------------------------------------------

// Generated at 7-470173f-49ef-43cb-adf7-1c395f07518c
fun newBotinokRegion(position: Int,
                     name: String,
                     x: Int,
                     y: Int,
                     w: Int,
                     h: Int,
                     pile: String,
                     arena: BotinokArena): BotinokRegion {
    val backing = Generated_BotinokRegion(
        Generated_BotinokRegionFields().also {it.position = java.lang.Integer(position)
                                              it.name = name
                                              it.x = java.lang.Integer(x)
                                              it.y = java.lang.Integer(y)
                                              it.w = java.lang.Integer(w)
                                              it.h = java.lang.Integer(h)
                                              it.pile = pile
                                              it.arena = arena._backing})
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
class Generated_BotinokRegion( // Generated at f-21265f2-3d69-4ab8-a07c-5595106a9e6b
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

            override var position: Int
                get() = _backing.botinokRegion.position.toInt()
                set(value) {_backing.botinokRegion.position = java.lang.Integer(value)}

            override var name: String
                get() = _backing.botinokRegion.name
                set(value) {_backing.botinokRegion.name = value}

            override var x: Int
                get() = _backing.botinokRegion.x.toInt()
                set(value) {_backing.botinokRegion.x = java.lang.Integer(value)}

            override var y: Int
                get() = _backing.botinokRegion.y.toInt()
                set(value) {_backing.botinokRegion.y = java.lang.Integer(value)}

            override var w: Int
                get() = _backing.botinokRegion.w.toInt()
                set(value) {_backing.botinokRegion.w = java.lang.Integer(value)}

            override var h: Int
                get() = _backing.botinokRegion.h.toInt()
                set(value) {_backing.botinokRegion.h = java.lang.Integer(value)}

            override var pile: String
                get() = _backing.botinokRegion.pile
                set(value) {_backing.botinokRegion.pile = value}

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
        return "BotinokRegion(position=${botinokRegion.position}, name=${botinokRegion.name}, x=${botinokRegion.x}, y=${botinokRegion.y}, w=${botinokRegion.w}, h=${botinokRegion.h}, pile=${botinokRegion.pile})"
    }
}

@XEmbeddable
class Generated_BotinokRegionFields { // Generated at 2-e91acff-5613-4b14-b71e-5edee254d029
    @XEmbedded var common: CommonFields = CommonFields()
    @XColumn lateinit var position: java.lang.Integer
    @XColumn(columnDefinition = "text") lateinit var name: String
    @XColumn lateinit var x: java.lang.Integer
    @XColumn lateinit var y: java.lang.Integer
    @XColumn lateinit var w: java.lang.Integer
    @XColumn lateinit var h: java.lang.Integer
    @XColumn(columnDefinition = "text") lateinit var pile: String
    @XManyToOne(fetch = XFetchType.EAGER/*, cascade = arrayOf(XCascadeType.ALL)*/) lateinit var arena: Generated_BotinokArena
}

// ------------------------------------------------------------------
// BotinokPointer
// ------------------------------------------------------------------

// Generated at 7-470173f-49ef-43cb-adf7-1c395f07518c
fun newBotinokPointer(position: Int,
                      name: String,
                      x: Int,
                      y: Int,
                      pile: String,
                      language: String,
                      script: String,
                      arena: BotinokArena): BotinokPointer {
    val backing = Generated_BotinokPointer(
        Generated_BotinokPointerFields().also {it.position = java.lang.Integer(position)
                                               it.name = name
                                               it.x = java.lang.Integer(x)
                                               it.y = java.lang.Integer(y)
                                               it.pile = pile
                                               it.language = language
                                               it.script = script
                                               it.arena = arena._backing})
    return backing.toManuallyDefinedInterface()
}

val BotinokPointer._backing
    get() = (this as Generated_BotinokPointerBackingProvider)._backing

val botinokPointerRepo: BotinokPointerRepository by lazy {
    val generatedRepo = backPlatform.springctx.getBean(Generated_BotinokPointerRepository::class.java)!!

    object:BotinokPointerRepository {
        override fun findOne(id: Long): BotinokPointer? {
            val shit = generatedRepo.findOne(id)
            return shit?.toManuallyDefinedInterface()
        }

        override fun findAll(): List<BotinokPointer> {
            val shit = generatedRepo.findAll()
            return shit.map {it.toManuallyDefinedInterface()}
        }

        override fun save(x: BotinokPointer): BotinokPointer {
            val shit = generatedRepo.save(x._backing)
            return shit.toManuallyDefinedInterface()
        }

        override fun delete(id: Long) {
            generatedRepo.delete(id)
        }

        override fun delete(x : BotinokPointer) {
            generatedRepo.delete(x._backing)
        }

    }
}

interface Generated_BotinokPointerRepository : XCrudRepository<Generated_BotinokPointer, Long> {
}

interface Generated_BotinokPointerBackingProvider : DBCodeGenUtils.GeneratedBackingEntityProvider<Generated_BotinokPointer> {
    override val _backing: Generated_BotinokPointer
}

@XEntity @XTable(name = "botinok_pointers")
class Generated_BotinokPointer( // Generated at f-21265f2-3d69-4ab8-a07c-5595106a9e6b
    @XEmbedded var botinokPointer: Generated_BotinokPointerFields
)
    : ClitoralEntity0(), DBCodeGenUtils.GeneratedEntity<BotinokPointer>
{
    override fun toManuallyDefinedInterface(): BotinokPointer {
        return object : BotinokPointer, Generated_BotinokPointerBackingProvider {
            override val _backing: Generated_BotinokPointer
                get() = this@Generated_BotinokPointer

            override var id: Long
                get() = _backing.id!!
                set(value) {_backing.id = value}

            override var createdAt: XTimestamp
                get() = _backing.botinokPointer.common.createdAt
                set(value) {_backing.botinokPointer.common.createdAt = value}

            override var updatedAt: XTimestamp
                get() = _backing.botinokPointer.common.updatedAt
                set(value) {_backing.botinokPointer.common.updatedAt = value}

            override var deleted: Boolean
                get() = _backing.botinokPointer.common.deleted
                set(value) {_backing.botinokPointer.common.deleted = value}

            override var position: Int
                get() = _backing.botinokPointer.position.toInt()
                set(value) {_backing.botinokPointer.position = java.lang.Integer(value)}

            override var name: String
                get() = _backing.botinokPointer.name
                set(value) {_backing.botinokPointer.name = value}

            override var x: Int
                get() = _backing.botinokPointer.x.toInt()
                set(value) {_backing.botinokPointer.x = java.lang.Integer(value)}

            override var y: Int
                get() = _backing.botinokPointer.y.toInt()
                set(value) {_backing.botinokPointer.y = java.lang.Integer(value)}

            override var pile: String
                get() = _backing.botinokPointer.pile
                set(value) {_backing.botinokPointer.pile = value}

            override var language: String
                get() = _backing.botinokPointer.language
                set(value) {_backing.botinokPointer.language = value}

            override var script: String
                get() = _backing.botinokPointer.script
                set(value) {_backing.botinokPointer.script = value}

            override var arena: BotinokArena
                get() = _backing.botinokPointer.arena.toManuallyDefinedInterface()
                set(value) {_backing.botinokPointer.arena = value._backing}

            override fun toString() = _backing.toString()

            override fun hashCode() = _backing.hashCode()

            override fun equals(other: Any?): Boolean {
                val otherShit = other as? Generated_BotinokPointerBackingProvider ?: return false
                return _backing == otherShit._backing
            }
        }
    }

    override fun toString(): String {
        return "BotinokPointer(position=${botinokPointer.position}, name=${botinokPointer.name}, x=${botinokPointer.x}, y=${botinokPointer.y}, pile=${botinokPointer.pile}, language=${botinokPointer.language}, script=${botinokPointer.script})"
    }
}

@XEmbeddable
class Generated_BotinokPointerFields { // Generated at 2-e91acff-5613-4b14-b71e-5edee254d029
    @XEmbedded var common: CommonFields = CommonFields()
    @XColumn lateinit var position: java.lang.Integer
    @XColumn(columnDefinition = "text") lateinit var name: String
    @XColumn lateinit var x: java.lang.Integer
    @XColumn lateinit var y: java.lang.Integer
    @XColumn(columnDefinition = "text") lateinit var pile: String
    @XColumn(columnDefinition = "text") lateinit var language: String
    @XColumn(columnDefinition = "text") lateinit var script: String
    @XManyToOne(fetch = XFetchType.EAGER/*, cascade = arrayOf(XCascadeType.ALL)*/) lateinit var arena: Generated_BotinokArena
}

object BotinokGeneratedDBPile {
    object ddl {
        val dropCreateAllScript = """
drop table if exists "botinok_regions";
drop table if exists "botinok_pointers";
drop table if exists "botinok_arenas";
drop table if exists "botinok_plays";
create table "botinok_plays" (
    id bigserial primary key,
    botinokPlay_common_createdAt timestamp not null,
    botinokPlay_common_updatedAt timestamp not null,
    botinokPlay_common_deleted boolean not null,
    botinokPlay_name text not null,
    botinokPlay_pile text not null
);

create table "botinok_arenas" (
    id bigserial primary key,
    botinokArena_common_createdAt timestamp not null,
    botinokArena_common_updatedAt timestamp not null,
    botinokArena_common_deleted boolean not null,
    botinokArena_position integer not null,
    botinokArena_name text not null,
    botinokArena_screenshot bytea not null,
    botinokArena_pile text not null,
    botinokArena_play__id bigint not null,
    foreign key (botinokArena_play__id) references botinok_plays(id)
);
create index on "botinok_arenas" (botinokArena_play__id);

create table "botinok_pointers" (
    id bigserial primary key,
    botinokPointer_common_createdAt timestamp not null,
    botinokPointer_common_updatedAt timestamp not null,
    botinokPointer_common_deleted boolean not null,
    botinokPointer_position integer not null,
    botinokPointer_name text not null,
    botinokPointer_x integer not null,
    botinokPointer_y integer not null,
    botinokPointer_pile text not null,
    botinokPointer_language text not null,
    botinokPointer_script text not null,
    botinokPointer_arena__id bigint not null,
    foreign key (botinokPointer_arena__id) references botinok_arenas(id)
);
create index on "botinok_pointers" (botinokPointer_arena__id);

create table "botinok_regions" (
    id bigserial primary key,
    botinokRegion_common_createdAt timestamp not null,
    botinokRegion_common_updatedAt timestamp not null,
    botinokRegion_common_deleted boolean not null,
    botinokRegion_position integer not null,
    botinokRegion_name text not null,
    botinokRegion_x integer not null,
    botinokRegion_y integer not null,
    botinokRegion_w integer not null,
    botinokRegion_h integer not null,
    botinokRegion_pile text not null,
    botinokRegion_arena__id bigint not null,
    foreign key (botinokRegion_arena__id) references botinok_arenas(id)
);
create index on "botinok_regions" (botinokRegion_arena__id);

        """
    }
}


/*
DDL
===

drop table if exists "botinok_regions";
drop table if exists "botinok_pointers";
drop table if exists "botinok_arenas";
drop table if exists "botinok_plays";
create table "botinok_plays" (
    id bigserial primary key,
    botinokPlay_common_createdAt timestamp not null,
    botinokPlay_common_updatedAt timestamp not null,
    botinokPlay_common_deleted boolean not null,
    botinokPlay_name text not null,
    botinokPlay_pile text not null
);

create table "botinok_arenas" (
    id bigserial primary key,
    botinokArena_common_createdAt timestamp not null,
    botinokArena_common_updatedAt timestamp not null,
    botinokArena_common_deleted boolean not null,
    botinokArena_position integer not null,
    botinokArena_name text not null,
    botinokArena_screenshot bytea not null,
    botinokArena_pile text not null,
    botinokArena_play__id bigint not null,
    foreign key (botinokArena_play__id) references botinok_plays(id)
);
create index on "botinok_arenas" (botinokArena_play__id);

create table "botinok_pointers" (
    id bigserial primary key,
    botinokPointer_common_createdAt timestamp not null,
    botinokPointer_common_updatedAt timestamp not null,
    botinokPointer_common_deleted boolean not null,
    botinokPointer_position integer not null,
    botinokPointer_name text not null,
    botinokPointer_x integer not null,
    botinokPointer_y integer not null,
    botinokPointer_pile text not null,
    botinokPointer_language text not null,
    botinokPointer_script text not null,
    botinokPointer_arena__id bigint not null,
    foreign key (botinokPointer_arena__id) references botinok_arenas(id)
);
create index on "botinok_pointers" (botinokPointer_arena__id);

create table "botinok_regions" (
    id bigserial primary key,
    botinokRegion_common_createdAt timestamp not null,
    botinokRegion_common_updatedAt timestamp not null,
    botinokRegion_common_deleted boolean not null,
    botinokRegion_position integer not null,
    botinokRegion_name text not null,
    botinokRegion_x integer not null,
    botinokRegion_y integer not null,
    botinokRegion_w integer not null,
    botinokRegion_h integer not null,
    botinokRegion_pile text not null,
    botinokRegion_arena__id bigint not null,
    foreign key (botinokRegion_arena__id) references botinok_arenas(id)
);
create index on "botinok_regions" (botinokRegion_arena__id);

*/