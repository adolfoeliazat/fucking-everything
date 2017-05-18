package vgrechka

import alraune.back.phiEval

typealias XTimestamp = PHPTimestamp

class PHPTimestamp(val time: Int) {
    override fun toString(): String {
        return phiEval("return gmdate('Y-m-d H:i:s', $time);") as String
    }
}

fun currentTimestampForEntity(): PHPTimestamp {
    return PHPTimestamp(phiEval("return time();") as Int)
}

object PHPTimePile {
}

object PHPPile {
    fun getType(x: dynamic): String {
        return phiEval(buildString {
            ln("${'$'}x = Phi::getCurrentEnv();")
            ln("${'$'}x = ${'$'}x->getVar('x');")
            ln("${'$'}x = ${'$'}x->value;")
            ln("return gettype(${'$'}x);")
        }) as String
    }
}



