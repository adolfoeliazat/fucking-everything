package vgrechka

import alraune.back.*

object DBPile {

    fun init() {
        phiEval("""
            global ${'$'}pdo;
            ${'$'}host = '127.0.0.1';
            ${'$'}db   = 'alraune';
            ${'$'}user = 'root';
            ${'$'}pass = '';
            ${'$'}charset = 'utf8';

            ${'$'}dsn = "mysql:host=${'$'}host;dbname=${'$'}db;charset=${'$'}charset";
            ${'$'}opt = array(
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC
            );
            ${'$'}pdo = new PDO(${'$'}dsn, ${'$'}user, ${'$'}pass, ${'$'}opt);
        """)

        // execute("set autocommit = 0")
        execute("set time_zone = '+0:00'")
    }

    fun execute(sql: String, params: List<Any?> = listOf(), uuid: String? = null) {
        phiEval("\$x = Phi::getCurrentEnv(); \$x = \$x->getVar('sql'); \$x = \$x->value; \$GLOBALS['sql'] = \$x;")

        phiEval("\$GLOBALS['params'] = array();")
        for (param in params) {
            phiEval("\$x = Phi::getCurrentEnv(); \$x = \$x->getVar('param'); \$x = \$x->value; array_push(\$GLOBALS['params'], \$x);")
        }

        phiEval(buildString {
            if (uuid != null) {
                ln("")
                ln("//")
                ln("// Query UUID: $uuid")
                ln("//")
            }
            ln("global ${'$'}pdo, ${'$'}sql, ${'$'}params, ${'$'}st;")
            ln("// var_dump(${'$'}sql);")
            ln("// var_dump(${'$'}params);")
            ln("${'$'}st = ${'$'}pdo->prepare(${'$'}sql);")
            ln("${'$'}res = ${'$'}st->execute(${'$'}params);")
            ln("if (!${'$'}res) {")
            ln("    throw new Exception('PDO error ' . ${'$'}st->errorCode() . '    2503eb26-1f1e-4df2-b4e1-cfc677e3cd57');")
            ln("}")
            // ln("${'$'}st->closeCursor();")
        })
    }

    fun query(sql: String, params: List<Any?> = listOf(), uuid: String? = null): List<List<Any?>> {
        execute(sql, params, uuid) // Sets global $st
        val rows = mutableListOf<List<Any?>>()
        phiEval(buildString {
            ln("global ${'$'}st, ${'$'}rows, ${'$'}numCols;")
            ln("${'$'}rows = ${'$'}st->fetchAll(PDO::FETCH_NUM);")
            ln("if (count(${'$'}rows) > 0) {")
            ln("    ${'$'}numCols = count(${'$'}rows[0]);")
            ln("}")
        })
        val numRows = phiEval("return count(${'$'}GLOBALS['rows']);") as Int
        if (numRows > 0) {
            val numCols = phiEval("return ${'$'}GLOBALS['numCols'];") as Int
            for (rowIndex in 0 until numRows) {
                val row = mutableListOf<Any?>()
                for (colIndex in 0 until numCols) {
                    @Suppress("UnsafeCastFromDynamic")
                    row.add(phiEval(buildString {
                        ln("${'$'}env = Phi::getCurrentEnv();")
                        ln("${'$'}rowIndex = ${'$'}env->getVar('rowIndex'); ${'$'}rowIndex = ${'$'}rowIndex->value;")
                        ln("${'$'}colIndex = ${'$'}env->getVar('colIndex'); ${'$'}colIndex = ${'$'}colIndex->value;")
                        ln("return ${'$'}GLOBALS['rows'][${'$'}rowIndex][${'$'}colIndex];")
                    }))
                }
                rows += row
            }
        }
        return rows
    }

    fun tx(block: () -> Unit) {
        execute("begin", uuid = "c1ba9d12-d7e6-4982-8d9d-3da40087ddb9")
        try {
            block()
            execute("commit", uuid = "e686454e-017f-4f0d-ade8-056bba22da2a")
        } catch (e: dynamic) {
            execute("rollback", uuid = "b0bb24e9-81d7-4b6a-8bc0-bcf54dea4a13")
            throw e
        }
    }

}

















