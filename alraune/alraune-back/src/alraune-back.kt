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
        println("no fucking token...")
    else
        println("userToken = $userToken")

    val page = phiEval("return isset(\$_GET['page']) ? \$_GET['page'] : 'landing';") as String
    when (page) {
        "order" -> spitOrderPage()
        else -> spitLandingPage()
    }
}

fun phucking1() {
    DBPile.init()

    DBPile.execute(alUserRepo.dropTableDDL)
    DBPile.execute(alUserRepo.createTableDDL)

    DBPile.tx {
        val fucko = newAlUser(
            firstName = "Fucko",
            lastName = "Pidoracko",
            email = "fucko@pidoracko.net",
            passwordHash = "bloody-secret",
            profilePhone = "911",
            aboutMe = "I am not pidar, it's just a name",
            adminNotes = "Pidar. Definitely",
            profileRejectionReason = "We don't tolerate any pidars in our community",
            banReason = "Achtung",
            subscribedToAllCategories = true
        )
         val savedFucko = alUserRepo.insert(fucko)
        println("savedFucko = " + savedFucko)

        val fucko2 = newAlUser(
            firstName = "Fucko 2",
            lastName = "Pidoracko",
            email = "fucko2@pidoracko.net",
            passwordHash = "bloody-secret-2",
            profilePhone = "911",
            aboutMe = "I am not pidar, it's just a name",
            adminNotes = "Pidar. Definitely",
            profileRejectionReason = null,
            banReason = "Achtung",
            subscribedToAllCategories = true
        )
        val savedFucko2 = alUserRepo.insert(fucko2)
        println("savedFucko2 = " + savedFucko2)
    }

    try {
        DBPile.tx {
            val fucko3 = newAlUser(
                firstName = "Fucko 3",
                lastName = "Pidoracko",
                email = "fucko3@pidoracko.net",
                passwordHash = "bloody-secret-3",
                profilePhone = "911",
                aboutMe = "I am not pidar, it's just a name",
                adminNotes = "Pidar. Definitely",
                profileRejectionReason = "We don't tolerate any pidars in our community",
                banReason = "Achtung",
                subscribedToAllCategories = true
            )
            val savedFucko3 = alUserRepo.insert(fucko3)
            println("savedFucko3 = " + savedFucko3)
            throw Exception("pizdets")
        }
    } catch (e: Throwable) {
        println("Caught exception: " + e.message)
    }

    DBPile.tx {
        val fucko4 = newAlUser(
            firstName = "Fucko 4",
            lastName = "Pidoracko",
            email = "fucko4@pidoracko.net",
            passwordHash = "bloody-secret-4",
            profilePhone = "911",
            aboutMe = "I am not pidar, it's just a name",
            adminNotes = "Pidar. Definitely",
            profileRejectionReason = "We don't tolerate any pidars in our community",
            banReason = "Achtung",
            subscribedToAllCategories = true
        )
        val savedFucko4 = alUserRepo.insert(fucko4)
        println("savedFucko4 = " + savedFucko4)
    }

    println("\n----- Select all -----\n")
    run {
        val rows = DBPile.query("select alUser_firstName, alUser_lastName from alraune_users;")
        for ((index, row) in rows.withIndex()) {
            println("${index + 1}) ${row[0]}    ${row[1]}")
        }
    }

    println("\n----- With specific secret -----\n")
    run {
        val items = alUserRepo.select(AlUser::passwordHash, DBPile.op.eq, "bloody-secret-2")
        for ((index, item) in items.withIndex()) {
            println("${index + 1})")
            println("    id = ${item.id}")
            println("    createdAt = ${item.createdAt.toString()}")
            println("    updatedAt = ${item.updatedAt.toString()}")
            println("    deleted = ${item.deleted}")
            println("    firstName = ${item.firstName}")
            println("    lastName = ${item.lastName}")
            println("    email = ${item.email}")
            println("    passwordHash = ${item.passwordHash}")
            println("    profilePhone = ${item.profilePhone}")
            println("    adminNotes = ${item.adminNotes}")
            println("    aboutMe = ${item.aboutMe}")
            println("    profileRejectionReason = ${item.profileRejectionReason}")
            println("    banReason = ${item.banReason}")
            println("    subscribedToAllCategories = ${item.subscribedToAllCategories}")
        }
    }

    println("\nOK")
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
        ln("    <script src='symlinks/out/shared-x/shared-x.js$scriptSuffix'></script>")
        ln("    <script src='symlinks/out/shared-kjs/shared-kjs.js$scriptSuffix'></script>")
        ln("    <script src='symlinks/out/alraune-shared/alraune-shared.js$scriptSuffix'></script>")
        ln("    <script src='out-front/alraune-front.js$scriptSuffix'></script>")
        ln("</body>")
        ln("</html>")
    })
}

