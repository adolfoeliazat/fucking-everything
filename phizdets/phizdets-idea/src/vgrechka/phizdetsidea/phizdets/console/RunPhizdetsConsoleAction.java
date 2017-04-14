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
package vgrechka.phizdetsidea.phizdets.console;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Pair;
import icons.PhizdetsIcons;

/**
 * @author oleg
 */
public class RunPhizdetsConsoleAction extends AnAction implements DumbAware {

  public RunPhizdetsConsoleAction() {
    super();
    getTemplatePresentation().setIcon(PhizdetsIcons.Phizdets.PhizdetsConsole);
  }

  @Override
  public void update(final AnActionEvent e) {
    e.getPresentation().setVisible(true);
    e.getPresentation().setEnabled(false);
    final Project project = e.getData(CommonDataKeys.PROJECT);
    if (project != null) {
      Pair<Sdk, Module> sdkAndModule = PydevConsoleRunner.findPhizdetsSdkAndModule(project, e.getData(LangDataKeys.MODULE));
      if (sdkAndModule.first != null) {
        e.getPresentation().setEnabled(true);
      }
    }
  }

  public void actionPerformed(final AnActionEvent e) {
    PydevConsoleRunner runner = PhizdetsConsoleRunnerFactory.getInstance().createConsoleRunner(e.getData(CommonDataKeys.PROJECT), e.getData(LangDataKeys.MODULE));
    runner.open();
  }
}
