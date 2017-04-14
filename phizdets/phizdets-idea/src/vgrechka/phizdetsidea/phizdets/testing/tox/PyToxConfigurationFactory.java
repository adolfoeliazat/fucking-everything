/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package vgrechka.phizdetsidea.phizdets.testing.tox;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import vgrechka.phizdetsidea.phizdets.run.PhizdetsConfigurationFactoryBase;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ilya.Kazakevich
 */
public final  class PyToxConfigurationFactory extends PhizdetsConfigurationFactoryBase {
  public static final ConfigurationFactory INSTANCE = new PyToxConfigurationFactory(PyToxConfigurationType.INSTANCE);

  public PyToxConfigurationFactory(@NotNull final ConfigurationType type) {
    super(type);
  }

  @Override
  public String getName() {
    return "Tox";
  }

  @NotNull
  @Override
  public RunConfiguration createTemplateConfiguration(@NotNull final Project project) {
    return new PyToxConfiguration(this, project);
  }
}
