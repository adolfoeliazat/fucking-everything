package vgrechka

import org.junit.Assert.assertEquals
import org.junit.Test

class TestLogger {
    val lines = mutableListOf<Line>()

    class Line(val text: String, val uuid: String)

    fun println(value: Any?, uuid: String) {
        clog(value)
        lines += Line(value.toString(), uuid)
    }

    fun shitWritten(): String {
        val longestLine = lines.map{it.text}.maxBy{it.length} ?: return ""
        return stringBuild {
            val width = longestLine.length + 4
            for (line in lines) {
                val spaces = width - line.text.length
                it += line.text + " ".repeat(spaces) + line.uuid + "\n"
            }
        }
    }

    fun assertEquals(expected: String) {
        val shitted = shitWritten()
        val preparedExpected = expected.trim().replace("\r\n", "\n")
        val preparedActual = shitted.trim().replace("\r\n", "\n")
        if (preparedExpected != preparedActual) {
            clog("\n-------------- SHITTED TO LOG ------------------\n")
            clog(shitted)
        }
        assertEquals(preparedExpected, preparedActual)
    }
}

class JFXPropertyTests {
    val log = TestLogger()

    data class Crap(val qualifier: String)
    class Model {
        var boobs by JFXProperty(Crap("strong"))
        var vagina by JFXProperty(Crap("big"))

        override fun toString() = "Model(boobs=${boobs.qualifier}, vagina=${vagina.qualifier})"
    }

    init {
        AttachedComputedShit.Companion.debug.reset()
    }

    @Test fun test1() {
        var model: Model? = Model()
        fun dumpModel(uuid: String) = log.println(model, uuid)

        model!!
        dumpModel("de7fe95f-cd3b-4729-94df-d2ffe3a8479c")

        val observables = JFXPropertyObservableExtractor<Model>().call(model)
        assertEquals(2, observables.size)
        observables.forEach {
            it.addListener {
                log.println("Ouch, they changed $it", "e51dea15-0130-42dd-bd12-273a0f725dd6")
                dumpModel("30b7eb0c-e5a4-44dc-9e30-4a74b3cea08e")
            }
        }

        model.boobs = Crap("floppy")
        model.vagina = Crap("tiny")

        log.println("Total shit in maps: ${AttachedComputedShit.Companion.debug.totalShitInMaps}", "81dde030-81b5-40d8-9683-e8f06ec25462")
        model = null
        AttachedComputedShit.Companion.debug.testGCReclaimsAllShit()
        log.println("Total shit in maps after GC: ${AttachedComputedShit.Companion.debug.totalShitInMaps}", "96f55f72-56b9-4f17-abd9-405500853620")

        log.assertEquals("""
Model(boobs=strong, vagina=big)                                      de7fe95f-cd3b-4729-94df-d2ffe3a8479c
Ouch, they changed ObjectProperty [value: Crap(qualifier=floppy)]    e51dea15-0130-42dd-bd12-273a0f725dd6
Model(boobs=floppy, vagina=big)                                      30b7eb0c-e5a4-44dc-9e30-4a74b3cea08e
Ouch, they changed ObjectProperty [value: Crap(qualifier=tiny)]      e51dea15-0130-42dd-bd12-273a0f725dd6
Model(boobs=floppy, vagina=tiny)                                     30b7eb0c-e5a4-44dc-9e30-4a74b3cea08e
Total shit in maps: 1                                                81dde030-81b5-40d8-9683-e8f06ec25462
Total shit in maps after GC: 0                                       96f55f72-56b9-4f17-abd9-405500853620
        """)
    }

//    @Test fun testWeakness() {
//        assertEquals(0, WeakAttachedComputedShit.shitToShit.size())
//
//        assertEquals(1, WeakAttachedComputedShit.shitToShit.size())
//        fuck!!.bitch = Bitch("another")
//        assertEquals(Bitch("another"), fuck!!.bitch)
//
//        System.gc()
//        assertEquals(1, WeakAttachedComputedShit.shitToShit.size())
//        assertEquals(Bitch("another"), fuck!!.bitch)
//
////        fuck = null
////        System.gc()
////        Thread.sleep(1000)
//        WeakAttachedComputedShit.shitToShit.cleanUp()
//        while (true) {
//            Thread.sleep(1000)
//            clog(WeakAttachedComputedShit.shitToShit.size())
//        }
//        // assertEquals(0, WeakAttachedComputedShit.shitToShit.size)
//    }
}

