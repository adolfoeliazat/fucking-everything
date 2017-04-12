package aps.back

import aps.*
import kotlin.reflect.KClass

interface XBackPlatform {
    val userRepo: UserRepository
    val userTokenRepo: UserTokenRepository
    val userParamsHistoryItemRepo: UserParamsHistoryItemRepository
    val uaOrderRepo: UAOrderRepository
    val uaOrderFileRepo: UAOrderFileRepository
    val uaDocumentCategoryRepo: UADocumentCategoryRepository
    val userTimesDocumentCategoryRepo: UserTimesDocumentCategoryRepository
    val userParamsHistoryItemTimesDocumentCategoryRepo: UserParamsHistoryItemTimesDocumentCategoryRepository
    val bidRepo: BidRepository
    val requestGlobus: RequestGlobusType
    val log: XLogger
    val debugLog: XLogger

    fun getServeObjectRequestFunction(params: Any): (Any) -> Any
    fun captureStackTrace(): Array<out XStackTraceElement>
    fun isRequestThread(): Boolean
    fun getResourceAsText(path: String): String
    fun highlightRanges(text: String, searchWords: List<String>): List<IntRangeRTO>
    fun recreateDBSchema()
    fun hashPassword(clearText: String): String
    fun dbTransaction(block: (ShitToDoInTransaction) -> Unit)
    val hackyObjectMapper: XHackyObjectMapper
    val shittyObjectMapper: XShittyObjectMapper
    fun makeDataSource(db: DB.Database): XDataSource
    fun makeServant(procedureName: String): BitchyProcedure
    fun requestTransaction(pathInfo: String, block: () -> Unit)
}

interface ShitToDoInTransaction {
    fun executeUpdate(sql: String): Int
}

interface XShittyObjectMapper {
    fun writeValueAsString(shit: Any): String
}

interface XHackyObjectMapper {
    fun <T : Any> readValue(content: String, valueType: KClass<T>): T
}

val requestGlobusThreadLocal = XThreadLocal<RequestGlobusType>()

class RequestGlobusType {
    val stamp by lazy {
        TestServerFiddling.nextRequestTimestamp.getAndReset()
            ?: XTimestamp(sharedPlatform.currentTimeMillis())
    }

//    var skipLoggingToRedis = false
    var actualSQLFromJOOQ: String? = null
//    var resultFromJOOQ: Result<*>? = null
//    val redisLogParentIDs = Stack<String>()
    lateinit var commonRequestFields: CommonRequestFieldsHolder
    var procedureCtx by notNullOnce<ProcedureContext>()
    val retrievedFields = mutableSetOf<FormFieldBack>()
    var requesterOrAnonymous by notNullOnce<User>()
    var requesterOrAnonymousInitialFields by notNullOnce<UserFields>()
    var shitIsDangerous by notNullOnce<Boolean>()
}

interface XStackTraceElement

annotation class Back

interface Culprit {
    val constructionStack: Array<out XStackTraceElement>
}

interface WithCulprit {
    val culprit: Culprit
}

class ExceptionWithCulprit(e: Throwable, override val culprit: Culprit): Throwable(e.message, e), WithCulprit

fun <T> beingCulprit(culprit: Culprit, f: () -> T): T {
    return try {
        f()
    } catch (e: Exception) {
        throw ExceptionWithCulprit(e, culprit)
    }
}


abstract class FormFieldBack(
    container: RequestMatumba,
    val name: String,
    val possiblyUnspecified: Boolean = false,
    val include: Boolean = true
) : Culprit {
    abstract fun loadOrBitch(input: Map<String, Any?>, fieldErrors: MutableList<FieldError>)

    var _specified by notNull<Boolean>()
    val specified: Boolean get() = _specified
    override val constructionStack = backPlatform.captureStackTrace()

    init {
        if (include) {
            @Suppress("LeakingThis")
            container._fields.add(this)
        }
    }

    fun load(input: Map<String, Any?>, fieldErrors: MutableList<FieldError>) {
        if (include) {
            beingCulprit(this, {
                if (possiblyUnspecified) {
                    _specified = input["$name-specified"] as Boolean
                }
                loadOrBitch(input, fieldErrors)
            })
        }
    }

    override fun toString(): String = bitch("Use field.value to get value of field [$name]")
}

