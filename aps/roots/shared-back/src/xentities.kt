package aps.back

import aps.*


//private const val MAX_STRING = 10000
private const val MAX_INDEXED_STRING = 255
private const val MAX_BLOB = 10 * 1024 * 1024


private fun currentTimestampForEntity(): XTimestamp {
    return when {
        backPlatform.isRequestThread() -> backPlatform.requestGlobus.stamp
        else -> XTimestamp(sharedPlatform.currentTimeMillis())
    }
}

@XMappedSuperclass
abstract class ClitoralEntity0 {
    @XId
    @XGeneratedValue(strategy = XGenerationType.IDENTITY, generator = "IdentityIfNotSetGenerator")
    @XGenericGenerator(name = "IdentityIfNotSetGenerator", strategy = "aps.back.IdentityIfNotSetGenerator")
    var id: Long? = null

    @XTransient
    var imposedIDToGenerate: Long? = null

    @XPreUpdate
    fun preFuckingUpdate() {
        if (backPlatform.isRequestThread() && !backPlatform.requestGlobus.shitIsDangerous) {
            if (this is User) {
                saveUserParamsHistory(this)
            }
        }
    }
}

class IdentityIfNotSetGeneratorLogic {
    /**
     * @return null if default identity generator should be used
     */
    fun generate(obj: Any?): Long? {
        val entity = obj as ClitoralEntity0
        val id = entity.id
        val imposedIDToGenerate = entity.imposedIDToGenerate
        return when {
            id != null -> id
            imposedIDToGenerate != null -> imposedIDToGenerate
            else -> null
        }
    }
}

@XMappedSuperclass
abstract class ClitoralEntity : ClitoralEntity0() {
    var createdAt: XTimestamp = currentTimestampForEntity()
    var updatedAt: XTimestamp = createdAt
    var deleted: Boolean = false

    fun touch() {
        updatedAt = backPlatform.requestGlobus.stamp
    }
}

@XEmbeddable
data class CommonFields(
    var createdAt: XTimestamp = currentTimestampForEntity(),
    var updatedAt: XTimestamp = createdAt,
    var deleted: Boolean = false
) {
    fun touch() {
        updatedAt = backPlatform.requestGlobus.stamp
    }
}


//============================== User ==============================


@XEntity @XTable(name = "users",
                 indexes = arrayOf(XIndex(columnList = "user_email")))
class User(
    @XEmbedded var user: UserFields,
    @XOneToMany(fetch = XFetchType.LAZY, mappedBy = "user", cascade = arrayOf(XCascadeType.ALL), orphanRemoval = true) var documentCategorySubscriptions: MutableList<UserTimesDocumentCategory> = mutableListOf()
)
    : MeganItem<UserRTO>, ClitoralEntity0()
{
    override val idBang get() = id!!

    override fun toRTO(searchWords: List<String>): UserRTO {
        return userLikeToRTO(idBang, user, documentCategorySubscriptions.map {it.category}, searchWords)
    }
}

@XEmbeddable
data class UserFields(
    @XEmbedded var common: CommonFields = CommonFields(),
    @XColumn(columnDefinition = "text") var firstName: String,
    @XColumn(columnDefinition = "varchar($MAX_INDEXED_STRING)") var email: String,
    @XColumn(columnDefinition = "text") var lastName: String,
    @XColumn(columnDefinition = "text") var passwordHash: String,
    @XColumn(columnDefinition = "text") var profilePhone: String,
    @XEnumerated(XEnumType.STRING) var kind: UserKind,
    @XEnumerated(XEnumType.STRING) var state: UserState,
    override @XColumn(columnDefinition = "text") var adminNotes: String,
    var profileUpdatedAt: XTimestamp? = null,
    @XColumn(columnDefinition = "text") var aboutMe: String = "",
    @XColumn(columnDefinition = "text") var profileRejectionReason: String? = null,
    @XColumn(columnDefinition = "text") var banReason: String? = null,
    var subscribedToAllCategories: Boolean
) : FieldsWithAdminNotes

