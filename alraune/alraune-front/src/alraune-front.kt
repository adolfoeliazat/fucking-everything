package alraune.front

import alraune.shared.*
import vgrechka.*
import vgrechka.kjs.*
import vgrechka.kjs.JQueryPile.byID
import vgrechka.kjs.JQueryPile.byIDSingle
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.js.json
import kotlin.properties.Delegates.notNull
import kotlin.reflect.*

// TODO:vgrechka Smarter way of dealing of name mangling

fun main(args: Array<String>) {
    clog("I am alraune-front 4")
    window.asDynamic()[AlFrontDebug::class.simpleName] = AlFrontDebug

    jqDocumentReady {
        AlFrontDebug.initDebugFacilities()

        parseShitFromBack()
        AlFrontPile.initShit()

        val maf = KJSPile.getURLParam("maf")
        @Suppress("UnsafeCastFromDynamic")
        maf?.let {
            val f = AlFrontDebug.asDynamic()[it] ?: bitch("maf = $maf    de7c46eb-7ad6-4c39-a605-81b21aa4d539")
            f.call(AlFrontDebug)
        }
    }
}

fun serializeForPosting(obj: Any?): String {
    return AlFrontPile.jsonize2(obj)
//    val unmangledObj = js("({})")
//    for (name in JSObject.getOwnPropertyNames(obj)) {
//        val unmangledName = name.substringBefore("_")
//        unmangledObj[unmangledName] = obj[name]
//    }
//    return JSON.stringify(unmangledObj)
}



class AlFrontSecurity {
    fun isSignedIn(): Boolean {
        return false
    }
}

private fun parseShitFromBack() {
    AlFrontPile.shitFromBack = run {
        val shitJQ = byIDSingle(AlDomID.shitPassedFromBackToFront)
        val dataShit = shitJQ.attr(AlSharedPile.attribute.data_shit)
        // clog("dataShit =", dataShit)
        JSON.parse<PieceOfShitFromBack>(dataShit)
    }
    clog("shitFromBack =", AlFrontPile.shitFromBack)
}


object AlFrontPile {
    val isDebugMode = true
    val debug_sleepBeforePost = 1000
    var shitFromBack by notNull<PieceOfShitFromBack>()
    var pristineModalContentHTML by notNull<String>()
    val pageInitSignal = ResolvableShit<Unit>()
    val serviceFuckedUpBannerSignal = ResolvableShit<Unit>()
    var documentCategoryPicker by notNull<DocumentCategoryPicker>()
    val topRightButtonModalTestLocks = ModalTestLocks()

//    object google {
//        var auth2 by notNullOnce<gapi.auth2.GoogleAuth>()
//    }

    val localStorage = AlLocalStorage(RealLocalStorage)
    val security = AlFrontSecurity()

    fun setTickerVisible(b: Boolean) {
        val tickerJQ = byIDSingle(AlDomID.ticker)
        tickerJQ.css("display", if (b) "block" else "none")
    }

    fun populateTextField2(prop: KProperty1<OrderFileFormPostData, String>, value: String) {
        val fieldJQ = byIDSingle(AlSharedPile.fieldDOMID(prop))
        fieldJQ.setVal(value)
    }

    fun populateTextField(prop: KProperty0<String>) {
        val fieldJQ = byIDSingle(AlSharedPile.fieldDOMID(prop))
        fieldJQ.setVal(prop.get())
    }

    suspend fun postRaw(path: String, rawData: Any?) = await(postRawPromise(path, rawData))

    fun postRawPromise(path: String, rawData: Any?): Promise<String> {
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

    class CandyPoop<T : Any>(val isCandy: Boolean) {
        var candy by notNullOnce<T>()
        var poop by notNullOnce<Throwable>()

        val isPoop = !isCandy

        companion object {
            fun <T : Any> candy(candy: T): CandyPoop<T> {
                val res = CandyPoop<T>(isCandy = true)
                res.candy = candy
                return res
            }

            fun <T : Any> poop(poop: Throwable): CandyPoop<T> {
                val res = CandyPoop<T>(isCandy = false)
                res.poop = poop
                return res
            }
        }
    }

    fun postRawCB(path: String, rawData: Any?, cb: (CandyPoop<String>) -> Unit) {
        val xhr = js("new XMLHttpRequest()")
        xhr.open("POST", shitFromBack.baseURL + path)
        xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded")

        xhr.onreadystatechange = {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) {
                    val response: String = xhr.responseText
                    cb(CandyPoop.candy(response))
                } else {
                    cb(CandyPoop.poop(Exception("Got shitty backend response from $path: status = ${xhr.status}")))
                }
            }
        }

