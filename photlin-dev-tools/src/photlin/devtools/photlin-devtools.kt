package photlin.devtools

import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
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


private var bs by notNullOnce<Bullshitter>()


object PhotlinDevToolsGlobal {
    val rpcServerPort = 12321
}

@Ser class PDTRemoteCommand_TestResult(
    val rawResponseFromPHPScript: String
) : Servant {
    override fun serve(): Any {
        bs.mumble("\n------------------- TEST RESULT ------------------\n")
        // bs.mumble(rawResponseFromPHPScript)

        val re = Regex("<b>([^<]*?)</b> on line <b>([^<]*?)</b>")
//        var plainTextStart = 0
        var searchStart = 0
        while (true) {
            val mr = re.find(rawResponseFromPHPScript, searchStart) ?: break
            val filePath = mr.groupValues[1]
            val lineNumber = mr.groupValues[2].toInt()

            bs.mumbleNoln(rawResponseFromPHPScript.substring(searchStart, mr.range.start))
            bs.mumbleNoln("$filePath on line ")
            bs.consoleView.printHyperlink("$lineNumber") {project ->
                bs.mumble("Jumping to $filePath:$lineNumber")
            }

            searchStart = mr.range.endInclusive + 1
            if (searchStart > rawResponseFromPHPScript.lastIndex) break
        }
        bs.mumble("")

        return "Cool"


        bs.mumble("What5?")
    }
}

class PhotlinDevToolsPlugin : ApplicationComponent {
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
                bs = Bullshitter(project)
                bs.mumble("Hello, sweetheart. I am Photlin Development Tools. Now use me")
            }
        })

        val am = ActionManager.getInstance()
        val group = am.getAction("ToolsMenu") as DefaultActionGroup
        group.addSeparator()

        run {
            val action = object : AnAction("PDT: _Mess Around") {
                override fun actionPerformed(event: AnActionEvent) {
                    messAroundAction(event)
                }
            }
            group.add(action)
        }

//        run {
//            val action = object : AnAction("Backdoor: Bullshit Something") {
//                override fun actionPerformed(event: AnActionEvent) {
//                    val bs = Bullshitter(event.project!!)
//                    bs.mumble("Something? How about fuck you?")
//                }
//            }
//            group.add(action)
//        }

//        run {
//            val action = object : AnAction("Backdoor: _Mess Around") {
//                override fun actionPerformed(event: AnActionEvent) {
//                    val title = "Fucking through backdoor"
//                    object : Task.Backgroundable(event.project, title, true) {
//                        var rawResponse by notNullOnce<String>()
//
//                        override fun run(indicator: ProgressIndicator) {
//                            indicator.text = title
//                            indicator.fraction = 0.5
//                            val json = "{projectName: '${event.project!!.name}'}"
//                            rawResponse = HTTPClient.postJSON("http://localhost:${BackdoorGlobal.rpcServerPort}?proc=MessAround", json)
//                            indicator.fraction = 1.0
//                        }
//
//                        override fun onFinished() {
//                            // Messages.showInfoMessage(rawResponse, "Response")
//                        }
//                    }.queue()
//                }
//            }
//            group.add(action)
//        }

        startRPCServer()
    }

    inner class startRPCServer {
        init {
            thread {
                try {
                    Server(PhotlinDevToolsGlobal.rpcServerPort)-{o->
                        o.handler = ServletHandler() -{o->
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
                req.queryString
                val rawRequest = req.reader.readText()
                clog("Got request:", rawRequest)
                val requestClass = Class.forName(this::class.java.`package`.name + ".PDTRemoteCommand_" + req.getParameter("proc"))
                val servant = relaxedObjectMapper.readValue(rawRequest, requestClass) as Servant

                var response by notNullOnce<Any>()
                ApplicationManager.getApplication().invokeAndWait {
                    response = servant.serve()
                }

                val rawResponse = relaxedObjectMapper.writeValueAsString(response)
                res.contentType = "application/json; charset=utf-8"
                res.writer.println(rawResponse)
                res.status = HttpServletResponse.SC_OK
            }
        }
    }
}

private interface Servant {
    fun serve(): Any
}

private fun messAroundAction(event: AnActionEvent) {
    PDTRemoteCommand_TestResult(rawResponseFromPHPScript = """<br />
<b>Notice</b>:  Use of undefined constant aps - assumed 'aps' in <b>C:\opt\xampp\htdocs\TryPhotlin\aps-back\aps-back.php</b> on line <b>16392</b><br />
<br />
<b>Fatal error</b>:  Call to undefined function back_main() in <b>C:\opt\xampp\htdocs\TryPhotlin\aps-back\aps-back.php</b> on line <b>16392</b><br />
    """).serve()
}

























