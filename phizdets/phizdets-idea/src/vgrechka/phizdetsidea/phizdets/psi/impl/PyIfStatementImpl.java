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
import vgrechka.phizdetsidea.phizdets.PyElementTypes;
import vgrechka.phizdetsidea.phizdets.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
 */
public class PyIfStatementImpl extends PyPartitionedElementImpl implements PyIfStatement {
  public PyIfStatementImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  protected void acceptPyVisitor(PyElementVisitor pyVisitor) {
    pyVisitor.visitPyIfStatement(this);
  }

  @NotNull
  public PyIfPart getIfPart() {
    return (PyIfPart)getPartNotNull(PyElementTypes.IF_PART_IF);
  }

  @NotNull
  public PyIfPart[] getElifParts() {
    return childrenToPsi(PyElementTypes.ELIFS, PyIfPart.EMPTY_ARRAY);
  }

  public PyElsePart getElsePart() {
    return (PyElsePart)getPart(PyElementTypes.ELSE_PART);
  }
}
