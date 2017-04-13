package photlinc

import org.jetbrains.kotlin.js.backend.ast.JsVisitor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.JsNumberLiteral.JsDoubleLiteral
import org.jetbrains.kotlin.js.backend.ast.JsNumberLiteral.JsIntLiteral
import org.jetbrains.kotlin.js.backend.ast.JsVars.JsVar
import org.jetbrains.kotlin.js.util.TextOutput
import gnu.trove.THashSet
import org.jetbrains.kotlin.backend.jvm.codegen.psiElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.js.backend.JsConstructExpressionVisitor
import org.jetbrains.kotlin.js.backend.JsFirstExpressionVisitor
import org.jetbrains.kotlin.js.backend.JsPrecedenceVisitor
import org.jetbrains.kotlin.js.backend.JsRequiresSemiVisitor
import org.jetbrains.kotlin.js.translate.utils.name
import org.jetbrains.kotlin.js.util.TextOutputImpl
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.constants.BooleanValue
import org.jetbrains.kotlin.resolve.constants.StringValue
import java.util.*
import photlin.*
import vgrechka.*

open class JsToStringGenerationVisitor2(out:TextOutput): JsVisitor() {
    protected var needSemi = true
    private var lineBreakAfterBlock = true
    /**
     * "Global" blocks are either the global block of a fragment, or a block
     * nested directly within some other global block. This definition matters
     * because the statements designated by statementEnds and statementStarts are
     * those that appear directly within these global blocks.
     */
    private val globalBlocks = THashSet<JsBlock>()
    val p:TextOutput
    init{
        p = out
    }


    override fun visitSingleLineComment(x: JsSingleLineComment) {
        p.print("// ${x.text}\n")
    }

    override fun visitPHPPlainCodeExpression(x: PHPPlainCodeExpression) {
        p.print(x.spewCode())
    }

    override fun visitArrayAccess(x:JsArrayAccess) {
        printPair(x, x.getArrayExpression())
        leftSquare()
        accept(x.getIndexExpression())
        rightSquare()
    }

    override fun visitArray(x: JsArrayLiteral) {
        printDebugTag(x)

        fun fuck(gen: JsToStringGenerationVisitor2) {
            gen.p.print("array(")
            gen.printExpressions(x.getExpressions())
            gen.p.print(")")
        }

        fuck(this)
    }

    private fun printExpressions(expressions:List<JsExpression>) {
        var notFirst = false
        for ((expressionIndex, expression) in expressions.withIndex())
        {
            notFirst = sepCommaOptSpace(notFirst) && !(expression is JsDocComment)
            val isEnclosed = parenPushIfCommaExpression(expression)

//            if (expression.debugTag == "1646") {
//                "break on me"
//            }
            if (expression is JsArrayLiteral || expression.valueParameterDescriptor?.type?.typeName == "kotlin.Array") {
                pizdavalue(byReference = expression !is JsArrayLiteral) {gen-> gen.accept(expression)}
            } else {
                accept(expression)
            }

            if (isEnclosed)
            {
                rightParen()
            }
        }
    }

    private fun pizdavalue(byReference: Boolean = false, fuck: (JsToStringGenerationVisitor2) -> Unit) {
        val tag = PhotlincDebugGlobal.nextDebugTag()
        if (tag == PhotlincDebugGlobal.breakOnPizdavalue) {
            "break on me"
        }
        val varName = "pizdavalue" + tag
        val out = TextOutputImpl()
        val generator = JsToStringGenerationVisitor2(out)
        fuck(generator)
        val eq = when {
            byReference -> "=&"
            else -> "="
        }
        printLineAbove("\$$varName $eq ${out.out};")
        p.print("\$$varName")
    }


    override fun visitBinaryExpression(binaryOperation: JsBinaryOperation) {
        val operator = binaryOperation.getOperator()
        val arg1 = binaryOperation.getArg1()
        val isExpressionEnclosed = parenPush(binaryOperation, arg1, !operator.isLeftAssociative())
        accept(arg1)
        if (operator.isKeyword())
        {
            _parenPopOrSpace(binaryOperation, arg1, !operator.isLeftAssociative())
        }
        else if (operator !== JsBinaryOperator.COMMA)
        {
            if (isExpressionEnclosed)
            {
                rightParen()
            }
            spaceOpt()
        }
        printDebugTag(binaryOperation)

        if (binaryOperation.elvis) {
            p.print("?:")
        } else {
            p.print(run {
                val type = arg1.kotlinTypeName
                if (type == "kotlin.String") {
                    if (operator.symbol == "+") return@run "."
                    if (operator.symbol == "+=") return@run ".="
                }
                return@run operator.symbol
            })
        }

//        if (operator.symbol == "=") {
//            if (binaryOperation.arg2.kotlinTypeName == "kotlin.Array") {
//                p.print("&")
//            }
//        }

        val arg2 = binaryOperation.getArg2()
        val isParenOpened:Boolean
        if (operator === JsBinaryOperator.COMMA)
        {
            isParenOpened = false
            spaceOpt()
        }
        else if (arg2 is JsBinaryOperation && (arg2 as JsBinaryOperation).getOperator() === JsBinaryOperator.AND)
        {
            spaceOpt()
            leftParen()
            isParenOpened = true
        }
        else
        {
            if (spaceCalc(operator, arg2))
            {
                isParenOpened = _parenPushOrSpace(binaryOperation, arg2, operator.isLeftAssociative())
            }
            else
            {
                spaceOpt()
                isParenOpened = parenPush(binaryOperation, arg2, operator.isLeftAssociative())
            }
        }
        accept(arg2)
        if (isParenOpened)
        {
            rightParen()
        }
    }