fun userLikeToRTO(id: Long, uf: UserFields, documentCategories: List<UADocumentCategory>, searchWords: List<String>): UserRTO {
    val title = "${uf.firstName} ${uf.lastName}"
    return UserRTO(
        id = id,
        createdAt = uf.common.createdAt.time,
        updatedAt = uf.common.updatedAt.time,
        profileUpdatedAt = uf.profileUpdatedAt?.time,
        kind = uf.kind,
        lang = Language.UA,
        email = uf.email,
        state = uf.state,
        profileRejectionReason = uf.profileRejectionReason,
        banReason = uf.banReason,
        adminNotes = uf.adminNotes,
        adminNotesHighlightRanges = backPlatform.highlightRanges(uf.adminNotes, searchWords),
        firstName = uf.firstName,
        lastName = uf.lastName,
        aboutMe = uf.aboutMe,
        aboutMeHighlightRanges = backPlatform.highlightRanges(uf.aboutMe, searchWords),
        roles = setOf(),
        profilePhone = uf.profilePhone,
        editable = false,
        title = title,
        titleHighlightRanges = backPlatform.highlightRanges(title, searchWords),
        allDocumentCategories = uf.subscribedToAllCategories,
        documentCategories = documentCategories.map {it.toRTO()}
    )
}

interface UserRepository : XCrudRepository<User, Long> {
    fun findByUser_Email(x: String): User?
    fun countByUser_KindAndUser_State(kind: UserKind, state: UserState): Long
}


//============================== UserDocumentCategorySubscription ==============================


@XEntity @XTable(name = "users__times__document_categories",
               indexes = arrayOf(XIndex(columnList = "user__id"),
                                 XIndex(columnList = "category__id")))
class UserTimesDocumentCategory(
    @XEmbedded var common: CommonFields = CommonFields(),
    @XManyToOne(fetch = XFetchType.LAZY) var user: User,
    @XManyToOne(fetch = XFetchType.LAZY) var category: UADocumentCategory
) : ClitoralEntity0()

interface UserTimesDocumentCategoryRepository : XCrudRepository<UserTimesDocumentCategory, Long> {
}


//============================== UserParamsHistoryItemTimesDocumentCategory ==============================


@XEntity @XTable(name = "user_params_history_items__times__document_categories",
               indexes = arrayOf(XIndex(columnList = "historyItem__id"),
                                 XIndex(columnList = "category__id")))
class UserParamsHistoryItemTimesDocumentCategory(
    @XEmbedded var common: CommonFields = CommonFields(),
    @XManyToOne(fetch = XFetchType.LAZY) var historyItem: UserParamsHistoryItem,
    @XManyToOne(fetch = XFetchType.LAZY) var category: UADocumentCategory
) : ClitoralEntity0()

interface UserParamsHistoryItemTimesDocumentCategoryRepository : XCrudRepository<UserParamsHistoryItemTimesDocumentCategory, Long> {
}








@XEmbeddable class HistoryFields(
    val entityID: Long,
    @XColumn(columnDefinition = "text") var descr: String,
    var createdAt: XTimestamp = currentTimestampForEntity(),
    @XManyToOne(fetch = XFetchType.LAZY) var requester: User,
    @XEmbedded var thenRequester: UserFields
)


//============================== UserParamsHistoryItem ==============================


@XEntity @XTable(name = "user_params_history_items",
               indexes = arrayOf(XIndex(columnList = "history_requester__id")))
