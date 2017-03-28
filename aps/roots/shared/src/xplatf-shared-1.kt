package aps

import kotlin.reflect.KProperty
import aps.Color.*

interface XSharedPlatform {
    fun currentTimeMillis(): Long
    fun getenv(name: String): String?
}

annotation class Dummy

val KOMMON_HOME: String get()= sharedPlatform.getenv("INTO_KOMMON_HOME") ?: die("I want INTO_KOMMON_HOME environment variable")

fun bitch(msg: String = "Just bitching..."): Nothing = throw Exception(msg)
fun imf(what: String = "me"): Nothing = throw Exception("Implement $what, please, fuck you")
fun wtf(msg: String = "...WTF didn't you describe this WTF?"): Nothing = throw Exception("WTF: $msg")
fun die(msg: String = "You've just killed me, motherfucker!"): Nothing = throw Exception("Aarrgghh... $msg")
fun fuckOff(msg: String = "Don't call me"): Nothing = throw Exception("Fuck off... $msg")
fun unsupported(what: String = "Didn't bother to describe"): Nothing = throw Exception("Unsupported: $what")

var exhaustive: Any? = null

fun <R> measuringAndPrinting(block: () -> R): R {
    val start = sharedPlatform.currentTimeMillis()
    val res = block()
    val ms = sharedPlatform.currentTimeMillis() - start
    println("COOL [${ms}ms]")
    return res
}

inline fun ifOrEmpty(test: Boolean, block: () -> String): String =
    if (test) block()
    else ""

inline fun <T> T.letu(block: (T) -> Unit): Unit = block(this)

class FieldError(val field: String, val error: String)

interface CommonResponseFields {
    var backendVersion: String
}



//------------------------- KOTLIN -------------------------

fun <T> lazy(initializer: () -> T): Lazy<T> = UnsafeLazyImpl(initializer)

private object UNINITIALIZED_VALUE

class UnsafeLazyImpl<out T>(initializer: () -> T) : Lazy<T> {
    private var initializer: (() -> T)? = initializer
    private var _value: Any? = UNINITIALIZED_VALUE

    override val value: T
        get() {
            if (_value === UNINITIALIZED_VALUE) {
                _value = initializer!!()
                initializer = null
            }
            @Suppress("UNCHECKED_CAST")
            return _value as T
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."

    private fun writeReplace(): Any = InitializedLazyImpl(value)
}

private class InitializedLazyImpl<out T>(override val value: T) : Lazy<T> {

    override fun isInitialized(): Boolean = true

    override fun toString(): String = value.toString()

}

inline fun check(value: Boolean): Unit = check(value) { "Check failed." }

//inline fun check(value: Boolean, lazyMessage: () -> Any): Unit {
//    if (!value) {
//        val message = lazyMessage()
//        throw IllegalStateException(message.toString())
//    }
//}

interface ReadOnlyProperty<in R, out T> {
    operator fun getValue(thisRef: R, property: KProperty<*>): T
}

interface ReadWriteProperty<in R, T> {
    operator fun getValue(thisRef: R, property: KProperty<*>): T
    operator fun setValue(thisRef: R, property: KProperty<*>, value: T)
}

fun <T: Any> notNull(): ReadWriteProperty<Any?, T> = NotNullVar()

private class NotNullVar<T: Any>() : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

fun <T: Any> notNullOnce(): ReadWriteProperty<Any?, T> = NotNullOnceVar()

private class NotNullOnceVar<T: Any> : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property `${property.name}` should be initialized before get.")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        check(this.value == null) {"Property `${property.name}` should be assigned only once"}
        this.value = value
    }
}

interface CommonRequestFields {
    //    var rootRedisLogMessageID: String?
    var databaseID: String?
    var fakeEmail: Boolean
    var clientURL: String
}

enum class UserKind {
    CUSTOMER, WRITER, ADMIN
}

enum class ClientKind {
    UA_CUSTOMER, UA_WRITER
}

sealed class WideClientKind {
    class User(val kind: ClientKind) : WideClientKind()
    class Test : WideClientKind()
}

