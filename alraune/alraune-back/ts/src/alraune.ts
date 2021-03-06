import AlFrontToBackCommandPile = alraune.AlFrontToBackCommandPile

namespace alraune {

    export function run<T>(f: () => T): T {
        return f()
    }

    export function runIgnoring<T>(f: () => T): void {
        f()
    }

    export function cast<T>(shit: any, check: (x: any) => x is T): T {
        if (check(shit))
            return shit
        else {
            if (typeof shit === "object" && shit.__stackAtCreation)
                console.warn("shit.__stackAtCreation", shit.__stackAtCreation)

            let shitDescription: string = typeof shit
            if (shitDescription === "object")
                shitDescription = "shit"
            return wtf(`One does not simply cast ${shitDescription} via ${check.name}`, {shit, check})
        }
    }

    export function castIndexed<T>(shitContainer: any, index: string | number, check: (x: any) => x is T): T {
        const shit = shitContainer[index]
        if (check(shit))
            return shit
        else {
            if (typeof shit === "object" && shit.__stackAtCreation)
                console.warn("shit.__stackAtCreation", shit.__stackAtCreation)

            let shitDescription: string = typeof shit
            if (shitDescription === "object")
                shitDescription = "shit"
            return wtf(`One does not simply cast ${shitDescription} via ${check.name}`, {shitContainer, index, shit, check})
        }
    }

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

