package vgrechka.spew

import vgrechka.*

object SpewSomeShit1 {
    @JvmStatic
    fun main(args: Array<String>) {
        spewForInputFiles("%FE%/spew-gen-tests/src/GeneratedEntitiesForAmazingWordsTest.kt")
        clog("OK")
    }
}



