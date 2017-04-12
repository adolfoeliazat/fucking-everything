package phizdetslib

external fun phiEval(code: String): Any?
external fun phiEvalToNative(code: String): Any?
external fun header(x: String)
external fun error_log(message: String, messageType: Int, destination: String)
external fun phiBreakDebugger()

fun phiEvalStar(code: String): Any? {
    val code2 = code.replace("*", "$")
    return phiEval(code2)
}

//fun <T> phiEval(code: String): T {
//    throw UnsupportedOperationException("pizda 5b4295a4-9059-47af-96fc-736e5921dc09")
//}

class PHPArray(val nativeValue: Any?) {
    private val path = "Phi::getCurrentEnv()->thisValue->getProperty('nativeValue')->value"

    val count: Int get() {
//        phiBreakDebugger()
//        phiEval("phiPrintln(phiShitToString(Phi::getCurrentEnv()->thisValue->getProperty('nativeValue')->value));")
        return phiEval("return count($path);") as Int
    }
}

object PhiTools {
    inline fun currentEnv(): dynamic {
        return phiEvalToNative("return Phi::getCurrentEnv();")
    }

    fun varCode(name: String): String {
        return "Phi::getCurrentEnv()->vars['$name']"
    }

    inline fun evalExpr(code: String): Any? {
        return phiEval("return $code;")
    }

    inline fun evalExprToNative(code: String): Any? {
        return phiEvalToNative("return $code;")
    }

    fun dumpEnvAndDie(env: dynamic) {
        dumpEnv(env)
        println("Marvels of bullshit")
        exit()
    }

    fun dumpEnv(env: dynamic) {
        val vars = evalExprToNative("${varCode("env")}->vars")
        val keys = evalExprToNative("array_keys(${varCode("vars")})")
        val keyCount = evalExpr("count(${varCode("keys")})") as Int
        for (i in 0 until keyCount) {
            val key = evalExpr("${varCode("keys")}[$i]") as String
            val value = evalExprToNative("${varCode("vars")}[${varCode("key")}->value]")
            val valueClass = evalExpr("get_class(${varCode("value")})") as String
            println("key = $key; valueClass = $valueClass")
            if (valueClass == "PhiString") {
                val string = evalExpr("${varCode("value")}->value") as String
                println("string = '$string'")
            }
        }
    }

    fun dumpNativeShitAndDie(shit: dynamic) {
        dumpNativeShit(shit)
        println("Magnificent shit")
        exit()


        val vars = PHPArray(phiEval("return array_keys(Phi::getCurrentEnv()->vars);"))
        log("vars.count = ${vars.count}")
        exit()

        phiEval("Phi::getCurrentEnv()->shit->keys = array_keys(Phi::getCurrentEnv()->vars);")
        val count = phiEval("return count(Phi::getCurrentEnv()->shit->keys);") as Int
        for (i in 0 until count) {
            val key = phiEval("return Phi::getCurrentEnv()->shit->keys[$i];") as String
            log("key = $key")
        }
//        log("varCount = " + count)
//        val shit = phiEval("return var_export(count(Phi::getCurrentEnv()->vars), true);") as String
//        log(shit)
        log("Waaaaat?")
        exit()
    }

    private fun dumpNativeShit(shit: dynamic) {
        val className = evalExpr("get_class(${varCode("shit")})")
        check(className == "PhiNativeValue") {"c6cc3ce5-d499-4490-a2d7-9f9cf1c9b957"}
        val type = evalExpr("gettype(${varCode("shit")}->value)")
        println("type = $type")
    }

    private fun exit() {
        phiEval("exit();")
    }

    fun log(s: String) {
        val logFile = phiEval("return MY_FUCKING_LOG;") as String
        error_log(s + "\n", 3, logFile)
    }
}









