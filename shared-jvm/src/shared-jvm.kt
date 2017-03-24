package vgrechka

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.concurrent.thread
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


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



