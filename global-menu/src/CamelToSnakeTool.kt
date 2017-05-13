package vgrechka

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.stage.WindowEvent
import org.parboiled.*
import org.parboiled.annotations.BuildParseTree
import org.parboiled.parserunners.ReportingParseRunner
import kotlin.system.exitProcess

// Java parser example:
// https://github.com/sirthias/parboiled/blob/master/examples-java/src/main/java/org/parboiled/examples/java/JavaParser.java

class CamelToSnakePHPTool : CamelToSnakeTool() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(CamelToSnakePHPTool::class.java, *args)
        }
    }
}

abstract class CamelToSnakeTool : Application() {
    @AllOpen @BuildParseTree
    class Parser : BaseParser<Any?>() {
        val items: MutableList<Item> = mutableListOf()

        sealed class Item {
            class UnparsableShit(val text: String) : Item()
            class Identifier(val text: String) : Item()
        }

        fun spew(): Rule {
            return ZeroOrMore(FirstOf(
                singleLineComment(),
                multilineComment(),
                stringLiteral(),
                identifier(),
                otherShit()))
        }

        fun otherShit(): Rule {
            return Sequence(ANY, action {items += Item.UnparsableShit(it.match)})
        }

        fun singleLineComment(): Rule {
            return Sequence(
                Sequence(
                    "//",
                    ZeroOrMore(TestNot(AnyOf("\r\n")), BaseParser.ANY),
                    FirstOf("\r\n", '\r', '\n', BaseParser.EOI)
                ),
                action {items += Item.UnparsableShit(it.match)})
        }

        fun multilineComment(): Rule {
            return Sequence(
                Sequence("/*", ZeroOrMore(TestNot("*/"), BaseParser.ANY), "*/"),
                action {items += Item.UnparsableShit(it.match)})
        }

        fun stringLiteral(): Rule {
            return Sequence(
                Sequence(
                    '"',
                    ZeroOrMore(
                        FirstOf(
                            escape(),
                            Sequence(TestNot(AnyOf("\r\n\"\\")), BaseParser.ANY))
                    ).suppressSubnodes(),
                    '"'),
                action {items += Item.UnparsableShit(it.match)})
        }

        fun escape(): Rule {
            return Sequence('\\', FirstOf(AnyOf("btnfr\"\'\\"), octalEscape(), unicodeEscape()))
        }

        fun octalEscape(): Rule {
            return FirstOf(
                Sequence(CharRange('0', '3'), CharRange('0', '7'), CharRange('0', '7')),
                Sequence(CharRange('0', '7'), CharRange('0', '7')),
                CharRange('0', '7')
            )
        }

        fun unicodeEscape(): Rule {
            return Sequence(OneOrMore('u'), hexDigit(), hexDigit(), hexDigit(), hexDigit())
        }

        fun hexDigit(): Rule {
            return FirstOf(CharRange('a', 'f'), CharRange('A', 'F'), CharRange('0', '9'))
        }

        fun identifier(): Rule {
            return Sequence(
                OneOrMore(FirstOf(
                    CharRange('0', '9'),
                    CharRange('a', 'z'),
                    CharRange('A', 'Z'),
                    AnyOf("_"))),
                action {items += Item.Identifier(it.match)})
        }

        fun action(block: (Context<Any?>) -> Unit): Action<Any?> {
            return Action {
                block(it)
                true
            }
        }
    }

    override fun start(primaryStage: Stage) {
        primaryStage.addEventHandler(WindowEvent.WINDOW_HIDDEN) {e->
            exitProcess(0)
        }

        val vbox = VBox()
        val camelArea = TextArea()
        val snakeArea = TextArea()
        vbox.children += camelArea
        camelArea.textProperty().addListener {_, _, _ ->
            val input = camelArea.text

            val parser = Parboiled.createParser(Parser::class.java)
            val result = ReportingParseRunner<Any?>(parser.spew()).run(input)
            if (result.hasErrors()) {
                JFXPile.errorAlert("Can't parse your shit")
                return@addListener
            }

            snakeArea.text = stringBuild {s->
                for (item in parser.items) {
                    s += when (item) {
                        is CamelToSnakeTool.Parser.Item.UnparsableShit -> item.text
                        is CamelToSnakeTool.Parser.Item.Identifier -> {
                            stringBuild {newIdent->
                                for ((i, c) in item.text.withIndex()) {
                                    if (i == 0 || c.isLowerCase()) {
                                        newIdent += c
                                    } else {
                                        newIdent += "_${c.toLowerCase()}"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        setTestInput(camelArea)
        vbox.children += snakeArea
        VBox.setVgrow(camelArea, Priority.ALWAYS)
        VBox.setVgrow(snakeArea, Priority.ALWAYS)
        val scene = Scene(vbox)

        primaryStage.title = "Camel to Snake"
        primaryStage.scene = scene
        primaryStage.show()
    }

    fun setTestInput(camelArea: TextArea) {
//        camelArea.text = """
//function phiThrow(${'$'}expr) {
//    ${'$'}phiValue = ${'$'}expr->evaluate();
//    if (!(${'$'}phiValue instanceof PhiObject))
//        throw new PhiIllegalStateException("d6b5d1bf-c9d9-4aa7-b9f9-420bd0124b1f");
//
//    ${'$'}messagePhiValue = ${'$'}phiValue->getProperty('message');
////    if (${'$'}messagePhiValue instanceof PhiUndefined) { // TODO:vgrechka @kill
////        ${'$'}messagePhiValue = ${'$'}phiValue->getProperty('message_ujvw20${'$'}_0');
////    }
//
//    if (${'$'}messagePhiValue instanceof PhiString) {
//        ${'$'}message = ${'$'}messagePhiValue->getValue();
//    }
//    else if (${'$'}messagePhiValue instanceof PhiUndefined) {
//        ${'$'}message = "";
//    }
//    else {
//        throw new PhiIllegalStateException("cbba8949-ba96-43d5-93f1-dd84bd002d67");
//    }
//
//    ${'$'}exception = new PhiBloodyException(${'$'}message, ${'$'}phiValue);
//    throw ${'$'}exception;
//}
//        """
    }
}
