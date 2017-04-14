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
package vgrechka.phizdetsidea.phizdets.sdk.flavors;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParamsGroup;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PatternUtil;
import vgrechka.phizdetsidea.phizdets.run.PhizdetsCommandLineState;
import icons.PhizdetsIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author yole
 */
public class JythonSdkFlavor extends PhizdetsSdkFlavor {
  private static final Pattern VERSION_RE = Pattern.compile("(Jython \\S+)( on .*)?");
  private static final String JYTHONPATH = "JYTHONPATH";
  private static final String PYTHON_PATH_PREFIX = "-Dphizdets.path=";

  private JythonSdkFlavor() {
  }

  public static JythonSdkFlavor INSTANCE = new JythonSdkFlavor();

  public boolean isValidSdkPath(@NotNull File file) {
    return FileUtil.getNameWithoutExtension(file).toLowerCase().startsWith("jython");
  }

  @Nullable
  @Override
  public String getVersionStringFromOutput(@NotNull String output) {
    return PatternUtil.getFirstMatch(Arrays.asList(StringUtil.splitByLines(output)), VERSION_RE);
  }

  @Override
  public String getVersionOption() {
    return "--version";
  }

  @Override
  public void initPhizdetsPath(GeneralCommandLine cmd, Collection<String> path) {
    initPhizdetsPath(path, cmd.getEnvironment());
    ParamsGroup paramGroup = cmd.getParametersList().getParamsGroup(PhizdetsCommandLineState.GROUP_EXE_OPTIONS);
    assert paramGroup != null;
    for (String param : paramGroup.getParameters()) {
      if (param.startsWith(PYTHON_PATH_PREFIX)) {
        return;
      }
    }
    paramGroup.addParameter(getPhizdetsPathCmdLineArgument(path));
  }

  @Override
  public void initPhizdetsPath(Collection<String> path, Map<String, String> env) {
    path = appendSystemEnvPaths(path, JYTHONPATH);
    final String jythonPath = StringUtil.join(path, File.pathSeparator);
    addToEnv(JYTHONPATH, jythonPath, env);
  }

  @NotNull
  @Override
  public String getName() {
    return "Jython";
  }

  public static String getPhizdetsPathCmdLineArgument(Collection<String> path) {
    return PYTHON_PATH_PREFIX + StringUtil.join(appendSystemEnvPaths(path, JYTHONPATH), File.pathSeparator);
  }

  @Override
  public Icon getIcon() {
    return PhizdetsIcons.Phizdets.Jython;
  }
}
