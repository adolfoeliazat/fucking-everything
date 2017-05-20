@file:GSpit(spewClassName = "vgrechka.spew.PHPDBEntitySpew",
            output = "%FE%/alraune/alraune-back/gen/generated--alraune-entities.kt")

@file:GDBEntitySpewOptions(pileObject = "AlGeneratedDBPile",
                           databaseDialect = GDBEntitySpewDatabaseDialect.MYSQL)
package alraune.back

import vgrechka.*
import vgrechka.spew.*
import kotlin.reflect.KMutableProperty1

@GEntity(table = "alraune_users")
interface AlUser {
    var id: String
    var createdAt: PHPTimestamp
    var updatedAt: PHPTimestamp
    var deleted: Boolean
    var firstName: String
    var email: String // TODO:vgrechka Index
    var lastName: String
    var passwordHash: String
    var sessionID: String
    var profilePhone: String
//    @XEnumerated(XEnumType.STRING) var kind: UserKind,
//    @XEnumerated(XEnumType.STRING) var state: UserState,
    var adminNotes: String
//    var profileUpdatedAt: XTimestamp? = null,
    var aboutMe: String
    var profileRejectionReason: String?
    var banReason: String?
    var subscribedToAllCategories: Boolean
}

interface WickedRepo<T> {
    fun select(prop: KMutableProperty1<T, String>, op: DBPile.Operator, arg: Any?): List<T>
    fun insert(x: T): T
}

interface AlUserRepository : WickedRepo<AlUser>