    override fun visitBlock(x:JsBlock) {
        printJsBlock(x, true)
    }

    override fun visitPHPClass(x: PHPClass) {
        printAnnotations(x.classDescriptor, x)
        val fuckingName = escapeIdent(x.className.ident)
        printDebugTag(x)
        p.print("class $fuckingName ")
        x.classDescriptor.phpSuperPrototypeNameRef?.let {
            val superClassName = (it.qualifier as JsNameRef).ident
            p.print("extends ${escapeIdent(superClassName)} ")
        }
        blockOpen()
        x.statements.forEach {accept(it); newlineOpt()}
        blockClose()
        p.newline()
        p.print("\$$fuckingName = new stdClassWithPhotlinProps(); // Fuck...")
        p.newline()
    }

    private fun printAnnotations(x: Annotated, node: JsNode) {
        val annotations = x.annotations
        if (!annotations.isEmpty()) {
            p.print("/**")
            for (ann in annotations) {
                val annAnnotations = ann.type.constructor.declarationDescriptor?.annotations
                if (annAnnotations != null) {
                    val emitAnnotationAsPHPCommentAnnotation = annAnnotations.find {
                        it.type.typeName == "photlin.EmitAnnotationAsPHPComment"
                    }
                    if (emitAnnotationAsPHPCommentAnnotation != null) {
                        val annotationTypeName = ann.type.typeName ?: wtf("0afd9e3c-b8f5-4cef-8d82-45de409b8e6a")
                        val nameArgValue = emitAnnotationAsPHPCommentAnnotation.argumentValue("name") as String
                        val phpName = when (nameArgValue) {
                            "" -> annotationTypeName.substringAfterLast(".")
                            else -> nameArgValue
                        }
                        p.print(" @" + phpName)
                        if (ann.allValueArguments.isNotEmpty()) {
                            p.print("(")
                            for ((key, value) in ann.allValueArguments) {
                                p.print(key.name.asString())
                                p.print("=")
                                when (value) {
                                    is StringValue -> {
                                        p.print(phpString(value.value, forceDoubleQuote = true))
                                    }
                                    is BooleanValue -> {
                                        p.print(value.value.toString())
                                    }
                                    else -> imf("7a2b3251-0044-4b96-a9c4-5ca3cbabe207")
                                }
                            }
                            p.print(")")
                        }
                    }
                }
            }
            p.print(" **/ ")
        }
    }

