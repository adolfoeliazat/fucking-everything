package alraune.back

import alraune.back.AlBackPile.button
import alraune.shared.AlSharedPile
import alraune.shared.ShitPassedFromBackToFront
import vgrechka.*
import vgrechka.PHPPile.peval
import vgrechka.PHPPile.peval_bitchIfFalse

external fun phiPrintln(x: String?)
external fun phiEval(code: String): dynamic
external fun phiEvalToNative(code: String): dynamic

fun main(args: Array<String>) {
    val hasArgs = phiEval("global \$argv; return isset(\$argv[1]);") as Boolean
    if (hasArgs) {
        val arg1 = phiEval("global \$argv; return \$argv[1];") as String
        when (arg1) {
            "phucking1" -> phucking1()
            else -> println("What the fuck do you mean by that args?")
        }
        return
    }

//    DBPile.init()

    val fakeSettingName = AlSharedPile.GetParam.fakeSetting.get()
    if (fakeSettingName != null) {
        val fakeSetting = FakeSetting.values().find {it.name == fakeSettingName} ?: wtf("bd7f0b1a-e78d-4f9d-8fd1-aa26c2f4141d")
        fakeSetting.ignite()
    }

    val sessionID = AlSharedPile.Cookie.sessionID.get()
    if (sessionID != null) {
        DebugLog.info("sessionID = $sessionID")
    }

    val page = AlSharedPile.GetParam.page.get() ?: "landing"
    when (page) {
        "order" -> spitOrderPage()
        else -> spitLandingPage()
    }
}

fun AlSharedPile.GetParam.get() = PHPPile.getGetParam(this.name)

fun AlSharedPile.Cookie.get() = AlBackPile.cookies.get(this.name)

class PHPInteropVar(initialValue: Any? = null, initCode: String? = null) {
    val name = "PHPInteropVar${PHPPile.nextPUID()}"
    val code = "\$GLOBALS['$name']"

    init {
        if (initCode == null) {
            PHPPile.setGlobal(name, initialValue)
        } else {
            phiEval("$code = $initCode;")
        }
    }

    override fun toString() = code

    fun get(): Any? {
        return phiEval("return $code;") as Any?
    }

    fun isFalse(): Boolean {
        return phiEval("return $code === false;") as Boolean
    }
}

class FileAppender(val fileName: String) {
    val fileVar by lazy {
        peval_bitchIfFalse("fopen(${PHPInteropVar(fileName)}, 'a')",
                           "Cannot open file: $fileName    60227ad4-7ca7-4b1e-98d8-aa1c0c707e76")
    }

    fun println(s: String) {
        peval_bitchIfFalse("fwrite($fileVar, ${PHPInteropVar(s + "\n")})",
                           "6a974bff-80f6-4633-8338-27b155d550cc")
    }
}

object DebugLog {
    val appender by lazy {
        FileAppender(AlBackPile.config.debugLogFile)-{o->
            val now = peval_bitchIfFalse("gmdate('Y-m-d H:i:s', time())", "77222e1a-64dd-4b76-9518-b160b94fbdb6")
            o.println("\n\n********************** ${now.get()} UTC ************************\n\n")
        }
    }

    fun info(msg: String) {
        appender.println(msg)
    }
}

class AlBackConfig(
    val debugLogFile: String
)

interface BackCookiesShim {
    fun get(name: String): String?
}

class FakeCookies : BackCookiesShim {
    val nameToValue = mutableMapOf<String, String>()

    override fun get(name: String): String? {
        return nameToValue[name]
    }
}

class PHPBackCookies : BackCookiesShim {
    override fun get(name: String): String? {
        val exists = phiEval("return isset(\$_COOKIE['$name']);") as Boolean
        return when {
            exists -> phiEval("return \$_COOKIE['$name'];") as String
            else -> null
        }
    }
}


fun t(en: String, ru: String): String {
    // TODO:vgrechka ...
    return ru
}

interface Renderable {
    fun render(): String
}


data class Attrs(
    val id: String? = null,
    val className: String? = null,
    val style: Style? = null
) {
    fun render(): String {
        return buildString {
            if (id != null)
                append(" id=\"$id\"")
            if (className != null)
                append(" class=\"$className\"")
            if (style != null)
                append(" style=\"${style.render()}\"")
        }
    }
}

class ktext(val content: String) : Renderable {
    override fun render(): String {
        return peval("htmlspecialchars(${PHPInteropVar(content)})").get() as String
    }
}

fun kdiv(attrs: Attrs = Attrs(), text: String? = null, build: (ktag) -> Unit = {}) =
    ktag("div", attrs, text, build)

fun kspan(attrs: Attrs = Attrs(), text: String? = null, build: (ktag) -> Unit = {}) =
    ktag("span", attrs, text, build)

fun kform(attrs: Attrs = Attrs(), text: String? = null, build: (ktag) -> Unit = {}) =
    ktag("form", attrs, text, build)

fun kul(attrs: Attrs = Attrs(), text: String? = null, build: (ktag) -> Unit = {}) =
    ktag("ul", attrs, text, build)

fun kol(attrs: Attrs = Attrs(), text: String? = null, build: (ktag) -> Unit = {}) =
    ktag("ol", attrs, text, build)

fun kli(attrs: Attrs = Attrs(), text: String? = null, build: (ktag) -> Unit = {}) =
    ktag("li", attrs, text, build)

fun ki(attrs: Attrs = Attrs(), text: String? = null, build: (ktag) -> Unit = {}) =
    ktag("i", attrs, text, build)

fun kbutton(attrs: Attrs = Attrs(), text: String? = null, build: (ktag) -> Unit = {}) =
    ktag("button", attrs, text, build)

