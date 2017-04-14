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
package vgrechka.phizdetsidea.phizdets.codeInsight.editorActions.smartEnter.fixers;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import vgrechka.phizdetsidea.phizdets.PyTokenTypes;
import vgrechka.phizdetsidea.phizdets.codeInsight.editorActions.smartEnter.PySmartEnterProcessor;
import vgrechka.phizdetsidea.phizdets.psi.PyArgumentList;
import vgrechka.phizdetsidea.phizdets.psi.PyClass;
import vgrechka.phizdetsidea.phizdets.psi.PyDecorator;
import vgrechka.phizdetsidea.phizdets.psi.impl.PyPsiUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey.Ivanov
 */
public class PyArgumentListFixer extends PyFixer<PyArgumentList> {
  public PyArgumentListFixer() {
    super(PyArgumentList.class);
  }

  @Override
  public void doApply(@NotNull Editor editor, @NotNull PySmartEnterProcessor processor, @NotNull PyArgumentList arguments) throws IncorrectOperationException {
    final PsiElement rBrace = PyPsiUtils.getChildByFilter(arguments, PyTokenTypes.CLOSE_BRACES, 0);
    if (arguments.getParent() instanceof PyClass || arguments.getParent() instanceof PyDecorator) {
      final PsiElement lBrace = PyPsiUtils.getChildByFilter(arguments, PyTokenTypes.OPEN_BRACES, 0);
      if (lBrace != null && rBrace == null) {
        final Document document = editor.getDocument();
        document.insertString(arguments.getTextRange().getEndOffset(), ")");
      }
    }
    else {
      if (rBrace == null) {
        final Document document = editor.getDocument();
        document.insertString(arguments.getTextRange().getEndOffset(), ")");
      }
    }
  }
}