class BitchyProcedureContext(
    val servletRequest: XHttpServletRequest,
    val servletResponse: XHttpServletResponse
)

interface FuckingHttpServletResponse {
    var contentType: String
    val writer: Writer
    var status: Status

    interface Writer {
        fun println(s: String)
    }

    enum class Status {
        OK
    }
}

interface FuckingHttpServletRequest {
    var characterEncoding: String
    val reader: Reader
    val pathInfo: String

    interface Reader {
        fun readText(): String
    }
}

inline fun <reified T> XCrudRepository<T, Long>.findOrDie(id: Long): T {
    return findOne(id) ?: die("No fucking ${T::class.simpleName} with ID $id")
}

class FuckSomeoneParams<Req : RequestMatumba, out Res : CommonResponseFields>(
    val bpc: BitchyProcedureContext,
    val req: (ProcedureContext) -> Req,
    val runShit: (ProcedureContext, Req) -> Res,
    val validate: (ProcedureContext, Req) -> Unit = { ctx, req -> },
    val wrapInFormResponse: Boolean,
    @Deprecated("Kill me") val needsDB: Boolean,
    val needsDangerousToken: Boolean,
    val needsUser: NeedsUser,
    val userKinds: Set<UserKind>,
    val considerNextRequestTimestampFiddling: Boolean,
    val logRequestJSON: Boolean
)

