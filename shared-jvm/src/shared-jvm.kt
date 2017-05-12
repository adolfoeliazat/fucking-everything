package vgrechka

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Equivalence
import com.google.common.collect.MapMaker
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.annotation.AnnotationDescription
import net.bytebuddy.description.modifier.Ownership
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.implementation.MethodDelegation
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import java.io.*
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.*
import kotlin.reflect.jvm.isAccessible


var exhaustive: Any? = null

fun clog(vararg xs: Any?): Unit = println(xs.joinToString(" "))
fun wtf(msg: String = "...WTF didn't you describe this WTF?"): Nothing = throw Exception("WTF: $msg")
fun die(msg: String = "You've just killed me, motherfucker!"): Nothing = throw Exception("Aarrgghh... $msg")
fun imf(what: String = "me"): Nothing = throw Exception("Implement $what, please, fuck you")
fun bitch(msg: String = "Just bitching..."): Nothing = throw Exception(msg)

fun clogSection(vararg xs: Any?): Unit = println("\n------------- " + xs.joinToString(" ") + " -------------\n")

data class RunProcessResult(
    val exitValue: Int,
    val stdout: String,
    val stderr: String
)


/**
 * Fuck thisy builders
 */
fun stringBuild(block: (StringBuilder) -> Unit) =
    StringBuilder().also(block).toString()

annotation class Ser
annotation class AllOpen

inline operator fun <T, FRet> T.minus(f: (T) -> FRet): T { f(this); return this }

fun Boolean.thenElseEmpty(block: () -> String): String = when {
    this -> block()
    else -> ""
}

fun <T> Boolean.thenElseNull(block: () -> T): T? = when {
    this -> block()
    else -> null
}

//fun <T: Any> notNullOnce(): ReadWriteProperty<Any?, T> = NotNullOnceVar()

//private class NotNullOnceVar<T: Any> : ReadWriteProperty<Any?, T> {
class notNullOnce<T: Any> : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property `${property.name}` should be initialized before get.")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        check(this.value == null) {"Property `${property.name}` should be assigned only once"}
        this.value = value
    }

    companion object {
        fun debugReset(prop: KMutableProperty0<*>) {
            prop.isAccessible = true
            val delegate = prop.getDelegate() as notNullOnce<*>
            delegate.value = null
        }
    }
}


class AttachedComputedShit<in Host : Any, out Shit>(val weak: Boolean = false,
                                                    val create: (Host) -> Shit) : ReadOnlyProperty<Host, Shit> {
    override fun getValue(thisRef: Host, property: KProperty<*>): Shit {
        val map = when {
            weak -> weak_thisRefToPropertyNameToValue
            else -> thisRefToPropertyNameToValue
        }
        val propertyNameToValue = map.computeIfAbsent(thisRef) {
            MapMaker().makeMap()
        }
        val res = propertyNameToValue.computeIfAbsent(property.name) {
            val xxx = create(thisRef) ?: NULL
            xxx
        }
        @Suppress("UNCHECKED_CAST")
        return (if (res === NULL) null else res) as Shit
    }

    companion object {
        private val NULL = Any()
        private val thisRefToPropertyNameToValue = makeMap(weak = false)
        private val weak_thisRefToPropertyNameToValue = makeMap(weak = true)

        private fun makeMap(weak: Boolean): ConcurrentMap<Any, MutableMap<String, Any>> {
            var mapMaker = MapMaker()

            if (weak) {
                mapMaker = mapMaker.weakKeys()
            }

            val keyEquivalenceMethod = mapMaker::class.java.getDeclaredMethod("keyEquivalence", Equivalence::class.java)
            keyEquivalenceMethod.isAccessible = true
            mapMaker = keyEquivalenceMethod.invoke(mapMaker, Equivalence.identity()) as MapMaker

            return mapMaker.makeMap<Any, MutableMap<String, Any>>()
        }

        object debug {
            val totalShitInMaps get() = thisRefToPropertyNameToValue.size + weak_thisRefToPropertyNameToValue.size

            fun reset() {
                thisRefToPropertyNameToValue.clear()
                weak_thisRefToPropertyNameToValue.clear()
            }

            fun testGCReclaimsAllShit() {
                System.gc()

                // XXX After enough writes freaking map finally reclaims entries with GCed keys.
                //     Using MapMakerInternalMap$CleanupMapTask is not reliable -- sometimes work, sometimes not
                val enoughWrites = 1000

                object {
                    inner class Junk
                    val Junk.moreJunk by AttachedComputedShit<Junk, String>(weak = true) {"more junk"}

                    init {
                        val refHolder = mutableListOf<Junk>()
                        for (i in 1..enoughWrites) {
                            val newShit = Junk()
                            refHolder += newShit
                            newShit.moreJunk // Puts new entry into the map
                        }
                    }
                }

                assertEquals("Only newly added junk is there", enoughWrites, totalShitInMaps)
                reset() // Make it virgin again
            }
        }
    }
}

