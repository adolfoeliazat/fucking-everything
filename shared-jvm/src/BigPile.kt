package vgrechka

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.*
import kotlin.concurrent.thread

object BigPile {
    val fuckingEverythingSmallRoot = "e:/fegh"
    val fuckingEverythingBigRoot = "e:/febig"
    val fuckingBackupsRoot = "c:/tmp/fucking-backups"

    val shitForTestsSmallRoot = fuckingEverythingSmallRoot + "/shit-for-tests"
    val shitForTestsBigRoot = fuckingEverythingBigRoot + "/shit-for-tests"

    val localSQLiteShebangDBFilePath = fuckingEverythingBigRoot + "/db/shebang.db"
    val localSQLiteShebangDBURL = "jdbc:intosqlite:$localSQLiteShebangDBFilePath"

    val localSQLiteShebangTestDBFilePath = fuckingEverythingBigRoot + "/db/shebang-test.db"
    val localSQLiteShebangTestDBURL = "jdbc:intosqlite:$localSQLiteShebangTestDBFilePath"

    object fepg {
        val prod by lazy {saucerfulOfSecrets.fepg.prod}
        val test by lazy {saucerfulOfSecrets.fepg.test}
    }

    val saucerfulOfSecrets by lazy {
        ObjectMapper().readValue(File(getEnvOrBitch("SAUCERFUL_OF_SECRETS")), JSON_SaucerfulOfSecrets::class.java)!!
    }

    private fun getEnvOrBitch(name: String) =
        System.getenv(name) ?: bitch("I want environment variable $name")

    fun mangleUUID(uuid: String): String {
        return uuid[0] + "-" + uuid.drop(1)
    }

    fun isRunningFromIntelliJ(): Boolean {
        val classPath = System.getProperty("java.class.path")
        return classPath.contains("idea_rt.jar")
    }

//    fun loadDBConnectionParamsFromSaucerful(jsonPath: String): DBConnectionParams {
//        var obj = ObjectMapper().readValue(File(getEnvOrBitch("SAUCERFUL_OF_SECRETS")), Map::class.java)
//        for (step in jsonPath.split(".")) {
//            obj = obj[step] as Map<*, *>
//        }
//        return DBConnectionParams(host = obj["host"] as String,
//                                  port = obj["port"] as Int,
//                                  dbName = obj["dbName"] as String,
//                                  user = obj["user"] as String,
//                                  password = obj["password"] as String?)
//    }

    fun runProcessAndWait(cmdPieces: List<String>, inheritIO: Boolean = true, input: String? = null): RunProcessResult {
        // clog("Executing: " + cmdPieces.joinToString(" "))
        val pb = ProcessBuilder()
        val cmd = pb.command()
        cmd.addAll(cmdPieces)
        if (inheritIO)
            pb.inheritIO()
        val proc = pb.start()

        fun suckAsync(stm: InputStream): StringBuilder {
            val buf = StringBuilder()
            thread {
                val reader = BufferedReader(InputStreamReader(stm, Charsets.UTF_8.name()))
                while (true) {
                    val line = reader.readLine()
                    if (line == null) {
                        break
                    } else {
                        // println(line)
                        buf.appendln(line)
                    }
                }
                // clog("Finished sucker thread")
            }
            return buf
        }

        val stdout = suckAsync(proc.inputStream)
        val stderr = suckAsync(proc.errorStream)

        if (input != null) {
            thread {
                val pw = PrintWriter(proc.outputStream, true)
                for (line in input.lines()) {
                    pw.println(line)
                }
                pw.close()
                // clog("Finished feeder thread")
            }
        }

        val exitValue = proc.waitFor()
        return RunProcessResult(exitValue = exitValue, stdout = stdout.toString(), stderr = stderr.toString())
    }
}

@Ser class DBConnectionParams(
    val host: String,
    val port: Int,
    val dbName: String,
    val user: String,
    val password: String?
)

@Ser class JSON_SaucerfulOfSecrets(
    val fepg: JSON_fepg,
    val pg_dump: String
) {
    @Ser class JSON_fepg(
        val prod: DBConnectionParams,
        val test: DBConnectionParams
    )
}



















