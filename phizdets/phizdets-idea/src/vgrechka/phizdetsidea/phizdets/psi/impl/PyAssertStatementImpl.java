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
package vgrechka.phizdetsidea.phizdets.psi.impl;

import com.intellij.lang.ASTNode;
import vgrechka.phizdetsidea.phizdets.PhizdetsDialectsTokenSetProvider;
import vgrechka.phizdetsidea.phizdets.psi.PyAssertStatement;
import vgrechka.phizdetsidea.phizdets.psi.PyElementVisitor;
import vgrechka.phizdetsidea.phizdets.psi.PyExpression;

/**
 * @author yole
 */
public class PyAssertStatementImpl extends PyElementImpl implements PyAssertStatement {
  public PyAssertStatementImpl(ASTNode astNode) {
    super(astNode);
  }

  protected void acceptPyVisitor(PyElementVisitor pyVisitor) {
    pyVisitor.visitPyAssertStatement(this);
  }

  @Override
  public PyExpression[] getArguments() {
    return childrenToPsi(PhizdetsDialectsTokenSetProvider.INSTANCE.getExpressionTokens(), PyExpression.EMPTY_ARRAY);
  }
}
