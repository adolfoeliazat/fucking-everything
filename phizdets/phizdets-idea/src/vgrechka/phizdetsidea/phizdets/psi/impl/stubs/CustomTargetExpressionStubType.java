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
package vgrechka.phizdetsidea.phizdets.psi.impl.stubs;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubInputStream;
import vgrechka.phizdetsidea.phizdets.psi.PyTargetExpression;
import vgrechka.phizdetsidea.phizdets.psi.stubs.PyTargetExpressionStub;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author yole
 */
public abstract class CustomTargetExpressionStubType<T extends CustomTargetExpressionStub> {
  public static final ExtensionPointName<CustomTargetExpressionStubType> EP_NAME = ExtensionPointName.create("Phizdetsid.customTargetExpressionStubType");

  @Nullable
  public abstract T createStub(PyTargetExpression psi);

  @Nullable
  public abstract T deserializeStub(StubInputStream stream) throws IOException;

  public void indexStub(PyTargetExpressionStub stub, IndexSink sink) {
  }
}