fun <T: Any> mere(value: T) = object:ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}

class relazy<out T>(val initializer: () -> T) {
    private var backing = lazy(initializer)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = backing.value

    fun reset() {
        backing = lazy(initializer)
    }

    companion object {
        fun reset(prop: KProperty0<Any?>) {
            val delegate = prop.also{it.isAccessible = true}.getDelegate() as relazy<*>
            delegate.reset()
        }
    }
}


object HTTPClient {
    object MediaTypeName {
        val JSON = "application/json"
        val XML = "application/xml"
    }

    fun post(mediaTypeName: String, url: String, content: String, readTimeoutSeconds: Long? = null): String {
        val mediaType = MediaType.parse(mediaTypeName + "; charset=utf-8")
        val client = OkHttpClient.Builder()
            .readTimeout(readTimeoutSeconds ?: 5, TimeUnit.SECONDS)
            .build()
        val body = RequestBody.create(mediaType, content)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        val code = response.code()
        if (code != 200)
            bitch("Shitty HTTP response code: $code")

        val charset = Charset.forName("UTF-8")
        return response.body().source().readString(charset)
    }
}

val relaxedObjectMapper by lazy {
    ObjectMapper()-{o->
        o.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        o.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    }
}

fun dontRun(block: () -> Unit) {}

val Throwable.stackTraceString: String get() {
    val sw = StringWriter()
    this.printStackTrace(PrintWriter(sw))
    return sw.toString()
}

@Ser data class FileLine(val file: String, val line: Int)
data class FileLineColumn(val file: String, val line: Int, val column: Int = 1)

operator fun StringBuilder.plusAssign(x: Any?) {
    this.append(x)
}

object TestPile {
    object existingPizdaFile {
        fun big(): File {
            val file = File(BigPile.shitForTestsBigRoot + "/existing-pizda.txt")
            check(file.readText() == "I exist in big-file repository for the sake of tests, don't touch me") {"00a5878c-b6b9-410d-8300-4eda095ce8c3"}
            return file
        }

        fun small(): File {
            val file = File(BigPile.shitForTestsSmallRoot + "/existing-pizda.txt")
            check(file.readText() == "I exist in small-file repository for the sake of tests, don't touch me") {"0bcaf93a-b1e2-457e-ba09-f5c881c8e11d"}
            return file
        }
    }


    class SuiteMakerImpl<BaseSUT : Any>(val suiteName: String, val makeBaseSUT: () -> BaseSUT) : SuiteMaker<BaseSUT> {
        // This is for jumping to source using IDE
        val pieceOfStack = "Suite at " + Exception().stackTrace[2]

        var testClassBuilder: DynamicType.Builder<Any> = ByteBuddy().subclass(Any::class.java)!!
        var casesWereDefined = false
        val childGeneratedClasses = mutableListOf<Class<*>>()

        companion object {
            private var counter = 0
            fun generateName(suggested: String): String {
                val res = suggested + "_" + (++counter) + "__"
//                run {
//                    if (res.contains("7"))
//                        "break on me"
//                }
                return res
            }
        }

        fun generateClasses(): Array<Class<*>> {
            val testClass = casesWereDefined.thenElseNull {
                testClassBuilder
                    .name(generateName(suiteName))
                    .defineMethod("beforeClass", Void.TYPE, Visibility.PUBLIC, Ownership.STATIC)
                    .intercept(MethodDelegation.to(object {
                        @Suppress("unused")
                        fun invokeBeforeClass() {
//                            System.err.println(pieceOfStack)
                        }
                    }))
                    .annotateMethod(AnnotationDescription.Builder.ofType(BeforeClass::class.java).build())
                    .make()
                    .load(this::class.java.classLoader)
                    .loaded
            }

            if (testClass != null && childGeneratedClasses.isEmpty()) {
                return arrayOf(testClass)
            } else {
                val allChildClasses = mutableListOf<Class<*>>()
                if (testClass != null)
                    allChildClasses += testClass
                allChildClasses += childGeneratedClasses
                return allChildClasses.toTypedArray()
            }
        }

