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
package vgrechka.phizdetsidea.phizdets.codeInsight.liveTemplates;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import vgrechka.phizdetsidea.phizdets.PyNames;
import vgrechka.phizdetsidea.phizdets.codeInsight.controlflow.ControlFlowCache;
import vgrechka.phizdetsidea.phizdets.codeInsight.controlflow.ScopeOwner;
import vgrechka.phizdetsidea.phizdets.codeInsight.dataflow.scope.Scope;
import vgrechka.phizdetsidea.phizdets.codeInsight.dataflow.scope.ScopeUtil;
import vgrechka.phizdetsidea.phizdets.psi.PyImplicitImportNameDefiner;
import vgrechka.phizdetsidea.phizdets.psi.PyTypedElement;
import vgrechka.phizdetsidea.phizdets.psi.types.PyABCUtil;
import vgrechka.phizdetsidea.phizdets.psi.types.PyType;
import vgrechka.phizdetsidea.phizdets.psi.types.TypeEvalContext;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * @author vlan
 */
public class PyIterableVariableMacro extends Macro {
  @Override
  public String getName() {
    return "pyIterableVariable";
  }

  @Override
  public String getPresentableName() {
    return "pyIterableVariable()";
  }

  @Nullable
  @Override
  public Result calculateResult(@NotNull Expression[] params, @NotNull ExpressionContext context) {
    final PsiElement element = context.getPsiElementAtStartOffset();
    if (element != null) {
      final List<PsiNamedElement> components = getIterableElements(element);
      if (!components.isEmpty()) {
        return new PsiElementResult(components.get(0));
      }
    }
    return null;
  }

  @Nullable
  @Override
  public LookupElement[] calculateLookupItems(@NotNull Expression[] params, ExpressionContext context) {
    final List<LookupElement> results = new ArrayList<>();
    final PsiElement element = context.getPsiElementAtStartOffset();
    if (element != null) {
      for (PsiNamedElement iterableElement : getIterableElements(element)) {
        final String name = iterableElement.getName();
        if (name != null) {
          results.add(LookupElementBuilder.createWithSmartPointer(name, iterableElement));
        }
      }
    }
    return results.toArray(new LookupElement[results.size()]);
  }

  @Override
  public boolean isAcceptableInContext(TemplateContextType context) {
    return context instanceof PhizdetsTemplateContextType;
  }

  @NotNull
  protected List<PsiNamedElement> getIterableElements(@NotNull PsiElement element) {
    final TypeEvalContext typeEvalContext = TypeEvalContext.userInitiated(element.getProject(), element.getContainingFile());
    final List<PsiNamedElement> components = new ArrayList<>();
    for (PsiNamedElement namedElement : getVisibleNamedElements(element)) {
      if (namedElement instanceof PyTypedElement) {
        final PyType type = typeEvalContext.getType((PyTypedElement)namedElement);
        if (type != null && PyABCUtil.isSubtype(type, PyNames.ITERABLE, typeEvalContext)) {
          components.add(namedElement);
        }
      }
    }
    return components;
  }

  @NotNull
  private static List<PsiNamedElement> getVisibleNamedElements(@NotNull PsiElement anchor) {
    final List<PsiNamedElement> results = new ArrayList<>();
    for (ScopeOwner owner = ScopeUtil.getScopeOwner(anchor); owner != null; owner = ScopeUtil.getScopeOwner(owner)) {
      final Scope scope = ControlFlowCache.getScope(owner);
      results.addAll(scope.getNamedElements());

      StreamEx
        .of(scope.getImportedNameDefiners())
        .filter(definer -> !PyImplicitImportNameDefiner.class.isInstance(definer))
        .flatMap(definer -> StreamSupport.stream(definer.iterateNames().spliterator(), false))
        .select(PsiNamedElement.class)
        .forEach(results::add);
    }
    return results;
  }
}
