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

fun phpify(program: JsProgram) {
    val phpifier = Phpifier(program)
    PhizdetscGlobal.reportTranslationStage(0, program)
    phpifier.stage1()
    PhizdetscGlobal.reportTranslationStage(1, program)
    phpifier.stage2()
    PhizdetscGlobal.reportTranslationStage(2, program)
}

class Phpifier(val program: JsProgram, val opts: Opts = Opts()) {
    class Opts(
        val skipDebuggingOptionInExpressionStatements: Boolean = false)

    var nextDebugTag = 1L

    val shitToShit = mutableMapOf<Any, Any?>()
    var JsNode.skipTransformation by AttachedShit<Boolean?>(shitToShit)
    var JsNode.isPhiVarsInvocation by AttachedShit<Boolean?>(shitToShit)

    fun nextDebugTagLiteral(): JsExpression {
        return JsStringLiteral(nextDebugTag())
    }

    private fun nextDebugTag() = "@@" + nextDebugTagWithoutAts()

    private fun nextDebugTagWithoutAts() = "${PhizdetscGlobal.debugTagPrefix}${nextDebugTag++}"

    private fun literalCodeStatement(code: String): JsStatement {
        val nameRef = JsNameRef(code)
        nameRef.skipTransformation = true
        val statement = nameRef.makeStmt()
        statement.skipTransformation = true
        return statement
    }

    inner class LoopKillingStuff {
        val loopID = nextDebugTagWithoutAts()
        val loopCounterVar = "\$__phi__loopCounter_$loopID"

        fun addShitBeforeLoop(ctx: JsContext<JsNode>) {
            ctx.addPrevious(literalCodeStatement("$loopCounterVar = 0;"))
        }

        fun transformBody(originalBody: JsStatement): JsBlock {
            val fuck = literalCodeStatement(
                "if (defined('PHI_KILL_LONG_LOOPS')) {" +
                    "    if (++$loopCounterVar === 100) { phiKillLongLoop(); };" +
                    "}")

            return JsBlock(mutableListOf<JsStatement>()-{o->
                o += fuck
                o += originalBody
            })
        }
    }

    fun stage1() {
        object : JsVisitorWithContextImpl() {
            override fun endVisit(x: JsNameRef, ctx: JsContext<*>) {
                super.endVisit(x, ctx)
                // @debug
//                if (x.source != null) {
//                    "break on me"
//                }
            }

            override fun endVisit(x: JsThrow, ctx: JsContext<*>) {
                super.endVisit(x, ctx)
                // @debug
//                if (x.source != null) {
//                    "break on me"
//                }
            }

            override fun endVisit(x: JsFor, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)

                val loopKillingStuff = LoopKillingStuff()
                loopKillingStuff.addShitBeforeLoop(ctx)
                val newBody = loopKillingStuff.transformBody(x.body)

                if (x.initVars != null) {
                    ctx.replaceMe(JsFor(x.initVars, x.condition, x.incrementExpression, newBody))
                }
                else if (x.initExpression != null) {
                    ctx.replaceMe(JsFor(x.initExpression, x.condition, x.incrementExpression, newBody))
                }
                else {
                    wtf("73acff0c-8fbe-466a-8e2b-b2c40e86795d")
                }
            }

            override fun endVisit(x: JsWhile, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)

                val loopKillingStuff = LoopKillingStuff()
                loopKillingStuff.addShitBeforeLoop(ctx)
                val newBody = loopKillingStuff.transformBody(x.body)

                ctx.replaceMe(JsWhile(x.condition, newBody))
            }

            override fun endVisit(x: JsExpressionStatement, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)