        fun generateClass(): Class<*> {
            val classes = generateClasses()
            if (classes.size == 1)
                return classes.first()
            else
                return ByteBuddy()
                    .subclass(Any::class.java)
                    .annotateType(AnnotationDescription.Builder
                                      .ofType(RunWith::class.java)
                                      .define("value", Suite::class.java).build())
                    .annotateType(AnnotationDescription.Builder
                                      .ofType(Suite.SuiteClasses::class.java)
                                      .defineTypeArray("value", *classes).build())
                    .name(generateName(suiteName))
                    .make()
                    .load(this::class.java.classLoader)
                    .loaded
        }

        override fun case(name: String, exercise: (BaseSUT) -> Unit) {
            // This is for jumping to source using IDE
            val pieceOfStack = StringBuilder()
            val trace = Exception().stackTrace
            pieceOfStack += "Case at " + trace[1] + "\n"
            for (item in trace.drop(2)) {
                if (item.toString().contains(TestPile::generateJUnitSuiteClassesFromBuilder.name))
                    break
                else
                    pieceOfStack += "        $item\n"
            }

            testClassBuilder = testClassBuilder
                .defineMethod(toMachineName(name), Void.TYPE, Visibility.PUBLIC)
                .intercept(MethodDelegation.to(object {
                    @Suppress("unused")
                    fun invokeTestMethod() {
                        clog(pieceOfStack)
                        exercise(makeBaseSUT())
                    }
                }))
                .annotateMethod(AnnotationDescription.Builder.ofType(Test::class.java).build())
            casesWereDefined = true
        }

        override fun <SUT : Any> suiteFor(makeSUT: (BaseSUT) -> SUT, prefix: String, build: (SuiteMaker<SUT>) -> Unit) {
            val newSUTMaker = {makeSUT(makeBaseSUT())}
            val childSuiteMaker = SuiteMakerImpl(prefix + objectNameFromMaker(newSUTMaker), newSUTMaker)
            build(childSuiteMaker)
            childGeneratedClasses += childSuiteMaker.generateClass()
        }

        override fun suite(suiteName: String, build: (SuiteMaker<BaseSUT>) -> Unit) {
            val childSuiteMaker = SuiteMakerImpl(toMachineName(suiteName), makeBaseSUT)
            build(childSuiteMaker)
            childGeneratedClasses += childSuiteMaker.generateClass()
        }

        private fun toMachineName(suiteName: String): String {
            val machineSuiteName = suiteName
                .replace(Regex("\\s+"), "_")
                .replace(Regex("[.,-:!;']"), "")
            return machineSuiteName
        }

        private fun objectNameFromMaker(make: () -> Any): String {
            val sampleSUT = make()
            var name = sampleSUT.javaClass.name
            val lastDot = name.lastIndexOf(".")
            if (lastDot != -1) {
                name = name.substring(lastDot + 1)
            }
            name = name.replace("$", "_")
            return name
        }
    }

    fun generateJUnitSuiteClassesFromBuilder(clazz: Class<*>): Array<Class<*>> {
        val clientSideBuilder = clazz.newInstance() as SuiteMakerClient
        val suiteName = clazz.name.replace("$", "_")
        val ourSideBuilder = SuiteMakerImpl(suiteName) {Unit}
        clientSideBuilder.build(ourSideBuilder)
        return ourSideBuilder.generateClasses()
    }
}

object AssertPile {
    fun thrown(assert: (Throwable) -> Unit, block: () -> Unit) {
        var thrown = false
        try {
            block()
        } catch (e: Throwable) {
            thrown = true
            assert(e)
        }
        assertTrue("Expected something to be thrown", thrown)
    }

    fun <T : Throwable> thrown(clazz: KClass<T>, block: () -> Unit) {
        thrown({assertEquals(clazz, it::class)}, block)
    }

    fun thrownExceptionOrOneOfItsCausesMessageContains(needle: String, block: () -> Unit) {
        thrown(assert = {
            val messages = mutableListOf<String?>()
            var lookingAt: Throwable? = it
            while (lookingAt != null) {
                messages.add(lookingAt.message)
                lookingAt = lookingAt.cause
            }
            if (!messages.any {it?.contains(needle) == true})
                fail("Thrown exception (or one of its causes) message should contain `$needle`.\n"
                         + "Instead got following messages:\n"
                         + messages.map{"    $it"}.joinToString("\n"))
        }, block = block)
    }

    fun thrownContainsUUID(uuidMangledForTests: String, block: () -> Unit) {
        val mangler = "--testref--"
        val manglerIndex = uuidMangledForTests.indexOf(mangler)
        check(manglerIndex == 1) {"ff82815b-c0c4-486c-9a36-d97723406715"}

        val uuid = uuidMangledForTests[0] + uuidMangledForTests.substring(manglerIndex + mangler.length)
        thrown(block = block, assert = {
            assertTrue("Expecting exception message\nto contain `$uuid`,\nbut got `${it.message}`",
                       it.message?.contains(uuid) == true)
        })
    }
}


