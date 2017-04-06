// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.jetbrains.kotlin.js.backend

import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.JsNumberLiteral.JsDoubleLiteral
import org.jetbrains.kotlin.js.backend.ast.JsNumberLiteral.JsIntLiteral
import org.jetbrains.kotlin.js.backend.ast.JsVars.JsVar
import org.jetbrains.kotlin.js.util.TextOutput
import gnu.trove.THashSet

/**
 * Produces text output from a JavaScript AST.
 */
open class PhizdetsToStringGenerationVisitor(val out: TextOutput, val taggedGenOut: TextOutput) : JsVisitor() {

    protected var needSemi = true
    private var lineBreakAfterBlock = true

    /**
     * "Global" blocks are either the global block of a fragment, or a block
     * nested directly within some other global block. This definition matters
     * because the statements designated by statementEnds and statementStarts are
     * those that appear directly within these global blocks.
     */
    private val globalBlocks = THashSet<JsBlock>()

    val p = object : TextOutput {
        override fun getPosition(): Int = out.getPosition()
        override fun getLine(): Int = out.getLine()
        override fun getColumn(): Int = out.getColumn()
        override fun indentIn() {
            taggedGenOut.indentIn()
            out.indentIn()
        }
        override fun indentOut() {
            taggedGenOut.indentOut()
            out.indentOut()
        }
        override fun newline() {
            taggedGenOut.newline()
            out.newline()
        }
        override fun print(c: Char) {
            taggedGenOut.print(c)
            out.print(c)
        }
        override fun print(v: Int) {
            taggedGenOut.print(v)
            out.print(v)
        }
        override fun print(v: Double) {
            taggedGenOut.print(v)
            out.print(v)
        }
        override fun print(s: CharArray?) {
            taggedGenOut.print(s)
            out.print(s)
        }
        override fun print(s: CharSequence) {
            if (s.contains("typeof")) {
//                "break on me"
            }
            taggedGenOut.print(s)
            out.print(s)
        }
        override fun printOpt(c: Char) {
            taggedGenOut.printOpt(c)
            out.printOpt(c)
        }
        override fun printOpt(s: CharArray?) {
            taggedGenOut.printOpt(s)
            out.printOpt(s)
        }
        override fun printOpt(s: String?) {
            taggedGenOut.printOpt(s)
            out.printOpt(s)
        }
        override fun isCompact(): Boolean = out.isCompact()
        override fun isJustNewlined(): Boolean = out.isJustNewlined()
        override fun setOutListener(outListener: TextOutput.OutListener?) = out.setOutListener(outListener)
        override fun maybeIndent() {
            taggedGenOut.maybeIndent()
            out.maybeIndent()
        }
    }

    override fun visitArrayAccess(x: JsArrayAccess) {
        printPair(x, x.arrayExpression)
        leftSquare()
        accept(x.indexExpression)
        rightSquare()
    }

    override fun visitArray(x: JsArrayLiteral) {
        leftSquare()
        printExpressions(x.expressions)
        rightSquare()
    }

    private fun printExpressions(expressions: List<JsExpression>) {
        var notFirst = false
        for (expression in expressions) {
            notFirst = sepCommaOptSpace(notFirst) && expression !is JsDocComment
            val isEnclosed = parenPushIfCommaExpression(expression)
            accept(expression)
            if (isEnclosed) {
                rightParen()
            }
        }
    }

    override fun visitBinaryExpression(binaryOperation: JsBinaryOperation) {
        val operator = binaryOperation.operator
        val arg1 = binaryOperation.arg1
        val isExpressionEnclosed = parenPush(binaryOperation, arg1, !operator.isLeftAssociative)

        accept(arg1)
        if (operator.isKeyword) {
            _parenPopOrSpace(binaryOperation, arg1, !operator.isLeftAssociative)
        } else if (operator != JsBinaryOperator.COMMA) {
            if (isExpressionEnclosed) {
                rightParen()
            }
            spaceOpt()
        }

        p.print(operator.symbol)

        val arg2 = binaryOperation.arg2
        val isParenOpened: Boolean
        if (operator == JsBinaryOperator.COMMA) {
            isParenOpened = false
            spaceOpt()
        } else if (arg2 is JsBinaryOperation && arg2.operator == JsBinaryOperator.AND) {
            spaceOpt()
            leftParen()
            isParenOpened = true
        } else {
            if (spaceCalc(operator, arg2)) {
                isParenOpened = _parenPushOrSpace(binaryOperation, arg2, operator.isLeftAssociative)
            } else {
                spaceOpt()
                isParenOpened = parenPush(binaryOperation, arg2, operator.isLeftAssociative)
            }
        }
        accept(arg2)
        if (isParenOpened) {
            rightParen()
        }
    }

