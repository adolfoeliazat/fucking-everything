package vgrechka

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Equivalence
import com.google.common.collect.MapMaker
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.annotation.AnnotationDescription
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.implementation.MethodDelegation
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MySuite
import org.junit.runners.Suite
import java.io.*
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
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

fun runProcessAndWait(cmdPieces: List<String>, inheritIO: Boolean = true, input: String? = null): RunProcessResult {
    // clog("Executing: " + cmdPieces.joinToString(" "))
    val pb = ProcessBuilder()
    val cmd = pb.command()
    cmd.addAll(cmdPieces)
    if (inheritIO)
        pb.inheritIO()
    val proc = pb.start()

    fun suckAsync(stm: InputStream): StringBuilder {
        val buf = StringBuilder()
        thread {
            val reader = BufferedReader(InputStreamReader(stm, Charsets.UTF_8.name()))
            while (true) {
                val line = reader.readLine()
                if (line == null) {
                    break
                } else {
                    // println(line)
                    buf.appendln(line)
                }
            }
            // clog("Finished sucker thread")
        }
        return buf
    }

    val stdout = suckAsync(proc.inputStream)
    val stderr = suckAsync(proc.errorStream)

    if (input != null) {
        thread {
            val pw = PrintWriter(proc.outputStream, true)
            for (line in input.lines()) {
                pw.println(line)
            }
            pw.close()
            // clog("Finished feeder thread")
        }
    }

    val exitValue = proc.waitFor()
    return RunProcessResult(exitValue = exitValue, stdout = stdout.toString(), stderr = stderr.toString())
}

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

