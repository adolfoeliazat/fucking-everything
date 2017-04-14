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
package vgrechka.phizdetsidea.phizdets.inspections.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.psi.*;
import vgrechka.phizdetsidea.phizdets.psi.resolve.PyResolveContext;
import vgrechka.phizdetsidea.phizdets.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

public class PyReplaceTupleWithListQuickFix implements LocalQuickFix {
  @NotNull
  @Override
  public String getFamilyName() {
    return PyBundle.message("QFIX.NAME.make.list");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement element = descriptor.getPsiElement();
    assert element instanceof PyAssignmentStatement;
    PyExpression[] targets = ((PyAssignmentStatement)element).getTargets();
    if (targets.length == 1 && targets[0] instanceof PySubscriptionExpression) {
      PySubscriptionExpression subscriptionExpression = (PySubscriptionExpression)targets[0];
      if (subscriptionExpression.getOperand() instanceof PyReferenceExpression) {
        PyReferenceExpression referenceExpression = (PyReferenceExpression)subscriptionExpression.getOperand();
        final TypeEvalContext context = TypeEvalContext.userInitiated(project, element.getContainingFile());
        final PyResolveContext resolveContext = PyResolveContext.noImplicits().withTypeEvalContext(context);
        element = referenceExpression.followAssignmentsChain(resolveContext).getElement();
        if (element instanceof PyParenthesizedExpression) {
          final PyExpression expression = ((PyParenthesizedExpression)element).getContainedExpression();
          replaceWithListLiteral(element, (PyTupleExpression)expression);
        }
        else if (element instanceof PyTupleExpression) {
          replaceWithListLiteral(element, (PyTupleExpression)element);
        }
      }
    }
  }

  private static void replaceWithListLiteral(PsiElement element, PyTupleExpression expression) {
    final String expressionText = expression.isEmpty() ? "" :expression.getText();
    final PyExpression literal = PyElementGenerator.getInstance(element.getProject()).
      createExpressionFromText(LanguageLevel.forElement(element),
                               "[" + expressionText + "]");
    element.replace(literal);
  }
}
