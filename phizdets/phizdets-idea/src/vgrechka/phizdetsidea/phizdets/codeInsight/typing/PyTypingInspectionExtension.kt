/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package vgrechka.phizdetsidea.phizdets.codeInsight.typing

import vgrechka.phizdetsidea.phizdets.PyNames
import vgrechka.phizdetsidea.phizdets.inspections.PyInspectionExtension
import vgrechka.phizdetsidea.phizdets.psi.impl.PyBuiltinCache
import vgrechka.phizdetsidea.phizdets.psi.types.PyClassLikeType
import vgrechka.phizdetsidea.phizdets.psi.types.PyClassType
import vgrechka.phizdetsidea.phizdets.psi.types.PyType
import vgrechka.phizdetsidea.phizdets.psi.types.TypeEvalContext

class PyTypingInspectionExtension : PyInspectionExtension() {

  override fun ignoreUnresolvedMember(type: PyType, name: String, context: TypeEvalContext): Boolean {
    return name == PyNames.GETITEM &&
           type is PyClassLikeType &&
           type.isDefinition &&
           !isBuiltin(type) &&
           isGenericItselfOrDescendant(type, context)
  }

  private fun isGenericItselfOrDescendant(type: PyClassLikeType,
                                          context: TypeEvalContext): Boolean {
    return type.classQName == PyTypingTypeProvider.GENERIC ||
           type.getSuperClassTypes(context).any { it.classQName == PyTypingTypeProvider.GENERIC }
  }

  private fun isBuiltin(type: PyClassLikeType): Boolean {
    return if (type is PyClassType) PyBuiltinCache.getInstance(type.pyClass).isBuiltin(type.pyClass) else false
  }
}