enum class Language(val decimalPoint: String) {
    EN(decimalPoint = "."),
    UA(decimalPoint = ",")
}


object const {

    val moreableChunkSize = 10

    object text {
        val numberSign = t("#", "№")
        val na = t("N/A", "ХЗ")

        object symbols {
            val rightDoubleAngleQuotation = "»"
            val rightDoubleAngleQuotationSpaced = " » "
            val nl2 = "\n\n"
            val nbsp: String = "" + 0xa0.toChar()
            val emdash = "—"
            val endash = "–"
            val threeQuotes = "\"\"\""
            val times = "×"
        }

        object shebang {
            val defaultCancelButtonTitle = t("Nah", "Не надо")
        }
    }

    object msg {
        val noItems = t("TOTE", "Савсэм ничего нэт, да...")
        val serviceFuckedUp = t("Service is temporarily fucked up, sorry", "Сервис временно в жопе, просим прощения")
    }

    object orderArea {
        val admin = "Admin"
        val customer = "Customer"
        val writer = "Writer"
    }

    object windowMessage {
        val whatsUp = "What's up?"
        val fileForbidden = "Fucking file is forbidden"
    }

//    object common {
//        val minTitleLen = 5
//        val maxTitleLen = 100
//        val minDetailsLen = 5
//        val maxDetailsLen = 2000
//    }
//
//    object order {
//        val minTitleLen = common.minTitleLen
//        val maxTitleLen = common.maxTitleLen
//        val minDetailsLen = common.minDetailsLen
//        val maxDetailsLen = common.maxDetailsLen
//    }
//
//    object file {
//        val minTitleLen = common.minTitleLen
//        val maxTitleLen = common.maxTitleLen
//        val minDetailsLen = common.minDetailsLen
//        val maxDetailsLen = common.maxDetailsLen
//    }

    val topNavbarHeight = 50.0

    object urlq {
        object test {
            val test = "test"
            val testSuite = "testSuite"
            val stopOnAssertions = "stopOnAssertions"
            val dontStopOnCorrectAssertions  = "dontStopOnCorrectAssertions"
            val animateUserActions = "animateUserActions"
            val handPauses = "handPauses"
        }
    }

    object elementID {
        val dynamicFooter = "dynamicFooter"
        val cutLineContainer = "cutLineContainer"
    }

    object productName {
        val uaCustomer = "APS UA"
        val uaWriter = "Writer UA"
    }

    object file {
        val APS_HOME get()= sharedPlatform.getenv("APS_HOME") ?: die("I want APS_HOME environment variable")
        val GENERATOR_BAK_DIR get()= "c:/tmp/aps-bak" // TODO:vgrechka @unhardcode
        val TMPDIR get()= sharedPlatform.getenv("TMPDIR") ?: die("I want TMPDIR environment variable")
        val APS_CLOUD_BACK_HOST get() = sharedPlatform.getenv("APS_CLOUD_BACK_HOST") ?: die("I want APS_CLOUD_BACK_HOST environment variable")
        val APS_TEMP get()= "c:/tmp/aps-tmp" // TODO:vgrechka @unhardcode

        val testFiles get()= "$APS_HOME/back/testfiles"
    }

    object userID {
        //        val testScenario = -10L
        val anonymousCustomer = -20L
        val anonymousWriter = -30L
    }

    object uaDocumentCategoryID {
        val root = -10L
        val misc = -20L

