package alraune.front

import alraune.shared.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent
import vgrechka.*
import vgrechka.kjs.*
import vgrechka.kjs.JQueryPile.byID
import vgrechka.kjs.JQueryPile.byIDSingle
import vgrechka.kjs.JQueryPile.jqbody
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.properties.Delegates.notNull
import kotlin.reflect.KFunction0
import kotlin.reflect.KProperty0

// https://alraune.local/orderCreationForm?frontMessAround=messAroundFront201
// https://alraune.local/orderParams?orderUUID=816dc211-2cfa-4423-a99a-fd0e3c8915ee&frontMessAround=messAroundFront301
// https://alraune.local/orderParamsForm?orderUUID=816dc211-2cfa-4423-a99a-fd0e3c8915ee

// TODO:vgrechka UI for showing logOfShitters
//               - Just ask backend to dump shit to its console
//               - Include stack traces along with UUIDs of shitters

fun main(args: Array<String>) {
    clog("I am alraune-front 4")
    window.asDynamic()[AlFrontDebug::class.simpleName] = AlFrontDebug

    jqDocumentReady {
        initDebugFacilities()

        parseShitFromBack()
        AlFrontPile.initShit()

        @Suppress("UnsafeCastFromDynamic")
        KJSPile.getURLParam("frontMessAround")?.let {
            val f = AlFrontDebug.asDynamic()[it] as? KFunction0<*> ?: bitch("$it is not a function")
            f()
        }
    }
}

