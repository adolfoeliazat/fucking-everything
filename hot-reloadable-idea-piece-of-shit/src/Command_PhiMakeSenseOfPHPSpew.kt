@file:Suppress("Unused")
package vgrechka.idea.hripos

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.wm.WindowManager
import vgrechka.*
import vgrechka.ideabackdoor.*
import javax.swing.JFrame
import org.hamcrest.core.AnyOf
import com.thaiopensource.xml.dtd.om.ZeroOrMore
import org.parboiled.*
import org.parboiled.annotations.BuildParseTree
import org.parboiled.parserunners.ReportingParseRunner
import org.parboiled.support.ParseTreeUtils
import kotlin.system.exitProcess

@Ser class Command_PhiMakeSenseOfPHPSpew(val spew: String) : Servant {
    @AllOpen @BuildParseTree
    class CalculatorParser : BaseParser<Any?>() {
        val shitForSendingToMapTool = mutableListOf<FileLine>()
        val items: MutableList<Item> = mutableListOf()

        sealed class Item {
            class Plain(val text: String) : Item()
            class FileLine(val text: String) : Item()
        }

        fun spew(): Rule {
            return ZeroOrMore(FirstOf(
                phpFileName(),
                Sequence(ANY, action {items += Item.Plain(it.match)})))
        }

        fun phpFileName(): Rule {
            return Sequence(
                Sequence(OneOrMore(FirstOf(
                    CharRange('0', '9'),
                    CharRange('a', 'z'),
                    CharRange('A', 'Z'),
                    AnyOf("_-"))), action {it.valueStack.push(it.match)},
                         ".php",
                         FirstOf(
                             Sequence(':', numberOrOptionallyRange()),
                             Sequence('(', numberOrOptionallyRange(), ')'),
                             Sequence(" on line ", numberOrOptionallyRange()))
                ),

                action {
                    val line = it.valueStack.pop() as Int
                    val file = it.valueStack.pop() as String
                    val interestingFile = APSBackPHPDevTools.findInterestingFile(file + ".php")
                    if (interestingFile != null) {
                        shitForSendingToMapTool += FileLine(interestingFile.shortName, line)
                        items += Item.FileLine(it.match)
                    } else {
                        items += Item.Plain(it.match)
                    }
                })
        }

        fun numberOrOptionallyRange(): Rule {
            return Sequence(OneOrMore(CharRange('0', '9')),
                            action {it.valueStack.push(it.match.toInt())},
                            Optional(Sequence('-', OneOrMore(CharRange('0', '9')))))}

        fun action(block: (Context<Any?>) -> Unit): Action<Any?> {
            return Action {
                block(it)
                true
            }
        }

    }

    override fun serve() = serveMumblingCommand("fegh") {con->
        val input = spew
        val parser = Parboiled.createParser(CalculatorParser::class.java)
        val result = ReportingParseRunner<Any?>(parser.spew()).run(input)
        if (result.hasErrors()) {
            con.bark("Can't parse your shit")
            return@serveMumblingCommand
        }

        val mappedLocations = runMapPhizdetsStackTool(con, parser.shitForSendingToMapTool)
            ?.mappedStack
            ?: return@serveMumblingCommand

        var fileLineIndex = 0
        for (item in parser.items) {
            exhaustive=when (item) {
                is CalculatorParser.Item.Plain -> con.mumbleNoln(item.text)
                is Command_PhiMakeSenseOfPHPSpew.CalculatorParser.Item.FileLine -> {
                    val spewFileLine = parser.shitForSendingToMapTool[fileLineIndex]
                    val mappedFileLine = mappedLocations[fileLineIndex]
                    ++fileLineIndex

                    val interestingFile = APSBackPHPDevTools.findInterestingFile(spewFileLine.file)!!
                    con.link(item.text, interestingFile.fullPath, spewFileLine.line)

                    if (listOf("aps-back.php", "phizdets-stdlib.php").contains(interestingFile.shortName)) {
                        con.mumbleNoln(" (")
                        con.link("--1", interestingFile.fullPath + "--1", spewFileLine.line)
                        con.mumbleNoln(")")
                    }

                    if (mappedFileLine != null) {
                        con.mumbleNoln(" <-- ")
                        var shortName = mappedFileLine.file
                        val lastSlash = Math.max(shortName.lastIndexOf("/"), shortName.lastIndexOf("\\"))
                        check(lastSlash != -1) {"8a31ff9e-c45c-4ad7-838a-03a9da2f22ec"}
                        shortName = shortName.substring(lastSlash + 1)
                        check(shortName.isNotBlank()) {"a972d84b-c38b-4af1-850b-9d5d1793f4e7"}
                        con.link(shortName + ":" + mappedFileLine.line, mappedFileLine.file, mappedFileLine.line)
                    } else {}
                }
            }
        }

        // con.mumble(spew)
        con.mumble("OK")
    }
}

