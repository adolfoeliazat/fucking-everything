package vgrechka.ideabackdoor

import com.fasterxml.jackson.databind.ObjectMapper
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
                    Server(BackdoorClientGlobal.rpcServerPort)-{o->
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
//                        if (name.contains("AttachedComputedShit"))
//                            clog("Loading " + name)
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

// Ex: _run vgrechka.ideabackdoor.SendSomeShitToBackdoor DebugConfiguration "{projectName: 'idea-backdoor-fucking', configurationName: 'FuckKt'}"
object SendSomeShitToBackdoor {
    @JvmStatic
    fun main(args: Array<String>) {
        val proc = args[0]
        val json = args[1]
        clog("json", json)
        val res = HTTPClient.postJSON("http://localhost:${BackdoorClientGlobal.rpcServerPort}?proc=$proc", json)
        clog("Response: $res")
        clog("OK")
    }
}




















