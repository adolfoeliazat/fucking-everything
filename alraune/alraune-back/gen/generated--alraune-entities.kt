/*
 * (C) Copyright 2017 Vladimir Grechka
 *
 * YOU DON'T MESS AROUND WITH THIS SHIT, IT WAS GENERATED BY A TOOL SMARTER THAN YOU
 */

//
// Generated on Tue May 23 12:52:51 EEST 2017
// Model: e:/fegh/alraune/alraune-back/src/alraune-entities.kt
//

package alraune.back

import kotlin.reflect.KClass
import vgrechka.*
import vgrechka.spew.*

// ------------------------------------------------------------------
// AlOrder
// ------------------------------------------------------------------

// Generated at 6-b0127c7-c425-4e05-9d26-a120e96010ce
fun newAlOrder(email: String,
               contactName: String,
               phone: String,
               documentTitle: String,
               documentDetails: String): AlOrder {
    val backing = Generated_AlOrder()
        .also {it.email = email
               it.contactName = contactName
               it.phone = phone
               it.documentTitle = documentTitle
               it.documentDetails = documentDetails}
    return backing.toManuallyDefinedInterface()
}

val alOrderRepo: AlOrderRepository by lazy {
    val generatedRepo = backPlatform.springctx.getBean(Generated_AlOrderRepository::class.java)!!

    object:AlOrderRepository {
        override fun findOne(id: Long): AlOrder? {
            val shit = generatedRepo.findOne(id)
            return shit?.toManuallyDefinedInterface()
        }

        override fun findAll(): List<AlOrder> {
            val shit = generatedRepo.findAll()
            return shit.map {it.toManuallyDefinedInterface()}
        }

        override fun save(x: AlOrder): AlOrder {
            val shit = generatedRepo.save(x._backing)
            return shit.toManuallyDefinedInterface()
        }

        override fun delete(id: Long) {
            generatedRepo.delete(id)
        }

        override fun delete(x : AlOrder) {
            generatedRepo.delete(x._backing)
        }

    }
}

interface Generated_AlOrderRepository : XCrudRepository<Generated_AlOrder, Long> {
}

val AlOrder._backing
    get() = (this as Generated_AlOrderBackingProvider)._backing

interface Generated_AlOrderBackingProvider : DBCodeGenUtils.GeneratedBackingEntityProvider<Generated_AlOrder> {
    override val _backing: Generated_AlOrder
}

@XEntity @XTable(name = "alraune_orders")
class Generated_AlOrder // Generated at 8-079453c-f675-490e-8367-7891d1a8b01a
    : DBCodeGenUtils.GeneratedEntity<AlOrder>
{
    @XId
    @XGeneratedValue(strategy = XGenerationType.IDENTITY, generator = "IdentityIfNotSetGenerator")
    @XGenericGenerator(name = "IdentityIfNotSetGenerator", strategy = "vgrechka.db.IdentityIfNotSetGenerator")
    @XColumn(name = "orders_id") var id: Long? = null
    @XTransient var imposedIDToGenerate: Long? = null

    @XColumn(name = "orders_createdAt") var createdAt: XTimestamp = DBCodeGenUtils.currentTimestampForEntity()
    @XColumn(name = "orders_updatedAt") var updatedAt: XTimestamp = createdAt
    @XColumn(name = "orders_deleted") var deleted: Boolean = false
    @XColumn(name = "orders_email", columnDefinition = "text") lateinit var email: String
    @XColumn(name = "orders_contactName", columnDefinition = "text") lateinit var contactName: String
    @XColumn(name = "orders_phone", columnDefinition = "text") lateinit var phone: String
    @XColumn(name = "orders_documentTitle", columnDefinition = "text") lateinit var documentTitle: String
    @XColumn(name = "orders_documentDetails", columnDefinition = "text") lateinit var documentDetails: String

    override fun toManuallyDefinedInterface(): AlOrder {
        return object : AlOrder, Generated_AlOrderBackingProvider {
            override val _backing: Generated_AlOrder
                get() = this@Generated_AlOrder

            override var id: Long
                get() = _backing.id!!
                set(value) {_backing.id = value}

            override var createdAt: XTimestamp
                get() = _backing.createdAt
                set(value) {_backing.createdAt = value}

            override var updatedAt: XTimestamp
                get() = _backing.updatedAt
                set(value) {_backing.updatedAt = value}

            override var deleted: Boolean
                get() = _backing.deleted
                set(value) {_backing.deleted = value}

            override var email: String
                get() = _backing.email
                set(value) {_backing.email = value}

            override var contactName: String
                get() = _backing.contactName
                set(value) {_backing.contactName = value}

            override var phone: String
                get() = _backing.phone
                set(value) {_backing.phone = value}

            override var documentTitle: String
                get() = _backing.documentTitle
                set(value) {_backing.documentTitle = value}

            override var documentDetails: String
                get() = _backing.documentDetails
                set(value) {_backing.documentDetails = value}

            override fun toString() = _backing.toString()

            override fun hashCode() = _backing.hashCode()

            override fun equals(other: Any?): Boolean {
                val otherShit = other as? Generated_AlOrderBackingProvider ?: return false
                return _backing == otherShit._backing
            }
        }
    }

    override fun toString(): String {
        return "AlOrder(email=${email}, contactName=${contactName}, phone=${phone}, documentTitle=${documentTitle}, documentDetails=${documentDetails})"
    }
}


object AlGeneratedDBPile {
    object ddl {
        val dropCreateAllScript = """
drop table if exists "alraune_orders";
create table "alraune_orders" (
    orders_id bigserial primary key,
    orders_createdAt timestamp not null,
    orders_updatedAt timestamp not null,
    orders_deleted boolean not null,
    orders_email text not null,
    orders_contactName text not null,
    orders_phone text not null,
    orders_documentTitle text not null,
    orders_documentDetails text not null
) ;

        """
    }
}


/*
DDL
===

drop table if exists "alraune_orders";
create table "alraune_orders" (
    orders_id bigserial primary key,
    orders_createdAt timestamp not null,
    orders_updatedAt timestamp not null,
    orders_deleted boolean not null,
    orders_email text not null,
    orders_contactName text not null,
    orders_phone text not null,
    orders_documentTitle text not null,
    orders_documentDetails text not null
) ;

*/