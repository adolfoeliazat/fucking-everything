package vgrechka.phizdetsidea

import com.intellij.execution.ExecutionException
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.task.ProjectTaskManager
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XDebuggerUtil
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.frame.XExecutionStack
import com.intellij.xdebugger.frame.XStackFrame
import com.intellij.xdebugger.frame.XSuspendContext
import com.intellij.xdebugger.impl.breakpoints.XBreakpointBase
import com.intellij.xdebugger.impl.breakpoints.XBreakpointManagerImpl
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl
import com.sun.jna.platform.win32.User32
import org.eclipse.core.runtime.ILog
import org.eclipse.core.runtime.ILogListener
import org.eclipse.core.runtime.IStatus
import org.eclipse.debug.core.DebugEvent
import org.eclipse.php.internal.debug.core.xdebug.communication.XDebugCommunicationDaemon
import org.eclipse.php.internal.debug.core.xdebug.dbgp.protocol.DBGpCommand
import org.eclipse.php.internal.debug.core.xdebug.dbgp.protocol.DBGpResponse
import org.eclipse.php.internal.debug.core.xdebug.dbgp.protocol.DBGpUtils
import org.eclipse.php.internal.debug.core.xdebug.dbgp.session.DBGpSession
import org.eclipse.php.internal.debug.core.xdebug.dbgp.session.DBGpSessionHandler
import org.eclipse.php.internal.debug.core.xdebug.dbgp.session.IDBGpSessionListener
import org.osgi.framework.Bundle
//import phizdets.MapPhizdetsStack
import vgrechka.*
import vgrechka.idea.*
import java.awt.MouseInfo
import java.io.File
import kotlin.concurrent.thread
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.util.*
import kotlin.properties.Delegates.notNull

// TODO:vgrechka Support multiple debug sessions

// TODO:vgrechka First debug run sometimes doesn't result in sessionListener invocation.
//               Hence no breakpoints are sent, etc. Maybe daemon.startListen() should be called beforehand

class PhizdetsIDEAPlugin : ApplicationComponent {
    override fun getComponentName(): String {
        return this::class.qualifiedName!!
    }

    override fun disposeComponent() {
    }

