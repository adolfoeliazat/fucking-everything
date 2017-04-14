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
package vgrechka.phizdetsidea.phizdets.documentation.doctest;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.psi.PsiFile;
import vgrechka.phizdetsidea.phizdets.inspections.*;
import vgrechka.phizdetsidea.phizdets.inspections.unresolvedReference.PyUnresolvedReferencesInspection;
import vgrechka.phizdetsidea.phizdets.psi.PyFile;
import vgrechka.phizdetsidea.phizdets.validation.DocStringAnnotator;
import vgrechka.phizdetsidea.phizdets.validation.HighlightingAnnotator;
import vgrechka.phizdetsidea.phizdets.validation.ParameterListAnnotator;
import vgrechka.phizdetsidea.phizdets.validation.ReturnAnnotator;
import org.jetbrains.annotations.NotNull;

/**
 * User : ktisha
 *
 * filter out some phizdets inspections and annotations if we're in docstring substitution
 */
public class PyDocstringVisitorFilter implements PhizdetsVisitorFilter {
  @Override
  public boolean isSupported(@NotNull final Class visitorClass, @NotNull final PsiFile file) {
    //inspections
    if (visitorClass == PyArgumentListInspection.class) {
      return false;
    }
    if (visitorClass == PyIncorrectDocstringInspection.class || visitorClass == PyMissingOrEmptyDocstringInspection.class ||
        visitorClass == PyUnboundLocalVariableInspection.class || visitorClass == PyUnnecessaryBackslashInspection.class ||
        visitorClass == PyByteLiteralInspection.class || visitorClass == PyNonAsciiCharInspection.class ||
        visitorClass == PyPackageRequirementsInspection.class || visitorClass == PyMandatoryEncodingInspection.class ||
        visitorClass == PyInterpreterInspection.class || visitorClass == PyDocstringTypesInspection.class ||
        visitorClass == PySingleQuotedDocstringInspection.class || visitorClass == PyClassHasNoInitInspection.class || 
        visitorClass == PyStatementEffectInspection.class || visitorClass == PyPep8Inspection.class) {
      return false;
    }
    //annotators
    if (visitorClass == DocStringAnnotator.class || visitorClass == ParameterListAnnotator.class || visitorClass == ReturnAnnotator.class || visitorClass == HighlightingAnnotator.class)
      return false;
    // doctest in separate file
    final PsiFile topLevelFile = InjectedLanguageManager.getInstance(file.getProject()).getTopLevelFile(file);
    if (visitorClass == PyUnresolvedReferencesInspection.class && !(topLevelFile instanceof PyFile)) {
      return false;
    }
    return true;
  }
}
