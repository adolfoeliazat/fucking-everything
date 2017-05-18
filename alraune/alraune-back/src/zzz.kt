package alraune.back

import vgrechka.*
import kotlin.properties.Delegates.notNull
import kotlin.reflect.KMutableProperty1

//fun newAlUser(
//    firstName: String,
//    email: String,
//    lastName: String,
//    passwordHash: String,
//    profilePhone: String,
//    adminNotes: String,
//    aboutMe: String,
//    profileRejectionReason: String?,
//    banReason: String?,
//    subscribedToAllCategories: Boolean
//): AlUser {
//    return Generated_AlUser().also {
//        it.firstName = firstName
//        it.email = email
//        it.lastName = lastName
//        it.passwordHash = passwordHash
//        it.profilePhone = profilePhone
//        it.adminNotes = adminNotes
//        it.aboutMe = aboutMe
//        it.profileRejectionReason = profileRejectionReason
//        it.banReason = banReason
//        it.subscribedToAllCategories = subscribedToAllCategories
//    }
//}

//class Generated_AlUser : AlUser {
//    override var id by notNull<String>()
//    override var createdAt: XTimestamp = DBPile.currentTimestampForEntity()
//    override var updatedAt: XTimestamp = createdAt
//    override var deleted: Boolean = false
//    override var firstName by notNull<String>()
//    override var email by notNull<String>()
//    override var lastName by notNull<String>()
//    override var passwordHash by notNull<String>()
//    override var profilePhone by notNull<String>()
//    override var adminNotes by notNull<String>()
//    override var aboutMe by notNull<String>()
//    override var profileRejectionReason: String? = null
//    override var banReason: String? = null
//    override var subscribedToAllCategories by notNull<Boolean>()
//
//    override fun toString(): String {
//        // TODO:vgrechka No secrets here
//
//        return buildString {
//            append("AlUser(")
//            append("id=$id, ")
//            append("createdAt=${phiEval("return gmdate('Y-m-d H:i:s', ${createdAt.time});") as String}, ")
//            append("updatedAt=${phiEval("return gmdate('Y-m-d H:i:s', ${updatedAt.time});") as String}, ")
//            append("deleted=$deleted, ")
//            append("firstName=$firstName, ")
//            append("email=$email, ")
//            append("lastName=$lastName, ")
//            append("profilePhone=$profilePhone, ")
//            append("adminNotes=$adminNotes, ")
//            append("aboutMe=$aboutMe, ")
//            append("profileRejectionReason=$profileRejectionReason, ")
//            append("banReason=$banReason, ")
//            append("subscribedToAllCategories=$subscribedToAllCategories)")
//        }
//    }
//}

val AlUserRepository.dropTableSQL get() = buildString {
    ln("drop table if exists `alraune_users`")
}

val AlUserRepository.createTableSQL get() = buildString {
    ln("create table `alraune_users` (")
    ln("    id bigint not null auto_increment primary key,")
    ln("    alUser_common_createdAt datetime not null,")
    ln("    alUser_common_updatedAt datetime not null,")
    ln("    alUser_common_deleted boolean not null,")
    ln("    alUser_firstName longtext not null,")
    ln("    alUser_email longtext not null,")
    ln("    alUser_lastName longtext not null,")
    ln("    alUser_passwordHash longtext not null,")
    ln("    alUser_profilePhone longtext not null,")
    ln("    alUser_adminNotes longtext not null,")
    ln("    alUser_aboutMe longtext not null,")
    ln("    alUser_profileRejectionReason longtext,")
    ln("    alUser_banReason longtext,")
    ln("    alUser_subscribedToAllCategories boolean not null")
    ln(") engine=InnoDB")
}

fun AlUserRepository.propertyToColumnName(prop: KMutableProperty1<AlUser, String>): String {
    if (prop.name == AlUser::passwordHash.name) return "alUser_passwordHash"
    throw Exception("TODO: Generate 59e545e8-7042-4978-bea2-a2e48d6d290e")
}

