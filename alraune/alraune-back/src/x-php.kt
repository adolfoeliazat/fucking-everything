package vgrechka

import alraune.back.phiEval

typealias XTimestamp = PHPTimestamp

class PHPTimestamp(val time: Int)

fun currentTimestampForEntity(): PHPTimestamp {
    return PHPTimestamp(phiEval("return time();") as Int)
}

object PHPTimePile {
}


