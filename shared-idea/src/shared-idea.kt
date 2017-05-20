package vgrechka.idea

import com.intellij.execution.ExecutionManager
import com.intellij.execution.Executor
import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.actions.ChooseRunConfigurationPopup
import com.intellij.execution.actions.ExecutorProvider
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.impl.ConsoleViewUtil
import com.intellij.execution.impl.ExecutionManagerImpl
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.execution.ui.*
import com.intellij.execution.ui.actions.CloseAction
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.ex.StatusBarEx
import com.intellij.unscramble.AnnotateStackTraceAction
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import javax.swing.JPanel
import vgrechka.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

//val Project.bullshitter by AttachedComputedShit(::Bullshitter)

class Bullshitter(val project: Project, val title: String? = null) {
    val consoleView: ConsoleView by relazy {
        var newConsoleView by notNullOnce<ConsoleView>()

        ApplicationManager.getApplication().invokeAndWait {
            val bullshitterID = UUID.randomUUID().toString()
            clog("Creating bullshitter for project ${project.name}: $bullshitterID")

            val builder = TextConsoleBuilderFactory.getInstance().createBuilder(project)
            newConsoleView = builder.console

            val toolbarActions = DefaultActionGroup()
            val consoleComponent = MyConsolePanel(newConsoleView, toolbarActions)
            val bullshitterDescr = object : RunContentDescriptor(newConsoleView, null, consoleComponent, title ?: "Bullshitter", null) {
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
                    if (descriptor == bullshitterDescr) {
                        clog("Bullshitter selected: $bullshitterID")
                    }
                }

                override fun contentRemoved(descriptor: RunContentDescriptor?, executor: Executor) {
                    if (descriptor == bullshitterDescr) {
                        clog("Bullshitter removed: $bullshitterID")
                        con.disconnect()
                        relazy.reset(this@Bullshitter::consoleView)
                    }
                }
            })
        }

        newConsoleView
    }


    fun mumble(s: String) {
        mumbleNoln(s + "\n")
    }

    fun mumbleNoln(s: String) {
        consoleView.print(s, ConsoleViewContentType.NORMAL_OUTPUT)
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

fun openFile(project: Project, path: String, line: Int): Boolean {
    val file = LocalFileSystem.getInstance().findFileByPath(path)
        ?: return false
    NonProjectFileWritingAccessProvider.allowWriting(file)

    val descriptor = OpenFileDescriptor(project, file)
    val editor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true)
        ?: bitch("f1924b93-fc47-4c88-8d7a-75c94e67e481")

    val position = LogicalPosition(line - 1, 0)
    editor.caretModel.removeSecondaryCarets()
    editor.caretModel.moveToLogicalPosition(position)
    editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
    editor.selectionModel.removeSelection()
    IdeFocusManager.getGlobalInstance().requestFocus(editor.contentComponent, true)
    return true
}

