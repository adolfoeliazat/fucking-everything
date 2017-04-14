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
import com.intellij.psi.util.PsiTreeUtil;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.PyNames;
import vgrechka.phizdetsidea.phizdets.psi.LanguageLevel;
import vgrechka.phizdetsidea.phizdets.psi.PyClass;
import vgrechka.phizdetsidea.phizdets.psi.PyElementGenerator;
import vgrechka.phizdetsidea.phizdets.psi.PyExpression;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey.Ivanov
 * Date: 03.03.2010
 * Time: 16:49:59
 */
public class TransformClassicClassQuickFix implements LocalQuickFix {
  @NotNull
  public String getFamilyName() {
    return PyBundle.message("QFIX.classic.class.transform");
  }

  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement psiElement = descriptor.getPsiElement();
    psiElement = PsiTreeUtil.getParentOfType(psiElement, PyClass.class);
    if (psiElement != null) {
      PyClass pyClass = (PyClass) psiElement;
      PyExpression[] superClassExpressions = pyClass.getSuperClassExpressions();
      PyElementGenerator generator = PyElementGenerator.getInstance(project);
      if (superClassExpressions.length == 0) {
        pyClass.replace(generator.createFromText(LanguageLevel.getDefault(), PyClass.class,
                                                 "class " + pyClass.getName() + "(" +
                                                 PyNames.OBJECT + "):\n    " + pyClass.getStatementList().getText()));
      } else {
        StringBuilder stringBuilder = new StringBuilder("class ");
        stringBuilder.append(pyClass.getName()).append("(");
        for (PyExpression expression: superClassExpressions) {
          stringBuilder.append(expression.getText()).append(", ");
        }
        stringBuilder.append(PyNames.OBJECT).append(":\n    ");
        stringBuilder.append(pyClass.getStatementList().getText());
        pyClass.replace(generator.createFromText(LanguageLevel.getDefault(), PyClass.class, stringBuilder.toString()));
      }
    }
  }
}
