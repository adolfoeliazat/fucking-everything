@file:GSpit(spew = OptsBuilderSpew::class, output = "%FE%/shared-jvm-2/gen/generated--HTTPClientRequest.kt")

package vgrechka

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.junit.Test
import vgrechka.spew.*
import java.util.concurrent.TimeUnit

@GOptsBuilder
class HTTPClientRequest : Generated_BaseFor_HTTPClientRequest() {
    data class Opts(
        val url: String,
        val readTimeoutSeconds: Long? = null,
        val headers: List<Pair<String, String>> = listOf(),
        val method: MethodOpts,
        val bitchUnless200: Boolean = true,
        val weirdLogging: WeirdLoggingOpts = WeirdLoggingOpts.None()) {

        sealed class MethodOpts {
            data class Post(
                val mediaTypeName: String,
                val content: String,
                val weirdAttachedPicture: AnimalOpts? = null) : MethodOpts() {

                sealed class AnimalOpts {
                    data class Cat(val whiskersLength: Int): AnimalOpts()
                    data class Rat(val tailLength: Int): AnimalOpts()
                }
            }

            class Get : MethodOpts()
        }

        sealed class WeirdLoggingOpts {
            class None : WeirdLoggingOpts()

            data class Simple(
                val level: String,
                val swearing: Boolean = false) : WeirdLoggingOpts()
        }
    }

    data class Response(
        val code: Int,
        val body: String
    )

    class Ignition(val opts: Opts) {
        fun ignite(): Response {
            val client = OkHttpClient.Builder()
                .readTimeout(opts.readTimeoutSeconds ?: 5, TimeUnit.SECONDS)
                .build()

            var requestBuilder = Request.Builder()
                .url(opts.url)
            for ((name, value) in opts.headers) {
                requestBuilder = requestBuilder.header(name, value)
            }

            exhaustive=when (opts.method) {
                is Opts.MethodOpts.Post -> {
                    val mediaType = MediaType.parse(opts.method.mediaTypeName + "; charset=utf-8")
                    val body = RequestBody.create(mediaType, opts.method.content)
                    requestBuilder = requestBuilder.post(body)
                }
                is Opts.MethodOpts.Get -> {
                    requestBuilder = requestBuilder.get()
                }
            }

            val request = requestBuilder.build()
            val response = client.newCall(request).execute()
            val code = response.code()
            if (code != 200 && opts.bitchUnless200)
                bitch("Shitty HTTP response code: $code")

            val charset = BigPile.charset.utf8
            return Response(
                code = code,
                body = response.body().source().readString(charset)
            )
        }
    }

}


class HTTPClientRequestTests {
    @Test
    fun sampleOptions_1() {
        val shit = HTTPClientRequest()
            .url("http://www.pepezdus.com")
            .readTimeoutSeconds(5L)
            .method_post {it
                .mediaTypeName("application/json")
                .content("{\"fucking\": \"question\"}")
                .weirdAttachedPicture_cat {it
                    .whiskersLength(58)
                }
            }
            .weirdLogging_simple {it
                .level("info")
                .swearing(true)
            }

        val opts = shit.hardenOpts()
        clog(opts)
    }
}


