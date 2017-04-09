package vgrechka.ideabackdoor

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import vgrechka.*

object BackdoorClientGlobal {
    val defaultRPCServerPort = 12312
}

fun sendCommandToIDEABackdoor(cmd: Any) {
    val className = cmd.javaClass.simpleName
    check(className.startsWith("Command_")) {"05ec0dad-c777-4e82-8438-e7bc04cd40e1"}
    val proc = className.substring("Command_".length)

    val port = BackdoorClientGlobal.defaultRPCServerPort
    val url = "http://localhost:$port?proc=$proc"
    val json = ObjectMapper().writeValueAsString(cmd)
    HTTPClient.postJSON(url, json)
}

fun rubRemoteIdeaTits(localProject: Project?, data: Any, taskTitle: String? = null, proc: String? = null, readTimeoutSeconds: Long? = null, onError: ((String) -> Unit)? = null) {
    val title = taskTitle ?: "Rubbing remote IDEA tits"
    val theProc = proc ?: run {
        val s = data::class.simpleName!!
        val prefix = "Command_"
        check(s.startsWith(prefix)) {"e0d8d58d-935f-4ea8-86e8-2e5f7c3a1d9a"}
        s.substring(prefix.length)
    }
    object : Task.Backgroundable(localProject, title, true) {
        var rawResponse by notNullOnce<String>()

        override fun run(indicator: ProgressIndicator) {
            indicator.text = title
            indicator.fraction = 0.5
            val json = ObjectMapper().writeValueAsString(data)
            rawResponse = HTTPClient.postJSON(
                "http://localhost:${BackdoorClientGlobal.defaultRPCServerPort}?proc=$theProc",
                json, readTimeoutSeconds = readTimeoutSeconds)
            indicator.fraction = 1.0
        }

        override fun onFinished() {
            val res = relaxedObjectMapper.readValue(rawResponse, Any::class.java)
            if (res is Map<*, *>) {
                val error = res["error"]
                if (error != null) {
                    onError?.invoke(error as String)
                }
            }
            // Messages.showInfoMessage(rawResponse, "Response")
        }
    }.queue()
}




