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
import com.intellij.psi.util.PsiTreeUtil;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.PyNames;
import vgrechka.phizdetsidea.phizdets.psi.PyClass;
import vgrechka.phizdetsidea.phizdets.psi.PyFunction;
import vgrechka.phizdetsidea.phizdets.psi.search.PySuperMethodsSearch;
import vgrechka.phizdetsidea.phizdets.psi.types.TypeEvalContext;
import vgrechka.phizdetsidea.phizdets.refactoring.changeSignature.PyChangeSignatureDialog;
import vgrechka.phizdetsidea.phizdets.refactoring.changeSignature.PyMethodDescriptor;
import vgrechka.phizdetsidea.phizdets.refactoring.changeSignature.PyParameterInfo;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PyChangeSignatureQuickFix implements LocalQuickFix {

  private final boolean myOverridenMethod;

  public PyChangeSignatureQuickFix(boolean overriddenMethod) {
    myOverridenMethod = overriddenMethod;
  }

  @NotNull
  public String getFamilyName() {
    return PyBundle.message("QFIX.NAME.change.signature");
  }

  public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
    final PyFunction function = PsiTreeUtil.getParentOfType(descriptor.getPsiElement(), PyFunction.class);
    if (function == null) return;
    final PyClass cls = function.getContainingClass();
    assert cls != null;
    final String functionName = function.getName();
    final String complementaryName = PyNames.NEW.equals(functionName) ? PyNames.INIT : PyNames.NEW;
    final TypeEvalContext context = TypeEvalContext.userInitiated(project, descriptor.getEndElement().getContainingFile());
    final PyFunction complementaryMethod = myOverridenMethod ? (PyFunction)PySuperMethodsSearch.search(function, context).findFirst()
                                                             : cls.findMethodByName(complementaryName, true, null);

    assert complementaryMethod != null;
    final PyMethodDescriptor methodDescriptor = new PyMethodDescriptor(function) {
      @Override
      public List<PyParameterInfo> getParameters() {
        final List<PyParameterInfo> parameterInfos = super.getParameters();
        final int paramLength = function.getParameterList().getParameters().length;
        final int complementaryParamLength = complementaryMethod.getParameterList().getParameters().length;
        if (complementaryParamLength > paramLength)
          parameterInfos.add(new PyParameterInfo(-1, "**kwargs", "", false));
        return parameterInfos;
      }
    };
    final PyChangeSignatureDialog dialog = new PyChangeSignatureDialog(project, methodDescriptor);
    dialog.show();
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }
}