    override fun visitBoolean(x:JsLiteral.JsBooleanLiteral) {
        if (x.getValue())
        {
            p.print(CHARS_TRUE)
        }
        else
        {
            p.print(CHARS_FALSE)
        }
    }
    override fun visitBreak(x:JsBreak) {
        p.print(CHARS_BREAK)
        continueOrBreakLabel(x)
    }
    override fun visitContinue(x:JsContinue) {
        p.print(CHARS_CONTINUE)
        continueOrBreakLabel(x)
    }
    private fun continueOrBreakLabel(x:JsContinue) {
        val label = x.getLabel()
        if (label != null && label.getIdent() != null)
        {
            space()
            p.print("1") // XXX PHP...
            // p.print(escapeIdent(label.getIdent()))
        }
    }
    override fun visitCase(x:JsCase) {
        p.print(CHARS_CASE)
        space()
        accept(x.getCaseExpression())
        _colon()
        newlineOpt()
        printSwitchMemberStatements(x)
    }
    private fun printSwitchMemberStatements(x:JsSwitchMember) {
        p.indentIn()
        for (stmt in x.getStatements())
        {
            needSemi = true
            accept(stmt)
            if (needSemi)
            {
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

//        val catchParam = ShitToShit.attrs(x).catchParam
//        if (catchParam != null) {
//            p.print(catchParam.typeReference!!.text + " $")
//        }

        accept(x.typeExpression)
        p.print(" $")
        nameDef(x.parameter.name)


        // Optional catch condition.
        //
        val catchCond = x.condition
        if (catchCond != null)
        {
            space()
            _if()
            space()
            accept(catchCond)
        }
        rightParen()
        spaceOpt()
        accept(x.body)
    }

    override fun visitConditional(x:JsConditional) {
        // Associativity: for the then and else branches, it is safe to insert
        // another
        // ternary expression, but if the test expression is a ternary, it should
        // get parentheses around it.
        printPair(x, x.getTestExpression(), true)
        spaceOpt()
        p.print('?')
        spaceOpt()
        printPair(x, x.getThenExpression())
        spaceOpt()
        _colon()
        spaceOpt()
        printPair(x, x.getElseExpression())
    }
    private fun printPair(parent:JsExpression, expression:JsExpression, wrongAssoc:Boolean = false) {
        val isNeedParen = parenCalc(parent, expression, wrongAssoc)
        if (isNeedParen)
        {
            leftParen()
        }
        accept(expression)
        if (isNeedParen)
        {
            rightParen()
        }
    }
    override fun visitDebugger(x:JsDebugger) {
        p.print(CHARS_DEBUGGER)
    }
    override fun visitDefault(x:JsDefault) {
        p.print(CHARS_DEFAULT)
        _colon()
        printSwitchMemberStatements(x)
    }
    override fun visitWhile(x:JsWhile) {
        _while()
        spaceOpt()
        leftParen()
        accept(x.getCondition())
        rightParen()
        nestedPush(x.getBody())
        accept(x.getBody())
        nestedPop(x.getBody())
    }
    override fun visitDoWhile(x:JsDoWhile) {
        p.print(CHARS_DO)
        nestedPush(x.getBody())
        accept(x.getBody())
        nestedPop(x.getBody())
        if (needSemi)
        {
            semi()
            newlineOpt()
        }
        else
        {
            spaceOpt()
            needSemi = true
        }
        _while()
        spaceOpt()
        leftParen()
        accept(x.getCondition())
        rightParen()
    }

    override fun visitEmpty(x:JsEmpty) {}

    override fun visitExpressionStatement(x:JsExpressionStatement) {
        val surroundWithParentheses = JsFirstExpressionVisitor.exec(x)
        if (surroundWithParentheses)
        {
            leftParen()
        }

        val expr = x.expression
        if (expr is JsFunction) {
            expr.shouldPrintName = true
        }
        accept(expr)
        if (surroundWithParentheses)
        {
            rightParen()
        }
    }
    override fun visitFor(x: JsFor) {
        _for()
        spaceOpt()
        leftParen()
        // The init expressions or var decl.
        //
        if (x.getInitExpression() != null)
        {
            accept(x.getInitExpression())
        }
        else if (x.getInitVars() != null)
        {
            accept(x.getInitVars())
        }
        semi()
        // The loop test.
        //
        if (x.getCondition() != null)
        {
            spaceOpt()
            accept(x.getCondition())
        }
        semi()
        // The incr expression.
        //
        if (x.getIncrementExpression() != null)
        {
            spaceOpt()
            accept(x.getIncrementExpression())
        }
        rightParen()
        nestedPush(x.getBody())
        accept(x.getBody())
        nestedPop(x.getBody())
    }

    override fun visitForIn(x:JsForIn) {
        wtf("visitForIn    7cd8a5a3-136d-4a4a-bfd4-78502e2dd11c")
//        _for()
//        spaceOpt()
//        leftParen()
//        if (x.getIterVarName() != null)
//        {
//            printVar()
//            space()
//            nameDef(x.getIterVarName())
//            if (x.getIterExpression() != null)
//            {
//                spaceOpt()
//                assignment()
//                spaceOpt()
//                accept(x.getIterExpression())
//            }
//        }
//        else
//        {
//            // Just a name ref.
//            //
//            accept(x.getIterExpression())
//        }
//        space()
//        p.print(CHARS_IN)
//        space()
//        accept(x.getObjectExpression())
//        rightParen()
//        nestedPush(x.getBody())
//        accept(x.getBody())
//        nestedPop(x.getBody())
    }

    override fun visitFunction(x: JsFunction) {
        val shouldDumpBodyToContainer = x.declarationDescriptor?.annotations
            ?.any {it.type.typeName == "photlin.PHPDumpBodyToContainer"}
            ?: false

        if (!shouldDumpBodyToContainer) {
            printDebugTag(x)
            p.print(CHARS_FUNCTION)

            if (x.declarationDescriptor != null) {
                val functionDescriptor = x.declarationDescriptor as? FunctionDescriptor
                    ?: wtf("19b83dc5-1876-4a36-a82a-7feb392655d8")
                if (functionDescriptor.returnType?.typeName == "kotlin.Array") {
                    p.print("&")
                }
            }

            space()

            if (x.getName() != null && x.shouldPrintName)
            {
                nameOf(x)
            }
            leftParen()
            var notFirst = false
            for (element in x.getParameters())
            {
                val param = element as JsParameter
                notFirst = sepCommaOptSpace(notFirst)
                accept(param)
            }
            rightParen()
            space()

            if (x.useNames.isNotEmpty()) {
                p.print("use (")
                for ((i, nameRef) in x.useNames.withIndex()) {
                    if (i > 0) p.print(", ")
                    if (isByRefShit(nameRef)) p.print("&")
                    p.print("$" + escapeIdent(nameRef.ident))
                }
                p.print(")")
            }

            lineBreakAfterBlock = false
        }

        accept(x.getBody())
        needSemi = true
    }

    override fun visitIf(x:JsIf) {
        _if()
        spaceOpt()
        leftParen()
        accept(x.getIfExpression())
        rightParen()

        var thenStmt = x.getThenStatement()
        var elseStatement = x.getElseStatement()

        // @fucking if-then-else printing
        thenStmt = JsBlock(thenStmt)
        elseStatement = JsBlock(elseStatement)

        if (elseStatement != null && thenStmt is JsIf && (thenStmt as JsIf).getElseStatement() == null)
        {
            thenStmt = JsBlock(thenStmt)
        }
        nestedPush(thenStmt)
        accept(thenStmt)
        nestedPop(thenStmt)
        if (elseStatement != null)
        {
            if (needSemi)
            {
                semi()
                newlineOpt()
            }
            else
            {
                spaceOpt()
                needSemi = true
            }
            p.print(CHARS_ELSE)
            val elseIf = elseStatement is JsIf
            if (!elseIf)
            {
                nestedPush(elseStatement)
            }
            else
            {
                space()
            }
            accept(elseStatement)
            if (!elseIf)
            {
                nestedPop(elseStatement)
            }
        }
    }

//    override fun visitPHPVarRef(varRef: PHPVarRef) {
//        printDebugTag(varRef)
//        p.print("$" + escapeDollar(varRef.getIdent()))
//    }

    override fun visitPHPGlobalVarRef(varRef: PHPGlobalVarRef) {
        printDebugTag(varRef)
        p.print("\$GLOBALS['" + escapeIdent(varRef.getIdent()) + "']")
    }

//    override fun visitPHPFieldRef(x: PHPFieldRef) {
//        accept(x.receiver)
//        p.print("->${x.fieldName}")
//    }

    override fun visitPHPMethodCall(call: PHPMethodCall) {
        accept(call.receiver)
        printDebugTag(call, spaceBefore = true)
        p.print("->${call.methodName}")
        leftParen()
        printExpressions(call.arguments)
        rightParen()
    }

    override fun visitPHPStaticMethodCall(call: PHPStaticMethodCall) {
        p.print("${call.className}::${call.methodName}")
        leftParen()
        printExpressions(call.arguments)
        rightParen()
    }

//    override fun visitPHPInvocation(invocation: PHPInvocation) {
////        val c = invocation.callee
//        accept(invocation.callee)
//
//        leftParen()
//        printExpressions(invocation.arguments)
//        rightParen()
//    }

    override fun visitInvocation(invocation: JsInvocation) {
        printDebugTag(invocation)

        val callee = invocation.qualifier
        if (callee is JsNameRef) {
            if (callee.qualifier?.kotlinTypeName == "kotlin.String") {
                "break on me"
                val builtInFunction = when (callee.ident) {
                    "charCodeAt" -> "Kotlin::charCodeAt"
                    "substring" -> "Kotlin::substring"
                    "split" -> "pizdasplit"
                    else -> wtf("d140179a-26b4-4726-bd6d-be69c62f42ed    callee.ident = ${callee.ident}")
//                    else -> "pizdunishka"
                }
                p.print("$builtInFunction(")
                accept(callee.qualifier)
                p.print(", ")
                printExpressions(invocation.arguments)
                p.print(")")
                return
            }

            accept(callee)
//            printPair(invocation, callee)
        } else {
            pizdavalue {gen-> gen.accept(callee)}
        }

        leftParen()
        printExpressions(invocation.arguments)
        rightParen()
    }

    override fun visitLabel(x:JsLabel) {
        nameOf(x)
        _colon()
        spaceOpt()
        accept(x.getStatement())
    }

    override fun visitNameRef(nameRef: JsNameRef) {
        printDebugTag(nameRef)

        if (nameRef.suppressWarnings)
            p.print("@")

        exhaustive=when (nameRef.kind) {
            PHPNameRefKind.STRING_LITERAL -> {
                if (nameRef.qualifier != null) {
                    wtf("STRING_LITERAL with qualifier    210ac0a8-eaf1-44fe-933f-4e78a7cf07a6")
                }
                p.print("'" + escapeIdent(nameRef.ident) + "'")
                return
            }

            PHPNameRefKind.GLOBAL_VAR -> {
                if (nameRef.qualifier != null) {
                    wtf("GLOBALVAR with qualifier    1325766c-29a4-44f9-b773-428a4f097e84")
                }
                p.print("\$GLOBALS['" + escapeIdent(nameRef.getIdent()) + "']")
            }

            PHPNameRefKind.VAR -> {
                if (nameRef.qualifier != null) {
                    wtf("VAR with qualifier    562283b3-071e-4fac-bcae-6be7f2fbc1ba")
                }
                p.print("$" + escapeIdent(nameRef.ident))
                return
            }

            PHPNameRefKind.FIELD -> {
                val qualifier = nameRef.qualifier
                if (qualifier != null) {
                    if (nameRef.qualifier?.kotlinTypeName == "kotlin.String") {
                        val builtInFunction = when (nameRef.ident) {
                            "length" -> "strlen"
                            else -> wtf("39e634a3-a9c7-4cef-bc57-01bd2e4ae195    nameRef.ident = ${nameRef.ident}")
                        }
                        p.print("$builtInFunction(")
                        accept(nameRef.qualifier)
                        p.print(")")
                        return
                    }
                }

                accept(nameRef.qualifier)
                p.print("->${escapeIdent(nameRef.ident)}")
                return
            }

            PHPNameRefKind.STATIC -> {
                accept(nameRef.qualifier)
                p.print("::${escapeIdent(nameRef.ident)}")
                return
            }

            PHPNameRefKind.LAMBDA,
            PHPNameRefKind.LAMBDA_CREATOR -> {
                val varName = "pizda_" + PhotlincDebugGlobal.nextDebugTag()
//            if (varName == "pizda_3485") {
//                "break on me"
//            }

                val crappyPHPLambda = StringBuilder()-{s->
                    s.append("function(")
                    val callableDescriptor = nameRef.callableDescriptor ?: wtf("ddb4e1a5-457a-4d51-b5d5-b999247e0b07")
                    val paramDescriptors = when (nameRef.kind) {
                        PHPNameRefKind.LAMBDA_CREATOR -> nameRef.additionalArgDescriptors ?: listOf()
                        else -> callableDescriptor.valueParameters + (nameRef.additionalArgDescriptors ?: listOf())
                    }
                    for ((i, valueParameter) in paramDescriptors.withIndex()) {
                        if (i > 0)
                            s.append(", ")
                        if (valueParameter is ValueDescriptor) {
                            if (valueParameter.type.typeName == "kotlin.Array")
                                s.append("&")
                        }
                        s.append("\$p$i")
                    }
                    s.append(") {")
                    s.append("return ${escapeIdent(nameRef.ident)}(")
                    for (i in 0..paramDescriptors.lastIndex) {
                        if (i > 0)
                            s.append(", ")
                        s.append("\$p$i")
                    }
                    s.append(");")
                    s.append("}")
                }

                printLineAbove("\$$varName = $crappyPHPLambda;")

                p.print("\$$varName")
                return
            }

            PHPNameRefKind.DUNNO -> {
                val qualifier = nameRef.qualifier
                if (qualifier != null)
                {
                    if (nameRef.qualifier?.kotlinTypeName == "kotlin.String") {
                        val builtInFunction = when (nameRef.ident) {
                            "length" -> "strlen"
                            else -> wtf("3c1a8c8c-4fe0-4d8b-bd4f-2bcab38a84f5    nameRef.ident = ${nameRef.ident}")
                        }
                        p.print("$builtInFunction(")
                        accept(nameRef.qualifier)
                        p.print(")")
                        return
                    }

                    var enclose:Boolean
                    if (qualifier is JsLiteral.JsValueLiteral)
                    {
                        // "42.foo" is not allowed, but "(42).foo" is.
                        enclose = qualifier is JsNumberLiteral
                    }
                    else
                    {
                        enclose = parenCalc(nameRef, qualifier, false)
                    }

                    if (qualifier !is JsNameRef)
                        enclose = false

                    if (enclose)
                    {
                        leftParen()
                    }

                    if (qualifier !is JsNameRef) {
//                        "break on me"
                        pizdavalue {gen-> gen.accept(qualifier)}
                    } else {
                        accept(qualifier)
                    }

                    if (enclose)
                    {
                        rightParen()
                    }
                    p.print("->")
                }
                p.maybeIndent()
                beforeNodePrinted(nameRef)

                p.print(escapeIdent(nameRef.ident))
            }
        }
    }

    private fun printLineAbove(s: String) {
        try {
            val out = (p as TextOutputImpl).out
            var index = out.length - 1
            while (out[index] != '\n' && index > 0) {
                --index
            }
            if (out[index] == '\r' && index > 0)
                --index

            out.insert(index, "\n$s")
        } catch(e: StringIndexOutOfBoundsException) {
            "break on me"
            throw e
        }
    }

    private fun printDebugTag(shit: AbstractNode, spaceBefore: Boolean = false) {
        val debugTag = shit.debugTag
        if (debugTag != null) {
            if (debugTag == PhotlincDebugGlobal.breakOnDebugTag) {
                "break on me"
            }

//            val mappedFrom = shit.mappedFromDebugTag?.let {"<$it>"} ?: ""
            val mappedFrom = ""
            p.printTagged(spaceBefore.ifOrEmpty{" "} + debugTag + mappedFrom + "@")
        }
    }

    fun escapeIdent(s: String): String {
        // PHP reserved words and alike
        if (s == "die") return "die__photlin_umri_skotina"
        if (s == "const") return "const__photlin_konstantinovich"
        if (s == "split") return "split__photlin_v_zhopu_zalit"

        return s.replace("$", "__dollar__")
    }


    protected fun beforeNodePrinted(node:JsNode) {}

    override fun visitNew(x: JsNew) {
        printDebugTag(x)
        p.print(CHARS_NEW)
        space()
        val constructorExpression = x.getConstructorExpression()
        val needsParens = JsConstructExpressionVisitor.exec(constructorExpression)
        if (needsParens)
        {
            leftParen()
        }
        accept(constructorExpression)
        if (needsParens)
        {
            rightParen()
        }
        leftParen()
        printExpressions(x.getArguments())
        rightParen()
    }
    override fun visitNull(x:JsNullLiteral) {
        printDebugTag(x)
        p.print(CHARS_NULL)
    }
    override fun visitInt(x:JsIntLiteral) {
        p.print(x.value)
    }
    override fun visitDouble(x:JsDoubleLiteral) {
        p.print(x.value)
    }

    override fun visitObjectLiteral(objectLiteral: JsObjectLiteral) {
        printDebugTag(objectLiteral)
        if (objectLiteral.propertyInitializers.isEmpty()) {
            p.print("new stdClassWithPhotlinProps()")
        } else {
//            imf("Generate non-empty object literal    653f3b11-ea9c-4f14-9b41-7d17a44c2d0d")
            p.print("array(")
            if (objectLiteral.isMultiline)
            {
                p.indentIn()
            }
            var notFirst = false
            for (item in objectLiteral.propertyInitializers)
            {
                if (notFirst)
                {
                    p.print(',')
                }
                if (objectLiteral.isMultiline)
                {
                    newlineOpt()
                }
                else if (notFirst)
                {
                    spaceOpt()
                }
                notFirst = true
                val labelExpr = item.labelExpr
                // labels can be either string, integral, or decimal literals
                p.print("'")
                if (labelExpr is JsNameRef)
                {
                    p.print((labelExpr as JsNameRef).ident)
                }
                else if (labelExpr is JsStringLiteral)
                {
                    p.print((labelExpr as JsStringLiteral).value)
                }
                else
                {
                    accept(labelExpr)
                }
                p.print("'")
                p.print(" => ")
                // _colon()
                space()
                val valueExpr = item.valueExpr
                val wasEnclosed = parenPushIfCommaExpression(valueExpr)
                accept(valueExpr)
                if (wasEnclosed)
                {
                    rightParen()
                }
            }
            if (objectLiteral.isMultiline())
            {
                p.indentOut()
                newlineOpt()
            }
            p.print(')')
        }

    }

    fun isByRefShit(node: JsNode): Boolean {
        return node.kotlinTypeName == "kotlin.Array"
    }

    override fun visitParameter(x: JsParameter) {
        printDebugTag(x)
        if (isByRefShit(x)) {
            p.print("&")
        }
        p.print("$" + escapeIdent(x.name.ident))
    }

    override fun visitPostfixOperation(x:JsPostfixOperation) {
        val op = x.getOperator()
        val arg = x.getArg()
        // unary operators always associate correctly (I think)
        printPair(x, arg)
        p.print(op.getSymbol())
    }
    override fun visitPrefixOperation(x:JsPrefixOperation) {
        val op = x.getOperator()
        p.print(op.getSymbol())
        val arg = x.getArg()
        if (spaceCalc(op, arg))
        {
            space()
        }
        // unary operators always associate correctly (I think)
        printPair(x, arg)
    }
    override fun visitProgram(x:JsProgram) {
        p.print("<JsProgram>")
    }
    override fun visitProgramFragment(x:JsProgramFragment) {
        p.print("<JsProgramFragment>")
    }
    override fun visitRegExp(x:JsRegExp) {
        slash()
        p.print(x.getPattern())
        slash()
        val flags = x.getFlags()
        if (flags != null)
        {
            p.print(flags)
        }
    }
    override fun visitReturn(x:JsReturn) {
        printDebugTag(x)
        p.print(CHARS_RETURN)
        val expr = x.getExpression()
        if (expr != null)
        {
            space()
            accept(expr)
        }
    }
    override fun visitString(x:JsStringLiteral) {
        p.print(phpString(x.value, forceDoubleQuote = true))
    }
    override fun visit(x:JsSwitch) {
        p.print(CHARS_SWITCH)
        spaceOpt()
        leftParen()
        accept(x.getExpression())
        rightParen()
        spaceOpt()
        blockOpen()
        acceptList(x.getCases())
        blockClose()
    }
    override fun visitThis(x:JsLiteral.JsThisRef) {
        p.print(CHARS_THIS)
    }

    override fun visitThrow(x: JsThrow) {
        printDebugTag(x)
        p.print(CHARS_THROW)
        space()
        accept(x.expression)
    }

    override fun visitTry(x:JsTry) {
        p.print(CHARS_TRY)
        spaceOpt()
        accept(x.getTryBlock())
        acceptList(x.getCatches())
        val finallyBlock = x.getFinallyBlock()
        if (finallyBlock != null)
        {
            p.print(CHARS_FINALLY)
            spaceOpt()
            accept(finallyBlock)
        }
    }

    override fun visit(v: JsVar) {
        v.propertyDescriptor?.let {printAnnotations(it, v)}
        v.visibility?.let {p.print("$it ")}
        printDebugTag(v)
        p.print("$" + escapeIdent(v.name.ident))
        val initExpr = v.initExpression
        if (initExpr != null) {
            spaceOpt()

//            if (v.debugTag == DebugGlobal.breakOnDebugTag) {
//                "break on me"
//            }

            assignment(byRef = isByRefShit(v) && initExpr is JsNameRef)
            spaceOpt()
            val isEnclosed = parenPushIfCommaExpression(initExpr)
            accept(initExpr)
            if (isEnclosed)
            {
                rightParen()
            }
        }
    }

    override fun visitVars(vars: JsVars) {
        space()
        var sep = false
        for (v in vars)
        {
            if (sep)
            {
                if (vars.isMultiline)
                {
                    newlineOpt()
                }
                spaceOpt()
            }
            else
            {
                sep = true
            }
            accept(v)
            p.print(';')
        }
    }

    override fun visitDocComment(comment:JsDocComment) {
        val asSingleLine = comment.getTags().size === 1
        if (!asSingleLine)
        {
            newlineOpt()
        }
        p.print("/**")
        if (asSingleLine)
        {
            space()
        }
        else
        {
            p.newline()
        }
        var notFirst = false
        for (entry in comment.getTags().entries)
        {
            if (notFirst)
            {
                p.newline()
                p.print(' ')
                p.print('*')
            }
            else
            {
                notFirst = true
            }
            p.print('@')
            p.print(entry.key)
            val value = entry.value
            if (value != null)
            {
                space()
                if (value is CharSequence)
                {
                    p.print(value as CharSequence)
                }
                else
                {
                    visitNameRef(value as JsNameRef)
                }
            }
            if (!asSingleLine)
            {
                p.newline()
            }
        }
        if (asSingleLine)
        {
            space()
        }
        else
        {
            newlineOpt()
        }
        p.print('*')
        p.print('/')
        if (asSingleLine)
        {
            spaceOpt()
        }
    }
    protected fun newlineOpt() {
        if (!p.isCompact())
        {
            p.newline()
        }

        // @debug
//        val breakOnLine = 37
//        if (p.line == breakOnLine - 1) {
//            "break on me"
//        }
    }
    protected fun printJsBlock(x:JsBlock, _finalNewline:Boolean) {
        var finalNewline = _finalNewline
        if (!lineBreakAfterBlock)
        {
            finalNewline = false
            lineBreakAfterBlock = true
        }
        val needBraces = !x.isGlobalBlock()
        if (needBraces)
        {
            blockOpen()
        }
        val iterator = x.getStatements().iterator()
        while (iterator.hasNext())
        {
            val isGlobal = x.isGlobalBlock() || globalBlocks.contains(x)
            val statement = iterator.next()
            if (statement is JsEmpty)
            {
                continue
            }
            needSemi = true
            var stmtIsGlobalBlock = false
            if (isGlobal)
            {
                if (statement is JsBlock)
                {
                    // A block inside a global block is still considered global
                    stmtIsGlobalBlock = true
                    globalBlocks.add(statement as JsBlock)
                }
            }

            val commentedOut = statement is AbstractNode && statement.commentedOut
            if (commentedOut) p.print("/*")
            accept(statement)
            if (commentedOut) p.print("*/")

            if (stmtIsGlobalBlock)
            {
                globalBlocks.remove(statement)
            }
            if (needSemi)
            {
                /*
         * Special treatment of function declarations: If they are the only item in a
         * statement (i.e. not part of an assignment operation), just give them
         * a newline instead of a semi.
         */
                val functionStmt = statement is JsExpressionStatement && (statement as JsExpressionStatement).getExpression() is JsFunction
                /*
         * Special treatment of the last statement in a block: only a few
         * statements at the end of a block require semicolons.
         */
                val lastStatement = !iterator.hasNext() && needBraces && !JsRequiresSemiVisitor.exec(statement)
                if (functionStmt)
                {
                    if (lastStatement)
                    {
                        newlineOpt()
                    }
                    else
                    {
                        p.newline()
                    }
                }
                else
                {
                    if (lastStatement)
                    {
                        p.printOpt(';')
                    }
                    else
                    {
                        semi()
                    }
                    newlineOpt()
                }
            }
        }
        if (needBraces)
        {
            // _blockClose() modified
            p.indentOut()
            p.print('}')
            if (finalNewline)
            {
                newlineOpt()
            }
        }
        needSemi = false
    }

    private fun assignment(byRef: Boolean) {
        p.print('=')
        if (byRef) p.print('&')
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
        // printDebugTag(name)
        p.print(escapeIdent(name.ident))
    }

    private fun nameOf(hasName: HasName) {
        nameDef(hasName.name)
    }

    private fun nestedPop(statement:JsStatement):Boolean {
        val pop = !(statement is JsBlock)
        if (pop)
        {
            p.indentOut()
        }
        return pop
    }
    private fun nestedPush(statement:JsStatement):Boolean {
        val push = !(statement is JsBlock)
        if (push)
        {
            newlineOpt()
            p.indentIn()
        }
        else
        {
            spaceOpt()
        }
        return push
    }
    private fun _parenPopOrSpace(parent:JsExpression, child:JsExpression, wrongAssoc:Boolean):Boolean {
        val doPop = parenCalc(parent, child, wrongAssoc)
        if (doPop)
        {
            rightParen()
        }
        else
        {
            space()
        }
        return doPop
    }
    private fun parenPush(parent:JsExpression, child:JsExpression, wrongAssoc:Boolean):Boolean {
        val doPush = parenCalc(parent, child, wrongAssoc)
        if (doPush)
        {
            leftParen()
        }
        return doPush
    }
    private fun parenPushIfCommaExpression(x:JsExpression):Boolean {
        val doPush = x is JsBinaryOperation && (x as JsBinaryOperation).getOperator() === JsBinaryOperator.COMMA
        if (doPush)
        {
            leftParen()
        }
        return doPush
    }
    private fun _parenPushOrSpace(parent:JsExpression, child:JsExpression, wrongAssoc:Boolean):Boolean {
        val doPush = parenCalc(parent, child, wrongAssoc)
        if (doPush)
        {
            leftParen()
        }
        else
        {
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
    private fun sepCommaOptSpace(sep:Boolean):Boolean {
        if (sep)
        {
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

    private fun printVar() {
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
        private val CHARS_NULL = "NULL".toCharArray()
        private val CHARS_RETURN = "return".toCharArray()
        private val CHARS_SWITCH = "switch".toCharArray()
        private val CHARS_THIS = "this".toCharArray()
        private val CHARS_THROW = "throw".toCharArray()
        private val CHARS_TRUE = "true".toCharArray()
        private val CHARS_TRY = "try".toCharArray()
        private val CHARS_VAR = "var".toCharArray()
        private val CHARS_WHILE = "while".toCharArray()
        private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

        fun javaScriptString(value:String):CharSequence {
            return phpString(value, false)
        }

        /**
         * Generate JavaScript code that evaluates to the supplied string. Adapted
         * from {@link org.mozilla.javascript.ScriptRuntime#escapeString(String)}
         * . The difference is that we quote with either &quot; or &apos; depending on
         * which one is used less inside the string.
         */
        fun phpString(chars: CharSequence, forceDoubleQuote: Boolean = false):CharSequence {
            val n = chars.length
            var quoteCount = 0
            var aposCount = 0
            for (i in 0..n - 1)
            {
                when (chars.get(i)) {
                    '"' -> ++quoteCount
                    '\'' -> ++aposCount
                }
            }
            val result = StringBuilder(n + 16)
            val quoteChar = if ((quoteCount < aposCount || forceDoubleQuote)) '"' else '\''
            result.append(quoteChar)
            for (i in 0..n - 1)
            {
                val c = chars.get(i)
                if (' ' <= c && c <= '~' && c != quoteChar && c != '\\' && c != '$')
                {
                    // an ordinary print character (like C isprint())
                    result.append(c)
                    continue
                }
                var escape = -1
                when (c) {
                    '\b' -> escape = 'b'.toInt()
//                    '\f' -> escape = 'f'
                    '\n' -> escape = 'n'.toInt()
                    '\r' -> escape = 'r'.toInt()
                    '\t' -> escape = 't'.toInt()
                    '"' -> escape = '"'.toInt()
                    '\'' -> escape = '\''.toInt()
                    '\\' -> escape = '\\'.toInt()
                    '$' -> escape = '$'.toInt()
                }// only reach here if == quoteChar
                // only reach here if == quoteChar
                if (escape >= 0)
                {
                    // an \escaped sort of character
                    result.append('\\')
                    result.append(escape.toChar())
                }
                else
                {
                    val hexSize:Int
                    if (c.toInt() < 256)
                    {
                        // 2-digit hex
                        result.append("\\x")
                        hexSize = 2
                    }
                    else
                    {
                        // Unicode.
                        result.append("\\u")
                        hexSize = 4
                    }
                    // append hexadecimal form of ch left-padded with 0
                    var shift = (hexSize - 1) * 4
                    while (shift >= 0)
                    {
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
         * Escapes any closing XML tags embedded in <code>str</code>, which could
         * potentially cause a parse failure in a browser, for example, embedding a
         * closing <code>&lt;script&gt;</code> tag.
         *
         * @param str an unescaped literal; May be null
         */
        private fun escapeClosingTags(str:StringBuilder) {
            if (str == null)
            {
                return
            }
            var index = 0
            while (true) {
                index = str.indexOf("</", index)
                if (index == -1) break
                str.insert(index + 1, '\\')
            }
        }
        private fun parenCalc(parent:JsExpression, child:JsExpression, wrongAssoc:Boolean):Boolean {
            val parentPrec = JsPrecedenceVisitor.exec(parent)
            val childPrec = JsPrecedenceVisitor.exec(child)
            return parentPrec > childPrec || parentPrec == childPrec && wrongAssoc
        }
        /**
         * Decide whether, if <code>op</code> is printed followed by <code>arg</code>,
         * there needs to be a space between the operator and expression.
         *
         * @return <code>true</code> if a space needs to be printed
         */
        private fun spaceCalc(op:JsOperator, arg:JsExpression):Boolean {
            if (op.isKeyword())
            {
                return true
            }
            if (arg is JsBinaryOperation)
            {
                val binary = arg as JsBinaryOperation
                /*
         * If the binary operation has a higher precedence than op, then it won't
         * be parenthesized, so check the first argument of the binary operation.
         */
                return binary.getOperator().getPrecedence() > op.getPrecedence() && spaceCalc(op, binary.getArg1())
            }
            if (arg is JsPrefixOperation)
            {
                val op2 = (arg as JsPrefixOperation).getOperator()
                return (op === JsBinaryOperator.SUB || op === JsUnaryOperator.NEG) && (op2 === JsUnaryOperator.DEC || op2 === JsUnaryOperator.NEG) || (op === JsBinaryOperator.ADD && op2 === JsUnaryOperator.INC)
            }
            if (arg is JsNumberLiteral && (op === JsBinaryOperator.SUB || op === JsUnaryOperator.NEG))
            {
                if (arg is JsIntLiteral)
                {
                    return (arg as JsIntLiteral).value < 0
                }
                else
                {
                    assert(arg is JsDoubleLiteral)
                    return (arg as JsDoubleLiteral).value < 0
                }
            }
            return false
        }
    }
}
