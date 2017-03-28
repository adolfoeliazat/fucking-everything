package photlinc

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.kotlin.js.util.TextOutput
import java.util.*
import javax.xml.bind.JAXB
import javax.xml.bind.JAXBContext
import javax.xml.bind.annotation.*
import kotlin.properties.Delegates.notNull
import kotlin.system.exitProcess
import photlin.devtools.*
import vgrechka.*
import java.io.*


private val HTDOCS_OUT = "C:/opt/xampp/htdocs/TryPhotlin/"
private val APS_TMP = "C:/tmp/aps-tmp"
private val PHP_SCRIPT_LOG_DIR = "C:/opt/xampp/htdocs/TryPhotlin/log"
private val PHP_INTERP = "C:\\opt\\xampp\\php\\php.exe"
private val APS_BACK_PHP_ROOT = "E:\\fegh\\aps\\back-php"
private val PHOTLINC_OUT_ROOT = "E:\\fegh\\out\\TryPhotlin"


@Ser @XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = arrayOf("php", "stdout", "stderr", "exitCode"))
class TestResult(val php: String, val stdout: String, val stderr: String, val exitCode: Int) {
    fun printHuman() {
        val xml = toXML()
        clog(xml
                 .replace("<php>", "\n<---------- PHP ---------->\n")
                 .replace("</php>", "</php>")
                 .replace("<stdout>", "\n<---------- STDOUT ---------->\n")
                 .replace("</stdout>", "</stdout>")
                 .replace("<stderr>", "\n<---------- STDERR ---------->\n")
                 .replace("</stderr>", "</stderr>")
                 .replace("    <exitCode>", "\n<exitCode>")
                 .replace("&gt;", ">").replace("&lt;", "<").replace("&amp;", "&"))

        val printWrappedStderr = true
        if (printWrappedStderr && stderr.isNotBlank()) {
            printWrappedText("WRAPPED STDERR", stderr)
        }

    }

    fun toXML(): String {
        val stringWriter = StringWriter()
        JAXB.marshal(this, stringWriter)
        return stringWriter.toString()
    }
}

private fun printWrappedText(title: String, text: String) {
    val maxRowLen = 45
    var wrappedStderr = ""
    var charsInRowSoFar = 0
    for (c in text) {
        if (c in setOf('\r', '\n')) {
            wrappedStderr += c
            charsInRowSoFar = 0
        } else if (charsInRowSoFar + 1 <= maxRowLen) {
            wrappedStderr += c
            ++charsInRowSoFar
        } else {
            wrappedStderr += "$c\n"
            charsInRowSoFar = 1
        }
    }
    printSectionTitle(title)
    clog(wrappedStderr)
}

private fun printSectionTitle(title: String) {
    clog()
    clog("========== $title ==========\n")
}


object TryPhotlin {
    val preventCompilationAndDeploying =
        false
//        true

    @JvmStatic
    fun main(_args: Array<String>) {
        clog("Tits :)")

        val args = when {
            _args.isEmpty() -> arrayOf("requestResponseScenario", "aps-back")
//            _args.isEmpty() -> arrayOf("justRun", "test1")
            else -> _args
        }.toMutableList()

        val cmd = args.removeAt(0)
        val p = TestParams()
        p.testName = args.removeAt(0)
        p.root = APS_BACK_PHP_ROOT
        p.srcRoot = "${p.root}\\src"
        p.outRoot = "$PHOTLINC_OUT_ROOT\\${p.testName}"

        when (cmd) {
            "harden" -> {
                clog("Hardening results of ${p.testName}")
                val actualXML = File("${p.outRoot}/${p.testName}-actual.xml").readText()
                File("${p.srcRoot}/${p.testName}-expected.xml").writeText(actualXML)
            }
            "justRun" -> justRun(p)
            "requestResponseScenario" -> requestResponseScenario(p)
            else -> wtf("cmd = $cmd    1743afc0-066f-4196-869d-cf33e199c5ab")
        }


        clog("OK")
    }

    fun copyFile(from: String, to: String) {
        File(to).writeText(File(from).readText())
    }