                if (x.expression is JsFunction) { // Hoist function declarations
                    // XXX Dirty stuff, but who gives a fuck?
                    val listContext = ctx as ListContext<JsNode>
                    val node = listContext.nodes.removeAt(listContext.index)
                    listContext.nodes.add(0, node)
                }
            }

            override fun endVisit(x: JsLabel, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)
                ctx.replaceMe(JsBlock(
                    JsStringLiteral("Here was label ${x.name.ident}").makeStmt(),
                    x.statement,
                    literalCodeStatement("${escapeIdent(x.name.ident)}:")
                ))
            }

            override fun visit(x: JsBreak, ctx: JsContext<JsNode>): Boolean {
                super.endVisit(x, ctx)
                if (x.label != null) {
                    ctx.replaceMe(literalCodeStatement("goto ${escapeIdent(x.label.ident)}"))
                    return false
                }
                return true
            }

            private fun escapeIdent(ident: String): String {
                return ident.replace("$", "_usd_")
            }
        }.accept(program)
    }

    fun stage2() {
        object : JsVisitorWithContextImpl() {
            var noFuckingNewlinesMode = false

            override fun endVisit(x: JsWhile, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)

                ctx.replaceMe(JsWhile(invocation("phiEvaluateToBoolean", listOf(x.condition)),
                                      x.body))
            }

            override fun endVisit(x: JsFor, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)

                if (x.initVars != null) {
                    wtf("1b200051-33aa-427c-8b04-3ffa42967d3f")
                    val shit = JsFor(x.initVars,
                                     invocation("phiEvaluateToBoolean", listOf(x.condition)),
                                     invocation("phiEvaluate", listOf(x.incrementExpression)),
                                     x.body)
                    ctx.replaceMe(shit)
                }
                else if (x.initExpression != null) {
                    val fuck = when {
                        x.initExpression.isPhiVarsInvocation == true -> x.initExpression
                        else -> invocation("phiEvaluate", listOf(x.initExpression))
                    }
                    val shit = JsFor(fuck,
                                     invocation("phiEvaluateToBoolean", listOf(x.condition)),
                                     invocation("phiEvaluate", listOf(x.incrementExpression)),
                                     x.body)
                    ctx.replaceMe(shit)
                }
                else {
                    wtf("ec8fa1fa-f368-4416-9a6d-599e4db32fd9")
                }
            }

            override fun endVisit(x: JsReturn, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)

                val replacement = JsReturn(invocation("phiEvaluate", listOf(x.expression ?: void0())))
                if (x.expression != null) {
                    replacement.source(x.expression.source)
                }
                ctx.replaceMe(replacement)

//                run { // @debug-5
//                    if (replacement.toString().contains("QUERY_STRING")) {
//                        "break on me"
//                    }
//                }
            }

            override fun endVisit(x: JsThrow, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)

                // @debug
                if (x.toString().contains("pizda")) {
                    val tag = x.source.debug_attachTag("dt-pizda")
//                    "break on me"
                }
                ctx.replaceMe(JsInvocation(JsNameRef("phiThrow"), x.expression).source(x.source).makeStmt())
            }

            override fun endVisit(x: JsStringLiteral, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)

                when (x.value) {
                    "@@phi-begin-noFuckingNewlines" -> noFuckingNewlinesMode = true
                    "@@phi-end-noFuckingNewlines" -> noFuckingNewlinesMode = false
                }

                ctx.replaceMe(new("PhiStringLiteral", listOf(x)).source(x.source))
            }

            private fun void0() = new("PhiUnaryOperation", listOf(nextDebugTagLiteral(), JsStringLiteral("prefix"), JsStringLiteral("void"), new("PhiNumberLiteral", listOf(JsStringLiteral("@@something"), program.getNumberLiteral(0)))))

            override fun endVisit(x: JsVars, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)
                val shit = x.vars.map {
                    JsInvocation(JsNameRef("array"), JsStringLiteral(it.name.ident), it.initExpression
                        ?: void0())
                }
                val jsInvocation = JsInvocation(JsNameRef("phiVars"), nextDebugTagLiteral(), JsInvocation(JsNameRef("array"), *shit.toTypedArray()))
                jsInvocation.isPhiVarsInvocation = true
                jsInvocation.source(x.source)
                ctx.replaceMe(jsInvocation.makeStmt())
            }


            override fun endVisit(x: JsArrayLiteral, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)
                ctx.replaceMe(new("PhiArrayLiteral", listOf(invocation("array", x.expressions))))
            }

            override fun endVisit(x: JsObjectLiteral, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)
                val args = x.propertyInitializers.map {
                    invocation("array", listOf(it.labelExpr, it.valueExpr))-{o->
                        if (!noFuckingNewlinesMode) {
                            o.insertFuckingNewlineBeforeMe = true
                        }
                    }
                }
                ctx.replaceMe(new("PhiObjectLiteral", listOf(
                    nextDebugTagLiteral(),
                    invocation("array", args)-{o->
                        if (!noFuckingNewlinesMode) {
                            o.insertFuckingNewlineAfterMe = true
                        }
                    })))
            }

            override fun endVisit(x: JsInvocation, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)

                val replacement = new("PhiInvocation", listOf(x.qualifier, invocation("array", x.arguments)))
                ctx.replaceMe(replacement.source(x.source))

