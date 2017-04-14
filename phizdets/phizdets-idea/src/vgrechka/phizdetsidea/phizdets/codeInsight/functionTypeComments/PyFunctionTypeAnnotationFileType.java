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
package vgrechka.phizdetsidea.phizdets.codeInsight.functionTypeComments;

import vgrechka.phizdetsidea.phizdets.PhizdetsFileType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mikhail Golubev
 */
public class PyFunctionTypeAnnotationFileType extends PhizdetsFileType {
  public static final PyFunctionTypeAnnotationFileType INSTANCE = new PyFunctionTypeAnnotationFileType();

  public PyFunctionTypeAnnotationFileType() {
    super(PyFunctionTypeAnnotationDialect.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "PhizdetsFunctionTypeComment";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Phizdets PEP-484 function type comment";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "functionTypeComment";
  }
}
