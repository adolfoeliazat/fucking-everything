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
import kotlin.properties.ReadWriteProperty
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

external class WeakMap<in K: Any, V: Any?> {
    fun delete(key: K): Boolean
    operator fun get(key: K): V?
    fun has(key: K): Boolean
    operator fun set(key: K, value: V): WeakMap<K, V>
}

inline fun <K: Any, V> WeakMap<K, V>.getOrPut(key: K, defaultValue: () -> V): V {
    val value = get(key)
    return if (value == null) {
        val answer = defaultValue()
        set(key, answer)
        answer
    } else {
        value
    }
}

object NamesOfThings {
    private val thingToName = WeakMap<Any, String>()
    private val sourceToSinks = WeakMap<Any, MutableList<Any>>()

    operator fun set(thing: Any, name: String) {
        thingToName[thing] = name
        val sinks = sourceToSinks[thing]
        sinks?.forEach {set(it, name)}
    }

    operator fun get(thing: Any): String? {
        return thingToName[thing]
    }

    fun flow(from: Any, to: Any) {
        val sinks = sourceToSinks.getOrPut(from) {mutableListOf()}
        sinks += to

        get(from)?.let {name->
            sinks.forEach {set(it, name)}
        }
    }

    fun unflow(from: Any, to: Any) {
        sourceToSinks[from]?.let {sinks->
            sinks -= to
        }
    }
}

class ResolvableShit<T> {
    private var _resolve by notNull<(T) -> Unit>()
    private var _reject by notNull<(Throwable) -> Unit>()
    private var _promise by notNull<Promise<T>>()
    private var hasPromise = false

    init {
        reset()
    }

    val promise: Promise<T> get() = _promise
    fun resolve(value: T) = _resolve(value)
    fun reject(e: Throwable) = _reject(e)

    suspend fun get(): T = await(promise)
    suspend fun wait(): T = await(promise)

    fun reset() {
        if (hasPromise) {
            NamesOfThings.unflow(this, promise)
        }

        _promise = Promise<T> {resolve, reject ->
            this._resolve = resolve
            this._reject = reject
        }
        hasPromise = true
        NamesOfThings.flow(this, promise)
    }
}

fun ResolvableShit<Unit>.resolve() = this.resolve(Unit)


private fun parseShitFromBack() {
    AlFrontPile.shitFromBack = run {
        val shitJQ = byIDSingle(AlDomID.shitPassedFromBackToFront, "12e47778-8369-4b1b-9817-c9a2a4b2200c")
        val dataShit = shitJQ.attr(AlSharedPile.attribute.data_shit)
        // clog("dataShit =", dataShit)
        JSON.parse<PieceOfShitFromBack>(dataShit)
    }
    clog("shitFromBack =", AlFrontPile.shitFromBack)
}

fun <T: Any> notNullNamed(initial: T, parentNamed: Any? = null): ReadWriteProperty<Any?, T> = NotNullNamedVar(initial, parentNamed)

private class NotNullNamedVar<T: Any>(initial: T?, val parentNamed: Any? = null) : ReadWriteProperty<Any?, T> {
    private var value: T? = initial

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val res = value ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
        val namePrefix = parentNamed?.let {NamesOfThings[it]}?.let {"$it."} ?: ""
        NamesOfThings[res] = namePrefix + property.name
        return res
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

fun timeoutSet(ms: Int, cb: () -> Unit) {
    window.setTimeout(cb, ms)
}

fun <T> Promise<T>.orTestTimeout(ms: Int, getPromiseName: (() -> String?)? = null): Promise<T> {
    val shit = ResolvableShit<T>()
    val thePromiseName = getPromiseName?.invoke() ?: "shit"
    timeoutSet(ms) {
        val msg = "Sick of waiting for $thePromiseName"
//        if (isTestPausedOnAssertion()) {
//            // console.warn("--- $msg, but not dying because test is paused on assertion ---")
//        } else {
            shit.reject(Exception(msg))
//        }
    }
    this.then({shit.resolve(it)})
    return shit.promise
}

fun <T> Promise<T>.orTestTimeoutNamedAfter(ms: Int, getPromiseNameBearer: () -> Any): Promise<T> {
    return this.orTestTimeout(ms, getPromiseName = {NamesOfThings[getPromiseNameBearer()]})
}

class TestLock(
    virgin: Boolean = false,
    val testPauseTimeout: Int = 10000,
    val sutPauseTimeout: Int = 10000
) {
    private val testPause by notNullNamed(ResolvableShit<Unit>(), parentNamed = this)
    private val sutPause by notNullNamed(ResolvableShit<Unit>(), parentNamed = this)

    init {
        if (!virgin) { // Initially everything is resolved, so if not in test, shit just works
            testPause.resolve()
            sutPause.resolve()
        }
    }

    fun reset() {
        testPause.reset()
        sutPause.reset()
    }

    suspend fun pauseTestFromTest() {
        await(testPause.promise.orTestTimeoutNamedAfter(testPauseTimeout, {testPause}))
    }

    fun resumeSutFromTest() {
        sutPause.resolve()
    }

    fun resumeTestFromSut() {
        testPause.resolve()
    }

    suspend fun resumeTestAndPauseSutFromSut() {
        testPause.resolve()
        await(sutPause.promise.orTestTimeoutNamedAfter(sutPauseTimeout, {sutPause}))
    }
}

object AlFrontPile {
    val isDebugMode = true
    val debug_sleepBeforePost = 1000
    var shitFromBack by notNull<PieceOfShitFromBack>()
    var pristineModalContentHTML by notNull<String>()
    val pageInitSignal = ResolvableShit<Unit>()
    val serviceFuckedUpBannerSignal = ResolvableShit<Unit>()
    var documentCategoryPicker by notNull<DocumentCategoryPicker>()
    val modalShownLock by notNullNamed(TestLock())
    val modalHiddenLock by notNullNamed(TestLock())

//    object google {
//        var auth2 by notNullOnce<gapi.auth2.GoogleAuth>()
//    }

