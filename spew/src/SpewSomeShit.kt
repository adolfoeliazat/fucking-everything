package vgrechka.spew

import vgrechka.*

object SpewSomeShit1 {
    @JvmStatic
    fun main(args: Array<String>) {
        spewForInputFiles(listOf("%FE%/spew-gen-tests/src/GeneratedEntitiesForAmazingWordsTest.kt"))
        clog("OK")
    }
}

object SpewSomeShit2 {
    @JvmStatic
    fun main(args: Array<String>) {
        val res = spewForInputFiles(listOf("%FE%/global-menu/src/botinok-entities.kt"))
        clog(res.ddl)
        clog("OK")
    }
}



