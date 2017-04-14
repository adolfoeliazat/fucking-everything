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
package vgrechka.phizdetsidea.phizdets.refactoring.surround.surrounders.expressions;

import vgrechka.phizdetsidea.phizdets.psi.PyExpression;
import vgrechka.phizdetsidea.phizdets.psi.PyIfStatement;
import vgrechka.phizdetsidea.phizdets.psi.PyStatement;
import vgrechka.phizdetsidea.phizdets.psi.PyStatementListContainer;

public class PyIfExpressionSurrounder extends PyExpressionAsConditionSurrounder {

  @Override
  protected String getTextToGenerate() {
    return "if a:\n pass";
  }

  @Override
  protected PyExpression getCondition(PyStatement statement) {
    if (statement instanceof PyIfStatement) {
      return ((PyIfStatement)statement).getIfPart().getCondition();
    }
    return null;
  }

  @Override
  protected PyStatementListContainer getStatementListContainer(PyStatement statement) {
    if (statement instanceof PyIfStatement) {
      return ((PyIfStatement)statement).getIfPart();
    }
    return null;
  }

  @Override
  public String getTemplateDescription() {
    return "if expr";
  }
}