private fun initDebugFacilities() {
    val body = document.body!!
    body.addEventListener("click", {e ->
        e as MouseEvent
        if (AlFrontPile.shitFromBack.debug_domElementStackTraces && e.ctrlKey) {
            e.preventAndStop()
            // clog("target =", e.target)
            val el = e.target as HTMLElement
            val stackID = el.getAttribute(AlSharedPile.attribute.data_tagCreationStackID) ?: bitch("1a23fa57-0fd2-404a-82cf-b300294aa6cc")
            async {
                AlFrontPile.post(AlPagePath.debug_post_dumpStackByID, JSON.stringify(DumpStackByIDPostData(stackID)))
                clog("Sent request for dumping stackID $stackID")
            }
        }
    }, /*capturingPhase*/ true)

    run {
        val drawerClass = "c-5ccefe3e-7cbf-4a0f-8d8e-3883f8dda8e3"
        val linkClass = "c-dc1eb630-2231-4eaf-b9a7-44b425badf7d"
        val linkStyle = "{display: block; color: white; padding: 2px;}"
        jqbody.append("""
            <style>
                .$drawerClass {
                    background: gray;
                    width: 3px;
                    position: absolute;
                    left: 0;
                    top: 0;
                    bottom: 0;
                    overflow-x: hidden;
                    opacity: 0.75;
                }
                .$drawerClass:hover {
                    width: 150px;
                }
                .$linkClass $linkStyle
                .$linkClass:hover $linkStyle
                .$linkClass:visited $linkStyle
                .$linkClass:active $linkStyle
                .$linkClass:focus $linkStyle
            </style>
        """)
        val drawerJQ = jq("<div class='$drawerClass'></div>")
        jqbody.append(drawerJQ)

        fun addItem(f: KFunction0<Unit>) {
            val itemJQ = jq("<a class='$linkClass' href='#'>${f.name}</a>")
            drawerJQ.append(itemJQ)
            itemJQ.on("click") {
                it.preventAndStop()
                f()
            }
        }

        addItem(AlFrontDebug::dumpBackCodePath)
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

    fun messAroundFront203() {
//        byIDSingle(AlDomID.createOrderForm_submitButton).click()
    }

    var messAroundFront301: (() -> Unit)? = null

    fun dumpBackCodePath() {
        async {
            AlFrontPile.post(AlPagePath.debug_post_dumpBackCodePath, JSON.stringify(
                DumpBackCodePathPostData(requestContextID = AlFrontPile.shitFromBack.requestContextID)))
            clog("Sent debug request")
        }
    }
}

private fun parseShitFromBack() {
    AlFrontPile.shitFromBack = run {
        val j = byIDSingle(AlDomID.shitPassedFromBackToFront)
        val dataShit = j.attr(AlSharedPile.attribute.data_shit)
        // clog("dataShit =", dataShit)
        JSON.parse<PieceOfShitFromBack>(dataShit)
    }
    clog("shitFromBack =", AlFrontPile.shitFromBack)
}


object AlFrontPile {
    val debug_sleepBeforePost = 1000
    var shitFromBack by notNull<PieceOfShitFromBack>()
    var pristineModalContentHTML by notNull<String>()

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

    suspend fun post(path: String, rawData: Any?) = await(postPromise(path, rawData))

    fun postPromise(path: String, rawData: Any?): Promise<String> {
//        val stackBeforeXHR: String = CaptureStackException().stack
        return Promise {resolve, reject ->
            val xhr = js("new XMLHttpRequest()")
            xhr.open("POST", shitFromBack.baseURL + path)
            xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded")

            xhr.onreadystatechange = {
                if (xhr.readyState == 4) {
                    if (xhr.status == 200) {
                        val response: String = xhr.responseText
                        // clog("response", response)
                        resolve(response)
                    } else {
                        reject(Exception("Got shitty backend response from $path: status = ${xhr.status}"))
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
            AlPageID.orderCreationForm -> frontInitPage_orderCreationForm()
            AlPageID.orderParams -> frontInitPage_orderParams()
        }
    }

    private fun frontInitPage_orderParams() {
        val hasErrors = shitFromBack.hasErrors ?: wtf("818b2f27-c252-4ce7-9d96-022e4936e7bf")
        if (!hasErrors) {
            AlFrontPile.pristineModalContentHTML = findShitBetweenMarkers(
                document.body!!.innerHTML,
                AlSharedPile.beginModalContentMarker,
                AlSharedPile.endModalContentMarker)
        }

        initOrderParamsLikeControls()

        fun handler() {
            byIDSingle(AlDomID.modalContent)[0]!!.outerHTML = AlFrontPile.pristineModalContentHTML
            initOrderParamsLikeControls()
            byIDSingle(AlDomID.orderParamsModal).asDynamic().modal()
        }
        byIDSingle(AlDomID.editOrderParamsButton).onClick {handler()}

        AlFrontDebug.messAroundFront301 = {
            handler()
        }
    }

    private fun frontInitPage_orderCreationForm() {
        initOrderParamsLikeControls()
    }

    fun findShitBetweenMarkers(haystack: String, beginMarker: String, endMarker: String): String {
        val i1 = haystack.indexOfOrNull(beginMarker) ?: wtf("c7ef8f87-c3ea-4d02-b31a-717dc1a8a01f")
        val i2 = haystack.indexOfOrNull(endMarker) ?: wtf("91747d64-af48-43a6-85ec-59a69994fd11")
        return haystack.substring(i1 + beginMarker.length, i2)
    }

    private fun initOrderParamsLikeControls() {
        val documentCategoryPicker = DocumentCategoryPicker()
        val button = byID(AlDomID.createOrderForm_submitButton)

        // TODO:vgrechka Remove event handlers
        fun submitButtonHandler() {
            clog("i am the fucking submitButtonHandler")
            showTicker()

            @Suppress("UNUSED_VARIABLE")
            val clazz = window.asDynamic()["alraune-front"].alraune.shared[OrderCreationFormPostData::class.simpleName]
            val inst = js("new clazz()")
            val propNames = JSObject.getOwnPropertyNames(inst)
            for (propName in propNames.toList() - listOf(OrderCreationFormPostData::documentCategoryID.name,
                                                         OrderCreationFormPostData::orderUUID.name)) {
                clog("propName", propName)
                inst[propName] = byIDSingle(AlSharedPile.fieldDOMID(propName)).getVal()
            }
            inst[OrderCreationFormPostData::documentCategoryID.name] = documentCategoryPicker.getSelectedCategoryID()
            inst[OrderCreationFormPostData::orderUUID.name] = shitFromBack.orderUUID
            val data = JSON.stringify(inst)
            // clog("data", data)
            async {
                sleep(debug_sleepBeforePost)
                // AlFrontPile.sleepTillEndOfTime()

                val html = post(shitFromBack.postPath, data)
                // clog("html", html)

                fun jerk(beginMarker: String, endMarker: String, idToReplace: String) {
                    val content = findShitBetweenMarkers(html, beginMarker, endMarker)
                    byIDSingle(idToReplace)[0]!!.outerHTML = content
                }

                jerk(AlSharedPile.beginShitPassedFromBackToFrontMarker, AlSharedPile.endShitPassedFromBackToFrontMarker, AlDomID.shitPassedFromBackToFront)
                parseShitFromBack()
                val modalJQ = byID(AlDomID.orderParamsModal).asDynamic()
                jerk(shitFromBack.replacement_beginMarker ?: wtf("3172e16b-1fd7-4774-b661-de17be1676dc"),
                     shitFromBack.replacement_endMarker ?: wtf("cc342050-d17f-4fb5-b316-b1f52c8371af"),
                     shitFromBack.replacement_id ?: wtf("40531be5-b7de-47e3-9f7e-6ff4b5f12c4c"))
                val hasErrors = shitFromBack.hasErrors ?: wtf("b7b2b8ef-dd9c-4212-bbc7-842d6ef91af0")
                if (!hasErrors) {
                    modalJQ.modal("hide")
                }

                initShit()
            }
        }

        fun make2xx(tamperWith: (OrderCreationFormPostData) -> OrderCreationFormPostData): () -> Unit {
            return {
                val data = tamperWith(OrderCreationFormPostData(
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
























