package phizdets

import com.fasterxml.jackson.databind.ObjectMapper
import vgrechka.*
import vgrechka.idea.hripos.*

object MapPhizdetsStackTool {
    @JvmStatic
    fun main(args: Array<String>) {
        val inputJSON = System.`in`.bufferedReader().readText()
        main1(inputJSON)
    }

    fun main1(inputJSON: String) {
        System.err.println("inputJSON = $inputJSON")
        val om = ObjectMapper()
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL)
        val input = om.readValue(inputJSON, MapPhizdetsStackToolIO.Input::class.java)
        val output = main2(input)
        val outputJSON = om.writeValueAsString(output)
        clog(outputJSON)
    }

    fun main2(input: MapPhizdetsStackToolIO.Input): MapPhizdetsStackToolIO.Output {
        if (input.projectName != "aps-back-php")
            return MapPhizdetsStackToolIO.Output.Poop("I don't support your fucking ${input.projectName}")

        return MapPhizdetsStackToolIO.Output.Candy(input.stack.map {obscureItem->
            val pizda = MapPhizdetsStack.mapFuckingLine(FileLineColumn(obscureItem.file, obscureItem.line, 9999999), getMapPath = ::getAPSBackPHPMapPath)
            when (pizda) {
                null -> null
                else -> FileLine(pizda.file, pizda.line)
            }
        })
    }
}

object JerkMapPhizdetsStackTool5 {
    @JvmStatic
    fun main(args: Array<String>) {
        val mapping = theSourceMappings.getCached("E:/fegh/out/phi-tests/aps-back/aps-back.php.map")
        val penetration = mapping.penetration
        clog(mapping.getMappingForLine(46, 9999999))
        "break on me"
    }
}

object JerkMapPhizdetsStackTool4 {
    @JvmStatic
    fun main(args: Array<String>) {
        val out = MapPhizdetsStackTool.main2(MapPhizdetsStackToolIO.Input("aps-back-php", listOf(
            FileLine("aps-back.php", 4284)
        ))) as MapPhizdetsStackToolIO.Output.Candy
        val shit = out.mappedStack.first()!!
        clog(shit.file + ":" + shit.line)
    }
}

object JerkMapPhizdetsStackTool3 {
    @JvmStatic
    fun main(args: Array<String>) {
        MapPhizdetsStackTool.main1("""["vgrechka.idea.hripos.MapPhizdetsStackToolIO${'$'}Input",{"projectName":"aps-back-php","stack":["java.util.ArrayList",[["vgrechka.idea.hripos.MapPhizdetsStackToolIO${'$'}StackItem",{"file":"aps-back.php","line":4092}],["vgrechka.idea.hripos.MapPhizdetsStackToolIO${'$'}StackItem",{"file":"aps-back.php","line":45}],["vgrechka.idea.hripos.MapPhizdetsStackToolIO${'$'}StackItem",{"file":"aps-back.php","line":4131}],["vgrechka.idea.hripos.MapPhizdetsStackToolIO${'$'}StackItem",{"file":"aps-back.php","line":8283}],["vgrechka.idea.hripos.MapPhizdetsStackToolIO${'$'}StackItem",{"file":"aps-back.php","line":8286}],["vgrechka.idea.hripos.MapPhizdetsStackToolIO${'$'}StackItem",{"file":"aps-back.php","line":6}],["vgrechka.idea.hripos.MapPhizdetsStackToolIO${'$'}StackItem",{"file":"aps-back.php","line":0}]]]}]""")
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


