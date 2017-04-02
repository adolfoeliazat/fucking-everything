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

object CompileStdlib {
    private var jsProgram by notNullOnce<JsProgram>()

    @JvmStatic
    fun main(args: Array<String>) {
        val inFile = File("E:/fegh/phizdets/phizdetsc/src/phizdets/php/kotlin--adapted.js")
        val options = Options("nashorn")
        options.set("anon.functions", true)
        options.set("parse.only", true)
        options.set("scripting", true)

        val errors = ErrorManager()
        val context = Context(options, errors, Thread.currentThread().contextClassLoader)
        val origSourceCode = inFile.readText()
        val source = Source.sourceFor(inFile.name, origSourceCode)
        val parser = Parser(context.env, source, errors)
        val nhProgram = parser.parse()

        jsProgram = JsProgram()
        fillBlock(jsProgram.globalBlock, nhProgram.body)

        run {
            val output = TextOutputImpl()
            val taggedGenOutput = TextOutputImpl()
            jsProgram.accept(PhizdetsSourceGenerationVisitor(output, taggedGenOutput, /*sourceMapBuilder*/ null))
            File("E:/fegh/phizdets/phizdetsc/src/phizdets/php/phizdets-stdlib--0.php").writeText(output.toString())
        }

        run {
            phpify1(jsProgram)
            val output = TextOutputImpl()
            val taggedGenOutput = TextOutputImpl()
            jsProgram.accept(PhizdetsSourceGenerationVisitor(output, taggedGenOutput, /*sourceMapBuilder*/ null))
            File("E:/fegh/phizdets/phizdetsc/src/phizdets/php/phizdets-stdlib--1.php").writeText(output.toString())
        }

        run {
            phpify2(jsProgram)
            val output = TextOutputImpl()
            val taggedGenOutput = TextOutputImpl()
            jsProgram.accept(PhizdetsSourceGenerationVisitor(output, taggedGenOutput, /*sourceMapBuilder*/ null))
            File("E:/fegh/phizdets/phizdetsc/src/phizdets/php/phizdets-stdlib.php").writeText(output.toString())
        }

        clog("OK")
    }

    private fun fillBlock(jsBlock: JsBlock, nhBlock: Block) {
        for (nhs in nhBlock.statements) {
            val jsStatement: JsStatement = when (nhs) {
                is ExpressionStatement -> {
                    JsExpressionStatement(mapExpression(nhs.expression))
                }
                is IfNode -> {
                    val ifExpression = mapExpression(nhs.test)
                    val thenStatement = mapBlock(nhs.pass)
                    val elseStatement = nhs.fail?.let {mapBlock(it)}
                    JsIf(ifExpression, thenStatement, elseStatement)
                }
                is VarNode -> {
                    val name = jsProgram.rootScope.declareName(nhs.name.name)
                    val initExpression = nhs.init?.let {mapExpression(it)}
                    JsVars(JsVars.JsVar(name, initExpression))
                }
                is ReturnNode -> {
                    when (nhs.expression) {
                        null -> JsReturn()
                        else -> JsReturn(mapExpression(nhs.expression))
                    }
                }
                is ForNode -> {
                    if (nhs.isForEach) {
                        wtf("f6e68fa7-6479-48ab-8dc7-4f0cce161255")
                    } else if (nhs.isForIn) {
                        wtf("ed650c69-b5f8-43d1-bca6-1ff1c5851b2f")
                    } else {
                        if (nhs.init == null) {
                            val condition = mapExpression(nhs.test)
                            val increment = mapExpression(nhs.modify)
                            val body = mapBlock(nhs.body)
                            JsFor(JsVars(), condition, increment, body)
                        } else {
                            val init = mapExpression(nhs.init)
                            val condition = mapExpression(nhs.test)
                            val increment = mapExpression(nhs.modify)
                            val body = mapBlock(nhs.body)
                            JsFor(init, condition, increment, body)
                        }
                    }
                }
                is ThrowNode -> {
                    JsThrow(mapExpression(nhs.expression))
                }
                is WhileNode -> {
                    val condition = mapExpression(nhs.test)
                    val body = mapBlock(nhs.body)
                    JsWhile(condition, body)
                }
                is BreakNode -> {
                    val label = when (nhs.labelName) {
                        null -> null
                        else -> JsNameRef(nhs.labelName)
                    }
                    JsBreak(label)
                }
                is SwitchNode -> {
                    val expression = mapExpression(nhs.expression)
                    val cases = nhs.cases.map {nhCase->
                        val jsSwitchMember = when {
                            nhCase.test != null -> {
                                JsCase().also {
                                    it.caseExpression = mapExpression(nhCase.test)
                                }
                            }
                            else -> {
                                JsDefault()
                            }
                        }

                        val tempJsBlock = JsBlock()
                        fillBlock(tempJsBlock, nhCase.body)
                        for (x in tempJsBlock.statements)
                            jsSwitchMember.statements.add(x)

                        jsSwitchMember
                    }
                    JsSwitch(expression, cases)
                }
                is LabelNode -> {
                    val label = jsProgram.scope.declareName(nhs.labelName)
                    val statement = mapBlock(nhs.body)
                    JsLabel(label, statement)
                }
                is ContinueNode -> {
                    val label = when (nhs.labelName) {
                        null -> null
                        else -> JsNameRef(nhs.labelName)
                    }
                    JsContinue(label)
                }
                is BlockStatement -> {
                    mapBlock(nhs.block)
                }
                is TryNode -> {
                    val tryBlock = mapBlock(nhs.body)
                    val jsCatch: JsCatch? = when {
                        nhs.catches.isEmpty() -> null
                        nhs.catches.size == 1 -> {
                            val nhCatch = nhs.catches.first()
                            JsCatch(jsProgram.scope, nhCatch.exception.name, mapBlock(nhCatch.body))
                        }
                        else -> wtf("82391a9a-cfab-4361-b114-a757026105f4")
                    }
                    val finallyBlock: JsBlock? = when {
                        nhs.finallyBody == null -> null
                        else -> mapBlock(nhs.finallyBody)
                    }
                    JsTry(tryBlock, jsCatch, finallyBlock)
                }
                else -> {
                    wtf("${nhs::class.simpleName}    6956184e-e142-49a2-a182-9cc995c058e5")
                }
            }
            jsBlock.statements += jsStatement
        }
    }