    override fun visitBlock(x: JsBlock) {
        printJsBlock(x, true)
    }

    override fun visitBoolean(x: JsLiteral.JsBooleanLiteral) {
        if (x.value) {
            p.print(CHARS_TRUE)
        } else {
            p.print(CHARS_FALSE)
        }
    }

    override fun visitBreak(x: JsBreak) {
        p.print(CHARS_BREAK)
        continueOrBreakLabel(x)
    }

    override fun visitContinue(x: JsContinue) {
        p.print(CHARS_CONTINUE)
        continueOrBreakLabel(x)
    }

    private fun continueOrBreakLabel(x: JsContinue) {
        val label = x.label
        if (label != null && label.ident != null) {
            space()
            p.print(label.ident)
        }
    }

    override fun visitCase(x: JsCase) {
        p.print(CHARS_CASE)
        space()
        accept(x.caseExpression)
        _colon()
        newlineOpt()

        printSwitchMemberStatements(x)
    }

    private fun printSwitchMemberStatements(x: JsSwitchMember) {
        p.indentIn()
        for (stmt in x.statements) {
            needSemi = true
            accept(stmt)
            if (needSemi) {
                semi()
            }
            newlineOpt()
        }
        p.indentOut()
        needSemi = false
    }

    override fun visitCatch(x: JsCatch) {
        spaceOpt()
        p.print(CHARS_CATCH)
        spaceOpt()
        leftParen()
        nameDef(x.parameter.name)

        // Optional catch condition.
        //
        val catchCond = x.condition
        if (catchCond != null) {
            space()
            _if()
            space()
            accept(catchCond)
        }

        rightParen()
        spaceOpt()
        accept(x.body)
    }

    override fun visitConditional(x: JsConditional) {
        // Associativity: for the then and else branches, it is safe to insert
        // another
        // ternary expression, but if the test expression is a ternary, it should
        // get parentheses around it.
        printPair(x, x.testExpression, true)
        spaceOpt()
        p.print('?')
        spaceOpt()
        printPair(x, x.thenExpression)
        spaceOpt()
        _colon()
        spaceOpt()
        printPair(x, x.elseExpression)
    }

    private fun printPair(parent: JsExpression, expression: JsExpression, wrongAssoc: Boolean = false) {
        val isNeedParen = parenCalc(parent, expression, wrongAssoc)
        if (isNeedParen) {
            leftParen()
        }
        accept(expression)
        if (isNeedParen) {
            rightParen()
        }
    }

    override fun visitDebugger(x: JsDebugger) {
        p.print(CHARS_DEBUGGER)
    }

    override fun visitDefault(x: JsDefault) {
        p.print(CHARS_DEFAULT)
        _colon()

        printSwitchMemberStatements(x)
    }

    override fun visitWhile(x: JsWhile) {
        _while()
        spaceOpt()
        leftParen()
        accept(x.condition)
        rightParen()
        nestedPush(x.body)
        accept(x.body)
        nestedPop(x.body)
    }

    override fun visitDoWhile(x: JsDoWhile) {
        p.print(CHARS_DO)
        nestedPush(x.body)
        accept(x.body)
        nestedPop(x.body)
        if (needSemi) {
            semi()
            newlineOpt()
        } else {
            spaceOpt()
            needSemi = true
        }
        _while()
        spaceOpt()
        leftParen()
        accept(x.condition)
        rightParen()
    }

    override fun visitEmpty(x: JsEmpty) {}

    override fun visitExpressionStatement(x: JsExpressionStatement) {
        val surroundWithParentheses = JsFirstExpressionVisitor.exec(x)
        if (surroundWithParentheses) {
            leftParen()
        }
        accept(x.expression)
        if (surroundWithParentheses) {
            rightParen()
        }
    }