class UserParamsHistoryItem(
    @XEmbedded var history: HistoryFields,
    @XEmbedded var entity: UserFields,
    @XOneToMany(fetch = XFetchType.LAZY, mappedBy = "historyItem", cascade = arrayOf(XCascadeType.ALL), orphanRemoval = true) var documentCategorySubscriptions: MutableList<UserParamsHistoryItemTimesDocumentCategory>
)
    : ClitoralEntity0(), MeganItem<UserParamsHistoryItemRTO>
{
    override val idBang get() = id!!

    override fun toRTO(searchWords: List<String>): UserParamsHistoryItemRTO {
        val entityID = history.entityID
        val title = history.descr

        val changer = history.requester
        return UserParamsHistoryItemRTO(
            descr = history.descr,
            entity = userLikeToRTO(entityID, entity, documentCategorySubscriptions.map {it.category}, searchWords),

            // HistoryItemRTOFields
            createdAt = history.createdAt.time,
            requester = changer.toRTO(searchWords = listOf()),
            thenRequester = userLikeToRTO(changer.id!!, history.thenRequester,
                                          documentCategories = listOf(), // TODO:vgrechka Need "then document categories" of "then requester"?
                                          searchWords = listOf()),

            // MelindaItemRTO
            id = idBang,
            title = title,
            editable = false,
            titleHighlightRanges = backPlatform.highlightRanges(title, searchWords)
        )
    }
}


interface UserParamsHistoryItemRepository : XCrudRepository<UserParamsHistoryItem, Long> {
    fun findTop2ByHistory_EntityIDOrderByIdDesc(x: Long): List<UserParamsHistoryItem>
}

fun saveUserToRepo(entity: User): User {
    val res = backPlatform.userRepo.save(entity)
    saveUserParamsHistory(entity, descr = "Created shit")
    return res
}

fun saveUserParamsHistory(entity: User, descr: String = "Updated shit") {
    val historyItem = backPlatform.userParamsHistoryItemRepo.save(
        UserParamsHistoryItem(
            history = HistoryFields(
                entityID = entity.idBang,
                descr = descr,
                requester = backPlatform.requestGlobus.requesterOrAnonymous,
                thenRequester = backPlatform.requestGlobus.requesterOrAnonymousInitialFields
            ),
            entity = entity.user.copy(),
            documentCategorySubscriptions = mutableListOf()
        )
    )

    for (entityCategorySubscription in entity.documentCategorySubscriptions) {
        historyItem.documentCategorySubscriptions.add(
            backPlatform.userParamsHistoryItemTimesDocumentCategoryRepo.save(
                UserParamsHistoryItemTimesDocumentCategory(
                    historyItem = historyItem,
                    category = entityCategorySubscription.category
                )))
    }
}

interface FieldsWithAdminNotes {
    var adminNotes: String
}

@XEmbeddable
data class UAOrderFields(
    @XEmbedded var common: CommonFields = CommonFields(),
    @XColumn(columnDefinition = "text") var title: String,
    @XEnumerated(XEnumType.STRING) var documentType: UADocumentType,
    var numPages: Int,
    var numSources: Int,
    @XColumn(columnDefinition = "text") var details: String,
    @XEnumerated(XEnumType.STRING) var state: UAOrderState,
    @XColumn(columnDefinition = "varchar($MAX_INDEXED_STRING)") var confirmationSecret: String,
    @XColumn(columnDefinition = "text") var customerFirstName: String,
    @XColumn(columnDefinition = "text") var customerLastName: String,
    @XColumn(columnDefinition = "text") var customerPhone: String,
    @XColumn(columnDefinition = "text") var customerEmail: String,
    @XColumn(columnDefinition = "text") var whatShouldBeFixedByCustomer : String? = null,
    override @XColumn(columnDefinition = "text") var adminNotes: String,
    var movedToStoreAt: XTimestamp? = null,
    var minAllowedPriceOffer: Int,
    var maxAllowedPriceOffer: Int,
    var minAllowedDurationOffer: Int,
    var maxAllowedDurationOffer: Int,
    @XManyToOne(fetch = XFetchType.EAGER) var customer: User?, // TODO:vgrechka Think about nullability of this shit. Order can be draft, before customer even confirmed herself
    @XManyToOne(fetch = XFetchType.LAZY) var category: UADocumentCategory,
    @XOneToMany(fetch = XFetchType.LAZY, mappedBy = "order") var bids: MutableList<Bid> = mutableListOf()
) : FieldsWithAdminNotes

