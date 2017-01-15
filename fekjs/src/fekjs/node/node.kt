@file:Suppress("UnsafeCastFromDynamic")
package fekjs.node

external val __dirname: String

@JsName("require")
external fun <T> nodeRequire(module: String): T

@JsName("(require('path'))")
external object path {
    fun join(vararg parts: String): String
}

@JsName("(require('url'))")
external object url {
    fun format(urlObject: URLObject): String
}

class URLObject(
    val pathname: String,
    val protocol: String,
    val slashes: Boolean
)

@JsName("process")
external object process {
    val argv: Array<String>
    val platform: String
}

