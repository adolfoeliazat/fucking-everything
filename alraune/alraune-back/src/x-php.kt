package vgrechka

import alraune.back.phiEval

typealias XTimestamp = PHPTimestamp

class PHPTimestamp(val time: Int) {
    override fun toString(): String {
        return phiEval("return gmdate('Y-m-d H:i:s', $time);") as String
    }
}

object PHPCode {
    fun bringVarsFromEnv(vararg names: String): String {
        return buildString {
            for (name in names) {
                append("\$$name = Phi::getCurrentEnv(); ")
                append("\$$name = \$$name->getVar('$name'); ")
                append("\$$name = \$$name->value; ")
            }
        }
    }

    fun global(name: String): String {
        return "\$GLOBALS['$name']"
    }
}

object PHPPile {
    private var nextPUID = 1

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

    fun getGetParam(name: String): String? {
        return phiEval(buildString {
            ln(PHPCode.bringVarsFromEnv("name"))
            ln("return isset(\$_GET[\$name]) ? \$_GET[\$name] : null;")
        }) as String?
    }

    fun setGlobal(name: String, value: Any?) {
        phiEval(buildString {
            ln(PHPCode.bringVarsFromEnv("name", "value"))
            ln("\$GLOBALS[\$name] = \$value;")
        })
    }

    fun nextPUID(): String {
        // TODO:vgrechka Make it Long (when properly implemented) or check for overflow
        return "${nextPUID++}"
    }
}














