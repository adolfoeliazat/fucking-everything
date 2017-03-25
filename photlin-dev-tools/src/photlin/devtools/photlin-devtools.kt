package photlin.devtools

import vgrechka.*

object PhotlinDevToolsGlobal {
    val rpcServerPort = 12321
}

@Ser class DevToolsTestResultRequest(
    val rawResponseFromPHPScript: String
)


