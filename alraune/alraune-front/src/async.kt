package alraune.front

import kotlin.coroutines.experimental.intrinsics.*
import kotlin.coroutines.experimental.*
import kotlin.js.Promise

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



