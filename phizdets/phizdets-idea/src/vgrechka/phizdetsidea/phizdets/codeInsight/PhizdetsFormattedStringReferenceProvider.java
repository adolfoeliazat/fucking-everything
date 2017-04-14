/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package vgrechka.phizdetsidea.phizdets.codeInsight;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import vgrechka.phizdetsidea.phizdets.inspections.PyStringFormatParser;
import vgrechka.phizdetsidea.phizdets.psi.PyStringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PhizdetsFormattedStringReferenceProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
    if (PhizdetsFormattedStringReferenceContributor.FORMAT_STRING_PATTERN.accepts(element)) {
      return getReferencesFromFormatString((PyStringLiteralExpression)element);
    }
    else {
      return getReferencesFromPercentString((PyStringLiteralExpression)element);
    }
  }

  private static PySubstitutionChunkReference[] getReferencesFromFormatString(@NotNull final PyStringLiteralExpression element) {
    final List<PyStringFormatParser.SubstitutionChunk> chunks = PyStringFormatParser.filterSubstitutions(
      PyStringFormatParser.parseNewStyleFormat(element.getText()));
    return getReferencesFromChunks(element, chunks, false);
  }

  private static PySubstitutionChunkReference[] getReferencesFromPercentString(@NotNull final PyStringLiteralExpression element) {
    final List<PyStringFormatParser.SubstitutionChunk>
      chunks = PyStringFormatParser.filterSubstitutions(PyStringFormatParser.parsePercentFormat(element.getText()));
    return getReferencesFromChunks(element, chunks, true);
  }

  @NotNull
  public static PySubstitutionChunkReference[] getReferencesFromChunks(@NotNull final PyStringLiteralExpression element,
                                                                       @NotNull final List<PyStringFormatParser.SubstitutionChunk> chunks,
                                                                       boolean isPercent) {
    final PySubstitutionChunkReference[] result = new PySubstitutionChunkReference[chunks.size()];
      for (int i = 0; i < chunks.size(); i++) {
        final PyStringFormatParser.SubstitutionChunk chunk = chunks.get(i);
        result[i] = new PySubstitutionChunkReference(element, chunk, i, isPercent);
      }
    return result;
  }
}