@XEntity @XTable(name = "ua_orders",
               indexes = arrayOf(XIndex(columnList = "order_confirmationSecret"),
                                 XIndex(columnList = "order_category__id"),
                                 XIndex(columnList = "order_customer__id")))
class UAOrder(@XEmbedded var order: UAOrderFields)
    : ClitoralEntity0(), MeganItem<UAOrderRTO>
{
    override val idBang get()= id!!

    override fun toRTO(searchWords: List<String>): UAOrderRTO {
        return UAOrderRTO(
            id = id!!,
            title = order.title,
            titleHighlightRanges = backPlatform.highlightRanges(order.title, searchWords),
            detailsHighlightRanges = backPlatform.highlightRanges(order.details, searchWords),
            editable = true, // TODO:vgrechka ...
            createdAt = order.common.createdAt.time,
            updatedAt = order.common.updatedAt.time,
            customer = order.customer!!.toRTO(searchWords = listOf()),
            documentType = order.documentType,
            price = -1,
            numPages = order.numPages,
            numSources = order.numSources,
            details = order.details,
            adminNotes = order.adminNotes,
            adminNotesHighlightRanges = backPlatform.highlightRanges(order.adminNotes, searchWords),
            state = order.state,
            customerPhone = order.customerPhone,
            customerFirstName = order.customerFirstName,
            customerLastName = order.customerLastName,
            customerEmail = order.customerEmail,
            whatShouldBeFixedByCustomer = order.whatShouldBeFixedByCustomer,
            movedToStoreAt = order.movedToStoreAt?.time,
            minAllowedPriceOffer = order.minAllowedPriceOffer,
            maxAllowedPriceOffer = order.maxAllowedPriceOffer,
            minAllowedDurationOffer = order.minAllowedDurationOffer,
            maxAllowedDurationOffer = order.maxAllowedDurationOffer,
            documentCategory = order.category.toRTO(),
            myBid = ifInStoreAndWriterLooking(this) {backPlatform.bidRepo.findByOrderAndBidder(this, requestUserEntity)?.toRTO(searchWords = listOf())},
            bidsSummary = ifInStoreAndWriterOrAdminLooking(this) {
                val bids = backPlatform.bidRepo.findByOrder(this)
                if (bids.isEmpty())
                    null
                else {
                    var firstBidAt = ValueAndWhetherMineRTO(Long.MAX_VALUE, false)
                    var lastBidAt = ValueAndWhetherMineRTO(Long.MIN_VALUE, false)
                    var minPriceOffer = ValueAndWhetherMineRTO(Int.MAX_VALUE, false)
                    var maxPriceOffer = ValueAndWhetherMineRTO(Int.MIN_VALUE, false)
                    var minDurationOffer = ValueAndWhetherMineRTO(Int.MAX_VALUE, false)
                    var maxDurationOffer = ValueAndWhetherMineRTO(Int.MIN_VALUE, false)
                    for (bid in bids) {
                        val mine = bid.bidder.id == requestUserEntity.id
                        if (bid.common.createdAt.time < firstBidAt.value) firstBidAt = ValueAndWhetherMineRTO(bid.common.createdAt.time, mine)
                        if (bid.common.createdAt.time > lastBidAt.value) lastBidAt = ValueAndWhetherMineRTO(bid.common.createdAt.time, mine)
                        if (bid.priceOffer < minPriceOffer.value) minPriceOffer = ValueAndWhetherMineRTO(bid.priceOffer, mine)
                        if (bid.priceOffer > maxPriceOffer.value) maxPriceOffer = ValueAndWhetherMineRTO(bid.priceOffer, mine)
                        if (bid.durationOffer < minDurationOffer.value) minDurationOffer = ValueAndWhetherMineRTO(bid.durationOffer, mine)
                        if (bid.durationOffer > maxDurationOffer.value) maxDurationOffer = ValueAndWhetherMineRTO(bid.durationOffer, mine)
                    }
                    BidsSummaryRTO(
                        numParticipants = bids.size,
                        minPriceOffer = minPriceOffer,
                        maxPriceOffer = maxPriceOffer,
                        minDurationOffer = minDurationOffer,
                        maxDurationOffer = maxDurationOffer,
                        firstBidAt = firstBidAt,
                        lastBidAt = lastBidAt
                    )
                }
            }
        )
    }
}

