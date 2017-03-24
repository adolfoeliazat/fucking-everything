package vgrechka

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
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

fun runProcessAndWait(cmdPieces: List<String>, inheritIO: Boolean = true): RunProcessResult {
    clog("Executing: " + cmdPieces.joinToString(" "))
    val pb = ProcessBuilder()
    val cmd = pb.command()
    cmd.addAll(cmdPieces)
    if (inheritIO) pb.inheritIO()
    val proc = pb.start()

    fun suck(stm: InputStream): StringBuilder {
        val buf = StringBuilder()
        thread {
            val reader = BufferedReader(InputStreamReader(stm, Charsets.UTF_8.name()))
            while (true) {
                val line = reader.readLine()
                if (line == null) {
                    break
                } else {
                    println(line)
                    buf.appendln(line)
                }
            }
        }
        return buf
    }

    val stdout = suck(proc.inputStream)
    val stderr = suck(proc.errorStream)
    val exitValue = proc.waitFor()
    return RunProcessResult(exitValue = exitValue, stdout = stdout.toString(), stderr = stderr.toString())
}

/**
 * Fuck thisy builders
 */
fun stringBuild(block: (StringBuilder) -> Unit) =
    StringBuilder().also(block).toString()

annotation class Ser

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



class AttachedComputedShit<in Host : Any, out Shit>(val create: (Host) -> Shit) : ReadOnlyProperty<Host, Shit> {
    override fun getValue(thisRef: Host, property: KProperty<*>): Shit {
        @Suppress("UNCHECKED_CAST")
        return shitToShit.computeIfAbsent(Key(thisRef, property.name)) {create(thisRef)} as Shit
    }

    data class Key(val host: Any, val prop: String)

    companion object {
        val shitToShit = ConcurrentHashMap<Key, Any?>()
    }
}



//class AttachedShit<in Host : Any, Shit>(val create: (Host) -> Shit) : ReadWriteProperty<Host, Shit> {
//    override fun getValue(thisRef: Host, property: KProperty<*>): Shit {
//        @Suppress("UNCHECKED_CAST")
//        return shitToShit.computeIfAbsent(Key(thisRef, property.name)) {create(thisRef)} as Shit
//    }
//
//    override fun setValue(thisRef: Host, property: KProperty<*>, value: Shit) {
//        shitToShit[Key(thisRef, property.name)] = value
//    }
//
//    data class Key(val host: Any, val prop: String)
//
//    companion object {
//        val shitToShit = ConcurrentHashMap<Key, Any?>()
//    }
//}

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





