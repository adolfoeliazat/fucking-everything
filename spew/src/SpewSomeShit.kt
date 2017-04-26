package vgrechka.spew

import vgrechka.*

object SpewSomeShit {
    @JvmStatic
    fun main(args: Array<String>) {
        spewAllDBEntities()
        clog("OK")
    }
}



