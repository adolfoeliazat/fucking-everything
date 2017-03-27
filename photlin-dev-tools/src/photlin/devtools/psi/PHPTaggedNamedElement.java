package photlin.devtools.psi;

import com.intellij.psi.PsiNameIdentifierOwner;

public interface PHPTaggedNamedElement extends PsiNameIdentifierOwner {
    String getValue();
}
