package vgrechka

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Equivalence
import com.google.common.cache.CacheBuilder
import com.google.common.collect.MapMaker
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.*
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible


var exhaustive: Any? = null

fun clog(vararg xs: Any?): Unit = println(xs.joinToString(" "))
fun wtf(msg: String = "...WTF didn't you describe this WTF?"): Nothing = throw Exception("WTF: $msg")
fun die(msg: String = "You've just killed me, motherfucker!"): Nothing = throw Exception("Aarrgghh... $msg")
fun imf(what: String = "me"): Nothing = throw Exception("Implement $what, please, fuck you")
fun bitch(msg: String = "Just bitching..."): Nothing = throw Exception(msg)

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

fun Boolean.ifOrEmpty(block: () -> String): String = when {
    this -> block()
    else -> ""
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


class AttachedComputedShit<in Host : Any, out Shit>(val weak: Boolean = false,
                                                    val create: (Host) -> Shit) : ReadOnlyProperty<Host, Shit> {
    private val NULL = Any()

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

fun assertThrown(check: (Throwable) -> Unit, block: () -> Unit) {
    try {
        block()
        fail("Expected something to be thrown")
    } catch (e: Throwable) {
        check(e)
    }
}

fun <T : Throwable> assertThrown(clazz: KClass<T>, block: () -> Unit) {
    assertThrown({assertEquals(clazz, it::class)}, block)
}

class TestLogger {
    val lines = mutableListOf<Line>()

    class Line(val text: String, val uuid: String)

    fun println(value: Any?, uuid: String) {
        clog(value)
        lines += Line(value.toString(), uuid)
    }

    fun shitWritten(): String {
        val longestLine = lines.map{it.text}.maxBy{it.length} ?: return ""
        return stringBuild {
            val width = longestLine.length + 4
            for (line in lines) {
                val spaces = width - line.text.length
                it += line.text + " ".repeat(spaces) + line.uuid + "\n"
            }
        }
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

























