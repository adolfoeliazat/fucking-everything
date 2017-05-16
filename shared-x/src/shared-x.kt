package vgrechka

fun main(args: Array<String>) {
    println("I am shared-x")
}

fun StringBuilder.ln(x: Any?) {
    append(x)
    append("\n")
}

