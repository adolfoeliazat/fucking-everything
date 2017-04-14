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
package vgrechka.phizdetsidea.phizdets.validation;

import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiFile;
import vgrechka.phizdetsidea.phizdets.inspections.PhizdetsVisitorFilter;
import org.jetbrains.annotations.NotNull;

/**
 * @author vlan
 */
public class PyMultiplePsiFilesVisitorFilter implements PhizdetsVisitorFilter {
  @Override
  public boolean isSupported(@NotNull Class visitorClass, @NotNull PsiFile file) {
    if (visitorClass == StringLiteralQuotesAnnotator.class &&
        file.getViewProvider() instanceof MultiplePsiFilesPerDocumentFileViewProvider) {
      return false;
    }
    return true;
  }
}