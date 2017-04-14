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
package vgrechka.phizdetsidea.phizdets.testing;

import vgrechka.phizdetsidea.phizdets.run.AbstractPhizdetsRunConfigurationParams;

/**
 * User: catherine
 */
public interface AbstractPhizdetsTestRunConfigurationParams {
  AbstractPhizdetsRunConfigurationParams getBaseParams();

  String getClassName();
  void setClassName(String className);

  String getFolderName();
  void setFolderName(String folderName);

  String getScriptName();
  void setScriptName(String scriptName);

  String getMethodName();
  void setMethodName(String methodName);

  AbstractPhizdetsLegacyTestRunConfiguration.TestType getTestType();
  void setTestType(AbstractPhizdetsLegacyTestRunConfiguration.TestType testType);

  boolean usePattern();
  void usePattern(boolean isPureUnittest);

  String getPattern();
  void setPattern(String pattern);

  boolean shouldAddContentRoots();
  boolean shouldAddSourceRoots();
  void setAddContentRoots(boolean addContentRoots);
  void setAddSourceRoots(boolean addSourceRoots);
}
