@file:Suppress("unused")
package alraune.back

abstract class AlBackToFrontCommand {
    @Suppress("LeakingThis")
    val opcode: String = TSPile.commandClassNameToTSTypeName(this::class.simpleName!!)
}

class SayWarmFuckYou(val toWhom: String) : AlBackToFrontCommand()

class SetClickHandler(val targetDomid: String,
                      val actions: List<AlBackToFrontCommand>) : AlBackToFrontCommand()

class OpenModalOnElementClickCommand(val triggerElementDomid: String,
                                     val modalHtml: String,
                                     val initCommands: List<AlBackToFrontCommand>) : AlBackToFrontCommand()

class CreateTextControlCommand(val placeHolderDomid: String,
                               val propName: String,
                               val value: String) : AlBackToFrontCommand()


