package photlinc

import com.intellij.util.SmartList
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.common.Symbol
import org.jetbrains.kotlin.js.translate.context.StaticContext
import org.jetbrains.kotlin.js.util.AstUtil
import kotlin.properties.Delegates.notNull
import photlin.*
import vgrechka.*

fun phpThisRef(): JsExpression {
    return JsNameRef("this")-{o->
        o.kind = PHPNameRefKind.VAR
    }
}

fun phpName(s: String): JsName {
    return StaticContext.current.program.scope.declareName(s)
}

fun phpNameRef(s: String): JsNameRef {
    return JsNameRef(s)
}

fun newPHPVar(name: JsName, expr: JsExpression?): JsVars {
    return JsVars(JsVars.JsVar(name, expr))
}

//class PHPVars @JvmOverloads constructor(private val vars: MutableList<PHPVar> = SmartList<PHPVar>(), val isMultiline: Boolean = false) : SourceInfoAwareJsNode(), JsStatement, Iterable<PHPVar> {
//
//    constructor(multiline: Boolean) : this(SmartList<PHPVar>(), multiline) {}
//
//    constructor(`var`: PHPVar) : this(SmartList(`var`), false) {}
//
//    constructor(vararg vars: PHPVar) : this(SmartList(*vars), false) {}
//
//
//    fun add(`var`: PHPVar) {
//        vars.add(`var`)
//    }
//
//    fun addAll(vars: Collection<PHPVar>) {
//        this.vars.addAll(vars)
//    }
//
//    fun addAll(otherVars: PHPVars) {
//        this.vars.addAll(otherVars.vars)
//    }
//
//    val isEmpty: Boolean
//        get() = vars.isEmpty()
//
//    override fun iterator(): Iterator<PHPVar> {
//        return vars.iterator()
//    }
//
//    fun getVars(): List<PHPVar> {
//        return vars
//    }
//
//    override fun accept(v: JsVisitor) {
//        v.visitPHPVars(this)
//    }
//
//    override fun acceptChildren(visitor: JsVisitor) {
//        visitor.acceptWithInsertRemove(vars)
//    }
//
//    override fun traverse(v: JsVisitorWithContext, ctx: JsContext<*>) {
//        if (v.visit(this, ctx)) {
//            v.acceptList(vars)
//        }
//        v.endVisit(this, ctx)
//    }
//
//    override fun deepCopy(): PHPVars {
//        return PHPVars(AstUtil.deepCopy(vars), isMultiline).withMetadataFrom(this)
//    }
//}


//class PHPVar(val name: JsName, var initExpression: JsExpression? = null, val visibility: String? = null) : SourceInfoAwareJsNode() {
//    init {
//        constructed()
//    }
//
//    private fun constructed() {
//        val fuck = "break on me"
//    }
//
//    override fun accept(v: JsVisitor) {
//        v.visit(this)
//    }
//
//    override fun acceptChildren(visitor: JsVisitor) {
//        val initExpression = this.initExpression
//        if (initExpression != null) {
//            visitor.accept<JsExpression>(initExpression)
//        }
//    }
//
//    override fun traverse(v: JsVisitorWithContext, ctx: JsContext<*>) {
//        if (v.visit(this, ctx)) {
//            if (initExpression != null) {
//                initExpression = v.accept<JsExpression>(initExpression)
//            }
//        }
//        v.endVisit(this, ctx)
//    }
//
//    override fun deepCopy(): PHPVar {
//        val initExpression = this.initExpression
//        if (initExpression == null) return PHPVar(name)
//
//        return PHPVar(name, initExpression.deepCopy()).withMetadataFrom(this)
//    }
//}

//class PHPFieldRef(var receiver: JsExpression, val fieldName: JsName) : JsExpression() {
//    init {
//    }
//
//    override fun accept(v: JsVisitor) {
//        v.visitPHPFieldRef(this)
//    }
//
//    override fun acceptChildren(visitor: JsVisitor) {
//        visitor.accept(receiver)
//    }
//
//    override fun traverse(v: JsVisitorWithContext, ctx: JsContext<*>) {
//        if (v.visit(this, ctx)) {
//            receiver = v.accept(receiver)
//        }
//        v.endVisit(this, ctx)
//    }
//
//    override fun deepCopy(): PHPFieldRef {
//        val receiverCopy = AstUtil.deepCopy(receiver)
//        return PHPFieldRef(receiverCopy!!, fieldName).withMetadataFrom(this)
//    }
//}

