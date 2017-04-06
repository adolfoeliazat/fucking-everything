package aps.back

import aps.*
import phizdetslib.*

typealias XSerializable = PHP_Serializable
typealias XTransient = PHP_Transient
typealias XCrudRepository<T, ID> = PHP_CrudRepository<T, ID>
typealias XGenericGenerator = PHP_GenericGenerator
typealias XTimestamp = PHP_Timestamp
typealias XFetchType = PHP_FetchType
typealias XCascadeType = PHP_CascadeType
typealias XIndex = PHP_Index
typealias XEmbeddable = PHP_Embeddable
typealias XPreUpdate = PHP_PreUpdate
typealias XGenerationType = PHP_GenerationType
typealias XGeneratedValue = PHP_GeneratedValue
typealias XId = PHP_Id
typealias XMappedSuperclass = PHP_MappedSuperclass
typealias XEntity = PHP_Entity
typealias XTable = PHP_Table
typealias XEmbedded = PHP_Embedded
typealias XOneToMany = PHP_OneToMany
typealias XColumn = PHP_Column
typealias XEnumerated = PHP_Enumerated
typealias XEnumType = PHP_EnumType
typealias XManyToOne = PHP_ManyToOne
typealias XJsonIgnoreProperties = PHP_JsonIgnoreProperties
typealias XLogger = PHP_Logger
typealias XDate = PHP_Date
typealias XXmlRootElement = PHP_XmlRootElement
typealias XXmlAccessType = PHP_XmlAccessType
typealias XXmlElement = PHP_XmlElement
typealias XCollections = PHP_Collections
typealias XXmlAccessorType = PHP_XmlAccessorType
typealias XBeanDefinition = PHP_BeanDefinition
typealias XScope = PHP_Scope
typealias XComponent = PHP_Component
typealias XDataSource = PHP_DataSource
typealias XServletException = PHP_ServletException
typealias XHttpServletRequest = PHP_HttpServletRequest
typealias XHttpServletResponse = PHP_HttpServletResponse
typealias XThreadLocal<T> = PHP_ThreadLocal<T>

class PHP_ThreadLocal<T> {
    private var value: T? = null

    fun set(newValue: T) {
        value = newValue
    }

}

class PHP_ServletException(cause: Throwable) : Exception(cause.message) {
    val fuckingCause = cause
}

class PHP_HttpServletRequest {
    var characterEncoding: String
        get() = imf("d81d0a76-3609-4ce3-b0ce-a96ee4e19e50")
        set(value) {imf("31fbdf22-7d70-4693-bca5-a194d246de72")}

    val reader: Reader
        get() = imf("3a69f489-d254-4aab-a321-a1a0d39713b8")

    interface Reader {
        fun readText(): String
    }

    val pathInfo: String
        get() = imf("561c681d-8cfe-44ee-afae-1da09189ef78")

    val queryString: String
        get() = phiEval("return \$_SERVER['QUERY_STRING'];")
}

class PHP_HttpServletResponse {
    companion object {
        val SC_OK = 200
    }

    var contentType: String
        get() = imf("3a14d9b1-5118-4f15-8e59-8d0c2a7634d6")
        set(value) {imf("78a5c5eb-bf55-4dec-b8c7-af6fb10894f1")}

    val writer: Writer
        get() = imf("ad50f428-2fe5-4eb4-b676-98b7128bbae3")

    interface Writer {
        fun println(s: String)
    }

    var status: Int
        get() = imf("93f1db06-5aa5-4609-8d06-7ddebfbb3612")
        set(value) {imf("b49d85f3-5745-40e8-89fd-5927f744bb30")}

    fun addHeader(name: String, value: String) {
        header("$name: $value")
    }
}

external fun header(x: String)

class PHP_DataSource {
    fun close() {
        imf("e738c4a5-8529-4d45-8227-f3df72bc5c31")
    }
}

annotation class PHP_Scope(val scopeName: String)

annotation class PHP_Component


interface PHP_BeanDefinition {
    companion object {
        const val SCOPE_PROTOTYPE = "prototype"
    }
}

