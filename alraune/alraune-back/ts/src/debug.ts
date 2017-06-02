namespace alraune {

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

        declareMaf("/orderParams", async function maf101() {
            await modalShownAfterDoing(() => {
                byDomidSingle("topRightButton").click()
            })
            clog("mooooodaaaaal shown")

            const entropy = nextIndexForTest()
            {const s = debug.setControlValue
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

        declareMaf("/orderCreationForm", async function maf201() {
            {const s = debug.setControlValue
                s("contactName", `Иммануил Пердондэ`)
                s("email", `iperdonde@mail.com`)
                s("phone", `+38 (068) 4542823`)
                s("documentType", "PRACTICE")
                s("documentTitle", "Как я пинал хуи на практике")
                s("documentDetails", "Детали? Я ебу, какие там детали...")
                s("numPages", "35")
                s("numSources", "7")
            }
            {const p = getDocumentCategoryPickerControl()
                p.debug_setSelectValue(AlUADocumentCategories.technicalID)
                p.debug_setSelectValue(AlUADocumentCategories.programmingID)
            }
            byDebugTag("submitButton").click()
        })

        declareMaf("/orderCreationForm", async function maf202() {
            { // Everything wrong
                byDebugTag("submitButton").click()
                if (1 === 1) return
            }
            {
                {const s = debug.setControlValue
                    s("contactName", `Иммануил Пердондэ`)
                    s("email", `bullshit`)
                    s("phone", `+38 (068) 4542823`)
                    s("documentType", "PRACTICE")
                    s("documentTitle", "Как я пинал хуи на практике")
                    s("documentDetails", "Детали? Я ебу, какие там детали...")
                    s("numPages", "35")
                    s("numSources", "7")
                }
                {const p = getDocumentCategoryPickerControl()
                    p.debug_setSelectValue(AlUADocumentCategories.technicalID)
                    p.debug_setSelectValue(AlUADocumentCategories.programmingID)
                }
                byDebugTag("submitButton").click()
            }
        })

        function getDocumentCategoryPickerControl(): DocumentCategoryPicker {
            return cast(state.debug.nameToControl.documentCategory, isDocumentCategoryPicker)
        }

        function declareMaf(activeWhenPath: string, f: () => void) {
            const itemName = f.name
            const fuck = `https://alraune.local${activeWhenPath}`
            const href = window.location.href
            if (href === fuck || href.startsWith(fuck + "?")) {
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
}


