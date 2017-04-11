@file:Suppress("Unused")
package vgrechka.idea.hripos

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.intellij.execution.ExecutionManager
import com.intellij.execution.Executor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.impl.ConsoleViewUtil
import com.intellij.execution.ui.*
import com.intellij.execution.ui.actions.CloseAction
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.WindowManager
import com.intellij.project.stateStore
import com.intellij.unscramble.AnnotateStackTraceAction
import com.intellij.util.containers.ConcurrentIntObjectMap
import org.jetbrains.kotlin.utils.stackTraceStr
import java.awt.BorderLayout
import javax.swing.JPanel
import vgrechka.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import vgrechka.*
import vgrechka.idea.*
import java.awt.Frame
import java.util.*
import javax.swing.JFrame
import kotlin.properties.Delegates.notNull
import kotlin.reflect.KFunction0

object MumblingCommandGlobal {
    var dontUseIDEAAndShitToStdoutInstead = false
}

interface Mumbler {
    fun scrollToEnd()
    fun barkNoln(s: String)
    fun bark(s: String)
    fun mumbleNoln(s: String)
    fun mumble(s: String)
    fun link(text: String, path: String, line: Int)
    fun toFront()
}

fun serveMumblingCommand(projectName: String, doUsefulShit: (Mumbler) -> Unit): Any {
    if (MumblingCommandGlobal.dontUseIDEAAndShitToStdoutInstead) {
        doUsefulShit(stdoutMumbler)
        return "blah-blah-blah"
    }

    return try {
        withProjectNamed(projectName) {project ->
            val title = "Moderate Bullshit"

            //        val p = HriposDebugOutput(project)
            //        p.showDialog(title = title)

            val mumbler = object:Mumbler {
                val bs: Any = run {
                    // FuckingUtils.noisy = true
                    FuckingUtils.aLittleNonGCableAbomination(
                        project = project,
                        id = "moderate-bullshit",
                        version = 1,
                        make = {MyBullshitter(project, title = title)})
                        ?: bitch("No bullshitter")
                }

                override fun mumble(s: String) {
                    val m = bs.javaClass.getMethod("mumble", String::class.java)
                    m.isAccessible = true
                    m.invoke(bs, s)
                }

                override fun bark(s: String) {
                    val m = bs.javaClass.getMethod("bark", String::class.java)
                    m.isAccessible = true
                    m.invoke(bs, s)
                }

                override fun mumbleNoln(s: String) {
                    val m = bs.javaClass.getMethod("mumbleNoln", String::class.java)
                    m.isAccessible = true
                    m.invoke(bs, s)
                }

                override fun barkNoln(s: String) {
                    val m = bs.javaClass.getMethod("barkNoln", String::class.java)
                    m.isAccessible = true
                    m.invoke(bs, s)
                }

                override fun link(text: String, path: String, line: Int) {
                    getConsoleViewImpl().printHyperlink(text) {project ->
                        if (!openFile(project, path, line)) {
                            FuckingUtils.error("No fucking way")
                        }
                    }
                }

                override fun scrollToEnd() {
                    getConsoleViewImpl().scrollToEnd()
                }

                private fun getConsoleViewImpl(): ConsoleViewImpl {
                    val m = bs.javaClass.getDeclaredMethod("getConsoleView")
                    m.isAccessible = true
                    return m.invoke(bs) as ConsoleViewImpl
                }

                override fun toFront() {
                    val m = bs.javaClass.getMethod("toFront")
                    m.isAccessible = true
                    m.invoke(bs)
                }
            }

            mumbler.toFront()
            mumbler.mumble("\n----- Hello, honey. It's ${Date()} -----\n")

            doUsefulShit(mumbler)
            mumbler.scrollToEnd()

            val f = WindowManager.getInstance().getFrame(project)
            if (f != null) {
                f.state = JFrame.NORMAL
            }
            // ProjectUtil.focusProjectWindow(project, true)

            ApplicationManager.getApplication().invokeLater {
                FuckingUtils.info("Received some shit...")
            }
        }
        "Astonishing success"
    } catch (e: Exception) {
        Messages.showErrorDialog(e.stackTraceStr, "Shit Didn't Work")
        "Bloody error"
    }
}

private val stdoutMumbler by lazy {object:Mumbler {
    override fun scrollToEnd() {}
    override fun toFront() {}

    override fun barkNoln(s: String) {
        System.err.print(s)
    }

    override fun bark(s: String) {
        System.err.println(s)
    }

    override fun mumbleNoln(s: String) {
        System.out.print(s)
    }

    override fun mumble(s: String) {
        System.out.println(s)
    }

    override fun link(text: String, path: String, line: Int) {
        mumbleNoln("<<$text --> $path:$line>>")
    }
}}

private class MyBullshitter(val project: Project, val title: String? = null) {
    private var executor: Executor? = null // by notNull<Executor>()
    private var bullshitterDescr: RunContentDescriptor? = null // by notNull<RunContentDescriptor>()