    export async function executeBackToFrontCommand(pile: AlBackToFrontCommandPile): Promise<void> {
        clog(`cmd: ${pile.opcode}`)

        if (pile.opcode === "CreateControl") {
            let control: object | undefined
            let afterControlDOMCreated = () => {}

            let html = ""
            html += `<div style="position: relative;">`


            if (pile.controlType === "Text") {
                html += `<input type="text" class="form-control">`
            }
            else if (pile.controlType === "TextArea") {
                html += `<textarea class="form-control" rows="5"></textarea>`
            }
            else if (pile.controlType === "Select") {
                html += `<select class="form-control">`
                for (const item of pile.titledValues) {
                    const selected = item.value === pile.stringValue ? "selected" : ""
                    html += `<option value="${escapeHTML(item.value)}" ${selected}>${escapeHTML(item.title)}</option>`
                }
                html += `</select>`
            }
            else if (pile.controlType === "DocumentCategoryPicker") usualCrappyControl(DocumentCategoryPicker)
            else if (pile.controlType === "ButtonBarWithTicker") usualCrappyControl(ButtonBarWithTicker)
            else wtf(`23b6c33d-f7ab-491f-a761-9b47d24cbdb3`, pile)

            function usualCrappyControl(ctor: new() => Alice10) {
                const co = new ctor()
                co.bob.backPile = pile
                control = co
                html += co.bob.placeholderHTML()
                afterControlDOMCreated = () => {co.init()}
            }

            if (pile.error) {
                html += `<div style="margin-top: 5px;
                                     margin-right: 9px;
                                     text-align: right;
                                     color: ${Color.RED_700};">${escapeHTML(pile.error)}</div>
                                     
                         <div style="width: 15px;
                                     height: 15px;
                                     background-color: ${Color.RED_300};
                                     border-radius: 10px;
                                     position: absolute; 
                                     top: 10px; 
                                     right: 8px"></div>`
            }
            html += `</div>`

            if (pile.putInFormGroup) {
                let formGroupStyle = ""
                if (pile.error)
                    formGroupStyle += "margin-bottom: 0;"

                html = `<div class="form-group" style="${formGroupStyle}">
                            <label>${escapeHTML(pile.title)}</label>
                            ${html}
                        </div>`
            }

            const jShit = $(html)

            if (!control) {
                if (pile.controlType === "Text" || pile.controlType === "TextArea" || pile.controlType === "Select") {
                    const selector = run(() => {
                        if (pile.controlType === "Text") return "input"
                        if (pile.controlType === "TextArea") return "textarea"
                        else if (pile.controlType === "Select") return "select"
                        else return wtf(`f090486c-fb4e-4da7-889a-f44fbfc5faa9`, pile)
                    })
                    const jControl = JQPile.ensureSingle(jShit.find(selector))

                    const co = new class implements StringValueControl, Focusable, FrontToBackContributor {
                        /// @augment 662e6426-5c73-49d5-b876-fe1ace1230b1

                        contributeToFrontToBackCommand(): void {
                            state2.frontToBackCommand[pile.ftbProp] = jControl.val()
                        }

                        setValue(value: string) {
                            jControl.val(value)
                        }

                        focus() {
                            jControl.focus()
                        }
                    }
                    control = co
                    co.setValue(pile.stringValue)
                }
                else wtf(`adf4ab63-23c2-40bc-b059-c0232cabcdb2`, pile)
            }

            state.uuidToSomething[pile.controlUUID] = control
            state.debug.putControlForProp(pile.ftbProp, control)
            // ;(state.debug.nameToControl as any)[pile.name] = control
            byIDSingle(unpileDomid(pile)).replaceWith(jShit)
            afterControlDOMCreated()
        }

        else if (pile.opcode === "OpenModalOnElementClick") {
            function fuck() {
                const jTriggerElement = byIDSingle(unpileDomid(pile))
                setOnClick(jTriggerElement, async () => {
                    const jModal = $(pile.html)
                    const jBody = $("body")
                    const bodyUnderModalClass = "paddingRightScrollbarWidthImportant"

                    jModal.on("show.bs.modal", () => {
                        jBody.css("overflow-y", "hidden")
                        jBody.addClass(bodyUnderModalClass)
                    })

                    jModal.on("shown.bs.modal", async () => {
                        state.jShownModal = jModal
                        await executeBackCommands(pile.onModalShown)
                        state.modalShown.resumeTestFromSut()
                    })

                    jModal.on("hide.bs.modal", () => {})

                    jModal.on("hidden.bs.modal", () => {
                        jBody.css("overflow-y", "scroll")
                        jBody.removeClass(bodyUnderModalClass)
                        jModal.data("bs.modal", null)
                        jModal.remove()
                        state.jShownModal = undefined
                        state.modalHidden.resumeTestFromSut()
                    })

                    jBody.append(jModal)
                    await executeBackCommands(pile.initCommands)
                    ;(jModal as any).modal()
                })
            }
            ;(window as any).fuck = fuck
            fuck()
        }

        else if (pile.opcode === "SayFuckYou") {
            clog("Yeah, fuck you... sure...")
        }

        else if (pile.opcode === "FocusControl") {
            controlByProp(pile.ftbProp).which(isFocusable).focus()
        }

        else if (pile.opcode === "SetTickerActive") {
            const ticker = cast(state.uuidToSomething[pile.controlUUID], isTicker)
            ticker.setActive(pile.bool)
        }

        else if (pile.opcode === "CallBackend") {
            state2.frontToBackCommand = {} as AlFrontToBackCommandPile
            state2.frontToBackCommand.orderUUID = pile.ftbOrderUUID
            state2.frontToBackCommand.itemUUID = pile.ftbItemUUID
            state2.frontToBackCommand.opcode = pile.ftbOpcode
            for (const controlUUID of pile.readValuesOfControlsWithUUIDs) {
                const contributor = castIndexed(state.uuidToSomething, controlUUID, isFrontToBackContributor)
                contributor.contributeToFrontToBackCommand()
            }

            const xhr = new XMLHttpRequest()
            xhr.open("POST", pile.postURL)
            xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded")

            xhr.onreadystatechange = () => {
                if (xhr.readyState === 4) {
                    if (xhr.status == 200) {
                        state.backResponse = JSON.parse(xhr.responseText)
                        executeBackCommands(state.backResponse.commands)
                        state.processedBackendResponse.resumeTestFromSut()
                    } else {
                        console.error(`Got shitty response from backend: status = ${xhr.status}`)
                        // TODO:vgrechka Show error banner
                    }
                }
            }

            const shitForSending = state2.frontToBackCommand
            clog({shitForSending})
            const debugDelay = 1000
            setTimeout(() => {
                xhr.send(JSON.stringify(shitForSending))
            }, debugDelay)
        }

        else if (pile.opcode === "ReplaceElement") {
            let jTemporaryNodeContainer: JQuery; {
                const domid = "jTemporaryNodeContainer"
                let j = byIDNoneOrSingle(domid)
                if (!j) {
                    j = $(`<div id="${domid}" style="display: block;"></div>`)
                    $("body").append(j)
                }
                jTemporaryNodeContainer = j
            }

            const domid = unpileDomid(pile)
            const jElementToBeReplaced = byIDSingle(domid)
            jElementToBeReplaced.find("*").addBack().attr("id", "toBeReplaced")
            const jNewElement = $(pile.html)
            if (jNewElement.attr("id") !== domid)
                bitch("97b83d01-53bf-4e85-b54a-8d7d9a9b016f", {pile})
            jTemporaryNodeContainer.append(jNewElement)
            await executeBackCommands(pile.initCommands)

            jElementToBeReplaced.replaceWith(jNewElement)
        }

        else if (pile.opcode === "SetLocationHref") {
            window.location.href = pile.href
        }

        else if (pile.opcode === "OnClick") {
            setOnClick(byIDSingle(unpileDomid(pile)), async () => {await executeBackCommands(pile.commands)})
        }

        else if (pile.opcode === "CloseModal") {
            if (!state.jShownModal) bitch("c3f1540d-57e0-4f2b-bd87-bb4dd82da7e0")
            await state.modalHidden.reset_do_pauseTest(() => {
                ;(state.jShownModal as any).modal("hide")
            })
        }

        else if (pile.opcode === "FuckElementOut") {
            const j = byIDSingle(unpileDomid(pile))
            j.addClass(AlFrontPile.fuckOutClass)
            await sleep(AlFrontPile.delayBeforeReplacingFuckingOutElement)
        }

        else wtf(`184e8001-a4eb-49fc-accb-ad17dabc052f`, {pile})
    }

