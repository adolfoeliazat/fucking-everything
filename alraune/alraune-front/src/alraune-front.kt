package alraune.front

import alraune.shared.AlSharedPile
import alraune.shared.ShitPassedFromBackToFront
import vgrechka.*
import vgrechka.kjs.*
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.js.json

fun main(args: Array<String>) {
    clog("I am alraune-front 4")
    window.asDynamic()[AlDebug::class.simpleName] = AlDebug

    @Suppress("UnsafeCastFromDynamic")
    AlFrontPile.shitFromBack = JSON.parse(window.asDynamic()[ShitPassedFromBackToFront::class.simpleName])
    clog("shitFromBack", AlFrontPile.shitFromBack)

    jqDocumentReady {
//        initGoogleAuth()
        async {initPage()}
    }
}

private suspend fun initPage() {
//    val pageID = AlFrontPile.shitFromBack.pageID
//    when (pageID) {
//        AlSharedPile.pageID.order -> initOrderPage()
//        AlSharedPile.pageID.landing -> initLandingPage()
//        else -> initLandingPage()
//    }
}

suspend fun initLandingPage() {
    clog("initLandingPage")
}

suspend fun initOrderPage() {
    clog("initOrderPage")
    if (AlFrontPile.security.isSignedIn()) {
    }
    else {
    }
}

class AlFrontSecurity {
    fun isSignedIn(): Boolean {
        return false
    }
}

object AlDebug {
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

