package alraune.back

import alraune.back.AlBackPile0.log
import alraune.back.AlRenderPile.t
import alraune.shared.AlDocumentCategories
import alraune.shared.AlDomID
import alraune.shared.AlSharedPile
import alraune.shared.Color
import vgrechka.*
import java.sql.Timestamp
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// TODO:vgrechka Revisit all this shit...

object AlRenderPile {
    fun t(en: String, ru: String) = ru

    fun pageTitle(text: String) = kh3(text)

    fun formatUnixTime(ms: Long, includeTZ: Boolean = true): String =
        when (AlBackPile0.locale) {
            AlLocale.UA -> {
                imf("9a4adaef-d4fb-4918-bb50-9add68b10007")
//                val double: Double = ms.toString().toDoubleOrNull() ?: wtf()
//                val s = moment.tz(double, "UTC").tz("Europe/Kiev").format("L LTS")
//                if (includeTZ) "$s (Киев)"
//                else s
            }
            AlLocale.EN -> imf("formatUnixTime for EN    cce58ed8-c54c-450a-8559-dd10c85a2fd9")
        }

    fun row(marginBottom: String? = "0.5em", className: String? = null, build: (Tag) -> Unit) =
        kdiv(Attrs(className = "row ${className?:""}", style = Style(marginBottom = marginBottom))){o->
            build(o)
        }

    fun col(size: Int, title: String, contentStyle: Style? = null, contentClassName: String? = null, colClassName: String? = null, build: (Tag) -> Unit) =
        kdiv(Attrs(className = "col-md-$size ${colClassName?:""}")){o->
            o- klabel(Style(marginBottom = "0")).text(title)
            o- kdiv(Attrs(className = contentClassName, style = contentStyle ?: Style())){o->
                build(o)
            }
        }

    fun col(size: Int, content: Renderable) =
        kdiv(Attrs(className = "col-md-$size")){o->
            o- content
        }

    fun col(size: Int, title: String, content: Renderable, contentClassName: String? = null, colClassName: String? = null) =
        col(size, title, contentClassName = contentClassName, colClassName = colClassName){o->
            o- content
        }

    fun col(size: Int, title: String, value: String, contentClassName: String? = null, textClassName: String? = null, icon: XIcon? = null) =
        col(size, title, contentClassName = contentClassName){o->
            icon?.let {
                o- icon.render(Style(marginRight = "0.5rem", marginTop = "-2px"))
            }
            o- kspan(Attrs(className = textClassName)).text(value)
        }


    fun createdAtCol(size: Int, value: Timestamp) =
        col(size, t("Created", "Создан")){o->
            val inLondon = ZonedDateTime.ofInstant(Instant.ofEpochMilli(value.time), ZoneId.of("UTC"))
            val inKiev = inLondon.withZoneSameInstant(ZoneId.of("Europe/Kiev"))
            o- DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(inKiev)
        }

    fun updatedAtCol(size: Int, value: Long) =
        col(size, t("Updated", "Изменен")){o->
            o- formatUnixTime(value)
        }

    fun detailsRow(value: String, highlightRanges: List<IntRange>, title: String? = null, contentClassName: String? = null): Renderable {
        return row{o->
            o- col(12, title ?: t("Details", "Детали"), Style(whiteSpace = "pre-wrap"), contentClassName = contentClassName){o->
                o- highlightedShit(value, highlightRanges, style = Style(marginBottom = "0px"))
            }
        }
    }

    fun renderOrderTitle(order: AlUAOrder): Renderable {
        return kdiv{o->
            o- pageTitle(t("Order #", "Заказ №") + order.id)
            o- kh4(order.documentTitle)
        }
    }

    fun renderOrderParams(order: AlUAOrder): Renderable {
        val f = AlFields.order
        return kdiv(Style(position = "relative")){o->
            o- row{o->
                o- createdAtCol(3, order.createdAt)
                o- col(3, f.status.title, order.state.title)
            }

            o- row{o->
                o- col(3, f.contactName.title, order.contactName)
                o- col(3, f.email.title, order.email)
                o- col(3, f.phone.title, order.phone)
            }

            o- row{o->
                o- col(3, f.documentType.title, AlDocumentType.valueOf(order.documentTypeID).title)
                o- col(6, f.documentCategory.title, run {
                    var cat = AlDocumentCategories.findByIDOrBitch(order.documentCategoryID)
                    val steps = mutableListOf<String>()
                    while (cat.id != AlDocumentCategories.root.id) {
                        steps += cat.title
                        cat = cat.parent!!
                    }
                    steps.reverse()
                    steps.joinToString(" " + AlSharedPile.text.rightAngleQuote + " ")
                })
            }

            o- row{o->
                o- col(3, f.numPages.title, order.numPages.toString())
                o- col(3, f.numSources.title, order.numSources.toString())
            }

//            o- detailsRow(order.details, order.detailsHighlightRanges, title = fields.orderDetails.title)
//            o- renderAdminNotesIfNeeded(order)
            o- detailsRow(order.documentDetails, listOf(), f.documentDetails.title)

//            if (isAdmin() && storeEditingParams != null) {
//                o- anotherHeader(
//                    t("TOTE", "Стор"),
//                    renderControlsTo = {o->
//                        o- Button(icon =  fa.pencil, key = buttons.editStoreParams) {
//                            openEditModal(
//                                title = t("TOTE", "Стор"),
//                                formSpec = FormSpec<UAOrderStoreParamsRequest, GenericResponse>(
//                                    procedureName = "UAUpdateOrderStoreParams",
//                                    req = UAOrderStoreParamsRequest().populateCheckingCompleteness{o->
//                                        o.orderID.value = order.id
//                                        o.uaDocumentCategory.setValue(order.documentCategory)
//                                        o.minAllowedPriceOffer.setValue(order.minAllowedPriceOffer)
//                                        o.maxAllowedPriceOffer.setValue(order.maxAllowedPriceOffer)
//                                        o.minAllowedDurationOffer.setValue(order.minAllowedDurationOffer)
//                                        o.maxAllowedDurationOffer.setValue(order.maxAllowedDurationOffer)
//                                    }
//                                ),
//                                onSuccessAfterClosingModal = {
//                                    storeEditingParams.tabitha.reloadPage(p = LoadPageForURLParams(
//                                        scroll = LoadPageForURLParams.Scroll.PRESERVE))
//                                }
//                            )
//                        }
//                    })
//
//                o- row{o->
//                    o- col(6, fields.uaDocumentCategory.title, order.documentCategory.pathTitle)
//                }
//
//                o- renderOrderStoreBoundaries(order)
//            }

//            o- renderBottomPageSpace()

        }
    }

