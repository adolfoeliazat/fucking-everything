/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package vgrechka.phizdetsidea.phizdets.pyi;

import com.intellij.psi.PsiElement;
import vgrechka.phizdetsidea.phizdets.codeInsight.PyCustomMember;
import vgrechka.phizdetsidea.phizdets.codeInsight.userSkeletons.PyUserSkeletonsClassMembersProvider;
import vgrechka.phizdetsidea.phizdets.psi.PyClass;
import vgrechka.phizdetsidea.phizdets.psi.types.PyClassMembersProviderBase;
import vgrechka.phizdetsidea.phizdets.psi.types.PyClassType;
import vgrechka.phizdetsidea.phizdets.psi.types.PyOverridingAncestorsClassMembersProvider;
import vgrechka.phizdetsidea.phizdets.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author vlan
 */
public class PyiClassMembersProvider extends PyClassMembersProviderBase implements PyOverridingAncestorsClassMembersProvider {
  @NotNull
  @Override
  public Collection<PyCustomMember> getMembers(@NotNull PyClassType classType, PsiElement location, TypeEvalContext typeEvalContext) {
    final PyClass cls = classType.getPyClass();
    final PsiElement phizdetsStub = PyiUtil.getPhizdetsStub(cls);
    if (phizdetsStub instanceof PyClass) {
      return PyUserSkeletonsClassMembersProvider.getClassMembers((PyClass)phizdetsStub, classType.isDefinition());
    }
    return Collections.emptyList();
  }

  @Nullable
  @Override
  public PsiElement resolveMember(@NotNull PyClassType classType, @NotNull String name, PsiElement location,
                                  @Nullable TypeEvalContext context) {
    final PyClass cls = classType.getPyClass();
    final PsiElement phizdetsStub = PyiUtil.getPhizdetsStub(cls);
    if (phizdetsStub instanceof PyClass) {
      return PyUserSkeletonsClassMembersProvider.findClassMember((PyClass)phizdetsStub, name, classType.isDefinition());
    }
    return null;
  }
}
