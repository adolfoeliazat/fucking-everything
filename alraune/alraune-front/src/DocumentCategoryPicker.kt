package alraune.front

import alraune.shared.AlDocumentCategories
import alraune.shared.AlDomID
import vgrechka.*
import vgrechka.kjs.JQueryPile.byIDNoneOrSingle
import vgrechka.kjs.JQueryPile.byIDSingle

// TODO:vgrechka Think about garbage collection

class DocumentCategoryPicker {
    val selectID = "c03af235-f18e-48ad-9a68-4ae0e523eff9"
    val backButtonID = "92c15f06-fd82-4d0a-9445-fd3eb59e8aab"
    val pathExceptLast = mutableListOf<AlDocumentCategories.Category>()

    init {
        var category = AlDocumentCategories.findByIDOrBitch(AlFrontPile.shitFromBack.documentCategoryID)
        while (true) {
            pathExceptLast += category
            val parent = category.parent
            if (parent == null)
                break
            else
                category = parent
        }
        pathExceptLast.reverse()
        val last = pathExceptLast.last()
        pathExceptLast.removeAt(pathExceptLast.lastIndex)

        update()

        selectJQ().setVal(last.id)
    }

    fun update() {
        val containerJQ = byIDSingle(AlDomID.documentCategoryPickerContainer)
        containerJQ.html(buildString {
            val items = pathExceptLast.last().children
            ln("<div style='display: flex; align-items: center;'>")
            val pathToShow = pathExceptLast.drop(1)
            for (step in pathToShow) {
                ln("<div style='margin-right: 0.5rem;'>${step.title}</div>")
            }
            if (pathToShow.isNotEmpty()) {
                ln("<button class='btn btn-default' style='margin-right: 0.5rem;' id='$backButtonID'>")
                ln("<i class='fa fa-arrow-left'></i></button>")
            }
            if (items.isNotEmpty()) {
                ln("<select class='form-control' id='$selectID'>")
                for (item in items) {
                    ln("<option value='${item.id}'>${item.title}</option>")
                }
                ln("</select>")
            }
        })

        selectJQ().on("change") {
            handleSelectChange()
        }

        val backButtonJQ = byIDNoneOrSingle(backButtonID)
        backButtonJQ?.let {
            it.on("click") {
                handleBackButtonClick()
            }
        }
    }

    private fun handleBackButtonClick() {
        pathExceptLast.removeAt(pathExceptLast.lastIndex)
        update()
    }

    fun debug_handleBackButtonClick() {
        handleBackButtonClick()
    }

    private fun handleSelectChange() {
        val categoryID = getSelectedCategoryID()
        val item = pathExceptLast.last().children.find {it.id == categoryID} ?: wtf("5162f6ed-31bc-4e89-8088-5528b9ea43d5")
        if (item.children.isNotEmpty()) {
            pathExceptLast += item
            update()
        }
    }

    fun getSelectedCategoryID(): String {
        val categoryID = selectJQ().getVal() ?: wtf("975e6a00-5798-44dd-a704-5e9f47e1e678")
        return categoryID
    }

    fun debug_setSelectValue(categoryID: String) {
        selectJQ().setVal(categoryID)
        handleSelectChange()
    }

    private fun selectJQ() = byIDSingle(selectID)
}