//class PHPVarRef : JsExpression, HasName {
//
//    private var ident: String? = null
//    private var name: JsName? = null
////    var qualifier: JsExpression? = null
//
//    constructor(name: JsName) {
//        this.name = name
//        constructed()
//    }
//
//    constructor(ident: String) {
//        this.ident = ident
//        constructed()
//    }
//
//    private fun constructed() {
//        initDebugTag()
//    }
//
//    fun getIdent(): String {
//        return if (name == null) ident!! else name!!.ident
//    }
//
//    override fun getName(): JsName? {
//        return name
//    }
//
//    override fun setName(name: JsName) {
//        this.name = name
//    }
//
//    override fun getSymbol(): Symbol? {
//        return name
//    }
//
//    override fun isLeaf(): Boolean {
//        return true
////        return qualifier == null
//    }
//
//    fun resolve(name: JsName) {
//        this.name = name
//        ident = null
//    }
//
//    override fun accept(v: JsVisitor) {
//        v.visitPHPVarRef(this)
//    }
//
//    override fun acceptChildren(visitor: JsVisitor) {
////        if (qualifier != null) {
////            visitor.accept<JsExpression>(qualifier)
////        }
//    }
//
//    override fun traverse(v: JsVisitorWithContext, ctx: JsContext<*>) {
//        if (v.visit(this, ctx)) {
////            if (qualifier != null) {
////                qualifier = v.accept<JsExpression>(qualifier)
////            }
//        }
//        v.endVisit(this, ctx)
//    }
//
//    override fun deepCopy(): PHPVarRef {
////        val qualifierCopy = AstUtil.deepCopy(qualifier)
//
//        val res = if (name != null) {
//            PHPVarRef(name!!).withMetadataFrom(this)
//        } else {
//            return PHPVarRef(ident!!).withMetadataFrom(this)
//        }
//        return res
//    }
//}

class PHPGlobalVarRef : JsExpression, HasName {
    private var ident: String? = null
    private var name: JsName? = null
//    var qualifier: JsExpression? = null

    constructor(name: JsName) {
        this.name = name
        constructed()
    }

    constructor(ident: String) {
        this.ident = ident
        constructed()
    }

    private fun constructed() {
        initDebugTag()
    }

    fun getIdent(): String {
        return if (name == null) ident!! else name!!.ident
    }

    override fun getName(): JsName? {
        return name
    }

    override fun setName(name: JsName) {
        this.name = name
    }

    override fun getSymbol(): Symbol? {
        return name
    }

    override fun isLeaf(): Boolean {
        return true
//        return qualifier == null
    }

    fun resolve(name: JsName) {
        this.name = name
        ident = null
    }

    override fun accept(v: JsVisitor) {
        v.visitPHPGlobalVarRef(this)
    }

    override fun acceptChildren(visitor: JsVisitor) {
//        if (qualifier != null) {
//            visitor.accept<JsExpression>(qualifier)
//        }
    }

    override fun traverse(v: JsVisitorWithContext, ctx: JsContext<*>) {
        if (v.visit(this, ctx)) {
//            if (qualifier != null) {
//                qualifier = v.accept<JsExpression>(qualifier)
//            }
        }
        v.endVisit(this, ctx)
    }

    override fun deepCopy(): PHPGlobalVarRef {
//        val qualifierCopy = AstUtil.deepCopy(qualifier)

        val res = if (name != null) {
            PHPGlobalVarRef(name!!).withMetadataFrom(this)
        } else {
            return PHPGlobalVarRef(ident!!).withMetadataFrom(this)
        }
        return res
    }
}

//sealed class PHPInvocationCallee {
//    class NamedFunction(val name: JsName) : PHPInvocationCallee()
////    class VarArrowMethod(val varName: String, val methodName: String) : PHPInvocationCallee()
//}

//class PHPInvocation(var callee: JsExpression, arguments: List<JsExpression>) : JsExpression.JsExpressionHasArguments(SmartList(arguments)) {
//
//    init {
//        constructed()
//    }
//
//    private fun constructed() {
//        val fuck = "break on me"
//    }
//
//    override fun getArguments(): List<JsExpression> {
//        return arguments
//    }
//
//    override fun accept(v: JsVisitor) {
//        v.visitPHPInvocation(this)
//    }
//
//    override fun acceptChildren(visitor: JsVisitor) {
//        visitor.accept(callee)
//        visitor.acceptList(arguments)
//    }
//
//    override fun traverse(v: JsVisitorWithContext, ctx: JsContext<*>) {
//        if (v.visit(this, ctx)) {
//            callee = v.accept(callee)
//            v.acceptList(arguments)
//        }
//        v.endVisit(this, ctx)
//    }
//
//    override fun deepCopy(): PHPInvocation {
//        val calleeCopy = AstUtil.deepCopy(callee)
//        val argumentsCopy = AstUtil.deepCopy(arguments)
//        return PHPInvocation(calleeCopy!!, argumentsCopy).withMetadataFrom(this)
//    }
//}

