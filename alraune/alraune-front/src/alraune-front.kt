package alraune.front

import alraune.shared.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent
import vgrechka.*
import vgrechka.kjs.*
import vgrechka.kjs.JQueryPile.byID
import vgrechka.kjs.JQueryPile.byIDSingle
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.properties.Delegates.notNull
import kotlin.reflect.KFunction0
import kotlin.reflect.KProperty0

// https://alraune.local/orderCreationForm?frontMessAround=messAroundFront201
// https://alraune.local/orderParams?orderUUID=816dc211-2cfa-4423-a99a-fd0e3c8915ee&frontMessAround=messAroundFront301
// https://alraune.local/orderParamsForm?orderUUID=816dc211-2cfa-4423-a99a-fd0e3c8915ee

fun main(args: Array<String>) {
    clog("I am alraune-front 4")
    window.asDynamic()[AlFrontDebug::class.simpleName] = AlFrontDebug

    jqDocumentReady {
        document.body!!.addEventListener("click", {e->
            e as MouseEvent
            if (AlFrontPile.shitFromBack.debug_domElementStackTraces && e.ctrlKey) {
                e.preventAndStop()
                // clog("target =", e.target)
                val el = e.target as HTMLElement
                val stackID = el.getAttribute(AlSharedPile.attribute.data_tagCreationStackID) ?: bitch("1a23fa57-0fd2-404a-82cf-b300294aa6cc")
                async {
                    AlFrontPile.post(AlFrontPile.shitFromBack.debug_urlForSendingStackID, JSON.stringify(DumpStackByIDRequest(stackID)))
                    clog("Sent request for dumping stackID $stackID")
                }
            }
        }, /*capturingPhase*/ true)

        parseShitFromBack()
        AlFrontPile.initShit()

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
    val AlFrontPile = alraune.front.AlFrontPile

    var messAroundFront201: (() -> Unit)? = null
    var messAroundFront202: (() -> Unit)? = null
    var messAroundFront301: (() -> Unit)? = null
}

private fun parseShitFromBack() {
    AlFrontPile.shitFromBack = run {
        val j = byIDSingle(AlDomID.shitPassedFromBackToFront)
        val dataShit = j.attr(AlSharedPile.attribute.data_shit)
        // clog("dataShit =", dataShit)
        JSON.parse<ShitPassedFromBackToFront>(dataShit)
    }
    clog("shitFromBack =", AlFrontPile.shitFromBack)
}


object AlFrontPile {
    val debug_sleepBeforePost = 1000
    var shitFromBack by notNull<ShitPassedFromBackToFront>()

//    object google {
//        var auth2 by notNullOnce<gapi.auth2.GoogleAuth>()
//    }

    val localStorage = AlLocalStorage(RealLocalStorage)
    val security = AlFrontSecurity()

    fun showTicker() {
        byIDSingle(AlDomID.ticker).css("display", "block")
    }

    fun populateTextField(prop: KProperty0<String>) {
        byIDSingle(AlSharedPile.fieldDOMID(prop)).setVal(prop.get())
    }

    suspend fun post(url: String, rawData: Any?) = await(postPromise(url, rawData))

    fun postPromise(url: String, rawData: Any?): Promise<String> {
//        val stackBeforeXHR: String = CaptureStackException().stack
        return Promise {resolve, reject ->
            val xhr = js("new XMLHttpRequest()")
            xhr.open("POST", url)
            xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded")

            xhr.onreadystatechange = {
                if (xhr.readyState == 4) {
                    if (xhr.status == 200) {
                        val response: String = xhr.responseText
                        // clog("response", response)
                        resolve(response)
                    } else {
                        reject(Exception("Got shitty backend response from $url: status = ${xhr.status}"))
                    }
                }
            }

            xhr.send(rawData)
        }
    }

    fun initShit() {
        AlFrontPile.shitFromBack.historyPushState?.let {
            window.history.pushState(null, "", it)
        }

        when (AlFrontPile.shitFromBack.pageID) {
            AlPageID.orderCreationForm -> dorothyOrderCreationFormPage()
            AlPageID.orderParams -> dorothyOrderParamsPage()
        }
    }

    private fun dorothyOrderParamsPage() {
        amanda(AmandaParams())

        fun handler() {
            byIDSingle(AlDomID.orderParamsModal).asDynamic().modal()
        }
        byIDSingle(AlDomID.editOrderParamsButton).onClickPreventStop {handler()}

        AlFrontDebug.messAroundFront301 = {
            handler()
        }
    }

    private fun dorothyOrderCreationFormPage() {
        amanda(AmandaParams())
    }

    class AmandaParams()

    fun findShitBetweenMarkers(haystack: String, beginMarker: String, endMarker: String): String {
        val i1 = haystack.indexOfOrNull(beginMarker) ?: wtf("c7ef8f87-c3ea-4d02-b31a-717dc1a8a01f")
        val i2 = haystack.indexOfOrNull(endMarker) ?: wtf("91747d64-af48-43a6-85ec-59a69994fd11")
        return haystack.substring(i1 + beginMarker.length, i2)
    }

    private fun amanda(p: AmandaParams) {
        val documentCategoryPicker = DocumentCategoryPicker()
        val button = byID(AlDomID.createOrderForm_submitButton)

        // TODO:vgrechka Remove event handlers
        fun submitButtonHandler() {
            clog("i am the fucking submitButtonHandler")
            showTicker()

            @Suppress("UNUSED_VARIABLE")
            val clazz = window.asDynamic()["alraune-front"].alraune.shared[OrderCreationForm::class.simpleName]
            val inst = js("new clazz()")
            val propNames = JSObject.getOwnPropertyNames(inst)
            for (propName in propNames.toList() - listOf(OrderCreationForm::documentCategoryID.name,
                                                         OrderCreationForm::orderUUID.name)) {
                clog("propName", propName)
                inst[propName] = byIDSingle(AlSharedPile.fieldDOMID(propName)).getVal()
            }
            inst[OrderCreationForm::documentCategoryID.name] = documentCategoryPicker.getSelectedCategoryID()
            inst[OrderCreationForm::orderUUID.name] = shitFromBack.orderUUID
            val data = JSON.stringify(inst)
            // clog("data", data)
            async {
                sleep(debug_sleepBeforePost)
                // AlFrontPile.sleepTillEndOfTime()

                val html = post(shitFromBack.postURL, data)
                // clog("html", html)

                fun jerk(beginMarker: String, endMarker: String, idToReplace: String) {
                    val content = findShitBetweenMarkers(html, beginMarker, endMarker)
                    byIDSingle(idToReplace)[0]!!.outerHTML = content
                }

                jerk(AlSharedPile.beginShitPassedFromBackToFrontMarker, AlSharedPile.endShitPassedFromBackToFrontMarker, AlDomID.shitPassedFromBackToFront)
                parseShitFromBack()
                val modalJQ = byID(AlDomID.orderParamsModal).asDynamic()
                jerk(shitFromBack.replacement_beginMarker, shitFromBack.replacement_endMarker, shitFromBack.replacement_id)
                modalJQ.modal("hide")

                initShit()
            }
        }

        fun make2xx(tamperWith: (OrderCreationForm) -> OrderCreationForm): () -> Unit {
            return {
                val data = tamperWith(OrderCreationForm(
                    orderUUID = "boobs",
                    email = "iperdonde@mail.com",
                    name = "Иммануил Пердондэ",
                    phone = "+38 (068) 4542823",
                    documentTypeID = "PRACTICE",
                    documentTitle = "Как я пинал хуи на практике",
                    documentDetails = "Детали? Я ебу, какие там детали...",
                    documentCategoryID = "boobs",
                    numPages = "35",
                    numSources = "7"))
                // TODO:vgrechka @improve d0fc960d-76be-4a0b-969c-7bbf94275e09
                val o = AlFrontPile::populateTextField
                o(data::email)
                o(data::name)
                o(data::phone)
                o(data::documentTitle)
                o(data::documentDetails)
                o(data::numPages)
                o(data::numSources)
                o(data::documentTypeID)

                documentCategoryPicker.debug_setSelectValue(AlDocumentCategories.humanitiesID)
                documentCategoryPicker.debug_setSelectValue(AlDocumentCategories.linguisticsID)
                submitButtonHandler()
            }
        }

        // https://alraune.local/order?frontMessAround=messAroundFront201
        AlFrontDebug.messAroundFront201 = make2xx {it}
        AlFrontDebug.messAroundFront202 = make2xx {it.copy(email = "", phone = "bullshit", documentDetails = "")}

        button.on("click") {
            it.preventAndStop()
            submitButtonHandler()
        }
    }

    suspend fun sleep(ms: Int) {
        await(delay(ms))
    }

    suspend fun sleepTillEndOfTime() {
        clog("===== Sleeping till the end of time =====")
        sleep(Int.MAX_VALUE)
    }

    fun delay(ms: Int): Promise<Unit> = Promise {resolve, _ ->
        window.setTimeout({resolve(Unit)}, ms)
    }
}
























//fun initGoogleAuth() {
//    gapi.load("auth2") {
//        clog("gapi.load")
//        AlFrontPile.google.auth2 = gapi.auth2.init(GApiClientConfig(
//            client_id = "1064147176813-n6l5pddt9qggcp9n4losnknb2dm5hl9t.apps.googleusercontent.com",
//            cookiepolicy = "single_host_origin"
//        ))
//        AlFrontPile.google.auth2.then(
//            onInit = {
//                clog("onInit: AlFrontPile.google.auth2.then")
//            },
//            onError = {e->
//                console.log("onError: AlFrontPile.google.auth2.then", e)
//            }
//        )
//    }
//}
//
//// https://developers.google.com/api-client-library/javascript/reference/referencedocs
//external object gapi {
//    fun load(what: String, block: () -> Unit)
//
//    object auth2 {
//        class GoogleAuth {
//            fun then(onInit: () -> Unit, onError: (dynamic) -> Unit)
//            fun signIn(options: GApiSignInOptions): Promise<dynamic>
//        }
//
//        fun init(params: GApiClientConfig): GoogleAuth
//    }
//}
//
//// https://developers.google.com/api-client-library/javascript/reference/referencedocs#gapiauth2clientconfig
//class GApiClientConfig(
//    val client_id: String,
//    val cookiepolicy: String
//)
//
//// https://developers.google.com/api-client-library/javascript/reference/referencedocs#gapiauth2signinoptions
//class GApiSignInOptions(
//    val redirect_uri: String
//)
//
//
//
//
//
///*
//// google.auth2.disconnect()
//// p = google.auth2.currentUser.get().getBasicProfile()
//// p.getName()
//
//$(_=> {
//    initGoogleAuth()
//
//    function initGoogleAuth() {
//        window.google = {}
//        gapi.load('auth2', _=> {
//            console.log('gapi.load')
//            google.auth2 = gapi.auth2.init({
//                client_id: '1064147176813-n6l5pddt9qggcp9n4losnknb2dm5hl9t.apps.googleusercontent.com',
//                cookiepolicy: 'single_host_origin'
//            })
//
//            google.auth2.then(
//                function onSuccess() {
//                    console.log('onSuccess: google.auth2.then')
//                },
//                function onError(error) {
//                    console.error('onError: google.auth2.then', error)
//                }
//            )
//
//            google.auth2.attachClickHandler(document.getElementById('googleSignInButton'),
//                {
//                    redirect_uri: pile.myIndexURL + '?page='
//                },
//                function onSuccess(googleUser) {
//                    console.log('onSuccess: google.auth2.attachClickHandler')
//                    google.user = googleUser
//                    var profile = google.user.getBasicProfile()
//                    console.log('ID: ' + profile.getId())
//                    console.log('Name: ' + profile.getName())
//                    console.log('Image URL: ' + profile.getImageUrl())
//                    console.log('Email: ' + profile.getEmail())
//                },
//                function onError(error) {
//                    console.error('onError: google.auth2.attachClickHandler', error)
//                }
//            )
//        })
//    }
//})
//
//*/

