package alraune.front

import vgrechka.*
import vgrechka.kjs.*
import kotlin.js.json

// https://developers.google.com/api-client-library/javascript/reference/referencedocs

fun main(args: Array<String>) {
    clog("I am alraune-front")
    jq {
        initGoogleAuth()
    }
}

object AlFrontPile {
    object google {
        var auth2 by notNullOnce<gapi.auth2.GoogleAuth>()
    }
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
                clog("onError: AlFrontPile.google.auth2.then", e)
            }
        )
    }
}

external object gapi {
    fun load(what: String, block: () -> Unit)

    object auth2 {
        class GoogleAuth {
            fun then(onInit: () -> Unit, onError: (dynamic) -> Unit)
        }

        fun init(params: GApiClientConfig): GoogleAuth
    }
}

class GApiClientConfig(
    val client_id: String,
    val cookiepolicy: String
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