class PHPMethodCall(val receiver: JsExpression, val methodName: String, arguments: List<JsExpression>) : JsExpression.JsExpressionHasArguments(SmartList(arguments)) {
    init {
        initDebugTag()
    }

    override fun getArguments(): List<JsExpression> {
        return arguments
    }

    override fun accept(v: JsVisitor) {
        v.visitPHPMethodCall(this)
    }

    override fun acceptChildren(visitor: JsVisitor) {
        visitor.acceptList(arguments)
    }

    override fun traverse(v: JsVisitorWithContext, ctx: JsContext<*>) {
        if (v.visit(this, ctx)) {
//            callee = v.accept(callee)
            v.acceptList(arguments)
        }
        v.endVisit(this, ctx)
    }

    override fun deepCopy(): PHPMethodCall {
//        val qualifierCopy = AstUtil.deepCopy(qualifier)
        val argumentsCopy = AstUtil.deepCopy(arguments)
        return PHPMethodCall(receiver, methodName, argumentsCopy).withMetadataFrom(this)
    }
}

class PHPStaticMethodCall(val className: String, val methodName: String, arguments: List<JsExpression>) : JsExpression.JsExpressionHasArguments(SmartList(arguments)) {

    init {
        initDebugTag()
    }

    override fun getArguments(): List<JsExpression> {
        return arguments
    }

    override fun accept(v: JsVisitor) {
        v.visitPHPStaticMethodCall(this)
    }

    override fun acceptChildren(visitor: JsVisitor) {
//        visitor.accept(qualifier)
        visitor.acceptList(arguments)
    }

    override fun traverse(v: JsVisitorWithContext, ctx: JsContext<*>) {
        if (v.visit(this, ctx)) {
//            callee = v.accept(callee)
            v.acceptList(arguments)
        }
        v.endVisit(this, ctx)
    }

    override fun deepCopy(): PHPStaticMethodCall {
        val argumentsCopy = AstUtil.deepCopy(arguments)
        return PHPStaticMethodCall(className, methodName, argumentsCopy).withMetadataFrom(this)
    }
}


class PHPClass @JvmOverloads constructor(val className: JsName, val statements: MutableList<JsStatement> = mutableListOf()) : SourceInfoAwareJsNode(), JsStatement {
    var constructorFunction by notNull<JsFunction>()

    init {
        initDebugTag()
    }

    val isEmpty: Boolean
        get() = statements.isEmpty()

    override fun accept(v: JsVisitor) {
        v.visitPHPClass(this)
    }

    override fun acceptChildren(visitor: JsVisitor) {
        visitor.acceptWithInsertRemove(statements)
    }

    override fun traverse(v: JsVisitorWithContext, ctx: JsContext<*>) {
        if (v.visit(this, ctx)) {
            v.acceptStatementList(statements)
        }
        v.endVisit(this, ctx)
    }

    override fun deepCopy(): PHPClass {
        return PHPClass(className, AstUtil.deepCopy(statements)).withMetadataFrom(this)
    }

}


class JsSingleLineComment(val text: String) : SourceInfoAwareJsNode(), JsStatement {
    init {
        initDebugTag()
    }

    override fun accept(v: JsVisitor) {
        v.visitSingleLineComment(this)
    }

    override fun traverse(v: JsVisitorWithContext, ctx: JsContext<*>) {
        v.visit(this, ctx)
        v.endVisit(this, ctx)
    }

    override fun deepCopy(): JsSingleLineComment {
        return this
    }
}


class PHPPlainCodeExpression(val spewCode: () -> String) : JsExpression() {
    init {
        initDebugTag()
    }

    override fun accept(visitor: JsVisitor) {
        visitor.visitPHPPlainCodeExpression(this)
    }

    override fun traverse(visitor: JsVisitorWithContext, ctx: JsContext<*>) {
    }

    override fun deepCopy(): PHPPlainCodeExpression {
        return this
    }
}

enum class PHPNameRefKind {
    DUNNO, VAR, GLOBAL_VAR, FIELD, STATIC, LAMBDA, LAMBDA_CREATOR, STRING_LITERAL
}

























