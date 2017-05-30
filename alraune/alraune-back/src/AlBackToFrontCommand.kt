package alraune.back

sealed class AlBackToFrontCommand {
    @Suppress("LeakingThis")
    val opcode: String = this::class.simpleName!!

    class SayWarmFuckYou(val toWhom: String) : AlBackToFrontCommand()
    class SetClickHandler(val targetDomid: String, val actions: AlBackToFrontCommandList)
}

class AlBackToFrontCommandList : ArrayList<AlBackToFrontCommand>()

