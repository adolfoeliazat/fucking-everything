package phizdets

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.debugging.sourcemap.SourceMapConsumerFactory
import com.google.debugging.sourcemap.SourceMapping
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
import phizdets.compiler.CompileStdlib
import phizdets.compiler.PhizdetscGlobal


object CurrentTestFiddling {
    val preventCompilationAndDeploying =
        false
//        true

    val preventRequest =
        false
//        true

}

object PhizdetsTestingGlobal {

}

object JerkAPSBackPHP {
    @JvmStatic
    fun main(args: Array<String>) {
        Boobs(TestParams(
            testName = "aps-back",
            sourceFiles = listOf(
                File("E:/fegh/aps/back-php/src/aps-back-php.kt"),
                File("E:/fegh/aps/back-php/src/shared-php-impl.kt"),
                File("E:/fegh/aps/back-php/src/shared-back-php-impl.kt"),
                File("E:/fegh/aps/back-php/phizdetslib--junction/src/phizdetslib.kt"),
                File("E:/fegh/aps/back-php/shared--junction/src/xplatf-shared-1.kt"),
                File("E:/fegh/aps/back-php/shared-back--junction/src/xentities.kt"),
                File("E:/fegh/aps/back-php/shared-back--junction/src/xplatf-back-1.kt"),
                File("E:/fegh/aps/back-php/shared-back--junction/src/xplatf-back-2.kt"),
                File("E:/fegh/aps/back-php/shared-back--junction/src/xplatf-back-shared-impl-1.kt")
            )
        )).goBananas()
    }
}


class TestParams(
    val testName: String,
    val sourceFiles: List<File>
)

class Boobs(val testParams: TestParams) {
    private val HTDOCS_OUT = "C:/opt/xampp/htdocs/phi-tests/"
    private val APS_TMP = "C:/tmp/aps-tmp"
    private val PHP_SCRIPT_LOG_DIR = "C:/opt/xampp/htdocs/phi-tests/log"
    private val PHOTLINC_OUT_ROOT = "E:\\fegh\\out\\phi-tests"

    var outFilePath by notNull<String>()
    val outRoot = "$PHOTLINC_OUT_ROOT\\${testParams.testName}"

