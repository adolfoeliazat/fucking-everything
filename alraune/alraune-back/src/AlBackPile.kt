package alraune.back

import alraune.back.AlRenderPile.escapeHTML
import alraune.back.AlRenderPile.t
import alraune.shared.AlCSS
import alraune.shared.AlSharedPile
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.validator.routines.EmailValidator
import vgrechka.*
import java.util.concurrent.ConcurrentHashMap

object AlBackPile {
    var idToTagCreationStack = ConcurrentHashMap<String, String>()

    fun fuckValidate1(x: String?, minLen: Int, maxLen: Int, boobs: (String) -> String?): ValidationResult {
        val sane = (x ?: "").trim()
        var error = when {
            sane.isBlank() -> t("TOTE", "Поле обязательно")
            sane.length < minLen -> t("TOTE", "Не менее $minLen символов")
            sane.length > maxLen -> t("TOTE", "Не более $maxLen символов")
            else -> null
        }
        if (error == null) {
            error = boobs(sane)
        }
        return ValidationResult(sane, error)
    }

    fun validateEmail(x: String?) = fuckValidate1(x, minLen = 3, maxLen = 50) {
        when {
            EmailValidator.getInstance().isValid(it) -> null
            else -> t("TOTE", "Странная почта какая-то")
        }
    }

    fun validateName(x: String?) = fuckValidate1(x, minLen = 3, maxLen = 50) {null}

    fun validatePhone(x: String?) = fuckValidate1(x, minLen = 3, maxLen = 50) {
        // TODO:vgrechka Revisit
        val minDigits = 6
        var digitCount = 0
        for (c in it.toCharArray()) {
            if (!Regex("(\\d| |-|\\+|\\(|\\))+").matches("$c")) return@fuckValidate1 t("TOTE", "Странный телефон какой-то")
            if (Regex("\\d").matches("$c")) ++digitCount
        }
        when {
            digitCount < minDigits -> t("TOTE", "Не менее $minDigits цифр")
            else -> null
        }
    }

    fun validateDocumentTitle(x: String?) = fuckValidate1(x, minLen = 5, maxLen = 250) {null}

    fun validateDocumentDetails(x: String?) = fuckValidate1(x, minLen = 5, maxLen = 1000) {null}

}

val kdiv = TagCtor("div")
val kspan = TagCtor("span")
val kp = TagCtor("p")
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

class Tag(val tag: String, var attrs: Attrs) : Renderable {
    val tagCreationStackID: String?
    val children = mutableListOf<Renderable>()

    init {
        val ctx = AlRequestContext.the
        tagCreationStackID = when {
            ctx.shitPassedFromBackToFront.debug_domElementStackTraces -> {
                val id = DebugPile.nextPUID().toString()
                val stack = Exception("Capturing Tag creation stack").stackTraceString
                AlBackPile.idToTagCreationStack[id] = stack
                id
            }
            else -> null
        }
    }

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
            val attr = AlSharedPile.attribute
            attrs.dataShit?.let {append(" ${attr.data_shit}='${escapeHTML(it)}'")}
            tagCreationStackID?.let {append(" ${attr.data_tagCreationStackID}='$it'")}

            append(">")
            for (child in children) {
                append(child.render())
            }
            append("</$tag>")
        }
    }

    fun add(x: Renderable?) {
        if (x != null) {
            children += x
        }
    }

    fun add(x: String?) {
        if (x != null) {
            children += Text(x)
        }
    }

    operator fun minus(x: Renderable?) = add(x)
    operator fun minus(x: String?) = add(x)

    fun text(x: String): Tag {
        add(Text(x))
        return this
    }

    fun amend(style: Style) {
        attrs = attrs.copy(style = style)
    }
}

enum class FieldType {
    TEXT, TEXTAREA
}

class TagCtor(val tag: String) {
    operator fun invoke(attrs: Attrs = Attrs(), block: (Tag) -> Unit = {}): Tag {
        val tag = Tag(tag, attrs)
        block(tag)
        return tag
    }

    operator fun invoke(style: Style, block: (Tag) -> Unit = {}): Tag {
        return this(Attrs(style = style), block)
    }

    fun className(className: String, block: (Tag) -> Unit = {}): Tag {
        return this(Attrs(className = className), block)
    }

    fun className(pack: AlCSS.Pack, block: (Tag) -> Unit = {}): Tag {
        return this(Attrs(className = pack.className), block)
    }

    fun id(id: String, block: (Tag) -> Unit): Tag {
        return this(Attrs(id = id), block)
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

data class Attrs(
    val id: String? = null,
    val className: String? = null,
    val style: Style? = null,
    val type: String? = null,
    val value: String? = null,
    val name: String? = null,
    val rows: Int? = null,
    val dataShit: String? = null
)

class ValidationResult(val sanitizedString: String, val error: String?)





