private fun <T> ifInStoreAndWriterLooking(order: UAOrder, block: () -> T): T? =
    ifInStore(order, {requestUser.kind in setOf(UserKind.WRITER)}, block)

private fun <T> ifInStoreAndWriterOrAdminLooking(order: UAOrder, block: () -> T): T? =
    ifInStore(order, {requestUser.kind in setOf(UserKind.WRITER, UserKind.ADMIN)}, block)

private fun <T> ifInStore(order: UAOrder, isAppropriateUser: () -> Boolean, block: () -> T): T? {
    if (!isAppropriateUser()) return null
    if (order.order.state != UAOrderState.IN_STORE) return null
    return block()
}


interface UAOrderRepository : XCrudRepository<UAOrder, Long> {
    fun findByOrder_ConfirmationSecret(x: String): UAOrder?
    fun countByOrder_State(x: UAOrderState): Long
}

@XEntity @XTable(name = "user_tokens",
               indexes = arrayOf(XIndex(columnList = "user__id"),
                                 XIndex(columnList = "token")))
class UserToken(
    @XColumn(columnDefinition = "varchar($MAX_INDEXED_STRING)") var token: String,
    @XManyToOne(fetch = XFetchType.LAZY) var user: User?
) : ClitoralEntity()

interface UserTokenRepository : XCrudRepository<UserToken, Long> {
    fun findByToken(x: String): UserToken?
}


@XEmbeddable
data class UAOrderFileFields(
    @XEmbedded var common: CommonFields = CommonFields(),
    @XColumn(columnDefinition = "text") var name: String,
    @XColumn(columnDefinition = "text") var title: String,
    @XColumn(columnDefinition = "text") var mime: String,
    @XColumn(columnDefinition = "text") var details: String,
    override @XColumn(columnDefinition = "text") var adminNotes: String,
    @XColumn(columnDefinition = "text") var sha256: String,
    var sizeBytes: Int,
    @Suppress("ArrayInDataClass") @XColumn(length = MAX_BLOB) var content: ByteArray,
    @XEnumerated(XEnumType.STRING) var forCustomerSeenAsFrom: UserKind,
    @XEnumerated(XEnumType.STRING) var forWriterSeenAsFrom: UserKind,
    @XManyToOne(fetch = XFetchType.LAZY) var creator: User,
    @XManyToOne(fetch = XFetchType.LAZY) var order: UAOrder
) : FieldsWithAdminNotes


@XEntity @XTable(name = "ua_order_files",
               indexes = arrayOf(XIndex(columnList = "orderfile_creator__id"),
                                 XIndex(columnList = "orderfile_order__id")))
class UAOrderFile(@XEmbedded var orderFile: UAOrderFileFields)
    : ClitoralEntity0(), MeganItem<UAOrderFileRTO>
{
    override val idBang get()= id!!

    override fun toRTO(searchWords: List<String>): UAOrderFileRTO {
        return UAOrderFileRTO(
            id = id!!,
            createdAt = orderFile.common.createdAt.time,
            updatedAt = orderFile.common.updatedAt.time,
            name = orderFile.name,
            title = orderFile.title,
            details = orderFile.details,
            sizeBytes = orderFile.sizeBytes,
            detailsHighlightRanges = backPlatform.highlightRanges(orderFile.details, searchWords),
            editable = run {
                val user = requestUserEntity
                when (user.user.kind) {
                    UserKind.ADMIN -> true
                    UserKind.CUSTOMER -> orderFile.order.order.state in setOf(UAOrderState.CUSTOMER_DRAFT, UAOrderState.RETURNED_TO_CUSTOMER_FOR_FIXING)
                    UserKind.WRITER -> imf("UAOrderFileRTO.editable for writer")
                }
            },
            nameHighlightRanges = when {
                searchWords.isEmpty() -> listOf()
                else -> backPlatform.highlightRanges(orderFile.name.chopOffFileExtension(), searchWords)
            },
            seenAsFrom = when (requestUserEntity.user.kind) {
                UserKind.CUSTOMER -> orderFile.forCustomerSeenAsFrom
                UserKind.WRITER -> orderFile.forWriterSeenAsFrom
                UserKind.ADMIN -> orderFile.creator.user.kind
            },
            titleHighlightRanges = when {
                searchWords.isEmpty() -> listOf()
                else -> backPlatform.highlightRanges(orderFile.title, searchWords)
            },
            adminNotes = orderFile.adminNotes,
            adminNotesHighlightRanges = when {
                searchWords.isEmpty() -> listOf()
                else -> backPlatform.highlightRanges(orderFile.adminNotes, searchWords)
            }
        )
    }
}