    val localStorage = AlLocalStorage(RealLocalStorage)
    val security = AlFrontSecurity()

    fun setTickerVisible(b: Boolean) {
        val tickerJQ = byIDSingle(AlDomID.ticker, "34da400e-9e82-4318-956b-ecec1fa39bc3")
        tickerJQ.css("display", if (b) "block" else "none")
    }

    fun populateTextField2(prop: KProperty1<OrderFileFormPostData, String>, value: String) {
        val fieldJQ = byIDSingle(AlSharedPile.fieldDOMID(prop), "2d2ed5b7-2a5d-4713-8ce0-10a4f050ce09")
        fieldJQ.setVal(value)
    }

    fun populateTextField(prop: KProperty0<String>) {
        val fieldJQ = byIDSingle(AlSharedPile.fieldDOMID(prop), "b26f4a4f-b8a0-4573-8bde-a8eb6149280f")
        fieldJQ.setVal(prop.get())
    }

    suspend fun postRaw(path: String, rawData: Any?) = await(postRawPromise(path, rawData))

    fun postRawPromise(path: String, rawData: Any?): Promise<String> {
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
            AlPageID.orderFiles -> frontInitPage_orderFiles()
        }

        AlFrontPile.pageInitSignal.resolve()
    }

    private fun frontInitPage_orderParams() {
        initModal(initFormControls = {initOrderParamsLikeControls()})
    }

    private fun frontInitPage_orderFiles() {
        initModal(initFormControls = {initSubmitButton()})
    }

    private fun initModal(initFormControls: () -> Unit) {
        val hasErrors = shitFromBack.hasErrors ?: wtf("07ebdde7-3a73-48bc-992a-54aba4cfb986")
        if (!hasErrors) {
            pristineModalContentHTML = findShitBetweenMarkersForDOMID(
                document.body!!.innerHTML,
                AlDomID.modalContent)
        }

        initFormControls()

        val modalJQ = byIDSingle(AlDomID.orderParamsModal, "c33cb863-3d92-45ad-ad31-3c15afc59a28")
        modalJQ.on("shown.bs.modal") {
            modalShownLock.resumeTestFromSut()
        }
        modalJQ.on("hidden.bs.modal") {
            modalHiddenLock.resumeTestFromSut()
        }

        val topRightButtonJQ = byIDSingle(AlDomID.topRightButton, "c40a46f8-fe29-42a3-87ef-4a9d8455d659")
        topRightButtonJQ.onClick {
            val modalContentJQ = byIDSingle(AlDomID.modalContent, "a5197d1b-eff7-4c1e-8715-19d231a0a5a1")
            modalContentJQ[0]!!.outerHTML = pristineModalContentHTML
            initFormControls()
            modalJQ.modal()
        }
    }

    private fun frontInitPage_orderCreationForm() {
        initOrderParamsLikeControls()
    }

    fun findShitBetweenMarkersForDOMID(haystack: String, id: String): String {
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
                val fieldJQ = byIDSingle(AlSharedPile.fieldDOMID(propName), "a7bc93b7-d01b-4a09-bd60-7a649b7289d6")
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
                    val formFooterAreaJQ = byIDSingle(AlDomID.formFooterArea, "7812f976-0fd3-4fdd-ade3-ebeefd2afe64")
                    val buttonsJQ = formFooterAreaJQ.find("button")
                    check(buttonJQ.length > 0) {"259e87e0-fddc-465b-86e7-2c2b0edafe74"}
                    buttonsJQ.attr("disabled", false)
                    setTickerVisible(false)

                    val formBannerAreaJQ = byIDSingle(AlDomID.formBannerArea, "2aa01a8d-f16f-4a30-ac9a-f6a852d8733e")
                    formBannerAreaJQ.children().css("display", "none")
                    val serviceFuckedUpBannerJQ = byIDSingle(AlDomID.serviceFuckedUpBanner, "9ee9eea2-9280-4d78-8280-aef0dbd6448a")
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
                    val fieldJQ = byIDSingle(AlSharedPile.fieldDOMID(unmangledPropName), "a7bc93b7-d01b-4a09-bd60-7a649b7289d6")
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
                    val formFooterAreaJQ = byIDSingle(AlDomID.formFooterArea, "7812f976-0fd3-4fdd-ade3-ebeefd2afe64")
                    val buttonsJQ = formFooterAreaJQ.find("button")
                    check(buttonJQ.length > 0) {"259e87e0-fddc-465b-86e7-2c2b0edafe74"}
                    buttonsJQ.attr("disabled", false)
                    setTickerVisible(false)

                    val formBannerAreaJQ = byIDSingle(AlDomID.formBannerArea, "2aa01a8d-f16f-4a30-ac9a-f6a852d8733e")
                    formBannerAreaJQ.children().css("display", "none")
                    val serviceFuckedUpBannerJQ = byIDSingle(AlDomID.serviceFuckedUpBanner, "9ee9eea2-9280-4d78-8280-aef0dbd6448a")
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
        val content = findShitBetweenMarkersForDOMID(html, domid)
        val elementJQ = byIDSingle(domid, "b2218cc4-a1db-4647-b890-a2572dc25819")
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
