        val economyGroup = -30L
        val auditing = -40L
        val banking = -50L
        val stockExchange = -60L
        val accounting = -70L
        val budgetSystem = -80L
        val currencyRegulation = -90L
        val currencyRelationships = -100L
        val moneyAndCredit = -110L
        val publicService = -120L
        val publicAdministration = -130L
        val publicFinances = -140L
        val documentManagement = -150L
        val econometrics = -160L
        val economy = -170L
        val enterpriseEconomics = -180L
        val laborEconomics = -190L
        val economicCybernetics = -200L
        val economicAnalysis = -210L
        val eCommerce = -220L
        val pr = -230L
        val foreignTradeActivities = -240L
        val investment = -250L
        val innovativeActivity = -260L
        val innovativeManagement = -270L
        val treasury = -280L
        val control = -290L
        val forestry = -300L
        val logistics = -310L
        val macroeconomics = -320L
        val marketingAndAdvertisement = -330L
        val management = -340L
        val customs = -350L
        val internationalEconomics = -360L
        val microeconomics = -370L
        val economicModeling = -380L
        val taxes = -390L
        val entrepreneurship = -400L
        val politicalEconomy = -410L
        val restaurantHotelBusinessAndConsumerService = -420L
        val securitiesMarket = -430L
        val locationOfProductiveForces = -440L
        val agriculture = -450L
        val standardizationAndQualityManagement = -460L
        val statistics = -470L
        val strategicManagement = -480L
        val insurance = -490L
        val commodityAndExpertise = -500L
        val tradeAndCommercialActivity = -510L
        val tourism = -520L
        val projectManagement = -530L
        val managementAccounting = -540L
        val finance = -550L
        val enterpriseFinance = -560L
        val financialAnalysis = -570L
        val financialManagement = -580L
        val pricing = -590L
        val naturalGroup = -600L
        val astronomy = -610L
        val biology = -620L
        val militaryFucking = -630L
        val geography = -640L
        val geodesy = -650L
        val geology = -660L
        val ecology = -670L
        val math = -680L
        val medicine = -690L
        val naturalHistory = -700L
        val pharmaceuticals = -710L
        val physics = -720L
        val chemistry = -730L
        val technicalGroup = -740L
        val aviationAndCosmonautics = -750L
        val architecture = -760L
        val databases = -770L
        val construction = -780L
        val electronics = -790L
        val electricalEngineering = -800L
        val informaticsAndComputing = -810L
        val informationSecurity = -820L
        val informationAnalyticalActivity = -830L
        val cybernetics = -840L
        val drawings = -850L
        val programming = -860L
        val technicalDesign = -870L
        val radioEngineering = -880L
        val theoreticalMechanics = -890L
        val theoryOfMechanismsAndMachines = -900L
        val heatEngineering = -910L
        val technologySystem = -920L
        val engineeringTechnology = -930L
        val cookingTechnology = -940L
        val transportConstruction = -950L
        val legalGroup = -960L
        val advocacy = -970L
        val administrativeLaw = -980L
        val arbitrationProceedings = -990L
        val economicLaw = -1000L
        val environmentalLaw = -1010L
        val housingLaw = -1020L
        val landLaw = -1030L
        val historyOfStateAndLaw = -1040L
        val constitutionalLaw = -1050L
        val corporateLaw = -1060L
        val forensics = -1070L
        val criminalLaw = -1080L
        val criminalProcess = -1090L
        val customsLaw = -1100L
        val internationalLaw = -1110L
        val municipalLaw = -1120L
        val notary = -1130L
        val businessLaw = -1140L
        val taxLaw = -1150L
        val law = -1160L
        val intellectualPropertyRights = -1170L
        val familyLaw = -1180L
        val insuranceLaw = -1190L
        val judicialAndLawEnforcementAgencies = -1200L
        val forensicMedicalExamination = -1210L
        val theoryOfStateAndLaw = -1220L
        val laborLaw = -1230L
        val financialLaw = -1240L
        val civilLaw = -1250L
        val humanitarianGroup = -1260L
        val analysisOfBankingActivities = -1270L
        val english = -1280L
        val lifeSafety = -1290L
        val design = -1300L
        val diplomacy = -1310L
        val aesthetics = -1320L
        val ethics = -1330L
        val journalismAndPublishing = -1340L
        val history = -1350L
        val localAreaStudies = -1360L
        val culture = -1370L
        val linguistics = -1380L
        val foreignLiterature = -1390L
        val russianLiterature = -1400L
        val ukrainianLiterature = -1410L
        val logic = -1420L
        val artAndCulture = -1430L
        val german = -1440L
        val pedagogy = -1450L
        val politicalScience = -1460L
        val psychology = -1470L
        val religion = -1480L
        val rhetoric = -1490L
        val russian = -1500L
        val socialWork = -1510L
        val sociology = -1520L
        val stylistics = -1530L
        val ukrainian = -1540L
        val sportsAndFucking = -1550L
        val philology = -1560L
        val philosophy = -1570L
        val phonetics = -1580L
        val french = -1590L
    }
}

