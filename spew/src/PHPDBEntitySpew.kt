package vgrechka.spew

import org.jetbrains.kotlin.psi.KtFile
import vgrechka.*

class PHPDBEntitySpew : Spew {
    override fun ignite(ktFile: KtFile, outputFilePath: String, spewResults: SpewResults) {
        CommonDBEntitySpew(ktFile, outputFilePath, spewResults)
    }
    private fun noise(x: Any?) {
        if (false) clog(x)
    }
}

