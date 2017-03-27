package photlin.devtools.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.tree.IElementType
import photlin.devtools.*
import javax.swing.Icon

class PHPTaggedTokenType(debugName: String) : IElementType(debugName, PHPTaggedLanguage) {
    override fun toString(): String {
        return "PHPTaggedTokenType." + super.toString()
    }
}

class PHPTaggedElementType(debugName: String) : IElementType(debugName, PHPTaggedLanguage)

class PHPTaggedFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, PHPTaggedLanguage) {
    override fun getFileType() = PHPTaggedFileType
    override fun toString() = "PHPTagged File"

    override fun getIcon(flags: Int): Icon? {
        return super.getIcon(flags)
    }
}














