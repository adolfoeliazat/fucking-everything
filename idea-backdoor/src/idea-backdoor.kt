package vgrechka.ideabackdoor

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ApplicationComponent
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

object BackdoorGlobal {
    val rpcServerPort = 12312
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
                project.bullshitter.mumble("Hello, sweetheart")
            }
        })

        val am = ActionManager.getInstance()
        val group = am.getAction("ToolsMenu") as DefaultActionGroup
        group.addSeparator()

        run {
            val action = object : AnAction("Bullshit Something") {
                override fun actionPerformed(event: AnActionEvent) {
                    event.project!!.bullshitter.mumble("Something? How about fuck you?")
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
                    Server(BackdoorGlobal.rpcServerPort)-{o->
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
                val loader = UrlClassLoader.build()
                    .parent(this::class.java.classLoader)
//                    .parent(Messages::class.java.classLoader)
                    .urls(*run {
                        val fuck = File("E:/fegh/out/production").listFiles()
                        val shit = File("E:/fegh/lib").listFiles()
                        val bitch = File("E:/fegh/lib-gradle").listFiles()
                        (fuck + shit + bitch).map {it.toURI().toURL()}.toTypedArray()
                    })
                    .get()
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

// Ex: _run vgrechka.ideabackdoor.SendSomeShitToBackdoor idea-backdoor-fucking "Fucking around?"
object SendSomeShitToBackdoor {
    @JvmStatic
    fun main(args: Array<String>) {
        val project = args[0]
        val message = args[1]
        val res = HTTPClient.postJSON("http://localhost:${BackdoorGlobal.rpcServerPort}?proc=PrintToBullshitter",
                            """{
                                   "projectName": "$project",
                                   "message": "$message"
                               }""")
        clog("Response: $res")
        clog("OK")
    }
}




















