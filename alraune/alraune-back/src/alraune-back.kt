package alraune.back

import vgrechka.*

external fun phiPrintln(x: String?)
external fun phiEval(code: String): dynamic
external fun phiEvalToNative(code: String): dynamic

fun main(args: Array<String>) {
    val page = phiEval("return isset(\$_GET['page']) ? \$_GET['page'] : 'landing';") as String
    when (page) {
        "order" -> spitOrderPage()
        else -> spitLandingPage()
    }
}


fun t(en: String, ru: String): String {
    // TODO:vgrechka ...
    return ru
}

fun spitOrderPage() {
    spitBasicTemplate(TemplateParams(
        pageTitle = "Alraune",
        body = buildString {
            ln("<div class='container'>")
            ln("<form>")
            ln("    <div class='form-group'>")
            ln("        <label>${t("Sign in via", "Войти через")}</label>")
            ln("        <div>")
            ln("            <button id='googleSignInButton' class='btn btn-default'>${t("Google", "Google")}</button>")
            ln("            <button class='btn btn-default'>${t("Facebook", "Facebook")}</button>")
            ln("        </div>")
            ln("    </div>")
            ln("</form>")
            ln("</div>")
        }
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
    val pageTitle: String,
    val body: String
)

private fun spitBasicTemplate(p: TemplateParams) {
    println(buildString {
        ln("<!DOCTYPE html>")
        ln("<html lang='en'>")
        ln("<head>")
        ln("    <meta charset='utf-8'>")
        ln("    <meta http-equiv='X-UA-Compatible' content='IE=edge'>")
        ln("    <meta name='viewport' content='width=device-width, initial-scale=1'>")
        ln("    <meta name='google-signin-client_id' content='1064147176813-n6l5pddt9qggcp9n4losnknb2dm5hl9t.apps.googleusercontent.com'>")
        ln("    <title>${p.pageTitle}</title>")
        ln("")
        ln("    <link href='node_modules/bootstrap/dist/css/bootstrap.min.css' rel='stylesheet'>")
        ln("</head>")
        ln("<body>")
        ln("    ${p.body}")
        ln("")
        ln("    <script src='node_modules/jquery/dist/jquery.min.js'></script>")
        ln("    <script src='node_modules/bootstrap/dist/js/bootstrap.min.js'></script>")
        ln("    <script src='https://apis.google.com/js/api:client.js'></script>")
        ln("    <script src='out-front/lib/kotlin.js'></script>")
        ln("    <script src='out-front/alraune-front.js'></script>")
        ln("</body>")
        ln("</html>")
    })
}

