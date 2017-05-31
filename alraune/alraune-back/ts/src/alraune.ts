/// <reference path="../../node_modules/@types/jquery/index.d.ts"/>
/// <reference path="Pile.ts"/>
/// <reference path="generated--ts-interop.ts"/>

import a = alraune
import clog = a.clog
import byIDSingle = a.byRawIDSingle
import getURLParam = a.getURLParam
import bitch = a.bitch
import preventAndStop = a.preventAndStop
import state = a.state
import modalShownAfterDoing = a.modalShownAfterDoing

$(() => {
    initShit()
    initDebugShit()
})

function exhausted(x: never): never {
    return bitch("3962cdb9-8dc2-43e6-a26d-0acf5b148d7a")
}

function initShit() {
    parseShitFromBackAndExecuteCommands()
}

function executeBackCommands(cmds: a.AlBackToFrontCommandPile[]) {
    for (const cmd of cmds) {
        a.executeBackToFrontCommand(cmd)
    }
}

function parseShitFromBackAndExecuteCommands() {
    const j = byIDSingle("shitPassedFromBackToFront2")
    state.backShit = JSON.parse(j.attr("data-shit"))
    // clog("backShit", JSON.stringify(state.backShit))

    executeBackCommands(state.backShit.commands)
}


function initDebugShit() {
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
            a.byDomidSingle("topRightButton").click()
        })
        clog("mooooodaaaaal shown")

        const entropy = a.nextIndexForTest()
        const o = state.debug.nameToStringValueControl
        o.name.setValue(`Fuckita Boobisto ${entropy}`)
        o.email.setValue(`fuckita-${entropy}@mail.com`)
        o.phone.setValue(`+38 (911) 4542877-${entropy}`)
    })

    function declareMaf(activeWhenPath: string, f: () => void) {
        const itemName = f.name
        if (window.location.href.startsWith(`https://alraune.local${activeWhenPath}?`)) {
            addItem(itemName, () => {
                const newHref = a.amendHref(window.location, "maf", itemName)
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