interface TestLoggerPrintln {
    fun println(value: Any?)
}

class TestLogger {
    val lines = mutableListOf<Line>()
    val loopLoggingIndices = mutableListOf<LoopLoggingIndex>()

    class LoopLoggingIndex(var index: Int)

    interface Line {
        fun usefulLength(): Int
        fun renderTo(buf: StringBuilder, width: Int)
    }

    fun println(value: Any?, uuid: String) {
        clog(value)
        val stringValue = value.toString() // Capture now, as `value` object may change later before rendering
        lines += object:Line {
            override fun usefulLength(): Int {
                return stringValue.length
            }

            override fun renderTo(buf: StringBuilder, width: Int) {
                val spaces = width - usefulLength()
                buf += stringValue + " ".repeat(spaces) + uuid + "\n"
            }
        }
    }

    fun section(title: String, uuid: String) {
        val content = "------------- $title -------------"
        clog("\n$content\n")
        lines += object:Line {
            override fun usefulLength(): Int {
                return content.length
            }

            override fun renderTo(buf: StringBuilder, width: Int) {
                val spaces = width - usefulLength()
                buf += "\n" + content + " ".repeat(spaces) + uuid + "\n\n"
            }
        }
    }

    fun shitWritten(): String {
        val longestLine = lines.maxBy {it.usefulLength()} ?: return ""
        return stringBuild {
            val width = longestLine.usefulLength() + 4
            for (line in lines) {
                line.renderTo(it, width)
            }
        }
    }

    fun <T> forEach(xs: Iterable<T>, uuid: String, block: (T, TestLoggerPrintln) -> Unit) {
        val lli = LoopLoggingIndex(-100)
        loopLoggingIndices += lli

        for ((i, x) in xs.withIndex()) {
            lli.index = i
            block(x, object : TestLoggerPrintln {
                override fun println(value: Any?) {
                    val indexPrefix = loopLoggingIndices.map{it.index}.joinToString("-")
                    this@TestLogger.println(value, "$indexPrefix--$uuid")
                }
            })
        }

        loopLoggingIndices.removeAt(loopLoggingIndices.lastIndex)
    }

    fun assertEquals(expected: String) {
        val shitted = shitWritten()
        val preparedExpected = expected.trim().replace("\r\n", "\n")
        val preparedActual = shitted.trim().replace("\r\n", "\n")
        if (preparedExpected != preparedActual) {
            clog("\n-------------- SHITTED TO LOG ------------------\n")
            clog(shitted)
        }
        assertEquals(preparedExpected, preparedActual)
    }
}

fun Any?.toVerticalString(): String {
    val buf = StringBuilder()
    val s = this.toString()
    var indent = 0
    var i = 0
    while (i < s.length) {
        if (s[i] == '(') {
            ++indent
            buf += "(\n" + "    ".repeat(indent)
        }
        else if (s[i] == '[') {
            ++indent
            buf += "[\n" + "    ".repeat(indent)
        }
        else if (s[i] == ')') {
            --indent
            buf += ")"
        }
        else if (s[i] == ']') {
            --indent
            buf += "]"
        }
        else if (s[i] == ',' && i + 1 <= s.lastIndex && s[i + 1] == ' ') {
            ++i
            buf += ",\n" + "    ".repeat(indent)
        }
        else {
            buf += s[i]
        }
        ++i
    }
    return buf.toString()
//    return this.toString().replaceFirst("(", "(\n    ").replace(", ", ",\n    ")
}

fun dedent(it: String): String {
    var lines = it.split(Regex("\\r?\\n"))
    if (lines.isNotEmpty() && lines[0].isBlank()) {
        lines = lines.drop(1)
    }
    if (lines.isNotEmpty() && lines.last().isBlank()) {
        lines = lines.dropLast(1)
    }

    var minIndent = 9999 // TODO:vgrechka Platform-specific max integer (for JS: Number.MAX_SAFE_INTEGER)
    for (line in lines) {
        if (!line.isBlank()) {
            val lineIndent = line.length - line.trimStart().length
            if (lineIndent < minIndent) {
                minIndent = lineIndent
            }
        }
    }

    return lines.map {line ->
        if (line.trim().isBlank()) ""
        else line.substring(minIndent)
    }.joinToString("\n")
}