    override fun initComponent() {
        val pm = ProjectManager.getInstance()
        pm.addProjectManagerListener(object : ProjectManagerListener {
            override fun projectOpened(project: Project) {
                clog("Opened project", project.name)
            }
        })

        addBuildMenuItems()

        val am = ActionManager.getInstance()
        val group = am.getAction("ToolsMenu") as DefaultActionGroup
        group.addSeparator()

        run {
            val action = object : AnAction("Phizdets: _Mess Around") {
                var event by notNull<AnActionEvent>()
                val robot = Robot()

                override fun actionPerformed(event: AnActionEvent) {
                    this.event = event
//                    testAddSDK()
//                    testCreateRunConfiguration()
//                    testPreparePHPDebugger()
//                    fuckAroundWithSourceMap()
//                    fuckAroundWithPDT()
                }

                private fun fuckAroundWithPDT() {
                    val daemon = XDebugCommunicationDaemon()
                    daemon.init()
                    daemon.startListen()
                }

                private fun fuckAroundWithSourceMap() {
                    val mapping = SourceMappings().getCached("E:\\fegh\\aps\\aps-back-phi\\out\\production\\aps-back-phi\\aps-back-phi.php.map")
                    val penetration = mapping.penetration
                    penetration.dumpSourceLineToGeneratedLine()
                    "break on me"
                }

                private fun testPreparePHPDebugger() {
                    val mapping = SourceMappings().getCached("E:\\fegh\\aps\\aps-back-phi\\out\\production\\aps-back-phi\\aps-back-phi.php.map")
                    val bm = XDebuggerManager.getInstance(event.project!!).breakpointManager as XBreakpointManagerImpl
                    for (point in bm.allBreakpoints) {
                        if (point is XLineBreakpointImpl) {
                            if (point.type.id == "kotlin-line") {
                                val fileLine = FileLine(point.fileUrl, point.line + 1)
                                val generatedLine = mapping.penetration.sourceFileLineToGeneratedLine[fileLine] ?: run {
                                    Messages.showErrorDialog("No fucking mapping for $fileLine", "Fuck You")
                                }
                                clog("Setting breakpoint at line $generatedLine <-- $fileLine")
                            }
                        }
                    }
                    Messages.showInfoMessage("Fuck, yeah", "Cool")
                }

                fun key(code: Int) {
                    robot.keyPress(code)
                    robot.keyRelease(code)
                }

                fun key(c: Char) {
                    key(c.toUpperCase().toInt())
                }

                fun click() {
                    Thread.sleep(500)
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                    Thread.sleep(500)
                }

                private fun testCreateRunConfiguration() {
                    bitchingThread {
                        robot.keyPress(KeyEvent.VK_ALT)
                        key('u')
                        robot.keyRelease(KeyEvent.VK_ALT)
                        key('r')

                        waitForFuckingWindow("Run/Debug Configurations")

                        click(318, 158)
                        click(372, 480)
                        click(1000, 375)
                        click(1000, 430)
                        click(944, 231)
                        typeText("E:\\fegh\\aps\\aps-back-phi\\out\\production\\aps-back-phi\\aps-back-phi.php")

                        click(880, 153)
                        repeat(15) {key(KeyEvent.VK_BACK_SPACE)}
                        typeText("fuck")

                        key(KeyEvent.VK_ENTER)
                    }
                }

                private fun typeText(text: String) {
                    text.forEach {c->
                        var holdShift = c.isLetter() && c.isUpperCase()

                        val keyCode = when {
                            c.isLetterOrDigit() -> c.toUpperCase().toInt()
                            else -> when (c) {
                                '-' -> KeyEvent.VK_MINUS
                                ' ' -> KeyEvent.VK_SPACE
                                ':' -> {holdShift = true; KeyEvent.VK_SEMICOLON}
                                '.' -> KeyEvent.VK_PERIOD
                                '/' -> KeyEvent.VK_SLASH
                                '\\' -> KeyEvent.VK_BACK_SLASH
                                else -> wtf("Dunno how to type key `$c`")
                            }
                        }

                        if (holdShift)
                            robot.keyPress(KeyEvent.VK_SHIFT)

                        try {
                            robot.keyPress(keyCode)
                            robot.keyRelease(keyCode)
                        } finally {
                            if (holdShift)
                                robot.keyRelease(KeyEvent.VK_SHIFT)
                        }
                    }
                }

                private fun bitchingThread(fuck: () -> Unit) {
                    thread {
                        try {
                            fuck()
                        } catch(e: Throwable) {
                            ApplicationManager.getApplication().invokeLater {
                                Messages.showErrorDialog(e.message, "No Way")
                            }
                        }
                    }
                }

                private fun waitForFuckingWindow(title: String) {
                    var gotWindow = false
                    for (i in 1..3) {
                        val hwnd = User32.INSTANCE.FindWindow(null, title)
                        if (hwnd != null) {
                            gotWindow = true
                            break
                        }
                        Thread.sleep(500)
                    }
                    if (!gotWindow)
                        bitch("No fucking window titled `$title`")
                }

                private fun click(x: Int, y: Int) {
                    robot.mouseMove(x, y)
                    click()
                }

                private fun testAddSDK() {
                    bitchingThread {
                        robot.keyPress(KeyEvent.VK_CONTROL)
                        robot.keyPress(KeyEvent.VK_ALT)
                        robot.keyPress(KeyEvent.VK_SHIFT)
                        key('s')
                        robot.keyRelease(KeyEvent.VK_SHIFT)
                        robot.keyRelease(KeyEvent.VK_ALT)
                        robot.keyRelease(KeyEvent.VK_CONTROL)

                        click(334, 314)
                        click(480, 90)

                        key(KeyEvent.VK_DOWN)
                        Thread.sleep(250)
                        key(KeyEvent.VK_ENTER)
                        Thread.sleep(250)
                        key(KeyEvent.VK_ENTER)
                    }
                }

            }
            group.add(action)
        }
    }

    private fun addBuildMenuItems() {
        val am = ActionManager.getInstance()
        val group = am.getAction("BuildMenu") as DefaultActionGroup
        group.addSeparator()
        group.addAction(object : AnAction("Build and Run in _Browser") {
            override fun actionPerformed(e: AnActionEvent) {
                ProjectTaskManager.getInstance(e.project).buildAllModules {
                    if (it.errors == 0) {
                        thread {
                            try {
                                Thread.sleep(500) // TODO:vgrechka Wait till JS2Phizdets finishes

                                val hwnd =
                                    User32.INSTANCE.FindWindow(null, "APS - Google Chrome")
                                        // ?: User32.INSTANCE.FindWindow(null, "Writer UA - Google Chrome")
                                        ?: bitch("No necessary Chrome window")
                                User32.INSTANCE.SetForegroundWindow(hwnd) || bitch("Cannot bring Chrome to foreground")
                                val origLocation = MouseInfo.getPointerInfo().location
                                val robot = Robot()
                                robot.mouseMove(600, 190) // Somewhere in page (or modal, so it won't be closed!) title
                                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                                robot.mouseMove(origLocation.x, origLocation.y)

                                robot.keyPress(KeyEvent.VK_CONTROL)
                                robot.keyPress('R'.toInt())
                                robot.keyRelease('R'.toInt())
                                robot.keyRelease(KeyEvent.VK_CONTROL)
                            } catch(e: Throwable) {
                                IDEAPile.later {IDEAPile.errorDialog(e)}
                            }
                        }
                    }
                }
            }
        })
    }

}

