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
import vgrechka.phizdetsidea.phizdets.psi.PyElementVisitor;
import org.jetbrains.annotations.NotNull;
import vgrechka.phizdetsidea.phizdets.psi.PyExpression;
import vgrechka.phizdetsidea.phizdets.psi.PyExpressionStatement;

/**
 * @author yole
 */
public class PyExpressionStatementImpl extends PyElementImpl implements PyExpressionStatement {
  public PyExpressionStatementImpl(ASTNode astNode) {
    super(astNode);
  }

  @NotNull
  public PyExpression getExpression() {
    return childToPsiNotNull(PhizdetsDialectsTokenSetProvider.INSTANCE.getExpressionTokens(), 0);
  }

  @Override
  protected void acceptPyVisitor(PyElementVisitor pyVisitor) {
    pyVisitor.visitPyExpressionStatement(this);
  }
}
