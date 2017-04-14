/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usageView.UsageInfo;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.documentation.docstrings.PyDocstringGenerator;
import vgrechka.phizdetsidea.phizdets.psi.PyCallExpression;
import vgrechka.phizdetsidea.phizdets.psi.PyFunction;
import vgrechka.phizdetsidea.phizdets.psi.PyParameter;
import vgrechka.phizdetsidea.phizdets.psi.PyStringLiteralExpression;
import vgrechka.phizdetsidea.phizdets.psi.resolve.PyResolveContext;
import vgrechka.phizdetsidea.phizdets.refactoring.PyRefactoringUtil;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;

public class PyRemoveParameterQuickFix implements LocalQuickFix {

  @NotNull
  public String getFamilyName() {
    return PyBundle.message("QFIX.NAME.remove.parameter");
  }

  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    final PsiElement parameter = descriptor.getPsiElement();
    assert parameter instanceof PyParameter;

    final PyFunction function = PsiTreeUtil.getParentOfType(parameter, PyFunction.class);

    if (function != null) {
      final PyResolveContext resolveContext = PyResolveContext.noImplicits();

      StreamEx
        .of(PyRefactoringUtil.findUsages(function, false))
        .map(UsageInfo::getElement)
        .nonNull()
        .map(PsiElement::getParent)
        .select(PyCallExpression.class)
        .flatMap(callExpression -> callExpression.multiMapArguments(resolveContext).stream())
        .flatMap(mapping -> mapping.getMappedParameters().entrySet().stream())
        .filter(entry -> entry.getValue() == parameter)
        .forEach(entry -> entry.getKey().delete());

      final PyStringLiteralExpression docStringExpression = function.getDocStringExpression();
      final String parameterName = ((PyParameter)parameter).getName();
      if (docStringExpression != null && parameterName != null) {
        PyDocstringGenerator.forDocStringOwner(function).withoutParam(parameterName).buildAndInsert();
      }
    }

    parameter.delete();
  }
}
