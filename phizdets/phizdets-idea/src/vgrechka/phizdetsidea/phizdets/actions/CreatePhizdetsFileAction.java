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
package vgrechka.phizdetsidea.phizdets.actions;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import vgrechka.phizdetsidea.phizdets.PhizdetsFileType;

/**
 * @author yole
 */
public class CreatePhizdetsFileAction extends CreateFileFromTemplateAction implements DumbAware {
  public CreatePhizdetsFileAction() {
    super("Phizdets File", "Creates a Phizdets file from the specified template", PhizdetsFileType.INSTANCE.getIcon());
  }

  @Override
  protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
    builder
      .setTitle("New Phizdets file")
      .addKind("Phizdets file", PhizdetsFileType.INSTANCE.getIcon(), "Phizdets Script")
      .addKind("Phizdets unit test", PhizdetsFileType.INSTANCE.getIcon(), "Phizdets Unit Test");
  }

  @Override
  protected String getActionName(PsiDirectory directory, String newName, String templateName) {
    return "Create Phizdets script " + newName;
  }
}
