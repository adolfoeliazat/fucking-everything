@file:Suppress("Unused")
package vgrechka.idea.hripos

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import vgrechka.*

object MapPhizdetsStackToolIO {
    @Ser class Input(val projectName: String, val stack: List<FileLine>)
    @Ser sealed class Output(dontDeleteMe: Unit = Unit) {
        @Ser class Candy(val mappedStack: List<FileLine?>) : Output()
        @Ser class Poop(val error: String) : Output()
    }
}


fun runMapPhizdetsStackTool(con: Mumbler, stackItems: MutableList<FileLine>): MapPhizdetsStackToolIO.Output.Candy? {
    val toolInput = MapPhizdetsStackToolIO.Input("aps-back-php", stackItems)
    val om = ObjectMapper()
    om.typeFactory = TypeFactory
        .defaultInstance()
        .withClassLoader(object{}.javaClass.classLoader)
    om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL)
    val inputJSON = om.writeValueAsString(toolInput)
    // mumble(inputJSON)
    con.scrollToEnd()

    val res = BigPile.runProcessAndWait(
        listOf("cmd.exe",
               "/c",
               "e:\\fegh\\_run.cmd phizdets.MapPhizdetsStackTool"),
        inheritIO = false,
        input = inputJSON)

    if (res.exitValue != 0) {
        run { // Dump output
            if (res.stderr.isNotBlank()) {
                con.barkNoln(res.stderr)
                if (!res.stderr.endsWith("\n"))
                    con.bark("")
            }
            con.mumbleNoln(res.stdout)
            if (!res.stdout.endsWith("\n"))
                con.mumble("")
        }
        FuckingUtils.error("MapPhizdetsStackTool returned ${res.exitValue}, meaning 'fuck you'")
        return null
    }

    val out = om.readValue(res.stdout, MapPhizdetsStackToolIO.Output::class.java)
    return when (out) {
        is MapPhizdetsStackToolIO.Output.Candy -> {
            out
        }
        is MapPhizdetsStackToolIO.Output.Poop -> {
            con.bark(out.error)
            null
        }
    }
}

@Ser class Command_PhiShowStack(val projectName: String, val stack: List<Map<String, Any?>>) : Servant {
    override fun serve(): Any {
        val spew = StringBuilder()
        for (item in stack.reversed().drop(1)) {
            val file = item["file"].toString()
            val line = item["line"].toString().toInt()
            spew += "$file:$line\n"
        }
        return Command_PhiMakeSenseOfPHPSpew(spew.toString()).serve()
    }

    private fun oldShit(con: Mumbler) {
        val stackItems = mutableListOf<FileLine>()
        for (item in stack.reversed().drop(1)) {
            val file = item["file"].toString()
            val line = item["line"].toString().toInt()
            for (shit in APSBackPHPDevTools.interestingFiles) {
                if (file.contains(shit.shortName)) {
                    stackItems += FileLine(shit.shortName, line)
                }
            }
        }

        val toolOut = runMapPhizdetsStackTool(con, stackItems) ?: return

        for ((i, item) in stackItems.withIndex()) {
            APSBackPHPDevTools.link(con, item)

            APSBackPHPDevTools.interestingFiles.find {it.shortName == item.file}?.let {
                con.mumbleNoln(" (")
                con.link("--1", it.fullPath + "--1", item.line)
                con.mumbleNoln(")")
            }

            con.mumbleNoln(" --> ")
            val mappedItem = toolOut.mappedStack[i]
            if (mappedItem == null) {
                con.mumbleNoln("[Obscure]")
            } else {
                APSBackPHPDevTools.link(con, mappedItem)
            }
            con.mumble("")
        }
        con.mumble("OK")
    }
}







object Command_PhiShowStackTest {
    @JvmStatic
    fun main(args: Array<String>) {
        HTTPClientRequest()
            .url("http://localhost:12312?proc=PhiShowStack")
            .method_post {it
                .mediaTypeName(BigPile.mediaType.json)
                .content(json)
            }
            .ignite()
    }

    val json = """
{
    "projectName": "fegh",
    "stack": [
        {
            "function": "{main}",
            "file": "\/media\/sf_phizdetsc-php\/try-shit--aps-back.php",
            "line": 0,
            "params": []
        },
        {
            "file": "\/media\/sf_phizdetsc-php\/try-shit--aps-back.php",
            "line": 6,
            "params": [],
            "include_filename": "\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php"
        },
        {
            "function": "phiExpressionStatement",
            "file": "\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php",
            "line": 8286,
            "params": {
                "expr": "???"
            }
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiBinaryOperation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 1857,
            "params": []
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiInvocation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 1472,
            "params": []
        },
        {
            "function": "invoke",
            "type": "dynamic",
            "class": "PhiFunction",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 2231,
            "params": {
                "receiver": "???",
                "args": "???"
            }
        },
        {
            "function": "{closure:\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php:4-8285}",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 320,
            "params": []
        },
        {
            "function": "phiExpressionStatement",
            "file": "\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php",
            "line": 8283,
            "params": {
                "expr": "???"
            }
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiInvocation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 1857,
            "params": []
        },
        {
            "function": "invoke",
            "type": "dynamic",
            "class": "PhiFunction",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 2231,
            "params": {
                "receiver": "???",
                "args": "???"
            }
        },
        {
            "function": "{closure:\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php:4130-4132}",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 320,
            "params": []
        },
        {
            "function": "phiExpressionStatement",
            "file": "\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php",
            "line": 4131,
            "params": {
                "expr": "???"
            }
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiInvocation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 1857,
            "params": []
        },
        {
            "function": "invoke",
            "type": "dynamic",
            "class": "PhiFunction",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 2231,
            "params": {
                "receiver": "???",
                "args": "???"
            }
        },
        {
            "function": "{closure:\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php:42-78}",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 320,
            "params": []
        },
        {
            "function": "phiVars",
            "file": "\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php",
            "line": 45,
            "params": {
                "debugTag": "???",
                "nameValuePairs": "???"
            }
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiInvocation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 2388,
            "params": []
        },
        {
            "function": "invoke",
            "type": "dynamic",
            "class": "PhiFunction",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 2231,
            "params": {
                "receiver": "???",
                "args": "???"
            }
        },
        {
            "function": "{closure:\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php:4091-4093}",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 320,
            "params": []
        },
        {
            "function": "phiEvaluate",
            "file": "\/media\/sf_phizdetsc-php\/fuck-around--aps-back.php",
            "line": 4092,
            "params": {
                "expr": "???"
            }
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiInvocation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 1917,
            "params": []
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiInvocation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 2229,
            "params": []
        },
        {
            "function": "evaluate",
            "type": "dynamic",
            "class": "PhiInvocation",
            "file": "\/media\/sf_phizdetsc-php\/phi-engine.php",
            "line": 2194,
            "params": []
        },
        {
            "function": "phiSendStack",
            "file": "xdebug:\/\/debug-eval",
            "line": 1,
            "params": []
        }
    ]
}
            """
}



