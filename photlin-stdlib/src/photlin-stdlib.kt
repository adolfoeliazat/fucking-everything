package photlin

import aps.*
import kotlin.reflect.KProperty

//------------------------- PHP -------------------------

val _SERVER = eval<Array<String>>("return \$_SERVER;")
external val argv: Array<String>

external fun echo(x: Any?)
external fun die()
external fun print_r(x: Any?)
external fun print_r(x: Any?, returnString: Boolean): String
external fun var_export(x: Any?)
external fun var_export(x: Any?, returnString: Boolean): String
external fun error_log(msg: String, messageType: Int = definedExternally, destination: String = definedExternally)
external fun <T> eval(code: String): T
external fun require_once(file: String)
external fun gettype(x: Any?): String
external fun count(x: Any): Int // TODO:vgrechka Types
external fun file_get_contents(name: String): String
external fun exit()
external fun str_replace(search: String, replace: String, subject: String): String

external class stdClass

operator fun stdClass.get(key: String): Any? {
    val receiver = this
    return eval("return \$receiver->\$key;")
}

external class PDO(url: String, user: String, password: String) {
    fun query(sql: String): PDOStatement
}

external interface PDOStatement {
    fun fetchAll(): Array<Array<Any>>
}

external class PDOException : Throwable

external fun json_encode(x: Array<Any?>): String

/**
 * @return null if failed
 */
external fun json_decode(x: String): stdClass?

//------------------------- DOCTRINE -------------------------

external interface DBALConnection {
    fun prepare(sql: String): DBALStatement
    fun executeUpdate(sql: String, params: Array<Any?> = definedExternally): DBALStatement
}

external interface DBALStatement {
    fun execute(params: Array<Any?> = definedExternally): Boolean
}

external interface DoctrineEntityManager {
    fun <T> getRepository(name: String): T
    fun getConnection(): DBALConnection
}

external interface DoctrineRepository<T> {
    fun findAll(): Array<T>
}

@EmitAnnotationAsPHPComment(name = "Entity")
annotation class EntityKillMe

annotation class Table(val name: String)
annotation class Id
annotation class Column(val type: String)
annotation class GeneratedValue


//------------------------- PHOTLIN -------------------------

annotation class EmitAnnotationAsPHPComment(val name: String = "")
annotation class PHPDumpBodyToContainer

operator fun <T> Array<T>.set(key: Any, value: Any?) {
    val receiver = this
    eval<Unit>("\$receiver[\$key] = \$value;")
}

operator fun <T> Array<T>.get(key: Any): T? {
    val receiver = this
    return eval<T>("return \$receiver[\$key];")
}

fun parray(vararg pairs: Pair<Any, Any?>): Array<Any?> {
    val arr = eval<Array<Any?>>("return array();")
    for ((k, v) in pairs) {
        // println("k = $k, v = $v")
        arr[k] = v
    }
    return arr
}

fun assertEquals(expected: Any?, actual: Any?, assertionID: String) {
    if (expected != actual) {
        throw AssertionError("assertEquals $assertionID: expected = $expected; actual = $actual")
    }
}

fun assertVarExportEquals(expected: String, actual: Any?, assertionID: String, log: Boolean = false) {
    val shit0 = var_export(actual, returnString = true)
    val shit = shit0.replace("\n  ", " ").replace("\n", "")
    if (log) {
        println("shit = $shit")
    }
    assertEquals(expected, shit, assertionID)
}

fun assertTrue(actual: Boolean, assertionID: String) {
    if (!actual) {
        throw AssertionError("assertTrue $assertionID")
    }
}

fun assertFalse(actual: Boolean, assertionID: String) {
    if (actual) {
        throw AssertionError("assertFalse $assertionID")
    }
}

//interface ReadWriteProperty<in R, T> {
//    operator fun getValue(thisRef: R, property: KProperty<*>): T
//    operator fun setValue(thisRef: R, property: KProperty<*>, value: T)
//}
//
//fun <T: Any> notNull(): ReadWriteProperty<Any?, T> = NotNullVar()
//
//private class NotNullVar<T: Any>() : ReadWriteProperty<Any?, T> {
//    private var value: T? = null
//
//    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
//        return value ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
//    }
//
//    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
//        this.value = value
//    }
//}
//
//var exhaustive: Any? = null

