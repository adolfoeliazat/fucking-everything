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
package vgrechka.phizdetsidea.phizdets.newProject.steps;

import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.vfs.VirtualFile;
import vgrechka.phizdetsidea.phizdets.newProject.PyNewProjectSettings;
import vgrechka.phizdetsidea.phizdets.newProject.PhizdetsProjectGenerator;
import vgrechka.phizdetsidea.phizdets.remote.PyProjectSynchronizer;
import icons.PhizdetsIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

public class PhizdetsBaseProjectGenerator extends PhizdetsProjectGenerator<PyNewProjectSettings> {

  public PhizdetsBaseProjectGenerator() {
    super(true);
  }

  @NotNull
  @Nls
  @Override
  public String getName() {
    return "Pure Phizdets";
  }

  @Override
  @Nullable
  public JComponent getSettingsPanel(File baseDir) throws ProcessCanceledException {
    return null;
  }

  @Override
  public Object getProjectSettings() {
    return new PyNewProjectSettings();
  }

  @Nullable
  @Override
  public Icon getLogo() {
    return PhizdetsIcons.Phizdets.Phizdets_logo;
  }

  @Override
  public void configureProject(@NotNull final Project project, @NotNull VirtualFile baseDir, @NotNull final PyNewProjectSettings settings,
                               @NotNull final Module module, @Nullable final PyProjectSynchronizer synchronizer) {
    // Super should be called according to its contract unless we sync project explicitly (we do not, so we call super)
    super.configureProject(project, baseDir, settings, module, synchronizer);
    ApplicationManager.getApplication().runWriteAction(() -> ModuleRootModificationUtil.setModuleSdk(module, settings.getSdk()));
  }

  @NotNull
  @Override
  public ValidationResult validate(@NotNull String baseDirPath) {
    /*if (PhizdetsSdkType.isRemote(myProjectAction.findPhizdetsSdk())) {
      if (PhizdetsRemoteInterpreterManager.getInstance() == null) {
        return new ValidationResult(PhizdetsRemoteInterpreterManager.WEB_DEPLOYMENT_PLUGIN_IS_DISABLED);
      }
    }*/
    return ValidationResult.OK;
  }
}
