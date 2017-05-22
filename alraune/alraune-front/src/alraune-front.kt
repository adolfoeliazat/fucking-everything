package alraune.front

import alraune.shared.AlSharedPile
import alraune.shared.OrderCreationForm
import alraune.shared.ShitPassedFromBackToFront
import vgrechka.*
import vgrechka.kjs.*
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.reflect.KFunction0

fun main(args: Array<String>) {
    clog("I am alraune-front 4")
    window.asDynamic()[AlFrontDebug::class.simpleName] = AlFrontDebug

    @Suppress("UnsafeCastFromDynamic")
    AlFrontPile.shitFromBack = JSON.parse(window.asDynamic()[ShitPassedFromBackToFront::class.simpleName])
    clog("shitFromBack", AlFrontPile.shitFromBack)

    jqDocumentReady {
        if (AlFrontPile.shitFromBack.pageID == AlSharedPile.pageID.orderCreation) {
            val button = byid(AlSharedPile.domID.createOrderForm_submitButton)
            // TODO:vgrechka Remove event handlers
            fun handler() {
                clog("i am the fucking handler")
                val clazz = window.asDynamic()["alraune-front"].alraune.shared[OrderCreationForm::class.simpleName]
                val inst = js("new clazz()")
                val props = JSObject.getOwnPropertyNames(inst)
                for (prop in props) {
                    clog("prop", prop)
                }
            }
            AlFrontDebug.messAroundFront201 = ::handler
            button.on("click") {
                it.preventAndStop()
                handler()
            }
        }

        @Suppress("UnsafeCastFromDynamic")
        KJSPile.getURLParam("frontMessAround")?.let {
            val f = AlFrontDebug.asDynamic()[it] as? KFunction0<*> ?: bitch("$it is not a function")
            f()
        }
    }
}

class AlFrontSecurity {
    fun isSignedIn(): Boolean {
        return false
    }
}

object AlFrontDebug {
    // Ex: AlDebug.AlFrontPile.google.auth2

    val AlFrontPile = alraune.front.AlFrontPile

    fun postShit1() {
        imf("66ea9a6b-4fc2-4bdb-86a3-5d039f0ac5e7")
//        val q = AlSharedPile.domID.orderCreationForm
//        val data = json(q.email to "iperdonde@mail.com",
//                        q.name to "Иммануил Пердондэ",
//                        q.phone to "+38 (068) 4542823",
//                        q.documentTitle to "Как я пинал хуи на практике",
//                        q.documentDetails to "Детали? Я ебу, какие там детали...")
//        window.location.href = "https://alraune.local/order?post=true&data=" + encodeURIComponent(JSON.stringify(data))
    }

    fun messAround1() {
        val button = byid(AlSharedPile.domID.createOrderForm_submitButton)
        button.off()
    }

    var messAroundFront201: KFunction0<Unit>? = null
}

object AlFrontPile {
    var shitFromBack by notNullOnce<ShitPassedFromBackToFront>()

    object google {
        var auth2 by notNullOnce<gapi.auth2.GoogleAuth>()
    }

    val localStorage = AlLocalStorage(RealLocalStorage)
    val security = AlFrontSecurity()
}

fun initGoogleAuth() {
    gapi.load("auth2") {
        clog("gapi.load")
        AlFrontPile.google.auth2 = gapi.auth2.init(GApiClientConfig(
            client_id = "1064147176813-n6l5pddt9qggcp9n4losnknb2dm5hl9t.apps.googleusercontent.com",
            cookiepolicy = "single_host_origin"
        ))
        AlFrontPile.google.auth2.then(
            onInit = {
                clog("onInit: AlFrontPile.google.auth2.then")
            },
            onError = {e->
                console.log("onError: AlFrontPile.google.auth2.then", e)
            }
        )
    }
}

// https://developers.google.com/api-client-library/javascript/reference/referencedocs
external object gapi {
    fun load(what: String, block: () -> Unit)

    object auth2 {
        class GoogleAuth {
            fun then(onInit: () -> Unit, onError: (dynamic) -> Unit)
            fun signIn(options: GApiSignInOptions): Promise<dynamic>
        }

        fun init(params: GApiClientConfig): GoogleAuth
    }
}

// https://developers.google.com/api-client-library/javascript/reference/referencedocs#gapiauth2clientconfig
class GApiClientConfig(
    val client_id: String,
    val cookiepolicy: String
)

// https://developers.google.com/api-client-library/javascript/reference/referencedocs#gapiauth2signinoptions
class GApiSignInOptions(
    val redirect_uri: String
)


/*
// google.auth2.disconnect()
// p = google.auth2.currentUser.get().getBasicProfile()
// p.getName()

$(_=> {
    initGoogleAuth()

    function initGoogleAuth() {
        window.google = {}
        gapi.load('auth2', _=> {
            console.log('gapi.load')
            google.auth2 = gapi.auth2.init({
                client_id: '1064147176813-n6l5pddt9qggcp9n4losnknb2dm5hl9t.apps.googleusercontent.com',
                cookiepolicy: 'single_host_origin'
            })

            google.auth2.then(
                function onSuccess() {
                    console.log('onSuccess: google.auth2.then')
                },
                function onError(error) {
                    console.error('onError: google.auth2.then', error)
                }
            )

            google.auth2.attachClickHandler(document.getElementById('googleSignInButton'),
                {
                    redirect_uri: pile.myIndexURL + '?page='
                },
                function onSuccess(googleUser) {
                    console.log('onSuccess: google.auth2.attachClickHandler')
                    google.user = googleUser
                    var profile = google.user.getBasicProfile()
                    console.log('ID: ' + profile.getId())
                    console.log('Name: ' + profile.getName())
                    console.log('Image URL: ' + profile.getImageUrl())
                    console.log('Email: ' + profile.getEmail())
                },
                function onError(error) {
                    console.error('onError: google.auth2.attachClickHandler', error)
                }
            )
        })
    }
})

*/

