@file:GSpit(spewClassName = "vgrechka.spew.KotlinDBEntitySpew2", output = "%FE%/alraune/alraune-back/gen/generated--alraune-entities.kt")
@file:GDBEntitySpewOptions(pileObject = "AlGeneratedDBPile",
                           databaseDialect = GDBEntitySpewDatabaseDialect.POSTGRESQL)

package alraune.back

import vgrechka.spew.*

@GEntity(table = "ua_orders")
interface AlUAOrder : GCommonEntityFields {
    var state: UAOrderState
    var email: String
    var contactName: String
    var phone: String
    var documentTypeID: String
    var documentTitle: String
    var documentDetails: String
    var documentCategoryID: String
    var numPages: Int
    var numSources: Int
}

interface AlUAOrderRepository : GRepository<AlUAOrder> {
}


