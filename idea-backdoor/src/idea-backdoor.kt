package vgrechka.ideabackdoor

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.ui.Messages
import com.intellij.util.lang.UrlClassLoader
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import vgrechka.*
import vgrechka.idea.*
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
            val action = object : AnAction("Backdoor: _Mess Around") {
                override fun actionPerformed(event: AnActionEvent) {
                    rubRemoteIdeaTits(event.project, mapOf("projectName" to event.project!!.name), proc = "MessAround")
                }
            }
            group.add(action)
        }

        startRPCServer()
    }


    inner class startRPCServer {
        init {
            thread {
                try {
                    val s = System.getProperty(IdeaBackdoorGlobal.rpcPortSystemProperty)
                    val port = s?.toInt() ?: BackdoorClientGlobal.defaultRPCServerPort

                    Server(port)-{o->
                        o.handler = ServletHandler()-{o->
                            o.addServletWithMapping(ServletHolder(FuckingServlet()), "/*")
                        }
                        o.start()
                        clog("Shit is spinning")
                        o.join()
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        inner class FuckingServlet : HttpServlet() {
            override fun service(req: HttpServletRequest, res: HttpServletResponse) {
                val loader = object:UrlClassLoader(
                    build()
                        .parent(this::class.java.classLoader)
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
                val clazz = loader.loadClass("vgrechka.idea.hripos.HotReloadableIdeaPieceOfShit")
                val inst = clazz.newInstance()
                val httpServletRequestClass = HttpServletRequest::class.java // loader.loadClass(HttpServletRequest::class.qualifiedName)
                val httpServletResponseClass = HttpServletResponse::class.java // loader.loadClass(HttpServletResponse::class.qualifiedName)
                val method = clazz.getMethod("serve", httpServletRequestClass, httpServletResponseClass)

                method.invoke(inst, req, res)
            }
        }
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
        val res = HTTPClient.post(HTTPClient.MediaTypeName.JSON, url, json)
        clog("Response: $res")
        clog("OK")
    }
}




















