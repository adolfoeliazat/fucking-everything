package alraune.front

import alraune.shared.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent
import vgrechka.*
import vgrechka.kjs.*
import vgrechka.kjs.JQueryPile.byIDSingle
import vgrechka.kjs.JQueryPile.jqbody
import kotlin.browser.document
import kotlin.reflect.*

object AlFrontDebug {
/*
https://alraune.local/orderCreationForm?frontMessAround=messAroundFront201
https://alraune.local/orderCreationForm?frontMessAround=messAroundFront202
https://alraune.local/orderCreationForm?frontMessAround=messAroundFront203
https://alraune.local/orderParams?orderUUID=fdfea4aa-1e1c-48f8-a341-a92d7e348961&frontMessAround=messAroundFront301
*/

    val AlFrontPile = alraune.front.AlFrontPile

    fun initDebugFacilities() {
        val body = document.body!!
        body.addEventListener("click", {e ->
            e as MouseEvent
            if (AlFrontPile.shitFromBack.debug_domElementStackTraces && e.ctrlKey) {
                e.preventAndStop()
                // clog("target =", e.target)
                val el = e.target as HTMLElement
                val stackID = el.getAttribute(AlSharedPile.attribute.data_tagCreationStackID) ?: bitch("1a23fa57-0fd2-404a-82cf-b300294aa6cc")
                async {
                    AlFrontPile.serializeAndPost(AlPagePath.debug_post_dumpStackByID, DumpStackByIDPostData(stackID))
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

        putSomeShitIntoGlobal()
    }

    private fun putSomeShitIntoGlobal() {
        AlFrontPile.gloshit.fuck1 = byIDSingle("itemShit-b169d1b4-8b0f-4ace-a5cb-f765e46fb9a6")
        AlFrontPile.gloshit.fuck2 = {
            AlFrontPile.fuckElementAwayAndRemove("itemShit-b169d1b4-8b0f-4ace-a5cb-f765e46fb9a6") {
                clog("Done")
            }
        }
    }

    suspend fun awaitPageInitAfterDoing(block: () -> Unit) {
        AlFrontPile.pageInitSignal.reset()
        block()
        AlFrontPile.pageInitSignal.wait()
    }

    suspend fun awaitServiceFuckedUpBannerAfterDoing(block: () -> Unit) {
        AlFrontPile.serviceFuckedUpBannerSignal.reset()
        block()
        AlFrontPile.serviceFuckedUpBannerSignal.wait()
    }

    suspend fun clickSubmitAndAwaitPageInit() {
        awaitPageInitAfterDoing {
            clickSubmitButton()
        }
    }

    private fun clickSubmitButton() {
        val submitButtonJQ = byIDSingle(AlDomID.submitButton)
        submitButtonJQ.click()
    }

    suspend fun clickSubmitAndAwaitServiceFuckedUpBanner() {
        awaitServiceFuckedUpBannerAfterDoing {
            clickSubmitButton()
        }
    }

    suspend fun clickElementAndAwaitModalShown(j: JQuery, modalTestLocks: ModalTestLocks) {
        val lock = modalTestLocks.shown
        lock.reset()
        j.click()
        lock.pauseTestFromTest()
    }

    suspend fun awaitModalHiddenAfterDoing(block: suspend () -> Unit) {
        val lock = AlFrontPile.topRightButtonModalTestLocks.hidden
        lock.reset()
        block()
        lock.pauseTestFromTest()
    }

    @Suppress("unused")
    fun messAroundFront203() {
        async {
            run { // Validation errors in order creation form
                clickSubmitAndAwaitPageInit()
            }

            run { // Create order
                populateOrderParamsForm(
                    data = OrderParamsFormPostData(
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
                clickElementByIDAndAwaitModalShown(AlDomID.topRightButton, AlFrontPile.topRightButtonModalTestLocks)

                val fuckDatabase = true
                if (fuckDatabase) {
                    postDebugFuckDatabaseForNextPost()
                    clickSubmitAndAwaitServiceFuckedUpBanner()
                }

                run { // Validation errors
                    AlFrontPile.documentCategoryPicker.debug_handleBackButtonClick()
                    populateOrderParamsForm(
                        data = OrderParamsFormPostData(
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
                    val phoneJQ = byIDSingle(AlSharedPile.fieldDOMID(OrderParamsFormPostData::phone.name))
                    phoneJQ.setVal("+38 (911) 4542823")

                    awaitModalHiddenAfterDoing {
                        clickSubmitAndAwaitPageInit()
                    }
                }
            }

            AlFrontPile.reload()

            clog("We good... hopefully")
        }
    }

    private suspend fun postDebugFuckDatabaseForNextPost() {
        AlFrontPile.serializeAndPost(AlPagePath.debug_post_fuckDatabaseForNextPost, "boobs")
    }

    private fun populateOrderParamsForm(data: OrderParamsFormPostData, documentCategoryPath: List<String>) {
        // TODO:vgrechka @unboilerplate
        val p = AlFrontPile::populateTextField
        p(data::email)
        p(data::name)
        p(data::phone)
        p(data::documentTitle)
        p(data::documentDetails)
        p(data::numPages)
        p(data::numSources)
        p(data::documentTypeID)

        AlFrontPile.documentCategoryPicker.let {
            val categoryIDs = documentCategoryPath
            for (id in categoryIDs)
                it.debug_setSelectValue(id)
        }
    }

    fun messAroundFront301() {
        async {
            clickElementByIDAndAwaitModalShown(AlDomID.topRightButton, AlFrontPile.topRightButtonModalTestLocks)
        }
    }

    private suspend fun clickElementByIDAndAwaitModalShown(domid: String, modalTestLocks: ModalTestLocks) {
        clickElementAndAwaitModalShown(byIDSingle(domid), modalTestLocks)
    }

    @Suppress("unused")
    fun messAroundFront401() {
        async {
            val p = AlFrontPile::populateTextField2

            clickElementByIDAndAwaitModalShown(AlDomID.topRightButton, AlFrontPile.topRightButtonModalTestLocks)

            run { // Validation errors
                p(OrderFileFormPostData::details, "In general your default keyboard mapping comes from your X server setup. If this setup is insufficient and you are unwilling to go through the process of reconfiguration and/or you are not the superuser you'll need to use the xmodmap program. This is the utility's global configuration file.")
                clickSubmitAndAwaitPageInit()
                // AlFrontPile.sleepTillEndOfTime()
            }

            run { // OK
                awaitModalHiddenAfterDoing {
                    p(OrderFileFormPostData::title, "The Fucking Keyboard Mapping")
                    clickSubmitAndAwaitPageInit()
                }
            }
        }
    }

    @Suppress("unused")
    fun messAroundFront402() {
        async {
            val itemUUID = "9968705b-8879-46b1-99b9-26da1429501a"

            run {
                val j = byIDSingle("${AlDomID.deleteItemIcon}-$itemUUID")
                val modalTestLocks = j.getModalTestLocks()
                clickElementAndAwaitModalShown(j, modalTestLocks)
            }

            awaitPageInitAfterDoing {
                val j = byIDSingle("${AlDomID.deleteItemSubmitButton}-$itemUUID")
                j.click()
            }
        }
    }

    fun dumpBackCodePath() {
        async {
            AlFrontPile.serializeAndPost(AlPagePath.debug_post_dumpBackCodePath, DumpBackCodePathPostData(requestContextID = AlFrontPile.shitFromBack.requestContextID))
            clog("Sent debug request")
        }
    }

    fun make2xx(tamperWith: (OrderParamsFormPostData) -> OrderParamsFormPostData): () -> Unit {
        return {
            async {
                val data = tamperWith(OrderParamsFormPostData(
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