    private fun requestResponseScenario(p: TestParams) {
        fuck(p) {
            val om = ObjectMapper()

            class PrepareResult(val text: String, val parsedOK: Boolean)
            fun prepare(json: String): PrepareResult {
                try {
                    val unsortedData = om.readValue(json, MutableMap::class.java) as MutableMap<Any?, Any?>
                    val data = TreeMap(unsortedData)
                    if (data["backendVersion"] != null) {
                        data.put("backendVersion", "--ignored--")
                    }
                    val formatted = om.writerWithDefaultPrettyPrinter().writeValueAsString(data) +
                        "\n" // To make `git diff` happy
                    return PrepareResult(text = formatted, parsedOK = true)
                } catch(e: JsonParseException) {
                    return PrepareResult(text = json, parsedOK = false)
                } catch(e: JsonMappingException) {
                    return PrepareResult(text = json, parsedOK = false)
                }
            }

            val deploymentDir = "$HTDOCS_OUT${p.testName}"
            if (!preventCompilationAndDeploying) {
                copyFile(p.outFilePath, deploymentDir + "/${p.testName}.php")
                copyFile("${p.outFilePath}--tagged", deploymentDir + "/${p.testName}.php--tagged")
                copyFile(p.phpSettingsSourceFilePath, deploymentDir + "/${p.phpSettingsFileName}")
            }

            val jaxbContext = JAXBContext.newInstance(RRLog::class.java)
            val unmarshaller = jaxbContext.createUnmarshaller()
            val apsTmp = APS_TMP
            val rrlog = unmarshaller.unmarshal(File("$apsTmp/rrlog-pizda.xml")) as RRLog
            for (entry in rrlog.entries.take(2)) {
                val procName = entry.pathInfo.substring(entry.pathInfo.lastIndexOf("/") + 1)
                val adaptedRequestJSON = entry.requestJSON.replaceFirst("{", "{\"proc\": \"$procName\", ")
                clog("Request ${entry.id}: ${entry.pathInfo}?${entry.queryString} $adaptedRequestJSON")

                val deploymentLogDir = PHP_SCRIPT_LOG_DIR
                for (file in File(deploymentLogDir).list()) {
                    // clog("Deleting " + file)
                    if (!File("$deploymentLogDir/$file").delete())
                        bitch("Cannot delete freaking $file")
                }

                val actualResponseJSON = HTTPClient.postJSON("http://localhost/TryPhotlin/${p.testName}/${p.testName}.php?${entry.queryString}", adaptedRequestJSON)
                val expectedResponseJSON = entry.responseJSON
                val actualPreparedResponse = prepare(actualResponseJSON)
                val expectedPreparedResponse = prepare(expectedResponseJSON)

                val allLogFile = File("$deploymentLogDir/all.log")
                if (allLogFile.exists()) {
                    val text = allLogFile.readText()
                    if (text.isNotBlank()) {
                        printSectionTitle("ALL.LOG")
                        clog(text)
                    }
                }

                if (actualPreparedResponse.text == expectedPreparedResponse.text) {
                    clog("Fine")
                } else {
                    clog("Shit")
                    val expectedFilePath = "$apsTmp/expected-php-response.json"
                    val actualFilePath = "$apsTmp/actual-php-response.json"
                    File(expectedFilePath).writeText(expectedPreparedResponse.text)
                    File(actualFilePath).writeText(actualPreparedResponse.text)

                    val res = runProcessAndWait(listOf("git", "diff", "--color", actualFilePath, expectedFilePath), inheritIO = true)
                    if (res.exitValue != 0) {
                        clog("Diff shitted at us with code ${res.exitValue}")
                    }

                    if (!actualPreparedResponse.parsedOK) {
                        val responseText = actualPreparedResponse.text
                        printWrappedText("WRAPPED RESPONSE", responseText
                            .replace("&gt;", ">")
                            .replace("&lt;", "<")
                            .replace("&amp;", "&"))

                        val message = PDTRemoteCommand_TestResult(rawResponseFromPHPScript = actualResponseJSON)
                        sendTestResultToDevTools(message)
                    }

                    exitProcess(1)
                }
            }
        }
    }

    private fun sendTestResultToDevTools(req: PDTRemoteCommand_TestResult) {
        print("Sending shit to photlin-dev-tools...")
        val proc = req::class.simpleName!!.substring("PDTRemoteCommand_".length)
        val rawResponse = HTTPClient.postJSON(
            "http://localhost:" + PhotlinDevToolsGlobal.rpcServerPort + "?proc=$proc",
            ObjectMapper().writeValueAsString(req))
        println(" " + rawResponse)
        //clog("Response: $rawResponse")
    }

//    fun postJSON(url: String, content: String): String {
//        return post(url, "application/json", content)
//    }
//
//    fun postXML(url: String, content: String): String {
//        return post(url, "application/xml", content)
//    }

//    private fun post(url: String, mime: String, content: String): String {
//        val JSON = MediaType.parse(mime + "; charset=utf-8")
//        val client = OkHttpClient()
//        val body = RequestBody.create(JSON, content)
//        val request = Request.Builder()
//            .url(url)
//            .post(body)
//            .build()
//        val response = client.newCall(request).execute()
//        return response.body().string()
//    }

    private fun justRun(p: TestParams) {
        fuck(p) {
            val res = execPHP(p)

            val testResult = TestResult(php = File(p.outFilePath).readText(),
                                        stdout = res.stdout,
                                        stderr = res.stderr,
                                        exitCode = res.exitValue)
            File("${p.outRoot}/${p.testName}-actual.xml").writeText(testResult.toXML())
            testResult.printHuman()

            if (res.exitValue != 0) {
                clog("PHP shitted at us with code ${res.exitValue}")
                exitProcess(1)
            }
        }
    }

