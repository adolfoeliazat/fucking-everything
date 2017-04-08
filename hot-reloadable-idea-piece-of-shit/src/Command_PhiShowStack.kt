package vgrechka.idea.hripos

import com.intellij.execution.ExecutionManager
import com.intellij.execution.Executor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.impl.ConsoleViewUtil
import com.intellij.execution.ui.*
import com.intellij.execution.ui.actions.CloseAction
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Key
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
import java.util.*

@Ser @Suppress("Unused")
class Command_PhiShowStack(val projectName: String, val stack: List<Map<String, Any?>>) : Servant {
    lateinit var bs: Any

    override fun serve() = withProjectNamed(projectName) {project->
        val title = this::class.simpleName!!.substring("Command_".length)

        val p = HriposDebugOutput(project)
//        for (item in stack.reversed().drop(1)) {
//            val file = item["file"].toString()
//            val line = item["line"].toString().toInt()
//            if (file.contains("aps-back.php")) {
//                p.println(file + " -- " + line)
//            }
//        }
//        p.showDialog(title = title)

        try {
            bs = FuckingUtils.aLittleNonGCableAbomination(
                project = project,
                id = "Command_PhiShowStack.bullshitter",
                version = 4,
                make = {MyBullshitter(project, title = title)})
                ?: bitch("No bullshitter")

            mumble("Hello, honey. It's ${Date()}")

            FuckingUtils.noisy = true
            val consoleViewImpl = run {
                val m = bs.javaClass.getDeclaredMethod("getConsoleView")
                m.isAccessible = true
                m.invoke(bs)
            }
            FuckingUtils.noise("Got ConsoleViewImpl " + consoleViewImpl)

            FuckingUtils.info("Good")
            "Astonishing success"
        }
        catch (e: Exception) {
            Messages.showErrorDialog(e.stackTraceStr, "Shit Didn't Work")
            "Bloody error"
        }
    }


    fun mumble(s: String) {
        val m = bs.javaClass.getMethod("mumble", String::class.java)
        m.isAccessible = true
        m.invoke(bs, s)
    }
}

object Command_PhiShowStackTest {
    @JvmStatic
    fun main(args: Array<String>) {
        HTTPClient.postJSON("http://localhost:12312?proc=PhiShowStack", json)
    }

    val json = """
{
    "projectName": "fegh",
    "stack": [
        {
            "function": "{main}",
            "file": "\/media\/sf_phizdetsc-php\/try-shit--aps-back.php",
            "line": 0,
            "params": []
        },
        {
            "file": "\/media\/sf_phizdetsc-php\/try-shit--aps-back.php",
            "line": 6,
            "params": [],
            "include_filename": "\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php"
        },
        {
            "function": "phiExpressionStatement",
            "file": "\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php",
            "line": 8286,
            "params": {
                "expr": "???"
            }
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiBinaryOperation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 1857,
            "params": []
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiInvocation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 1472,
            "params": []
        },
        {
            "function": "invoke",
            "type": "dynamic",
            "class": "PhiFunction",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 2231,
            "params": {
                "receiver": "???",
                "args": "???"
            }
        },
        {
            "function": "{closure:\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php:4-8285}",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 320,
            "params": []
        },
        {
            "function": "phiExpressionStatement",
            "file": "\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php",
            "line": 8283,
            "params": {
                "expr": "???"
            }
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiInvocation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 1857,
            "params": []
        },
        {
            "function": "invoke",
            "type": "dynamic",
            "class": "PhiFunction",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 2231,
            "params": {
                "receiver": "???",
                "args": "???"
            }
        },
        {
            "function": "{closure:\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php:4130-4132}",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 320,
            "params": []
        },
        {
            "function": "phiExpressionStatement",
            "file": "\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php",
            "line": 4131,
            "params": {
                "expr": "???"
            }
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiInvocation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 1857,
            "params": []
        },
        {
            "function": "invoke",
            "type": "dynamic",
            "class": "PhiFunction",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 2231,
            "params": {
                "receiver": "???",
                "args": "???"
            }
        },
        {
            "function": "{closure:\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php:42-78}",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 320,
            "params": []
        },
        {
            "function": "phiVars",
            "file": "\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php",
            "line": 45,
            "params": {
                "debugTag": "???",
                "nameValuePairs": "???"
            }
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiInvocation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 2388,
            "params": []
        },
        {
            "function": "invoke",
            "type": "dynamic",
            "class": "PhiFunction",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 2231,
            "params": {
                "receiver": "???",
                "args": "???"
            }
        },
        {
            "function": "{closure:\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php:4091-4093}",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 320,
            "params": []
        },
        {
            "function": "phiEvaluate",
            "file": "\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php",
            "line": 4092,
            "params": {
                "expr": "???"
            }
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiInvocation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 1917,
            "params": []
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiInvocation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 2229,
            "params": []
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiInvocation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 2194,
            "params": []
        },
        {
            "function": "phiSendStack",
            "file": "xdebug:\/\/debug-eval",
            "line": 1,
            "params": []
        }
    ]
}
            """
}



private class MyBullshitter(val project: Project, val title: String? = null) {
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
                        relazy.reset(this@MyBullshitter::consoleView)
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
            Messages.showErrorDialog(e.stackTraceStr, "Shit Didn't Work")
            return null
        }
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





