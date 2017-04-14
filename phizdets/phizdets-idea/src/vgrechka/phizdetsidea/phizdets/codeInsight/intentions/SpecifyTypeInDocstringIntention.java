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
package vgrechka.phizdetsidea.phizdets.codeInsight.intentions;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ObjectUtils;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.PyNames;
import vgrechka.phizdetsidea.phizdets.debugger.PySignature;
import vgrechka.phizdetsidea.phizdets.debugger.PySignatureCacheManager;
import vgrechka.phizdetsidea.phizdets.documentation.docstrings.DocStringUtil;
import vgrechka.phizdetsidea.phizdets.documentation.docstrings.PyDocstringGenerator;
import vgrechka.phizdetsidea.phizdets.psi.*;
import vgrechka.phizdetsidea.phizdets.toolbox.Substring;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: ktisha
 * <p/>
 * Helps to specify type
 */
public class SpecifyTypeInDocstringIntention extends TypeIntention {
  private String myText = PyBundle.message("INTN.specify.type");

  @NotNull
  public String getText() {
    return myText;
  }

  @NotNull
  public String getFamilyName() {
    return PyBundle.message("INTN.specify.type");
  }

  @Override
  public void doInvoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    final PsiElement elementAt = PyUtil.findNonWhitespaceAtOffset(file, editor.getCaretModel().getOffset());
    final PyExpression problemElement = getProblemElement(elementAt);
    final PsiReference reference = problemElement == null ? null : problemElement.getReference();

    final PsiElement resolved = reference != null ? reference.resolve() : null;
    final PyNamedParameter parameter = getParameter(problemElement, resolved);

    if (parameter != null) {
      final PyFunction parentFunction = PsiTreeUtil.getParentOfType(parameter, PyFunction.class);
      if (parentFunction != null) {
        generateDocstring(parameter, parentFunction);
      }
    }
    else {
      StreamEx
        .of(getMultiCallable(elementAt))
        .select(PyFunction.class)
        .forEach(function -> generateDocstring(null, function));
    }
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

  @Nullable
  @Override
  public PsiElement getElementToMakeWritable(@NotNull PsiFile currentFile) {
    return currentFile;
  }

  private static void generateDocstring(@Nullable PyNamedParameter param, @NotNull PyFunction pyFunction) {
    if (!DocStringUtil.ensureNotPlainDocstringFormat(pyFunction)) {
      return;
    }

    final PyDocstringGenerator docstringGenerator = PyDocstringGenerator.forDocStringOwner(pyFunction);
    String type = PyNames.OBJECT;
    if (param != null) {
      final String paramName = StringUtil.notNullize(param.getName());
      final PySignature signature = PySignatureCacheManager.getInstance(pyFunction.getProject()).findSignature(pyFunction);
      if (signature != null) {
        type = ObjectUtils.chooseNotNull(signature.getArgTypeQualifiedName(paramName), type);
      }
      docstringGenerator.withParamTypedByName(param, type);
    }
    else {
      final PySignature signature = PySignatureCacheManager.getInstance(pyFunction.getProject()).findSignature(pyFunction);
      if (signature != null) {
        type = ObjectUtils.chooseNotNull(signature.getReturnTypeQualifiedName(), type);
      }
      docstringGenerator.withReturnValue(type);
    }

    WriteAction.run(() -> {
      docstringGenerator.addFirstEmptyLine().buildAndInsert();
      docstringGenerator.startTemplate();
    });
  }

  @Override
  protected void updateText(boolean isReturn) {
    myText = PyBundle.message(isReturn ? "INTN.specify.return.type" : "INTN.specify.type");
  }

  @Override
  protected boolean isParamTypeDefined(@NotNull PyParameter parameter) {
    final PyFunction pyFunction = PsiTreeUtil.getParentOfType(parameter, PyFunction.class);
    if (pyFunction != null) {
      final StructuredDocString structuredDocString = pyFunction.getStructuredDocString();
      if (structuredDocString == null) {
        return false;
      }
      final Substring typeSub = structuredDocString.getParamTypeSubstring(StringUtil.notNullize(parameter.getName()));
      return typeSub != null && !typeSub.isEmpty();
    }
    return false;
  }

  @Override
  protected boolean isReturnTypeDefined(@NotNull PyFunction function) {
    final StructuredDocString structuredDocString = function.getStructuredDocString();
    return structuredDocString != null && structuredDocString.getReturnType() != null;
  }
}