package alraune.back

abstract class AlBackToFrontCommand {
    @Suppress("LeakingThis")
    val opcode: String = this::class.simpleName!!
}

class SayWarmFuckYou(val toWhom: String) : AlBackToFrontCommand()
class SetClickHandler(val targetDomid: String, val actions: List<AlBackToFrontCommand>) : AlBackToFrontCommand()
class OpenModalCommand(val domid: String) : AlBackToFrontCommand()