sealed class FormResponse : CommonResponseFields {
    override lateinit var backendVersion: String

    class Hunky<Meat>(val meat: Meat): FormResponse()
    class Shitty(val error: String, val fieldErrors: List<FieldError>): FormResponse()
}

inline operator fun <T, FRet> T.minus(f: (T) -> FRet): T { f(this); return this }

class UserRTO(
    override var id: Long,
    override var title: String,
    override var titleHighlightRanges: List<IntRangeRTO>,
    val createdAt: Long,
    val updatedAt: Long,
    val profileUpdatedAt: Long?,
    val kind: UserKind,
    val lang: Language,
    val email: String,
    val state: UserState,
    val profileRejectionReason: String?,
    val banReason: String?,
    override var adminNotes: String,
    override var adminNotesHighlightRanges: List<IntRangeRTO>,
    val firstName: String,
    val lastName: String,
    val profilePhone: String,
    val aboutMe: String,
    val aboutMeHighlightRanges: List<IntRangeRTO>,
    val roles: Set<UserRole>,
    override var editable: Boolean,
    val allDocumentCategories: Boolean,
    val documentCategories: List<UADocumentCategoryRTO>
) : RTOWithAdminNotes, MelindaItemRTO, TabithaEntityRTO

data class PieceOfShitDownload(
    val id: Long,
    val name: String,
    val forbidden: Boolean,
    val sha1: String
)

annotation class NoArgCtor
annotation class AllOpen
annotation class Ser

enum class UserState(override val title: String, val icon: XIcon? = null, val style: String = "") : Titled {
    COOL(t("TOTE", "Прохладный"),                                emojis.tw.sunglasses, "background-color: $WHITE;"),
    PROFILE_PENDING(t("TOTE", "Без профиля"),                    emojis.one.nameBadge, "background-color: $GRAY_200;"),
    PROFILE_APPROVAL_PENDING(t("TOTE", "Ждет аппрува профиля"),  emojis.tw.clock8, "background-color: $ORANGE_100;"),
    PROFILE_REJECTED(t("TOTE", "Профиль завернут"),              emojis.one.fuckYou_medium, "background-color: $RED_100;"),
    BANNED(t("TOTE", "Забанен"),                                 emojis.tw.noEntry, "background-color: $RED_300;");
}

