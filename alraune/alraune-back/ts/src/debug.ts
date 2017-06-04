namespace alraune {
    export namespace debug {
        export function initDebugShit() {
            const drawerClass = "c-5ccefe3e-7cbf-4a0f-8d8e-3883f8dda8e3"
            const linkClass = "c-dc1eb630-2231-4eaf-b9a7-44b425badf7d"
            const linkStyle = "{display: block; color: white; padding: 2px;}"
            let jBody = $("body")
            jBody.append(`
            ${"<style>"}
            .${drawerClass} {
                background: gray;
                width: 3px;
                position: absolute;
                left: 0;
                top: 0;
                bottom: 0;
                overflow-x: hidden;
                opacity: 0.9;
            }
            .${drawerClass}:hover {
                width: 300px;
            }
            .${linkClass} ${linkStyle}
            .${linkClass}:hover ${linkStyle}
            .${linkClass}:visited ${linkStyle}
            .${linkClass}:active ${linkStyle}
            .${linkClass}:focus ${linkStyle}
            ${"</style>"}
        `)
            const jDrawer = $(`<div class="${drawerClass}"></div>`)
            jBody.append(jDrawer)

            const currentMafValue = getURLParam(AlURLParams.maf)

            declareMaf({activeWhenPath: "/orderParams"}, async function maf101() {
                await testStep("1", async () => {
                    await modalShownAfterDoing(() => {
                        byDomidSingle("topRightButton").click()
                    })
                    clog("mooooodaaaaal shown")
                })

                await testStep("2", async () => {
                    const entropy = nextIndexForTest()
                    {const s = setControlValue
                        s("contactName", `Fuckita Boobisto ${entropy}`)
                        s("email", `fuckita-${entropy}@mail.com`)
                        s("phone", `+38 (911) 4542877-${entropy}`)
                    }
                    {const p = getDocumentCategoryPickerControl()
                        p.debug_handleBackButtonClick()
                        p.debug_setSelectValue(AlUADocumentCategories.humanitiesID)
                        p.debug_setSelectValue(AlUADocumentCategories.linguisticsID)
                    }
                })
            })

            declareMaf({activeWhenPath: "/orderCreationForm"}, async function maf201_createOrder_happy() {
                await testStep("1", async () => {
                    {const s = setControlValue
                        s("contactName", `Иммануил Пердондэ`)
                        s("email", `iperdonde@mail.com`)
                        s("phone", `+38 (068) 4542823`)
                        s("documentType", "PRACTICE")
                        s("title", "Как я пинал хуи на практике")
                        s("details", "Детали? Я ебу, какие там детали...")
                        s("numPages", "35")
                        s("numSources", "7")
                    }
                    {const p = getDocumentCategoryPickerControl()
                        p.debug_setSelectValue(AlUADocumentCategories.technicalID)
                        p.debug_setSelectValue(AlUADocumentCategories.programmingID)
                    }
                    byDebugTag("submitButton").click()
                })
            })

            declareMaf({activeWhenPath: "/orderCreationForm"}, async function maf202_createOrder_validation() {
                await testStep("Everything's wrong", async () => {
                    byDebugTag("submitButton").click()
                    await sleep(1000)
                })

                await testStep("Slightly better", async () => {
                    {const s = setControlValue
                        s("contactName", `Иммануил Пердондэ`)
                        s("email", `bullshit`)
                        s("phone", `crap`)
                        s("documentType", "PRACTICE")
                        s("title", "Как я пинал хуи на практике")
                        s("details", "Детали? Я ебу, какие там детали...")
                        s("numPages", "1000")
                        s("numSources", "plenty")
                    }
                    {const p = getDocumentCategoryPickerControl()
                        p.debug_setSelectValue(AlUADocumentCategories.technicalID)
                        p.debug_setSelectValue(AlUADocumentCategories.programmingID)
                    }
                    byDebugTag("submitButton").click()
                    await sleep(1000)
                })

                await testStep("All good", async () => {
                    {const s = setControlValue
                        s("contactName", `Иммануил Пердондэ`)
                        s("email", `perdonde@mail.com`)
                        s("phone", `+38 (068) 4542823`)
                        s("documentType", "PRACTICE")
                        s("title", "Как я пинал хуи на практике")
                        s("details", "Детали? Я ебу, какие там детали...")
                        s("numPages", "35")
                        s("numSources", "7")
                    }
                    byDebugTag("submitButton").click()
                })
            })

            declareMaf({activeWhenPath: "/order", tabParam: "params"}, async function maf301_editOrderParams() {
                await openModal("Edit params")

                await testStep("With validation errors", async () => {
                    {const s = setControlValue
                        const entropy = nextIndexForTest()
                        s("contactName", `Иммануил Пердондэ ${entropy}`)
                        s("phone", `secret`)
                        s("documentType", "ESSAY")
                        s("title", "Как я пинал большие хуи на практике")
                        s("details", "Детали? Я ебу, какие там детали... Да, ебу! И не ебет")
                        s("numPages", "35")
                    }
                    {const p = getDocumentCategoryPickerControl()
                        p.debug_handleBackButtonClick()
                        p.debug_setSelectValue(AlUADocumentCategories.humanitiesID)
                        p.debug_setSelectValue(AlUADocumentCategories.linguisticsID)
                    }
                    await state.processedBackendResponse.reset_do_pauseTest(() => {
                        byDebugTag("submitButton").click()
                    })
                })

                await testStep("All good", async () => {
                    await sleep(0)
                    {const s = setControlValue
                        s("phone", `+38 (068) 5992823`)
                    }
                    byDebugTag("submitButton").click()
                })
            })

            declareMaf({activeWhenPath: "/order", tabParam: "files"}, async function maf401_createOrderFile() {
                await openModal("Create file")

                if (1 == 1) return
                await testStep("With validation errors", async () => {
                    {const s = setControlValue
                        const entropy = nextIndexForTest()
                        s("contactName", `Иммануил Пердондэ ${entropy}`)
                        s("phone", `secret`)
                        s("documentType", "ESSAY")
                        s("title", "Как я пинал большие хуи на практике")
                        s("details", "Детали? Я ебу, какие там детали... Да, ебу! И не ебет")
                        s("numPages", "35")
                    }
                    {const p = getDocumentCategoryPickerControl()
                        p.debug_handleBackButtonClick()
                        p.debug_setSelectValue(AlUADocumentCategories.humanitiesID)
                        p.debug_setSelectValue(AlUADocumentCategories.linguisticsID)
                    }
                    await state.processedBackendResponse.reset_do_pauseTest(() => {
                        byDebugTag("submitButton").click()
                    })
                })

                await testStep("All good", async () => {
                    await sleep(0)
                    {const s = setControlValue
                        s("phone", `+38 (068) 5992823`)
                    }
                    byDebugTag("submitButton").click()
                })
            })


            function getDocumentCategoryPickerControl(): DocumentCategoryPicker {
                return cast(state.debug.getControlForProp("documentCategory"), isDocumentCategoryPicker)
            }

            type DeclareMafParams = {activeWhenPath: string, tabParam?: string}
            function declareMaf(p: DeclareMafParams, f: () => void) {
                const itemName = f.name
                const alice = `https://alraune.local${p.activeWhenPath}`
                const href = window.location.href

                let wannaAdd = href === alice || href.startsWith(alice + "?")
                if (wannaAdd && p.tabParam)
                    wannaAdd = p.tabParam === alraune.getURLParam("tab")
                if (wannaAdd) {
                    addItem(itemName, () => {
                        const newHref = amendHref(window.location, AlURLParams.maf, itemName)
                        console.log("newHref =", newHref)
                        window.location.href = newHref
                    })
                }

                if (currentMafValue == itemName) {
                    clog("Executing MAF:", itemName)
                    f()
                }
            }

            function addItem(name: String, block: () => void) {
                const jItem = $(`<div><a class='${linkClass}' href='#'>${name}</a></div>`)
                jDrawer.append(jItem)
                jItem.on("click", e => {
                    preventAndStop(e)
                    block()
                })
            }
        }

        function setControlValue(prop: AlFrontToBackCommandPileProp, value: any) {
            const ctrl = state.debug.getControlForProp(prop)
            if (typeof value === "string") {
                cast(ctrl, isStringValueControl).setValue(value)
            }
            else wtf("54da9c71-2b48-40dc-b265-17d5809ee013")
        }


        async function openModal(descr: string) {
            await testStep(`Open modal: ${descr}`, async () => {
                await state.modalShown.reset_do_pauseTest(() => {
                    byDebugTag("topRightButton").click()
                })
            })
        }

        async function testStep(title: string, f: () => Promise<void>): Promise<void> {
            await sleep(0) // Till all DOM manipulations settle
            clog(`===== testStep: ${title} =====`)
            await f()
        }

    }
}