//fun wtf(msg: String = "...WTF didn't you describe this WTF?"): Nothing = throw Exception("WTF: $msg")
//fun imf(what: String = "me"): Nothing = throw Exception("Implement $what, please, fuck you")
//fun bitch(msg: String = "Just bitching..."): Nothing = throw Exception(msg)

fun <T> instantiate(className: String): T {
    return eval("return new \$className;")
}

//fun <T: Any> notNullOnce(): ReadWriteProperty<Any?, T> = NotNullOnceVar()
//
//private class NotNullOnceVar<T: Any> : ReadWriteProperty<Any?, T> {
//    private var value: T? = null
//
//    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
//        return value ?: throw IllegalStateException("Property `${property.name}` should be initialized before get.")
//    }
//
//    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
//        check(this.value == null) {"Property `${property.name}` should be assigned only once"}
//        this.value = value
//    }
//}

//------------------------- KOTLIN -------------------------

data class Pair<out A, out B>(val first: A, val second: B) {
    override fun toString(): String = "($first, $second)"
}

infix fun <A, B> A.to(that: B): Pair<A, B> = Pair(this, that)

inline fun check(value: Boolean): Unit = check(value) { "Check failed." }

inline fun check(value: Boolean, lazyMessage: () -> Any): Unit {
    if (!value) {
        val message = lazyMessage()
        throw IllegalStateException(message.toString())
    }
}

fun println(x: Any?) {
    echo(x)
    echo("\n")
}

fun println() = println("")

inline fun <R> run(block: () -> R): R = block()

fun <T> Array<out T>.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): String {
    return joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
}

fun <T, A : Appendable> Array<out T>.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): A {
    buffer.append(prefix)
    var count = 0
    for (element in this) {
        if (++count > 1) buffer.append(separator)
        if (limit < 0 || count <= limit) {
            buffer.appendElement(element, transform)
        } else break
    }
    if (limit >= 0 && count > limit) buffer.append(truncated)
    buffer.append(postfix)
    return buffer
}

internal fun <T> Appendable.appendElement(element: T, transform: ((T) -> CharSequence)?) {
    when {
        transform != null -> append(transform(element))
        element is CharSequence? -> append(element)
        element is Char -> append(element)
        else -> append(element.toString())
    }
}

public interface Appendable {
    fun append(csq: CharSequence?): Appendable
    fun append(csq: CharSequence?, start: Int, end: Int): Appendable
    fun append(c: Char): Appendable
}

public class StringBuilder(content: String = "") : Appendable, CharSequence {
    constructor(capacity: Int) : this() {}

    constructor(content: CharSequence) : this(content.toString()) {}

    private var string: String = content

    override val length: Int
        get() {
            val s = string
            return eval("return strlen(\$s);")
//            return string.asDynamic().length
        }

    override fun get(index: Int): Char = string[index]

    override fun subSequence(start: Int, end: Int): CharSequence = string.substring(start, end)

    override fun append(c: Char): StringBuilder {
        string += c
        return this
    }

    override fun append(csq: CharSequence?): StringBuilder {
        string += csq.toString()
        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): StringBuilder {
        string += csq.toString().substring(start, end)
        return this
    }

    fun append(obj: Any?): StringBuilder {
        string += obj.toString()
        return this
    }

    fun reverse(): StringBuilder {
        val s = string
        eval<Any?>("preg_match_all('/./us', \$s, \$arr); \$s = join('', array_reverse(\$arr[0]));")
        string = s
//        string = string.asDynamic().split("").reverse().join("")
        return this
    }

    override fun toString(): String {
        return string
    }
}

fun String.replace(oldValue: String, newValue: String): String {
    return str_replace(oldValue, newValue, this)
}

fun String.startsWith(prefix: String, ignoreCase: Boolean = false): Boolean {
    if (ignoreCase) imf("b6e4cc36-79bc-48e1-b241-ed4cb461485a")
    return false
}

@PHPDumpBodyToContainer
fun quickTest_String_startsWith() {
    assertTrue("/rpc/shit".startsWith("/rpc/"), "8f1b5065-3ac2-49a8-87df-5d6bc8441206")
    assertFalse("/rpc/shit".startsWith("pizda"), "a3b6e45e-c0c3-47cf-8455-a539f8afbeb2")
}