enum class Color(val string: String) {
    // https://www.google.com/design/spec/style/color.html#color-color-palette
    BLACK("#000000"), BLACK_BOOT("#333333"), WHITE("#ffffff"),
    RED_50("#ffebee"), RED_100("#ffcdd2"), RED_200("#ef9a9a"), RED_300("#e57373"), RED_400("#ef5350"), RED_500("#f44336"), RED_600("#e53935"), RED_700("#d32f2f"), RED_800("#c62828"), RED_900("#b71c1c"), RED_A100("#ff8a80"), RED_A200("#ff5252"), RED_A400("#ff1744"), RED_A700("#d50000"),
    PINK_50("#fce4ec"), PINK_100("#f8bbd0"), PINK_200("#f48fb1"), PINK_300("#f06292"), PINK_400("#ec407a"), PINK_500("#e91e63"), PINK_600("#d81b60"), PINK_700("#c2185b"), PINK_800("#ad1457"), PINK_900("#880e4f"), PINK_A100("#ff80ab"), PINK_A200("#ff4081"), PINK_A400("#f50057"), PINK_A700("#c51162"),
    PURPLE_50("#f3e5f5"), PURPLE_100("#e1bee7"), PURPLE_200("#ce93d8"), PURPLE_300("#ba68c8"), PURPLE_400("#ab47bc"), PURPLE_500("#9c27b0"), PURPLE_600("#8e24aa"), PURPLE_700("#7b1fa2"), PURPLE_800("#6a1b9a"), PURPLE_900("#4a148c"), PURPLE_A100("#ea80fc"), PURPLE_A200("#e040fb"), PURPLE_A400("#d500f9"), PURPLE_A700("#aa00ff"),
    DEEP_PURPLE_50("#ede7f6"), DEEP_PURPLE_100("#d1c4e9"), DEEP_PURPLE_200("#b39ddb"), DEEP_PURPLE_300("#9575cd"), DEEP_PURPLE_400("#7e57c2"), DEEP_PURPLE_500("#673ab7"), DEEP_PURPLE_600("#5e35b1"), DEEP_PURPLE_700("#512da8"), DEEP_PURPLE_800("#4527a0"), DEEP_PURPLE_900("#311b92"), DEEP_PURPLE_A100("#b388ff"), DEEP_PURPLE_A200("#7c4dff"), DEEP_PURPLE_A400("#651fff"), DEEP_PURPLE_A700("#6200ea"),
    INDIGO_50("#e8eaf6"), INDIGO_100("#c5cae9"), INDIGO_200("#9fa8da"), INDIGO_300("#7986cb"), INDIGO_400("#5c6bc0"), INDIGO_500("#3f51b5"), INDIGO_600("#3949ab"), INDIGO_700("#303f9f"), INDIGO_800("#283593"), INDIGO_900("#1a237e"), INDIGO_A100("#8c9eff"), INDIGO_A200("#536dfe"), INDIGO_A400("#3d5afe"), INDIGO_A700("#304ffe"),
    BLUE_50("#e3f2fd"), BLUE_100("#bbdefb"), BLUE_200("#90caf9"), BLUE_300("#64b5f6"), BLUE_400("#42a5f5"), BLUE_500("#2196f3"), BLUE_600("#1e88e5"), BLUE_700("#1976d2"), BLUE_800("#1565c0"), BLUE_900("#0d47a1"), BLUE_A100("#82b1ff"), BLUE_A200("#448aff"), BLUE_A400("#2979ff"), BLUE_A700("#2962ff"),
    LIGHT_BLUE_50("#e1f5fe"), LIGHT_BLUE_100("#b3e5fc"), LIGHT_BLUE_200("#81d4fa"), LIGHT_BLUE_300("#4fc3f7"), LIGHT_BLUE_400("#29b6f6"), LIGHT_BLUE_500("#03a9f4"), LIGHT_BLUE_600("#039be5"), LIGHT_BLUE_700("#0288d1"), LIGHT_BLUE_800("#0277bd"), LIGHT_BLUE_900("#01579b"), LIGHT_BLUE_A100("#80d8ff"), LIGHT_BLUE_A200("#40c4ff"), LIGHT_BLUE_A400("#00b0ff"), LIGHT_BLUE_A700("#0091ea"),
    CYAN_50("#e0f7fa"), CYAN_100("#b2ebf2"), CYAN_200("#80deea"), CYAN_300("#4dd0e1"), CYAN_400("#26c6da"), CYAN_500("#00bcd4"), CYAN_600("#00acc1"), CYAN_700("#0097a7"), CYAN_800("#00838f"), CYAN_900("#006064"), CYAN_A100("#84ffff"), CYAN_A200("#18ffff"), CYAN_A400("#00e5ff"), CYAN_A700("#00b8d4"),
    TEAL_50("#e0f2f1"), TEAL_100("#b2dfdb"), TEAL_200("#80cbc4"), TEAL_300("#4db6ac"), TEAL_400("#26a69a"), TEAL_500("#009688"), TEAL_600("#00897b"), TEAL_700("#00796b"), TEAL_800("#00695c"), TEAL_900("#004d40"), TEAL_A100("#a7ffeb"), TEAL_A200("#64ffda"), TEAL_A400("#1de9b6"), TEAL_A700("#00bfa5"),
    GREEN_50("#e8f5e9"), GREEN_100("#c8e6c9"), GREEN_200("#a5d6a7"), GREEN_300("#81c784"), GREEN_400("#66bb6a"), GREEN_500("#4caf50"), GREEN_600("#43a047"), GREEN_700("#388e3c"), GREEN_800("#2e7d32"), GREEN_900("#1b5e20"), GREEN_A100("#b9f6ca"), GREEN_A200("#69f0ae"), GREEN_A400("#00e676"), GREEN_A700("#00c853"),
    LIGHT_GREEN_50("#f1f8e9"), LIGHT_GREEN_100("#dcedc8"), LIGHT_GREEN_200("#c5e1a5"), LIGHT_GREEN_300("#aed581"), LIGHT_GREEN_400("#9ccc65"), LIGHT_GREEN_500("#8bc34a"), LIGHT_GREEN_600("#7cb342"), LIGHT_GREEN_700("#689f38"), LIGHT_GREEN_800("#558b2f"), LIGHT_GREEN_900("#33691e"), LIGHT_GREEN_A100("#ccff90"), LIGHT_GREEN_A200("#b2ff59"), LIGHT_GREEN_A400("#76ff03"), LIGHT_GREEN_A700("#64dd17"),
    LIME_50("#f9fbe7"), LIME_100("#f0f4c3"), LIME_200("#e6ee9c"), LIME_300("#dce775"), LIME_400("#d4e157"), LIME_500("#cddc39"), LIME_600("#c0ca33"), LIME_700("#afb42b"), LIME_800("#9e9d24"), LIME_900("#827717"), LIME_A100("#f4ff81"), LIME_A200("#eeff41"), LIME_A400("#c6ff00"), LIME_A700("#aeea00"),
    YELLOW_50("#fffde7"), YELLOW_100("#fff9c4"), YELLOW_200("#fff59d"), YELLOW_300("#fff176"), YELLOW_400("#ffee58"), YELLOW_500("#ffeb3b"), YELLOW_600("#fdd835"), YELLOW_700("#fbc02d"), YELLOW_800("#f9a825"), YELLOW_900("#f57f17"), YELLOW_A100("#ffff8d"), YELLOW_A200("#ffff00"), YELLOW_A400("#ffea00"), YELLOW_A700("#ffd600"),
    AMBER_50("#fff8e1"), AMBER_100("#ffecb3"), AMBER_200("#ffe082"), AMBER_300("#ffd54f"), AMBER_400("#ffca28"), AMBER_500("#ffc107"), AMBER_600("#ffb300"), AMBER_700("#ffa000"), AMBER_800("#ff8f00"), AMBER_900("#ff6f00"), AMBER_A100("#ffe57f"), AMBER_A200("#ffd740"), AMBER_A400("#ffc400"), AMBER_A700("#ffab00"),
    ORANGE_50("#fff3e0"), ORANGE_100("#ffe0b2"), ORANGE_200("#ffcc80"), ORANGE_300("#ffb74d"), ORANGE_400("#ffa726"), ORANGE_500("#ff9800"), ORANGE_600("#fb8c00"), ORANGE_700("#f57c00"), ORANGE_800("#ef6c00"), ORANGE_900("#e65100"), ORANGE_A100("#ffd180"), ORANGE_A200("#ffab40"), ORANGE_A400("#ff9100"), ORANGE_A700("#ff6d00"),
    DEEP_ORANGE_50("#fbe9e7"), DEEP_ORANGE_100("#ffccbc"), DEEP_ORANGE_200("#ffab91"), DEEP_ORANGE_300("#ff8a65"), DEEP_ORANGE_400("#ff7043"), DEEP_ORANGE_500("#ff5722"), DEEP_ORANGE_600("#f4511e"), DEEP_ORANGE_700("#e64a19"), DEEP_ORANGE_800("#d84315"), DEEP_ORANGE_900("#bf360c"), DEEP_ORANGE_A100("#ff9e80"), DEEP_ORANGE_A200("#ff6e40"), DEEP_ORANGE_A400("#ff3d00"), DEEP_ORANGE_A700("#dd2c00"),
    BROWN_50("#efebe9"), BROWN_100("#d7ccc8"), BROWN_200("#bcaaa4"), BROWN_300("#a1887f"), BROWN_400("#8d6e63"), BROWN_500("#795548"), BROWN_600("#6d4c41"), BROWN_700("#5d4037"), BROWN_800("#4e342e"), BROWN_900("#3e2723"),
    GRAY_50("#fafafa"), GRAY_100("#f5f5f5"), GRAY_200("#eeeeee"), GRAY_300("#e0e0e0"), GRAY_400("#bdbdbd"), GRAY_500("#9e9e9e"), GRAY_600("#757575"), GRAY_700("#616161"), GRAY_800("#424242"), GRAY_900("#212121"),
    BLUE_GRAY_50("#eceff1"), BLUE_GRAY_100("#cfd8dc"), BLUE_GRAY_200("#b0bec5"), BLUE_GRAY_300("#90a4ae"), BLUE_GRAY_400("#78909c"), BLUE_GRAY_500("#607d8b"), BLUE_GRAY_600("#546e7a"), BLUE_GRAY_700("#455a64"), BLUE_GRAY_800("#37474f"), BLUE_GRAY_900("#263238"),
    RED("red"), GREEN("green"), BLUE("blue"), ROSYBROWN("rosybrown"),;

