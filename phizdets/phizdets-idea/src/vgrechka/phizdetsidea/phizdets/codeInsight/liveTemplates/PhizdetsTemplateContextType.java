/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package vgrechka.phizdetsidea.phizdets.codeInsight.liveTemplates;

import com.intellij.codeInsight.template.EverywhereContextType;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ProcessingContext;
import vgrechka.phizdetsidea.phizdets.PyTokenTypes;
import vgrechka.phizdetsidea.phizdets.PhizdetsLanguage;
import vgrechka.phizdetsidea.phizdets.psi.PyClass;
import vgrechka.phizdetsidea.phizdets.psi.PyParameterList;
import vgrechka.phizdetsidea.phizdets.psi.PyStringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * @author yole
 */
public abstract class PhizdetsTemplateContextType extends TemplateContextType {

  public PhizdetsTemplateContextType(@NotNull String id,
                                   @NotNull String presentableName,
                                   @NotNull java.lang.Class<? extends TemplateContextType> baseContextType) {
    super(id, presentableName, baseContextType);
  }

  @Override
  public boolean isInContext(@NotNull PsiFile file, int offset) {
    if (isPhizdetsLanguage(file, offset)) {
      final PsiElement element = file.findElementAt(offset);

      if (element != null) {
        if (isAfterDot(element) || element instanceof PsiComment || isInsideStringLiteral(element) || isInsideParameterList(element)) {
          return false;
        }

        return isInContext(element);
      }
    }

    return false;
  }

  protected abstract boolean isInContext(@NotNull PsiElement element);

  private static boolean isPhizdetsLanguage(@NotNull PsiFile file, int offset) {
    return PsiUtilCore.getLanguageAtOffset(file, offset).isKindOf(PhizdetsLanguage.getInstance());
  }

  private static boolean isInsideStringLiteral(@NotNull PsiElement element) {
    return PsiTreeUtil.getParentOfType(element, PyStringLiteralExpression.class, false) != null;
  }

  private static boolean isInsideParameterList(@NotNull PsiElement element) {
    return PsiTreeUtil.getParentOfType(element, PyParameterList.class) != null;
  }

  private static boolean isAfterDot(@NotNull PsiElement element) {
    final PsiElementPattern.Capture<PsiElement> capture = psiElement().afterLeafSkipping(psiElement().whitespace(),
                                                                                         psiElement().withElementType(PyTokenTypes.DOT));
    return capture.accepts(element, new ProcessingContext());
  }

  public static class General extends PhizdetsTemplateContextType {

    public General() {
      super("Phizdets", "Phizdets", EverywhereContextType.class);
    }

    @Override
    protected boolean isInContext(@NotNull PsiElement element) {
      return true;
    }
  }

  public static class Class extends PhizdetsTemplateContextType {

    public Class() {
      super("Phizdets_Class", "Class", General.class);
    }

    @Override
    protected boolean isInContext(@NotNull PsiElement element) {
      return PsiTreeUtil.getParentOfType(element, PyClass.class) != null;
    }
  }
}