val kindaEclipseLog: ILog by lazy {object:ILog {
    override fun removeLogListener(p0: ILogListener?) {
        throw UnsupportedOperationException("Implement me, please, fuck you")
    }

    override fun addLogListener(p0: ILogListener?) {
        throw UnsupportedOperationException("Implement me, please, fuck you")
    }

    override fun getBundle(): Bundle {
        throw UnsupportedOperationException("Implement me, please, fuck you")
    }

    override fun log(shit: IStatus) {
        val statusString = when (shit.severity) {
            IStatus.OK -> "OK"
            IStatus.INFO -> "INFO"
            IStatus.WARNING -> "WARNING"
            IStatus.ERROR -> "ERROR"
            IStatus.CANCEL -> "CANCEL"
            else -> wtf("faed31ba-f73e-49a7-943d-3de7ba1b8e54")
        }
        clog("$statusString: ${shit.message}")
    }
}}

class XDebugDaemonAndShit(val project: Project) {
    class Breakpoint(val phpLine: Int, val descr: String, val xbreakpoint: XBreakpointBase<*, *, *>)

    enum class StepMode {
        Kotlin, PHP
    }

    val stepMode = StepMode.Kotlin

    companion object {
        @Volatile var instance: XDebugDaemonAndShit? = null
    }

    var ideaDebugSession by notNullOnce<XDebugSession>()
    private var dbgpSession by notNullOnce<DBGpSession>()
    private var phpLineBreakpoints by notNullOnce<MutableList<Breakpoint>>()
    private var sessionListener by notNullOnce<IDBGpSessionListener>()
    private var daemon by notNullOnce<XDebugCommunicationDaemon>()
    private val sourceMappings = SourceMappings(allowInexactMatches = false)

    init {
        if (instance != null)
            throw ExecutionException("Another instance of XDebugDaemonAndShit is active    e9083cfd-1e5d-4cb4-8dd5-c14b4908793f")
        instance = this

        phpLineBreakpoints = run {
            val res = mutableListOf<Breakpoint>()
            val mapping = sourceMappings.getCached("E:\\fegh\\aps\\aps-back-phi\\out\\production\\aps-back-phi\\aps-back-phi.php.map")
            val bm = XDebuggerManager.getInstance(project).breakpointManager as XBreakpointManagerImpl
            for (point in bm.allBreakpoints) {
                if (point is XLineBreakpointImpl) {
                    if (point.type.id == "kotlin-line") {
                        val fileLine = FileLine(point.fileUrl, point.line + 1)
                        val generatedLine = mapping.penetration.sourceFileLineToGeneratedLine[fileLine] ?: run {
                            throw ExecutionException("No fucking mapping for $fileLine")
                        }
                        res += Breakpoint(generatedLine, "breakpoint at PHP line $generatedLine <-- $fileLine", point)-{o->
                            clog("Will set ${o.descr}")
                        }
                    }
                }
            }
            res
        }


        sessionListener = IDBGpSessionListener {_session->
            noise("SessionCreated")
            dbgpSession = _session
            dbgpSession.xDebugDaemonAndShit = this
            dbgpSession.startSession()

            phpLineBreakpoints.forEach {breakpoint->
                val res = dbgpSession.sendSyncCmd(DBGpCommand.breakPointSet, "" +
                    "-t line" +
                    " -f file://E:/fegh/aps/aps-back-phi/out/production/aps-back-phi/aps-back-phi.php" +
                    " -n ${breakpoint.phpLine}")
                if (res.errorCode != DBGpResponse.ERROR_OK) {
                    wtfBalloon("Can't set ${breakpoint.descr}\n\n" +
                                   "errorCode = ${res.errorCode}\n" +
                                   "errorMessage = ${res.errorMessage}")
                    dbgpSession.endSession()
                    return@IDBGpSessionListener true
                }
            }

            dbgpSession.sendSyncCmd(DBGpCommand.run)
            true
        }
        DBGpSessionHandler.getInstance().addSessionListener(sessionListener)

        daemon = XDebugCommunicationDaemon()
        daemon.init()
        daemon.startListen()
    }

