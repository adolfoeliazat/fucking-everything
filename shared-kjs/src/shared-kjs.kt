package vgrechka.kjs

fun main(args: Array<String>) {
    clog("I am shared-kjs")
}

fun clog(vararg xs: Any?) {
    console.log(*xs)
}

