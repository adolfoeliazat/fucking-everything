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
import vgrechka.phizdetsidea.phizdets.psi.PyElementGenerator;
import vgrechka.phizdetsidea.phizdets.psi.PyNamedParameter;
import vgrechka.phizdetsidea.phizdets.psi.PyParameterList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Insert 'self' in a method that lacks any arguments
 * User: dcheryasov
 * Date: Nov 19, 2008
 */
public class AddSelfQuickFix implements LocalQuickFix {
  private final String myParamName;

  public AddSelfQuickFix(String paramName) {
    myParamName = paramName;
  }

  @NotNull
  public String getName() {
    return PyBundle.message("QFIX.add.parameter.self", myParamName);
  }

  @NonNls
  @NotNull
  public String getFamilyName() {
    return "Add parameter";
  }

  public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
    PsiElement element = descriptor.getPsiElement();
    if (element instanceof PyParameterList) {
      final PyParameterList parameterList = (PyParameterList)element;
      PyNamedParameter newParameter = PyElementGenerator.getInstance(project).createParameter(myParamName);
      parameterList.addParameter(newParameter);
    }
  }
}
