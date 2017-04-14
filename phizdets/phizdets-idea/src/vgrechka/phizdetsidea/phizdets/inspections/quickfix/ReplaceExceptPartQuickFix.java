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
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.PyTokenTypes;
import vgrechka.phizdetsidea.phizdets.psi.LanguageLevel;
import vgrechka.phizdetsidea.phizdets.psi.PyElementGenerator;
import vgrechka.phizdetsidea.phizdets.psi.PyExceptPart;
import vgrechka.phizdetsidea.phizdets.psi.PyTryExceptStatement;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey.Ivanov
 */
public class ReplaceExceptPartQuickFix implements LocalQuickFix {
  @NotNull
  @Override
  public String getFamilyName() {
    return PyBundle.message("INTN.convert.except.to");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement exceptPart = descriptor.getPsiElement();
    if (exceptPart instanceof PyExceptPart) {
      PyElementGenerator elementGenerator = PyElementGenerator.getInstance(project);
      PsiElement element = ((PyExceptPart)exceptPart).getExceptClass().getNextSibling();
      while (element instanceof PsiWhiteSpace) {
        element = element.getNextSibling();
      }
      assert element != null;
      PyTryExceptStatement newElement =
        elementGenerator.createFromText(LanguageLevel.forElement(exceptPart), PyTryExceptStatement.class, "try:  pass except a as b:  pass");
      ASTNode node = newElement.getExceptParts()[0].getNode().findChildByType(PyTokenTypes.AS_KEYWORD);
      assert node != null;
      element.replace(node.getPsi());
    }
  }

}
