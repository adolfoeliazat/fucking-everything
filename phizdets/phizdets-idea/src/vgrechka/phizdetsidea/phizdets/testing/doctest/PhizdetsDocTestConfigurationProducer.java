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

/*
 * User: catherine
 */
package vgrechka.phizdetsidea.phizdets.testing.doctest;

import com.intellij.execution.Location;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import vgrechka.phizdetsidea.phizdets.psi.PyClass;
import vgrechka.phizdetsidea.phizdets.psi.PyElement;
import vgrechka.phizdetsidea.phizdets.psi.PyFile;
import vgrechka.phizdetsidea.phizdets.psi.PyFunction;
import vgrechka.phizdetsidea.phizdets.psi.types.TypeEvalContext;
import vgrechka.phizdetsidea.phizdets.testing.AbstractPhizdetsLegacyTestRunConfiguration;
import vgrechka.phizdetsidea.phizdets.testing.PhizdetsTestLegacyConfigurationProducer;
import vgrechka.phizdetsidea.phizdets.testing.PhizdetsTestConfigurationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhizdetsDocTestConfigurationProducer extends PhizdetsTestLegacyConfigurationProducer {

  public PhizdetsDocTestConfigurationProducer() {
    super(PhizdetsTestConfigurationType.getInstance().PY_DOCTEST_FACTORY);
  }

  @Override
  protected boolean isTestFunction(@NotNull final PyFunction pyFunction, @Nullable final AbstractPhizdetsLegacyTestRunConfiguration configuration) {
    return PhizdetsDocTestUtil.isDocTestFunction(pyFunction);
  }

  @Override
  protected boolean isTestClass(@NotNull PyClass pyClass,
                                @Nullable final AbstractPhizdetsLegacyTestRunConfiguration configuration,
                                @Nullable final TypeEvalContext context) {
    return PhizdetsDocTestUtil.isDocTestClass(pyClass);
  }

  @Override
  protected boolean isTestFile(@NotNull PyFile file) {
    final List<PyElement> testCases = PhizdetsDocTestUtil.getDocTestCasesFromFile(file);
    return !testCases.isEmpty();
  }

  protected boolean isAvailable(@NotNull final Location location) {
    final Module module = location.getModule();
    if (!isPhizdetsModule(module)) return false;
    final PsiElement element = location.getPsiElement();
    if (element instanceof PsiFile) {
      final PyDocTestVisitor visitor = new PyDocTestVisitor();
      element.accept(visitor);
      return visitor.hasTests;
    }
    else return true;
  }

  private static class PyDocTestVisitor extends PsiRecursiveElementVisitor {
    boolean hasTests = false;

    @Override
    public void visitFile(PsiFile node) {
      if (node instanceof PyFile) {
        List<PyElement> testClasses = PhizdetsDocTestUtil.getDocTestCasesFromFile((PyFile)node);
        if (!testClasses.isEmpty()) hasTests = true;
      }
      else {
        final String text = node.getText();
        if (PhizdetsDocTestUtil.hasExample(text)) hasTests = true;
      }
    }
  }

  @Override
  protected boolean isTestFolder(@NotNull VirtualFile virtualFile, @NotNull Project project) {
    return false;
  }

}