    export const AlFrontPile = new class {
        readonly fuckOutClass = "fuckOut"
        readonly fuckOutDuration = 500
        readonly delayBeforeReplacingFuckingOutElement = 450
    }

    export function controlByProp(prop: AlFrontToBackCommandPileProp) {
        // const shit = state.debug.nameToControl[prop]
        const shit = state.debug.getControlForProp(prop)
        return {
            which<T>(check: (x: any) => x is T): T {
                return cast(shit, check)
            }
        }
    }

    export interface Focusable {
        focus(): void
    }

    export function isFocusable(x: any): x is Focusable {
        return x && x.__isFocusable
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

        reset_do_pauseTest(f: () => void): Promise<void> {
            this.reset()
            f()
            return this.pauseTestFromTest()
        }
    }

    export const state = new class {
        backResponse = {} as AlBackResponsePile
        readonly modalShown = new TestLock()
        readonly modalHidden = new TestLock()
        readonly processedBackendResponse = new TestLock()
        readonly uuidToSomething = {} as {[K: string]: any}
        jShownModal = undefined as JQuery | undefined

        readonly debug = new class {
            private readonly propToControl = {} as {[K in AlFrontToBackCommandPileProp]?: any}

            putControlForProp(prop: AlFrontToBackCommandPileProp, control: any) {
                clog(`putNameToControl: ${prop}`, {control, stackCapture: new Error("Capturing stack")})
                this.propToControl[prop] = control
            }

            getControlForProp(prop: AlFrontToBackCommandPileProp): any {
                return this.propToControl[prop]
            }
        }
    }

    export const state2 = new class {
        frontToBackCommand: AlFrontToBackCommandPile
    }

