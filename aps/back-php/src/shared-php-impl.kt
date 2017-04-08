package aps

import aps.back.*

val sharedPlatform = object : XSharedPlatform {
    override fun currentTimeMillis(): Long {
        imf("4edaf379-ad6a-4d11-a4d2-3fffb8372576")
    }

    override fun getenv(name: String): String? {
        imf("001706a0-c7a6-4fbf-b57f-f0069cfff08b")
    }
}

fun var_export(x: Any?, returnString: Boolean = false): String {
    imf("3a451072-7b8d-4cc1-b577-a2e8fc1a670b")
}

fun assertEquals(expected: Any?, actual: Any?, assertionID: String) {
    if (expected != actual) {
        throw AssertionError("assertEquals $assertionID: expected = $expected; actual = $actual")
    }
}

fun assertVarExportEquals(expected: String, actual: Any?, assertionID: String, log: Boolean = false) {
    val shit0 = var_export(actual, returnString = true)
    val shit = shit0.replace("\n  ", " ").replace("\n", "")
    if (log) {
        println("shit = $shit")
    }
    assertEquals(expected, shit, assertionID)
}




























