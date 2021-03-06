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
import vgrechka.phizdetsidea.phizdets.psi.PyStatementList;
import vgrechka.phizdetsidea.phizdets.psi.PyStatementPart;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract statement part implementation; extracts the statements list.
 * User: dcheryasov
 * Date: Mar 16, 2009 4:36:50 AM
 */
public abstract class PyStatementPartImpl extends PyElementImpl implements PyStatementPart {
  protected PyStatementPartImpl(ASTNode astNode) {
    super(astNode);
  }

  @NotNull
  public PyStatementList getStatementList() {
    return childToPsiNotNull(PyElementTypes.STATEMENT_LIST);
  }
}
