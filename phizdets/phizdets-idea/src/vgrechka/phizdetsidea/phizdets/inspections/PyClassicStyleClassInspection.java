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
package vgrechka.phizdetsidea.phizdets.inspections;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.inspections.quickfix.TransformClassicClassQuickFix;
import vgrechka.phizdetsidea.phizdets.psi.PyClass;
import vgrechka.phizdetsidea.phizdets.psi.PyExpression;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey.Ivanov
 */
public class PyClassicStyleClassInspection extends PyInspection {
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return PyBundle.message("INSP.NAME.classic.class.usage");
  }

  @Override
  public boolean isEnabledByDefault() {
    return false;
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                        boolean isOnTheFly,
                                        @NotNull LocalInspectionToolSession session) {
    return new Visitor(holder, session);
  }

  private static class Visitor extends PyInspectionVisitor {
    public Visitor(@Nullable ProblemsHolder holder, @NotNull LocalInspectionToolSession session) {
      super(holder, session);
    }

    @Override
    public void visitPyClass(PyClass node) {
      final ASTNode nameNode = node.getNameNode();
      if (!node.isNewStyleClass(myTypeEvalContext) && nameNode != null) {
        PyExpression[] superClassExpressions = node.getSuperClassExpressions();
        if (superClassExpressions.length == 0) {
          registerProblem(nameNode.getPsi(), PyBundle.message("INSP.classic.class.usage.old.style.class"), new TransformClassicClassQuickFix());
        } else {
          registerProblem(nameNode.getPsi(), PyBundle.message("INSP.classic.class.usage.old.style.class.ancestors"));
        }
      }
    }
  }
}
