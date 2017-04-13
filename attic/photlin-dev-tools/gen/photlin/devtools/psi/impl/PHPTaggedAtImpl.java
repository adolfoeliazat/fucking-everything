// This is a generated file. Not intended for manual editing.
package photlin.devtools.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static photlin.devtools.psi.PHPTaggedTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import photlin.devtools.psi.*;

public class PHPTaggedAtImpl extends ASTWrapperPsiElement implements PHPTaggedAt {

  public PHPTaggedAtImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PHPTaggedVisitor visitor) {
    visitor.visitAt(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PHPTaggedVisitor) accept((PHPTaggedVisitor)visitor);
    else super.accept(visitor);
  }

}
