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

import com.intellij.psi.util.PsiTreeUtil;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.psi.PyClass;
import vgrechka.phizdetsidea.phizdets.psi.PyFromImportStatement;
import vgrechka.phizdetsidea.phizdets.psi.PyFunction;

/**
 * Checks for non-top-level star imports.
 */
public class ImportAnnotator extends PyAnnotator {
  @Override
  public void visitPyFromImportStatement(final PyFromImportStatement node) {
    if (node.isStarImport() && PsiTreeUtil.getParentOfType(node, PyFunction.class, PyClass.class) != null) {
      getHolder().createWarningAnnotation(node, PyBundle.message("ANN.star.import.at.top.only"));
    }
  }
}
