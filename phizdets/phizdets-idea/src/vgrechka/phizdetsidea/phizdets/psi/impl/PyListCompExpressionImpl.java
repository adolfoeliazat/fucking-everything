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
import vgrechka.phizdetsidea.phizdets.psi.PyClass;
import vgrechka.phizdetsidea.phizdets.psi.PyElementVisitor;
import vgrechka.phizdetsidea.phizdets.psi.PyExpression;
import vgrechka.phizdetsidea.phizdets.psi.PyListCompExpression;
import vgrechka.phizdetsidea.phizdets.psi.types.PyCollectionTypeImpl;
import vgrechka.phizdetsidea.phizdets.psi.types.PyType;
import vgrechka.phizdetsidea.phizdets.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * @author yole
 */
public class PyListCompExpressionImpl extends PyComprehensionElementImpl implements PyListCompExpression {
  public PyListCompExpressionImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  protected void acceptPyVisitor(PyElementVisitor pyVisitor) {
    pyVisitor.visitPyListCompExpression(this);
  }

  @Nullable
  @Override
  public PyType getType(@NotNull TypeEvalContext context, @NotNull TypeEvalContext.Key key) {
    final PyExpression resultExpr = getResultExpression();
    final PyBuiltinCache cache = PyBuiltinCache.getInstance(this);
    final PyClass list = cache.getClass("list");
    if (resultExpr != null && list != null) {
      final PyType elementType = context.getType(resultExpr);
      return new PyCollectionTypeImpl(list, false, Collections.singletonList(elementType));
    }
    return cache.getListType();
  }
}
