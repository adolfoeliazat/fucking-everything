package alraune.back

import vgrechka.*

object AlDebugServerFiddling {
    val fuckDatabaseForPost = SetGetResetShit<Boolean>()

//    val nextRequestTimestamp = SetGetResetShit<XTimestamp>()
//    val nextGeneratedPassword = SetGetResetShit<String>()
//    val nextRequestError = SetGetResetShit<String>()
//    val nextGeneratedConfirmationSecret = SetGetResetShit<String>()
//    val nextGeneratedUserToken = SetGetResetShit<String>()
//    val nextOrderID = SetGetResetShit<Long>()
//    @Volatile var rejectAllRequestsNeedingDB: Boolean = false
}

class SetGetResetShit<T> {
    private @Volatile var value: T? = null

    fun getAndReset(): T? {
        val res = value
        value = null
        return res
    }

    fun set(newValue: T) {
        value = newValue
    }
}

class CodeStep(val title: String, val throwableForStack: Throwable, val stackStringLinesToDrop: Int)

fun handlePost_debug_post_fuckDatabaseForNextPost() {
    clog("So, database will be fucked up for next post")
    AlDebugServerFiddling.fuckDatabaseForPost.set(true)
}

fun handlePost_debug_post_dumpStackByID() {
    val data = rctx.postData.dumpStackByID
    val stack = AlBackPile.idToTagCreationStack[data.stackID] ?: bitch("5aaece41-c3f3-4eae-8c98-e7f69147ef3b")
    clog(stack
             .lines()
             .filter {line ->
                 !listOf(
                     "Tag.<init>",
                     "TagCtor.invoke")
                     .any {line.contains(it)}
             }
             .joinToString("\n"))
}

fun handlePost_debug_post_dumpBackCodePath() {
    val data = rctx.postData.dumpBackCodePath
    clog("\n=============== requestContextID = ${data.requestContextID} ===================")
    val ctx = AlBackDebug.idToRequestContext[data.requestContextID] ?: bitch("data.requestID = ${data.requestContextID}    225159bd-f456-4cb2-9503-b8e6be6d6139")
    for ((index, codeStep) in ctx.codeSteps.withIndex()) {
        clog()
        clog("${index + 1}) ${codeStep.title}")
        clog(codeStep.throwableForStack.stackTraceString
                 .lines()
                 .drop(codeStep.stackStringLinesToDrop)
                 .map {"    $it"}
                 .joinToString("\n"))
    }
}







