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
package vgrechka.phizdetsidea.phizdets.refactoring.classes.membersManager;

import com.intellij.util.containers.MultiMap;
import vgrechka.phizdetsidea.phizdets.psi.PyClass;
import vgrechka.phizdetsidea.phizdets.psi.PyElement;
import vgrechka.phizdetsidea.phizdets.psi.PyRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Recursive visitor with multimap, to be used for {@link vgrechka.phizdetsidea.phizdets.refactoring.classes.membersManager.MembersManager#getDependencies(vgrechka.phizdetsidea.phizdets.psi.PyElement)}
 */
class PyRecursiveElementVisitorWithResult extends PyRecursiveElementVisitor {
  @NotNull
  protected final MultiMap<PyClass, PyElement> myResult;

  PyRecursiveElementVisitorWithResult() {
    myResult = new MultiMap<>();
  }
}