    override fun visitFor(x: JsFor) {
        _for()
        spaceOpt()
        leftParen()

        // The init expressions or var decl.
        //
        if (x.initExpression != null) {
            accept(x.initExpression)
        } else if (x.initVars != null) {
            accept(x.initVars)
        }

        semi()

        // The loop test.
        //
        if (x.condition != null) {
            spaceOpt()
            accept(x.condition)
        }

        semi()

        // The incr expression.
        //
        if (x.incrementExpression != null) {
            spaceOpt()
            accept(x.incrementExpression)
        }

        rightParen()
        nestedPush(x.body)
        accept(x.body)
        nestedPop(x.body)
    }

    override fun visitForIn(x: JsForIn) {
        _for()
        spaceOpt()
        leftParen()

        if (x.iterVarName != null) {
            `var`()
            space()
            nameDef(x.iterVarName)

            if (x.iterExpression != null) {
                spaceOpt()
                assignment()
                spaceOpt()
                accept(x.iterExpression)
            }
        } else {
            // Just a name ref.
            //
            accept(x.iterExpression)
        }

        space()
        p.print(CHARS_IN)
        space()
        accept(x.objectExpression)

        rightParen()
        nestedPush(x.body)
        accept(x.body)
        nestedPop(x.body)
    }

    override fun visitFunction(x: JsFunction) {
        p.print(CHARS_FUNCTION)
        space()
        if (x.name != null) {
            nameOf(x)
        }

        leftParen()
        var notFirst = false
        for (element in x.parameters) {
            notFirst = sepCommaOptSpace(notFirst)
            accept(element)
        }
        rightParen()
        space()

        lineBreakAfterBlock = false
        accept(x.body)
        needSemi = true
    }

    override fun visitIf(x: JsIf) {
        _if()
        spaceOpt()
        leftParen()
        accept(x.ifExpression)
        rightParen()
        var thenStmt = x.thenStatement
        val elseStatement = x.elseStatement
        if (elseStatement != null && thenStmt is JsIf && thenStmt.elseStatement == null) {
            thenStmt = JsBlock(thenStmt)
        }
        nestedPush(thenStmt)
        accept(thenStmt)
        nestedPop(thenStmt)
        if (elseStatement != null) {
            if (needSemi) {
                semi()
                newlineOpt()
            } else {
                spaceOpt()
                needSemi = true
            }
            p.print(CHARS_ELSE)
            val elseIf = elseStatement is JsIf
            if (!elseIf) {
                nestedPush(elseStatement)
            } else {
                space()
            }
            accept(elseStatement)
            if (!elseIf) {
                nestedPop(elseStatement)
            }
        }
    }

    override fun visitInvocation(invocation: JsInvocation) {
        if (invocation.insertFuckingNewlineBeforeMe)
            p.print("\n")
        printDebugTag(invocation)
        printPair(invocation, invocation.qualifier)

        leftParen()
        printExpressions(invocation.arguments)
        rightParen()
        if (invocation.insertFuckingNewlineAfterMe)
            p.print("\n")
    }

    override fun visitLabel(x: JsLabel) {
        nameOf(x)
        _colon()
        spaceOpt()
        accept(x.statement)
    }

    override fun visitNameRef(nameRef: JsNameRef) {
        printDebugTag(nameRef)
        val qualifier = nameRef.qualifier
        if (qualifier != null) {
            val enclose: Boolean
            if (qualifier is JsLiteral.JsValueLiteral) {
                // "42.foo" is not allowed, but "(42).foo" is.
                enclose = qualifier is JsNumberLiteral
            } else {
                enclose = parenCalc(nameRef, qualifier, false)
            }

            if (enclose) {
                leftParen()
            }
            accept(qualifier)
            if (enclose) {
                rightParen()
            }
            p.print('.')
        }

        p.maybeIndent()
        beforeNodePrinted(nameRef)
        p.print(nameRef.ident)
    }

    private fun printDebugTag(node: AbstractNode) {
        taggedGenOut.print(node.debugTag + "@")
    }

    protected open fun beforeNodePrinted(node: JsNode) {}