//                run { // @debug-5
//                    if (replacement.toString().contains("QUERY_STRING")) {
//                        "break on me"
//                    }
//                }
            }

            override fun endVisit(x: JsConditional, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)
                if (x.toString().contains("typeof") && x.toString().contains("QUERY_STRING")) {
                    "break on me"
                }
                ctx.replaceMe(new("PhiConditional", listOf(x.testExpression, x.thenExpression, x.elseExpression))
                                  .source(x.source))
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

            override fun endVisit(x: JsIf, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)
                val ifExpression = invocation("phiEvaluateToBoolean", listOf(x.ifExpression))
                ctx.replaceMe(JsIf(ifExpression, x.thenStatement, x.elseStatement))
            }

            override fun endVisit(x: JsBinaryOperation, ctx: JsContext<JsNode>) {
                super.visit(x, ctx)
                ctx.replaceMe(new("PhiBinaryOperation", listOf(nextDebugTagLiteral(), JsStringLiteral(x.operator.toString()), x.arg1, x.arg2)))
            }

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
                if (x.skipTransformation == true)
                    return
                ctx.replaceMe(new("PhiNew", listOf(x.constructorExpression, invocation("array", x.arguments))))
            }

            override fun endVisit(x: JsNullLiteral, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)
                ctx.replaceMe(new("PhiNullLiteral", listOf(nextDebugTagLiteral())))
            }

            override fun endVisit(x: JsLiteral.JsBooleanLiteral, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)
                ctx.replaceMe(new("PhiBooleanLiteral", listOf(nextDebugTagLiteral(), x)))
            }

            override fun endVisit(x: JsNumberLiteral, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)
                ctx.replaceMe(new("PhiNumberLiteral", listOf(nextDebugTagLiteral(), x)))
            }

            override fun endVisit(x: JsNameRef, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)

                // @debug
//                if (x.source != null) {
//                    "break on me"
//                }

                if (x.skipTransformation == true)
                    return
                val replacement = when {
                    x.qualifier == null -> {
                        val dd = x.declarationDescriptor
                        val isExternal =
                            dd is SimpleFunctionDescriptor && dd.isExternal
                            || x.ident.startsWith("phi") && x.ident.length > 3 && x.ident[3].isUpperCase()
                        val shit = when {
                            isExternal -> "PhiExternalNameRef"
                            else -> "PhiNameRef"
                        }
                        new(shit, listOf(JsStringLiteral(x.ident)))
                    }
                    else -> {
                        new("PhiDot", listOf(x.qualifier!!, JsStringLiteral(x.ident)))
                    }
                }
                try {
                    replacement.debug_attachShit("replacedNode", x)
                    ctx.replaceMe(replacement)
                } catch(e: Exception) {
                    "break on me"
                    throw e
                }
            }

            fun invocation(functionName: String, args: List<JsExpression>): JsInvocation {
                return JsInvocation(JsNameRef(functionName), args)
            }

            override fun endVisit(x: JsExpressionStatement, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)
                if (x.skipTransformation == true)
                    return

                val expr = x.expression
