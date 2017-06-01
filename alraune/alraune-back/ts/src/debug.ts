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

        const currentMafValue = getURLParam("maf")

        // declareMaf("maf401_createFile", "/orderFiles", async () => {
        //     clog("pizda")
        //     // clickElementByIDAndAwaitModalShown(AlDomID.topRightButton, AlFrontPile.topRightButtonModalTestLocks)
        //     //
        //     // run {
        //     //     // Validation errors
        //     //     p(OrderFileFormPostData::details, "In general your default keyboard mapping comes from your X server setup. If this setup is insufficient and you are unwilling to go through the process of reconfiguration and/or you are not the superuser you'll need to use the xmodmap program. This is the utility's global configuration file.")
        //     //     clickSubmitAndAwaitPageInit()
        //     //     // AlFrontPile.sleepTillEndOfTime()
        //     // }
        //     //
        //     // run {
        //     //     // OK
        //     //     awaitModalHiddenAfterDoing {
        //     //         p(OrderFileFormPostData::title, "The Fucking Keyboard Mapping")
        //     //         clickSubmitAndAwaitPageInit()
        //     //     }
        //     // }
        // })

        declareMaf("/orderParams", async function maf101() {
            clog(maf101.name)
            await modalShownAfterDoing(() => {
                byDomidSingle("topRightButton").click()
            })
            clog("mooooodaaaaal shown")

            const entropy = nextIndexForTest()
            {const s = debug.setControlValue
                s("name", `Fuckita Boobisto ${entropy}`)
                s("email", `fuckita-${entropy}@mail.com`)
                s("phone", `+38 (911) 4542877-${entropy}`)
            }
            {const p = cast(state.debug.nameToControl.documentCategory, isDocumentCategoryPicker)
                p.debug_handleBackButtonClick()
                p.debug_setSelectValue(AlUADocumentCategories.humanitiesID)
                p.debug_setSelectValue(AlUADocumentCategories.linguisticsID)
            }
        })

        function declareMaf(activeWhenPath: string, f: () => void) {
            const itemName = f.name
            if (window.location.href.startsWith(`https://alraune.local${activeWhenPath}?`)) {
                addItem(itemName, () => {
                    const newHref = amendHref(window.location, "maf", itemName)
                    console.log("newHref =", newHref)
                    window.location.href = newHref
                })
            }
            if (currentMafValue == itemName) {
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


