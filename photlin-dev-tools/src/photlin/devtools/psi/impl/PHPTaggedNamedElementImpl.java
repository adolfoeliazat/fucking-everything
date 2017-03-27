package photlin.devtools.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import photlin.devtools.psi.*;

public abstract class PHPTaggedNamedElementImpl extends ASTWrapperPsiElement implements PHPTaggedNamedElement {
    public PHPTaggedNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }
}


