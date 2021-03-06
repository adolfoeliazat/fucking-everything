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
import vgrechka.phizdetsidea.phizdets.psi.PyExpressionStatement;
import vgrechka.phizdetsidea.phizdets.psi.PyTupleExpression;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * Author: Alexey.Ivanov
 * Date:   06.03.2010
 * Time:   16:50:53
 */
public class ReplaceListComprehensionsQuickFix implements LocalQuickFix {
  @NotNull
  @Override
  public String getFamilyName() {
    return PyBundle.message("INTN.replace.list.comprehensions");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement expression = descriptor.getPsiElement();
    if (expression instanceof PyTupleExpression) {
      PyElementGenerator elementGenerator = PyElementGenerator.getInstance(project);
      PyExpressionStatement statement = elementGenerator.createFromText(LanguageLevel.forElement(expression), PyExpressionStatement.class,
                                                                        "(" + expression.getText() + ")");
      expression.replace(statement.getExpression());
    }
  }
}