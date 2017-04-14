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
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.io.StringRef;
import vgrechka.phizdetsidea.phizdets.PyElementTypes;
import vgrechka.phizdetsidea.phizdets.psi.PyImportElement;
import vgrechka.phizdetsidea.phizdets.psi.PyStubElementType;
import vgrechka.phizdetsidea.phizdets.psi.PyTargetExpression;
import vgrechka.phizdetsidea.phizdets.psi.impl.PyImportElementImpl;
import vgrechka.phizdetsidea.phizdets.psi.stubs.PyImportElementStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author yole
 */
public class PyImportElementElementType extends PyStubElementType<PyImportElementStub, PyImportElement> {
  public PyImportElementElementType() {
    this("IMPORT_ELEMENT");
  }

  public PyImportElementElementType(@NotNull @NonNls String debugName) {
    super(debugName);
  }

  @NotNull
  @Override
  public PsiElement createElement(@NotNull ASTNode node) {
    return new PyImportElementImpl(node);
  }

  @Override
  public PyImportElement createPsi(@NotNull PyImportElementStub stub) {
    return new PyImportElementImpl(stub);
  }

  @NotNull
  @Override
  public PyImportElementStub createStub(@NotNull PyImportElement psi, StubElement parentStub) {
    final PyTargetExpression asName = psi.getAsNameElement();
    return new PyImportElementStubImpl(psi.getImportedQName(), asName != null ? asName.getName() : "", parentStub, getStubElementType());
  }

  public void serialize(@NotNull PyImportElementStub stub, @NotNull StubOutputStream dataStream) throws IOException {
    QualifiedName.serialize(stub.getImportedQName(), dataStream);
    dataStream.writeName(stub.getAsName());
  }

  @NotNull
  public PyImportElementStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    QualifiedName qName = QualifiedName.deserialize(dataStream);
    StringRef asName = dataStream.readName();
    return new PyImportElementStubImpl(qName, asName.getString(), parentStub, getStubElementType());
  }

  protected IStubElementType getStubElementType() {
    return PyElementTypes.IMPORT_ELEMENT;
  }
}
