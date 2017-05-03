package vgrechka

import com.google.common.collect.MapMaker
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import java.util.concurrent.ConcurrentHashMap

@RunWith(Suite::class) @Suite.SuiteClasses(
    FileLineTests::class, AttachedComputedShitTests::class, JFXPropertyTests::class,
    FilePile.Tests::class)
class SharedJVMTestSuite

class FileLineTests {
    @Test
    fun testNoArgDoesntMessShitUp() {
        val shit = FileLine("fuck.txt", 123)
        assertEquals("fuck.txt", shit.file)
        assertEquals(123, shit.line)
        assertEquals("FileLine(file=fuck.txt, line=123)", shit.toString())
    }
}

class MapBehaviorTests {
    @Test fun testConcurrentHashMapDoesntAllowNullValues() {
        testGivenMapDoesntAllowNullValues(ConcurrentHashMap<Any?, Any?>())
    }

    @Test fun testMapMakerDoesntAllowNullValues() {
        testGivenMapDoesntAllowNullValues(MapMaker().makeMap<Any?, Any?>())
    }

    private fun testGivenMapDoesntAllowNullValues(map: MutableMap<Any?, Any?>) {
        AssertPile.thrown(NullPointerException::class) {
            map.put("shit", null)
        }
    }
}

class AttachedComputedShitTests {
    val log = TestLogger()

    data class Shit(val name: String)

    init {
        AttachedComputedShit.Companion.debug.reset()
    }

    @Test fun testIdentityComparison() {
        object {
            var counter = 0
            val Shit.moreShit by AttachedComputedShit<Shit, String> {"${it.name} much shit ${++counter}"}
            init {
                val shit1 = Shit("first")
                assertEquals("first much shit 1", shit1.moreShit)
                assertEquals("Same value for subsequent calls", "first much shit 1", shit1.moreShit)

                val shit2 = Shit("first")
                assertEquals("first much shit 2", shit2.moreShit)
            }
        }
    }

    @Test fun testWeakKeys() {
        object {
            var counter = 0
            val Shit.moreShit by AttachedComputedShit<Shit, String>(weak = true) {"${it.name} much shit ${++counter}"}
            init {
                var pizda: Shit? = Shit("pizda")
                assertEquals("pizda much shit 1", pizda!!.moreShit)

                assertEquals(1, AttachedComputedShit.Companion.debug.totalShitInMaps)

                pizda = null
                AttachedComputedShit.Companion.debug.testGCReclaimsAllShit()
            }
        }
    }
}


















