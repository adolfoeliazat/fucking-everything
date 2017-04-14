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
package vgrechka.phizdetsidea.phizdets.testing.unittest;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Leonid Shalupov
 */
public class PhizdetsUnitTestRunConfigurationEditor extends SettingsEditor<PhizdetsUnitTestRunConfiguration> {
  private PhizdetsUnitTestRunConfigurationForm myForm;

  public PhizdetsUnitTestRunConfigurationEditor(final Project project, final PhizdetsUnitTestRunConfiguration configuration) {
    myForm = new PhizdetsUnitTestRunConfigurationForm(project, configuration);
  }

  protected void resetEditorFrom(@NotNull final PhizdetsUnitTestRunConfiguration config) {
    PhizdetsUnitTestRunConfiguration.copyParams(config, myForm);
  }

  protected void applyEditorTo(@NotNull final PhizdetsUnitTestRunConfiguration config) throws ConfigurationException {
    PhizdetsUnitTestRunConfiguration.copyParams(myForm, config);
  }

  @NotNull
  protected JComponent createEditor() {
    return myForm.getPanel();
  }

  protected void disposeEditor() {
    myForm = null;
  }
}
