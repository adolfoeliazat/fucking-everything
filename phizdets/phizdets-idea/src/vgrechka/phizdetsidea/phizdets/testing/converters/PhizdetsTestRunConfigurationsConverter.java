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
package vgrechka.phizdetsidea.phizdets.testing.converters;

import com.google.common.collect.ImmutableMap;
import com.intellij.conversion.CannotConvertException;
import com.intellij.conversion.ConversionProcessor;
import com.intellij.conversion.RunManagerSettings;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.testing.PhizdetsTestConfigurationType;
import org.jdom.Element;

/**
 * @author yole
 */
public class PhizdetsTestRunConfigurationsConverter extends ConversionProcessor<RunManagerSettings> {
  private static ImmutableMap<String, String> ourTypeToFactoryNameMap = ImmutableMap.<String, String>builder()
    .put("PhizdetsUnitTestConfigurationType", PyBundle.message("runcfg.unittest.display_name"))
    .put("PhizdetsDocTestRunConfigurationType", PyBundle.message("runcfg.doctest.display_name"))
    .put("PhizdetsNoseTestRunConfigurationType", PyBundle.message("runcfg.nosetests.display_name"))
    .put("py.test", PyBundle.message("runcfg.pytest.display_name"))
    .build();
  
  @Override
  public boolean isConversionNeeded(RunManagerSettings runManagerSettings) {
    for (Element e : runManagerSettings.getRunConfigurations()) {
      if (isConversionNeeded(e)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void process(RunManagerSettings runManagerSettings) throws CannotConvertException {
    for (Element element : runManagerSettings.getRunConfigurations()) {
      final String confType = element.getAttributeValue("type");
      final String factoryName = ourTypeToFactoryNameMap.get(confType);
      if (factoryName != null) {
        element.setAttribute("type", PhizdetsTestConfigurationType.ID);
        element.setAttribute("factoryName", factoryName);
      }
    }
  }

  private static boolean isConversionNeeded(Element element) {
    final String confType = element.getAttributeValue("type");
    return ourTypeToFactoryNameMap.containsKey(confType);
  }
}
