package vgrechka.idea.hripos

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.actions.ChooseRunConfigurationPopup
import com.intellij.execution.actions.ExecutorProvider
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowId
import vgrechka.*
import vgrechka.idea.*
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Suppress("Unused")
class HotReloadableIdeaPieceOfShit {
    fun serve(req: HttpServletRequest, res: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        req.queryString
        val rawRequest = req.reader.readText()
        clog("Got request:", rawRequest)
        val requestClass = Class.forName(this::class.java.`package`.name + ".Command_" + req.getParameter("proc"))
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

private interface Servant {
    fun serve(): Any
}

class SimpleResponse(val status: String)

private fun withProjectNamed(projectName: String, block: (Project) -> SimpleResponse): SimpleResponse {
    val project = ProjectManager.getInstance().openProjects.find {it.name == projectName}
        ?: return SimpleResponse("No fucking project `$projectName`")

    return block(project)
}

private fun coolResponse() = SimpleResponse("Cool")

@Ser @Suppress("Unused")
class Command_MessAround(val projectName: String) : Servant {
    override fun serve() = withProjectNamed(projectName) {project->
        val title = this::class.simpleName!!.substring("Command_".length)
        val bs = Bullshitter(project, title = title)
        bs.mumble("Just messing around... (${Date()})")
        coolResponse()
    }
}

@Ser @Suppress("Unused")
class Command_PrintToBullshitter(val projectName: String, val message: String) : Servant {
    override fun serve() = withProjectNamed(projectName) {project->
        val bs = Bullshitter(project)
        bs.mumble("Someone said: " + message)
        coolResponse()
    }
}

@Ser @Suppress("Unused")
class Command_DebugConfiguration(val projectName: String, val configurationName: String) : Servant {
    override fun serve(): Any {
        return withProjectNamed(projectName) o@{project->
            val executor = ExecutorRegistry.getInstance().getExecutorById(ToolWindowId.DEBUG)
            val executorProvider = ExecutorProvider {executor}
            val list = ChooseRunConfigurationPopup.createSettingsList(project, executorProvider, false)
            for (item in list) {
                val config = item.value
                if (config is RunnerAndConfigurationSettings) {
                    if (config.name == configurationName) {
                        ExecutionUtil.runConfiguration(config, executor)
                        return@o coolResponse()
                    }
                }
            }

            SimpleResponse("No fucking debug configuration `$configurationName`")
        }
    }
}














