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

import com.intellij.lang.ASTNode;
import vgrechka.phizdetsidea.phizdets.PyTokenTypes;
import vgrechka.phizdetsidea.phizdets.psi.PyArgumentList;
import vgrechka.phizdetsidea.phizdets.psi.PyExpression;
import vgrechka.phizdetsidea.phizdets.psi.PyGeneratorExpression;

/**
 * @author yole
 */
public class GeneratorInArgumentListAnnotator extends PyAnnotator {
  @Override
  public void visitPyArgumentList(PyArgumentList node) {
    if (node.getArguments().length > 1) {
      for (PyExpression expression : node.getArguments()) {
        if (expression instanceof PyGeneratorExpression) {
          ASTNode firstChildNode = expression.getNode().getFirstChildNode();
          if (firstChildNode.getElementType() != PyTokenTypes.LPAR) {
            markError(expression, "Generator expression must be parenthesized if not sole argument");
          }
        }
      }
    }
  }
}