    private fun mapExpression(nhe: Expression): JsExpression {
        return when (nhe) {
            is IdentNode -> {
                JsNameRef(nhe.name)
            }
            is LiteralNode<*> -> {
                when {
                    nhe.isNull -> {
                        JsNullLiteral()
                    }
                    nhe.isString -> {
                        JsStringLiteral(nhe.string)
                    }
                    nhe.isNumeric -> {
                        jsProgram.getNumberLiteral(nhe.number)
                    }
                    nhe.value is Boolean -> {
                        if (nhe.value as Boolean) JsLiteral.TRUE else JsLiteral.FALSE
                    }
                    nhe is LiteralNode.ArrayLiteralNode -> {
                        JsArrayLiteral(nhe.elementExpressions.map(this::mapExpression))
                    }
                    else -> {
                        wtf("$nhe    bf467fef-f9d7-44f1-9143-161303af2563")
                    }
                }
            }
            is CallNode -> {
                val qualifier = mapExpression(nhe.function)
                val arguments = nhe.args.map(this::mapExpression)
                JsInvocation(qualifier, arguments)
            }
            is FunctionNode -> {
                val body = mapBlock(nhe.body)
                JsFunction(JsFunctionScope(jsProgram.rootScope, "boobs"), body, "boobs")-{o->
                    for (nhParam in nhe.parameters) {
                        o.parameters += JsParameter(jsProgram.scope.declareName(nhParam.name))
                    }
                }
            }
            is UnaryNode -> {
                if (nhe.tokenType() == TokenType.NEW) {
                    val nhCall = nhe.expression as? CallNode
                        ?: wtf("643f563b-869e-41ee-a4ea-11de930d50dc")
                    val ctor = mapExpression(nhCall.function)
                    val args = nhCall.args.map(this::mapExpression)
                    JsNew(ctor, args)
                } else {
                    val (op: JsUnaryOperator, isPrefix) = when (nhe.tokenType()) {
                        TokenType.TYPEOF -> JsUnaryOperator.TYPEOF to true
                        TokenType.NOT -> JsUnaryOperator.NOT to true
                        TokenType.INCPOSTFIX -> JsUnaryOperator.INC to false
                        TokenType.DECPOSTFIX -> JsUnaryOperator.DEC to false
                        TokenType.INCPREFIX -> JsUnaryOperator.INC to true
                        TokenType.DECPREFIX -> JsUnaryOperator.DEC to true
                        TokenType.ADD -> JsUnaryOperator.POS to true
                        TokenType.SUB -> JsUnaryOperator.NEG to true
                        TokenType.BIT_NOT -> JsUnaryOperator.BIT_NOT to true
                        TokenType.VOID -> JsUnaryOperator.VOID to true
                        TokenType.DELETE -> JsUnaryOperator.DELETE to true
                        else -> wtf("token = ${nhe.tokenType().name}    68e5ec78-97a1-49c7-9e2e-741dd6ef99b2")
                    }
                    val arg = mapExpression(nhe.expression)
                    if (isPrefix) JsPrefixOperation(op, arg) else JsPostfixOperation(op, arg)
                }
            }
            is BinaryNode -> {
                val op: JsBinaryOperator = when (nhe.tokenType()) {
                    TokenType.AND -> JsBinaryOperator.AND
                    TokenType.BIT_AND -> JsBinaryOperator.BIT_AND
                    TokenType.OR -> JsBinaryOperator.OR
                    TokenType.BIT_OR -> JsBinaryOperator.BIT_OR
                    TokenType.EQ -> JsBinaryOperator.EQ
                    TokenType.EQ_STRICT -> JsBinaryOperator.REF_EQ
                    TokenType.ASSIGN -> JsBinaryOperator.ASG
                    TokenType.IN -> JsBinaryOperator.INOP
                    TokenType.MUL -> JsBinaryOperator.MUL
                    TokenType.LT -> JsBinaryOperator.LT
                    TokenType.ADD -> JsBinaryOperator.ADD
                    TokenType.NE_STRICT -> JsBinaryOperator.REF_NEQ
                    TokenType.SAR -> JsBinaryOperator.SHR
                    TokenType.SHL -> JsBinaryOperator.SHL
                    TokenType.INSTANCEOF -> JsBinaryOperator.INSTANCEOF
                    TokenType.LE -> JsBinaryOperator.LTE
                    TokenType.GE -> JsBinaryOperator.GTE
                    TokenType.MOD -> JsBinaryOperator.MOD
                    TokenType.DIV -> JsBinaryOperator.DIV
                    TokenType.ASSIGN_ADD -> JsBinaryOperator.ASG_ADD
                    TokenType.SUB -> JsBinaryOperator.SUB
                    TokenType.BIT_XOR -> JsBinaryOperator.BIT_XOR
                    TokenType.NE -> JsBinaryOperator.NEQ
                    TokenType.GT -> JsBinaryOperator.GT
                    TokenType.SHR -> JsBinaryOperator.SHRU
                    TokenType.ASSIGN_BIT_AND -> JsBinaryOperator.ASG_BIT_AND
                    TokenType.ASSIGN_SUB -> JsBinaryOperator.ASG_SUB
                    TokenType.COMMARIGHT -> JsBinaryOperator.COMMA
                    TokenType.COMMALEFT ->
                        wtf("a4dbf36d-31be-46c1-8ecf-f95288b72dc0")
                    else -> wtf("token = ${nhe.tokenType().name}    cd8fb0f4-ddaa-4c62-ae8f-22492b72ed6d")
                }
                val arg1 = mapExpression(nhe.lhs())
                val arg2 = mapExpression(nhe.rhs())
                JsBinaryOperation(op, arg1, arg2)
            }
            is JoinPredecessorExpression -> {
                mapExpression(nhe.expression)
            }
            is AccessNode -> {
                JsNameRef(nhe.property, mapExpression(nhe.base))
            }
            is ObjectNode -> {
                val properties = nhe.elements.map {
                    val labelExpr = mapExpression(it.key)
                    val valueExpr = mapExpression(it.value)
                    JsPropertyInitializer(labelExpr, valueExpr)
                }
                JsObjectLiteral(properties)
            }
            is TernaryNode -> {
                val testExpression = mapExpression(nhe.test)
                val thenExpression = mapExpression(nhe.trueExpression)
                val elseExpression = mapExpression(nhe.falseExpression)
                JsConditional(testExpression, thenExpression, elseExpression)
            }
            is IndexNode -> {
                val arrayExpression = mapExpression(nhe.base)
                val indexExpression = mapExpression(nhe.index)
                JsArrayAccess(arrayExpression, indexExpression)
            }
            else -> {
                wtf("${nhe::class.simpleName}    a422c443-7e15-4714-8080-1bea0f4db5bc")
            }
        }
    }

