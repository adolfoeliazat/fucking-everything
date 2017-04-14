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
package vgrechka.phizdetsidea.phizdets.refactoring.classes.pullUp;


import vgrechka.phizdetsidea.phizdets.psi.PyClass;
import vgrechka.phizdetsidea.phizdets.refactoring.classes.membersManager.vp.MembersBasedView;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ilya.Kazakevich
 *         View for pull-up refactoring
 */
public interface PyPullUpView extends MembersBasedView<PyPullUpViewInitializationInfo> {

  /**
   * @return Parent that user selected
   */
  @NotNull
  PyClass getSelectedParent();

  /**
   * Display "nothing to refactor" message.
   */
  void showNothingToRefactor();
}