val backPlatform = object : XBackPlatform {
    override val log = object : XLogger {
        override fun info(s: String) {
            phplog.info(s)
        }

        override fun error(s: String, e: Throwable) {
            imf("539567e3-19ef-4eb5-b89b-87316ad6752a")
        }

        override fun section(s: String) {
            imf("e1456463-02c4-4a7a-8771-42ce63b12fd1")
        }

    }

    override fun makeServant(procedureName: String): BitchyProcedure {
        imf("15b54189-8f6f-4077-a3a3-b54b214fd23f")
    }

    override fun requestTransaction(pathInfo: String, block: () -> Unit) {
        imf("24fa286c-d938-4f0f-9b15-ebe66b9290ab")
    }

    override fun dbTransaction(block: (ShitToDoInTransaction) -> Unit) {
        imf("ddc40b4a-2409-4586-b052-9a14168c5c58")
    }

    override fun makeDataSource(db: DB.Database): XDataSource {
        imf("2128b176-c20b-4c91-98c6-150f4b457768")
    }

    override fun recreateDBSchema() {
        imf("59ba047a-305d-48f8-9ed3-2d09991e4347")
    }

    override fun hashPassword(clearText: String): String {
        imf("cd0e3c93-885a-44da-97dd-a27a50524485")
    }

    override val userRepo: aps.back.UserRepository
        get() = imf("4407362e-e680-4428-8895-0610b4387d02")
    override val userTokenRepo: UserTokenRepository
        get() = imf("26e27a93-3629-4b6a-99ed-e78fcbe34dd7")
    override val userParamsHistoryItemRepo: UserParamsHistoryItemRepository
        get() = imf("b218bb24-a063-4d73-bfa9-8cf0576e97ef")
    override val uaOrderRepo: UAOrderRepository
        get() = imf("e61037f8-c86f-4302-b975-2eee2325d6d6")
    override val uaOrderFileRepo: UAOrderFileRepository
        get() = imf("e5987681-0fd4-462f-bad6-84f09b1bdc8a")
    override val uaDocumentCategoryRepo: UADocumentCategoryRepository
        get() = imf("6a39f4b8-a677-49bc-8370-de8778e2aba4")
    override val userTimesDocumentCategoryRepo: UserTimesDocumentCategoryRepository
        get() = imf("8f610afc-3ffc-4fd7-97b3-3e9c30878ecd")
    override val userParamsHistoryItemTimesDocumentCategoryRepo: UserParamsHistoryItemTimesDocumentCategoryRepository
        get() = imf("0008f7e4-e9ab-41ab-b210-81d0b90bf437")
    override val bidRepo: BidRepository
        get() = imf("b9b83c63-ac6b-4e21-b88f-1ee972d11501")
    override val requestGlobus: RequestGlobusType
        get() = imf("a714ef49-e3a7-48f1-8f12-c4efa1e859a6")
    override val debugLog: XLogger
        get() = imf("d63b32ee-94cf-4777-9e44-2133102acd0a")

    override fun getServeObjectRequestFunction(params: Any): (Any) -> Any {
        imf("812eea57-ef5a-4694-8707-a430d0533526")
    }

    override fun captureStackTrace(): Array<out XStackTraceElement> {
        imf("6635c61c-85e1-49b6-8bcf-43250dcfa763")
    }

    override fun isRequestThread(): Boolean {
        imf("b2b115c7-8e0c-4606-aeaa-5776eb44fea2")
    }

    override fun getResourceAsText(path: String): String {
        imf("d1071b3f-2012-45d8-9e52-baa8d3a0e533")
    }

    override fun highlightRanges(text: String, searchWords: List<String>): List<IntRangeRTO> {
        imf("dec07828-b259-46c9-8192-2374ff069057")
    }

    override val hackyObjectMapper: XHackyObjectMapper
        get() = imf("70245e8a-3883-4d5c-b148-5dcbdfc6850b")
    override val shittyObjectMapper: XShittyObjectMapper
        get() = imf("cfed105b-5fb9-4e50-a570-65d4a325eb1c")
}


annotation class PHP_XmlRootElement(val name: String = "##default")

annotation class PHP_XmlElement(val name: String = "##default")

annotation class PHP_XmlAccessorType(val value: XXmlAccessType)

enum class PHP_XmlAccessType {
    FIELD
}

object PHP_Collections {
    fun <T> synchronizedList(list: MutableList<T>): MutableList<T> {
        return list
    }
}


class PHP_Date {
}

interface PHP_Logger {
    fun info(s: String)
    fun error(s: String, e: Throwable)
    fun section(s: String)
}

interface PHP_Serializable

annotation class PHP_JsonIgnoreProperties(val ignoreUnknown: Boolean)

annotation class PHP_Transient

interface PHP_SharedSessionContractImplementor {
}

class PHP_Timestamp(val time: Long) {
}

enum class PHP_FetchType {
    LAZY, EAGER
}

enum class PHP_CascadeType {
    ALL
}

annotation class PHP_Index(val columnList: String)

annotation class PHP_Embeddable

annotation class PHP_PreUpdate

enum class PHP_GenerationType {
    IDENTITY
}

annotation class PHP_GeneratedValue(
    val strategy: XGenerationType,
    val generator: String
)

annotation class PHP_Id

annotation class PHP_MappedSuperclass

annotation class PHP_Entity

annotation class PHP_Table(val name: String, val indexes: Array<XIndex>)

annotation class PHP_Embedded

annotation class PHP_OneToMany(
    val fetch: XFetchType,
    val mappedBy: String,
    val cascade: Array<XCascadeType> = arrayOf(),
    val orphanRemoval: Boolean = false
)

annotation class PHP_Column(val length: Int = 255, val columnDefinition: String = "")

annotation class PHP_Enumerated(val type: XEnumType)

enum class PHP_EnumType {
    STRING
}

annotation class PHP_ManyToOne(val fetch: XFetchType)

interface PHP_CrudRepository<T, ID> {
    fun <S : T> save(entity: S): S
    fun <S : T> save(entities: Iterable<S>): Iterable<S>
    fun findOne(id: ID): T?
    fun exists(id: ID): Boolean
    fun findAll(): Iterable<T>
    fun findAll(ids: Iterable<ID>): Iterable<T>
    fun count(): Long
    fun delete(id: ID)
    fun delete(entity: T)
    fun delete(entities: Iterable<T>)
    fun deleteAll()
}

annotation class PHP_GenericGenerator(
    val name: String,
    val strategy: String
)

