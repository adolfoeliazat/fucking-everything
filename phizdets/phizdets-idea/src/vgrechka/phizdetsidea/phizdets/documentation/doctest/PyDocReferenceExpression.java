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
package vgrechka.phizdetsidea.phizdets.documentation.doctest;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.util.PsiTreeUtil;
import vgrechka.phizdetsidea.phizdets.psi.PyFromImportStatement;
import vgrechka.phizdetsidea.phizdets.psi.PyImportElement;
import vgrechka.phizdetsidea.phizdets.psi.impl.PyReferenceExpressionImpl;
import vgrechka.phizdetsidea.phizdets.psi.impl.references.PyImportReference;
import vgrechka.phizdetsidea.phizdets.psi.impl.references.PyQualifiedReference;
import vgrechka.phizdetsidea.phizdets.psi.resolve.PyResolveContext;
import org.jetbrains.annotations.NotNull;

/**
 *
 * User : ktisha
 */
public class PyDocReferenceExpression extends PyReferenceExpressionImpl {

  public PyDocReferenceExpression(ASTNode astNode) {
    super(astNode);
  }

  @NotNull
  @Override
  public PsiPolyVariantReference getReference(@NotNull PyResolveContext context) {
    if (isQualified()) {
      return new PyQualifiedReference(this, context);
    }
    final PsiElement importParent = PsiTreeUtil.getParentOfType(this, PyImportElement.class, PyFromImportStatement.class);
    if (importParent != null) {
      return PyImportReference.forElement(this, importParent, context);
    }
    return new PyDocReference(this, context);
  }
}

