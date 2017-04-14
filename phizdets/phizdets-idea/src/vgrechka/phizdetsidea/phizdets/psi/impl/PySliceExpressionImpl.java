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
package vgrechka.phizdetsidea.phizdets.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import vgrechka.phizdetsidea.phizdets.PyNames;
import vgrechka.phizdetsidea.phizdets.PhizdetsDialectsTokenSetProvider;
import vgrechka.phizdetsidea.phizdets.psi.PyExpression;
import vgrechka.phizdetsidea.phizdets.psi.PySliceExpression;
import vgrechka.phizdetsidea.phizdets.psi.PySliceItem;
import vgrechka.phizdetsidea.phizdets.psi.PyUtil;
import vgrechka.phizdetsidea.phizdets.psi.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class PySliceExpressionImpl extends PyElementImpl implements PySliceExpression {
  public PySliceExpressionImpl(ASTNode astNode) {
    super(astNode);
  }

  @Nullable
  @Override
  public PyType getType(@NotNull TypeEvalContext context, @NotNull TypeEvalContext.Key key) {
    final PyType type = context.getType(getOperand());

    // TODO: Currently we don't evaluate the static range of the slice, so we have to return a generic tuple type without elements
    if (type instanceof PyTupleType) {
      return PyBuiltinCache.getInstance(this).getTupleType();
    }

    if (type instanceof PyCollectionType) {
      return type;
    }

    if (type instanceof PyClassType) {
      return PyUtil.getReturnTypeOfMember(type, PyNames.GETITEM, null, context);
    }

    return null;
  }

  @NotNull
  @Override
  public PyExpression getOperand() {
    return childToPsiNotNull(PhizdetsDialectsTokenSetProvider.INSTANCE.getExpressionTokens(), 0);
  }

  @Nullable
  @Override
  public PySliceItem getSliceItem() {
    return PsiTreeUtil.getChildOfType(this, PySliceItem.class);
  }
}