    override fun toString() = string
}

interface Titled {
    val title: String
}

object emojis {
    object tw {
        val sunglasses = Twemoji("1f60e")
        val fuckYou_medium = Twemoji("1f595-1f3fd")
        val shit = Twemoji("1f4a9")
        val clock8 = Twemoji("1f557")
        val noEntry = Twemoji("26d4")
    }

    object one {
        val nameBadge = EmojiOne("1f4db")
        val fuckYou_medium = EmojiOne("1f595-1f3fd")
    }

}

class UserParamsHistoryItemRTO(
    val entity: UserRTO,
    val descr: String,

    // HistoryItemRTOFields
    override val createdAt: Long,
    override val requester: UserRTO,
    override val thenRequester: UserRTO,

    // MelindaItemRTO
    override val id: Long,
    override val title: String,
    override val editable: Boolean,
    override val titleHighlightRanges: List<IntRangeRTO>
) : MelindaItemRTO, HistoryItemRTOFields

enum class UADocumentType(override val title: String) : Titled {
    ABSTRACT(t("TOTE", "Реферат")),
    COURSE(t("TOTE", "Курсовая работа")),
    GRADUATION(t("TOTE", "Дипломная работа")),
    LAB(t("TOTE", "Лабораторная работа")),
    TEST(t("TOTE", "Контрольная работа")),
    RGR(t("TOTE", "РГР")),
    DRAWING(t("TOTE", "Чертеж")),
    DISSERTATION(t("TOTE", "Диссертация")),
    ESSAY(t("TOTE", "Эссе (сочинение)")),
    PRACTICE(t("TOTE", "Отчет по практике")),
    OTHER(t("TOTE", "Другое"))
}