    override fun visitNew(x: JsNew) {
        p.print(CHARS_NEW)
        space()

        val constructorExpression = x.constructorExpression
        val needsParens = JsConstructExpressionVisitor.exec(constructorExpression)
        if (needsParens) {
            leftParen()
        }
        accept(constructorExpression)
        if (needsParens) {
            rightParen()
        }

        leftParen()
        printExpressions(x.arguments)
        rightParen()
    }

    override fun visitNull(x: JsNullLiteral) {
        p.print(CHARS_NULL)
    }

    override fun visitInt(x: JsIntLiteral) {
        p.print(x.value)
    }

    override fun visitDouble(x: JsDoubleLiteral) {
        p.print(x.value)
    }

    override fun visitObjectLiteral(objectLiteral: JsObjectLiteral) {
        p.print('{')
        if (objectLiteral.isMultiline) {
            p.indentIn()
        }

        var notFirst = false
        for (item in objectLiteral.propertyInitializers) {
            if (notFirst) {
                p.print(',')
            }

            if (objectLiteral.isMultiline) {
                newlineOpt()
            } else if (notFirst) {
                spaceOpt()
            }

            notFirst = true

            val labelExpr = item.labelExpr
            // labels can be either string, integral, or decimal literals
            if (labelExpr is JsNameRef) {
                p.print(labelExpr.ident)
            } else if (labelExpr is JsStringLiteral) {
                p.print(labelExpr.value)
            } else {
                accept(labelExpr)
            }

            _colon()
            space()
            val valueExpr = item.valueExpr
            val wasEnclosed = parenPushIfCommaExpression(valueExpr)
            accept(valueExpr)
            if (wasEnclosed) {
                rightParen()
            }
        }

        if (objectLiteral.isMultiline) {
            p.indentOut()
            newlineOpt()
        }

        p.print('}')
    }

    override fun visitParameter(x: JsParameter) {
        nameOf(x)
    }

    override fun visitPostfixOperation(x: JsPostfixOperation) {
        val op = x.operator
        val arg = x.arg
        // unary operators always associate correctly (I think)
        printPair(x, arg)
        p.print(op.symbol)
    }

    override fun visitPrefixOperation(x: JsPrefixOperation) {
        val op = x.operator
        p.print(op.symbol)
        val arg = x.arg
        if (spaceCalc(op, arg)) {
            space()
        }
        // unary operators always associate correctly (I think)
        printPair(x, arg)
    }

    override fun visitProgram(x: JsProgram) {
        p.print("<JsProgram>")
    }

    override fun visitProgramFragment(x: JsProgramFragment) {
        p.print("<JsProgramFragment>")
    }

    override fun visitRegExp(x: JsRegExp) {
        slash()
        p.print(x.pattern)
        slash()
        val flags = x.flags
        if (flags != null) {
            p.print(flags)
        }
    }

    override fun visitReturn(x: JsReturn) {
        p.print(CHARS_RETURN)
        val expr = x.expression
        if (expr != null) {
            space()
            accept(expr)
        }
    }

    override fun visitString(x: JsStringLiteral) {
        p.print(javaScriptString(x.value))
    }

    override fun visit(x: JsSwitch) {
        p.print(CHARS_SWITCH)
        spaceOpt()
        leftParen()
        accept(x.expression)
        rightParen()
        spaceOpt()
        blockOpen()
        acceptList(x.cases)
        blockClose()
    }

    override fun visitThis(x: JsLiteral.JsThisRef) {
        p.print(CHARS_THIS)
    }

    override fun visitThrow(x: JsThrow) {
        p.print(CHARS_THROW)
        space()
        accept(x.expression)
    }

    override fun visitTry(x: JsTry) {
        p.print(CHARS_TRY)
        spaceOpt()
        accept(x.tryBlock)

        acceptList(x.catches)

        val finallyBlock = x.finallyBlock
        if (finallyBlock != null) {
            p.print(CHARS_FINALLY)
            spaceOpt()
            accept(finallyBlock)
        }
    }

    override fun visit(`var`: JsVar) {
        nameOf(`var`)
        val initExpr = `var`.initExpression
        if (initExpr != null) {
            spaceOpt()
            assignment()
            spaceOpt()
            val isEnclosed = parenPushIfCommaExpression(initExpr)
            accept(initExpr)
            if (isEnclosed) {
                rightParen()
            }
        }
    }

