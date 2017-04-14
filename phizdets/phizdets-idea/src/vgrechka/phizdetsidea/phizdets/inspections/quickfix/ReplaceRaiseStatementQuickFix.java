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
import vgrechka.phizdetsidea.phizdets.psi.LanguageLevel;
import vgrechka.phizdetsidea.phizdets.psi.PyElementGenerator;
import vgrechka.phizdetsidea.phizdets.psi.PyExpression;
import vgrechka.phizdetsidea.phizdets.psi.PyRaiseStatement;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey.Ivanov
 */
public class ReplaceRaiseStatementQuickFix implements LocalQuickFix {
  @NotNull
  @Override
  public String getFamilyName() {
    return PyBundle.message("INTN.replace.raise.statement");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement raiseStatement = descriptor.getPsiElement();
    if (raiseStatement instanceof PyRaiseStatement) {
      PyExpression[] expressions = ((PyRaiseStatement)raiseStatement).getExpressions();
      PyElementGenerator elementGenerator = PyElementGenerator.getInstance(project);
      String newExpressionText = expressions[0].getText() + "(" + expressions[1].getText() + ")";
      if (expressions.length == 2) {
        raiseStatement.replace(elementGenerator.createFromText(LanguageLevel.forElement(raiseStatement), PyRaiseStatement.class, "raise " + newExpressionText));
      } else if (expressions.length == 3) {
        raiseStatement.replace(elementGenerator.createFromText(LanguageLevel.forElement(raiseStatement), PyRaiseStatement.class,
                                                               "raise " + newExpressionText + ".with_traceback(" + expressions[2].getText() + ")"));
      }
    }
  }
}