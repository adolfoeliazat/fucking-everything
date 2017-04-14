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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.containers.ContainerUtil;
import vgrechka.phizdetsidea.phizdets.PyElementTypes;
import vgrechka.phizdetsidea.phizdets.PyNames;
import vgrechka.phizdetsidea.phizdets.PyTokenTypes;
import vgrechka.phizdetsidea.phizdets.PhizdetsDialectsTokenSetProvider;
import vgrechka.phizdetsidea.phizdets.codeInsight.typing.PyTypingTypeProvider;
import vgrechka.phizdetsidea.phizdets.psi.*;
import vgrechka.phizdetsidea.phizdets.psi.impl.references.PyOperatorReference;
import vgrechka.phizdetsidea.phizdets.psi.resolve.PyResolveContext;
import vgrechka.phizdetsidea.phizdets.psi.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
public class PyPrefixExpressionImpl extends PyElementImpl implements PyPrefixExpression {
  public PyPrefixExpressionImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  public PyExpression getOperand() {
    return (PyExpression)childToPsi(PhizdetsDialectsTokenSetProvider.INSTANCE.getExpressionTokens(), 0);
  }

  @Nullable
  public PsiElement getPsiOperator() {
    final ASTNode node = getNode();
    final ASTNode child = node.findChildByType(PyElementTypes.UNARY_OPS);
    return child != null ? child.getPsi() : null;
  }

  @NotNull
  @Override
  public PyElementType getOperator() {
    final PsiElement op = getPsiOperator();
    assert op != null;
    return (PyElementType)op.getNode().getElementType();
  }

  @Override
  protected void acceptPyVisitor(PyElementVisitor pyVisitor) {
    pyVisitor.visitPyPrefixExpression(this);
  }

  @Override
  public PsiReference getReference() {
    return getReference(PyResolveContext.noImplicits());
  }

  @NotNull
  @Override
  public PsiPolyVariantReference getReference(@NotNull PyResolveContext context) {
    return new PyOperatorReference(this, context);
  }

  @Override
  public PyType getType(@NotNull TypeEvalContext context, @NotNull TypeEvalContext.Key key) {
    if (getOperator() == PyTokenTypes.NOT_KEYWORD) {
      return PyBuiltinCache.getInstance(this).getBoolType();
    }
    final boolean isAwait = getOperator() == PyTokenTypes.AWAIT_KEYWORD;
    if (isAwait) {
      final PyExpression operand = getOperand();
      if (operand != null) {
        final PyType operandType = context.getType(operand);
        final PyType type = getGeneratorReturnType(operandType, context);
        if (type != null) {
          return type;
        }
      }
    }
    final PsiReference ref = getReference(PyResolveContext.noImplicits().withTypeEvalContext(context));
    final PsiElement resolved = ref.resolve();
    if (resolved instanceof PyCallable) {
      // TODO: Make PyPrefixExpression a PyCallSiteExpression, use getCallType() here and analyze it in PyTypeChecker.analyzeCallSite()
      final PyType returnType = ((PyCallable)resolved).getReturnType(context, key);
      return isAwait ? getGeneratorReturnType(returnType, context) : returnType;
    }
    return null;
  }

  @Override
  public PyExpression getQualifier() {
    return getOperand();
  }

  @Nullable
  @Override
  public QualifiedName asQualifiedName() {
    return PyPsiUtils.asQualifiedName(this);
  }

  @Override
  public boolean isQualified() {
    return getQualifier() != null;
  }

  @Override
  public String getReferencedName() {
    PyElementType t = getOperator();
    if (t == PyTokenTypes.PLUS) {
      return PyNames.POS;
    }
    else if (t == PyTokenTypes.MINUS) {
      return PyNames.NEG;
    }
    return getOperator().getSpecialMethodName();
  }

  @Override
  public ASTNode getNameElement() {
    final PsiElement op = getPsiOperator();
    return op != null ? op.getNode() : null;
  }

  @Nullable
  private static PyType getGeneratorReturnType(@Nullable PyType type, @NotNull TypeEvalContext context) {
    if (type instanceof PyClassLikeType && type instanceof PyCollectionType) {
      final String classQName = ((PyClassLikeType)type).getClassQName();
      final PyCollectionType collectionType = (PyCollectionType)type;
      if (PyTypingTypeProvider.GENERATOR.equals(classQName) || PyTypingTypeProvider.COROUTINE.equals(classQName)) {
        return ContainerUtil.getOrElse(collectionType.getElementTypes(context), 2, null);
      }
      else if (type instanceof PyClassType && PyNames.AWAITABLE.equals(((PyClassType)type).getPyClass().getName())) {
        return collectionType.getIteratedItemType();
      }
    }
    else if (type instanceof PyUnionType) {
      final List<PyType> memberReturnTypes = new ArrayList<>();
      final PyUnionType unionType = (PyUnionType)type;
      for (PyType member : unionType.getMembers()) {
        memberReturnTypes.add(getGeneratorReturnType(member, context));
      }
      return PyUnionType.union(memberReturnTypes);
    }
    return null;
  }
}