enum class UAOrderState(override val title: String, val icon: XIcon? = null, val style: String = "") : Titled {
    CREATED(t("TOTE", "Создан"),                                            null, "background-color: green;"),
    CUSTOMER_DRAFT(t("TOTE", "Черновик"),                                   null, "background-color: green;"),
    LOOKING_WRITERS(t("TOTE", "Ищем писателей"),                            null, "background-color: green;"),
    WAITING_PAYMENT(t("TOTE", "Ждем оплаты"),                               null, "background-color: green;"),
    WRITER_ASSIGNED(t("TOTE", "Писатель назначен"),                         null, "background-color: green;"),
    WAITING_EMAIL_CONFIRMATION(t("TOTE", "Ждем подтверждения имейла"),      null, "background-color: green;"),
    WAITING_ADMIN_APPROVAL(t("TOTE", "Ждем одобрения админом"),             null, "background-color: $AMBER_100;"),
    RETURNED_TO_CUSTOMER_FOR_FIXING(t("TOTE", "Заказчик фиксит заявку"),    null, "background-color: $RED_50;"),
    IN_STORE(t("TOTE", "Ищем писателей"),                                   null, "background-color: $BLUE_100;")
}

class UAOrderRTO(
    override var id: Long,
    override var title: String,
    override var editable: Boolean,
    override var titleHighlightRanges: List<IntRangeRTO>,
    val createdAt: Long,
    val updatedAt: Long,
    val customer: UserRTO,
    val documentType: UADocumentType,
    val price: Int?,
    val numPages: Int,
    val numSources: Int,
    val details: String,
    val detailsHighlightRanges: List<IntRangeRTO>,
    override var adminNotes: String,
    override var adminNotesHighlightRanges: List<IntRangeRTO>,
    val state: UAOrderState,
    val customerPhone: String,
    val customerFirstName: String,
    val customerLastName: String,
    val whatShouldBeFixedByCustomer: String?,
    val customerEmail: String,
    var movedToStoreAt: Long?,
    var minAllowedPriceOffer: Int,
    var maxAllowedPriceOffer: Int,
    var minAllowedDurationOffer: Int,
    var maxAllowedDurationOffer: Int,
    val documentCategory: UADocumentCategoryRTO,
    val myBid: BidRTO?,
    val bidsSummary: BidsSummaryRTO?
) : MelindaItemRTO, RTOWithAdminNotes, TabithaEntityRTO

