package vgrechka.phizdetsidea.phizdets.findUsages;

import com.intellij.find.findUsages.FindUsagesHandler;
import vgrechka.phizdetsidea.phizdets.psi.PyTargetExpression;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mikhail Golubev
 */
public class PyTargetExpressionFindUsagesHandler extends FindUsagesHandler {
  public PyTargetExpressionFindUsagesHandler(@NotNull PyTargetExpression psiElement) {
    super(psiElement);
  }
}