fun <Req : RequestMatumba, Res : CommonResponseFields>
    fuckSomeone(p: FuckSomeoneParams<Req, Res>)
{
    object {
        lateinit var responseBean: CommonResponseFields
        val log = backPlatform.debugLog
        val ctx = ProcedureContext()

        init {
            backPlatform.requestGlobus.procedureCtx = ctx
            try {
                p.bpc.servletRequest.characterEncoding = "UTF-8"
                val requestJSON = p.bpc.servletRequest.reader.readText()
                if (p.logRequestJSON) {
                    log.info("${p.bpc.servletRequest.pathInfo}: $requestJSON")
                }
                val rmap = backPlatform.hackyObjectMapper.readValue(requestJSON, Map::class)
                rmap as Map<String, Any?>
                backPlatform.requestGlobus.commonRequestFields = backPlatform.hackyObjectMapper.readValue(requestJSON, CommonRequestFieldsHolder::class)
                // log.section("rmap:", rmap)

                fun serviceShit() {
                    (rmap["wideClientKind"] as String).let {
                        when (it) {
                            WideClientKind.User::class.simpleName -> {
                                ctx.clientKind = ClientKind.valueOf(rmap["clientKind"] as String)
                                ctx.wideClientKind = WideClientKind.User(ctx.clientKind)
                                ctx.clientDomain = when (ctx.clientKind) {
                                    ClientKind.UA_CUSTOMER -> "aps-ua-customer.local"
                                    ClientKind.UA_WRITER -> "aps-ua-writer.local"
                                }
                                ctx.clientPortSuffix = when (ctx.clientKind) {
                                    ClientKind.UA_CUSTOMER -> ":3012"
                                    ClientKind.UA_WRITER -> ":3022"
                                }
                            }
                            WideClientKind.Test::class.simpleName -> {
                                ctx.wideClientKind = WideClientKind.Test()
                            }
                            else -> wtf("wideClientKind: $it")
                        }
                    }
                    ctx.lang = Language.valueOf(rmap["lang"] as String)

                    fun runShitWithMaybeDB(): Res {
                        if (p.needsUser != NeedsUser.NO) {
                            val token = rmap["token"] as String?
                            if (token == null) {
                                if (p.needsUser == NeedsUser.YES)
                                    bitch("I want freaking token")
                                ctx.hasUser = false
                            } else {
                                ctx.token = token
                                val u = userByToken2(ctx.token)
                                ctx.user = u
                                ctx.user_killme = u.toRTO(searchWords = listOf())
                                if (!p.userKinds.contains(ctx.user_killme.kind))
                                    bitch("User kind not allowed: ${ctx.user_killme.kind}")
                                ctx.hasUser = true
                            }
                        }

                        val input  = rmap["fields"] as Map<String, Any?>
                        val req = p.req(ctx)
                        for (field in req._fields) field.load(input, ctx.fieldErrors)

                        if (p.needsDangerousToken) {
                            if (rmap["token"] != systemDangerousToken()) {
                                bitch("Invalid dangerous token")
                            }
                            backPlatform.requestGlobus.shitIsDangerous = true
                        } else {
                            backPlatform.requestGlobus.shitIsDangerous = false
                        }

                        p.validate(ctx, req)
                        if (ctx.fieldErrors.isNotEmpty()) bitchExpectedly(t("Please fix errors below", "Пожалуйста, исправьте ошибки ниже"))

                        if (!backPlatform.requestGlobus.shitIsDangerous) {
                            backPlatform.requestGlobus.requesterOrAnonymous = ctx.user ?: when (ctx.clientKind) {
                                ClientKind.UA_CUSTOMER -> backPlatform.userRepo.findOrDie(const.userID.anonymousCustomer)
                                ClientKind.UA_WRITER -> backPlatform.userRepo.findOrDie(const.userID.anonymousWriter)
                            }
                            backPlatform.requestGlobus.requesterOrAnonymousInitialFields = backPlatform.requestGlobus.requesterOrAnonymous.user.copy()
                        }

                        return p.runShit(ctx, req)
                    }

                    val res = if (p.needsDB) {
                        if (TestServerFiddling.rejectAllRequestsNeedingDB) bitch("Fuck you. I mean nothing personal, I do this to everyone...")

                        runShitWithMaybeDB()
                    } else {
                        runShitWithMaybeDB()
                    }
                    res.backendVersion = BackGlobus.version

                    responseBean = if (p.wrapInFormResponse) FormResponse.Hunky(res) else res
                }

                val pathInfo = p.bpc.servletRequest.pathInfo
                serviceShit()
//                if (pathInfo.contains("privilegedRedisCommand"))
//                    serviceShit()
//                else
//                    redisLog.group("Request: $pathInfo", ::serviceShit)
            }
            catch (e: ExpectedRPCShit) {
                if (p.wrapInFormResponse) {
                    log.info("Softened RPC shit: ${e.message}")
                    responseBean = FormResponse.Shitty(e.message, ctx.fieldErrors)
                } else {
                    throw e
                }
            }

            responseBean.backendVersion = BackGlobus.version

            p.bpc.servletResponse-{o->
                o.contentType = "application/json; charset=utf-8"
                o.writer.println(backPlatform.shittyObjectMapper.writeValueAsString(responseBean))
                o.status = XHttpServletResponse.SC_OK
            }
        }
    }
}


object TestServerFiddling {
    val nextRequestTimestamp = SetGetResetShit<XTimestamp>()
    val nextGeneratedPassword = SetGetResetShit<String>()
    val nextRequestError = SetGetResetShit<String>()
    val nextGeneratedConfirmationSecret = SetGetResetShit<String>()
    val nextGeneratedUserToken = SetGetResetShit<String>()
    val nextOrderID = SetGetResetShit<Long>()
    @Volatile var rejectAllRequestsNeedingDB: Boolean = false
}

class SetGetResetShit<T> {
    private @Volatile var value: T? = null

    fun getAndReset(): T? {
        val res = value
        value = null
        return res
    }

    fun set(newValue: T) {
        value = newValue
    }
}

@XJsonIgnoreProperties(ignoreUnknown = true)
class CommonRequestFieldsHolder : CommonRequestFields {
    //    override var rootRedisLogMessageID: String? = null
    override var databaseID: String? = null
    override var fakeEmail = false
    override lateinit var clientURL: String
}

