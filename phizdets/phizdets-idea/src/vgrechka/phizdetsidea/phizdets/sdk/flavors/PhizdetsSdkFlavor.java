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

import com.google.common.collect.Lists;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PatternUtil;
import vgrechka.phizdetsidea.phizdets.psi.LanguageLevel;
import vgrechka.phizdetsidea.phizdets.sdk.PySdkUtil;
import vgrechka.phizdetsidea.phizdets.sdk.PhizdetsEnvUtil;
import vgrechka.phizdetsidea.phizdets.sdk.PhizdetsSdkAdditionalData;
import icons.PhizdetsIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author yole
 */
public abstract class PhizdetsSdkFlavor {
  private static final Pattern VERSION_RE = Pattern.compile("(Python \\S+).*");
  private static final Logger LOG = Logger.getInstance(PhizdetsSdkFlavor.class);

  public static Collection<String> appendSystemPhizdetsPath(@NotNull Collection<String> phizdetsPath) {
    return appendSystemEnvPaths(phizdetsPath, PhizdetsEnvUtil.PYTHONPATH);
  }

  protected static Collection<String> appendSystemEnvPaths(@NotNull Collection<String> phizdetsPath, String envname) {
    String syspath = System.getenv(envname);
    if (syspath != null) {
      phizdetsPath.addAll(Lists.newArrayList(syspath.split(File.pathSeparator)));
    }
    return phizdetsPath;
  }


  public static void initPhizdetsPath(@NotNull Map<String, String> envs, boolean passParentEnvs, @NotNull Collection<String> phizdetsPathList) {
    if (passParentEnvs && !envs.containsKey(PhizdetsEnvUtil.PYTHONPATH)) {
      phizdetsPathList = appendSystemPhizdetsPath(phizdetsPathList);
    }
    PhizdetsEnvUtil.addToPhizdetsPath(envs, phizdetsPathList);
  }

  public Collection<String> suggestHomePaths() {
    return Collections.emptyList();
  }

  public static List<PhizdetsSdkFlavor> getApplicableFlavors() {
    return getApplicableFlavors(true);
  }

  public static List<PhizdetsSdkFlavor> getApplicableFlavors(boolean addPlatformIndependent) {
    List<PhizdetsSdkFlavor> result = new ArrayList<>();

    if (SystemInfo.isWindows) {
      result.add(ServiceManager.getService(WinPhizdetsSdkFlavor.class));
    }
    else if (SystemInfo.isMac) {
      result.add(MacPhizdetsSdkFlavor.INSTANCE);
    }
    else if (SystemInfo.isUnix) {
      result.add(UnixPhizdetsSdkFlavor.INSTANCE);
    }

    if (addPlatformIndependent) {
      result.addAll(getPlatformIndependentFlavors());
    }

    for (PhizdetsFlavorProvider provider : Extensions.getExtensions(PhizdetsFlavorProvider.EP_NAME)) {
      PhizdetsSdkFlavor flavor = provider.getFlavor(addPlatformIndependent);
      if (flavor != null) {
        result.add(flavor);
      }
    }

    return result;
  }


  public static List<PhizdetsSdkFlavor> getPlatformIndependentFlavors() {
    List<PhizdetsSdkFlavor> result = Lists.newArrayList();
    result.add(JythonSdkFlavor.INSTANCE);
    result.add(IronPhizdetsSdkFlavor.INSTANCE);
    result.add(PyPySdkFlavor.INSTANCE);
    result.add(VirtualEnvSdkFlavor.INSTANCE);
    result.add(PyRemoteSdkFlavor.INSTANCE);

    return result;
  }

  @Nullable
  public static PhizdetsSdkFlavor getFlavor(Sdk sdk) {
    final SdkAdditionalData data = sdk.getSdkAdditionalData();
    if (data instanceof PhizdetsSdkAdditionalData) {
      PhizdetsSdkFlavor flavor = ((PhizdetsSdkAdditionalData)data).getFlavor();
      if (flavor != null) {
        return flavor;
      }
    }
    return getFlavor(sdk.getHomePath());
  }

