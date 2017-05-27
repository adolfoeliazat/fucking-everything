package alraune.front

import alraune.shared.*
import vgrechka.*
import vgrechka.kjs.JQueryPile.byIDSingle

// TODO:vgrechka Think about garbage collection

class FilePicker {
    val t = AlXPile::t
    val addButtonID = "83cae995-420f-497e-b880-73feb97da127"
    val fileItemClass = "c54b26083505347faa437cdfac0f424ba"
    val fileItemTitleClass = "c5e9b440a927d4b56acc1c104802e80a8"
    val fileItemSizeClass = "c02c33b6e08de447baca4dd9cae3512eb"

    val files = mutableListOf<FileItem>()

    class FileItem(val name: String, val sizeBytes: Int)

    init {
        if (true) {
            files += FileItem("monster boobs.rtf", 3241)
            files += FileItem("considerable tits.rtf", 8204023)
            files += FileItem("little vagina.rtf", 55930)
            files += FileItem("ltrace.conf", 14867)
            files += FileItem("magic.mime", 423797)
            files += FileItem("mailcap.order", 48388)
            files += FileItem("manpath.config", 88344)
            files += FileItem("mke2fs.conf", 28342983)
            files += FileItem("overlayroot.conf", 1288530)
            files += FileItem("postgresql-common", 334)
            files += FileItem("shadow", 129992)
            files += FileItem("update-manager", 4322)
        }
        update()
    }

    fun update() {
        val containerJQ = byIDSingle(AlDomID.filePickerContainer, "b15e1df6-fbb7-4f2d-b559-a8c40b426191")
        containerJQ.html(buildString {
            ln("""
                <style>
                    .$fileItemClass {
                        display: inline-block;
                        border-left: 1px solid #bdbdbd;
                        padding: 0.5rem;
                    }
                </style>
            """)
            ln("<div style='margin-bottom: 1.5rem;'>")
            for (file in files) {
                ln("<div class='$fileItemClass'>")
                ln("    <div class='$fileItemTitleClass'>${AlSharedPile.escapeHTML(file.name)}</div>")
                ln("    <div class='$fileItemSizeClass'>${formatFileSizeApprox(file.sizeBytes)}</div>")
                ln("</div>")
            }
            ln("<button id='$addButtonID' class='btn btn-default'><i class='fa fa-plus'></i></button>&nbsp;&nbsp;${t("TOTE", "Добавить файл")}")
            ln("</div>")
        })

        val addButtonJQ = byIDSingle(addButtonID, "f1762bf8-6f4f-4f95-b639-a3a23da1e10f")
        addButtonJQ.on("click") {
            handleAddButtonClick()
        }
    }

    private fun handleAddButtonClick() {
    }

    fun formatFileSizeApprox(totalBytes: Int): String {
        val locale = AlLocale.UA

        val kb = 1024
        val mb = 1024 * kb
        val gb = 1024 * mb

        if (totalBytes >= gb) bitch("You fucking crazy, I'm not dealing with gigabyte files")

        val point = when (locale) {
            AlLocale.EN -> "."
            AlLocale.UA -> ","
        }

        val megs = totalBytes / mb
        val kils = (totalBytes - megs * mb) / kb
        val bytes = totalBytes - megs * mb * kils * kb

        if (megs > 0) return "" +
            megs +
            (if (kils >= 100) "$point${kils / 100}" else "") +
            when (locale) {
                AlLocale.EN -> " MB"
                AlLocale.UA -> " МБ"
            }

        if (kils > 0) return "" +
            kils +
            when (locale) {
                AlLocale.EN -> " KB"
                AlLocale.UA -> " КБ"
            }

        return "" +
            bytes +
            when (locale) {
                AlLocale.EN -> " B"
                AlLocale.UA -> " Б"
            }
    }
}

enum class AlLocale {
    UA, EN
}











