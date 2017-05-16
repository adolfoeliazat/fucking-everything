package aps.back

external fun phiPrintln(x: String?)
external fun phiEval(code: String): dynamic
external fun phiEvalToNative(code: String): dynamic

fun main(args: Array<String>) {
    val page = phiEval("return \$_GET['page'];") as String
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
        pageTitle = "APS",
        body = """
<script src="https://apis.google.com/js/platform.js" async defer></script>
<script>
function onGoogleSignIn(googleUser) {
    var profile = googleUser.getBasicProfile();
    console.log('ID: ' + profile.getId()); // Do not send to your backend! Use an ID token instead.
    console.log('Name: ' + profile.getName());
    console.log('Image URL: ' + profile.getImageUrl());
    console.log('Email: ' + profile.getEmail()); // This is null if the 'email' scope is not present.
}
</script>

<div class="container">
<form>
  <div class="form-group">
    <label>${t("TOTE", "Войти через")}</label>
    <div class="g-signin2" data-onsuccess="onGoogleSignIn"></div>
  </div>
</form>
</div>
"""
    ))
}

fun spitLandingPage() {
    val lorem1 = (""
        + "<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p>"
        + "<p>Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?</p>")
        .repeat(1)
    val lorem2 = (""
        + "<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p>"
        + "<p>Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?</p>")
        .repeat(4)

    spitBasicTemplate(TemplateParams(
        pageTitle = "APS",
        body = """
<div class="container">
    <div class="row">
        $lorem1
    </div>
    <div class="row">
        <div style="width: 12rem; margin: 0 auto 0.5rem auto;">
            <button class="btn btn-primary" style="width: 100%;">${t("TOTE", "Заказать")}</button>
        </div>
    </div>
    <div class="row">
        $lorem2
    </div>
</div>
"""
    ))
}

class TemplateParams(
    val pageTitle: String,
    val body: String
)

private fun spitBasicTemplate(p: TemplateParams) {
    println("""
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="google-signin-client_id" content="1064147176813-n6l5pddt9qggcp9n4losnknb2dm5hl9t.apps.googleusercontent.com">
    <title>${p.pageTitle}</title>

    <link href="node_modules/bootstrap/dist/css/bootstrap.min.css" rel="stylesheet">
  </head>
  <body>
    ${p.body}

    <script src="node_modules/jquery/dist/jquery.min.js"></script>
    <script src="node_modules/bootstrap/dist/js/bootstrap.min.js"></script>
  </body>
</html>
    """)
}

private fun goBananas() {
    printSomeShit("Fuck 5")
    printSomeShit("Shit")
    printSomeShit("Bitch")
}

fun printSomeShit(shit: String) {
    yetAnotherShit()
    println("First " + shit)
    println("Second " + shit)
}

fun yetAnotherShit() {
    println("I am yet another shit")
}




