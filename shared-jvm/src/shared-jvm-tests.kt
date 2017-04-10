package vgrechka

import org.junit.Assert.assertEquals
import org.junit.Test

class FileLineTests {
    @Test
    fun testNoArgDoesntMessShitUp() {
        val shit = FileLine("fuck.txt", 123)
        assertEquals("fuck.txt", shit.file)
        assertEquals(123, shit.line)
        assertEquals("FileLine(file=fuck.txt, line=123)", shit.toString())
    }
}