class ProcedureContext {
    var wideClientKind by notNullOnce<WideClientKind>()
    var clientKind by notNullOnce<ClientKind>()
    var lang by notNullOnce<Language>()
    var clientDomain by notNullOnce<String>()
    var clientPortSuffix by notNullOnce<String>()
    var user_killme by notNullOnce<UserRTO>()
    var token by notNullOnce<String>()
    var hasUser by notNullOnce<Boolean>()
    var user: User? = null

    val fieldErrors = mutableListOf<FieldError>()

    val clientProtocol = "http" // TODO:vgrechka Switch everything to HTTPS
    val clientRootPath = ""

    val clientRoot get()= "$clientProtocol://$clientDomain$clientPortSuffix$clientRootPath"
}

enum class NeedsUser {
    YES, NO, MAYBE
}

fun userByToken2(token: String): User {
    val ut = backPlatform.userTokenRepo.findByToken(token) ?: bitch("Invalid token")
    return ut.user!!
}

fun systemDangerousToken(): String = sharedPlatform.getenv("APS_DANGEROUS_TOKEN") ?: die("I want APS_DANGEROUS_TOKEN environment variable")

fun bitchExpectedly(msg: String): Nothing {
    throw ExpectedRPCShit(msg)
}

object BackGlobus {
    val db = DB.testLocalMariaDB
//    val db = DB.apsTestOnTestServer

    var tracingEnabled = true
    lateinit var startMoment: XDate
    val slimJarName = "apsback-slim.jar"
    val killResponse = "Aarrgghh..."

//    val version by lazy {
//        this::class.java.classLoader.getResource("aps/version.txt").readText()
//    }

    val version: String get() = backPlatform.getResourceAsText("aps/version.txt")

    @Volatile var lastDownloadedPieceOfShit: PieceOfShitDownload? = null

    val rrlog = RRLog()
}

class ExpectedRPCShit(override val message: String) : Throwable(message)

@Ser @XXmlRootElement(name = "rrlog") @XXmlAccessorType(XXmlAccessType.FIELD)
class RRLog {
    @XXmlElement(name = "entry")
    val entries = XCollections.synchronizedList(mutableListOf<RRLogEntry>())
}

@Ser @XXmlRootElement @XXmlAccessorType(XXmlAccessType.FIELD)
class RRLogEntry(
    val id: Long,
    val pathInfo: String,
    val queryString: String,
    val requestJSON: String,
    val responseJSON: String)

interface MeganItem<out RTO> : ToRtoable<RTO> {
    val idBang: Long
}

interface ToRtoable<out RTO> {
    fun toRTO(searchWords: List<String>): RTO
}

val requestUserMaybe get() = backPlatform.requestGlobus.procedureCtx.user
val requestUserEntity get() = requestUserMaybe!!
val requestUser get() = requestUserEntity.user

abstract class BitchyProcedure {
    var bpc by notNullOnce<BitchyProcedureContext>()
    abstract fun serve()
}

class FuckDangerouslyParams<Req : RequestMatumba, out Res : CommonResponseFields>(
    val bpc: BitchyProcedureContext,
    val makeRequest: (ProcedureContext) -> Req,
    val runShit: (ProcedureContext, Req) -> Res)

fun <Req : RequestMatumba, Res : CommonResponseFields>
    fuckDangerously(p: FuckDangerouslyParams<Req, Res>)
{
    fuckSomeone(FuckSomeoneParams(
        bpc = p.bpc,
        req = p.makeRequest,
        runShit = p.runShit,
        wrapInFormResponse = false,
        needsDB = true,
        needsDangerousToken = true,
        needsUser = NeedsUser.NO,
        userKinds = setOf(),
        considerNextRequestTimestampFiddling = false,
        logRequestJSON = false
    ))
}

