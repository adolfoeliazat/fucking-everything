namespace alraune {

    export class DocumentCategoryPicker {
        public readonly __isDocumentCategoryPicker = true
        private readonly containerDomid = nextUID()
        private readonly selectID = nextUID()
        private readonly backButtonID = nextUID()
        private readonly pathExceptLast = <AlUADocumentCategory[]>[]

        constructor(private backPile: AlBackToFrontCommandPile) {}

        init() {
            let category = this.findCategoryOrBitch(this.backPile.stringValue)
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

            this.updateDocumentCategoryPicker()

            this.selectJQ().val(last.id)
        }

        private findCategoryOrBitch(id: string): AlUADocumentCategory {
            return maybeFindByID(id, AlUADocumentCategories.root) || bitch("id = $id    c6606a3c-c677-4f8d-8a0b-b1336efbd3fb")

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
            this.updateDocumentCategoryPicker()
        }

        private updateDocumentCategoryPicker() {
            const containerJQ = byIDSingle(this.containerDomid)
            containerJQ.html(run(() => {
                let res = ""
                function ln(x: string) {res += `${x}\n`}

                const items = this.pathExceptLast[this.pathExceptLast.length - 1].children
                ln(`<div style='display: flex; align-items: center;'>`)
                const pathToShow = this.pathExceptLast.slice(1)
                for (const step of pathToShow) {
                    ln(`<div style='margin-right: 0.5rem;'>${step.title}</div>`)
                }
                if (pathToShow.length > 0) {
                    ln(`<button class='btn btn-default' style='margin-right: 0.5rem;' id='${this.backButtonID}'>`)
                    ln(`<i class='fa fa-arrow-left'></i></button>`)
                }
                if (items.length > 0) {
                    ln(`<select class='form-control' id='${this.selectID}'>`)
                    for (const item of items) {
                        ln(`<option value='${item.id}'>${item.title}</option>`)
                    }
                    ln(`</select>`)
                }
                return res
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
                this.updateDocumentCategoryPicker()
            }
        }

        private getSelectedCategoryID(): string {
            return this.selectJQ().val() || wtf("975e6a00-5798-44dd-a704-5e9f47e1e678")
        }

        placeholderHTML(): string {
            return `<div id="${this.containerDomid}"></div>`
        }
    }

    export function isDocumentCategoryPicker(x: any): x is DocumentCategoryPicker {
        return x && x.__isDocumentCategoryPicker
    }

}



