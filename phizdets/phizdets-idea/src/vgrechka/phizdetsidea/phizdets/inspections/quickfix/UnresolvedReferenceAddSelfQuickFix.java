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

import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.psi.LanguageLevel;
import vgrechka.phizdetsidea.phizdets.psi.PyElementGenerator;
import vgrechka.phizdetsidea.phizdets.psi.PyExpression;
import vgrechka.phizdetsidea.phizdets.psi.PyReferenceExpression;
import org.jetbrains.annotations.NotNull;

/**
 * User: catherine
 *
 * QuickFix to add self to unresolved reference
 */
public class UnresolvedReferenceAddSelfQuickFix implements LocalQuickFix, HighPriorityAction {
  private PyReferenceExpression myElement;
  private String myQualifier;

  public UnresolvedReferenceAddSelfQuickFix(@NotNull final PyReferenceExpression element, @NotNull final String qualifier) {
    myElement = element;
    myQualifier = qualifier;
  }

  @NotNull
  public String getName() {
    return PyBundle.message("QFIX.unresolved.reference", myElement.getText(), myQualifier);
  }

  @NotNull
  public String getFamilyName() {
    return "Add qualifier";
  }

  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PyElementGenerator elementGenerator = PyElementGenerator.getInstance(project);
    PyExpression expression = elementGenerator.createExpressionFromText(LanguageLevel.forElement(myElement),
                                                                        myQualifier + "." + myElement.getText());
    myElement.replace(expression);
  }
}