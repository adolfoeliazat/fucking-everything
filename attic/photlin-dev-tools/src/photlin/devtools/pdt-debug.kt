package photlin.devtools

import com.intellij.lang.*
import com.intellij.lang.impl.PsiBuilderImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.util.diff.FlyweightCapableTreeStructure
import vgrechka.*
import java.io.PrintWriter
import java.io.StringWriter

//class TracingPsiBuilder(val wrappee: PsiBuilder) : PsiBuilder {
//    val sw = StringWriter()
//    val pw = PrintWriter(sw)
//    var level = 0
//
//    fun dumpShit() {
//        clog("\n================= Thread ${Thread.currentThread().id} ===============\n")
//        clog(sw.toString())
//    }
//
//    fun spewBegin(s: String) {
//        fun fuck() = pw.print("-- PsiBuilder.$s")
//
//        if (level == 0) {
//            fuck()
//        } else {
//            pw.println()
//            pw.print("  ".repeat(level))
//            fuck()
//        }
//
//        ++level
//    }
//
//    fun spewEnd(result: Any?) {
//        if (result != Unit) {
//            val s = result.toString().replace("\r", "\\r").replace("\n", "\\n")
//            pw.print(": $s")
//        }
//        pw.println()
//        --level
//    }
//
//    override fun getCurrentOffset(): Int {
//        spewBegin("getCurrentOffset()")
//        val res = wrappee.getCurrentOffset()
//        spewEnd(": $res")
//        return res
//    }
//
//    override fun advanceLexer() {
//        spewBegin("advanceLexer()")
//        wrappee.advanceLexer()
//        spewEnd(Unit)
//    }
//
//    override fun lookAhead(steps: Int): IElementType? {
//        spewBegin("lookAhead(steps = $steps)")
//        val res = wrappee.lookAhead(steps)
//        spewEnd(res)
//        return res
//    }
//
//    override fun <T : Any?> getUserDataUnprotected(key: Key<T>): T? {
//        spewBegin("getUserDataUnprotected(key = $key)")
//        val res = wrappee.getUserDataUnprotected(key)
//        spewEnd(res)
//        return res
//    }
//
//    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
//        spewBegin("putUserData()")
//        wrappee.putUserData(key, value)
//        spewEnd(Unit)
//    }
//
//    override fun getLightTree(): FlyweightCapableTreeStructure<LighterASTNode> {
//        spewBegin("getLightTree()")
//        val res = wrappee.getLightTree()
//        spewEnd(res)
//        return res
//    }
//
//    override fun getLatestDoneMarker(): LighterASTNode? {
//        spewBegin("getLatestDoneMarker()")
//        val res = wrappee.getLatestDoneMarker()
//        spewEnd(res)
//        return res
//    }
//
//    override fun getTreeBuilt(): ASTNode {
//        spewBegin("getTreeBuilt()")
//        val res = wrappee.getTreeBuilt()
//        spewEnd(res)
//        return res
//    }
//
//    override fun setDebugMode(dbgMode: Boolean) {
//        spewBegin("setDebugMode(dbgMode = $dbgMode)")
//        wrappee.setDebugMode(dbgMode)
//        spewEnd(Unit)
//    }
//
//    override fun getTokenText(): String? {
//        spewBegin("getTokenText()")
//        val res = wrappee.getTokenText()
//        spewEnd(res)
//        return res
//    }
//
//    override fun rawLookup(steps: Int): IElementType? {
//        spewBegin("rawLookup(steps = $steps)")
//        val res = wrappee.rawLookup(steps)
//        spewEnd(res)
//        return res
//    }
//
//    override fun getOriginalText(): CharSequence {
//        spewBegin("getOriginalText()")
//        val res = wrappee.getOriginalText()
//        spewEnd(res)
//        return res
//    }
//
//    override fun eof(): Boolean {
//        spewBegin("eof()")
//        val res = wrappee.eof()
//        spewEnd(res)
//        return res
//    }
//
//    override fun error(messageText: String?) {
//        spewBegin("error(messageText = $messageText)")
//        wrappee.error(messageText)
//        spewEnd(Unit)
//    }
//
//    override fun getProject(): Project {
//        spewBegin("getProject()")
//        val res = wrappee.getProject()
//        spewEnd(res)
//        return res
//    }
//
//    override fun rawTokenTypeStart(steps: Int): Int {
//        spewBegin("rawTokenTypeStart(steps = $steps)")
//        val res = wrappee.rawTokenTypeStart(steps)
//        spewEnd(res)
//        return res
//    }
//
//    override fun <T : Any?> getUserData(key: Key<T>): T? {
//        spewBegin("getUserData(key = $key)")
//        val res = wrappee.getUserData(key)
//        spewEnd(res)
//        return res
//    }
//
//    override fun setTokenTypeRemapper(remapper: ITokenTypeRemapper?) {
//        spewBegin("setTokenTypeRemapper(remapper = $remapper)")
//        wrappee.setTokenTypeRemapper(remapper)
//        spewEnd(Unit)
//    }
//
//    override fun setWhitespaceSkippedCallback(callback: WhitespaceSkippedCallback?) {
//        spewBegin("setWhitespaceSkippedCallback(callback = $callback)")
//        wrappee.setWhitespaceSkippedCallback(callback)
//        spewEnd(Unit)
//    }
//
//    override fun rawTokenIndex(): Int {
//        spewBegin("rawTokenIndex()")
//        val res = wrappee.rawTokenIndex()
//        spewEnd(res)
//        return res
//    }
//
//    override fun remapCurrentToken(type: IElementType?) {
//        spewBegin("remapCurrentToken(type = $type)")
//        wrappee.remapCurrentToken(type)
//        spewEnd(Unit)
//    }
//
//    override fun getTokenType(): IElementType? {
//        spewBegin("getTokenType()")
//        val res = wrappee.getTokenType()
//        spewEnd(res)
//        return res
//    }
//
//    override fun <T : Any?> putUserDataUnprotected(key: Key<T>, value: T?) {
//        spewBegin("putUserDataUnprotected(key = $key, value = $value)")
//        wrappee.putUserDataUnprotected(key, value)
//        spewEnd(Unit)
//    }
//
//    override fun mark(): PsiBuilder.Marker {
//        spewBegin("mark()")
//        val marker = wrappee.mark()
//        spewEnd(marker)
//        val productionMarker = marker as PsiBuilderImpl.ProductionMarker // TracingMarker(pw, marker)
//
//        val tracingProductionMarker = object : PsiBuilderImpl.ProductionMarker(), PsiBuilder.Marker {
//
//            // --------------- ProductionMarker -----------------
//
//            fun hc(): Int {
//
//            }
//
//            override fun clean() {
//                spewBegin("(PM)clean()")
//                val res = productionMarker.clean()
//                spewEnd(res)
//                return res
//            }
//
//            override fun remapTokenType(type: IElementType) {
//                spewBegin("(PM)remapTokenType(type = $type)")
//                val res = productionMarker.remapTokenType(type)
//                spewEnd(res)
//                return res
//            }
//
//            override fun getStartIndex(): Int {
//                spewBegin("(PM)getStartIndex()")
//                val res = productionMarker.getStartIndex()
//                spewEnd(res)
//                return res
//            }
//
//            override fun getEndIndex(): Int {
//                spewBegin("(PM)getEndIndex()")
//                val res = productionMarker.getEndIndex()
//                spewEnd(res)
//                return res
//            }
//
//            // --------------- Marker -----------------
//
//            var level = 1
//
//            fun spewBegin(s: String) {
//                fun fuck() = pw.print("-- Marker.$s")
//
//                if (level == 0) {
//                    fuck()
//                } else {
//                    pw.println()
//                    pw.print("  ".repeat(level))
//                    fuck()
//                }
//
//                ++level
//            }
//
//            fun spewEnd(result: Any?) {
//                if (result != Unit) {
//                    val s = result.toString().replace("\r", "\\r").replace("\n", "\\n")
//                    pw.print(": $s")
//                }
//                pw.println()
//                --level
//            }
//
//
//            override fun setCustomEdgeTokenBinders(left: WhitespacesAndCommentsBinder?, right: WhitespacesAndCommentsBinder?) {
//                spewBegin("setCustomEdgeTokenBinders(left = $left, right = $right)")
//                val res = marker.setCustomEdgeTokenBinders(left, right)
//                spewEnd(res)
//                return res
//            }
//
//            override fun collapse(type: IElementType) {
//                spewBegin("collapse(type = $type)")
//                val res = marker.collapse(type)
//                spewEnd(res)
//                return res
//            }
//
//            override fun errorBefore(message: String?, before: PsiBuilder.Marker) {
//                spewBegin("errorBefore(message = $message, before = $before)")
//                val res = marker.errorBefore(message, before)
//                spewEnd(res)
//                return res
//            }
//
//            override fun drop() {
//                spewBegin("drop()")
//                val res = marker.drop()
//                spewEnd(res)
//                return res
//            }
//
//            override fun rollbackTo() {
//                spewBegin("rollbackTo()")
//                val res = marker.rollbackTo()
//                spewEnd(res)
//                return res
//            }
//
//            override fun done(type: IElementType) {
//                spewBegin("done(type = $type)")
//                val res = marker.done(type)
//                spewEnd(res)
//                return res
//            }
//
//            override fun doneBefore(type: IElementType, before: PsiBuilder.Marker) {
//                spewBegin("doneBefore(type = $type, before = $before)")
//                val res = marker.doneBefore(type, before)
//                spewEnd(res)
//                return res
//            }
//
//            override fun doneBefore(type: IElementType, before: PsiBuilder.Marker, errorMessage: String?) {
//                spewBegin("doneBefore(type = $type, before = $before, errorMessage = $errorMessage)")
//                val res = marker.doneBefore(type, before, errorMessage)
//                spewEnd(res)
//                return res
//            }
//
//            override fun error(message: String?) {
//                spewBegin("error(message = $message)")
//                val res = marker.error(message)
//                spewEnd(res)
//                return res
//            }
//
//            override fun precede(): PsiBuilder.Marker {
//                spewBegin("precede()")
//                val res = marker.precede()
//                spewEnd(res)
//                return res
//            }
//        }
//
//        return marker
//    }
//
//    override fun enforceCommentTokens(tokens: TokenSet) {
//        spewBegin("enforceCommentTokens(tokens = $tokens)")
//        wrappee.enforceCommentTokens(tokens)
//        spewEnd(Unit)
//    }
//
//}
//
//class TracingMarker(val pw: PrintWriter, val wrappee: PsiBuilder.Marker) : PsiBuilder.Marker {
//    var level = 1
//
//    fun spewBegin(s: String) {
//        fun fuck() = pw.print("-- Marker.$s")
//
//        if (level == 0) {
//            fuck()
//        } else {
//            pw.println()
//            pw.print("  ".repeat(level))
//            fuck()
//        }
//
//        ++level
//    }
//
//    fun spewEnd(result: Any?) {
//        if (result != Unit) {
//            val s = result.toString().replace("\r", "\\r").replace("\n", "\\n")
//            pw.print(": $s")
//        }
//        pw.println()
//        --level
//    }
//
//
//    override fun setCustomEdgeTokenBinders(left: WhitespacesAndCommentsBinder?, right: WhitespacesAndCommentsBinder?) {
//        spewBegin("setCustomEdgeTokenBinders(left = $left, right = $right)")
//        val res = wrappee.setCustomEdgeTokenBinders(left, right)
//        spewEnd(res)
//        return res
//    }
//
//    override fun collapse(type: IElementType) {
//        spewBegin("collapse(type = $type)")
//        val res = wrappee.collapse(type)
//        spewEnd(res)
//        return res
//    }
//
//    override fun errorBefore(message: String?, before: PsiBuilder.Marker) {
//        spewBegin("errorBefore(message = $message, before = $before)")
//        val res = wrappee.errorBefore(message, before)
//        spewEnd(res)
//        return res
//    }
//
//    override fun drop() {
//        spewBegin("drop()")
//        val res = wrappee.drop()
//        spewEnd(res)
//        return res
//    }
//
//    override fun rollbackTo() {
//        spewBegin("rollbackTo()")
//        val res = wrappee.rollbackTo()
//        spewEnd(res)
//        return res
//    }
//
//    override fun done(type: IElementType) {
//        spewBegin("done(type = $type)")
//        val res = wrappee.done(type)
//        spewEnd(res)
//        return res
//    }
//
//    override fun doneBefore(type: IElementType, before: PsiBuilder.Marker) {
//        spewBegin("doneBefore(type = $type, before = $before)")
//        val res = wrappee.doneBefore(type, before)
//        spewEnd(res)
//        return res
//    }
//
//    override fun doneBefore(type: IElementType, before: PsiBuilder.Marker, errorMessage: String?) {
//        spewBegin("doneBefore(type = $type, before = $before, errorMessage = $errorMessage)")
//        val res = wrappee.doneBefore(type, before, errorMessage)
//        spewEnd(res)
//        return res
//    }
//
//    override fun error(message: String?) {
//        spewBegin("error(message = $message)")
//        val res = wrappee.error(message)
//        spewEnd(res)
//        return res
//    }
//
//    override fun precede(): PsiBuilder.Marker {
//        spewBegin("precede()")
//        val res = wrappee.precede()
//        spewEnd(res)
//        return res
//    }
//
//}





