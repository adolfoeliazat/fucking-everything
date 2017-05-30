package alraune.back

sealed class AlBackToFrontCommand {
    @Suppress("LeakingThis")
    val opcode: String = this::class.simpleName!!
}

class AlBackToFrontCommandList(xs: List<AlBackToFrontCommand>) : ArrayList<AlBackToFrontCommand>(xs) {
    companion object {
        fun commands(vararg xs: AlBackToFrontCommand): AlBackToFrontCommandList {
            return AlBackToFrontCommandList(xs.toList())
        }
    }
}

class SayWarmFuckYou(val toWhom: String) : AlBackToFrontCommand()
class SetClickHandler(val targetDomid: String, val actions: AlBackToFrontCommandList) : AlBackToFrontCommand()


