package alraune.back

object AlBackPile {
    var cookies: BackCookiesShim = PHPBackCookies()
    val config = AlBackConfig(debugLogFile = "c:/tmp/alraune-debug.log")
    val spacing = 0.5
    val spacingRem = "${spacing}rem"

    fun button(title: String? = null,
               level: ButtonLevel = ButtonLevel.DEFAULT,
               icon: IconClass? = null,
               attrs: Attrs = Attrs()): Renderable {
        return kbutton(attrs.copy(className = "btn btn-$level ${attrs.className ?: ""}")){o->
            if (icon != null)
                o- ki(Attrs(className = "$icon"))
            if (title != null) {
                val marginLeft = when {
                    icon != null -> spacingRem
                    else -> ""
                }
                o- kspan(text = title, attrs = Attrs(style = Style(marginLeft = marginLeft)))
            }
        }
    }
}

