package phi.gross.test.one

class Error(val message: String)

external fun echo(x: String)

class Shit(val name: String, val text: String) {
}

fun main(args: Array<String>) {
    sayShit(Shit("Archibald", "Fuck you"))
}

private fun sayShit(shit: Shit) {
    echo("<b>${shit.text}, ${shit.name}</b>")
}














