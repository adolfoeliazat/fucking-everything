/// <reference path="../../node_modules/@types/jquery/index.d.ts"/>
/// <reference path="Pile.ts"/>
/// <reference path="generated--by-backend.ts"/>

import clog = Pile.clog
import byIDSingle = Pile.byIDSingle
import getURLParam = Pile.getURLParam
import bitch = Pile.bitch
import setOnClick = Pile.setOnClick
import preventAndStop = Pile.preventAndStop
import state = Pile.state

$(() => {
    initShit()
    initDebugShit()
})

function exhausted(x: never): never {
    return bitch("3962cdb9-8dc2-43e6-a26d-0acf5b148d7a")
}

function initShit() {
    parseShitFromBackAndExecuteCommands()

    // onClick(byIDSingle(AlDomid.topRightButton), () => {
    //     clog("yeeeeeeeeaaaaaaaahhhhhhhhhh")
    // })
}

function executeBackCommands(cmds: AlBackToFrontCommand.Type[]) {
    for (const cmd of cmds) {
        switch (cmd.opcode) {
            case "SayWarmFuckYou": {
                clog(`Fuck you, ${cmd.toWhom}`)
            }break

            case "SetClickHandler": {
                const j = byIDSingle(cmd.targetDomid)
                setOnClick(j, () => {
                    executeBackCommands(cmd.actions)
                })
            }break

            default: exhausted(cmd)
        }
    }
}

function parseShitFromBackAndExecuteCommands() {
    const j = byIDSingle("shitPassedFromBackToFront2")
    state.backShit = JSON.parse(j.attr("data-shit"))
    console.log("backShit", JSON.stringify(state.backShit))

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

    const currentMafValue = getURLParam(Pile.httpGetParam.maf)

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
        clog("mmmaaaaaaaaaaaaaaffffffffff")
        byIDSingle(AlDomid.topRightButton).click()
    })

    function declareMaf(activeWhenPath: string, f: () => void) {
        const itemName = f.name
        if (window.location.href.startsWith(`https://alraune.local${activeWhenPath}?`)) {
            addItem(itemName, () => {
                const newHref = Pile.amendHref(window.location, Pile.httpGetParam.maf, itemName)
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


const AlDomid = {
    topRightButton: "",
    shitPassedFromBackToFront: "",
}


!function constantize() {
    for (const name of Object.getOwnPropertyNames(AlDomid)) {
        (AlDomid as any)[name] = name
    }
}()














