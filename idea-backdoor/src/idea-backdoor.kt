package vgrechka.ideabackdoor

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.util.lang.UrlClassLoader
import com.sun.jna.platform.win32.User32
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import vgrechka.*
import vgrechka.idea.*
import vgrechka.idea.hripos.*
import java.awt.MouseInfo
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.io.File
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.concurrent.thread

object IdeaBackdoorGlobal {
    val rpcPortSystemProperty = "vgrechka.ideabackdoor.rpcServerPort"
}

class IdeaBackdoorPlugin : ApplicationComponent {
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
//                val bs = Bullshitter(project)
//                bs.mumble("Hello, sweetheart")
            }
        })

        val am = ActionManager.getInstance()
        val group = am.getAction("ToolsMenu") as DefaultActionGroup
        group.addSeparator()

//        run {
//            val action = object : AnAction("Backdoor: Bullshit Something") {
//                override fun actionPerformed(event: AnActionEvent) {
//                    val bs = Bullshitter(event.project!!)
//                    bs.mumble("Something? How about fuck you?")
//                }
//            }
//            group.add(action)
//        }

        run {
            group.add(object : AnAction("Backdoor: _Mess Around") {
                override fun actionPerformed(event: AnActionEvent) {
                    rubRemoteIdeaTits(localProject = event.project,
                                      data = mapOf("projectName" to event.project!!.name),
                                      proc = "MessAround",
                                      port = getIDEABackdoorPortForThisJVM())
                }
            })

            group.add(object : AnAction("Backdoor: T_ools") {
                override fun actionPerformed(e: AnActionEvent) {
                    val backdoorToolsGroup = loadHotClass(BackdoorToolsGroup::class.qualifiedName!!)
                        .newInstance() as DefaultActionGroup

                    val dataContext = e.dataContext
                    val popup = JBPopupFactory.getInstance().createActionGroupPopup(
                            "Backdoor Tools",
                            backdoorToolsGroup,
                            dataContext,
                            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                            false)

                    popup.showInBestPositionFor(dataContext)
                }
            })
        }

        addBuildMenuItems()

        startRPCServer()
    }

    private fun addBuildMenuItems() {
        val am = ActionManager.getInstance()
        val group = am.getAction("BuildMenu") as DefaultActionGroup
        group.addSeparator()
        for (key in listOf('b', 'o', 'n', 'q', 'w')) {
            group.addAction(object : AnAction("Custom Build: _${key.toUpperCase()}") {
                override fun actionPerformed(e: AnActionEvent) {
                    val clazz = loadHotClass(CustomBuildHandler::class.qualifiedName!!)
                    val method = clazz.getMethod(CustomBuildHandler::onCustomBuild.name, AnActionEvent::class.java, Char::class.java)
                    val inst = clazz.newInstance()
                    method.invoke(inst, e, key)
                }
            })
        }
    }

    inner class startRPCServer {
        init {
            thread {
                try {
                    val port = getIDEABackdoorPortForThisJVM()
                    Server(port)-{o->
                        o.handler = ServletHandler()-{o->
                            o.addServletWithMapping(ServletHolder(FuckingServlet()), "/*")
                        }
                        o.start()
                        clog("[IDEA Backdoor] Shit is spinning on port $port")
                        o.join()
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        inner class FuckingServlet : HttpServlet() {
            override fun service(req: HttpServletRequest, res: HttpServletResponse) {
                val clazz = loadHotClass("vgrechka.idea.hripos.HotReloadableIdeaPieceOfShit")
                val inst = clazz.newInstance()
                val httpServletRequestClass = HttpServletRequest::class.java // loader.loadClass(HttpServletRequest::class.qualifiedName)
                val httpServletResponseClass = HttpServletResponse::class.java // loader.loadClass(HttpServletResponse::class.qualifiedName)
                val method = clazz.getMethod("serve", httpServletRequestClass, httpServletResponseClass)

                method.invoke(inst, req, res)
            }
        }
    }


    private fun getIDEABackdoorPortForThisJVM(): Int {
        val s = System.getProperty(IdeaBackdoorGlobal.rpcPortSystemProperty)
        return s?.toInt() ?: BackdoorClientGlobal.defaultRPCServerPort
    }
}

// Ex: _run vgrechka.ideabackdoor.SendSomeShitToBackdoor MessAround "{projectName: 'fegh'}"
// Ex: _run vgrechka.ideabackdoor.SendSomeShitToBackdoor DebugConfiguration "{projectName: 'idea-backdoor-fucking', configurationName: 'FuckKt'}"
// Ex: _run vgrechka.ideabackdoor.SendSomeShitToBackdoor IdeaBackdoorTesting_CheckKotlinClassesAreLoadedByPluginClassLoader:12313 "{projectName: 'idea-backdoor-fucking', configurationName: 'FuckKt'}"
object SendSomeShitToBackdoor {
    @JvmStatic
    fun main(args: Array<String>) {
        var port = BackdoorClientGlobal.defaultRPCServerPort
        var proc = args[0]
        val colon = proc.indexOf(":")
        if (colon != -1) {
            port = proc.substring(colon + 1).toInt()
            proc = proc.substring(0, colon)
        }

        val url = "http://localhost:$port?proc=$proc"
        val json = args[1]
        clog("url =", url)
        clog("json =", json)
        val res = HTTPPile.postJSON_bitchUnlessOK(url, json)
        clog("Response: $res")
        clog("OK")
    }
}

class CustomBuildHandler {
    fun onCustomBuild(e: AnActionEvent, key: Char) {
        when (key) {
            'b' -> CustomBuilds.rerunAlrauneAndShowInBrowser(e)
//            'n' -> CustomBuilds.buildAlrauneAndRunPhucking1(e)
            else -> IDEAPile.infoDialog("I have no idea how to custom build '$key'")
        }

    }
}

private fun loadHotClass(className: String): Class<*> {
    val loader = object : UrlClassLoader(
        build()
            .parent(IdeaBackdoorPlugin::class.java.classLoader)
            .urls(*(
                File("E:/fegh/out/production").listFiles()
                    + File("E:/fegh/lib").listFiles()
                    + File("E:/fegh/lib-gradle").listFiles())
                .map {it.toURI().toURL()}.toTypedArray())
    ) {
        override fun loadClass(name: String, resolve: Boolean): Class<*> {
            if (name.startsWith("org.jetbrains.kotlin.")) {
                val kotlinPluginId = PluginManager.getPluginByClassName("org.jetbrains.kotlin.psi.KtFile") ?: bitch("5848894b-cf98-4da0-b826-751b9e7ca8d0")
                val kotlinPluginDescriptor = PluginManager.getPlugin(kotlinPluginId) ?: bitch("18c09d84-4d63-4380-80e4-8daac75c6e7e")
                val kotlinPluginClassLoader = kotlinPluginDescriptor.pluginClassLoader ?: bitch("3f8eb3ff-2985-4935-b5c8-e79e6f538274")
                return kotlinPluginClassLoader.loadClass(name)
            }

            if (!name.startsWith("vgrechka."))
                return super.loadClass(name, resolve)


            synchronized(getClassLoadingLock(name)) {
                var c = findLoadedClass(name)
                if (c == null) {
                    c = findClass(name)
                }
                if (resolve) {
                    resolveClass(c)
                }
                return c
            }
        }
    }
    val clazz = loader.loadClass(className)
    return clazz
}

object CustomBuilds {
    val robot = Robot()

    fun rerunAlrauneAndShowInBrowser(e: AnActionEvent) {
        val configurationName = "alraune.back.StartAlrauneBack"
        val debug = false
        val runTimeout = 15000
        val terminationTimeout = 5000
        val flagFile = File("c:/tmp/alraune-back-started")
        val flagFilePollTimes = 20
        val flagFilePollInterval = 500L

        IDEAPile.revealingException {
            if (flagFile.exists())
                check(flagFile.delete()) {"443a0f3f-530f-4a59-9ef0-4a48a3536ecb"}

            IDEAPile.threadRevealingException(showInBalloon = {true}) thread@{
                IDEAPile.waitForConfigurationToTerminateAndThenRun(e.project!!, configurationName, debug, runTimeout, terminationTimeout)
                // IDEAPile.showProgressBalloon(e.project!!, MessageType.INFO, "OK, shit started...")

                for (i in 1..flagFilePollTimes) {
                    if (flagFile.exists())
                        break
                    Thread.sleep(flagFilePollInterval)
                }

                if (!flagFile.exists()) {
                    IDEAPile.showProgressBalloon(e.project!!, MessageType.WARNING, "Sick of waiting for the flag file")
                } else {
                    // IDEAPile.showProgressBalloon(e.project!!, MessageType.INFO, "Switching to browser...")
                    val hwnd =
                        User32.INSTANCE.FindWindow(null, "Alraune - Google Chrome")
                            ?: bitch("No necessary Chrome window")
                    User32.INSTANCE.SetForegroundWindow(hwnd) || bitch("Cannot bring Chrome to foreground")
                    val origLocation = MouseInfo.getPointerInfo().location
                    robot.mouseMove(600, 190) // Somewhere in page (or modal, so it won't be closed!) title
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                    robot.mouseMove(origLocation.x, origLocation.y)

//                    refreshBrowser()
//                    navigateBrowser("https://alraune.local/orderCreationForm?frontMessAround=messAroundFront201")
//                    navigateBrowser("https://alraune.local/orderParams?orderUUID=816dc211-2cfa-4423-a99a-fd0e3c8915ee&frontMessAround=messAroundFront301")
//                    navigateBrowser("https://alraune.local/orderCreationForm?frontMessAround=messAroundFront203")
//                    navigateBrowser("https://alraune.local/orderFiles?orderUUID=e785b9be-529c-4122-962d-b7b4d565e6ad&mab=messAroundBack401")
                    navigateBrowser("https://alraune.local/orderFiles?orderUUID=e785b9be-529c-4122-962d-b7b4d565e6ad&mab=messAroundBack401&maf=messAroundFront402")
                }
            }

            IDEAPile.runConfiguration(e.project!!, configurationName)
        }

//        ProjectTaskManager.getInstance(e.project).buildAllModules {
//            if (it.errors == 0) {
//
//            }
//        }


//        buildAlrauneAndThen(e) {
//            val hwnd =
//                User32.INSTANCE.FindWindow(null, "Alraune - Google Chrome")
//                    // ?: User32.INSTANCE.FindWindow(null, "Writer UA - Google Chrome")
//                    ?: bitch("No necessary Chrome window")
//            User32.INSTANCE.SetForegroundWindow(hwnd) || bitch("Cannot bring Chrome to foreground")
//            val origLocation = MouseInfo.getPointerInfo().location
//            val robot = Robot()
//            robot.mouseMove(600, 190) // Somewhere in page (or modal, so it won't be closed!) title
//            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
//            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
//            robot.mouseMove(origLocation.x, origLocation.y)
//
//            robot.keyPress(KeyEvent.VK_CONTROL)
//            robot.keyPress('R'.toInt())
//            robot.keyRelease('R'.toInt())
//            robot.keyRelease(KeyEvent.VK_CONTROL)
//        }
    }

    private fun refreshBrowser() {
        RobotPile.keyWithModifier(KeyEvent.VK_CONTROL, 'R')
    }

    private fun navigateBrowser(url: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val selection = StringSelection(url)
        clipboard.setContents(selection, selection)

        RobotPile.keyWithModifier(KeyEvent.VK_ALT, 'D')
        Thread.sleep(100)
        RobotPile.keyWithModifier(KeyEvent.VK_CONTROL, 'V')
        RobotPile.key(KeyEvent.VK_ENTER)

        // RobotPile.typeTextCR(url)
    }

//    fun killme_buildAlrauneAndShowInBrowser(e: AnActionEvent) {
//        buildAlrauneAndThen(e) {
//            val hwnd =
//                User32.INSTANCE.FindWindow(null, "Alraune - Google Chrome")
//                    // ?: User32.INSTANCE.FindWindow(null, "Writer UA - Google Chrome")
//                    ?: bitch("No necessary Chrome window")
//            User32.INSTANCE.SetForegroundWindow(hwnd) || bitch("Cannot bring Chrome to foreground")
//            val origLocation = MouseInfo.getPointerInfo().location
//            val robot = Robot()
//            robot.mouseMove(600, 190) // Somewhere in page (or modal, so it won't be closed!) title
//            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
//            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
//            robot.mouseMove(origLocation.x, origLocation.y)
//
//            robot.keyPress(KeyEvent.VK_CONTROL)
//            robot.keyPress('R'.toInt())
//            robot.keyRelease('R'.toInt())
//            robot.keyRelease(KeyEvent.VK_CONTROL)
//        }
//    }

//    fun killme_buildAlrauneAndRunPhucking1(e: AnActionEvent) {
//        buildAlrauneAndThen(e) {
//            val windowTitle = "phucking1"
//            val hwnd =
//                User32.INSTANCE.FindWindow(null, windowTitle)
//                    ?: bitch("I want a cmd.exe window titled `$windowTitle`")
//            User32.INSTANCE.SetForegroundWindow(hwnd) || bitch("Cannot bring window to foreground")
//
//            val robot = Robot()
//
////            val origLocation = MouseInfo.getPointerInfo().location
////            robot.mouseMove(600, 190) // Somewhere in page (or modal, so it won't be closed!) title
////            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
////            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
////            robot.mouseMove(origLocation.x, origLocation.y)
//
//            RobotPile.typeTextCR("cls & php index.php phucking1")
//        }
//    }

//    private fun killme_buildAlrauneAndThen(e: AnActionEvent, block: () -> Unit) {
//        ProjectTaskManager.getInstance(e.project).buildAllModules {
//            if (it.errors == 0) {
//                thread {
//                    try {
//                        Thread.sleep(50)
//                        val res = BigPile.runProcessAndWait(inheritIO = false, cmdPieces = listOf(
//                            "java", "-cp",
//                            listOf("C:/opt/jdk1.8.0_121/jre/lib/ext/nashorn.jar",
//                                   "C:/opt/jdk1.8.0_121/jre/lib/rt.jar",
//                                   "E:/fegh/out/production/shared-jvm",
//                                   "E:/fegh/out/production/phizdetsc",
//                                   "E:/fegh/lib/kotlin-runtime.jar",
//                                   "E:/fegh/lib/kotlin-reflect.jar",
//                                   "E:/fegh/lib-gradle/guava-21.0.jar",
//                                   "E:/fegh/lib-gradle/kotlin-compiler-1.1.0.jar",
//                                   "E:/fegh/lib-gradle/closure-compiler-v20170218.jar")
//                                .joinToString(File.pathSeparator),
//                            "phizdets.compiler.JS2Phizdets",
//                            "--outdir=E:/fegh/alraune/alraune-back/out-back",
//                            "E:/fegh/alraune/alraune-back/out-back/alraune-back.js",
//                            "E:/fegh/out/production/shared-x/shared-x.js",
//                            "E:/fegh/out/production/alraune-shared/alraune-shared.js"
//                        ))
//                        if (res.exitValue != 0) {
//                            IDEAPile.errorDialog(res.stdout + "\n" + res.stderr)
//                            return@thread
//                        }
//
//                        IDEAPile.showProgressBalloon(e.project!!, MessageType.INFO, "Phizdets compiled shit OK")
//
//                        block()
//                    } catch(e: Throwable) {
//                        IDEAPile.later {IDEAPile.errorDialog(e)}
//                    }
//                }
//            }
//        }
//    }

}

class BackdoorToolsGroup : DefaultActionGroup() {
    init {
        addClipboardInSomethingOutAction("lns") {p, shit ->
            for (line in shit.lines()) {
                p.println("ln(\"" + line.replace("\"", "\\\"").replace("$", "\\$") + "\")")
            }
        }

        addClipboardInSomethingOutAction("Generate Alraune group IDs") {p, shit ->
            val buf = StringBuilder()
            val re = Regex("ids\\.\\w+")
            var nextID = 102
            var searchStart = 0
            var ok = false
            for (i in 1..1000) { // Prevent infinite loop by mistake
                val m = re.find(shit, searchStart)
                if (m == null) {
                    ok = true
                    break
                }
                buf.append(shit.substring(searchStart, m.range.start))
                buf.append("\"${nextID++}\"")
                searchStart = m.range.endInclusive + 1
            }
            if (!ok)
                bitch("It was too much    507e043f-19d8-4195-aa07-c6adbcf091c8")
            buf.append(shit.substring(searchStart))
            p.println(buf.toString())
        }


//        add(object : AnAction("Build Alraune and show in browser") {
//            override fun actionPerformed(e: AnActionEvent) {
//                CustomBuilds.buildAlrauneAndShowInBrowser(e)
//            }
//        })

        add(object : AnAction("Say 'fuck you'") {
            override fun actionPerformed(e: AnActionEvent?) {
                IDEAPile.infoDialog("fuuuuuck youuuuuuuuu")
            }
        })
        add(object : AnAction("Say 'pizda'") {
            override fun actionPerformed(e: AnActionEvent?) {
                IDEAPile.infoDialog("pizdaaaaaaaa")
            }
        })
    }

    private fun addClipboardInSomethingOutAction(title: String, pedro: (HriposDebugOutput, String) -> Unit) {
        add(object : AnAction(title) {
            override fun actionPerformed(e: AnActionEvent) {
                IDEAPile.revealingException {
                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    val transferable = clipboard.getContents(null)
                    val shit = transferable.getTransferData(DataFlavor.stringFlavor) as String? ?: ""
                    val p = HriposDebugOutput(e.project!!)
                    pedro(p, shit)
                    p.showDialog(title = title)
                }
            }
        })
    }

}