    private fun mapBlock(nhBlock: Block): JsBlock {
        val jsBlock = JsBlock()
        fillBlock(jsBlock, nhBlock)
        return jsBlock
    }
}


object GrossTestPhizdets {
    val preventCompilationAndDeploying =
        false
//        true

    val preventRequest =
//        false
        true

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
                copyFile(p.outFilePath + "--tagged-gen", deploymentDir + "/${p.testName}.php--tagged-gen")
                copyFile(p.outRoot + "/phi-engine.php", deploymentDir + "/phi-engine.php")

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

fun phpify(program: JsProgram) {
//    object : JsVisitorWithContextImpl() {
//        override fun endVisit(x: JsLabel, ctx: JsContext<*>) {
//            super.endVisit(x, ctx)
//            clog("Label:", x.name)
//        }
//    }.accept(program)
//    exitProcess(1)

    phpify1(program)
    phpify2(program)
}

fun phpify1(program: JsProgram) {
    object : JsVisitorWithContextImpl() {
        override fun endVisit(x: JsLabel, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            ctx.replaceMe(JsBlock(
                JsStringLiteral("Here was label ${x.name.ident}").makeStmt(),
                x.statement,
                JsNameRef("${escapeIdent(x.name.ident)}:").makeStmt()
            ))
        }

        override fun visit(x: JsBreak, ctx: JsContext<JsNode>): Boolean {
            super.endVisit(x, ctx)
            if (x.label != null) {
                ctx.replaceMe(JsNameRef("goto ${escapeIdent(x.label.ident)}").makeStmt())
                return false
            }
            return true
        }

        private fun escapeIdent(ident: String): String {
            return ident.replace("$", "_usd_")
        }
    }.accept(program)
}

fun phpify2(program: JsProgram) {
    object : JsVisitorWithContextImpl() {
        val shitToShit = mutableMapOf<Any, Any?>()
        var nextDebugTag = 1L

        fun nextDebugTagLiteral(): JsExpression {
            return JsStringLiteral("@@${nextDebugTag++}")
        }

        override fun endVisit(x: JsReturn, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            ctx.replaceMe(JsReturn(invocation("phiEvaluate", listOf(x.expression))))
        }

        override fun endVisit(x: JsThrow, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            ctx.replaceMe(JsInvocation(JsNameRef("phiThrow"), x.expression).makeStmt())
        }

        override fun endVisit(x: JsStringLiteral, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            ctx.replaceMe(new("PhiStringLiteral", listOf(x)))
        }

        override fun endVisit(x: JsVars, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            val shit = x.vars.map {
                JsInvocation(JsNameRef("array"), JsStringLiteral(it.name.ident), it.initExpression
                    ?: new("PhiUnaryOperation", listOf(JsStringLiteral("prefix"), JsStringLiteral("void"), new("PhiNumberLiteral", listOf(JsStringLiteral("@@something"), program.getNumberLiteral(0))))))
            }
            ctx.replaceMe(JsInvocation(JsNameRef("phiVars"), nextDebugTagLiteral(), JsInvocation(JsNameRef("array"), *shit.toTypedArray())).makeStmt())
        }

        override fun endVisit(x: JsArrayLiteral, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            ctx.replaceMe(new("PhiArrayLiteral", listOf(invocation("array", x.expressions))))
        }

        override fun endVisit(x: JsObjectLiteral, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            val args = x.propertyInitializers.map {
                invocation("array", listOf(it.labelExpr, it.valueExpr))
            }
            ctx.replaceMe(new("PhiObjectLiteral", listOf(nextDebugTagLiteral(), invocation("array", args))))
        }

        override fun endVisit(x: JsInvocation, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            ctx.replaceMe(new("PhiInvocation", listOf(x.qualifier, invocation("array", x.arguments))))
        }

        override fun endVisit(x: JsConditional, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            ctx.replaceMe(new("PhiConditional", listOf(x.testExpression, x.thenExpression, x.elseExpression)))
        }

        var shitCounter = 0
        override fun endVisit(x: JsExpressionStatement, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)

            ctx.replaceMe(invocation("\$GLOBALS['shit'] = ${++shitCounter}; phiExpressionStatement", listOf(x.expression)).makeStmt())

//            ctx.replaceMe(JsBlock(
//                JsNameRef("\$GLOBALS['shit'] = ${++shitCounter}").makeStmt(),
//                invocation("phiExpressionStatement", listOf(x.expression)).makeStmt()
//            ))

//            ctx.replaceMe(invocation("phiExpressionStatement", listOf(x.expression)).makeStmt())
        }

        override fun endVisit(x: JsFunction, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            val argNames = mutableListOf<JsExpression>()
            for (p in x.parameters)
                argNames += JsStringLiteral(p.name.ident)
            val args = JsInvocation(JsNameRef("array"), argNames)
            val body = x.deepCopy()
            body.name = null
            body.parameters.clear()
            val name = when (x.name) {
                null -> JsNullLiteral()
                else -> JsStringLiteral(x.name.ident)
            }

            ctx.replaceMe(new("PhiFunctionExpression", listOf(name, args, body)))
        }

        fun invocation(functionName: String, args: List<JsExpression>): JsInvocation {
            return JsInvocation(JsNameRef(functionName), args)
        }

        fun new(ctor: String, args: List<JsExpression>): JsNew {
            return new(JsNameRef(ctor), args)
        }

        override fun endVisit(x: JsIf, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            val ifExpression = invocation("phiEvaluateToBoolean", listOf(x.ifExpression))
            ctx.replaceMe(JsIf(ifExpression, x.thenStatement, x.elseStatement))
        }

        override fun endVisit(x: JsBinaryOperation, ctx: JsContext<JsNode>) {
            super.visit(x, ctx)
            ctx.replaceMe(new("PhiBinaryOperation", listOf(nextDebugTagLiteral(), JsStringLiteral(x.operator.toString()), x.arg1, x.arg2)))
        }

        private fun new(ctor: JsNameRef, args: List<JsExpression>) = JsNew(ctor, args)

        override fun endVisit(x: JsArrayAccess, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            ctx.replaceMe(new("PhiBrackets", listOf(x.array, x.index)))
        }

        override fun endVisit(x: JsLiteral.JsThisRef, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            ctx.replaceMe(new("PhiThis", listOf()))
        }

        override fun endVisit(x: JsNew, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            ctx.replaceMe(new("PhiNew", listOf(x.constructorExpression, invocation("array", x.arguments))))
        }

        override fun endVisit(x: JsNullLiteral, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            ctx.replaceMe(new("PhiNullLiteral", listOf(nextDebugTagLiteral())))
        }

        override fun endVisit(x: JsNumberLiteral, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            ctx.replaceMe(new("PhiNumberLiteral", listOf(nextDebugTagLiteral(), x)))
        }

        override fun endVisit(x: JsNameRef, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            val replacement = when {
                x.qualifier == null -> {
                    val dd = x.declarationDescriptor
                    val shit = when {
                        dd is SimpleFunctionDescriptor && dd.isExternal -> "PhiExternalNameRef"
                        else -> "PhiNameRef"
                    }
                    new(shit, listOf(JsStringLiteral(x.ident)))
                }
                else -> new("PhiDot", listOf(x.qualifier!!, JsStringLiteral(x.ident)))
            }
            try {
                ctx.replaceMe(replacement)
            } catch(e: Exception) {
                "break on me"
                throw e
            }
        }

        override fun endVisit(x: JsPrefixOperation, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            ctx.replaceMe(new("PhiUnaryOperation", listOf(JsStringLiteral("prefix"), JsStringLiteral(x.operator.toString()), x.arg)))
        }

        override fun endVisit(x: JsPostfixOperation, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            ctx.replaceMe(new("PhiUnaryOperation", listOf(JsStringLiteral("postfix"), JsStringLiteral(x.operator.toString()), x.arg)))
        }

        override fun endVisit(x: JsCatch, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)

            val ident = "Exception \$__phiException"
            val catchBody = JsBlock()
            catchBody.statements.add(JsNameRef("phiVars(array(array('${x.parameter.name.ident}', \$__phiException->phiValue)));").makeStmt())
            catchBody.statements.addAll(x.body.statements)
            ctx.replaceMe(JsCatch(x.scope, ident, catchBody))
        }


    }.accept(program)
}


























