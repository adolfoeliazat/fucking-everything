package vgrechka.phizdetsidea

import com.intellij.debugger.DebuggerManager
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
import com.intellij.openapi.ui.Messages
import com.intellij.util.PlatformUtils
import com.intellij.util.lang.UrlClassLoader
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import com.intellij.xdebugger.impl.breakpoints.XBreakpointManagerImpl
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl
import com.sun.jna.platform.win32.User32
import org.eclipse.php.internal.debug.core.xdebug.communication.XDebugCommunicationDaemon
import org.eclipse.php.internal.debug.daemon.DaemonPlugin
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
import kotlin.properties.Delegates.notNull

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

                val daemon = XDebugCommunicationDaemon()
                daemon.init()
                daemon.startListen()
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
                    val mapping = SourceMappingCache.getMapping("E:\\fegh\\aps\\aps-back-phi\\out\\production\\aps-back-phi\\aps-back-phi.php.map")
                    val penetration = mapping.penetration
                    penetration.dumpSourceLineToGeneratedLine()
                    "break on me"
                }

                private fun testPreparePHPDebugger() {
                    preparePHPDebugger()
                }

                fun preparePHPDebugger(): Boolean {
                    val mapping = SourceMappingCache.getMapping("E:\\fegh\\aps\\aps-back-phi\\out\\production\\aps-back-phi\\aps-back-phi.php.map")
                    val bm = XDebuggerManager.getInstance(event.project!!).breakpointManager as XBreakpointManagerImpl
                    for (point in bm.allBreakpoints) {
                        if (point is XLineBreakpointImpl) {
                            if (point.type.id == "kotlin-line") {
                                val fileLine = FileLine(point.fileUrl, point.line + 1)
                                val generatedLine = mapping.penetration.sourceFileLineToGeneratedLine[fileLine] ?: run {
                                    Messages.showErrorDialog("No fucking mapping for $fileLine", "Fuck You")
                                    return false
                                }
                                clog("Setting breakpoint at line $generatedLine <-- $fileLine")
                            }
                        }
                    }
                    Messages.showInfoMessage("Fuck, yeah", "Cool")
                    return true
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







