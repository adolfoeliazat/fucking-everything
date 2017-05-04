package vgrechka

import com.google.common.collect.MapMaker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.FreakingSuite
import org.junit.runners.Suite
import java.io.File
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@RunWith(Suite::class) @Suite.SuiteClasses(
    FileLineTests::class, AttachedComputedShitTests::class, JFXPropertyTests::class,
    FilePileTests::class)
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

@RunWith(FreakingSuite::class)
class FilePileTests : SuiteMakerClient {
    override fun build(make: SuiteMaker<Unit>) {
        buildShit(make,
                  suitePrefix = "fesmall__",
                  expectedBakFilePath = BigPile.fuckingBackupsRoot + "/fesmall----shit-for-tests--existing-pizda.txt----2015-05-25-10-11-12-345",
                  fileToBackUp = TestPile.existingPizdaFile.small())

        buildShit(make,
                  suitePrefix = "febig__",
                  expectedBakFilePath = BigPile.fuckingBackupsRoot + "/febig----shit-for-tests--existing-pizda.txt----2015-05-25-10-11-12-345",
                  fileToBackUp = TestPile.existingPizdaFile.big())
    }

    class buildShit(make: SuiteMaker<Unit>,
                    val suitePrefix: String,
                    val expectedBakFilePath: String,
                    val fileToBackUp: File) {

        init {
            make.suiteFor({FilePile.backUp().ifExists()}, prefix = suitePrefix) {
                buildCommonCases(it)
                buildDoesntExistCase(it) {exercise ->
                    assertNull(exercise())
                }
            }

            make.suiteFor({FilePile.backUp().orBitchIfDoesntExist()}, prefix = suitePrefix) {
                buildCommonCases(it)
                buildDoesntExistCase(it) {exercise ->
                    AssertPile.thrownContainsUUID("c--testref--5a4b69c-e2d5-4833-b9c0-4eb5b0b06c25") {
                        exercise()
                    }
                }
            }
        }

        fun buildDoesntExistCase(it: SuiteMaker<out FilePile.backUp.Ignite>, block: (() -> String?) -> Unit) {
            it.case("doesntExist") {sut ->
                block {sut.ignite(File(BigPile.fuckingEverythingSmallRoot + "/non-existent-pizda.txt"))}
            }
        }

        fun buildCommonCases(it: SuiteMaker<out FilePile.backUp.Ignite>) {
            it.case("existsButUnderObscureRoot") {sut ->
                AssertPile.thrownContainsUUID("9--testref--911cfc6-6435-4a54-aa74-ad492162181a") {
                    val fileFromObscureRoot = File(FilePile.tmpDirPath + "/435405f1-3a4e-412a-afcf-8234eeb7c15e")
                    if (!fileFromObscureRoot.exists()) {
                        fileFromObscureRoot.writeText("Hello, hoser :)")
                    }
                    sut.ignite(fileFromObscureRoot)
                }
            }

            it.case("cool") {sut ->
                TimePile.withTestLdtnow(LocalDateTime.of(2015, 5, 25, 10, 11, 12, 345000000)) {
                    run {
                        val f = File(expectedBakFilePath)
                        if (f.exists()) {
                            check(f.delete()) {"3c959326-52f7-441b-881a-596e2fb730b1"}
                        }
                    }
                    val actualBakFilePath = sut.ignite(fileToBackUp)
                    assertEquals(expectedBakFilePath, actualBakFilePath)
                    assertEquals(fileToBackUp.readText(), File(actualBakFilePath).readText())
                }
            }
        }
    }

}

















