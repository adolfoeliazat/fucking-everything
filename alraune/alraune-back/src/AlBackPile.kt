package alraune.back

import alraune.back.AlBackPile.escapeHTML

object AlBackPile {
    val rootDir = "E:/fegh/alraune/alraune-back"

    fun t(en: String, ru: String) = ru

    fun pageTitle(text: String) = kh3(text)

    fun escapeHTML(s: String): String {
        return s
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }
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
val klabel = TagCtor("label")
val kh1 = TagCtor("h1")
val kh2 = TagCtor("h2")
val kh3 = TagCtor("h3")
val kh4 = TagCtor("h4")
val kh5 = TagCtor("h5")

class Tag(val tag: String, val attrs: Attrs) : Renderable {
    val children = mutableListOf<Renderable>()

    override fun render(): String {
        return buildString {
            append("<$tag")
            attrs.id?.let {append(" id='$it'")}
            attrs.className?.let {append(" class='$it'")}
            attrs.style?.let {append(" style='${it.render()}'")}
            attrs.type?.let {append(" type='$it'")}
            attrs.name?.let {append(" name='$it'")}
            attrs.value?.let {append(" value='${escapeHTML(it)}'")}
            attrs.rows?.let {append(" rows='$it'")}
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

enum class FieldType {
    TEXT, TEXTAREA
}

class TagCtor(val tag: String) {
    operator fun invoke(attrs: Attrs = Attrs(), block: (Tag) -> Unit): Tag {
        val tag = Tag(tag, attrs)
        block(tag)
        return tag
    }

    fun className(className: String, block: (Tag) -> Unit): Tag {
        return this(Attrs(className = className), block)
    }

    operator fun invoke(text: String): Tag {
        return this {it-Text(text)}
    }

    operator fun invoke(attrs: Attrs, text: String): Tag {
        return this(attrs) {it-Text(text)}
    }
}

interface Renderable {
    fun render(): String
}

class Text(val value: String) : Renderable {
    override fun render(): String {
        return escapeHTML(value)
    }
}

class Attrs(
    val id: String? = null,
    val className: String? = null,
    val style: Style? = null,
    val type: String? = null,
    val value: String? = null,
    val name: String? = null,
    val rows: Int? = null
)






