    function orTestTimeout<T>({promise, ms} : {promise: Promise<T>, ms: number}): Promise<T> {
        const shit = new ResolvableShit<T>()
        const thePromiseName = (promise as any).name || "shit"
        setTimeout(()=>{
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

    export interface StringValueControl {
        setValue(value: string): void
    }

    export function isStringValueControl(x: any): x is StringValueControl {
        return x && x.__isStringValueControl
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

    export function byDebugTag(tag: AlDebugTag): JQuery {
        const j = $(`*[data-debugTag="${tag}"]`)
        if (j.length != 1)
            bitch(`I want one element with debug tag [${tag}], got ${j.length}`)
        return j
    }

    export function byIDNoneOrSingle(id: string): JQuery | undefined {
        const j = byID(id)
        if (j.length === 0)
            return undefined
        else if (j.length === 1)
            return j
        else
            bitch(`I want either none or single element with ID [${id}], got ${j.length}`)
    }

    export function byDomidSingle(domid: AlDomid): JQuery {
        return byIDSingle(domid)
    }

    export function byID(id: string): JQuery {
        const selector = `#${id}`.replace(/\./, "\\.")
        return $(selector)
    }

    export function bitch(msg: string, ctx: any = undefined): never {
        if (ctx !== undefined)
            console.warn("ctx", ctx)
        throw new Error(msg)
    }

    export function wtf(msg: string, ctx: any = undefined): never {
        if (ctx !== undefined)
            console.warn("ctx", ctx)
        throw new Error(msg)
    }

    export function imf(msg: string, ctx: any = undefined): never {
        if (ctx !== undefined)
            console.warn("ctx", ctx)
        throw new Error("Implement me, please, fuck you... " + msg)
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

    export namespace JQPile {
        export function ensureSingle(j: JQuery): JQuery {
            if (j.length != 1)
                bitch(`I want one and only one element, got ${j.length}`)
            return j
        }
    }

    export function escapeHTML(s: string): string {
        return s
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("'", "&#39;")
            .replace("\"", "&#34;")
    }

    let _nextUID = 1
    export function nextUID(): string {
        return "uid" + _nextUID++
    }

    function exhausted(x: never): never {
        return bitch("3962cdb9-8dc2-43e6-a26d-0acf5b148d7a")
    }

    export async function initShit() {
        $("body").append(`<style>
            .${AlFrontPile.fuckOutClass} {
                animation-name: fuckOut;
                animation-duration: ${AlFrontPile.fuckOutDuration}ms;
                animation-iteration-count: 1;
            }
            @keyframes fuckOut {
                0% {opacity: 1;}
                100% {opacity: 0;}
            }
        </style>`)

        const initialBackResponse = (window as any).initialBackResponse
        clog({initialBackResponse})
        state.backResponse = initialBackResponse
        await executeBackCommands(state.backResponse.commands)
        debug.initDebugShit()
    }

    export async function executeBackCommands(cmds: AlBackToFrontCommandPile[]): Promise<void> {
        clog("---------------------")
        for (const cmd of cmds) {
            await executeBackToFrontCommand(cmd)
        }
    }

    export function withIndex<T>(xs: T[]): [number, T][] {
        let i = 0
        return xs.map(x => [i++, x]) as [number, T][]
    }

    export interface FrontToBackContributor {
        contributeToFrontToBackCommand(): void
    }

    export function isFrontToBackContributor(x: any): x is FrontToBackContributor {
        return x && x.__isFrontToBackContributor
    }

    export async function sleep(ms: number): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            setTimeout(resolve, ms)
        })
    }

    export function sleepTillEndOfTime(): Promise<void> {
        console.warn("============ Sleeping till the end of time ===========")
        return sleep(2147483647) // More causes no delay at all
    }


    export const Color = {
        // https://www.google.com/design/spec/style/color.html#color-color-palette
        BLACK: "#000000", BLACK_BOOT: "#333333", WHITE: "#ffffff",
        RED_50: "#ffebee", RED_100: "#ffcdd2", RED_200: "#ef9a9a", RED_300: "#e57373", RED_400: "#ef5350", RED_500: "#f44336", RED_600: "#e53935", RED_700: "#d32f2f", RED_800: "#c62828", RED_900: "#b71c1c", RED_A100: "#ff8a80", RED_A200: "#ff5252", RED_A400: "#ff1744", RED_A700: "#d50000",
        PINK_50: "#fce4ec", PINK_100: "#f8bbd0", PINK_200: "#f48fb1", PINK_300: "#f06292", PINK_400: "#ec407a", PINK_500: "#e91e63", PINK_600: "#d81b60", PINK_700: "#c2185b", PINK_800: "#ad1457", PINK_900: "#880e4f", PINK_A100: "#ff80ab", PINK_A200: "#ff4081", PINK_A400: "#f50057", PINK_A700: "#c51162",
        PURPLE_50: "#f3e5f5", PURPLE_100: "#e1bee7", PURPLE_200: "#ce93d8", PURPLE_300: "#ba68c8", PURPLE_400: "#ab47bc", PURPLE_500: "#9c27b0", PURPLE_600: "#8e24aa", PURPLE_700: "#7b1fa2", PURPLE_800: "#6a1b9a", PURPLE_900: "#4a148c", PURPLE_A100: "#ea80fc", PURPLE_A200: "#e040fb", PURPLE_A400: "#d500f9", PURPLE_A700: "#aa00ff",
        DEEP_PURPLE_50: "#ede7f6", DEEP_PURPLE_100: "#d1c4e9", DEEP_PURPLE_200: "#b39ddb", DEEP_PURPLE_300: "#9575cd", DEEP_PURPLE_400: "#7e57c2", DEEP_PURPLE_500: "#673ab7", DEEP_PURPLE_600: "#5e35b1", DEEP_PURPLE_700: "#512da8", DEEP_PURPLE_800: "#4527a0", DEEP_PURPLE_900: "#311b92", DEEP_PURPLE_A100: "#b388ff", DEEP_PURPLE_A200: "#7c4dff", DEEP_PURPLE_A400: "#651fff", DEEP_PURPLE_A700: "#6200ea",
        INDIGO_50: "#e8eaf6", INDIGO_100: "#c5cae9", INDIGO_200: "#9fa8da", INDIGO_300: "#7986cb", INDIGO_400: "#5c6bc0", INDIGO_500: "#3f51b5", INDIGO_600: "#3949ab", INDIGO_700: "#303f9f", INDIGO_800: "#283593", INDIGO_900: "#1a237e", INDIGO_A100: "#8c9eff", INDIGO_A200: "#536dfe", INDIGO_A400: "#3d5afe", INDIGO_A700: "#304ffe",
        BLUE_50: "#e3f2fd", BLUE_100: "#bbdefb", BLUE_200: "#90caf9", BLUE_300: "#64b5f6", BLUE_400: "#42a5f5", BLUE_500: "#2196f3", BLUE_600: "#1e88e5", BLUE_700: "#1976d2", BLUE_800: "#1565c0", BLUE_900: "#0d47a1", BLUE_A100: "#82b1ff", BLUE_A200: "#448aff", BLUE_A400: "#2979ff", BLUE_A700: "#2962ff",
        LIGHT_BLUE_50: "#e1f5fe", LIGHT_BLUE_100: "#b3e5fc", LIGHT_BLUE_200: "#81d4fa", LIGHT_BLUE_300: "#4fc3f7", LIGHT_BLUE_400: "#29b6f6", LIGHT_BLUE_500: "#03a9f4", LIGHT_BLUE_600: "#039be5", LIGHT_BLUE_700: "#0288d1", LIGHT_BLUE_800: "#0277bd", LIGHT_BLUE_900: "#01579b", LIGHT_BLUE_A100: "#80d8ff", LIGHT_BLUE_A200: "#40c4ff", LIGHT_BLUE_A400: "#00b0ff", LIGHT_BLUE_A700: "#0091ea",
        CYAN_50: "#e0f7fa", CYAN_100: "#b2ebf2", CYAN_200: "#80deea", CYAN_300: "#4dd0e1", CYAN_400: "#26c6da", CYAN_500: "#00bcd4", CYAN_600: "#00acc1", CYAN_700: "#0097a7", CYAN_800: "#00838f", CYAN_900: "#006064", CYAN_A100: "#84ffff", CYAN_A200: "#18ffff", CYAN_A400: "#00e5ff", CYAN_A700: "#00b8d4",
        TEAL_50: "#e0f2f1", TEAL_100: "#b2dfdb", TEAL_200: "#80cbc4", TEAL_300: "#4db6ac", TEAL_400: "#26a69a", TEAL_500: "#009688", TEAL_600: "#00897b", TEAL_700: "#00796b", TEAL_800: "#00695c", TEAL_900: "#004d40", TEAL_A100: "#a7ffeb", TEAL_A200: "#64ffda", TEAL_A400: "#1de9b6", TEAL_A700: "#00bfa5",
        GREEN_50: "#e8f5e9", GREEN_100: "#c8e6c9", GREEN_200: "#a5d6a7", GREEN_300: "#81c784", GREEN_400: "#66bb6a", GREEN_500: "#4caf50", GREEN_600: "#43a047", GREEN_700: "#388e3c", GREEN_800: "#2e7d32", GREEN_900: "#1b5e20", GREEN_A100: "#b9f6ca", GREEN_A200: "#69f0ae", GREEN_A400: "#00e676", GREEN_A700: "#00c853",
        LIGHT_GREEN_50: "#f1f8e9", LIGHT_GREEN_100: "#dcedc8", LIGHT_GREEN_200: "#c5e1a5", LIGHT_GREEN_300: "#aed581", LIGHT_GREEN_400: "#9ccc65", LIGHT_GREEN_500: "#8bc34a", LIGHT_GREEN_600: "#7cb342", LIGHT_GREEN_700: "#689f38", LIGHT_GREEN_800: "#558b2f", LIGHT_GREEN_900: "#33691e", LIGHT_GREEN_A100: "#ccff90", LIGHT_GREEN_A200: "#b2ff59", LIGHT_GREEN_A400: "#76ff03", LIGHT_GREEN_A700: "#64dd17",
        LIME_50: "#f9fbe7", LIME_100: "#f0f4c3", LIME_200: "#e6ee9c", LIME_300: "#dce775", LIME_400: "#d4e157", LIME_500: "#cddc39", LIME_600: "#c0ca33", LIME_700: "#afb42b", LIME_800: "#9e9d24", LIME_900: "#827717", LIME_A100: "#f4ff81", LIME_A200: "#eeff41", LIME_A400: "#c6ff00", LIME_A700: "#aeea00",
        YELLOW_50: "#fffde7", YELLOW_100: "#fff9c4", YELLOW_200: "#fff59d", YELLOW_300: "#fff176", YELLOW_400: "#ffee58", YELLOW_500: "#ffeb3b", YELLOW_600: "#fdd835", YELLOW_700: "#fbc02d", YELLOW_800: "#f9a825", YELLOW_900: "#f57f17", YELLOW_A100: "#ffff8d", YELLOW_A200: "#ffff00", YELLOW_A400: "#ffea00", YELLOW_A700: "#ffd600",
        AMBER_50: "#fff8e1", AMBER_100: "#ffecb3", AMBER_200: "#ffe082", AMBER_300: "#ffd54f", AMBER_400: "#ffca28", AMBER_500: "#ffc107", AMBER_600: "#ffb300", AMBER_700: "#ffa000", AMBER_800: "#ff8f00", AMBER_900: "#ff6f00", AMBER_A100: "#ffe57f", AMBER_A200: "#ffd740", AMBER_A400: "#ffc400", AMBER_A700: "#ffab00",
        ORANGE_50: "#fff3e0", ORANGE_100: "#ffe0b2", ORANGE_200: "#ffcc80", ORANGE_300: "#ffb74d", ORANGE_400: "#ffa726", ORANGE_500: "#ff9800", ORANGE_600: "#fb8c00", ORANGE_700: "#f57c00", ORANGE_800: "#ef6c00", ORANGE_900: "#e65100", ORANGE_A100: "#ffd180", ORANGE_A200: "#ffab40", ORANGE_A400: "#ff9100", ORANGE_A700: "#ff6d00",
        DEEP_ORANGE_50: "#fbe9e7", DEEP_ORANGE_100: "#ffccbc", DEEP_ORANGE_200: "#ffab91", DEEP_ORANGE_300: "#ff8a65", DEEP_ORANGE_400: "#ff7043", DEEP_ORANGE_500: "#ff5722", DEEP_ORANGE_600: "#f4511e", DEEP_ORANGE_700: "#e64a19", DEEP_ORANGE_800: "#d84315", DEEP_ORANGE_900: "#bf360c", DEEP_ORANGE_A100: "#ff9e80", DEEP_ORANGE_A200: "#ff6e40", DEEP_ORANGE_A400: "#ff3d00", DEEP_ORANGE_A700: "#dd2c00",
        BROWN_50: "#efebe9", BROWN_100: "#d7ccc8", BROWN_200: "#bcaaa4", BROWN_300: "#a1887f", BROWN_400: "#8d6e63", BROWN_500: "#795548", BROWN_600: "#6d4c41", BROWN_700: "#5d4037", BROWN_800: "#4e342e", BROWN_900: "#3e2723",
        GRAY_50: "#fafafa", GRAY_100: "#f5f5f5", GRAY_200: "#eeeeee", GRAY_300: "#e0e0e0", GRAY_400: "#bdbdbd", GRAY_500: "#9e9e9e", GRAY_600: "#757575", GRAY_700: "#616161", GRAY_800: "#424242", GRAY_900: "#212121",
        BLUE_GRAY_50: "#eceff1", BLUE_GRAY_100: "#cfd8dc", BLUE_GRAY_200: "#b0bec5", BLUE_GRAY_300: "#90a4ae", BLUE_GRAY_400: "#78909c", BLUE_GRAY_500: "#607d8b", BLUE_GRAY_600: "#546e7a", BLUE_GRAY_700: "#455a64", BLUE_GRAY_800: "#37474f", BLUE_GRAY_900: "#263238",
        RED: "red", GREEN: "green", BLUE: "blue", ROSYBROWN: "rosybrown"
    }
}


