package phizdets.compiler

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.kotlin.cli.js.K2PhizdetsCompiler
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.facade.K2JSTranslator
import org.jetbrains.kotlin.js.translate.utils.jsAstUtils.array
import org.jetbrains.kotlin.js.translate.utils.jsAstUtils.index
import photlin.devtools.*
import vgrechka.*
import java.io.File
import java.io.StringWriter
import java.util.*
import javax.xml.bind.JAXB
import javax.xml.bind.JAXBContext
import javax.xml.bind.annotation.*
import kotlin.properties.Delegates.notNull
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.system.exitProcess
import jdk.nashorn.internal.ir.*
import jdk.nashorn.internal.ir.visitor.NodeVisitor
import jdk.nashorn.internal.parser.Parser
import jdk.nashorn.internal.parser.TokenType
import jdk.nashorn.internal.runtime.Context
import jdk.nashorn.internal.runtime.ErrorManager
import jdk.nashorn.internal.runtime.Source
import jdk.nashorn.internal.runtime.options.Options
import org.jetbrains.kotlin.js.sourceMap.PhizdetsSourceGenerationVisitor
import org.jetbrains.kotlin.js.util.TextOutputImpl

val shitToShit = mutableMapOf<Any, Any?>()
var JsNode.declarationDescriptor by AttachedShit<DeclarationDescriptor?>(shitToShit)

object PhizdetscGlobal {
    var testParams by notNull<GrossTestPhizdets.TestParams>()

    fun reportTranslationStage(num: Int, program: JsProgram) {
        val output = TextOutputImpl()
        val taggedGenOutput = TextOutputImpl()
        program.accept(PhizdetsSourceGenerationVisitor(output, taggedGenOutput, /*sourceMapBuilder*/ null))
        File(testParams.outFilePath + "--$num").writeText(output.toString())
    }
}

object GrossTestPhizdets {
    val preventCompilationAndDeploying =
        false
//        true

    val preventRequest =
        false
//        true

    private val HTDOCS_OUT = "C:/opt/xampp/htdocs/GrossTestPhizdets/"
    private val APS_TMP = "C:/tmp/aps-tmp"
    private val PHP_SCRIPT_LOG_DIR = "C:/opt/xampp/htdocs/GrossTestPhizdets/log"
    private val PHP_INTERP = "C:\\opt\\xampp\\php\\php.exe"
    private val APS_BACK_PHP_ROOT = "E:\\fegh\\aps\\back-php"
    private val PHOTLINC_OUT_ROOT = "E:\\fegh\\out\\GrossTestPhizdets"

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

    @Ser @XmlRootElement(name = "rrlog") @XmlAccessorType(XmlAccessType.FIELD)
    class RRLog {
        @XmlElement(name = "entry")
        val entries = Collections.synchronizedList(mutableListOf<RRLogEntry>())
    }

    @Ser @XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
    class RRLogEntry(val id: Long, val pathInfo: String, val queryString: String, val requestJSON: String, val responseJSON: String)

    @JvmStatic
    fun main(_args: Array<String>) {
        clog("Tits :)")

        val args = when {
            _args.isEmpty() -> arrayOf("requestResponseScenario", "phi-gross-test-1")
//            _args.isEmpty() -> arrayOf("justRun", "test1")
            else -> _args
        }.toMutableList()

        val cmd = args.removeAt(0)
        val p = TestParams()
        PhizdetscGlobal.testParams = p
        p.testName = args.removeAt(0)
        // @here
        p.root = "E:/fegh/phizdets/phi-gross-test-1"
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
//                copyFile(p.outFilePath + "--tagged-gen", deploymentDir + "/${p.testName}.php--tagged-gen")
                copyFile(p.outRoot + "/phi-engine.php", deploymentDir + "/phi-engine.php")
                copyFile(p.outRoot + "/phizdets-stdlib.php", deploymentDir + "/phizdets-stdlib.php")

                val shit = File(deploymentDir + "/${p.testName}.php").readText()
                File("E:/fegh/phizdets/phizdetsc/src/phizdets/php/fuck-around.php").writeText("<?php " + shit.substring(shit.indexOf(";")))

                // @here
//                copyFile("${p.outFilePath}--tagged", deploymentDir + "/${p.testName}.php--tagged")
//                copyFile(p.phpSettingsSourceFilePath, deploymentDir + "/${p.phpSettingsFileName}")
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

                if (preventRequest) {
                    clog("Skipping actual request")
                } else {
                    val actualResponseJSON = HTTPClient.postJSON("http://localhost/GrossTestPhizdets/${p.testName}/${p.testName}.php?${entry.queryString}", adaptedRequestJSON)
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
                        // @here
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

    private fun fuck(p: TestParams, boobs: () -> Unit) {
        p.outFilePath = "${p.outRoot}\\${p.testName}.php" // XXX Generated module name corresponds to file name
        val errMsgCtx = "testName = `${p.testName}`"

//        val srcDir = File(p.srcRoot)

        fun filesFrom(dirPath: String): List<File> {
            val dir = File(dirPath)
            check(dir.isDirectory) {"$errMsgCtx    bea2f16f-20e7-43f7-8535-4be029c415c3"}
            return dir.listFiles {_, name -> name.endsWith(".kt")}.toList()
        }

        // @here
        val sourceFiles =
            filesFrom(p.srcRoot) /*+
                filesFrom("$APS_BACK_PHP_ROOT/photlin-stdlib--junction/src") +
                filesFrom("$APS_BACK_PHP_ROOT/shared--junction/src") +
                filesFrom("$APS_BACK_PHP_ROOT/shared-back--junction/src")*/

        check(sourceFiles.isNotEmpty()) {"$errMsgCtx    946635ac-3ba8-471e-9ff1-12ff84de8361"}
        if (!preventCompilationAndDeploying) {
            K2PhizdetsCompiler.main(
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
        outFile.writeText(outFile.readText())
        run(boobs)
    }

}

class AttachedShit<T>(val shitToShit: MutableMap<Any, Any?>) : ReadWriteProperty<Any, T> {
    data class Key(val obj: Any, val prop: String)

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        @Suppress("UNCHECKED_CAST")
        return shitToShit[Key(thisRef, property.name)] as T
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        shitToShit[Key(thisRef, property.name)] = value
    }

}



