    override fun visitVars(vars: JsVars) {
        `var`()
        space()
        var sep = false
        for (`var` in vars) {
            if (sep) {
                if (vars.isMultiline) {
                    newlineOpt()
                }
                p.print(',')
                spaceOpt()
            } else {
                sep = true
            }

            accept(`var`)
        }
    }

    override fun visitDocComment(comment: JsDocComment) {
        val asSingleLine = comment.tags.size == 1
        if (!asSingleLine) {
            newlineOpt()
        }
        p.print("/**")
        if (asSingleLine) {
            space()
        } else {
            p.newline()
        }

        var notFirst = false
        for ((key, value) in comment.tags) {
            if (notFirst) {
                p.newline()
                p.print(' ')
                p.print('*')
            } else {
                notFirst = true
            }

            p.print('@')
            p.print(key)
            if (value != null) {
                space()
                if (value is CharSequence) {
                    p.print(value)
                } else {
                    visitNameRef(value as JsNameRef)
                }
            }

            if (!asSingleLine) {
                p.newline()
            }
        }

        if (asSingleLine) {
            space()
        } else {
            newlineOpt()
        }

        p.print('*')
        p.print('/')
        if (asSingleLine) {
            spaceOpt()
        }
    }

    protected fun newlineOpt() {
        if (!p.isCompact) {
            p.newline()
        }
    }

    protected fun printJsBlock(x: JsBlock, finalNewline: Boolean) {
        var finalNewline = finalNewline
        if (!lineBreakAfterBlock) {
            finalNewline = false
            lineBreakAfterBlock = true
        }

        val needBraces = !x.isGlobalBlock
        if (needBraces) {
            blockOpen()
        }

        val iterator = x.statements.iterator()
        while (iterator.hasNext()) {
            val isGlobal = x.isGlobalBlock || globalBlocks.contains(x)

            val statement = iterator.next()
            if (statement is JsEmpty) {
                continue
            }

            needSemi = true
            var stmtIsGlobalBlock = false
            if (isGlobal) {
                if (statement is JsBlock) {
                    // A block inside a global block is still considered global
                    stmtIsGlobalBlock = true
                    globalBlocks.add(statement)
                }
            }

            accept(statement)
            if (stmtIsGlobalBlock) {

                globalBlocks.remove(statement)
            }
            if (needSemi) {
                /*
                * Special treatment of function declarations: If they are the only item in a
                * statement (i.e. not part of an assignment operation), just give them
                * a newline instead of a semi.
                */
                val functionStmt = statement is JsExpressionStatement && statement.expression is JsFunction
                /*
                * Special treatment of the last statement in a block: only a few
                * statements at the end of a block require semicolons.
                */

                // @debug
//                if (statement is JsExpressionStatement) {
//                    val expr = statement.expression
//                    if (expr is JsNameRef) {
//                        if (expr.ident.contains("setVar")) {
//                            "break on me"
//                        }
//                    }
//                }

                val lastStatement = !iterator.hasNext() && needBraces && !JsRequiresSemiVisitor.exec(statement)
                if (functionStmt) {
                    if (lastStatement) {
                        newlineOpt()
                    } else {
                        p.newline()
                    }
                } else {
                    if (lastStatement) {
                        p.printOpt(';')
                    } else {
                        semi()
                    }
                    val needsFuckingNewline = when (statement) {
                        is AbstractNode -> statement.statementNeedsFuckingNewline
                        else -> false
                    }
                    if (needsFuckingNewline) {
                        newlineOpt()
                    }
                }
            }
        }

        if (needBraces) {
            // _blockClose() modified
            p.indentOut()
            p.print('}')
            if (finalNewline) {
                newlineOpt()
            }
        }
        needSemi = false
    }

    private fun assignment() {
        p.print('=')
    }

    private fun blockClose() {
        p.indentOut()
        p.print('}')
        newlineOpt()
    }

    private fun blockOpen() {
        p.print('{')
        p.indentIn()
        newlineOpt()
    }

    private fun _colon() {
        p.print(':')
    }

    private fun _for() {
        p.print(CHARS_FOR)
    }

