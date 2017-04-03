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

fun phpify(program: JsProgram) {
    PhizdetscGlobal.reportTranslationStage(0, program)
    phpify1(program)
    PhizdetscGlobal.reportTranslationStage(1, program)
    phpify2(program)
    PhizdetscGlobal.reportTranslationStage(2, program)
}

fun phpify1(program: JsProgram) {
    object : JsVisitorWithContextImpl() {
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
            return JsStringLiteral("@@${PhizdetscGlobal.debugTagPrefix}${nextDebugTag++}")
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
                    ?: new("PhiUnaryOperation", listOf(nextDebugTagLiteral(), JsStringLiteral("prefix"), JsStringLiteral("void"), new("PhiNumberLiteral", listOf(JsStringLiteral("@@something"), program.getNumberLiteral(0))))))
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
            ctx.replaceMe(new("PhiUnaryOperation", listOf(nextDebugTagLiteral(), JsStringLiteral("prefix"), JsStringLiteral(x.operator.toString()), x.arg)))
        }

        override fun endVisit(x: JsPostfixOperation, ctx: JsContext<JsNode>) {
            super.endVisit(x, ctx)
            ctx.replaceMe(new("PhiUnaryOperation", listOf(nextDebugTagLiteral(), JsStringLiteral("postfix"), JsStringLiteral(x.operator.toString()), x.arg)))
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

