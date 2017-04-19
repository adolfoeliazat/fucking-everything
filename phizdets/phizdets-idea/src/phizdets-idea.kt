package vgrechka.phizdetsidea

import com.intellij.debugger.DebuggerManager
import com.intellij.execution.ExecutionException
import com.intellij.ide.actions.ShowStructureSettingsAction
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.options.ex.SingleConfigurableEditor
import com.intellij.openapi.options.newEditor.SettingsDialog
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.pom.Navigatable
import com.intellij.pom.NonNavigatable
import com.intellij.util.PlatformUtils
import com.intellij.util.lang.UrlClassLoader
import com.intellij.util.ui.UIUtil
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XDebuggerUtil
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
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
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.IDebugEventSetListener
import org.eclipse.php.internal.debug.core.xdebug.communication.XDebugCommunicationDaemon
import org.eclipse.php.internal.debug.core.xdebug.dbgp.DBGpLogger
import org.eclipse.php.internal.debug.core.xdebug.dbgp.model.DBGpTarget
import org.eclipse.php.internal.debug.core.xdebug.dbgp.protocol.DBGpCommand
import org.eclipse.php.internal.debug.core.xdebug.dbgp.protocol.DBGpResponse
import org.eclipse.php.internal.debug.core.xdebug.dbgp.protocol.DBGpUtils
import org.eclipse.php.internal.debug.core.xdebug.dbgp.session.DBGpSession
import org.eclipse.php.internal.debug.core.xdebug.dbgp.session.DBGpSessionHandler
import org.eclipse.php.internal.debug.core.xdebug.dbgp.session.IDBGpSessionListener
import org.eclipse.php.internal.debug.daemon.DaemonPlugin
import org.osgi.framework.Bundle
//import phizdets.MapPhizdetsStack
import vgrechka.*
import vgrechka.idea.*
import java.io.File
import kotlin.concurrent.thread
import vgrechka.*
import vgrechka.phizdetsidea.phizdets.debugger.*
import java.awt.MouseInfo
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
        dbgpSend(DBGpCommand.stepOver)
    }

    fun ideaSays_stepInto() {
        when (stepMode) {
            XDebugDaemonAndShit.StepMode.Kotlin -> {
//                dbgpSend(DBGpCommand.breakPointSet, "" +
//                    "-t call" +
//                    " -m phiExpressionStatement" +
//                    " -- base64(false)") || return

                dbgpSend(DBGpCommand.eval, "" +
                    "-- ${base64Encode("@\$GLOBALS['phiExpressionStatement_counter'] = 0;")}"
                ) || return

                dbgpSend(DBGpCommand.breakPointSet, "" +
                    "-t line" +
                    " -f E:/fegh/aps/aps-back-phi/out/production/aps-back-phi/phi-engine.php" +
                    " -n 2130" +
                    " -- ${base64Encode("@\$GLOBALS['phiExpressionStatement_counter'] === 1")}") || return

                dbgpSend(DBGpCommand.run) || return
                // TODO:vgrechka Unhardcode line
                // TODO:vgrechka Remove function breakpoint

            }

            XDebugDaemonAndShit.StepMode.PHP -> {
                dbgpSend(DBGpCommand.stepInto)
            }
        }
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

    private fun fuckingSuspended() {
        val filesLines = getCurrentStack() ?: run {
            clog("2e35e98d-1a4c-4b53-bbf4-331aebe842e5")
            return
        }

        val frames = mutableListOf<XStackFrame>()
        for (fileLine in filesLines) {
            var virtualFile = IDEAStuff.getVirtualFileByPath(fileLine.file)!!
            var line = fileLine.line - 1

            val mapFilePath = virtualFile.path + ".map"
            if (File(mapFilePath).exists()) {
                val mapping = sourceMappings.getCached(mapFilePath)
                val originalMapping = mapping.getMappingForLine(fileLine.line, 999999)
                if (originalMapping != null) {
                    val originalFilePath = originalMapping.originalFile.replace(Regex("^file://"), "")
                    val originalVirtualFile = IDEAStuff.getVirtualFileByPath(originalFilePath)
                    if (originalVirtualFile == null) {
                        wtfBalloon("50f8d771-a7ff-4c42-9ac9-aa0326529e70")
                    } else {
                        virtualFile = originalVirtualFile
                        line = originalMapping.lineNumber - 1
                    }
                }
            }

            val frame = object : XStackFrame() {
                override fun getSourcePosition(): XSourcePosition? {
                    return XDebuggerUtil.getInstance().createPosition(virtualFile, line)
                }
            }

            frames += frame
        }

        val executionStack = object : XExecutionStack("Pizda") {
            override fun getTopFrame(): XStackFrame? {
                return frames.first()
            }

            override fun computeStackFrames(firstFrameIndex: Int, container: XStackFrameContainer) {
                container.addStackFrames(frames, true)
            }
        }

        val suspendContext: XSuspendContext = object : XSuspendContext() {
            override fun getActiveExecutionStack(): XExecutionStack? {
                return executionStack
            }
        }

        ideaDebugSession.positionReached(suspendContext)
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
            IDEAStuff.showErrorBalloonForDebugToolWindow(project, "WTF: " + message)
        }
    }
}