class ValueAndWhetherMineRTO<out T>(val value: T, val mine: Boolean)

class BidsSummaryRTO(
    val numParticipants: Int,
    val firstBidAt: ValueAndWhetherMineRTO<Long>,
    val lastBidAt: ValueAndWhetherMineRTO<Long>,
    val minPriceOffer: ValueAndWhetherMineRTO<Int>,
    val maxPriceOffer: ValueAndWhetherMineRTO<Int>,
    val minDurationOffer: ValueAndWhetherMineRTO<Int>,
    val maxDurationOffer: ValueAndWhetherMineRTO<Int>
)

class UAOrderFileRTO(
    override var id: Long,
    val seenAsFrom: UserKind,
    override var editable: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val name: String,
    val nameHighlightRanges: List<IntRangeRTO>,
    override var title: String,
    override var titleHighlightRanges: List<IntRangeRTO>,
    val details: String,
    val detailsHighlightRanges: List<IntRangeRTO>,
    override var adminNotes: String,
    override var adminNotesHighlightRanges: List<IntRangeRTO>,
    val sizeBytes: Int
) : MelindaItemRTO, RTOWithAdminNotes

interface TabithaEntityRTO {
    val id: Long
}

data class IntRangeRTO(
    val start: Int,
    val endInclusive: Int
)

interface MelindaItemRTO {
    val id: Long
    val title: String
    val editable: Boolean
    val titleHighlightRanges: List<IntRangeRTO>
}

interface RTOWithAdminNotes {
    val adminNotes: String
    val adminNotesHighlightRanges: List<IntRangeRTO>
}

interface HistoryItemRTOFields {
    val createdAt: Long
    val requester: UserRTO
    val thenRequester: UserRTO
}


class UADocumentCategoryRTO(
    val id: Long,
    val title: String,
    val pathTitle: String,
    val children: List<UADocumentCategoryRTO>
)

class BidRTO(
    override var id: Long,
    override var title: String,
    override var editable: Boolean,
    override var titleHighlightRanges: List<IntRangeRTO>,
    val createdAt: Long,
    val updatedAt: Long,
    override var adminNotes: String,
    override var adminNotesHighlightRanges: List<IntRangeRTO>,
    val priceOffer: Int,
    val durationOffer: Int,
    val comment: String
) : MelindaItemRTO, RTOWithAdminNotes

enum class UserRole {
    SUPPORT
}

fun String.chopOffFileExtension(): String =
    substring(0, lastIndexOfOrNull(".") ?: length)

fun String.lastIndexOfOrNull(s: String): Int? {
    val index = lastIndexOf(s)
    return if (index == -1) null else index
}

class RecreateTestDatabaseSchemaRequest() : RequestMatumba() {
}

class GenericResponse : CommonResponseFieldsImpl()

class relazy<out T>(val initializer: () -> T) {
    private var backing = lazy(initializer)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = backing.value

    fun reset() {
        backing = lazy(initializer)
    }
}


abstract class CommonResponseFieldsImpl : CommonResponseFields {
    override lateinit var backendVersion: String
}




















