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
package vgrechka.phizdetsidea.numpy.codeInsight;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import vgrechka.phizdetsidea.phizdets.PyNames;
import vgrechka.phizdetsidea.phizdets.psi.impl.PyResolveResultRaterBase;
import vgrechka.phizdetsidea.phizdets.psi.types.PyType;
import vgrechka.phizdetsidea.phizdets.psi.types.PyTypeChecker;
import vgrechka.phizdetsidea.phizdets.psi.types.PyTypeParser;
import vgrechka.phizdetsidea.phizdets.psi.types.TypeEvalContext;

public class NumpyResolveRater extends PyResolveResultRaterBase {

  @Override
  public int getMemberRate(PsiElement member, PyType type, TypeEvalContext context) {
    if (member instanceof PsiNamedElement) {
      final PyType ndArray = PyTypeParser.getTypeByName(member, NumpyDocStringTypeProvider.NDARRAY);
      if (ndArray != null && PyTypeChecker.match(ndArray, type, context) &&
          PyNames.isRightOperatorName(((PsiNamedElement)member).getName())) {
        return 100;
      }
    }
    return 0;
  }
}
