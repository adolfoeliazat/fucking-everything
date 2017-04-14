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
package vgrechka.phizdetsidea.phizdets.newProject.steps;

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep;
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase;
import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.util.BooleanFunction;
import com.intellij.util.NullableConsumer;
import vgrechka.phizdetsidea.phizdets.configuration.PyConfigurableInterpreterList;
import vgrechka.phizdetsidea.phizdets.newProject.PyNewProjectSettings;
import vgrechka.phizdetsidea.phizdets.newProject.PhizdetsProjectGenerator;
import vgrechka.phizdetsidea.phizdets.sdk.PyDetectedSdk;
import vgrechka.phizdetsidea.phizdets.sdk.PhizdetsSdkAdditionalData;
import vgrechka.phizdetsidea.phizdets.sdk.PhizdetsSdkType;
import vgrechka.phizdetsidea.phizdets.sdk.PhizdetsSdkUpdater;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhizdetsGenerateProjectCallback implements NullableConsumer<ProjectSettingsStepBase> {
  private static final Logger LOG = Logger.getInstance(PhizdetsGenerateProjectCallback.class);

  @Override
  public void consume(@Nullable ProjectSettingsStepBase step) {
    if (!(step instanceof ProjectSpecificSettingsStep)) return;

    final ProjectSpecificSettingsStep settingsStep = (ProjectSpecificSettingsStep)step;
    final DirectoryProjectGenerator generator = settingsStep.getProjectGenerator();
    Sdk sdk = settingsStep.getSdk();

    if (sdk instanceof PyDetectedSdk) {
      sdk = addDetectedSdk(settingsStep, sdk);
    }

    if (generator instanceof PhizdetsProjectGenerator) {
      final BooleanFunction<PhizdetsProjectGenerator> beforeProjectGenerated = ((PhizdetsProjectGenerator)generator).beforeProjectGenerated(sdk);
      if (beforeProjectGenerated != null) {
        final boolean result = beforeProjectGenerated.fun((PhizdetsProjectGenerator)generator);
        if (!result) {
          Messages.showWarningDialog("Project can not be generated", "Error in Project Generation");
          return;
        }
      }
    }
    final Project newProject = generateProject(settingsStep);
    if (generator instanceof PhizdetsProjectGenerator && sdk == null && newProject != null) {
      final PyNewProjectSettings settings = (PyNewProjectSettings)((PhizdetsProjectGenerator)generator).getProjectSettings();
      ((PhizdetsProjectGenerator)generator).createAndAddVirtualEnv(newProject, settings);
      sdk = settings.getSdk();
    }

    if (newProject != null && generator instanceof PhizdetsProjectGenerator) {
      SdkConfigurationUtil.setDirectoryProjectSdk(newProject, sdk);
      final List<Sdk> sdks = PhizdetsSdkType.getAllSdks();
      for (Sdk s : sdks) {
        final SdkAdditionalData additionalData = s.getSdkAdditionalData();
        if (additionalData instanceof PhizdetsSdkAdditionalData) {
          ((PhizdetsSdkAdditionalData)additionalData).reassociateWithCreatedProject(newProject);
        }
      }
    }
  }

  private static Sdk addDetectedSdk(ProjectSpecificSettingsStep settingsStep, Sdk sdk) {
    final Project project = ProjectManager.getInstance().getDefaultProject();
    final ProjectSdksModel model = PyConfigurableInterpreterList.getInstance(project).getModel();
    final String name = sdk.getName();
    VirtualFile sdkHome = ApplicationManager.getApplication().runWriteAction((Computable<VirtualFile>)() -> LocalFileSystem.getInstance().refreshAndFindFileByPath(name));
    sdk = SdkConfigurationUtil.createAndAddSDK(sdkHome.getPath(), PhizdetsSdkType.getInstance());
    if (sdk != null) {
      PhizdetsSdkUpdater.updateOrShowError(sdk, null, project, null);
    }

    model.addSdk(sdk);
    settingsStep.setSdk(sdk);
    try {
      model.apply();
    }
    catch (ConfigurationException exception) {
      LOG.error("Error adding detected phizdets interpreter " + exception.getMessage());
    }
    return sdk;
  }

  @Nullable
  private static Project generateProject(@NotNull final ProjectSettingsStepBase settings) {
    final DirectoryProjectGenerator generator = settings.getProjectGenerator();
    final String location = FileUtil.expandUserHome(settings.getProjectLocation());
    return AbstractNewProjectStep.doGenerateProject(ProjectManager.getInstance().getDefaultProject(), location, generator,
                                                    file -> computeProjectSettings(generator, (ProjectSpecificSettingsStep)settings));
  }

  public static Object computeProjectSettings(DirectoryProjectGenerator<?> generator, final ProjectSpecificSettingsStep settings) {
    Object projectSettings = null;
    if (generator instanceof PhizdetsProjectGenerator) {
      final PhizdetsProjectGenerator<?> projectGenerator = (PhizdetsProjectGenerator<?>)generator;
      projectSettings = projectGenerator.getProjectSettings();
    }
    else if (generator instanceof WebProjectTemplate) {
      projectSettings = ((WebProjectTemplate<?>)generator).getPeer().getSettings();
    }
    if (projectSettings instanceof PyNewProjectSettings) {
      final PyNewProjectSettings newProjectSettings = (PyNewProjectSettings)projectSettings;
      newProjectSettings.setSdk(settings.getSdk());
      newProjectSettings.setInstallFramework(settings.installFramework());
      newProjectSettings.setRemotePath(settings.getRemotePath());
    }
    return projectSettings;
  }
}
