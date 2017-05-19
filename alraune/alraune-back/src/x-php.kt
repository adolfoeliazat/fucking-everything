package vgrechka

import alraune.back.phiEval

typealias XTimestamp = PHPTimestamp

class PHPTimestamp(val time: Int) {
    override fun toString(): String {
        return phiEval("return gmdate('Y-m-d H:i:s', $time);") as String
    }
}

object PHPPile {
    fun getType(x: dynamic): String {
        return phiEval(buildString {
            ln("\$x = Phi::getCurrentEnv();")
            ln("\$x = \$x->getVar('x');")
            ln("\$x = \$x->value;")
            ln("return gettype(\$x);")
        }) as String
    }

    fun time(): Int {
        return phiEval("return time();") as Int
    }

    fun code_getVarsFromEnv(vararg names: String): String {
        return buildString {
            for (name in names) {
                append("\$$name = Phi::getCurrentEnv(); ")
                append("\$$name = \$$name->getVar('$name'); ")
                append("\$$name = \$$name->value; ")
            }
        }
    }

    fun getGetParam(name: String): String? {
        return phiEval(buildString {
            ln(code_getVarsFromEnv("name"))
            ln("return isset(\$_GET[\$name]) ? \$_GET[\$name] : null;")
        }) as String?
    }
}