    fun highlightedShit(text: String, ranges: List<IntRange>, backgroundColor: Color? = null, tag: String? = null, style: Style = Style()): Renderable {
        return rawHTML(highlightedHTML(text, ranges, backgroundColor ?: Color.AMBER_200))
    }

    fun rawHTML(x: String) = object : Renderable {
        override fun render() = x
    }

    fun highlightedHTML(text: String, ranges: List<IntRange>, backgroundColor: Color): String {
        fun noise(x: String) {
            if (false) log.debug(x)
        }

        for ((i, range) in ranges.withIndex()) {
            noise("Range $i: [" + text.substring(range) + "]")
        }

        val markedText = buildString {
            for ((i, c) in text.withIndex()) {
                if (ranges.any {it.start == i})
                    append("\\{")
                if (c == '\\')
                    append("\\")
                append(c)
                if (ranges.any {it.endInclusive == i})
                    append("\\}")
            }
        }

//        var html = kp(markedText).render()
//        check(html.startsWith("<p>") && html.endsWith("</p>"))
//        html = html.substring("<p>".length, html.length - "</p>".length)
        val html = Text(markedText).render()

        return buildString {
            var i = 0
            while (i < html.length) {
                if (html[i] == '\\') {
                    check(i + 1 < html.length)
                    append(
                        when (html[i + 1]) {
                            '\\' -> '\\'
                            '{' -> "<span style='background-color: $backgroundColor;'>"
                            '}' -> "</span>"
                            else -> wtf("Bad escape sequence: \\${html[i + 1]}")
                        })
                    ++i
                } else {
                    append(html[i])
                }
                ++i
            }
        }
    }

    fun renderFormBannerArea(hasErrors: Boolean, idSuffix: String = ""): Tag {
        return kdiv.id(AlDomID.formBannerArea + idSuffix) {o ->
            o - kdiv(Attrs(id = AlDomID.serviceFuckedUpBanner + idSuffix, className = AlCSS.errorBanner.className, style = Style(display = "none")))
                .text(t("Service is temporarily fucked up, sorry...", "Сервис временно в жопе, просим прощения..."))

            if (hasErrors)
                o - kdiv.className(AlCSS.errorBanner)
                    .text(t("TOTE", "Кое-что нужно исправить..."))
        }
    }


}


interface XIcon {
    fun render0(style: Style, size: Int): Renderable
}

fun XIcon.render(style: Style = Style(), size: Int = 18) =
    render0(style, size)

class Twemoji(val hex: String) : XIcon {
    override fun render0(style: Style, size: Int): Renderable {
        return object : Renderable {
            override fun render(): String {
                return buildString {
                    append("<img src='https://twemoji.maxcdn.com/2/svg/$hex.svg'")
                    append(" width='$size' height='$size'")
                    append(" style='${style.render()}'")
                    append("></img>")
                }
            }
        }
    }
}

class EmojiOne(val hex: String) : XIcon {
    override fun render0(style: Style, size: Int): Renderable {
        return object : Renderable {
            override fun render(): String {
                return buildString {
                    append("<img src='https://cdnjs.cloudflare.com/ajax/libs/emojione/2.2.7/assets/svg/$hex.svg'")
                    append(" width='$size' height='$size'")
                    append(" style='${style.render()}'")
                    append("></img>")
                }
            }
        }
    }
}


data class AlIntRange(
    val start: Int,
    val endInclusive: Int
)

object AlFields {
    class Fuck(val title: String)

    object order {
        val status = Fuck(t("TOTE", "Статус"))
        val email = Fuck(t("TOTE", "Почта"))
        val contactName = Fuck(t("TOTE", "Имя"))
        val phone = Fuck(t("TOTE", "Телефон"))
        val documentTitle = Fuck(t("TOTE", "Тема работы (задание)"))
        val documentType = Fuck(t("TOTE", "Тип документа"))
        val documentCategory = Fuck(t("TOTE", "Категория"))
        val documentDetails = Fuck(t("TOTE", "Детали"))
        val numPages = Fuck(t("TOTE", "Страниц"))
        val numSources = Fuck(t("TOTE", "Источников"))
    }

    object orderFile {
        val title = Fuck(t("TOTE", "Название"))
        val documentDetails = Fuck(t("TOTE", "Детали"))
    }
}