    fun ideaSays_resume() {
        dbgpSend(DBGpCommand.run)
    }

    fun ideaSays_stepOver() {
        when (stepMode) {
            XDebugDaemonAndShit.StepMode.Kotlin -> {
                var expr = "\$GLOBALS['phiExpressionStatement_level']"
                if (!standingInsidePhiExpressionStatement()) {
                    expr += " + 1"
                }
                fuckingStep(maxLevelToBreakOnExpr = expr)
            }
            XDebugDaemonAndShit.StepMode.PHP -> {
                dbgpSend(DBGpCommand.stepOver)
            }
        }
    }

    private val phiExpressionStatementBreakpointLineZeroBased by lazy {
        // TODO:vgrechka Unhardcode phi-engine.php
        val phiFile = File("E:/fegh/aps/aps-back-phi/out/production/aps-back-phi/phi-engine.php")
        val res = phiFile.readLines().indexOfFirst {it.trim() == "strval(\"phiExpressionStatement: break here\");"}
        check(res != -1) {"6d7e5f6a-4b0e-4c5b-bfe3-f46a8ddc81c5"}
        res
    }

    fun ideaSays_stepInto() {
        when (stepMode) {
            XDebugDaemonAndShit.StepMode.Kotlin -> {
                fuckingStep(maxLevelToBreakOnExpr = "INF")
            }
            XDebugDaemonAndShit.StepMode.PHP -> {
                dbgpSend(DBGpCommand.stepInto)
            }
        }
    }

    private var shitWasSet = false
    private fun fuckingStep(maxLevelToBreakOnExpr: String) {
        dbgpSend(DBGpCommand.eval, "-- ${base64Encode(
            "array(" +
                "@\$GLOBALS['phiExpressionStatement_counter'] = 0," +
                "@\$GLOBALS['phiExpressionStatement_maxLevelToBreakOn'] = $maxLevelToBreakOnExpr," +
                "@\$GLOBALS['phiExpressionStatement_breakOnCounter'] = 1);")}") || (return)

        // TODO:vgrechka Unhardcode phi-engine.php
        if (!shitWasSet) {
            dbgpSend(DBGpCommand.breakPointSet, "" +
                "-t line" +
                " -f E:/fegh/aps/aps-back-phi/out/production/aps-back-phi/phi-engine.php" +
                " -n ${phiExpressionStatementBreakpointLineZeroBased + 1}") || (return)
            shitWasSet = true
        }