interface UAOrderFileRepository : XCrudRepository<UAOrderFile, Long> {
}





//============================== UADocumentCategory ==============================

// TODO:vgrechka Ditch UADocumentCategoryFields?

@XEntity @XTable(name = "ua_document_categories",
               indexes = arrayOf(XIndex(columnList = "category_parent__id")))
class UADocumentCategory(@XEmbedded var category: UADocumentCategoryFields)
    : ClitoralEntity0()
{

    fun toRTO(loadChildren: Boolean = false): UADocumentCategoryRTO {
        var pathTitle = category.title
        var parent = category.parent
        while (parent != null && parent.id != const.uaDocumentCategoryID.root) {
            pathTitle = parent.category.title + const.text.symbols.rightDoubleAngleQuotationSpaced + pathTitle
            parent = parent.category.parent
        }
        return UADocumentCategoryRTO(
            id = id!!,
            title = category.title,
            pathTitle = pathTitle,
            children = when {
                loadChildren -> category.children.map {it.toRTO(loadChildren = true)}
                else -> listOf()
            }
        )
    }
}

@XEmbeddable
data class UADocumentCategoryFields(
    @XEmbedded var common: CommonFields = CommonFields(),
    @XColumn(columnDefinition = "text") var title: String,
    @XManyToOne(fetch = XFetchType.LAZY) var parent: UADocumentCategory?,
    @XOneToMany(fetch = XFetchType.LAZY, mappedBy = "category.parent") var children: MutableList<UADocumentCategory>
)

interface UADocumentCategoryRepository : XCrudRepository<UADocumentCategory, Long> {
}


//============================== Bid ==============================


@XEntity @XTable(name = "bids",
               indexes = arrayOf(XIndex(columnList = "order__id")))
class Bid(
    @XEmbedded var common: CommonFields = CommonFields(),
    var priceOffer: Int,
    var durationOffer: Int,
    @XColumn(columnDefinition = "text") var comment: String,
    override @XColumn(columnDefinition = "text") var adminNotes: String,
    @XManyToOne(fetch = XFetchType.LAZY) var order: UAOrder,
    @XManyToOne(fetch = XFetchType.LAZY) var bidder: User,
    var toConsiderByAdmin: Boolean
)
    : ClitoralEntity0(), FieldsWithAdminNotes
{
    fun toRTO(searchWords: List<String>): BidRTO {
        val title = "pizda"
        return BidRTO(
            id = id!!,
            title = title,
            editable = false,
            titleHighlightRanges = backPlatform.highlightRanges(title, searchWords),
            createdAt = common.createdAt.time,
            updatedAt = common.updatedAt.time,
            adminNotes = adminNotes,
            adminNotesHighlightRanges = backPlatform.highlightRanges(adminNotes, searchWords),
            priceOffer = priceOffer,
            durationOffer = durationOffer,
            comment = comment
        )
    }
}

interface BidRepository : XCrudRepository<Bid, Long> {
    fun findByOrderAndBidder(order: UAOrder, bidder: User): Bid?
    fun findByOrder(order: UAOrder): List<Bid>
    fun countByToConsiderByAdmin(x: Boolean): Long
}

















