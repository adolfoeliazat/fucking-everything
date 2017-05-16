package alraune.front

import vgrechka.kjs.*

fun main(args: Array<String>) {
    console.log("I am alraune-front")
}

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

