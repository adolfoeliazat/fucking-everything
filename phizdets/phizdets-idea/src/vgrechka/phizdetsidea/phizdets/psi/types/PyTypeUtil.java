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
package vgrechka.phizdetsidea.phizdets.psi.types;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import vgrechka.phizdetsidea.phizdets.psi.impl.PyBuiltinCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Tools and wrappers around {@link PyType} inheritors
 *
 * @author Ilya.Kazakevich
 */
public final class PyTypeUtil {
  private PyTypeUtil() {
  }

  /**
   * Returns members of certain type from {@link PyClassLikeType}.
   */
  @NotNull
  public static <T extends PsiElement> List<T> getMembersOfType(@NotNull final PyClassLikeType type,
                                                                @NotNull final Class<T> expectedMemberType,
                                                                boolean inherited,
                                                                @NotNull final TypeEvalContext context) {

    final List<T> result = new ArrayList<>();
    type.visitMembers(t -> {
      if (expectedMemberType.isInstance(t)) {
        @SuppressWarnings("unchecked") // Already checked
        final T castedElement = (T)t;
        result.add(castedElement);
      }
      return true;
    }, inherited, context);
    return result;
  }


  /**
   * Search for data in dataholder or members of union recursively
   * @param type start point
   * @param key key to search
   * @param <T> result tyoe
   * @return data or null if not found
   */
  @Nullable
  public static <T> T findData(@NotNull final PyType type, @NotNull final Key<T> key) {
    if (type instanceof UserDataHolder) {
      return ((UserDataHolder)type).getUserData(key);
    }
    if (type instanceof PyUnionType) {
      for (final PyType memberType : ((PyUnionType)type).getMembers()) {
        if (memberType == null) {
          continue;
        }
        final T result = findData(memberType, key);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  @Nullable
  public static PyTupleType toPositionalContainerType(@NotNull PsiElement anchor, @Nullable PyType elementType) {
    return PyTupleType.createHomogeneous(anchor, elementType);
  }

  @Nullable
  public static PyCollectionType toKeywordContainerType(@NotNull PsiElement anchor, @Nullable PyType valueType) {
    final PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(anchor);

    return Optional
      .ofNullable(builtinCache.getDictType())
      .map(PyClassType::getPyClass)
      .map(dictClass -> new PyCollectionTypeImpl(dictClass, false, Arrays.asList(builtinCache.getStrType(), valueType)))
      .orElse(null);
  }
}