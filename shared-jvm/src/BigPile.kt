package vgrechka

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
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

    object mime {
        val octetStream = "application/octet-stream"
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

    fun runProcessAndWait(cmdPieces: List<String>,
                          inheritIO: Boolean = true,
                          input: String? = null,
                          workingDirectory: File? = null,
                          noisy: Boolean = false): RunProcessResult {
        if (noisy) {
            clog("Executing: " + cmdPieces.joinToString(" "))
        }
        val pb = ProcessBuilder()
        val cmd = pb.command()
        cmd.addAll(cmdPieces)
        if (inheritIO)
            pb.inheritIO()
        if (workingDirectory != null)
            pb.directory(workingDirectory)
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

    fun win2WSL(windowsPath: String): String {
        var s = windowsPath
        if (s.startsWith("e:/"))
            s = "/mnt/e/" + s.substring(3)
        else
            wtf("fddaec58-f69c-4974-bb83-4af4e35555d1")
        return s
    }

}

@Ser class JSON_SaucerfulOfSecrets(
    val fepg: JSON_fepg,
    val pg_dump: String,
    val dropbox: JSON_dropbox,
    val gdrive: JSON_gdrive
) {
    @Ser class JSON_fepg(
        val prod: DBConnectionParams,
        val test: DBConnectionParams)

    @Ser class JSON_dropbox(
        val vgrechka: JSON_DropboxAppAccessConfig)

    @Ser class JSON_gdrive(
        val pepezdus: JSON_pepezdus
    ) {
        @Ser class JSON_pepezdus(
            val installed: GoogleClientSecrets.Details)
    }
}


@Ser class DBConnectionParams(
    val host: String,
    val port: Int,
    val dbName: String,
    val user: String,
    val password: String?
)

@Ser class JSON_DropboxAppAccessConfig(
    val appName: String,
    val appKey: String,
    val appSecret: String,
    val accessToken: String
)

class ConsoleBullshitter(val label: String) {
    fun say(x: Any? = "") = clog("$label $x")

    fun <T> operation(title: String, block: () -> T): T {
        print("$label $title...")
        try {
            val res = block()
            println("    OK")
            return res
        } catch (e: Throwable) {
            println("    FAILED")
            throw e
        }
    }

    fun section(title: String, block: () -> Unit) {
        say(title)
        say("-".repeat(title.length))
        block()
        say()
    }
}






















