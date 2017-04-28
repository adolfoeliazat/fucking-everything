package vgrechka

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.stage.WindowEvent
import org.parboiled.*
import org.parboiled.annotations.BuildParseTree
import org.parboiled.parserunners.ReportingParseRunner
import kotlin.system.exitProcess

class CamelToSnakeTool : Application() {
    @AllOpen @BuildParseTree
    class Parser : BaseParser<Any?>() {
        val items: MutableList<Item> = mutableListOf()

        sealed class Item {
            class UnparsableShit(val text: String) : Item()
            class Identifier(val text: String) : Item()
        }

        fun spew(): Rule {
            return ZeroOrMore(FirstOf(
                identifier(),
                Sequence(ANY, action {items += Item.UnparsableShit(it.match)})))
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
                JFXStuff.errorAlert("Can't parse your shit")
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
        // camelArea.text = "fuckingBitch MotherFucker"
        vbox.children += snakeArea
        val scene = Scene(vbox)

        primaryStage.title = "Camel to Snake"
        primaryStage.scene = scene
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(CamelToSnakeTool::class.java, *args)
        }
    }
}
