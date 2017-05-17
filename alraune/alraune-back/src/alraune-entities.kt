@file:GSpit(spewClassName = "vgrechka.spew.PHPDBEntitySpew",
            output = "%FE%/alraune/alraune-back/gen/generated--alraune-entities.kt1")

@file:GDBEntitySpewOptions(pileObject = "AlGeneratedDBPile",
                           databaseDialect = GDBEntitySpewDatabaseDialect.MYSQL)
package alraune.back

import vgrechka.*
import vgrechka.spew.*

@GEntity(table = "alraune_users")
interface AlUser : GCommonEntityFields {
    var firstName: String
    var email: String // TODO:vgrechka Index
    var lastName: String
    var passwordHash: String
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

interface AlUserRepository : GRepository<AlUser> {
    fun findByName(x: String): AlUser?
}


