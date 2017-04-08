@file:Suppress("Unused")

package vgrechka.idea.hripos

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
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
import kotlin.properties.Delegates.notNull

object MapPhizdetsStackToolIO {
    @Ser class Input(val projectName: String, val stack: List<StackItem>)
    @Ser class StackItem(val file: String, val line: Int)
    @Ser sealed class Output(dontDeleteMe: Unit = Unit) {
        @Ser class Candy(val mappedStack: List<StackItem?>) : Output()
        @Ser class Poop(val error: String) : Output()
    }
}

@Ser class Command_PhiShowStack(val projectName: String, val stack: List<Map<String, Any?>>) : Servant {
    class InterestingFile(val shortName: String, val fullPath: String)

    override fun serve(): Any {
        return try {
            Go().bananas()
            "Astonishing success"
        } catch (e: Exception) {
            Messages.showErrorDialog(e.stackTraceStr, "Shit Didn't Work")
            "Bloody error"
        }
    }

    inner class Go {
        val interestingFiles = listOf(
            InterestingFile("aps-back.php", "E:/fegh/out/phi-tests/aps-back/aps-back.php"),
            InterestingFile("phizdets-stdlib.php", "E:/fegh/phizdets/phizdetsc/src/phizdets/php/phizdets-stdlib.php")
        )

        lateinit var bs: Any

        fun bananas() {
            withProjectNamed(projectName) {project ->
                val title = Command_PhiShowStack::class.simpleName!!.substring("Command_".length)

                //        val p = HriposDebugOutput(project)
                //        p.showDialog(title = title)

                bs = FuckingUtils.aLittleNonGCableAbomination(
                    project = project,
                    id = "Command_PhiShowStack.bullshitter",
                    version = 19,
                    make = {MyBullshitter(project, title = title)})
                    ?: bitch("No bullshitter")

                toFront()
                mumble("\n----- Hello, honey. It's ${Date()} -----\n")

                // FuckingUtils.noisy = true
                serve1()
                FuckingUtils.info("Received some shit to dig") // To bring the window to foreground
            }
        }

        fun serve1() {
            val stackItems = mutableListOf<MapPhizdetsStackToolIO.StackItem>()
            for (item in stack.reversed().drop(1)) {
                val file = item["file"].toString()
                val line = item["line"].toString().toInt()
                for (shit in interestingFiles) {
                    if (file.contains(shit.shortName)) {
                        stackItems += MapPhizdetsStackToolIO.StackItem(shit.shortName, line)
                    }
                }
            }

            val toolInput = MapPhizdetsStackToolIO.Input("aps-back-php", stackItems)
            val om = ObjectMapper()
            om.typeFactory = TypeFactory
                .defaultInstance()
                .withClassLoader(this::class.java.classLoader)
            om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL)
            val inputJSON = om.writeValueAsString(toolInput)
            // mumble(inputJSON)
            scrollToEnd()

            val res = runProcessAndWait(
                listOf("cmd.exe",
                       "/c",
                       "e:\\fegh\\_run.cmd phizdets.MapPhizdetsStackTool"),
                inheritIO = false,
                input = inputJSON)

            if (res.exitValue != 0) {
                run { // Dump output
                    if (res.stderr.isNotBlank()) {
                        barkNoln(res.stderr)
                        if (!res.stderr.endsWith("\n"))
                            bark("")
                    }
                    mumbleNoln(res.stdout)
                    if (!res.stdout.endsWith("\n"))
                        mumble("")
                }
                FuckingUtils.error("MapPhizdetsStackTool returned ${res.exitValue}, meaning 'fuck you'")
                return
            }

            val out = om.readValue(res.stdout, MapPhizdetsStackToolIO.Output::class.java)
            exhaustive=when (out) {
                is MapPhizdetsStackToolIO.Output.Candy -> {
                    for ((i, item) in stackItems.withIndex()) {
                        link(item)

                        interestingFiles.find {it.shortName == item.file}?.let {
                            mumbleNoln(" (")
                            link("--1", it.fullPath + "--1", item.line)
                            mumbleNoln(")")
                        }

                        mumbleNoln(" --> ")
                        val mappedItem = out.mappedStack[i]
                        if (mappedItem == null) {
                            mumbleNoln("[Obscure]")
                        } else {
                            link(mappedItem)
                        }
                        mumble("")
                    }
                }
                is MapPhizdetsStackToolIO.Output.Poop -> {
                    bark(out.error)
                }
            }
            mumble("OK")
        }

        fun mumble(s: String) {
            val m = bs.javaClass.getMethod("mumble", String::class.java)
            m.isAccessible = true
            m.invoke(bs, s)
        }

        fun bark(s: String) {
            val m = bs.javaClass.getMethod("bark", String::class.java)
            m.isAccessible = true
            m.invoke(bs, s)
        }

        fun mumbleNoln(s: String) {
            val m = bs.javaClass.getMethod("mumbleNoln", String::class.java)
            m.isAccessible = true
            m.invoke(bs, s)
        }

        fun barkNoln(s: String) {
            val m = bs.javaClass.getMethod("barkNoln", String::class.java)
            m.isAccessible = true
            m.invoke(bs, s)
        }

        fun link(item: MapPhizdetsStackToolIO.StackItem) {
            val path = interestingFiles.find {it.shortName == item.file}?.fullPath
                ?: item.file
            link(item.file + ":" + item.line, path, item.line)
        }

        fun link(text: String, path: String, line: Int) {
            getConsoleViewImpl().printHyperlink(text) {project ->
                if (!openFile(project, path, line)) {
                    FuckingUtils.error("No fucking way")
                }
            }
        }

        fun scrollToEnd() {
            getConsoleViewImpl().scrollToEnd()
        }

        private fun getConsoleViewImpl(): ConsoleViewImpl {
            val m = bs.javaClass.getDeclaredMethod("getConsoleView")
            m.isAccessible = true
            return m.invoke(bs) as ConsoleViewImpl
        }

        fun toFront() {
            val m = bs.javaClass.getMethod("toFront")
            m.isAccessible = true
            m.invoke(bs)
        }
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
            // FuckingUtils.info("executor " + executor)
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