    private fun _if() {
        p.print(CHARS_IF)
    }

    private fun leftParen() {
        p.print('(')
    }

    private fun leftSquare() {
        p.print('[')
    }

    private fun nameDef(name: JsName) {
        p.print(name.ident)
    }

    private fun nameOf(hasName: HasName) {
        nameDef(hasName.name)
    }

    private fun nestedPop(statement: JsStatement): Boolean {
        val pop = statement !is JsBlock
        if (pop) {
            p.indentOut()
        }
        return pop
    }

    private fun nestedPush(statement: JsStatement): Boolean {
        val push = statement !is JsBlock
        if (push) {
            newlineOpt()
            p.indentIn()
        } else {
            spaceOpt()
        }
        return push
    }

    private fun _parenPopOrSpace(parent: JsExpression, child: JsExpression, wrongAssoc: Boolean): Boolean {
        val doPop = parenCalc(parent, child, wrongAssoc)
        if (doPop) {
            rightParen()
        } else {
            space()
        }
        return doPop
    }

    private fun parenPush(parent: JsExpression, child: JsExpression, wrongAssoc: Boolean): Boolean {
        val doPush = parenCalc(parent, child, wrongAssoc)
        if (doPush) {
            leftParen()
        }
        return doPush
    }

    private fun parenPushIfCommaExpression(x: JsExpression): Boolean {
        val doPush = x is JsBinaryOperation && x.operator == JsBinaryOperator.COMMA
        if (doPush) {
            leftParen()
        }
        return doPush
    }

    private fun _parenPushOrSpace(parent: JsExpression, child: JsExpression, wrongAssoc: Boolean): Boolean {
        val doPush = parenCalc(parent, child, wrongAssoc)
        if (doPush) {
            leftParen()
        } else {
            space()
        }
        return doPush
    }

    private fun rightParen() {
        p.print(')')
    }

    private fun rightSquare() {
        p.print(']')
    }

    private fun semi() {
        p.print(';')
    }

    private fun sepCommaOptSpace(sep: Boolean): Boolean {
        if (sep) {
            p.print(',')
            spaceOpt()
        }
        return true
    }

    private fun slash() {
        p.print('/')
    }

    private fun space() {
        p.print(' ')
    }

    private fun spaceOpt() {
        p.printOpt(' ')
    }

    private fun `var`() {
        p.print(CHARS_VAR)
    }

    private fun _while() {
        p.print(CHARS_WHILE)
    }

