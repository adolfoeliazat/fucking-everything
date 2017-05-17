package alraune.back

import alraune.shared.AlSharedPile
import alraune.shared.ShitPassedFromBackToFront
import vgrechka.*

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

    val userToken = AlBackPile.cookies.get("userToken")
    if (userToken == null)
        println("no fucking token")
    else
        println("userToken = $userToken")

    val page = phiEval("return isset(\$_GET['page']) ? \$_GET['page'] : 'landing';") as String
    when (page) {
        "order" -> spitOrderPage()
        else -> spitLandingPage()
    }
}

fun phucking1() {
    phiEval("""
global ${'$'}pdo;
${'$'}host = '127.0.0.1';
${'$'}db   = 'alraune';
${'$'}user = 'root';
${'$'}pass = '';
${'$'}charset = 'utf8';

${'$'}dsn = "mysql:host=${'$'}host;dbname=${'$'}db;charset=${'$'}charset";
${'$'}opt = array(
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES   => false,
);
${'$'}pdo = new PDO(${'$'}dsn, ${'$'}user, ${'$'}pass, ${'$'}opt);
    """)

    val sql = buildString {
        ln("insert into `alraune_users`(")
        ln("    `alUser_common_createdAt`,")
        ln("    `alUser_common_updatedAt`,")
        ln("    `alUser_common_deleted`,")
        ln("    `alUser_firstName`,")
        ln("    `alUser_email`,")
        ln("    `alUser_lastName`,")
        ln("    `alUser_passwordHash`,")
        ln("    `alUser_profilePhone`,")
        ln("    `alUser_adminNotes`,")
        ln("    `alUser_aboutMe`,")
        ln("    `alUser_profileRejectionReason`,")
        ln("    `alUser_banReason`,")
        ln("    `alUser_subscribedToAllCategories`")
        ln(") values (utc_timestamp(), utc_timestamp(), false, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
    }
    phiEval("\$x = Phi::getCurrentEnv(); \$x = \$x->getVar('sql'); \$x = \$x->value; \$GLOBALS['sql'] = \$x;")

    val params = listOf("fuck1", "fuck2", "fuck3", "fuck4", "fuck5", "fuck6", "fuck7", "fuck8", "fuck9", true)
    phiEval("\$GLOBALS['params'] = array();")
    for (param in params) {
        phiEval("\$x = Phi::getCurrentEnv(); \$x = \$x->getVar('param'); \$x = \$x->value; array_push(\$GLOBALS['params'], \$x);")
    }

    phiEval("""
global ${'$'}pdo, ${'$'}sql, ${'$'}params;
// var_dump(${'$'}sql);
// var_dump(${'$'}params);
${'$'}stmt = ${'$'}pdo->prepare(${'$'}sql);
${'$'}stmt->execute(${'$'}params);
    """)

    println("Hello, pizda")
}

object AlBackPile {
    val cookies: BackCookiesShim = PHPBackCookies()
}

interface BackCookiesShim {
    fun get(name: String): String?
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

fun spitOrderPage() {
    spitBasicTemplate(TemplateParams(
        pageID = AlSharedPile.pageID.order,
        pageTitle = "Alraune",
        body = buildString {
            val id = AlSharedPile.dom.id
            ln("<div class='container'>")
            ln("<form>")
            ln("    <div class='form-group'>")
            ln("        <label>${t("Sign in via", "Войти через")}</label>")
            ln("        <div>")
            ln("            <button id='${id.googleSignInButton}' class='btn btn-default'>${t("Google", "Google")}</button>")
            ln("            <button id='${id.facebookSignInButton}' class='btn btn-default'>${t("Facebook", "Facebook")}</button>")
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
    val pageTitle: String,
    val body: String
)

private fun spitBasicTemplate(p: TemplateParams) {
//    println(ShitPassedFromBackToFront::pageID.name)
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
        ln("")
        ln("")
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
        ln("    <script src='symlinks/out/shared-x/shared-x.js'></script>")
        ln("    <script src='symlinks/out/shared-kjs/shared-kjs.js'></script>")
        ln("    <script src='symlinks/out/alraune-shared/alraune-shared.js'></script>")
        ln("    <script src='out-front/alraune-front.js'></script>")
        ln("</body>")
        ln("</html>")
    })
}

