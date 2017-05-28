package vgrechka.kjs

import org.w3c.dom.events.Event
import vgrechka.*
import kotlin.browser.window
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.coroutines.experimental.intrinsics.*
import kotlin.coroutines.experimental.*
import kotlin.js.Promise

fun main(args: Array<String>) {
    clog("I am shared-kjs")
}

object KJSPile {
    fun getURLParam(name: String): String? {
        val shit = decodeURIComponent(window.location.search.substring(1))
        for (pairString in shit.split('&')) {
            val pair = pairString.split("=")
            if (pair[0] == name)
                return pair[1]
        }
        return null
    }
}

fun clog(vararg xs: Any?) {
    console.log(*xs)
}

external fun encodeURIComponent(s: String): String
external fun decodeURIComponent(s: String): String

fun Event.preventAndStop() {
    this.preventDefault()
    this.stopPropagation()
}

external @JsName("Object")
object JSObject {
    fun keys(x: Any): Array<String>
    fun getOwnPropertyNames(x: Any): Array<String>
}

class ModalTestLocks {
    val shown by notNullNamed(TestLock())
    val hidden by notNullNamed(TestLock())
}


fun <T: Any> notNullNamed(initial: T, parentNamed: Any? = null): ReadWriteProperty<Any?, T> = NotNullNamedVar(initial, parentNamed)

private class NotNullNamedVar<T: Any>(initial: T?, val parentNamed: Any? = null) : ReadWriteProperty<Any?, T> {
    private var value: T? = initial

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val res = value ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
        val namePrefix = parentNamed?.let {NamesOfThings[it]}?.let {"$it."} ?: ""
        NamesOfThings[res] = namePrefix + property.name
        return res
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

object NamesOfThings {
    private val thingToName = WeakMap<Any, String>()
    private val sourceToSinks = WeakMap<Any, MutableList<Any>>()

    operator fun set(thing: Any, name: String) {
        thingToName[thing] = name
        val sinks = sourceToSinks[thing]
        sinks?.forEach {set(it, name)}
    }

    operator fun get(thing: Any): String? {
        return thingToName[thing]
    }

    fun flow(from: Any, to: Any) {
        val sinks = sourceToSinks.getOrPut(from) {mutableListOf()}
        sinks += to

        get(from)?.let {name->
            sinks.forEach {set(it, name)}
        }
    }

    fun unflow(from: Any, to: Any) {
        sourceToSinks[from]?.let {sinks->
            sinks -= to
        }
    }
}

class ResolvableShit<T> {
    private var _resolve by notNull<(T) -> Unit>()
    private var _reject by notNull<(Throwable) -> Unit>()
    private var _promise by notNull<Promise<T>>()
    private var hasPromise = false

    init {
        reset()
    }

    val promise: Promise<T> get() = _promise
    fun resolve(value: T) = _resolve(value)
    fun reject(e: Throwable) = _reject(e)

    suspend fun get(): T = await(promise)
    suspend fun wait(): T = await(promise)

    fun reset() {
        if (hasPromise) {
            NamesOfThings.unflow(this, promise)
        }

        _promise = Promise<T> {resolve, reject ->
            this._resolve = resolve
            this._reject = reject
        }
        hasPromise = true
        NamesOfThings.flow(this, promise)
    }
}

fun ResolvableShit<Unit>.resolve() = this.resolve(Unit)

external class WeakMap<in K: Any, V: Any?> {
    fun delete(key: K): Boolean
    operator fun get(key: K): V?
    fun has(key: K): Boolean
    operator fun set(key: K, value: V): WeakMap<K, V>
}

inline fun <K: Any, V> WeakMap<K, V>.getOrPut(key: K, defaultValue: () -> V): V {
    val value = get(key)
    return if (value == null) {
        val answer = defaultValue()
        set(key, answer)
        answer
    } else {
        value
    }
}

fun timeoutSet(ms: Int, cb: () -> Unit) {
    window.setTimeout(cb, ms)
}

fun <T> Promise<T>.orTestTimeout(ms: Int, getPromiseName: (() -> String?)? = null): Promise<T> {
    val shit = ResolvableShit<T>()
    val thePromiseName = getPromiseName?.invoke() ?: "shit"
    timeoutSet(ms) {
        val msg = "Sick of waiting for $thePromiseName"
//        if (isTestPausedOnAssertion()) {
//            // console.warn("--- $msg, but not dying because test is paused on assertion ---")
//        } else {
        shit.reject(Exception(msg))
//        }
    }
    this.then({shit.resolve(it)})
    return shit.promise
}

fun <T> Promise<T>.orTestTimeoutNamedAfter(ms: Int, getPromiseNameBearer: () -> Any): Promise<T> {
    return this.orTestTimeout(ms, getPromiseName = {NamesOfThings[getPromiseNameBearer()]})
}

class TestLock(
    virgin: Boolean = false,
    val testPauseTimeout: Int = 10000,
    val sutPauseTimeout: Int = 10000
) {
    private val testPause by notNullNamed(ResolvableShit<Unit>(), parentNamed = this)
    private val sutPause by notNullNamed(ResolvableShit<Unit>(), parentNamed = this)

    init {
        if (!virgin) { // Initially everything is resolved, so if not in test, shit just works
            testPause.resolve()
            sutPause.resolve()
        }
    }

    fun reset() {
        testPause.reset()
        sutPause.reset()
    }

    suspend fun pauseTestFromTest() {
        await(testPause.promise.orTestTimeoutNamedAfter(testPauseTimeout, {testPause}))
    }

    fun resumeSutFromTest() {
        sutPause.resolve()
    }

    fun resumeTestFromSut() {
        testPause.resolve()
    }

    suspend fun resumeTestAndPauseSutFromSut() {
        testPause.resolve()
        await(sutPause.promise.orTestTimeoutNamedAfter(sutPauseTimeout, {sutPause}))
    }
}

fun <T> async(block: suspend () -> T) = Promise<T> {resolve, reject ->
    block.startCoroutine(object: Continuation<T> {
        override val context = EmptyCoroutineContext

        override fun resume(value: T) {
            resolve(value)
        }

        override fun resumeWithException(exception: Throwable) {
            reject(exception)
        }
    })
}

suspend fun <T> await(p: Promise<T>): T {
//    if (TestGlobal.killAwait) die()
    return suspendCoroutineOrReturn {c: Continuation<T> ->
        p.then<Any?>(
            onFulfilled = {
                c.resume(it)
            },
            onRejected = {
                c.resumeWithException(it)
            }
        )
        COROUTINE_SUSPENDED
    }
}