object IDEAPile {
    fun showErrorBalloonForDebugToolWindow(project: Project, fullMessage: String) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindowId = ToolWindowId.DEBUG
        if (toolWindowManager.canShowNotification(toolWindowId)) {
            toolWindowManager.notifyByBalloon(toolWindowId, MessageType.ERROR, fullMessage, null, null)
        } else {
            Messages.showErrorDialog(project, UIUtil.toHtml(fullMessage), "")
        }
    }

    fun getVirtualFileByPath(path: String): VirtualFile? {
        return LocalFileSystem.getInstance().findFileByPath(path)
    }

    fun runConfiguration(project: Project, configurationName: String, debug: Boolean = false) {
        later {
            val res = getRunningContentDescriptors(project, configurationName, debug)
            val candy = when (res) {
                is IDEAPile.GetRunningDescriptorsResult.Poop -> bitch(res.error)
                is IDEAPile.GetRunningDescriptorsResult.Candy -> res
            }

            if (candy.descriptors.isNotEmpty()) {
                ExecutionUtil.restart(candy.descriptors.first())
            } else {
                ExecutionUtil.runConfiguration(candy.config, getRunExecutor(debug))
            }
        }
    }

    sealed class GetRunningDescriptorsResult {
        class Poop(val error: String) : GetRunningDescriptorsResult()
        class Candy(val config: RunnerAndConfigurationSettings,
                    val descriptors: List<RunContentDescriptor>) : GetRunningDescriptorsResult()
    }

    fun getRunningContentDescriptorsFromNonUIThread(project: Project, configurationName: String, debug: Boolean = false): GetRunningDescriptorsResult {
        val queue = ArrayBlockingQueue<GetRunningDescriptorsResult>(1)
        later {
            val res = getRunningContentDescriptors(project, configurationName, debug)
            queue.add(res)
        }
        return queue.poll(1, TimeUnit.SECONDS) ?: bitch("Sick of waiting for running content descriptors from UI thread")
    }

    fun getRunningContentDescriptors(project: Project, configurationName: String, debug: Boolean = false): GetRunningDescriptorsResult {
        val executor = getRunExecutor(debug)
        val executorProvider = ExecutorProvider {executor}
        val list = ChooseRunConfigurationPopup.createSettingsList(project, executorProvider, false)
        for (item in list) {
            val config = item.value
            if (config is RunnerAndConfigurationSettings) {
                if (config.name == configurationName) {
                    val runningDescriptors = ExecutionManagerImpl.getInstance(project).getRunningDescriptors {it == config}
                    return GetRunningDescriptorsResult.Candy(config, runningDescriptors)
                }
            }
        }

        return GetRunningDescriptorsResult.Poop("No fucking run configuration `$configurationName` in `${project.name}`")
    }

    private fun getRunExecutor(debug: Boolean): Executor {
        val executorToolWindowID = when {
            debug -> ToolWindowId.DEBUG
            else -> ToolWindowId.RUN
        }
        val executor = ExecutorRegistry.getInstance().getExecutorById(executorToolWindowID)
        return executor
    }

    fun errorDialog(msg: String) {
        later {Messages.showErrorDialog(msg, "Shit Didn't Work")}
    }

    fun errorDialog(e: Throwable) {
        later {Messages.showErrorDialog(e.stackTraceString, "Shit Didn't Work")}
    }

    fun infoDialog(message: String) {
        later {Messages.showInfoMessage(message, "Read This Shit")}
    }

    fun showingErrorOnException(block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable ) {
            IDEAPile.errorDialog(e)
        }
    }

    fun later(block: () -> Unit) {
        ApplicationManager.getApplication().invokeLater(block)
    }

    class WaitRunConfigurationStatus(val project: Project, val configurationName: String, val debug: Boolean) {
        val pollInterval = 100L

        class Sick(msg: String) : Exception(msg)

        fun loopUntilTrueOrTimeout(timeout: Int, test: () -> Boolean): Boolean {
            // We don't want accidental infinite loop due to some bug in return logic, as that will render whole IDE hosed.
            // Approximate timeout is OK
            val numIterations = timeout / pollInterval
            check(numIterations in 1..100) {"fe6fbc28-db08-4754-a4b9-26b39cb7c5f2"}
            for (i in 1..numIterations) {
                Thread.sleep(pollInterval)
                if (test())
                    return true
            }
            return false
        }

        fun wait(timeout: Int, errorMessage: String, condition: (List<RunContentDescriptor>) -> Boolean) {
            val ok = loopUntilTrueOrTimeout(timeout) {
                val descriptors = (getRunningContentDescriptorsFromNonUIThread(project, configurationName, debug)
                    as? GetRunningDescriptorsResult.Candy
                    ?: bitch("d687999f-5597-45da-a282-637976a0bac4"))
                    .descriptors
                condition(descriptors)
            }
            if (!ok)
                throw Sick(errorMessage)
        }
    }

    /**
     * Don't call this on UI thread
     * @throws something if failed
     */
    fun waitForConfigurationToRunAndThenTerminate(project: Project, configurationName: String, debug: Boolean, runTimeout: Int, terminationTimeout: Int) {
        val wrcs = WaitRunConfigurationStatus(project, configurationName, debug)
        wrcs.wait(runTimeout, "Sick of waiting for this shit to run: $configurationName") {it.isNotEmpty()}
        wrcs.wait(terminationTimeout, "Sick of waiting for this shit to terminate: $configurationName") {it.isEmpty()}
    }

    /**
     * Don't call this on UI thread
     * @throws something if failed
     */
    fun waitForConfigurationToTerminateAndThenRun(project: Project, configurationName: String, debug: Boolean, runTimeout: Int, terminationTimeout: Int) {
        val wrcs = WaitRunConfigurationStatus(project, configurationName, debug)
        wrcs.wait(terminationTimeout, "Sick of waiting for this shit to terminate: $configurationName") {it.isEmpty()}
        wrcs.wait(runTimeout, "Sick of waiting for this shit to run: $configurationName") {it.isNotEmpty()}
    }

    fun showProgressBalloon(project: Project?, messageType: MessageType, message: String) {
        IDEAPile.later {
            val ideFrame = WindowManager.getInstance().getIdeFrame(project)
            if (ideFrame != null) {
                val statusBar = ideFrame.statusBar as StatusBarEx
                statusBar.notifyProgressByBalloon(messageType, message, null, null)
            }
        }
    }

    fun revealingException(showInBalloon: ((Throwable) -> Boolean)? = null, block: () -> Unit) {
        val theShowInBalloon = showInBalloon ?: {false}

        try {
            block()
        } catch(e: Throwable) {
            if (theShowInBalloon(e))
                showProgressBalloon(null, MessageType.ERROR, e.message ?: "No fucking message")
            else
                IDEAPile.later {IDEAPile.errorDialog(e)}
        }
    }

    fun threadRevealingException(showInBalloon: ((Throwable) -> Boolean)? = null, block: () -> Unit) {
        thread {
            revealingException(showInBalloon, block)
        }
    }

}



















