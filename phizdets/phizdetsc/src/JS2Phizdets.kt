package phizdets.compiler

import vgrechka.*
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.debugging.sourcemap.SourceMapping
import com.google.debugging.sourcemap.proto.Mapping
import org.jetbrains.kotlin.cli.js.K2PhizdetsCompiler
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.facade.K2JSTranslator
import org.jetbrains.kotlin.js.translate.utils.jsAstUtils.array
import org.jetbrains.kotlin.js.translate.utils.jsAstUtils.index
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
import org.jetbrains.kotlin.incremental.makeModuleFile
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.js.facade.SourceMapBuilderConsumer
import org.jetbrains.kotlin.js.sourceMap.PhizdetsSourceGenerationVisitor
import org.jetbrains.kotlin.js.sourceMap.SourceMap3Builder
import org.jetbrains.kotlin.js.util.TextOutputImpl
import org.junit.Assert
import org.junit.Assert.fail
import org.junit.Test

object JS2Phizdets {
    @JvmStatic
    fun main(args: Array<String>) {
        val inputFile = File(args[0])
        clog("JS2Phizdets: ${inputFile.name}")

        Barbos(inputFilePath = inputFile.absolutePath,
               outputFilePath = run {
                   val fuck = inputFile.absolutePath
                   check(fuck.endsWith(".js")) {"cfaf84a6-215a-4f49-ae37-6b106deb1347"}
                   fuck.substring(0, fuck.length - 3) + ".php"},
               copyPhiEngine = true,
               copyPhiStdlib = true)
            .ignite()

        clog("OK, strange")
    }
}

class Barbos(val inputFilePath: String, val outputFilePath: String, val copyPhiEngine: Boolean, val copyPhiStdlib: Boolean) {
    private var jsProgram by notNullOnce<JsProgram>()
    private var source by notNullOnce<Source>()
    private var jsMapping by notNullOnce<SourceMapping>()

    fun ignite() {
        val jsMapFile = File(inputFilePath + ".map")
        check(jsMapFile.exists()) {"6a5793bb-2a14-4039-a7ac-98f0a61615e5"}
        jsMapping = SourceMappingCache.getMapping(jsMapFile.path)

        PhizdetscGlobal.debugTagPrefix = "s"
        val inFile = File(inputFilePath)
        val options = Options("nashorn")
        options.set("anon.functions", true)
        options.set("parse.only", true)
        options.set("scripting", true)

        val errors = ErrorManager()
        val context = Context(options, errors, Thread.currentThread().contextClassLoader)
        val origSourceCode = inFile.readText()
        source = Source.sourceFor(inFile.name, origSourceCode)
        val parser = Parser(context.env, source, errors)
        val nhProgram = parser.parse()

        jsProgram = JsProgram()
        fillBlock(jsProgram.globalBlock, nhProgram.body)

        run {
            val output = TextOutputImpl()
            val taggedGenOutput = TextOutputImpl()
            jsProgram.accept(PhizdetsSourceGenerationVisitor(output, taggedGenOutput, /*sourceMapBuilder*/ null))
            File("$outputFilePath--0").writeText(output.toString())
        }

        val phpifier = Phpifier(jsProgram)

        run {
            phpifier.stage1()
            val output = TextOutputImpl()
            val taggedGenOutput = TextOutputImpl()
            jsProgram.accept(PhizdetsSourceGenerationVisitor(output, taggedGenOutput, /*sourceMapBuilder*/ null))
            File("$outputFilePath--1").writeText(output.toString())
        }

        val outputFile = File(outputFilePath)
        run {
            phpifier.stage2()
            val output = TextOutputImpl()
            val taggedGenOutput = TextOutputImpl()
            val sourceMapBuilder = SourceMap3Builder(outputFile, output, SourceMapBuilderConsumer())
            jsProgram.accept(PhizdetsSourceGenerationVisitor(output, taggedGenOutput, sourceMapBuilder))
            outputFile.writeText(output.toString())
            val mapFile = File("$outputFilePath.map")
            mapFile.writeText(sourceMapBuilder.build())
        }

        if (copyPhiEngine) {
            val shit = javaClass.classLoader.getResource("phizdets/php/phi-engine.php").readText()
            File (outputFile.parent + "/phi-engine.php").writeText(shit)
        }

        if (copyPhiStdlib) {
            val shit = javaClass.classLoader.getResource("phizdets/php/phizdets-stdlib.php").readText()
            File (outputFile.parent + "/phizdets-stdlib.php").writeText(shit)
        }
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

            if (jsStatement !is JsExpressionStatement) {
                fillSourcePosition(jsStatement, nhs)
            }

            jsBlock.statements += jsStatement
        }
    }

//    private val jsLineToKtLine = mutableMapOf<Int, Int>()
    private fun fillSourcePosition(to: JsNode, from: Node) {
        val pos = from.position()
        val jsLine = source.getLine(pos)
        val jsColumn = source.getColumn(pos) + 1
        val ktPlace: Mapping.OriginalMapping?
        ktPlace = jsMapping.getMappingForLine(jsLine, jsColumn)
        if (ktPlace != null) {
            val ktLine = ktPlace.lineNumber - 1
//            if (ktLine == 5) {
//                "fuck"
//            }
//            val existingKtLine = jsLineToKtLine[jsLine]
//            if (existingKtLine != null && existingKtLine != ktLine) {
//                "qwe"
//            } else {
//                jsLineToKtLine[jsLine] = ktLine
//            }
            to.source(FileLineColumn(inputFilePath, ktLine, ktPlace.columnPosition))
        }
    }

    private fun mapExpression(nhe: Expression): JsExpression {
        val jsExpression = when (nhe) {
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
                JsFunction(JsFunctionScope(jsProgram.rootScope, "boobs"), body, "boobs") - {o ->
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
        fillSourcePosition(jsExpression, nhe)
        return jsExpression
    }

    private fun mapBlock(nhBlock: Block): JsBlock {
        val jsBlock = JsBlock()
        fillBlock(jsBlock, nhBlock)
        return jsBlock
    }
}

class BarbosTests {
    @Test
    fun test1() {
        val jsFileName = "fuck.js"
        val scuko = this::class.qualifiedName!!.replace(".", "/") + "_files/" + this::test1.name
        val shit = scuko + "/" + jsFileName
        val jsFile = File(javaClass.classLoader.getResource(shit).path)
        val mapFile = File(jsFile.path + ".map")
        val outDir = File(System.getProperty("java.io.tmpdir") + "/" + scuko.replace("/", "."))
        if (outDir.exists()) {
            outDir.listFiles().forEach {
                check(it.delete()) {"2f1288a5-e95c-4647-bad6-59835d718ebc"}
            }
        } else {
            check(outDir.mkdir()) {"856e0f03-a79e-4e40-98d4-7261f05da9c4"}
        }

        val outputFilePath = outDir.path + "/" + jsFileName + ".php"
        Barbos(inputFilePath = jsFile.path,
               outputFilePath = outputFilePath,
               copyPhiEngine = true,
               copyPhiStdlib = true)
            .ignite()

        fun dumpMapFile(path: String) {
            clog(path + ":")
            val mapping = SourceMappingCache.getMapping(path)
            val penetration = mapping.penetration
            penetration.dumpSourceLineToGeneratedLine()
        }

        dumpMapFile(mapFile.absolutePath)
        dumpMapFile(outputFilePath + ".map")

        // TODO:vgrechka Maybe some assertions? :)
    }
}





