        dbgpSend(DBGpCommand.run) || (return)
    }

    private fun base64Encode(s: String) = Base64.getEncoder().encodeToString(s.toByteArray(Charsets.UTF_8))

    private fun dbgpSend(cmd: String, args: String? = null): Boolean {
        val res = dbgpSession.sendSyncCmd(cmd, args)
        return when (res.errorCode) {
            DBGpResponse.ERROR_OK -> true
            else -> {
                wtfBalloon("Can't fucking `$cmd`\n\n" +
                               "errorCode = ${res.errorCode}\n" +
                               "errorMessage = ${res.errorMessage}")
                false
            }
        }
    }

    fun dbgpSessionSays_suspended(detail: Int) {
        noise("dbgpSessionSays_suspended: detail = $detail")
        when (detail) {
            DebugEvent.STEP_END -> {
                fuckingSuspended()
            }
            else -> wtfBalloon("detail = $detail    e75b3bfb-4418-46fb-a3d8-d5d106676fb9")
        }
    }

    class MyStackFrame(val virtualFile: VirtualFile, val line: Int) : XStackFrame() {
        override fun getSourcePosition(): XSourcePosition? {
            return XDebuggerUtil.getInstance().createPosition(virtualFile, line)
        }
    }

    private var frames by notNull<List<MyStackFrame>>()

    private fun fuckingSuspended() {
        val filesLines = getCurrentStack() ?: run {
            clog("2e35e98d-1a4c-4b53-bbf4-331aebe842e5")
            return
        }

        frames = mutableListOf<MyStackFrame>()

        for (fileLine in filesLines) {
            var virtualFile = IDEAPile.getVirtualFileByPath(fileLine.file)!!
            var line = fileLine.line - 1

            val mapFilePath = virtualFile.path + ".map"
            if (File(mapFilePath).exists()) {
                val mapping = sourceMappings.getCached(mapFilePath)
                val originalMapping = mapping.getMappingForLine(fileLine.line, 1)
                if (originalMapping != null) {
                    val originalFilePath = originalMapping.originalFile.replace(Regex("^file://"), "")
                    val originalVirtualFile = IDEAPile.getVirtualFileByPath(originalFilePath)
                    if (originalVirtualFile == null) {
                        wtfBalloon("50f8d771-a7ff-4c42-9ac9-aa0326529e70")
                    } else {
                        virtualFile = originalVirtualFile
                        line = originalMapping.lineNumber - 1
                    }
                }
            }

            frames += MyStackFrame(virtualFile, line)
        }

        var framesToShow = frames
        if (stepMode == StepMode.Kotlin) {
            if (standingInsidePhiExpressionStatement()) {
                framesToShow = frames.drop(1)
            }
        }

        val executionStack = object : XExecutionStack("Pizda") {
            override fun getTopFrame(): XStackFrame? {
                return framesToShow.first()
            }

            override fun computeStackFrames(firstFrameIndex: Int, container: XStackFrameContainer) {
                container.addStackFrames(framesToShow, true)
            }
        }

        val suspendContext: XSuspendContext = object : XSuspendContext() {
            override fun getActiveExecutionStack(): XExecutionStack? {
                return executionStack
            }
        }

        ideaDebugSession.positionReached(suspendContext)
    }

    private fun standingInsidePhiExpressionStatement(): Boolean {
        val first = frames.first()
        return first.virtualFile.name == "phi-engine.php" && first.line == phiExpressionStatementBreakpointLineZeroBased
    }

    fun getCurrentStack(): List<FileLine>? {
        val response = dbgpSession.sendSyncCmd(DBGpCommand.stackGet) ?: run {
            wtfBalloon("96e254f7-ef02-4d1b-8abb-0a45446662ce")
            return null
        }

        if (response.status == DBGpResponse.STATUS_STOPPED) {
            noise("Received STATUS_STOPPED when trying to get the stack -- disposing shit")
            disposeShit()
            return null
        }

        return mutableListOf<FileLine>()-{res->
            val nodes = response.parentNode.childNodes
            for (i in 0 until nodes.length) {
                val stackData = nodes.item(i)
                val line = DBGpResponse.getAttribute(stackData, "lineno")
                val lineno = try {
                    Integer.parseInt(line)}
                catch (nfe: NumberFormatException) {
                    wtfBalloon("96e254f7-ef02-4d1b-8abb-0a45446662ce")
                    return null
                }

                val filename = DBGpUtils.getFilenameFromURIString(DBGpResponse.getAttribute(stackData, "filename"))
                res += FileLine(filename, lineno)
            }
        }
    }

    fun dbgpSessionSays_breakpointHit() {
        noise("dbgpSessionSays_breakpointHit")

        fuckingSuspended()

//        val fileLine = getCurrentFileLine() ?: run {
//            clog("45dc483a-1975-4f13-ae20-0ae7e80cde9d")
//            return
//        }
//
//        val xbreakpoint = phpLineBreakpoints.find {it.phpLine == fileLine.line}?.xbreakpoint
//            ?: return wtfBalloon("f86f657c-2f0e-4b61-8253-086fe33ce6c0")
//
//        val frame = object:XStackFrame() {
//            override fun getSourcePosition(): XSourcePosition {
//                return xbreakpoint.getSourcePosition()!!
//            }
//        }
//
//        val executionStack = object:XExecutionStack("Pizda") {
//            override fun getTopFrame(): XStackFrame? {
//                return frame
//            }
//
//            override fun computeStackFrames(firstFrameIndex: Int, container: XStackFrameContainer) {
//            }
//        }
//
//        val suspendContext: XSuspendContext = object : XSuspendContext() {
//            override fun getActiveExecutionStack(): XExecutionStack? {
//                return executionStack
//            }
//        }
//        ideaDebugSession.breakpointReached(xbreakpoint, null, suspendContext)
    }

    fun dbgpSessionSays_sessionEnded() {
        noise("dbgpSessionSays_sessionEnded")
        disposeShit()
    }

    private fun disposeShit() {
        DBGpSessionHandler.getInstance().removeSessionListener(sessionListener)
        daemon.stopListen()
        instance = null

        ApplicationManager.getApplication().invokeLater {
            Messages.showInfoMessage("It's over. Completely fucking over...", "Yeah")
        }
    }

    fun noise(s: String) {
        clog("NOISE: " + s)
    }

    private fun wtfBalloon(message: String) {
        ApplicationManager.getApplication().invokeLater {
            IDEAPile.showErrorBalloonForDebugToolWindow(project, "WTF: " + message)
        }
    }
}






