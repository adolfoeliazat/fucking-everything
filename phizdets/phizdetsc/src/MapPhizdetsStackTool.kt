package phizdets

import com.fasterxml.jackson.databind.ObjectMapper
import vgrechka.*
import vgrechka.idea.hripos.*

object MapPhizdetsStackTool {
    @JvmStatic
    fun main(args: Array<String>) {
        val inputJSON = System.`in`.bufferedReader().readText()
        System.err.println("inputJSON = $inputJSON")
        val om = ObjectMapper()
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL)
        val input = om.readValue(inputJSON, MapPhizdetsStackToolIO.Input::class.java)
        val output = MapPhizdetsStackToolIO.Output.Poop("Wat?")
        val outputJSON = om.writeValueAsString(output)
        clog(outputJSON)
    }
}

object JerkMapPhizdetsStackTool2 {
    @JvmStatic
    fun main(args: Array<String>) {
        val om = ObjectMapper()
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL)

        val json = om.writeValueAsString(MapPhizdetsStackToolIO.Output.Poop("pizda"))
        clog(json)

        val out = om.readValue(json, MapPhizdetsStackToolIO.Output::class.java)
        exhaustive=when (out) {
            is MapPhizdetsStackToolIO.Output.Candy -> {
                clog("Candy")
            }
            is MapPhizdetsStackToolIO.Output.Poop -> {
                clog("Poop: ${out.error}")
            }
        }

        clog("aaaaaaaaaaaaaaaa")
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


