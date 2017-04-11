package phizdetslib

external fun phiEval(code: String): Any?
external fun header(x: String)
external fun error_log(message: String, messageType: Int, destination: String)

fun phiEvalStar(code: String): Any? {
    val code2 = code.replace("*", "$")
    return phiEval(code2)
}

//fun <T> phiEval(code: String): T {
//    throw UnsupportedOperationException("pizda 5b4295a4-9059-47af-96fc-736e5921dc09")
//}

