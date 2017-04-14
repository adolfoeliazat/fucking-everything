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
package vgrechka.phizdetsidea.phizdets.refactoring.introduce.constant;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.HashSet;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.codeInsight.controlflow.ScopeOwner;
import vgrechka.phizdetsidea.phizdets.codeInsight.imports.AddImportHelper;
import vgrechka.phizdetsidea.phizdets.psi.PyExpression;
import vgrechka.phizdetsidea.phizdets.psi.PyFile;
import vgrechka.phizdetsidea.phizdets.psi.PyParameterList;
import vgrechka.phizdetsidea.phizdets.refactoring.PyReplaceExpressionUtil;
import vgrechka.phizdetsidea.phizdets.refactoring.introduce.IntroduceHandler;
import vgrechka.phizdetsidea.phizdets.refactoring.introduce.IntroduceOperation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Alexey.Ivanov
 */
public class PyIntroduceConstantHandler extends IntroduceHandler {
  public PyIntroduceConstantHandler() {
    super(new ConstantValidator(), PyBundle.message("refactoring.introduce.constant.dialog.title"));
  }

  @Override
  protected PsiElement replaceExpression(PsiElement expression, PyExpression newExpression, IntroduceOperation operation) {
    if (PsiTreeUtil.getParentOfType(expression, ScopeOwner.class) instanceof PyFile) {
      return super.replaceExpression(expression, newExpression, operation);
    }
    return PyReplaceExpressionUtil.replaceExpression(expression, newExpression);
  }

  @Override
  protected PsiElement addDeclaration(@NotNull final PsiElement expression,
                                      @NotNull final PsiElement declaration,
                                      @NotNull final IntroduceOperation operation) {
    final PsiElement anchor = expression.getContainingFile();
    assert anchor instanceof PyFile;
    return anchor.addBefore(declaration, AddImportHelper.getFileInsertPosition((PyFile)anchor));
  }

  @Override
  protected Collection<String> generateSuggestedNames(@NotNull final PyExpression expression) {
    Collection<String> names = new HashSet<>();
    for (String name : super.generateSuggestedNames(expression)) {
      names.add(StringUtil.toUpperCase(name));
    }
    return names;
  }

  @Override
  protected boolean isValidIntroduceContext(PsiElement element) {
    return super.isValidIntroduceContext(element) || PsiTreeUtil.getParentOfType(element, PyParameterList.class) != null;
  }

  @Override
  protected String getHelpId() {
    return "phizdets.reference.introduceConstant";
  }

  @Override
  protected String getRefactoringId() {
    return "refactoring.phizdets.introduce.constant";
  }
}
