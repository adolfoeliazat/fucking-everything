package alraune.back

import vgrechka.*

object AlBackPile {

}

val kdiv = TagCtor("div")
val kspan = TagCtor("span")
val kbutton = TagCtor("button")
val kform = TagCtor("form")
val kul = TagCtor("ul")
val kol = TagCtor("ol")
val kli = TagCtor("li")
val kinput = TagCtor("input")
val ktextarea = TagCtor("textarea")

class Tag(val tag: String, val attrs: Attrs) : Renderable {
    val children = mutableListOf<Renderable>()

    override fun render(): String {
        return buildString {
            append("<$tag")
            attrs.id?.let {append(" id = '$it'")}
            attrs.className?.let {append(" class = '$it'")}
            attrs.style?.let {append(" style = '${it.render()}'")}
            appendln(">")
            for (child in children) {
                appendln(child.render())
            }
            appendln("</$tag>")
        }
    }

    fun add(re: Renderable?) {
        if (re != null) {
            children += re
        }
    }

    operator fun minus(re: Renderable?) = add(re)
}

class TagCtor(val tag: String) {
    operator fun invoke(attrs: Attrs = Attrs(), block: (Tag) -> Unit): Tag {
        val tag = Tag(tag, attrs)
        block(tag)
        return tag
    }

    operator fun invoke(text: String): Tag {
        return this {it-Text(text)}
    }
}

interface Renderable {
    fun render(): String
}

class Text(val value: String) : Renderable {
    override fun render(): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }
}

class Attrs(
    val id: String? = null,
    val className: String? = null,
    val style: Style? = null
)

