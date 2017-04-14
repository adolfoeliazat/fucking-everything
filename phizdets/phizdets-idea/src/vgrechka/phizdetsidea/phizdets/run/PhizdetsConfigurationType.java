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
package vgrechka.phizdetsidea.phizdets.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import icons.PhizdetsIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author yole
 */
public class PhizdetsConfigurationType implements ConfigurationType {

  private final PhizdetsConfigurationFactory myFactory = new PhizdetsConfigurationFactory(this);

  public static PhizdetsConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(PhizdetsConfigurationType.class);
  }

  public static class PhizdetsConfigurationFactory extends PhizdetsConfigurationFactoryBase {
    protected PhizdetsConfigurationFactory(ConfigurationType configurationType) {
      super(configurationType);
    }

    public RunConfiguration createTemplateConfiguration(Project project) {
      return new PhizdetsRunConfiguration(project, this);
    }
  }

  public String getDisplayName() {
    return "Phizdets";
  }

  public String getConfigurationTypeDescription() {
    return "Phizdets run configuration";
  }

  public Icon getIcon() {
    return PhizdetsIcons.Phizdets.Phizdets;
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myFactory};
  }

  public PhizdetsConfigurationFactory getFactory() {
    return myFactory;
  }

  @NotNull
  @NonNls
  public String getId() {
    return "PhizdetsConfigurationType";
  }
}
