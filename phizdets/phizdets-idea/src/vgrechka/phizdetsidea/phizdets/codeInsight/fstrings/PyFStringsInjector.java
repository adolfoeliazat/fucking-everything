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
package vgrechka.phizdetsidea.phizdets.codeInsight.fstrings;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.psi.PsiElement;
import vgrechka.phizdetsidea.phizdets.codeInsight.PyInjectorBase;
import vgrechka.phizdetsidea.phizdets.codeInsight.fstrings.FStringParser.Fragment;
import vgrechka.phizdetsidea.phizdets.documentation.doctest.PyDocstringLanguageDialect;
import vgrechka.phizdetsidea.phizdets.psi.PyStringLiteralExpression;
import vgrechka.phizdetsidea.phizdets.psi.PyUtil.StringNodeInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static vgrechka.phizdetsidea.phizdets.psi.PyUtil.as;

/**
 * @author Mikhail Golubev
 */
public class PyFStringsInjector extends PyInjectorBase {
  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    final PyStringLiteralExpression pyString = as(context, PyStringLiteralExpression.class);
    if (pyString == null) {
      return;
    }
    
    for (ASTNode node : pyString.getStringNodes()) {
      final int relNodeOffset = node.getTextRange().getStartOffset() - pyString.getTextRange().getStartOffset();
      for (Fragment offsets : getInjectionRanges(node)) {
        if (offsets.containsNamedUnicodeEscape()) continue;
        registrar.startInjecting(PyDocstringLanguageDialect.getInstance());
        registrar.addPlace(null, null, pyString, offsets.getContentRange().shiftRight(relNodeOffset));
        registrar.doneInjecting();
      }
    }
  }

  @NotNull
  private static List<Fragment> getInjectionRanges(@NotNull ASTNode node) {
    final StringNodeInfo nodeInfo = new StringNodeInfo(node);
    if (nodeInfo.isFormatted()) {
      return FStringParser.parse(node.getText()).getFragments();
    }
    return Collections.emptyList();
  }

  @Nullable
  @Override
  public Language getInjectedLanguage(@NotNull PsiElement context) {
    return context instanceof PyStringLiteralExpression? PyDocstringLanguageDialect.getInstance() : null;
  }
}
