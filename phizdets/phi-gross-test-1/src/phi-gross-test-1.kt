package phi.gross.test.one

external fun phiPrint(x: String)

class Shit(val name: String, val text: String)

fun main(args: Array<String>) {
    sayShit(Shit("Archibald", "Fuck you"))
}

private fun sayShit(shit: Shit) {
    phiPrint("<b>${shit.text}, ${shit.name}</b>")
}














