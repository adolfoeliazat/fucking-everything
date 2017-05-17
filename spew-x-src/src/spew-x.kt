package vgrechka.spew

import vgrechka.*

@Target(AnnotationTarget.FILE)
annotation class GSpit(val spewClassName: String, val output: String)

@Target(AnnotationTarget.FILE)
annotation class GDBEntitySpewOptions(
    val pileObject: String = "",
    val databaseDialect: GDBEntitySpewDatabaseDialect)

enum class GDBEntitySpewDatabaseDialect {
    SQLITE, POSTGRESQL, MYSQL
}

annotation class GEntity(val table: String)
annotation class GOneToMany(val mappedBy: String, val fetch: GFetchType = GFetchType.LAZY)
annotation class GManyToOne(val fetch: GFetchType = GFetchType.EAGER)
annotation class GIsOrderColumn

enum class GFetchType {LAZY, EAGER}

interface GCommonEntityFields {
    var id: Long
    var createdAt: XTimestamp
    var updatedAt: XTimestamp
    var deleted: Boolean
}

interface GRepository<Entity : GCommonEntityFields> {
    fun findOne(id: Long): Entity?
    fun findAll(): List<Entity>
    fun save(x: Entity): Entity
    fun delete(id: Long)
    fun delete(x: Entity)
}