//                if (expr is JsNew) {
//                    val constructorExpression = expr.constructorExpression
//                    if (constructorExpression is JsNameRef) {
//                        if (constructorExpression.ident == "PhiInvocation") {
//                            val firstArg = expr.arguments[0]
//                            if (firstArg is JsNew) {
//                                val firstArgConstructorExpression = firstArg.constructorExpression
//                                if (firstArgConstructorExpression is JsNameRef) {
//                                    if (firstArgConstructorExpression.ident == "PhiExternalNameRef") {
//                                        val shit = firstArg.arguments[0]
//                                        if (shit is JsStringLiteral && shit.value == "phiEval") {
//                                            val secondArg = expr.arguments[1] as JsInvocation
//                                            val secondArgQualifier = secondArg.qualifier
//                                            check(secondArgQualifier is JsNameRef && secondArgQualifier.ident == "array") {"72bb7eed-1a8f-453f-9013-c597e57be8b8"}
//                                            val bitch = secondArg.arguments[0]
//
//                                            ctx.replaceMe(invocation("\$__phiShitToEvaluate = phiEvaluate", listOf(bitch)).makeStmt())
//                                            return
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }

                val args = mutableListOf(expr)
                if (opts.skipDebuggingOptionInExpressionStatements)
                    args += JsNameRef("array('skipDebugging' => true)")-{o->
                        o.skipTransformation = true
                    }
                ctx.replaceMe(invocation("phiExpressionStatement", args).makeStmt())

//            ctx.replaceMe(invocation("\$GLOBALS['shit'] = ${++shitCounter}; phiExpressionStatement", listOf(x.expression)).makeStmt())

//            ctx.replaceMe(JsBlock(
//                JsNameRef("\$GLOBALS['shit'] = ${++shitCounter}").makeStmt(),
//                invocation("phiExpressionStatement", listOf(x.expression)).makeStmt()
//            ))

//            ctx.replaceMe(invocation("phiExpressionStatement", listOf(x.expression)).makeStmt())
            }

            private fun new(ctor: JsNameRef, args: List<JsExpression>): JsNew {
                return JsNew(ctor, args)-{o->
                    o.skipTransformation = true
                }
            }

            fun new(ctor: String, args: List<JsExpression>): JsNew {
                val ctorNameRef = JsNameRef(ctor)
                ctorNameRef.skipTransformation = true
                val new = new(ctorNameRef, args)
                new.skipTransformation = true
                new.debugTag = nextDebugTag()
//            if (new.debugTag == "@@334") {
//                "break on me"
//            }
                return new
            }

            override fun endVisit(x: JsPrefixOperation, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)
                ctx.replaceMe(new("PhiUnaryOperation", listOf(nextDebugTagLiteral(), JsStringLiteral("prefix"), JsStringLiteral(x.operator.toString()), x.arg)))
            }

            override fun endVisit(x: JsPostfixOperation, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)
                ctx.replaceMe(new("PhiUnaryOperation", listOf(nextDebugTagLiteral(), JsStringLiteral("postfix"), JsStringLiteral(x.operator.toString()), x.arg)))
            }

            override fun endVisit(x: JsCatch, ctx: JsContext<JsNode>) {
                super.endVisit(x, ctx)

                val ident = "PhiBloodyException \$__phiException"
                val catchBody = JsBlock()
                catchBody.statements.add(JsNameRef("Phi::getCurrentEnv()->setVar('${x.parameter.name.ident}', \$__phiException->phiValue)").makeStmt()-{o->
                    (o as AbstractNode).statementNeedsFuckingNewline = false
                })
                catchBody.statements.addAll(x.body.statements)
                ctx.replaceMe(JsCatch(x.scope, ident, catchBody))
            }
        }.accept(program)
    }
}