    companion object {
        private val CHARS_BREAK = "break".toCharArray()
        private val CHARS_CASE = "case".toCharArray()
        private val CHARS_CATCH = "catch".toCharArray()
        private val CHARS_CONTINUE = "continue".toCharArray()
        private val CHARS_DEBUGGER = "debugger".toCharArray()
        private val CHARS_DEFAULT = "default".toCharArray()
        private val CHARS_DO = "do".toCharArray()
        private val CHARS_ELSE = "else".toCharArray()
        private val CHARS_FALSE = "false".toCharArray()
        private val CHARS_FINALLY = "finally".toCharArray()
        private val CHARS_FOR = "for".toCharArray()
        private val CHARS_FUNCTION = "function".toCharArray()
        private val CHARS_IF = "if".toCharArray()
        private val CHARS_IN = "in".toCharArray()
        private val CHARS_NEW = "new".toCharArray()
        private val CHARS_NULL = "null".toCharArray()
        private val CHARS_RETURN = "return".toCharArray()
        private val CHARS_SWITCH = "switch".toCharArray()
        private val CHARS_THIS = "this".toCharArray()
        private val CHARS_THROW = "throw".toCharArray()
        private val CHARS_TRUE = "true".toCharArray()
        private val CHARS_TRY = "try".toCharArray()
        private val CHARS_VAR = "var".toCharArray()
        private val CHARS_WHILE = "while".toCharArray()
        private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

        fun javaScriptString(value: String): CharSequence {
            return javaScriptString(value, true).toString().replace("$", "\\$")
//            return javaScriptString(value, false)
        }

        /**
         * Generate JavaScript code that evaluates to the supplied string. Adapted
         * from [org.mozilla.javascript.ScriptRuntime.escapeString]
         * . The difference is that we quote with either &quot; or &apos; depending on
         * which one is used less inside the string.
         */
        fun javaScriptString(chars: CharSequence, forceDoubleQuote: Boolean): CharSequence {
            val n = chars.length
            var quoteCount = 0
            var aposCount = 0

            for (i in 0..n - 1) {
                when (chars[i]) {
                    '"' -> ++quoteCount
                    '\'' -> ++aposCount
                }
            }

            val result = StringBuilder(n + 16)

            val quoteChar = if (quoteCount < aposCount || forceDoubleQuote) '"' else '\''
            result.append(quoteChar)

            for (i in 0..n - 1) {
                val c = chars[i]

                if (' ' <= c && c <= '~' && c != quoteChar && c != '\\') {
                    // an ordinary print character (like C isprint())
                    result.append(c)
                    continue
                }

                var escape = -1
                when (c) {
                    '\b' -> escape = 'b'.toInt()
//                    '\f' -> escape = 'f'.toInt()
                    '\n' -> escape = 'n'.toInt()
                    '\r' -> escape = 'r'.toInt()
                    '\t' -> escape = 't'.toInt()
                    '"' -> escape = '"'.toInt()
                    '\'' -> escape = '\''.toInt()
                    '\\' -> escape = '\\'.toInt()
                }// only reach here if == quoteChar
                // only reach here if == quoteChar

                if (escape >= 0) {
                    // an \escaped sort of character
                    result.append('\\')
                    result.append(escape.toChar())
                } else {
                    val hexSize: Int
                    if (c.toInt() < 256) {
                        // 2-digit hex
                        result.append("\\x")
                        hexSize = 2
                    } else {
                        // Unicode.
                        result.append("\\u")
                        hexSize = 4
                    }
                    // append hexadecimal form of ch left-padded with 0
                    var shift = (hexSize - 1) * 4
                    while (shift >= 0) {
                        val digit = 0xf and (c.toInt() shr shift)
                        result.append(HEX_DIGITS[digit])
                        shift -= 4
                    }
                }
            }
            result.append(quoteChar)
            escapeClosingTags(result)
            return result
        }

        /**
         * Escapes any closing XML tags embedded in `str`, which could
         * potentially cause a parse failure in a browser, for example, embedding a
         * closing `<script>` tag.

         * @param str an unescaped literal; May be null
         */
        private fun escapeClosingTags(str: StringBuilder?) {
            if (str == null) {
                return
            }

            var index = 0
            while (true) {
                index = str.indexOf("</", index)
                if (index == -1) break
                str.insert(index + 1, '\\')
            }
        }

        private fun parenCalc(parent: JsExpression, child: JsExpression, wrongAssoc: Boolean): Boolean {
            val parentPrec = JsPrecedenceVisitor.exec(parent)
            val childPrec = JsPrecedenceVisitor.exec(child)
            return parentPrec > childPrec || parentPrec == childPrec && wrongAssoc
        }

        /**
         * Decide whether, if `op` is printed followed by `arg`,
         * there needs to be a space between the operator and expression.

         * @return `true` if a space needs to be printed
         */
        private fun spaceCalc(op: JsOperator, arg: JsExpression): Boolean {
            if (op.isKeyword) {
                return true
            }
            if (arg is JsBinaryOperation) {
                val binary = arg
                /*
            * If the binary operation has a higher precedence than op, then it won't
            * be parenthesized, so check the first argument of the binary operation.
            */
                return binary.operator.precedence > op.precedence && spaceCalc(op, binary.arg1)
            }
            if (arg is JsPrefixOperation) {
                val op2 = arg.operator
                return (op === JsBinaryOperator.SUB || op === JsUnaryOperator.NEG) && (op2 === JsUnaryOperator.DEC || op2 === JsUnaryOperator.NEG) || op === JsBinaryOperator.ADD && op2 === JsUnaryOperator.INC
            }
            if (arg is JsNumberLiteral && (op === JsBinaryOperator.SUB || op === JsUnaryOperator.NEG)) {
                if (arg is JsIntLiteral) {
                    return arg.value < 0
                } else {
                    assert(arg is JsDoubleLiteral)

                    return (arg as JsDoubleLiteral).value < 0
                }
            }
            return false
        }
    }
}
