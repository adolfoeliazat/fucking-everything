namespace alraune {

    export class StringBuilder {
        buf = ""
        ln(x: string) {this.buf += `${x}\n`}
    }

    export function buildString(f: (x: StringBuilder) => void): string {
        const sb = new StringBuilder()
        f(sb)
        return sb.buf
    }

    export interface Alice10 {
        bob: Bob10
        init(): void
    }

    export class Bob10 {
        readonly containerDomid = nextUID()
        backPile: AlBackToFrontCommandPile

        placeholderHTML() {
            return `<div id="${this.containerDomid}"></div>`
        }

        jContainer(): JQuery {
            return byIDSingle(this.containerDomid)
        }

        setHTML(s: string) {
            this.jContainer().html(s)
        }
    }

    export class ButtonBarWithTicker implements Alice10 {
        /// @augment 64460c12-3bcf-4e37-bdeb-9e5c264df8c2
        readonly bob = new Bob10()

        init() {
            this.update()
        }

        update() {
            this.bob.setHTML(buildString(s => {
                s.ln("pizdarde")
            }))
        }
    }

    export class DocumentCategoryPicker implements Alice10 {
        /// @augment 9086702e-f236-465b-b689-8a57da9d55a6
        readonly bob = new Bob10()
        readonly selectID = nextUID()
        readonly backButtonID = nextUID()
        readonly pathExceptLast = <AlUADocumentCategory[]>[]

        init() {
            let category = this.findCategoryOrBitch(this.bob.backPile.stringValue)
            while (true) {
                this.pathExceptLast.push(category)
                const parent = category.parent
                if (parent == null)
                    break
                else
                    category = parent
            }
            this.pathExceptLast.reverse()
            const last = this.pathExceptLast[this.pathExceptLast.length - 1]
            this.pathExceptLast.pop()

            this.update()

            this.selectJQ().val(last.id)
        }

        private findCategoryOrBitch(id: string): AlUADocumentCategory {
            return maybeFindByID(id, AlUADocumentCategories.root) || bitch("bfe6c71d-2bd0-4c3b-9757-932f662780cf", {id})

            function maybeFindByID(id: string, parent: AlUADocumentCategory): AlUADocumentCategory | undefined {
                for (const child of parent.children) {
                    if (child.id === id)
                        return child
                    else {
                        let x = maybeFindByID(id, child)
                        if (x) return x
                    }
                }
            }
        }

        debug_handleBackButtonClick() {
            this.handleBackButtonClick()
        }

        debug_setSelectValue(categoryID: string) {
            this.selectJQ().val(categoryID)
            this.handleSelectChange()
        }

        private handleBackButtonClick() {
            this.pathExceptLast.pop()
            this.update()
        }

        private update() {
            this.bob.setHTML(buildString(s => {
                const items = this.pathExceptLast[this.pathExceptLast.length - 1].children
                s.ln(`<div style='display: flex; align-items: center;'>`)
                const pathToShow = this.pathExceptLast.slice(1)
                for (const step of pathToShow) {
                    s.ln(`<div style='margin-right: 0.5rem;'>${step.title}</div>`)
                }
                if (pathToShow.length > 0) {
                    s.ln(`<button class='btn btn-default' style='margin-right: 0.5rem;' id='${this.backButtonID}'>`)
                    s.ln(`<i class='fa fa-arrow-left'></i></button>`)
                }
                if (items.length > 0) {
                    s.ln(`<select class='form-control' id='${this.selectID}'>`)
                    for (const item of items) {
                        s.ln(`<option value='${item.id}'>${item.title}</option>`)
                    }
                    s.ln(`</select>`)
                }
            }))

            this.selectJQ().on("change", () => {
                this.handleSelectChange()
            })

            const backButtonJQ = byIDNoneOrSingle(this.backButtonID)
            if (backButtonJQ !== undefined) {
                backButtonJQ.on("click", () => {
                    this.handleBackButtonClick()
                })
            }
        }

        private selectJQ(): JQuery {
            return byIDSingle(this.selectID)
        }

        private handleSelectChange() {
            const categoryID = this.getSelectedCategoryID()
            const item = this.pathExceptLast[this.pathExceptLast.length - 1].children
                    .find(x => x.id == categoryID)
                || wtf("5162f6ed-31bc-4e89-8088-5528b9ea43d5")
            if (item.children.length > 0) {
                this.pathExceptLast.push(item)
                this.update()
            }
        }

        private getSelectedCategoryID(): string {
            return this.selectJQ().val() || wtf("975e6a00-5798-44dd-a704-5e9f47e1e678")
        }
    }

    export function isDocumentCategoryPicker(x: any): x is DocumentCategoryPicker {
        return x && x.__isDocumentCategoryPicker
    }

}