object Command_PhiMakeSenseOfPHPSpewTest2 {
    @JvmStatic
    fun main(args: Array<String>) {
        MumblingCommandGlobal.dontUseIDEAAndShitToStdoutInstead = true
        val res = Command_PhiMakeSenseOfPHPSpew(someSpewForTesting).serve()
        clog(res)
    }
}

object Command_PhiMakeSenseOfPHPSpewTest {
    @JvmStatic
    fun main(args: Array<String>) {
        sendCommandToIDEABackdoor(Command_PhiMakeSenseOfPHPSpew(someSpewForTesting))
    }
}




private val someSpewForTesting = """
SSED
phiQuickTest_constructor: PASSED
PHP Warning:  Cannot modify header information - headers already sent by (output started at /media/sf_phizdetsc-php/phi-engine.php:1295) in /media/sf_phizdetsc-php/phi-engine.php on line 2249
PHP Stack trace:
PHP   1. {main}() /media/sf_phizdetsc-php/try-shit--aps-back.php:0
PHP   2. require_once() /media/sf_phizdetsc-php/try-shit--aps-back.php:6
PHP   3. phiExpressionStatement() /media/sf_phizdetsc-php/fuck-around--aps-back.php:8120
PHP   4. PhiBinaryOperation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1890
PHP   5. PhiInvocation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1493
PHP   6. PhiFunction->invoke() /media/sf_phizdetsc-php/phi-engine.php:2282
PHP   7. {closure:/media/sf_phizdetsc-php/fuck-around--aps-back.php:4-8119}() /media/sf_phizdetsc-php/phi-engine.php:339
PHP   8. phiExpressionStatement() /media/sf_phizdetsc-php/fuck-around--aps-back.php:8117
PHP   9. PhiInvocation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1890
PHP  10. PhiFunction->invoke() /media/sf_phizdetsc-php/phi-engine.php:2282
PHP  11. {closure:/media/sf_phizdetsc-php/fuck-around--aps-back.php:4073-4075}() /media/sf_phizdetsc-php/phi-engine.php:339
PHP  12. phiExpressionStatement() /media/sf_phizdetsc-php/fuck-around--aps-back.php:4074
PHP  13. PhiInvocation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1890
PHP  14. PhiFunction->invoke() /media/sf_phizdetsc-php/phi-engine.php:2282
PHP  15. {closure:/media/sf_phizdetsc-php/fuck-around--aps-back.php:43-79}() /media/sf_phizdetsc-php/phi-engine.php:339
PHP  16. phiExpressionStatement() /media/sf_phizdetsc-php/fuck-around--aps-back.php:45
PHP  17. PhiInvocation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1890
PHP  18. PhiFunction->invoke() /media/sf_phizdetsc-php/phi-engine.php:2282
PHP  19. {closure:/media/sf_phizdetsc-php/fuck-around--aps-back.php:4324-4326}() /media/sf_phizdetsc-php/phi-engine.php:339
PHP  20. phiExpressionStatement() /media/sf_phizdetsc-php/fuck-around--aps-back.php:4325
PHP  21. PhiInvocation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1890
PHP  22. header() /media/sf_phizdetsc-php/phi-engine.php:2249
PHP Notice:  Undefined index: QUERY_STRING in /media/sf_phizdetsc-php/phi-engine.php(2609) : eval()'d code on line 1
PHP Stack trace:
PHP   1. {main}() /media/sf_phizdetsc-php/try-shit--aps-back.php:0
PHP   2. require_once() /media/sf_phizdetsc-php/try-shit--aps-back.php:6
PHP   3. phiExpressionStatement() /media/sf_phizdetsc-php/fuck-around--aps-back.php:8120
PHP   4. PhiBinaryOperation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1890
PHP   5. PhiInvocation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1493
PHP   6. PhiFunction->invoke() /media/sf_phizdetsc-php/phi-engine.php:2282
PHP   7. {closure:/media/sf_phizdetsc-php/fuck-around--aps-back.php:4-8119}() /media/sf_phizdetsc-php/phi-engine.php:339
PHP   8. phiExpressionStatement() /media/sf_phizdetsc-php/fuck-around--aps-back.php:8117
PHP   9. PhiInvocation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1890
PHP  10. PhiFunction->invoke() /media/sf_phizdetsc-php/phi-engine.php:2282
PHP  11. {closure:/media/sf_phizdetsc-php/fuck-around--aps-back.php:4073-4075}() /media/sf_phizdetsc-php/phi-engine.php:339
PHP  12. phiExpressionStatement() /media/sf_phizdetsc-php/fuck-around--aps-back.php:4074
PHP  13. PhiInvocation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1890
PHP  14. PhiFunction->invoke() /media/sf_phizdetsc-php/phi-engine.php:2282
PHP  15. {closure:/media/sf_phizdetsc-php/fuck-around--aps-back.php:43-79}() /media/sf_phizdetsc-php/phi-engine.php:339
PHP  16. phiVars() /media/sf_phizdetsc-php/fuck-around--aps-back.php:46
PHP  17. PhiInvocation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:2439
PHP  18. PhiDot->evaluate() /media/sf_phizdetsc-php/phi-engine.php:2280
PHP  19. PhiObject->getProperty() /media/sf_phizdetsc-php/phi-engine.php:54
PHP  20. PhiObject->getProperty() /media/sf_phizdetsc-php/phi-engine.php:175
PHP  21. PhiFunction->invoke() /media/sf_phizdetsc-php/phi-engine.php:168
PHP  22. {closure:/media/sf_phizdetsc-php/fuck-around--aps-back.php:4281-4285}() /media/sf_phizdetsc-php/phi-engine.php:339
PHP  23. phiEvaluate() /media/sf_phizdetsc-php/fuck-around--aps-back.php:4284
PHP  24. PhiConditional->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1953
PHP  25. PhiBinaryOperation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:2360
PHP  26. PhiBinaryOperation->testReferenceEquality() /media/sf_phizdetsc-php/phi-engine.php:1481
PHP  27. PhiUnaryOperation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1416
PHP  28. PhiBinaryOperation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:2006
PHP  29. PhiInvocation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1493
PHP  30. phiEval() /media/sf_phizdetsc-php/phi-engine.php:2249
PHP  31. eval() /media/sf_phizdetsc-php/phi-engine.php:2609
PHP Fatal error:  Uncaught Exception: Illegal cast in /media/sf_phizdetsc-php/phi-engine.php:1909
Stack trace:
#0 /media/sf_phizdetsc-php/phizdets-stdlib.php(2123): phiThrow(Object(PhiNew))
#1 /media/sf_phizdetsc-php/phi-engine.php(339): {closure}()
#2 /media/sf_phizdetsc-php/phi-engine.php(2282): PhiFunction->invoke(Object(PhiObject), Array)
#3 /media/sf_phizdetsc-php/phi-engine.php(2364): PhiInvocation->evaluate()
#4 /media/sf_phizdetsc-php/phi-engine.php(1953): PhiConditional->evaluate()
#5 /media/sf_phizdetsc-php/fuck-around--aps-back.php(4284): phiEvaluate(Object(PhiConditional))
#6 /media/sf_phizdetsc-php/phi-engine.php(339): {closure}()
#7 /media/sf_phizdetsc-php/phi-engine.php(168): PhiFunction->invoke(Object(PhiObject), Array)
#8 /media/sf_phizdetsc-php/phi-engine.php(175): PhiObject->getProperty('queryString', Array)
#9 /media/sf_phizdetsc-php/phi-engine.php(54): PhiObject->getProperty('queryString')
#10 /media/sf_phizdetsc-php/phi-engine.php(2280): PhiDot->evaluate()
#11 /media/sf_phizdetsc-php/phi-engine.php(2439): in /media/sf_phizdetsc-php/phi-engine.php on line 1909

Process finished with exit code 255
"""














