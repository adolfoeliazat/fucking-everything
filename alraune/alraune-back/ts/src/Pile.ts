/// <reference path="generated--ts-interop.ts"/>

namespace alraune {

    export function unpileDomid(p: AlBackToFrontCommandPile): string {
        let res
        if (p.rawDomid)
            res = p.rawDomid
        else if (p.domid)
            res = p.domid
        else
            throw new Error("I want a domid    6c8b79a1-d481-4842-9970-129b38a7eded")

        if (p.domidSuffix)
            res += `-${p.domidSuffix}`

        return res
    }

    export function executeBackToFrontCommand(pile: AlBackToFrontCommandPile) {
        clog(`cmd: ${pile.opcode}`)

        if (pile.opcode === "CreateTextControl") {
            // TODO:vgrechka Display error
            const jInput = $(`<input type="text" class="form-control">`)
            const me = {
                setValue(value: string) {
                    jInput.val(value)
                }
            }
            ;(state.debug.nameToStringValueControl as any)[pile.name] = me
            me.setValue(pile.stringValue)
            byRawIDSingle(unpileDomid(pile)).replaceWith(jInput)
            return
        }

        if (pile.opcode === "OpenModalOnElementClick") {
            const jTriggerElement = byRawIDSingle(pile.domid)
            setOnClick(jTriggerElement, () => {
                const jModal = $(pile.html)
                const jBody = $("body")
                const bodyUnderModalClass = "paddingRightScrollbarWidthImportant"

                jModal.on("show.bs.modal", () => {
                    jBody.css("overflow-y", "hidden")
                    jBody.addClass(bodyUnderModalClass)
                })

                jModal.on("shown.bs.modal", () => {
                    state.modalShown.resumeTestFromSut()
                })

                jModal.on("hide.bs.modal", () => {})

                jModal.on("hidden.bs.modal", () => {
                    jBody.css("overflow-y", "scroll")
                    jBody.removeClass(bodyUnderModalClass)
                    // locks.hidden.resumeTestFromSut()

                    jModal.data("bs.modal", null)
                    jModal.remove()
                })

                jBody.append(jModal)
                executeBackCommands(pile.initCommands)
                ;(jModal as any).modal()
            })
            return
        }

        wtf(`opcode = ${pile.opcode}    184e8001-a4eb-49fc-accb-ad17dabc052f`)
    }

    class ResolvableShit<T> {
        private _resolve: (value?: (PromiseLike<T> | T)) => void
        private _reject: (reason?: any) => void
        promise: Promise<T>

        constructor() {
            this.reset()
        }

        reset() {
            this.promise = new Promise<T>((resolve, reject) => {
                this._resolve = resolve
                this._reject = reject
            })
        }

        resolveWith(x: T) {
            this._resolve(x)
        }

        resolve() {
            this.resolveWith(undefined as any)
        }

        reject(x: any) {
            this._reject(x)
        }
    }

    class TestLock {
        private readonly testPauseTimeout = 10000
        private readonly sutPauseTimeout = 10000

        private testPause = new ResolvableShit()
        private sutPause = new ResolvableShit()

        constructor(virgin: boolean = false) {
            if (!virgin) { // Initially everything is resolved, so if not in test, shit just works
                this.testPause.resolve()
                this.sutPause.resolve()
            }
        }

        reset() {
            this.testPause.reset()
            this.sutPause.reset()
        }

        async pauseTestFromTest(): Promise<void> {
            await orTestTimeout({promise: this.testPause.promise, ms: this.testPauseTimeout})
        }

        resumeTestFromSut() {
            this.testPause.resolve()
        }
    }

    function orTestTimeout<T>({promise, ms} : {promise: Promise<T>, ms: number}): Promise<T> {
        const shit = new ResolvableShit<T>()
        const thePromiseName = (promise as any).name || "shit"
        setTimeout(() => {
            const msg = `Sick of waiting for ${thePromiseName}`
            shit.reject(new Error(msg))
        }, ms)
        promise.then(res => {
            shit.resolveWith(res)
        })
        return shit.promise
    }

    export async function modalShownAfterDoing(f: () => void): Promise<void> {
        state.modalShown.reset()
        f()
        await state.modalShown.pauseTestFromTest()
    }

    export const state = {
        backShit: {} as BackShit,
        modalShown: new TestLock(),
        debug: {
            nameToStringValueControl: {} as {[key in AlFrontToBackCommandPileProp]: StringValueControl}
        }
    }

    export interface StringValueControl {
        setValue(value: string): void
    }

    export const httpGetParam = {
        maf: "maf"
    }

    export function clog(message?: any, ...optionalParams: any[]) {
        console.log(message, ...optionalParams)
    }

    export function amendHref(loc: Location, paramName: string, paramValue: string): string {
        const params = loc.search
            .substring(1)
            .split("&")
            .map(x => {
                const xs = x.split("=")
                return {name: xs[0], value: xs[1]}
            })
            .filter(x => x.name != paramName)

        const newParams = [...params, {name: paramName, value: paramValue}]
        return loc.protocol + "//" + loc.host + loc.pathname
            + "?" + newParams.map(x => `${x.name}=${x.value}`).join("&")
    }

    export function getURLParam(name: string): string | undefined {
        const shit = decodeURIComponent(window.location.search.substring(1))
        for (const pairString of shit.split('&')) {
            const pair = pairString.split("=")
            if (pair[0] == name) {
                return pair[1]
            }
        }
    }

    export function byRawIDSingle(id: string): JQuery {
        const j = byID(id)
        if (j.length != 1)
            bitch(`I want one element with ID [${id}], got ${j.length}`)
        return j
    }

    export function byDomidSingle(domid: AlDomid): JQuery {
        return byRawIDSingle(domid)
    }

    export function byID(id: string): JQuery {
        const selector = `#${id}`.replace(/\./, "\\.")
        return $(selector)
    }

    export function bitch(msg: string): never {
        throw new Error(msg)
    }

    export function wtf(msg: string): never {
        throw new Error(msg)
    }

    export function setOnClick(j: JQuery, f: (e: JQueryEventObject) => void) {
        j.off("click")
        j.on("click", e => {
            preventAndStop(e)
            f(e)
        })
    }

    export function preventAndStop(e: JQueryEventObject) {
        e.preventDefault()
        e.stopPropagation()
    }


    export function nextIndexForTest(): number {
        let key = nextIndexForTest.name + ":value"
        let res = parseInt(localStorage.getItem(key) || "1", 10)
        localStorage.setItem(key, (res + 1).toString())
        return res
    }

    interface BackShit {
        commands: AlBackToFrontCommandPile[]
    }
}