    fun goBananas() {
        PhizdetscGlobal.reportTranslationStage = {num: Int, program: JsProgram ->
            val output = TextOutputImpl()
            val taggedGenOutput = TextOutputImpl()
            program.accept(PhizdetsSourceGenerationVisitor(output, taggedGenOutput, /*sourceMapBuilder*/ null))
            val file = File(outFilePath + "--$num")
            file.parentFile.mkdirs()
            file.writeText(output.toString())
        }

        compileAndStuff(testParams) {
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

            val deploymentDir = "$HTDOCS_OUT${testParams.testName}"
            if (!CurrentTestFiddling.preventCompilationAndDeploying) {
                copyFile(outFilePath, deploymentDir + "/${testParams.testName}.php")
//                copyFile(p.outFilePath + "--tagged-gen", deploymentDir + "/${p.testName}.php--tagged-gen")
                copyFile(outRoot + "/phi-engine.php", deploymentDir + "/phi-engine.php")
                copyFile(CompileStdlib.stdlibPHPFilePath, outRoot + "/phizdets-stdlib.php")
                copyFile(outRoot + "/phizdets-stdlib.php", deploymentDir + "/phizdets-stdlib.php")

                val shit = File(deploymentDir + "/${testParams.testName}.php").readText()
                File("E:/fegh/phizdets/phizdetsc/src/phizdets/php/fuck-around--${testParams.testName}.php").writeText("<?php " + shit.substring(shit.indexOf(";")))

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
                val deploymentLogDirFile = File(deploymentLogDir)
                deploymentLogDirFile.mkdirs()
                for (file in deploymentLogDirFile.list()) {
                    // clog("Deleting " + file)
                    if (!File("$deploymentLogDir/$file").delete())
                        bitch("Cannot delete freaking $file")
                }

                if (CurrentTestFiddling.preventRequest) {
                    clog("Skipping actual request")
                } else {
                    val actualResponseJSON = HTTPClient.postJSON("http://localhost/phi-tests/${testParams.testName}/${testParams.testName}.php?${entry.queryString}", adaptedRequestJSON)
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

                        run { // Map stack trace
                            val s = actualPreparedResponse.text
                            var readingStackLines = false
                            val stackLines = mutableListOf<MapPhizdetsStack.StackLine>()
                            for (line in s.lines()) {
                                if (readingStackLines) {
                                    val mr = Regex("(#\\d+) (.*)").matchEntire(line)
                                    if (mr != null) {
                                        val itemNumber = mr.groupValues[1]
                                        val rest = mr.groupValues[2]
                                        val mr2 = Regex("(.*?\\.php)\\((\\d+)\\): (.*)").matchEntire(rest)
                                        if (mr2 != null) {
                                            val path = mr2.groupValues[1]
                                            val line = mr2.groupValues[2].toInt()
                                            val description = mr2.groupValues[3]
                                            stackLines += MapPhizdetsStack.StackLine(
                                                prefix = "", resource = path, line = line,

                                                // XXX See SourceMapConsumerV3, condition `((SourceMapConsumerV3.Entry)entries.get(0)).getGeneratedColumn() > column`.
                                                //     We don't want that condition to be true, so that it doesn't look for another mapping
                                                column = 99999
                                            )
                                        }
                                    } else {
                                        break
                                    }
                                } else {
                                    if (line == "Stack trace:") {
                                        readingStackLines = true
                                    }
                                }
                            }

                            if (stackLines.isNotEmpty()) {
                                clog("\n------------------- FUCKING STACK -------------------\n")
                                for (line in stackLines) {
                                    val fuck = MapPhizdetsStack.mapFuckingLine(
                                        line,
                                        getMapPath = {resource ->
                                            when (resource) {
                                                "C:\\opt\\xampp\\htdocs\\phi-tests\\aps-back\\aps-back.php" -> MapPhizdetsStack.MapPath.Just("E:/fegh/out/phi-tests/aps-back/aps-back.php.map")
                                                else -> MapPhizdetsStack.MapPath.Skip()
                                            }
                                        }
                                    )
                                    clog(fuck ?: "[Some obscure shit]")
                                }
                                clog()
                            }
                        }

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

    private fun compileAndStuff(testParams: TestParams, doStuffAfterCompilation: () -> Unit) {
        outFilePath = "$outRoot\\${testParams.testName}.php" // XXX Generated module name corresponds to file name
        val errMsgCtx = "testName = `${testParams.testName}`"

//        val srcDir = File(p.srcRoot)

        // @here
        val sourceFiles =
            testParams.sourceFiles /*+
                filesFrom("$APS_BACK_PHP_ROOT/photlin-stdlib--junction/src") +
                filesFrom("$APS_BACK_PHP_ROOT/shared--junction/src") +
                filesFrom("$APS_BACK_PHP_ROOT/shared-back--junction/src")*/

        check(sourceFiles.isNotEmpty()) {"$errMsgCtx    946635ac-3ba8-471e-9ff1-12ff84de8361"}
        if (!CurrentTestFiddling.preventCompilationAndDeploying) {
            print("Compiling shit...")
            K2PhizdetsCompiler.main(
                *sourceFiles.map {it.absolutePath}.toTypedArray(),
                "-source-map",
                // "-no-stdlib",
                "-output", outFilePath)
            println(" OK, strange")
        }

        val outFile = File(outFilePath)
        outFile.writeText(outFile.readText())
        run(doStuffAfterCompilation)
    }
}

object GrossTestPhizdets {
    private val PHP_INTERP = "C:\\opt\\xampp\\php\\php.exe"
    private val APS_BACK_PHP_ROOT = "E:\\fegh\\aps\\back-php"

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

    @JvmStatic
    fun main(_args: Array<String>) {
        clog("Tits :)")
        PhizdetscGlobal.debugTagPrefix = ""

        val args = when {
            _args.isEmpty() -> arrayOf("requestResponseScenario", "phi-gross-test-1")
//            _args.isEmpty() -> arrayOf("justRun", "test1")
            else -> _args
        }.toMutableList()

        val cmd = args.removeAt(0)
        val testName = args.removeAt(0)
        val root = "E:/fegh/phizdets/phi-gross-test-1"
        val srcRoot = "$root\\src"
        val p = TestParams(
            testName = testName,
            sourceFiles = filesFrom(srcRoot)
        )

        when (cmd) {
//            "harden" -> {
//                clog("Hardening results of ${p.testName}")
//                val actualXML = File("${p.outRoot}/${p.testName}-actual.xml").readText()
//                File("${p.srcRoot}/${p.testName}-expected.xml").writeText(actualXML)
//            }
//            "justRun" -> justRun(p)
            "requestResponseScenario" -> Boobs(p).goBananas()
            else -> wtf("cmd = $cmd    1743afc0-066f-4196-869d-cf33e199c5ab")
        }


        clog("OK")
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

//    private fun justRun(p: TestParams) {
//        compileAndStuff(p) {
//            val res = execPHP(p)
//
//            val testResult = TestResult(php = File(p.outFilePath).readText(),
//                                        stdout = res.stdout,
//                                        stderr = res.stderr,
//                                        exitCode = res.exitValue)
//            File("${p.outRoot}/${p.testName}-actual.xml").writeText(testResult.toXML())
//            testResult.printHuman()
//
//            if (res.exitValue != 0) {
//                clog("PHP shitted at us with code ${res.exitValue}")
//                exitProcess(1)
//            }
//        }
//    }

//    private fun execPHP(p: TestParams): RunProcessResult {
//        return runProcessAndWait(listOf(PHP_INTERP, p.outFilePath), inheritIO = false)
//    }

}

object PrepareFuckAroundPHP {
    @JvmStatic
    fun main(args: Array<String>) {
        CompileStdlib.main(arrayOf())
        GrossTestPhizdets.main(arrayOf())
    }
}

fun copyFile(from: String, to: String) {
    val toFile = File(to)
    toFile.parentFile.mkdirs()
    toFile.writeText(File(from).readText())
}

@Ser @XmlRootElement(name = "rrlog") @XmlAccessorType(XmlAccessType.FIELD)
class RRLog {
    @XmlElement(name = "entry")
    val entries = Collections.synchronizedList(mutableListOf<RRLogEntry>())
}

@Ser @XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
class RRLogEntry(val id: Long, val pathInfo: String, val queryString: String, val requestJSON: String, val responseJSON: String)

private fun printSectionTitle(title: String) {
    clog()
    clog("========== $title ==========\n")
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

private fun sendTestResultToDevTools(req: PDTRemoteCommand_TestResult) {
    print("Sending shit to photlin-dev-tools...")
    val proc = req::class.simpleName!!.substring("PDTRemoteCommand_".length)
    val rawResponse = HTTPClient.postJSON(
        "http://localhost:" + PhotlinDevToolsGlobal.rpcServerPort + "?proc=$proc",
        ObjectMapper().writeValueAsString(req))
    println(" " + rawResponse)
    //clog("Response: $rawResponse")
}

fun filesFrom(dirPath: String): List<File> {
    val dir = File(dirPath)
    check(dir.isDirectory) {"bea2f16f-20e7-43f7-8535-4be029c415c3"}
    return dir.listFiles {_, name -> name.endsWith(".kt")}.toList()
}

object MapPhizdetsStack {
    private val mappingCache =
        /*if (CACHE_MAPPINGS_BETWEEN_REQUESTS) sharedMappingCache
        else*/ makeMappingCache()

    class StackLine(val prefix: String, val resource: String, val line: Int, val column: Int)

    sealed class MapPath {
        class Just(val path: String) : MapPath()
        class Skip : MapPath()
        class Bitch : MapPath()
    }

    private class Skip(reason: String) : Exception(reason)
    private class Verbatim(reason: String) : Exception(reason)

    fun mapFuckingLine(stackLine: StackLine, getMapPath: (resource: String) -> MapPath): String? {
        if (stackLine.line == 1) throw Skip("Ignoring line 1, as it's probably __awaiter() or some other junk")

        val fuck = getMapPath(stackLine.resource)
        val mapPath = when (fuck) {
            is MapPath.Skip -> return null
            is MapPath.Bitch -> throw Verbatim("No map file for ${stackLine.resource}")
            is MapPath.Just -> fuck.path
        }

        val sourceMapping = mappingCache[mapPath]

        val orig = sourceMapping.getMappingForLine(stackLine.line, stackLine.column)
            ?: throw Verbatim("No mapping for line")

        var longPath = orig.originalFile
        var shortPath = orig.originalFile
        if (longPath.startsWith("file://")) {
            longPath = normalizePath(shortPath.substring("file://".length))
            shortPath = when {
            //                        longPath.startsWith(NORMAL_APS_HOME) -> "APS" + longPath.substring(NORMAL_APS_HOME.length)
            //                        longPath.startsWith(NORMAL_KOMMON_HOME) -> "KOMMON" + longPath.substring(NORMAL_KOMMON_HOME.length)
                else -> shortPath
            }
        }

        var marginNotes = mutableListOf<String>()
        try {
            val line = File(longPath).readLines()[orig.lineNumber - 1]
            if (line.contains(Regex("\\so\\."))) marginNotes.add("o.")
            Regex("\\bassert(\\w|\\d|_)*").find(line)?.let {marginNotes.add(it.value)}
            when {
                line.contains("\"\"\"") -> marginNotes.add("\"\"\"")
                line.contains("\"") -> marginNotes.add("\"")
            }
        } catch (e: Exception) {
        }

        val result = "${stackLine.prefix} ($shortPath:${orig.lineNumber}:${orig.columnPosition})" +
            nbsp.repeat(5) + marginNotes.joinToString(nbsp.repeat(3))
        return result
    }

    fun goBananas(rawStack: String): String {
        val noisy = false

        // Ex:    at Object.die_61zpoe$ (http://aps-ua-writer.local:3022/into-kommon-js-enhanced.js:32:17)
        // Ex:    at http://aps-ua-writer.local:3022/front-enhanced.js:12225:48
        // Ex:    at Generator.next (<anonymous>)
        // Ex:    at __awaiter (http://aps-ua-writer.local:3022/into-kommon-js-enhanced.js:1:138)

//        val NORMAL_APS_HOME = normalizePath(const.file.APS_HOME)
//        val NORMAL_KOMMON_HOME = normalizePath(KOMMON_HOME)


        val resultLines = mutableListOf<String>()

        for (mangledLine in rawStack.lines()) {

            try {
                if (!mangledLine.startsWith("    at ")) throw Verbatim("Doesn't start with [at]")

                val mr = Regex("(.*?)\\(?(https?://.*):(\\d+):(\\d+)\\)?").matchEntire(mangledLine)
                    ?: run {
                    if (mangledLine.contains("at Generator.next")) throw Skip("Useless junk")
                    throw Verbatim("Unrecognized")
                }
                if (noisy) dwarnStriking("Recognized: \n" + inspectMatchResult(mr))

                val stackLine = run {
                    val (prefix, resource, lineString, columnString) = mr.destructured
                    val (line, column) =
                        try {Pair(lineString.toInt(), columnString.toInt())}
                        catch (e: NumberFormatException) {throw Verbatim("Bad line or column number")}
                    StackLine(prefix, resource, line, column)
                }

                val result = mapFuckingLine(stackLine, getMapPath = {resource->
                    when {
                    //                    resource.contains("/front-enhanced.js") -> "${const.file.APS_HOME}/front/out/front.js.map"
                    //                    resource.contains("/into-kommon-js-enhanced.js") -> "$KOMMON_HOME/js/out/into-kommon-js.js.map"
                        resource.contains("pizda") -> MapPath.Just("pizda.map")
                        else -> MapPath.Bitch()
                    }
                })
                if (result != null)
                    resultLines.add(result)
            }
            catch (e: Skip) {
                if (noisy) dwarnStriking("Skip: ${e.message}: $mangledLine")
            }
            catch (e: Verbatim) {
                if (noisy) dwarnStriking("Verbatim: ${e.message}: $mangledLine")
                resultLines.add(
                    if (mangledLine.startsWith("    at ")) mangledLine.replaceRange(0, 1, when {
                        mangledLine.contains("kotlin") -> "K" // Standard library
                        else -> "?"
                    })
                    else mangledLine
                )
            }
        }

        return resultLines.joinToString("\n")
    }


    private fun makeMappingCache(): LoadingCache<String, SourceMapping> {
        return CacheBuilder.newBuilder().build(object:CacheLoader<String, SourceMapping>() {
            override fun load(mapPath: String): SourceMapping {
                return SourceMapConsumerFactory.parse(File(mapPath).readText())
            }
        })
    }

    inline fun dwarnStriking(vararg xs: Any?) = dwarn("\n\n", "**********", *xs, "\n")

    fun dwarn(vararg xs: Any?) = clog(xs.joinToString(" "))

    fun inspectMatchResult(mr: MatchResult): String =
        mr.groups.mapIndexed {i, g -> "Group $i: ${g?.value}"}.joinToString("\n")

    val nbsp: String = "" + 0xa0.toChar()

    fun normalizePath(junky: String): String {
        val path = File(junky).toPath()
        var normal = path.normalize().toString()
        if (normal.matches(Regex("\\w:(\\\\|/).*"))) normal = normal[0].toLowerCase() + normal.substring(1)
        return normal
    }
}