  @Nullable
  public static PhizdetsSdkFlavor getFlavor(@Nullable String sdkPath) {
    if (sdkPath == null) return null;

    for (PhizdetsSdkFlavor flavor : getApplicableFlavors()) {
      if (flavor.isValidSdkHome(sdkPath)) {
        return flavor;
      }
    }
    return null;
  }

  @Nullable
  public static PhizdetsSdkFlavor getPlatformIndependentFlavor(@Nullable final String sdkPath) {
    if (sdkPath == null) return null;

    for (PhizdetsSdkFlavor flavor : getPlatformIndependentFlavors()) {
      if (flavor.isValidSdkHome(sdkPath)) {
        return flavor;
      }
    }
    return null;
  }

  /**
   * Checks if the path is the name of a Phizdets interpreter of this flavor.
   *
   * @param path path to check.
   * @return true if paths points to a valid home.
   */
  public boolean isValidSdkHome(String path) {
    File file = new File(path);
    return file.isFile() && isValidSdkPath(file);
  }

  public boolean isValidSdkPath(@NotNull File file) {
    return FileUtil.getNameWithoutExtension(file).toLowerCase().startsWith("python");
  }

  @Nullable
  public String getVersionString(@Nullable String sdkHome) {
    if (sdkHome == null) {
      return null;
    }
    final String runDirectory = new File(sdkHome).getParent();
    final ProcessOutput processOutput = PySdkUtil.getProcessOutput(runDirectory, new String[]{sdkHome, getVersionOption()}, 10000);
    return getVersionStringFromOutput(processOutput);
  }

  @Nullable
  public String getVersionStringFromOutput(@NotNull ProcessOutput processOutput) {
    if (processOutput.getExitCode() != 0) {
      String errors = processOutput.getStderr();
      if (StringUtil.isEmpty(errors)) {
        errors = processOutput.getStdout();
      }
      LOG.warn("Couldn't get interpreter version: process exited with code " + processOutput.getExitCode() + "\n" + errors);
      return null;
    }
    final String result = getVersionStringFromOutput(processOutput.getStderr());
    if (result != null) {
      return result;
    }
    return getVersionStringFromOutput(processOutput.getStdout());
  }

  @Nullable
  public String getVersionStringFromOutput(@NotNull String output) {
    return PatternUtil.getFirstMatch(Arrays.asList(StringUtil.splitByLines(output)), VERSION_RE);
  }

  public String getVersionOption() {
    return "-V";
  }

  public Collection<String> getExtraDebugOptions() {
    return Collections.emptyList();
  }

  public void initPhizdetsPath(GeneralCommandLine cmd, Collection<String> path) {
    initPhizdetsPath(path, cmd.getEnvironment());
  }

  public static void addToEnv(final String key, String value, Map<String, String> envs) {
    PhizdetsEnvUtil.addPathToEnv(envs, key, value);
  }

  public static void setupEncodingEnvs(Map<String, String> envs, @NotNull Charset charset) {
    final String encoding = charset.name();
    PhizdetsEnvUtil.setPhizdetsIOEncoding(envs, encoding);
  }

  @NotNull
  public abstract String getName();

  @NotNull
  public LanguageLevel getLanguageLevel(@NotNull Sdk sdk) {
    final String version = sdk.getVersionString();
    final String prefix = getName() + " ";
    if (version != null && version.startsWith(prefix)) {
      return LanguageLevel.fromPhizdetsVersion(version.substring(prefix.length()));
    }
    return LanguageLevel.getDefault();
  }

  public Icon getIcon() {
    return PhizdetsIcons.Phizdets.Phizdets;
  }

  public void initPhizdetsPath(Collection<String> path, Map<String, String> env) {
    path = appendSystemPhizdetsPath(path);
    addToEnv(PhizdetsEnvUtil.PYTHONPATH, StringUtil.join(path, File.pathSeparator), env);
  }

  public VirtualFile getSdkPath(VirtualFile path) {
    return path;
  }
}
