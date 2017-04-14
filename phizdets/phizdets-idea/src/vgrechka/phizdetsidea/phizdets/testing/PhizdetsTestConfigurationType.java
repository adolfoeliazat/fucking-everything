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
package vgrechka.phizdetsidea.phizdets.testing;

import com.google.common.collect.ObjectArrays;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.impl.ProjectLifecycleListener;
import com.intellij.openapi.startup.StartupManager;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.run.PhizdetsConfigurationFactoryBase;
import vgrechka.phizdetsidea.phizdets.testing.doctest.PhizdetsDocTestRunConfiguration;
import vgrechka.phizdetsidea.phizdets.testing.nosetest.PhizdetsNoseTestRunConfiguration;
import vgrechka.phizdetsidea.phizdets.testing.pytest.PyTestRunConfiguration;
import vgrechka.phizdetsidea.phizdets.testing.unittest.PhizdetsUnitTestRunConfiguration;
import vgrechka.phizdetsidea.phizdets.testing.universalTests.PyUniversalTestLegacyInteropKt;
import vgrechka.phizdetsidea.phizdets.testing.universalTests.PyUniversalTestsKt;
import icons.PhizdetsIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User : catherine
 * <p>
 * This type is used both with Legacy and New test runners.
 * {@link PyUniversalTestLegacyInteropKt} is used to support legacy. To drop legacy support, remove all code that depends on it.
 */
public final class PhizdetsTestConfigurationType implements ConfigurationType {
  public static final String ID = "tests";

  public final PhizdetsConfigurationFactoryBase PY_DOCTEST_FACTORY = new PhizdetsDocTestConfigurationFactory(this);
  public final PhizdetsConfigurationFactoryBase LEGACY_UNITTEST_FACTORY = new PhizdetsLegacyUnitTestConfigurationFactory(this);
  public final PhizdetsConfigurationFactoryBase LEGACY_NOSETEST_FACTORY = new PhizdetsLegacyNoseTestConfigurationFactory(this);
  public final PhizdetsConfigurationFactoryBase LEGACY_PYTEST_FACTORY = new PhizdetsLegacyPyTestConfigurationFactory(this);

  public static PhizdetsTestConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(PhizdetsTestConfigurationType.class);
  }

  public PhizdetsTestConfigurationType() {
  }

  @Override
  public String getDisplayName() {
    return PyBundle.message("runcfg.test.display_name");
  }

  @Override
  public String getConfigurationTypeDescription() {
    return PyBundle.message("runcfg.test.description");
  }

  @Override
  public Icon getIcon() {
    return PhizdetsIcons.Phizdets.PhizdetsTests;
  }


  private static class PhizdetsLegacyUnitTestConfigurationFactory extends PhizdetsConfigurationFactoryBase {
    protected PhizdetsLegacyUnitTestConfigurationFactory(ConfigurationType configurationType) {
      super(configurationType);
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
      return new PhizdetsUnitTestRunConfiguration(project, this);
    }

    @Override
    public String getName() {
      return PyBundle.message("runcfg.unittest.display_name");
    }
  }

  private static class PhizdetsDocTestConfigurationFactory extends PhizdetsConfigurationFactoryBase {
    protected PhizdetsDocTestConfigurationFactory(ConfigurationType configurationType) {
      super(configurationType);
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
      return new PhizdetsDocTestRunConfiguration(project, this);
    }

    @Override
    public String getName() {
      return PyBundle.message("runcfg.doctest.display_name");
    }
  }

  private static class PhizdetsLegacyPyTestConfigurationFactory extends PhizdetsConfigurationFactoryBase {
    protected PhizdetsLegacyPyTestConfigurationFactory(ConfigurationType configurationType) {
      super(configurationType);
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
      return new PyTestRunConfiguration(project, this);
    }

    @Override
    public String getName() {
      return PyBundle.message("runcfg.pytest.display_name");
    }
  }

  private static class PhizdetsLegacyNoseTestConfigurationFactory extends PhizdetsConfigurationFactoryBase {
    protected PhizdetsLegacyNoseTestConfigurationFactory(ConfigurationType configurationType) {
      super(configurationType);
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
      return new PhizdetsNoseTestRunConfiguration(project, this);
    }

    @Override
    public String getName() {
      return PyBundle.message("runcfg.nosetests.display_name");
    }
  }

  @NotNull
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public ConfigurationFactory[] getConfigurationFactories() {
    // Use new or legacy factories depending to new config
    final ConfigurationFactory[] factories = PyUniversalTestLegacyInteropKt.isNewTestsModeEnabled()
                                             ? PyUniversalTestsKt.getFactories()
                                             : new ConfigurationFactory[]
                                               {LEGACY_UNITTEST_FACTORY, LEGACY_NOSETEST_FACTORY, LEGACY_PYTEST_FACTORY};
    return ObjectArrays.concat(factories, PY_DOCTEST_FACTORY);
  }
}
