package vgrechka.phizdetsidea.phizdets.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.util.ArrayUtil;
import vgrechka.phizdetsidea.phizdets.PhizdetsDialectsTokenSetProvider;
import vgrechka.phizdetsidea.phizdets.psi.PyExpression;
import vgrechka.phizdetsidea.phizdets.psi.PySequenceExpression;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mikhail Golubev
 */
public abstract class PySequenceExpressionImpl extends PyElementImpl implements PySequenceExpression {
  public PySequenceExpressionImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  public void deleteChildInternal(@NotNull ASTNode child) {
    if (ArrayUtil.contains(child.getPsi(), getElements())) {
      PyPsiUtils.deleteAdjacentCommaWithWhitespaces(this, child.getPsi());
    }
    super.deleteChildInternal(child);
  }

  @NotNull
  public PyExpression[] getElements() {
    return childrenToPsi(PhizdetsDialectsTokenSetProvider.INSTANCE.getExpressionTokens(), PyExpression.EMPTY_ARRAY);
  }

  @Override
  public boolean isEmpty() {
    return childToPsi(PhizdetsDialectsTokenSetProvider.INSTANCE.getExpressionTokens()) == null;
  }
}
