package phizdets

import com.fasterxml.jackson.databind.ObjectMapper
import vgrechka.*
import vgrechka.idea.hripos.*

object MapPhizdetsStackTool {
    @JvmStatic
    fun main(args: Array<String>) {
        val inputJSON = System.`in`.bufferedReader().readText()
        val om = ObjectMapper()
        val input = om.readValue(inputJSON, MapPhizdetsStackToolIO.Input::class.java)
        clog("So, your fucking project is named ${input.projectName}?")
    }
}

object JerkMapPhizdetsStackTool {
    @JvmStatic
    fun main(args: Array<String>) {
        val res = runProcessAndWait(
            listOf(
                "cmd.exe",
                "/c",
                "e:\\fegh\\_run.cmd phizdets.MapPhizdetsStackTool"),
            inheritIO = false,
            input = "{\"fuck\": 123}")
        if (res.stderr.isNotBlank()) {
            clog("STDERR: " + res.stderr)
        }
        clog("STDOUT: " + res.stdout)
        if (res.exitValue != 0) {
            clog("JERK: MapPhizdetsStackTool returned ${res.exitValue}, meaning 'fuck you'")
            return
        }
    }
}


