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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import vgrechka.phizdetsidea.phizdets.psi.PyStubElementType;
import vgrechka.phizdetsidea.phizdets.psi.PyTupleParameter;
import vgrechka.phizdetsidea.phizdets.psi.impl.PyTupleParameterImpl;
import vgrechka.phizdetsidea.phizdets.psi.stubs.PyTupleParameterStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Does actual storing and loading of tuple parameter stub. Not much to do.
 */
public class PyTupleParameterElementType extends PyStubElementType<PyTupleParameterStub, PyTupleParameter> {

  public PyTupleParameterElementType() {
    super("TUPLE_PARAMETER");
  }

  @NotNull
  public PsiElement createElement(@NotNull final ASTNode node) {
    return new PyTupleParameterImpl(node);
  }

  public PyTupleParameter createPsi(@NotNull PyTupleParameterStub stub) {
    return new PyTupleParameterImpl(stub);
  }

  @NotNull
  public PyTupleParameterStub createStub(@NotNull PyTupleParameter psi, StubElement parentStub) {
    return new PyTupleParameterStubImpl(psi.hasDefaultValue(), parentStub);
  }

  @NotNull
  public PyTupleParameterStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    boolean hasDefaultValue = dataStream.readBoolean();
    return new PyTupleParameterStubImpl(hasDefaultValue, parentStub);
  }

  public void serialize(@NotNull PyTupleParameterStub stub, @NotNull StubOutputStream dataStream) throws IOException {
    dataStream.writeBoolean(stub.hasDefaultValue());
  }
}