fun enhanceDB() {
    exhaustive=when (BackGlobus.db.engine) {
        DB.DatabaseEngine.POSTGRESQL -> {
            backPlatform.dbTransaction {shit->
                val forTest = true
                if (forTest) {
                    shit.executeUpdate("""
                drop function if exists ua_order_files__tsv_trigger();
            """)
                }

                shit.executeUpdate("""
            alter table ua_order_files add column tsv tsvector not null;
            create index ua_order_files__tsv_idx on ua_order_files using gin (tsv);

            create function ua_order_files__tsv_trigger() returns trigger as $$
            begin
              new.tsv \:=
                 setweight(to_tsvector('pg_catalog.russian', ' '
                     ||' '|| regexp_replace(coalesce(new.orderFile_name, ''), '\..*$', '')
                     ||' '|| coalesce(new.orderFile_title, '')
                     ),'A')
                 ||
                 setweight(to_tsvector('pg_catalog.russian', ' '
                     ||' '|| coalesce(new.orderFile_details, '')
                     ),'B')
              ;
              return new;
            end
            $$ language plpgsql;

            create trigger ua_order_files__tsvector_update
                before insert or update on ua_order_files
                for each row execute procedure ua_order_files__tsv_trigger();
        """)
            }
        }

        DB.DatabaseEngine.MARIADB -> {

        }
    }

    backPlatform.userRepo.save(User(UserFields(CommonFields(), firstName = "Anonymous", lastName = "Customer", email = "No fucking email", passwordHash = "No fucking password", profilePhone = "No fucking phone", kind = UserKind.CUSTOMER, state = UserState.COOL, adminNotes = "", subscribedToAllCategories = false))-{o->
        o.id = const.userID.anonymousCustomer
    })
    backPlatform.userRepo.save(User(UserFields(CommonFields(), firstName = "Anonymous", lastName = "Writer", email = "No fucking email", passwordHash = "No fucking password", profilePhone = "No fucking phone", kind = UserKind.WRITER, state = UserState.COOL, adminNotes = "", subscribedToAllCategories = false))-{o->
        o.id = const.userID.anonymousWriter
    })
}

object DB {
    val PORT_DEV = 5432   // On disk
    val PORT_TEST = 5433  // On memory drive
    val snapshotPrefix = "apsTestSnapshotOnTestServer-"

    val dbs = mutableListOf<Database>()
    //    val testTemplateUA1 = Database("testTemplateUA1", "127.0.0.1", PORT_TEST, "test-template-ua-1", "postgres", allowRecreation = true, populate = {q -> populate_testTemplateUA1(q)})
    val postgresOnTestServer = Database(DatabaseEngine.POSTGRESQL, "postgresOnTestServer", "127.0.0.1", PORT_TEST, "postgres", "postgres")
    val apsTestOnTestServer = Database(DatabaseEngine.POSTGRESQL, "apsTestOnTestServer", "127.0.0.1", PORT_TEST, "aps-test", "postgres", allowRecreation = true, correspondingAdminDB = postgresOnTestServer)
    val testLocalMariaDB = Database(DatabaseEngine.MARIADB, "testLocalMariaDB", "127.0.0.1", 3306, "aps-test", "aps-test", password = "aps-test")
    val postgresOnDevServer = Database(DatabaseEngine.POSTGRESQL, "postgresOnDevServer", "127.0.0.1", PORT_DEV, "postgres", "postgres")
    val localDevUA = Database(DatabaseEngine.POSTGRESQL, "localDevUA", "127.0.0.1", PORT_DEV, "aps-dev-ua", user = "postgres", password = null)
//    val bmix_fuckingAround_postgres by lazy {databaseFromEnv("bmix_fuckingAround_postgres")}
//    val bmix_fuckingAround_apsdevua by lazy {databaseFromEnv("bmix_fuckingAround_apsdevua", allowRecreation = true, correspondingAdminDB = bmix_fuckingAround_postgres)}

//    fun apsTestSnapshotOnTestServer(id: String) =
//        Database(snapshotPrefix + id, "127.0.0.1", PORT_TEST, snapshotPrefix + id, "postgres", allowRecreation = true, correspondingAdminDB = postgresOnTestServer)
//
//    val systemDatabases = mapOf(
//        PORT_DEV to postgresOnDevServer,
//        PORT_TEST to postgresOnTestServer
//    )

//    fun databaseFromEnv(id: String, allowRecreation: Boolean = false, correspondingAdminDB: Database? = null): Database {
//        val pname = "APS_DB_URI_$id"
//        val uri = System.getenv(pname) ?: wtf("I want env property $pname")
//        val mr = Regex("postgres://(.*?):(.*?)@(.*?):(.*?)/(.*?)").matchEntire(uri) ?: wtf("Bad database URI")
//        return Database(id,
//                        user = mr.groupValues[1],
//                        password = mr.groupValues[2],
//                        host = mr.groupValues[3],
//                        port = mr.groupValues[4].toInt(),
//                        name = mr.groupValues[5],
//                        allowRecreation = allowRecreation,
//                        correspondingAdminDB = correspondingAdminDB)
//    }

