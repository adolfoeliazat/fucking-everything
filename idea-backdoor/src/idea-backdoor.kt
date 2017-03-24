package vgrechka.ideabackdoor

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import vgrechka.*
import vgrechka.idea.*
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
                req.characterEncoding = "UTF-8"
                val raw = req.reader.readText()
                clog("Got request:", raw)
            }
        }
    }
}

object SendSomeShitToBackdoor {
    @JvmStatic
    fun main(args: Array<String>) {
        HTTPClient.postXML("http://localhost:${BackdoorGlobal.rpcServerPort}",
                           "{\"shit\": \"some\"}")
        clog("OK")
    }
}




















