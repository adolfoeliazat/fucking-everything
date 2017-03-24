package vgrechka.idea

import com.intellij.execution.ExecutionManager
import com.intellij.execution.Executor
import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.actions.ChooseDebugConfigurationPopupAction
import com.intellij.execution.actions.ChooseRunConfigurationPopup
import com.intellij.execution.actions.ExecutorProvider
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.impl.ConsoleViewUtil
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.execution.ui.*
import com.intellij.execution.ui.actions.CloseAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.unscramble.AnnotateStackTraceAction
import java.awt.BorderLayout
import java.io.*
import javax.swing.JPanel
import javax.xml.bind.JAXBContext
import kotlin.concurrent.thread
import vgrechka.*
import java.io.PrintWriter
import java.io.StringWriter

val Project.bullshitter by AttachedComputedShit(::Bullshitter)

class Bullshitter(val project: Project) {
    private val consoleView: ConsoleView by relazy {
        var newConsoleView by notNullOnce<ConsoleView>()

        ApplicationManager.getApplication().invokeAndWait {
            clog("Creating bullshitter for project ${project.name}")

            val builder = TextConsoleBuilderFactory.getInstance().createBuilder(project)
            newConsoleView = builder.console

            val toolbarActions = DefaultActionGroup()
            val consoleComponent = MyConsolePanel(newConsoleView, toolbarActions)
            val bullshitterDescr = object : RunContentDescriptor(newConsoleView, null, consoleComponent, "Bullshitter", null) {
                override fun isContentReuseProhibited(): Boolean {
                    return true
                }
            }

            val executor = DefaultRunExecutor.getRunExecutorInstance()
            for (action in newConsoleView.createConsoleActions()) {
                toolbarActions.add(action)
            }
            val console = newConsoleView as ConsoleViewImpl
            ConsoleViewUtil.enableReplaceActionForConsoleViewEditor(console.editor)
            console.editor.settings.isCaretRowShown = true
            toolbarActions.add(AnnotateStackTraceAction(console.editor, console.hyperlinks))
            toolbarActions.add(CloseAction(executor, bullshitterDescr, project))
            ExecutionManager.getInstance(project).contentManager.showRunContent(executor, bullshitterDescr)
            newConsoleView.allowHeavyFilters()

            ExecutionManager.getInstance(project).contentManager

            val con = project.messageBus.connect()
            con.subscribe(RunContentManager.TOPIC, object:RunContentWithExecutorListener {
                override fun contentSelected(descriptor: RunContentDescriptor?, executor: Executor) {
//                if (descriptor != null) {
//                    clog("Shit selected", descriptor.displayName)
//                }
                }

                override fun contentRemoved(descriptor: RunContentDescriptor?, executor: Executor) {
                    if (descriptor == bullshitterDescr) {
                        clog("Bullshitter for project ${project.name} was removed")
                        con.disconnect()
                        relazy.reset(this@Bullshitter::consoleView)
                    }
                }
            })
        }

        newConsoleView
    }


    fun mumble(s: String) {
        consoleView.print(s + "\n", ConsoleViewContentType.NORMAL_OUTPUT)
    }

    fun bark(s: String) {
        consoleView.print(s + "\n", ConsoleViewContentType.ERROR_OUTPUT)
    }

    fun bark(e: Throwable) {
        e.printStackTrace()
        val stringWriter = StringWriter()
        e.printStackTrace(PrintWriter(stringWriter))
        bark(stringWriter.toString())
    }

    private class MyConsolePanel(consoleView: ExecutionConsole, toolbarActions: ActionGroup) : JPanel(BorderLayout()) {
        init {
            val toolbarPanel = JPanel(BorderLayout())
            toolbarPanel.add(ActionManager.getInstance()
                                 .createActionToolbar("PLACE?", toolbarActions, false)
                                 .component)
            add(toolbarPanel, BorderLayout.WEST)
            add(consoleView.component, BorderLayout.CENTER)
        }
    }

}















//class Fuck {
//
//    fun init(project: Project) {
//        initRPCServer()
//        mumble("Fuck you")
//    }
//
//    private fun initRPCServer() {
//        val server = RPCServer()
//        server.spin()
//    }
//
//
//
//    private inner class RPCServer {
//        fun spin() {
////            val f = lastRPCMessageFile()
////            if (f.exists()) {
////                handleRPCMessage(f.readText())
////            }
////
////            thread {
////                try {
////                    Server(PhotlinGlobus.serverPort)-{o->
////                        o.handler = ServletHandler()-{o->
////                            o.addServletWithMapping(ServletHolder(FuckingServlet()), "/*")
////                        }
////                        o.start()
////                        mumble("Shit is spinning")
////                        o.join()
////                    }
////                } catch(e: Exception) {
////                    bark(e)
////                }
////            }
//        }
//
////        inner class FuckingServlet : HttpServlet() {
////            override fun service(req: HttpServletRequest, res: HttpServletResponse) {
////                req.characterEncoding = "UTF-8"
////                val raw = req.reader.readText()
////                handleRPCMessage(raw)
////            }
////        }
//    }
//
////    private fun handleRPCMessage(raw: String) {
////        lastRPCMessageFile().writeText(raw)
////
////        val jaxbContext = JAXBContext.newInstance(TestResultMessage::class.java)
////        val unmarshaller = jaxbContext.createUnmarshaller()
////        val msg = unmarshaller.unmarshal(StringReader(raw)) as TestResultMessage
////        mumble("Got test result")
////        mumble("---------------")
////        mumble(msg.responseText)
////    }
//
////    private fun lastRPCMessageFile() = File(PhotlinGlobus.tempDir + "/last-rpc-message")
//
//}

