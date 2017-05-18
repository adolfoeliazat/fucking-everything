package alraune.back

import vgrechka.*
import kotlin.properties.Delegates.notNull
import kotlin.reflect.KMutableProperty1

//val AlUserRepository.dropTableDDL get() = buildString {
//    ln("drop table if exists `alraune_users`")
//}
//
//val AlUserRepository.createTableDDL get() = buildString {
//    ln("create table `alraune_users` (")
//    ln("    id bigint not null auto_increment primary key,")
//    ln("    alUser_common_createdAt datetime not null,")
//    ln("    alUser_common_updatedAt datetime not null,")
//    ln("    alUser_common_deleted boolean not null,")
//    ln("    alUser_firstName longtext not null,")
//    ln("    alUser_email longtext not null,")
//    ln("    alUser_lastName longtext not null,")
//    ln("    alUser_passwordHash longtext not null,")
//    ln("    alUser_profilePhone longtext not null,")
//    ln("    alUser_adminNotes longtext not null,")
//    ln("    alUser_aboutMe longtext not null,")
//    ln("    alUser_profileRejectionReason longtext,")
//    ln("    alUser_banReason longtext,")
//    ln("    alUser_subscribedToAllCategories boolean not null")
//    ln(") engine=InnoDB")
//}

//fun AlUserRepository.propertyToColumnName(prop: KMutableProperty1<AlUser, String>): String {
//    if (prop.name == AlUser::passwordHash.name) return "alUser_passwordHash"
//    throw Exception("TODO: Generate 59e545e8-7042-4978-bea2-a2e48d6d290e")
//}