//val alUserRepo: AlUserRepository by lazy {
//    object : AlUserRepository {
//        override fun select(prop: KMutableProperty1<AlUser, String>, op: DBPile.Operator, arg: Any?): List<AlUser> {
//            println("findBy(prop = ${prop.name}; op = ${op.toString()}; arg = $arg)")
//            val params = mutableListOf<Any?>()
//            val sql = buildString {
//                ln("select")
//                ln("    cast(id as char),")
//                ln("    unix_timestamp(alUser_common_createdAt),")
//                ln("    unix_timestamp(alUser_common_updatedAt),")
//                ln("    alUser_common_deleted,")
//                ln("    alUser_firstName,")
//                ln("    alUser_email,")
//                ln("    alUser_lastName,")
//                ln("    alUser_passwordHash,")
//                ln("    alUser_profilePhone,")
//                ln("    alUser_adminNotes,")
//                ln("    alUser_aboutMe,")
//                ln("    alUser_profileRejectionReason,")
//                ln("    alUser_banReason,")
//                ln("    alUser_subscribedToAllCategories")
//                ln("from alraune_users")
//                ln("where")
//                ln("${propertyToColumnName(prop)} ${op.sql} ?")
//                params.add(arg)
//            }
//            val rows = DBPile.query(sql, params, uuid = "TODO: Generate e8bd8a84-b3bd-42c9-bc88-160d6e7a94a9")
//            println("findBy: Found ${rows.size} rows")
//            val items = mutableListOf<AlUser>()
//            for (row in rows) {
////                run {
////                    val value = row[13]
////                    println("--- type = ${PHPPile.getType(value)}; value = $value")
////                }
//                items += Generated_AlUser().also {
//                    it.id = row[0] as String
//                    it.createdAt = DBPile.mysqlValueToPHPTimestamp(row[1])
//                    it.updatedAt = DBPile.mysqlValueToPHPTimestamp(row[2])
//                    it.deleted = DBPile.mysqlValueToBoolean(row[3])
//                    it.firstName = row[4] as String
//                    it.email = row[5] as String
//                    it.lastName = row[6] as String
//                    it.passwordHash = row[7] as String
//                    it.profilePhone = row[8] as String
//                    it.adminNotes = row[9] as String
//                    it.aboutMe = row[10] as String
//                    it.profileRejectionReason = row[11] as String?
//                    it.banReason = row[12] as String?
//                    it.subscribedToAllCategories = DBPile.mysqlValueToBoolean(row[13])
//                }
//            }
//            return items
//        }
//
//        override fun insert(x: AlUser): AlUser {
//            DBPile.execute(
//                sql = buildString {
//                    ln("insert into `alraune_users`(")
//                    ln("    `alUser_common_createdAt`,")
//                    ln("    `alUser_common_updatedAt`,")
//                    ln("    `alUser_common_deleted`,")
//                    ln("    `alUser_firstName`,")
//                    ln("    `alUser_email`,")
//                    ln("    `alUser_lastName`,")
//                    ln("    `alUser_passwordHash`,")
//                    ln("    `alUser_profilePhone`,")
//                    ln("    `alUser_adminNotes`,")
//                    ln("    `alUser_aboutMe`,")
//                    ln("    `alUser_profileRejectionReason`,")
//                    ln("    `alUser_banReason`,")
//                    ln("    `alUser_subscribedToAllCategories`")
//                    ln(") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
//                },
//                params = listOf(
//                    phiEval("return gmdate('Y-m-d H:i:s', ${x.createdAt.time});") as String,
//                    phiEval("return gmdate('Y-m-d H:i:s', ${x.updatedAt.time});") as String,
//                    false,
//                    x.firstName,
//                    x.email,
//                    x.lastName,
//                    x.passwordHash,
//                    x.profilePhone,
//                    x.adminNotes,
//                    x.aboutMe,
//                    x.profileRejectionReason,
//                    x.banReason,
//                    x.subscribedToAllCategories
//                ),
//                uuid = "// TODO:vgrechka [Generated UUID of statement here, in comment, for debugging]"
//            )
//
//            val res = DBPile.query(
//                sql = "select cast(last_insert_id() as char)",
//                uuid = "TODOOOOOOOOOOOOOO"
//            )
//
//            x.id = res.first().first() as String
//            return x
//        }
//    }
//}















