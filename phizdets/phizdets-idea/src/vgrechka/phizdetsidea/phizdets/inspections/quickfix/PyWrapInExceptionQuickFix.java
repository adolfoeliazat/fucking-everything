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
import org.jetbrains.annotations.NotNull;

public class PyWrapInExceptionQuickFix implements LocalQuickFix {
  @NotNull
  @Override
  public String getFamilyName() {
    return PyBundle.message("QFIX.NAME.wrap.in.exception");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    final PsiElement string = descriptor.getPsiElement();
    if (string instanceof PyStringLiteralExpression) {
      final PyCallExpression callExpression =
        PyElementGenerator.getInstance(project).createCallExpression(LanguageLevel.forElement(string), "Exception");
      final PyArgumentList list = callExpression.getArgumentList();
      assert list != null;
      list.addArgument((PyExpression)string);
      string.replace(callExpression);
    }
  }
}