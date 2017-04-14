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
package vgrechka.phizdetsidea.phizdets.testing.doctest;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import vgrechka.phizdetsidea.phizdets.testing.AbstractPhizdetsLegacyTestRunConfiguration;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * User: catherine
 */
public class PhizdetsDocTestRunConfiguration extends AbstractPhizdetsLegacyTestRunConfiguration<PhizdetsDocTestRunConfiguration>
                                          implements PhizdetsDocTestRunConfigurationParams {
  protected String myPluralTitle = "Doctests";
  protected String myTitle = "Doctest";
  public PhizdetsDocTestRunConfiguration(Project project,
                                       ConfigurationFactory configurationFactory) {
    super(project, configurationFactory);
  }

  @Override
  protected SettingsEditor<PhizdetsDocTestRunConfiguration> createConfigurationEditor() {
    return new PhizdetsDocTestRunConfigurationEditor(getProject(), this);
  }

  @Override
  public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    return new PhizdetsDocTestCommandLineState(this, env);
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    super.readExternal(element);
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
  }

  @Override
  protected String getTitle() {
    return myTitle;
  }

  @Override
  protected String getPluralTitle() {
    return myPluralTitle;
  }

  public static void copyParams(PhizdetsDocTestRunConfigurationParams source, PhizdetsDocTestRunConfigurationParams target) {
    copyParams(source.getTestRunConfigurationParams(), target.getTestRunConfigurationParams());
  }
}