    val consoleView: ConsoleView by relazy {
        var newConsoleView by notNullOnce<ConsoleView>()

        ApplicationManager.getApplication().invokeAndWait {
            val bullshitterID = UUID.randomUUID().toString()
            clog("Creating bullshitter for project ${project.name}: $bullshitterID")

            val builder = TextConsoleBuilderFactory.getInstance().createBuilder(project)
            newConsoleView = builder.console

            val toolbarActions = DefaultActionGroup()
            val consoleComponent = MyConsolePanel(newConsoleView, toolbarActions)
            bullshitterDescr = object : RunContentDescriptor(newConsoleView, null, consoleComponent, title ?: "Bullshitter", null) {
                override fun isContentReuseProhibited(): Boolean {
                    return true
                }
            }

            executor = DefaultRunExecutor.getRunExecutorInstance()
            // FuckingUtils.noise("executor =" + executor)
            for (action in newConsoleView.createConsoleActions()) {
                toolbarActions.add(action)
            }
            val console = newConsoleView as ConsoleViewImpl
            ConsoleViewUtil.enableReplaceActionForConsoleViewEditor(console.editor)
            console.editor.settings.isCaretRowShown = true
            toolbarActions.add(AnnotateStackTraceAction(console.editor, console.hyperlinks))
            toolbarActions.add(CloseAction(executor, bullshitterDescr, project))
            ExecutionManager.getInstance(project).contentManager.showRunContent(executor!!, bullshitterDescr!!)
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
                        relazy.reset(this@MyBullshitter::consoleView)
                    }
                }
            })
        }

        newConsoleView
    }

    fun toFront() {
        consoleView // Init the bitch
        ExecutionManager.getInstance(project).contentManager.toFrontRunContent(executor!!, bullshitterDescr!!)
    }

    fun mumble(s: String) {
        mumbleNoln(s + "\n")
    }

    fun mumbleNoln(s: String) {
        consoleView.print(s, ConsoleViewContentType.NORMAL_OUTPUT)
    }

    fun bark(s: String) {
        barkNoln(s + "\n")
    }

    fun barkNoln(s: String) {
        consoleView.print(s, ConsoleViewContentType.ERROR_OUTPUT)
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

object FuckingUtils {
    var noisy = false

    fun aLittleNonGCableAbomination(project: Project, id: String, version: Int, make: () -> Any): Any? {
        try {
            val fullKeyName = id + version
            var key: Key<Any?>? = null

            val allKeys = run {
                val f = Key::class.java.getDeclaredField("allKeys")
                f.isAccessible = true
                f.get(null) as ConcurrentIntObjectMap<Key<*>>
            }
            noise("${allKeys.size()} keys")

            val keysToRemove = mutableListOf<Key<*>>()
            val iterator = allKeys.entries().iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val existingKey = entry.value
                val existingKeyName = getKeyName(existingKey) ?: continue

                if (existingKeyName.startsWith(id) && existingKeyName != fullKeyName) {
                    keysToRemove += entry.value
                }
                else if (fullKeyName == existingKeyName) {
                    key = existingKey as Key<Any?>
                    // noise("Found key " + key)
                }
            }

            if (keysToRemove.isNotEmpty()) {
                for (x in keysToRemove) {
                    allKeys.remove(getKeyIndex(x)!!)
                }
                noise("Removed ${keysToRemove.size} old motherfuckers, ${allKeys.size()} left in map")
            }

            val motherfucker = if (key == null) {
                noise("Creating new motherfucker")
                key = Key<Any?>(fullKeyName)
                val newMotherfucker = make()
                val data = mutableListOf(key, newMotherfucker)
                project.putUserData(key, data)
                newMotherfucker
            }
            else {
                noise("Found suitable existing motherfucker")
                (project.getUserData(key) as MutableList<*>)[1]!!
            }

            noise("Cool")
            return motherfucker
        }
        catch (e: Exception) {
            error(e.stackTraceStr)
            return null
        }
    }

    fun error(s: String) {
        Messages.showErrorDialog(s, "Shit Didn't Work")
    }

    fun noise(s: String) {
        if (noisy) {
            Messages.showInfoMessage(s, "Noise")
        }
    }

    fun info(s: String) {
        Messages.showInfoMessage(s, "Info")
    }

    private fun getKeyIndex(key: Key<*>): Int? {
        return try {
            val f = key.javaClass.getDeclaredField("myIndex")
            f.isAccessible = true
            f.get(key) as Int
        } catch (e: NoSuchFieldException) {
            null
        }
    }

    private fun getKeyName(key: Key<*>): String? {
        return try {
            val f = key.javaClass.getDeclaredField("myName")
            f.isAccessible = true
            f.get(key) as String
        } catch (e: NoSuchFieldException) {
            null
        }
    }

}