val tmpDirPath get() = System.getProperty("java.io.tmpdir")

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


    class SuiteMakerImpl<BaseSUT>(val makeBaseSUT: () -> BaseSUT) : SuiteMaker<BaseSUT> {
        var testClassBuilder: DynamicType.Builder<Any> = ByteBuddy().subclass(Any::class.java)!!
        var casesWereDefined = false
        val childGeneratedClasses = mutableListOf<Class<*>>()

        fun generateClass(): Class<*> {
            val testClass = casesWereDefined.thenElseNull {
                testClassBuilder
                    .name("Tests_" + DebugPile.nextPUID())
                    .make()
                    .load(this::class.java.classLoader)
                    .loaded}

            if (testClass != null && childGeneratedClasses.isEmpty()) {
                return testClass
            } else {
                val allChildClasses = mutableListOf<Class<*>>()
                if (testClass != null)
                    allChildClasses += testClass
                allChildClasses += childGeneratedClasses

                val suiteClass = ByteBuddy()
                    .subclass(Any::class.java)
                    .annotateType(AnnotationDescription.Builder
                                      .ofType(RunWith::class.java)
                                      .define("value", Suite::class.java).build())
                    .annotateType(AnnotationDescription.Builder
                                      .ofType(Suite.SuiteClasses::class.java)
                                      .defineTypeArray("value", *allChildClasses.toTypedArray()).build())
                    .name("Suite_" + DebugPile.nextPUID())
                    .make()
                    .load(this::class.java.classLoader)
                    .loaded
                return suiteClass
            }
        }

        override fun case(name: String, exercise: (BaseSUT) -> Unit) {
            testClassBuilder = testClassBuilder
                .defineMethod(name, Void.TYPE, Visibility.PUBLIC)
                .intercept(MethodDelegation.to(object {
                    fun invokeMotherfucker() {
                        exercise(makeBaseSUT())
                    }
                }))
                .annotateMethod(AnnotationDescription.Builder.ofType(Test::class.java).build())
            casesWereDefined = true
        }

        override fun <SUT> suiteFor(makeSUT: (BaseSUT) -> SUT, build: (SuiteMaker<SUT>) -> Unit) {
            val sampleSUT = makeSUT(makeBaseSUT()) as Any
            var name = sampleSUT.javaClass.name
            val lastDot = name.lastIndexOf(".")
            if (lastDot != -1) {
                name = name.substring(lastDot + 1)
            }
            name = name.replace("$", "_")

            val childSuiteMaker = SuiteMakerImpl<SUT> {makeSUT(makeBaseSUT())}
            build(childSuiteMaker)

            childGeneratedClasses += childSuiteMaker.generateClass()
        }
    }

    fun generateJUnitSuiteClassesFromBuilder(klass: Class<*>): Array<Class<*>> {
        val clientSideBuilder = klass.newInstance() as SuiteMakerClient
        val ourSideBuilder = SuiteMakerImpl {Unit}
        clientSideBuilder.build(ourSideBuilder)
        return arrayOf(ourSideBuilder.generateClass())
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

val PG_LOCAL_DATE_TIME = DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(DateTimeFormatter.ISO_LOCAL_DATE)
    .appendLiteral(' ')
    .append(DateTimeFormatter.ISO_LOCAL_TIME)
    .toFormatter()!!


object TimePile {
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
    fun build(it: SuiteMaker<Unit>)
}

object FilePile {
    class backUp {
        interface Ignite {
            fun ignite(file: File): String?
        }

        inner class fromFuckingEverythingSmallRoot {
            inner class ifExists : Ignite {
                override fun ignite(file: File): String? {
                    if (!file.exists()) return null

                    check(file.path.replace("\\", "/").startsWith(BigPile.fuckingEverythingSmallRoot + "/")) {"9911cfc6-6435-4a54-aa74-ad492162181a"}

                    val stamp = TimePile.ldtnow().format(PG_LOCAL_DATE_TIME).replace(Regex("[ :\\.]"), "-")
                    val prefixForGeneratedFileName = "fesmall----"
                    val outPath = (
                        BigPile.fuckingBackupsRoot + "/" +
                            prefixForGeneratedFileName +
                            file.path
                                .substring(BigPile.fuckingEverythingSmallRoot.length)
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

        inner class fromFuckingEverythingBigRoot {
            inner class ifExists {
                fun ignite(file: File): String? {
                    imf("15186bbb-1f56-4a4c-b1a1-1dbc66d0bb2a")
                }
            }
        }
    }

    class Foo {
        inner class Bar {
            fun shitFromBar1() = "shit from bar 1"
            fun shitFromBar2() = "shit from bar 2"
            inner class Baz {
                fun shitFromBaz() = "shit from baz"
            }
        }
    }

    @RunWith(MySuite::class)
    class Tests : SuiteMakerClient {
        override fun build(it: SuiteMaker<Unit>) {
            it.suiteFor({Foo().Bar()}) {
                it.case("first") {sut->
                    clog("Case: first")
                    assertEquals("shit from bar 1", sut.shitFromBar1())
                }
                it.case("second") {sut->
                    clog("Case: second")
                    assertEquals("shit from bar 2", sut.shitFromBar2())
                }
                it.suiteFor({it.Baz()}) {
                    it.case("third") {sut->
                        clog("Case: third")
                        assertEquals("shit from baz", sut.shitFromBaz())
                    }
                }
            }

            it.case("weirdCase") {sut->
                clog("Well, I'm weird...")
            }

            it.suiteFor({backUp().fromFuckingEverythingSmallRoot().ifExists()}) {
                buildCommonCases(it)
                buildDoesntExistCase(it) {exercise->
                    assertNull(exercise())
                }
            }

            it.suiteFor({backUp().fromFuckingEverythingSmallRoot().orBitchIfDoesntExist()}) {
                buildCommonCases(it)
                buildDoesntExistCase(it) {exercise->
                    AssertPile.thrownContainsUUID("c--testref--5a4b69c-e2d5-4833-b9c0-4eb5b0b06c25") {
                        exercise()
                    }
                }
            }
        }

        private fun buildDoesntExistCase(it: SuiteMaker<out backUp.Ignite>, block: (() -> String?) -> Unit) {
            it.case("doesntExist") {sut ->
                block {sut.ignite(File(BigPile.fuckingEverythingSmallRoot + "/non-existent-pizda.txt"))}
            }
        }

        private fun buildCommonCases(it: SuiteMaker<out backUp.Ignite>) {
            it.case("existsButNotUnderGivenRoot") {sut ->
                AssertPile.thrownContainsUUID("9--testref--911cfc6-6435-4a54-aa74-ad492162181a") {
                    sut.ignite(TestPile.existingPizdaFile.big())
                }
            }
            it.case("cool") {sut ->
                TimePile.withTestLdtnow(LocalDateTime.of(2015, 5, 25, 10, 11, 12, 345000000)) {
                    fail("// TODO:vgrechka Delete backup file first")
                    val res = sut.ignite(TestPile.existingPizdaFile.small())
                    assertEquals(BigPile.fuckingBackupsRoot + "/fesmall----shit-for-tests--existing-pizda.txt----2015-05-25-10-11-12-345", res)
                    fail("// TODO:vgrechka Check file content")
                }
            }
        }
    }
}

interface SuiteMaker<BaseSUT> {
    fun case(name: String, exercise: (BaseSUT) -> Unit)
    fun <SubSUT> suiteFor(makeSUT: (BaseSUT) -> SubSUT, build: (SuiteMaker<SubSUT>) -> Unit)
}

//interface CaseMaker<SUT> {
//    fun case(name: String, exercise: (SUT) -> Unit)
//    fun <SUT2> suiteFor(makeSUT: (SUT) -> SUT2, build: (CaseMaker<SUT2>) -> Unit)
//}

fun String.substituteMyVars(): String {
    return this.replace("%FE%", BigPile.fuckingEverythingSmallRoot)
}

fun String.indexOfOrNull(needle: String, startIndex: Int = 0, ignoreCase: Boolean = false): Int? {
    val index = indexOf(needle, startIndex, ignoreCase)
    return if (index >= 0) index else null
}

val Throwable.stackTraceStr: String
    get() {
        val sw = StringWriter()
        PrintWriter(sw).use { printStackTrace(it) }
        return sw.toString()
    }













//startSteps({backUp()}) {
//    it.step({it.fromFuckingEverythingSmallRoot()}) {
//        it.step({it.ifExists()}) {
//            it.case("doesntExist") {sut->
//                val res = sut.ignite(File(BigPile.fuckingEverythingSmallRoot + "/non-existent-pizda.txt"))
//                assertNull(res)
//            }
//            it.case("existsButNotInFuckingEverything") {sut->
//                AssertPile.thrownContainsUUID("9--testref--911cfc6-6435-4a54-aa74-ad492162181a") {
//                    sut.ignite(TestPile.existingPizdaFile.big())
//                }
//            }
//            it.case("cool") {sut->
//                TimePile.withTestLdtnow(LocalDateTime.of(2015, 5, 25, 10, 11, 12, 345000000)) {
//                    val res = sut.ignite(TestPile.existingPizdaFile.small())
//                    assertEquals(BigPile.fuckingBackupsRoot + "/fesmall----shit-for-tests--existing-pizda.txt----2015-05-25-10-11-12-345", res)
//                }
//            }
//        }
//    }
//}

//class StepShit {
//    class StepBuilder<PrevStep> {
//        fun <Step> step(makeStep: (PrevStep) -> Step, buildFurther: (StepBuilder<Step>) -> Unit) {
//            imf("6d0e172d-6801-403d-bcc9-db6d2a7b94c2")
//        }
//
//        fun case(name: String, exercise: (PrevStep) -> Unit) {
//            imf("5dd331fd-3add-4a66-81ff-a06ba2a39ce7")
//        }
//    }
//
//    fun <Step> startSteps(makeFirstStep: () -> Step, buildFurtherSteps: (StepBuilder<Step>) -> Unit) {
//        imf("09e64f48-228d-475b-ae0b-418463f2dac6")
//    }
//}

























