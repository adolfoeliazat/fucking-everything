@file:GSpit(spewClassName = "vgrechka.spew.KotlinDBEntitySpew2", output = "%FE%/alraune/alraune-back/gen/generated--alraune-entities.kt")
@file:GDBEntitySpewOptions(pileObject = "AlGeneratedDBPile",
                           databaseDialect = GDBEntitySpewDatabaseDialect.POSTGRESQL)

package alraune.back

import alraune.shared.OrderCreationFormPostData
import alraune.shared.OrderFileFormPostData
import vgrechka.spew.*

@GEntity(table = "ua_orders")
interface AlUAOrder : GCommonEntityFields {
    var uuid: String
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

    @GOneToMany(mappedBy = "order")
    var files: MutableList<AlUAOrderFile>

    fun toForm() = OrderCreationFormPostData(
        orderUUID = null,
        email = email, name = contactName, phone = phone, documentTypeID = documentTypeID, documentTitle = documentTitle,
        documentDetails = documentDetails, documentCategoryID = documentCategoryID,
        numPages = when (numPages) {
            -1 -> ""
            else -> numPages.toString()
        },
        numSources = when (numSources) {
            -1 -> ""
            else -> numSources.toString()
        }
    )
}

interface AlUAOrderRepository : GRepository<AlUAOrder> {
    fun findByUuid(x: String): AlUAOrder?
}



@GEntity(table = "ua_order_files")
interface AlUAOrderFile : GCommonEntityFields {
    var uuid: String
    var state: UAOrderFileState
    var name: String
    var title: String
    var details: String
    @GManyToOne var order: AlUAOrder

    fun toForm() = OrderFileFormPostData(
        orderUUID = null, fileUUID = uuid,
        name = name, title = title, details = details)
}

interface AlUAOrderFileRepository : GRepository<AlUAOrderFile> {
    fun findByUuid(x: String): AlUAOrderFile?
}

















