/// <reference path="generated--by-backend.ts"/>

namespace Pile {

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
            propNameToTextControl: {} as {[key: string]: TextControl}
        }
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

    export function byIDSingle(id: string): JQuery {
        const j = byID(id)
        if (j.length != 1)
            bitch(`I want one element with ID [${id}], got ${j.length}`)
        return j
    }

    export function byID(id: string): JQuery {
        const selector = `#${id}`.replace(/\./, "\\.")
        return $(selector)
    }

    export function bitch(msg: string): never {
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

    export class TextControl {
        private jInput = $(`<input type="text" class="form-control">`)
        private cmd: AlBackToFrontCommand.CreateTextControl

        constructor(cmd: AlBackToFrontCommand.CreateTextControl) {
            state.debug.propNameToTextControl[cmd.propName] = this
            this.cmd = cmd
            this.setValue(cmd.value)
            byIDSingle(cmd.placeHolderDomid).replaceWith(this.jInput)
        }

        setValue(value: string) {
            this.jInput.val(value)
        }

    }
}

interface BackShit {
    commands: AlBackToFrontCommand.Type[]
}












