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
package vgrechka.phizdetsidea.phizdets.psi.impl.references;

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import vgrechka.phizdetsidea.phizdets.psi.PyFromImportStatement;
import vgrechka.phizdetsidea.phizdets.psi.impl.PyReferenceExpressionImpl;
import vgrechka.phizdetsidea.phizdets.psi.resolve.PyResolveContext;
import vgrechka.phizdetsidea.phizdets.psi.resolve.RatedResolveResult;
import vgrechka.phizdetsidea.phizdets.psi.resolve.ResolveImportUtil;
import vgrechka.phizdetsidea.phizdets.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Reference to the import source in a 'from ... import' statement:<br/>
 * <code>from <u>foo</u> import name</code>

 * @author yole
 */
public class PyFromImportSourceReference extends PyImportReference {
  private PyFromImportStatement myStatement;
  
  public PyFromImportSourceReference(PyReferenceExpressionImpl element, PyResolveContext context) {
    super(element, context);
    myStatement = PsiTreeUtil.getParentOfType(element, PyFromImportStatement.class);
    assert myStatement != null;
  }

  @NotNull
  @Override
  protected List<RatedResolveResult> resolveInner() {
    List<PsiElement> targets = ResolveImportUtil.resolveFromImportStatementSource(myStatement, myElement.asQualifiedName());
    return ResolveImportUtil.rateResults(targets);
  }

  @Override
  public HighlightSeverity getUnresolvedHighlightSeverity(TypeEvalContext context) {
    return myElement.isQualified() ? HighlightSeverity.WARNING : HighlightSeverity.ERROR;
  }
}