        xhr.send(rawData)
    }

    fun initShit() {
        AlFrontPile.shitFromBack.historyPushState?.let {
            window.history.pushState(null, "", it)
        }

        when (AlFrontPile.shitFromBack.pageID) {
            AlPageID.orderCreationForm -> frontInitPage_orderCreationForm()
            AlPageID.orderParams -> frontInitPage_orderParams()
            AlPageID.orderFiles -> frontInitPage_orderFiles()
        }

        AlFrontPile.pageInitSignal.resolve()
    }

    private fun frontInitPage_orderParams() {
        initTopRightButtonModal(initFormControls = {initOrderParamsLikeControls()})
    }

    private fun frontInitPage_orderFiles() {
        initTopRightButtonModal(initFormControls = {initSubmitButton()})
        initDeleteItemModals()
    }

    fun initDeleteItemModals() {
        val postPath = AlPagePath.post_deleteOrderFile

        val jIcons = jq("i[id|='${AlDomID.deleteItemIcon}']")
        for (i in 0 until jIcons.length) {
            val jIcon = jIcons.eq(i)
            val jIconID = jIcon.attr("id")
            val hyphen = jIconID.indexOfOrNull("-") ?: wtf("15ac565f-8b61-451e-a6c3-aeea08ee8fdf")
            val itemUUID = jIconID.substring(hyphen + 1)

            val jModal = byIDSingle("${AlDomID.deleteItemModal}-$itemUUID")
            val locks = ModalTestLocks()
            jIcon.setModalTestLocks(locks)
            tieTestModalLock(jModal, locks)
            jIcon.onClick {
                jModal.modal()
            }

            val jSubmitButton = byIDSingle("${AlDomID.deleteItemSubmitButton}-$itemUUID")
            val jCancelButton = byIDSingle("${AlDomID.deleteItemCancelButton}-$itemUUID")
            jSubmitButton.onClick {
                val jTicker = byIDSingle("${AlDomID.deleteItemTicker}-$itemUUID")
                jSubmitButton.attr("disabled", "true")
                jCancelButton.attr("disabled", "true")
                jTicker.css("display", "block")

                timeoutSet(debug_sleepBeforePost) {
                    postRawCB(postPath, serializeForPosting(DeleteItemPostData(itemUUID)), fun(res) {
                        if (res.isPoop) {
                            jSubmitButton.attr("disabled", null)
                            jCancelButton.attr("disabled", null)
                            jTicker.css("display", "none")

                            val jFormBannerArea = byIDSingle("${AlDomID.formBannerArea}-$itemUUID")
                            jFormBannerArea.children().css("display", "none")
                            val jServiceFuckedUpBanner = byIDSingle("${AlDomID.serviceFuckedUpBanner}-$itemUUID")
                            jServiceFuckedUpBanner.css("display", "block")

                            AlFrontPile.serviceFuckedUpBannerSignal.resolve()
                            return
                        }

                        val html = res.candy
                        replaceWithNewContent(AlDomID.shitPassedFromBackToFront, html)
                        parseShitFromBack()

                        val hasErrors = shitFromBack.hasErrors ?: wtf("b7b2b8ef-dd9c-4212-bbc7-842d6ef91af0")
                        if (hasErrors) {
                            replaceWithNewContent(AlDomID.deleteItemModalContent, html)
                        } else {
                            jModal.modal("hide")
                        }
                    })
                }
            }
        }
    }

    private fun initTopRightButtonModal(initFormControls: () -> Unit) {
        val hasErrors = shitFromBack.hasErrors ?: wtf("07ebdde7-3a73-48bc-992a-54aba4cfb986")
        if (!hasErrors) {
            pristineModalContentHTML = findShitBetweenMarkersForDomid(
                document.body!!.innerHTML,
                AlDomID.modalContent)
        }

        initFormControls()

        val jModal = byIDSingle(AlDomID.orderParamsModal)
        tieTestModalLock(jModal, topRightButtonModalTestLocks)

        val topRightButtonJQ = byIDSingle(AlDomID.topRightButton)
        topRightButtonJQ.onClick {
            val modalContentJQ = byIDSingle(AlDomID.modalContent)
            modalContentJQ[0]!!.outerHTML = pristineModalContentHTML
            initFormControls()
            jModal.modal()
        }
    }

    private fun tieTestModalLock(jModal: JQuery, locks: ModalTestLocks) {
        jModal.on("shown.bs.modal") {
            locks.shown.resumeTestFromSut()
        }
        jModal.on("hidden.bs.modal") {
            locks.hidden.resumeTestFromSut()
        }
    }

    private fun frontInitPage_orderCreationForm() {
        initOrderParamsLikeControls()
    }

    fun findShitBetweenMarkersForDomid(haystack: String, id: String): String {
        val beginMarker = AlSharedPile.beginContentMarkerForDOMID(id)
        val endMarker = AlSharedPile.endContentMarkerForDOMID(id)
        val i1 = indexOfOrBitchNoisily(haystack, beginMarker, "abe97b2e-f40a-4f3a-9b8f-38642695b22a")
        val i2 = indexOfOrBitchNoisily(haystack, endMarker, "52d8da6a-7e19-41ae-8065-be94247314d9")
        return haystack.substring(i1 + beginMarker.length, i2)
    }

    private fun indexOfOrBitchNoisily(haystack: String, needle: String, errorMessage: String): Int {
        val index = haystack.indexOfOrNull(needle)
        if (index == null) {
            if (AlFrontPile.isDebugMode) {
                clog("needle =", needle)
                clog("haystack =", haystack)
            }
            bitch(errorMessage)
        }
        return index
    }

    private fun initOrderParamsLikeControls() {
        val documentCategoryPicker = DocumentCategoryPicker()
        AlFrontPile.documentCategoryPicker = documentCategoryPicker
        initSubmitButton()
    }

    fun setPossiblyMangledProperty(obj: dynamic, name: String, value: dynamic) {
        val mangledPropertyName = JSObject.getOwnPropertyNames(obj).find {
            it.contains("_")
            && it.substringBefore("_") == name}
        obj[mangledPropertyName ?: name] = value
    }

    fun pageHasModal() = shitFromBack.pageID in setOf(AlPageID.orderParams, AlPageID.orderFiles)

    fun initSubmitButton() {
        val buttonJQ = byID(AlDomID.submitButton)

        fun submitButtonHandler() {
            clog("i am the fucking submitButtonHandler")
            buttonJQ.attr("disabled", "true")
            setTickerVisible(true)

            fun getTextField(propName: String): String {
                val fieldJQ = byIDSingle(AlSharedPile.fieldDOMID(propName))
                return fieldJQ.getVal() ?: bitch("4f9a730a-8636-4b36-b4fd-ec57cfdc4af8")
            }

            val inst: Any = when (AlFrontPile.shitFromBack.pageID) {
                AlPageID.orderCreationForm -> OrderParamsFormPostData(
                    orderUUID = null,
                    documentCategoryID = documentCategoryPicker.getSelectedCategoryID(),
                    email = getTextField(OrderParamsFormPostData::email.name),
                    name = getTextField(OrderParamsFormPostData::name.name),
                    phone = getTextField(OrderParamsFormPostData::phone.name),
                    documentTypeID = getTextField(OrderParamsFormPostData::documentTypeID.name),
                    documentTitle = getTextField(OrderParamsFormPostData::documentTitle.name),
                    documentDetails = getTextField(OrderParamsFormPostData::documentDetails.name),
                    numPages = getTextField(OrderParamsFormPostData::numPages.name),
                    numSources = getTextField(OrderParamsFormPostData::numSources.name))

                AlPageID.orderParams -> OrderParamsFormPostData(
                    orderUUID = shitFromBack.orderUUID,
                    documentCategoryID = documentCategoryPicker.getSelectedCategoryID(),
                    email = getTextField(OrderParamsFormPostData::email.name),
                    name = getTextField(OrderParamsFormPostData::name.name),
                    phone = getTextField(OrderParamsFormPostData::phone.name),
                    documentTypeID = getTextField(OrderParamsFormPostData::documentTypeID.name),
                    documentTitle = getTextField(OrderParamsFormPostData::documentTitle.name),
                    documentDetails = getTextField(OrderParamsFormPostData::documentDetails.name),
                    numPages = getTextField(OrderParamsFormPostData::numPages.name),
                    numSources = getTextField(OrderParamsFormPostData::numSources.name))

                AlPageID.orderFiles -> OrderFileFormPostData(
                    orderUUID = shitFromBack.orderUUID,
                    fileUUID = null,
                    title = getTextField(OrderFileFormPostData::title.name),
                    details = getTextField(OrderFileFormPostData::details.name),
                    name = "pizda")

                else -> wtf("14c54600-425f-4586-b0a9-e61f7f33d0f2")
            }

            clog(inst)
            val data = serializeForPosting(inst)

            // clog("data", data)
            async {
                sleep(debug_sleepBeforePost)
                // AlFrontPile.sleepTillEndOfTime()

                // clog("html", html)

                val html = try {
                    postRaw(shitFromBack.postPath, data)
                } catch (e: dynamic) {
                    val formFooterAreaJQ = byIDSingle(AlDomID.formFooterArea)
                    val buttonsJQ = formFooterAreaJQ.find("button")
                    check(buttonJQ.length > 0) {"259e87e0-fddc-465b-86e7-2c2b0edafe74"}
                    buttonsJQ.attr("disabled", false)
                    setTickerVisible(false)

                    val formBannerAreaJQ = byIDSingle(AlDomID.formBannerArea)
                    formBannerAreaJQ.children().css("display", "none")
                    val serviceFuckedUpBannerJQ = byIDSingle(AlDomID.serviceFuckedUpBanner)
                    serviceFuckedUpBannerJQ.css("display", "block")

                    AlFrontPile.serviceFuckedUpBannerSignal.resolve()
                    return@async
                }

                replaceWithNewContent(AlDomID.shitPassedFromBackToFront, html)
                parseShitFromBack()
                val modalJQ = byID(AlDomID.orderParamsModal).asDynamic()
                replaceWithNewContent(shitFromBack.replacement_id ?: wtf("40531be5-b7de-47e3-9f7e-6ff4b5f12c4c"), html)


                if (pageHasModal()) {
                    val hasErrors = shitFromBack.hasErrors ?: wtf("b7b2b8ef-dd9c-4212-bbc7-842d6ef91af0")
                    if (!hasErrors) {
                        modalJQ.modal("hide")
                    }
                }

                initShit()
            }
        }

        buttonJQ.onClick {
            submitButtonHandler()
        }
    }

    fun killme_initSubmitButton(postDataClass: KClass<*>, customPropNames: List<String>, setCustomProps: (dynamic) -> Unit) {
        val buttonJQ = byID(AlDomID.submitButton)

        fun submitButtonHandler() {
            clog("i am the fucking submitButtonHandler")
            buttonJQ.attr("disabled", "true")
            setTickerVisible(true)

            @Suppress("UNUSED_VARIABLE")
            val clazz = window.asDynamic()["alraune-front"].alraune.shared[postDataClass.simpleName]
            val inst = js("new clazz()")
            val propNames = JSObject.getOwnPropertyNames(inst)
            for (propName in propNames.toList()) {
                val unmangledPropName = propName.substringBefore("_")
                if (unmangledPropName !in customPropNames) {
                    clog("propName", propName)
                    val fieldJQ = byIDSingle(AlSharedPile.fieldDOMID(unmangledPropName))
                    inst[propName] = fieldJQ.getVal()
                }
            }
            setCustomProps(inst)
            val data = serializeForPosting(inst)
            // clog("data", data)
            async {
                sleep(debug_sleepBeforePost)
                // AlFrontPile.sleepTillEndOfTime()

                // clog("html", html)

                val html = try {
                    postRaw(shitFromBack.postPath, data)
                } catch (e: dynamic) {
                    val formFooterAreaJQ = byIDSingle(AlDomID.formFooterArea)
                    val buttonsJQ = formFooterAreaJQ.find("button")
                    check(buttonJQ.length > 0) {"259e87e0-fddc-465b-86e7-2c2b0edafe74"}
                    buttonsJQ.attr("disabled", false)
                    setTickerVisible(false)

                    val formBannerAreaJQ = byIDSingle(AlDomID.formBannerArea)
                    formBannerAreaJQ.children().css("display", "none")
                    val serviceFuckedUpBannerJQ = byIDSingle(AlDomID.serviceFuckedUpBanner)
                    serviceFuckedUpBannerJQ.css("display", "block")

                    AlFrontPile.serviceFuckedUpBannerSignal.resolve()
                    return@async
                }

                replaceWithNewContent(AlDomID.shitPassedFromBackToFront, html)
                parseShitFromBack()
                val modalJQ = byID(AlDomID.orderParamsModal).asDynamic()
                replaceWithNewContent(shitFromBack.replacement_id ?: wtf("40531be5-b7de-47e3-9f7e-6ff4b5f12c4c"), html)


                if (pageHasModal()) {
                    val hasErrors = shitFromBack.hasErrors ?: wtf("b7b2b8ef-dd9c-4212-bbc7-842d6ef91af0")
                    if (!hasErrors) {
                        modalJQ.modal("hide")
                    }
                }

                initShit()
            }
        }

        buttonJQ.onClick {
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

    fun replaceWithNewContent(domid: String, html: String) {
        val content = findShitBetweenMarkersForDomid(html, domid)
        val elementJQ = byIDSingle(domid)
        elementJQ[0]!!.outerHTML = content
    }

    fun reload() {
        window.location.reload()
    }

    suspend fun serializeAndPost(pagePath: String, data: dynamic) {
        AlFrontPile.postRaw(pagePath, serializeForPosting(data))
    }

    val gloshit: dynamic = window.asDynamic()

    // TODO:vgrechka @revisit
    fun jsonize2(shit: Any?): String {
        val out = jsonizeToObject2(shit)
        val json = js("JSON").stringify(out)
        return json
    }

    // TODO:vgrechka @revisit
    fun jsonizeToObject2(shit: Any?): Any? {
        fun noise(vararg xs: Any?) {
            if (true) clog("jsonizeToObject2", *xs)
        }

        gloshit.gloshit_jsonizeToObject2 = shit
        if (shit == null) return null
        val jsType = jsTypeOf(shit)
        if (jsType in setOf("string", "number", "boolean")) return shit
        if (jsType != "object") wtf("jsType: $jsType")

        when (shit) {
            is List<*> -> {
                return Array(shit.size) {i->
                    jsonizeToObject2(shit[i])
                }
            }
            is Long -> {
                return shit.toString()
            }
            is KClass<*> -> {
                return json("@class" to "kotlin.reflect.KClass",
                            "simpleName" to shit.simpleName)
            }
            else -> {
                val out = json()
                val className = shit::class.simpleName
                noise("className", className)
                out["@class"] = "alraune.shared." + className

                val props = JSObject.getOwnPropertyNames(shit.asDynamic())
                val protoProps = JSObject.getOwnPropertyNames(shit.asDynamic().__proto__).toSet() - setOf("constructor")
                for (prop in protoProps + props) {
                    if (!prop.startsWith("component") && !prop.startsWith("copy_") && prop != "toString" && prop != "hashCode" && prop != "equals") {
                        val value = shit.asDynamic()[prop]
                        noise("prop", prop, value)
                        out[prop] = jsonizeToObject2(value)
                    }
                }

                return out
            }
        }
    }

}

























