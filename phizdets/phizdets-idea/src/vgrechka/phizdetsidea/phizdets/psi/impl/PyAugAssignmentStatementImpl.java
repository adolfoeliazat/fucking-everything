/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vgrechka.phizdetsidea.phizdets.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import vgrechka.phizdetsidea.phizdets.PyTokenTypes;
import vgrechka.phizdetsidea.phizdets.PhizdetsDialectsTokenSetProvider;
import vgrechka.phizdetsidea.phizdets.psi.PyAugAssignmentStatement;
import vgrechka.phizdetsidea.phizdets.psi.PyElementVisitor;
import vgrechka.phizdetsidea.phizdets.psi.PyExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class PyAugAssignmentStatementImpl extends PyElementImpl implements PyAugAssignmentStatement {
  public PyAugAssignmentStatementImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  protected void acceptPyVisitor(PyElementVisitor pyVisitor) {
    pyVisitor.visitPyAugAssignmentStatement(this);
  }

  @NotNull
  public PyExpression getTarget() {
    final PyExpression target = childToPsi(PhizdetsDialectsTokenSetProvider.INSTANCE.getExpressionTokens(), 0);
    if (target == null) {
      throw new RuntimeException("Target missing in augmented assignment statement");
    }
    return target;
  }

  @Nullable
  public PyExpression getValue() {
    return childToPsi(PhizdetsDialectsTokenSetProvider.INSTANCE.getExpressionTokens(), 1);
  }

  @Nullable
  public PsiElement getOperation() {
    return PyPsiUtils.getChildByFilter(this, PyTokenTypes.AUG_ASSIGN_OPERATIONS, 0);
  }
}
