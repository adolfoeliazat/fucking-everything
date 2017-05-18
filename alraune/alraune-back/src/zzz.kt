package alraune.back

import vgrechka.*
import kotlin.properties.Delegates.notNull

object AlGeneratedDBPile {

}

fun newAlUser(firstName: String,
              email: String,
              lastName: String,
              passwordHash: String,
              profilePhone: String,
              adminNotes: String,
              aboutMe: String,
              profileRejectionReason: String?,
              banReason: String?,
              subscribedToAllCategories: Boolean): AlUser {
    return Generated_AlUser().also {
        it.firstName = firstName
        it.email = email
        it.lastName = lastName
        it.passwordHash = passwordHash
        it.profilePhone = profilePhone
        it.adminNotes = adminNotes
        it.aboutMe = aboutMe
        it.profileRejectionReason = profileRejectionReason
        it.banReason = banReason
        it.subscribedToAllCategories = subscribedToAllCategories
    }
}

class Generated_AlUser : AlUser {
    override var id by notNullOnce<String>()
    override var createdAt: XTimestamp = currentTimestampForEntity()
    override var updatedAt: XTimestamp = createdAt
    override var deleted: Boolean = false
    override var firstName by notNull<String>()
    override var email by notNull<String>()
    override var lastName by notNull<String>()
    override var passwordHash by notNull<String>()
    override var profilePhone by notNull<String>()
    override var adminNotes by notNull<String>()
    override var aboutMe by notNull<String>()
    override var profileRejectionReason: String? = null
    override var banReason: String? = null
    override var subscribedToAllCategories by notNull<Boolean>()

    override fun toString(): String {
        // TODO:vgrechka No secrets here

        return buildString {
            append("AlUser(")
            append("id=$id, ")
            append("createdAt=${phiEval("return gmdate('Y-m-d H:i:s', ${createdAt.time});") as String}, ")
            append("updatedAt=${phiEval("return gmdate('Y-m-d H:i:s', ${updatedAt.time});") as String}, ")
            append("deleted=$deleted, ")
            append("firstName=$firstName, ")
            append("email=$email, ")
            append("lastName=$lastName, ")
            append("profilePhone=$profilePhone, ")
            append("adminNotes=$adminNotes, ")
            append("aboutMe=$aboutMe, ")
            append("profileRejectionReason=$profileRejectionReason, ")
            append("banReason=$banReason, ")
            append("subscribedToAllCategories=$subscribedToAllCategories)")
        }
    }
}

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

val alUserRepo: AlUserRepository by lazy {
    object : AlUserRepository {
        override fun findByName(x: String): AlUser? {
            imf("54de8c1e-f912-4001-853e-6f282a01af6b")
        }

        override fun findOne(id: Long): AlUser? {
            imf("59b778ee-ad34-4d6e-8426-38e79bb64a2a")
        }

        override fun findAll(): List<AlUser> {
            imf("8e99d48a-6392-4fe8-9d7c-d290fac40d8f")
        }

        override fun save(x: AlUser): AlUser {
//            phiEval("\$x = Phi::getCurrentEnv(); \$x = \$x->getVar('sql'); \$x = \$x->value; \$GLOBALS['sql'] = \$x;")

//            phiEval("\$GLOBALS['params'] = array();")
//            for (param in params) {
//                phiEval("\$x = Phi::getCurrentEnv(); \$x = \$x->getVar('param'); \$x = \$x->value; array_push(\$GLOBALS['params'], \$x);")
//            }

            DBPile.execute(
                sql = buildString {
                    ln("insert into `alraune_users`(")
                    ln("    `alUser_common_createdAt`,")
                    ln("    `alUser_common_updatedAt`,")
                    ln("    `alUser_common_deleted`,")
                    ln("    `alUser_firstName`,")
                    ln("    `alUser_email`,")
                    ln("    `alUser_lastName`,")
                    ln("    `alUser_passwordHash`,")
                    ln("    `alUser_profilePhone`,")
                    ln("    `alUser_adminNotes`,")
                    ln("    `alUser_aboutMe`,")
                    ln("    `alUser_profileRejectionReason`,")
                    ln("    `alUser_banReason`,")
                    ln("    `alUser_subscribedToAllCategories`")
                    ln(") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                },
                params = listOf(
                    phiEval("return gmdate('Y-m-d H:i:s', ${x.createdAt.time});") as String,
                    phiEval("return gmdate('Y-m-d H:i:s', ${x.updatedAt.time});") as String,
                    false,
                    x.firstName,
                    x.email,
                    x.lastName,
                    x.passwordHash,
                    x.profilePhone,
                    x.adminNotes,
                    x.aboutMe,
                    x.profileRejectionReason,
                    x.banReason,
                    x.subscribedToAllCategories
                ),
                uuid = "// TODO:vgrechka [Generated UUID of statement here, in comment, for debugging]"
            )

//            val id = phiEval("""
//                global ${'$'}pdo, ${'$'}sql, ${'$'}params;
//                // var_dump(${'$'}sql);
//                // var_dump(${'$'}params);
//                ${'$'}st = ${'$'}pdo->prepare(${'$'}sql);
//                ${'$'}res = ${'$'}st->execute(${'$'}params);
//                if (!${'$'}res) {
//                    throw new Exception('PDO error ' . ${'$'}st->errorCode() . '    1a7ebebb-bf1f-4f4b-8a91-26edaca0795e');
//                }
//                // ${'$'}st->closeCursor();
//
//                ${'$'}st = ${'$'}pdo->prepare('select cast(last_insert_id() as char)');
//                ${'$'}res = ${'$'}st->execute();
//                if (!${'$'}res) {
//                    throw new Exception('PDO error ' . ${'$'}st->errorCode() . '    6a831ee1-26d2-49b2-b827-af8c801a5574');
//                }
//                ${'$'}res = ${'$'}st->fetch(PDO::FETCH_NUM);
//                if (!${'$'}res) {
//                    throw new Exception('ab92fe49-e6e7-4892-b32e-200c08502f03');
//                }
//                ${'$'}id = ${'$'}res[0];
//                // ${'$'}st->closeCursor();
//                return ${'$'}id;
//            """) as String
//
//            x.id = id

            val res = DBPile.query(
                sql = "select cast(last_insert_id() as char)",
                uuid = "TODOOOOOOOOOOOOOO"
            )

            // val res: List<List<Any?>> = listOf(listOf("444"))

            x.id = res.first().first() as String
            return x
        }

        override fun delete(id: Long) {
            imf("fdc64b89-6dd4-452f-ac75-fb8fd6d4b8b7")
        }

        override fun delete(x: AlUser) {
            imf("c8213e46-345c-49bc-9737-25554377b726")
        }
    }
}















