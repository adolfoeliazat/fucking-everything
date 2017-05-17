package vgrechka.spew

import vgrechka.*

object SpewForDBStuff {
    @JvmStatic
    fun main(args: Array<String>) {
        spewForInputFiles(listOf("%FE%/db-stuff/src/db-stuff.kt"))
        clog("OK")
    }
}

object SpewForAlrauneEntities {
    @JvmStatic
    fun main(args: Array<String>) {
        spewForInputFiles(listOf("%FE%/db-stuff/src/db-stuff.kt"))
        clog("OK")
    }
}

