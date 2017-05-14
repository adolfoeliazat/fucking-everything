@file:GSpit(spew = OptsBuilderSpew::class, output = "%FE%/shared-jvm-2/gen/generated--HTTPClientRequest.kt")

package vgrechka

import org.junit.Test
import vgrechka.spew.*

@GOptsBuilder
class HTTPClientRequest : Generated_BaseFor_HTTPClientRequest() {
    data class Opts(
        val url: String,
        val readTimeoutSeconds: Long? = null,
        val method: MethodOpts,
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

            data class Get(
                val uselessOption: String) : MethodOpts()
        }

        sealed class WeirdLoggingOpts {
            class None : WeirdLoggingOpts()

            data class Simple(
                val level: String,
                val swearing: Boolean = false) : WeirdLoggingOpts()
        }
    }


    object MediaTypeName {
        val JSON = "application/json"
        val XML = "application/xml"
    }

    class Ignition(val opts: Opts) {
        fun ignite(): String {
            imf("99ffcd25-a35f-404b-9177-6e289a5fd18c")
        }
    }

    //    fun post(mediaTypeName: String, url: String, content: String, readTimeoutSeconds: Long? = null): String {
//        val mediaType = MediaType.parse(mediaTypeName + "; charset=utf-8")
//        val client = OkHttpClient.Builder()
//            .readTimeout(readTimeoutSeconds ?: 5, TimeUnit.SECONDS)
//            .build()
//        val body = RequestBody.create(mediaType, content)
//        val request = Request.Builder()
//            .url(url)
//            .post(body)
//            .build()
//        val response = client.newCall(request).execute()
//        val code = response.code()
//        if (code != 200)
//            bitch("Shitty HTTP response code: $code")
//
//        val charset = Charset.forName("UTF-8")
//        return response.body().source().readString(charset)
//    }

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