    private fun execPHP(p: TestParams): RunProcessResult {
        return runProcessAndWait(listOf(PHP_INTERP, p.outFilePath), inheritIO = false)
    }

    private fun fuck(p: TestParams, boobs: TryPhotlin.() -> Unit) {
        p.outFilePath = "${p.outRoot}\\${p.testName}.php" // XXX Generated module name corresponds to file name
        val errMsgCtx = "testName = `${p.testName}`"

//        val srcDir = File(p.srcRoot)

        fun filesFrom(dirPath: String): List<File> {
            val dir = File(dirPath)
            check(dir.isDirectory) {"$errMsgCtx    bea2f16f-20e7-43f7-8535-4be029c415c3"}
            return dir.listFiles {_, name -> name.endsWith(".kt")}.toList()
        }

        val sourceFiles =
            filesFrom(p.srcRoot) +
            filesFrom("$APS_BACK_PHP_ROOT/photlin-stdlib--junction/src") +
            filesFrom("$APS_BACK_PHP_ROOT/shared--junction/src") +
            filesFrom("$APS_BACK_PHP_ROOT/shared-back--junction/src")

        check(sourceFiles.isNotEmpty()) {"$errMsgCtx    946635ac-3ba8-471e-9ff1-12ff84de8361"}
        if (!preventCompilationAndDeploying) {
            K2PHPCompiler.main(
                *sourceFiles.map {it.absolutePath}.toTypedArray(),
                // "-no-stdlib",
                "-output", p.outFilePath)
        }

        p.phpSettingsFileName = "${p.testName}-settings.php"
        p.phpSettingsSourceFilePath = "${p.srcRoot}/${p.phpSettingsFileName}"
        p.phpSettingsSourceFile = File(p.phpSettingsSourceFilePath)
        if (p.phpSettingsSourceFile.exists()) {
            File("${p.outRoot}/${p.testName}-settings.php").writeText(p.phpSettingsSourceFile.readText())
        }
        val outFile = File(p.outFilePath)
        outFile.writeText(outFile.readText() +
                              "\n\$mainArgs = array(); main(\$mainArgs);")
        run(boobs)
    }
}

class TestParams {
    var root by notNull<String>()
    var srcRoot by notNull<String>()
    var outRoot by notNull<String>()
    var testName by notNull<String>()
    var outFilePath by notNull<String>()
    var phpSettingsSourceFile by notNull<File>()
    var phpSettingsFileName by notNull<String>()
    var phpSettingsSourceFilePath by notNull<String>()
}


fun printPHPFilePrelude(p: TextOutput) {
    p.print(object{}::class.java.getResource("prelude.php").readText())
}

@Ser @XmlRootElement(name = "rrlog") @XmlAccessorType(XmlAccessType.FIELD)
class RRLog {
    @XmlElement(name = "entry")
    val entries = Collections.synchronizedList(mutableListOf<RRLogEntry>())
}

@Ser @XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
class RRLogEntry(val id: Long, val pathInfo: String, val queryString: String, val requestJSON: String, val responseJSON: String)





























//class RPCClient @Throws(IOException::class, TimeoutException::class)
//constructor() {
//
//    private val connection: Connection
//    private val channel: Channel
//    private val requestQueueName = PhotlinGlobus.rabbitRPCQueueName
//    private val replyQueueName: String
//
//    init {
//        val factory = ConnectionFactory()
//        factory.host = "localhost"
//
//        connection = factory.newConnection()
//        channel = connection.createChannel()
//
//        replyQueueName = channel.queueDeclare().queue
//    }
//
//    @Throws(IOException::class, InterruptedException::class)
//    fun call(message: String): String? {
//        val corrId = UUID.randomUUID().toString()
//
//        val props = AMQP.BasicProperties.Builder()
//            .correlationId(corrId)
//            .replyTo(replyQueueName)
//            .build()
//
//        channel.basicPublish("", requestQueueName, props, message.toByteArray(charset("UTF-8")))
//
//        val response = ArrayBlockingQueue<String>(1)
//
//        channel.basicConsume(replyQueueName, true, object : DefaultConsumer(channel) {
//            @Throws(IOException::class)
//            override fun handleDelivery(consumerTag: String?, envelope: Envelope?, properties: AMQP.BasicProperties?, body: ByteArray?) {
//                if (properties!!.correlationId == corrId) {
//                    response.offer(body!!.toString(Charsets.UTF_8))
//                }
//            }
//        })
//
//        return response.poll(3, TimeUnit.SECONDS)
//    }
//
//    @Throws(IOException::class)
//    fun close() {
//        connection.close()
//    }
//
//    //...
//}




















