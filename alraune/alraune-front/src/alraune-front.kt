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
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KFunction0
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

fun main(args: Array<String>) {
    clog("I am alraune-front 4")
    window.asDynamic()[AlFrontDebug::class.simpleName] = AlFrontDebug

    jqDocumentReady {
        initDebugFacilities()

        parseShitFromBack()
        AlFrontPile.initShit()

        @Suppress("UnsafeCastFromDynamic")
        KJSPile.getURLParam("frontMessAround")?.let {
            val f = AlFrontDebug.asDynamic()[it] ?: bitch("de7c46eb-7ad6-4c39-a605-81b21aa4d539")
            f.call(AlFrontDebug)
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

object AlFrontDebug {
/*
https://alraune.local/orderCreationForm?frontMessAround=messAroundFront201
https://alraune.local/orderCreationForm?frontMessAround=messAroundFront202
https://alraune.local/orderCreationForm?frontMessAround=messAroundFront203
https://alraune.local/orderParams?orderUUID=fdfea4aa-1e1c-48f8-a341-a92d7e348961&frontMessAround=messAroundFront301
*/

    val AlFrontPile = alraune.front.AlFrontPile

    suspend fun awaitPageInitAfterDoing(block: () -> Unit) {
        AlFrontPile.pageInitSignal.reset()
        block()
        AlFrontPile.pageInitSignal.wait()
    }

    suspend fun clickSubmitAndAwaitPageInit() {
        awaitPageInitAfterDoing {
            byIDSingle(AlDomID.submitButton).click()
        }
    }

    suspend fun clickButtonAndAwaitModalShown(buttonID: String) {
        val lock = AlFrontPile.modalShownLock
        lock.reset()
        byIDSingle(buttonID).click()
        lock.pauseTestFromTest()
    }

    suspend fun awaitModalHiddenAfterDoing(block: suspend () -> Unit) {
        val lock = AlFrontPile.modalHiddenLock
        lock.reset()
        block()
        lock.pauseTestFromTest()
    }

    fun messAroundFront203() {
        async {
            run { // Errors in order creation form
                clickSubmitAndAwaitPageInit()
            }

            run { // Create order
                populateOrderParamsForm(
                    data = OrderCreationFormPostData(
                        orderUUID = "boobs",
                        email = "iperdonde@mail.com",
                        name = "Иммануил Пердондэ",
                        phone = "+38 (068) 4542823",
                        documentTypeID = "PRACTICE",
                        documentTitle = "Как я пинал хуи на практике",
                        documentDetails = "Детали? Я ебу, какие там детали...",
                        documentCategoryID = "boobs",
                        numPages = "35",
                        numSources = "7"),
                    documentCategoryPath = listOf(
                        AlDocumentCategories.humanitiesID,
                        AlDocumentCategories.linguisticsID))
                clickSubmitAndAwaitPageInit()
            }

            run { // Modal
                clickButtonAndAwaitModalShown(AlDomID.editOrderParamsButton)

                run { // Validation errors
                    AlFrontPile.documentCategoryPicker.debug_handleBackButtonClick()
                    populateOrderParamsForm(
                        data = OrderCreationFormPostData(
                            orderUUID = "boobs",
                            email = "fart@mail.com",
                            name = "Иммануил Пердондэ III",
                            phone = "bullshit",
                            documentTypeID = "ESSAY",
                            documentTitle = "Как я пинал большие хуи на практике",
                            documentDetails = "Детали? Я ебу, какие там детали... Да, ебу. И не ебет",
                            documentCategoryID = "boobs",
                            numPages = "55",
                            numSources = "3"),
                        documentCategoryPath = listOf(
                            AlDocumentCategories.technicalID,
                            AlDocumentCategories.programmingID))
                    clickSubmitAndAwaitPageInit()
                }

                run { // OK
                    byIDSingle(AlSharedPile.fieldDOMID(OrderCreationFormPostData::phone.name)).setVal("+38 (911) 4542823")
                    awaitModalHiddenAfterDoing {
                        clickSubmitAndAwaitPageInit()
                    }
                }
            }

            clog("We good... hopefully")
        }
    }

    private fun populateOrderParamsForm(data: OrderCreationFormPostData, documentCategoryPath: List<String>) {
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

        AlFrontPile.documentCategoryPicker.let {
            val categoryIDs = documentCategoryPath
            for (id in categoryIDs)
                it.debug_setSelectValue(id)
        }
    }

    fun messAroundFront301() {
        async {
            clickButtonAndAwaitModalShown(AlDomID.editOrderParamsButton)
        }
    }

    fun dumpBackCodePath() {
        async {
            AlFrontPile.post(AlPagePath.debug_post_dumpBackCodePath, JSON.stringify(
                DumpBackCodePathPostData(requestContextID = AlFrontPile.shitFromBack.requestContextID)))
            clog("Sent debug request")
        }
    }

    fun make2xx(tamperWith: (OrderCreationFormPostData) -> OrderCreationFormPostData): () -> Unit {
        return {
            async {
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

                AlFrontPile.documentCategoryPicker.let {
                    it.debug_setSelectValue(AlDocumentCategories.humanitiesID)
                    it.debug_setSelectValue(AlDocumentCategories.linguisticsID)
                }

                clickSubmitAndAwaitPageInit()
            }
        }
    }

    val messAroundFront201 = make2xx {it}
    val messAroundFront202 = make2xx {it.copy(email = "", phone = "bullshit", documentDetails = "")}
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
    var documentCategoryPicker by notNull<DocumentCategoryPicker>()
    val modalShownLock by notNullNamed(TestLock())
    val modalHiddenLock by notNullNamed(TestLock())

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

        AlFrontPile.pageInitSignal.resolve(Unit)
    }

    private fun frontInitPage_orderParams() {
        val hasErrors = shitFromBack.hasErrors ?: wtf("818b2f27-c252-4ce7-9d96-022e4936e7bf")
        if (!hasErrors) {
            AlFrontPile.pristineModalContentHTML = findShitBetweenMarkersForDOMID(
                document.body!!.innerHTML,
                AlDomID.modalContent)
        }

        initOrderParamsLikeControls()

        val modalJQ = byIDSingle(AlDomID.orderParamsModal).asDynamic()
        modalJQ.on("shown.bs.modal") {
            AlFrontPile.modalShownLock.resumeTestFromSut()
        }
        modalJQ.on("hidden.bs.modal") {
            AlFrontPile.modalHiddenLock.resumeTestFromSut()
        }

        fun handler() {
            byIDSingle(AlDomID.modalContent)[0]!!.outerHTML = AlFrontPile.pristineModalContentHTML
            initOrderParamsLikeControls()
            modalJQ.modal()
        }
        byIDSingle(AlDomID.editOrderParamsButton).onClick {handler()}
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
        val buttonJQ = byID(AlDomID.submitButton)

        // TODO:vgrechka Remove event handlers
        fun submitButtonHandler() {
            clog("i am the fucking submitButtonHandler")
            buttonJQ.attr("disabled", "true")
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

                // clog("html", html)

                val html = post(shitFromBack.postPath, data)
                replaceWithNewContent(AlDomID.shitPassedFromBackToFront, html)
                parseShitFromBack()
                val modalJQ = byID(AlDomID.orderParamsModal).asDynamic()
                replaceWithNewContent(shitFromBack.replacement_id ?: wtf("40531be5-b7de-47e3-9f7e-6ff4b5f12c4c"), html)

                if (shitFromBack.pageID == AlPageID.orderParams) {
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
        byIDSingle(domid)[0]!!.outerHTML = content
    }

}
























