/// <reference path="generated--by-backend.ts"/>

namespace Pile {
    export const state = {
        backShit: {} as BackShit,
    }

    export const httpGetParam = {
        maf: "maf"
    }

    export const attribute = {
        data_shit: "data-shit"
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
}

enum AlPageID {
    landing,
    orderCreationForm,
    orderParams,
    orderFiles,
}

interface BackShit {
    commands: AlBackToFrontCommand.Type[]
}












