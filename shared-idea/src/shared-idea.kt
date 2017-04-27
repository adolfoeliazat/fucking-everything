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
import com.intellij.unscramble.AnnotateStackTraceAction
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import javax.swing.JPanel
import vgrechka.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

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

object IDEAStuff {
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

    fun debugConfiguration(project: Project, configurationName: String) {
        val executor = ExecutorRegistry.getInstance().getExecutorById(ToolWindowId.DEBUG)
        val executorProvider = ExecutorProvider {executor}
        val list = ChooseRunConfigurationPopup.createSettingsList(project, executorProvider, false)
        for (item in list) {
            val config = item.value
            if (config is RunnerAndConfigurationSettings) {
                if (config.name == configurationName) {
                    val runningDescriptors = ExecutionManagerImpl.getInstance(project).getRunningDescriptors {it == config}
                    if (runningDescriptors.size > 0) {
                        ExecutionUtil.restart(runningDescriptors.first())
                    } else {
                        ExecutionUtil.runConfiguration(config, executor)
                    }
                    return
                }
            }
        }
        bitch("No fucking debug configuration `$configurationName` in `${project.name}`")
    }

    fun errorDialog(e: Throwable) {
        Messages.showErrorDialog(e.stackTraceStr, "Shit Didn't Work")
    }

    fun infoDialog(message: String) {
        Messages.showInfoMessage(message, "Read This Shit")
    }
}



