fun reindent(newIndent: Int, it: String): String {
    return dedent(it).split("\n").joinToString("\n") {" ".repeat(newIndent) + it}
}

object TimePile {
    val FMT_PG_LOCAL_DATE_TIME = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral(' ')
        .append(DateTimeFormatter.ISO_LOCAL_TIME)
        .toFormatter()!!

    val FMT_YMD = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendValue(ChronoField.YEAR, 4)
        .appendValue(ChronoField.MONTH_OF_YEAR, 2)
        .appendValue(ChronoField.DAY_OF_MONTH, 2)
        .toFormatter()!!


    private var testLdtnow: LocalDateTime? = null

    fun ldtnow(): LocalDateTime {
        return testLdtnow ?: LocalDateTime.now()!!
    }

    fun withTestLdtnow(ldt: LocalDateTime, block: () -> Unit) {
        val old_testLdtnow = testLdtnow
        testLdtnow = ldt
        try {
            block()
        } finally {
            testLdtnow = old_testLdtnow
        }
    }
}

interface SuiteMakerClient {
    fun build(make: SuiteMaker<Unit>)
}

object FilePile {
    val tmpDirPath get() = System.getProperty("java.io.tmpdir")

    class backUp {
        interface Ignite {
            fun ignite(file: File): String?
        }

        inner class ifExists : Ignite {
            override fun ignite(file: File): String? {
                if (!file.exists()) return null

                class R2P(val root: String, val generatedPrefix: String)
                val r2p = listOf(R2P(BigPile.fuckingEverythingSmallRoot, "fesmall----"),
                                 R2P(BigPile.fuckingEverythingBigRoot, "febig----"))
                    .find {file.path.replace("\\", "/").startsWith(it.root + "/")}
                    ?: bitch("9911cfc6-6435-4a54-aa74-ad492162181a")

                val stamp = currentTimestampForFileName()
                val prefixForGeneratedFileName = r2p.generatedPrefix
                val outPath = (
                    BigPile.fuckingBackupsRoot + "/" +
                        prefixForGeneratedFileName +
                        file.path
                            .substring(r2p.root.length)
                            .replace("\\", "/")
                            .replace(Regex("^/"), "")
                            .replace("/", "--") +
                        "----$stamp"
                    )

                // clog("Backing up: $outPath")
                File(outPath).writeBytes(file.readBytes())
                return outPath
            }
        }

        inner class orBitchIfDoesntExist : Ignite {
            override fun ignite(file: File): String {
                val backupPath = ifExists().ignite(file)
                    ?: bitch("Cannot find file for backing it up: ${file.path}    c5a4b69c-e2d5-4833-b9c0-4eb5b0b06c25")
                return backupPath
            }
        }
    }

    fun currentTimestampForFileName(): String {
        // replace(Regex("[ :.]"), "-")
        return TimePile.ldtnow().format(TimePile.FMT_PG_LOCAL_DATE_TIME)
            .replace(Regex("-"), "")
            .replace(Regex(":"), "")
            .replace(Regex(" "), "-")
            .replace(Regex("\\."), "-")
    }

}

interface SuiteMaker<BaseSUT : Any> {
    fun case(name: String, exercise: (BaseSUT) -> Unit)
    fun <SubSUT : Any> suiteFor(makeSUT: (BaseSUT) -> SubSUT, prefix: String = "", build: (SuiteMaker<SubSUT>) -> Unit)
    fun suite(suiteName: String, build: (SuiteMaker<BaseSUT>) -> Unit)
}

fun String.substituteMyVars(): String {
    return this.replace("%FE%", BigPile.fuckingEverythingSmallRoot)
}

fun String.indexOfOrNull(needle: String, startIndex: Int = 0, ignoreCase: Boolean = false): Int? {
    val index = indexOf(needle, startIndex, ignoreCase)
    return if (index >= 0) index else null
}

inline fun <T> List<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
    val index = this.indexOfFirst(predicate)
    return when (index) {
        -1 -> null
        else -> index
    }
}

fun <T> List<T>.indexOfOrNull(element: T): Int? {
    val index = this.indexOf(element)
    return when (index) {
        -1 -> null
        else -> index
    }
}

interface CollectBlockReceiver<in T> {
    fun yield(x: T)
}

fun <From, To> Iterable<From>.collect(block: CollectBlockReceiver<To>.(From) -> Unit): List<To> {
    val res = mutableListOf<To>()
    val receiver = object : CollectBlockReceiver<To> {
        override fun yield(x: To) {
            res += x
        }
    }
    for (x in this)
        receiver.block(x)
    return res
}