    fun byNameOnTestServer(name: String): Database =
        dbs.find {it.port == PORT_TEST && it.name == name} ?: wtf("No database [$name] on test server")

    fun byID(id: String): Database = when {
//        id == "bmix_fuckingAround_apsdevua" -> bmix_fuckingAround_apsdevua
        id == "apsTestOnTestServer" -> apsTestOnTestServer
        else -> wtf("No database with ID $id")
    }

    enum class DatabaseEngine {
        POSTGRESQL, MARIADB
    }

    class Database(
        val engine: DatabaseEngine,
        val id: String,
        val host: String, val port: Int, val name: String,
        val user: String, val password: String? = null,
        val allowRecreation: Boolean = false,
        val correspondingAdminDB: Database? = null
    ) {
        private val dslazy = relazy {backPlatform.makeDataSource(this)}

        init {
            dbs.add(this)
        }

        val ds: XDataSource by dslazy

        fun close() {
            ds.close()
            dslazy.reset()
        }

        override fun toString() = "Database(id='$id')"
    }
}


fun serveShit(req: XHttpServletRequest, res: XHttpServletResponse) {
    requestGlobusThreadLocal.set(RequestGlobusType())

    res.addHeader("Access-Control-Allow-Origin", "*")

    val pathInfo = run {
        val ss = req.queryString.split("=")
        check(ss.size == 2) {"6b774739-3c19-4cfd-b02f-487d98b4740f    req.queryString = [${req.queryString}]"}
        backPlatform.log.info("piiiiiiizdaaaaaaaaaaaaaaaaaa")
        check(ss[0] == "pathInfo") {"5144ffd0-9703-497b-82af-fa92e265b33c"}
        ss[1]
    }

//    try {
        when {
//            pathInfo == "/welcome" -> {
//                res.spitText("FUCK YOU")
//            }
//
//            pathInfo == "/startMoment" -> {
//                res.spitText(SimpleDateFormat("YYYYMMDD-hhmmss").format(BackGlobus.startMoment))
//            }
//
//            pathInfo == "/version" -> {
//                res.spitText(BackGlobus.version)
//            }

            pathInfo.startsWith("/rpc/") -> {
                val procedureName = pathInfo.substring("/rpc/".length)
                val servant = backPlatform.makeServant(procedureName)
                servant.bpc = BitchyProcedureContext(req, res)
                backPlatform.requestTransaction(pathInfo) {servant.serve()}
            }

            else -> bitch("Weird pathInfo: $pathInfo")
        }
//    } catch(fuckup: Throwable) {
//        backPlatform.log.error("Can't fucking service [$pathInfo]: ${fuckup.message}", fuckup)
//
//        if (fuckup is WithCulprit) {
//            backPlatform.log.section("Culprit:\n\n" + fuckup.culprit.constructionStack.joinToString("\n") {it.toString()})
//        }
//
//        throw XServletException(fuckup)
//    }
}






















