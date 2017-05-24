@file:GSpit(spewClassName = "vgrechka.spew.KotlinDBEntitySpew2", output = "%FE%/alraune/alraune-back/gen/generated--alraune-entities.kt")
@file:GDBEntitySpewOptions(pileObject = "AlGeneratedDBPile",
                           databaseDialect = GDBEntitySpewDatabaseDialect.POSTGRESQL)

package alraune.back

import vgrechka.spew.*

@GEntity(table = "alraune_orders")
interface AlOrder : GCommonEntityFields {
    var email: String
    var contactName: String
    var phone: String
    var documentTitle: String
    var documentDetails: String
    var documentCategoryID: String
}

interface AlOrderRepository : GRepository<AlOrder> {
}