class ktag(val tag: String, val attrs: Attrs = Attrs(), text: String? = null, build: (ktag) -> Unit = {}) : Renderable {
    val children = mutableListOf<Renderable>()

    init {
        add(text)
        build(this)
    }

    override fun render(): String {
        return buildString {
            append("<$tag ${attrs.render()}>")
            for (child in children) {
                append(child.render())
            }
            append("</$tag>")
        }
    }

    operator fun minus(x: String?) = add(x)

    fun add(x: String?) {
        if (x != null) {
            children += ktext(x)
        }
    }

    operator fun minus(x: Renderable?) = add(x)

    fun add(x: Renderable?) {
        if (x != null) {
            children += x
        }
    }
}

enum class ButtonLevel(val string: String) {
    DEFAULT("default"),
    PRIMARY("primary"),
    SUCCESS("success"),
    INFO("info"),
    WARNING("warning"),
    DANGER("danger");

    override fun toString(): String {
        return string
    }
}


fun spitOrderPage() {
    spitBasicTemplate(TemplateParams(
        pageID = AlSharedPile.pageID.order,
        body = kdiv(Attrs(className = "container")){o->
            fun socialButton(title: String, icon: IconClass) =
                button(title = title, icon = icon, attrs = Attrs(style = Style(width = "20rem")))

            o- socialButton("Google 2", fa.google)
            o- socialButton("Facebook", fa.facebook)
        }.render()

//            body = buildString {
//            val id = AlSharedPile.dom.id
//            ln("<div class='container'>")
//            ln("<form>")
//            ln("    <div class='form-group'>")
//            ln("        <label>${t("Sign in via", "Войти через")}</label>")
//            ln("        <div>")
//            ln("            <button id='${id.googleSignInButton}' class='btn btn-default'>${t("Google", "Google")}</button>")
//            ln("            <button id='${id.facebookSignInButton}' class='btn btn-default'>${t("Facebook", "Facebook")}</button>")
//            ln("        </div>")
//            ln("        <div>")
//            ln("            <div>Fuck</div>")
//            ln("            <div>Shit</div>")
//            ln("            <div>Bitch</div>")
//            ln("        </div>")
//            ln("    </div>")
//            ln("</form>")
//
//            ln("</div>")
//        }
    ))
}

fun spitLandingPage() {
    val lorem1 = buildString {
        (1..1).forEach {
            ln("<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p>")
            ln("<p>Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?</p>")
        }
    }

    val lorem2 = buildString {
        (1..4).forEach {
            ln("<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p>")
            ln("<p>Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?</p>")
        }
    }

    spitBasicTemplate(TemplateParams(
        pageID = AlSharedPile.pageID.landing,
        pageTitle = "Alraune",
        body = buildString {
            ln("<div class='container'>")
            ln("    <div class='row'>")
            ln("        $lorem1")
            ln("    </div>")
            ln("    <div class='row'>")
            ln("        <div style='width: 12rem; margin: 0 auto 0.5rem auto;'>")
            ln("            <button class='btn btn-primary' style='width: 100%;'>${t("TOTE", "Заказать")}</button>")
            ln("        </div>")
            ln("    </div>")
            ln("    <div class='row'>")
            ln("        $lorem2")
            ln("    </div>")
            ln("</div>")
        }
    ))
}

class TemplateParams(
    val pageID: String,
    val pageTitle: String = "Alraune",
    val body: String
)

private fun spitBasicTemplate(p: TemplateParams) {
//    println(ShitPassedFromBackToFront::pageID.name)
    val scriptSuffix = "?" + PHPPile.time()
    println(buildString {
        ln("<!DOCTYPE html>")
        ln("<html lang='en'>")
        ln("<head>")
        ln("    <meta charset='utf-8'>")
        ln("    <meta http-equiv='X-UA-Compatible' content='IE=edge'>")
        ln("    <meta name='viewport' content='width=device-width, initial-scale=1'>")
        ln("    <meta name='google-signin-client_id' content='1064147176813-n6l5pddt9qggcp9n4losnknb2dm5hl9t.apps.googleusercontent.com'>")
        ln("    <title>${p.pageTitle}</title>")
        ln("    <script>")
        ln("        window.${ShitPassedFromBackToFront::class.simpleName} = {")
        ln("            ${ShitPassedFromBackToFront::pageID.name}: '${p.pageID}'")
        ln("        }")
        ln("    </script>")
        ln("")
        ln("    <link href='node_modules/bootstrap/dist/css/bootstrap.min.css' rel='stylesheet'>")
        ln("    <link rel=\"stylesheet\" href=\"node_modules/font-awesome/css/font-awesome.min.css\">")
        ln("</head>")
        ln("<body>")
        ln("    ${p.body}")
        ln("")
        ln("    <script src='node_modules/jquery/dist/jquery.min.js'></script>")
        ln("    <script src='node_modules/bootstrap/dist/js/bootstrap.min.js'></script>")
        ln("    <script src='https://apis.google.com/js/api:client.js'></script>")
        ln("    <script src='out-front/lib/kotlin.js'></script>")
        ln("    <script src='symlinks/out/shared-x/shared-x.js$scriptSuffix'></script>")
        ln("    <script src='symlinks/out/shared-kjs/shared-kjs.js$scriptSuffix'></script>")
        ln("    <script src='symlinks/out/alraune-shared/alraune-shared.js$scriptSuffix'></script>")
        ln("    <script src='out-front/alraune-front.js$scriptSuffix'></script>")
        ln("</body>")
        ln("</html>")
    })
}

class IconClass(val className: String) {
    override fun toString() = className
}





