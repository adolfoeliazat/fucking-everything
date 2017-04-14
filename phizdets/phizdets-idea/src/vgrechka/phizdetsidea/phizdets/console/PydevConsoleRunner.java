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
package vgrechka.phizdetsidea.phizdets.console;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.console.LanguageConsoleView;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingProjectManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.PathMappingSettings;
import vgrechka.phizdetsidea.phizdets.console.completion.PydevConsoleElement;
import vgrechka.phizdetsidea.phizdets.console.parsing.PhizdetsConsoleData;
import vgrechka.phizdetsidea.phizdets.console.pydev.ConsoleCommunication;
import vgrechka.phizdetsidea.phizdets.remote.PyRemotePathMapper;
import vgrechka.phizdetsidea.phizdets.remote.PyRemoteSdkAdditionalDataBase;
import vgrechka.phizdetsidea.phizdets.remote.PhizdetsRemoteInterpreterManager;
import vgrechka.phizdetsidea.phizdets.run.PhizdetsCommandLineState;
import vgrechka.phizdetsidea.phizdets.sdk.PySdkUtil;
import vgrechka.phizdetsidea.phizdets.sdk.PhizdetsSdkType;
import vgrechka.phizdetsidea.phizdets.sdk.flavors.PhizdetsSdkFlavor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

import static vgrechka.phizdetsidea.phizdets.sdk.PhizdetsEnvUtil.setPhizdetsIOEncoding;
import static vgrechka.phizdetsidea.phizdets.sdk.PhizdetsEnvUtil.setPhizdetsUnbuffered;

/**
 * Created by Yuli Fiterman on 9/13/2016.
 */
public interface PydevConsoleRunner {

  Key<ConsoleCommunication> CONSOLE_KEY = new Key<>("PYDEV_CONSOLE_KEY");
  Key<Sdk> CONSOLE_SDK = new Key<>("PYDEV_CONSOLE_SDK_KEY");

  interface ConsoleListener {
    void handleConsoleInitialized(LanguageConsoleView consoleView);
  }


  @Nullable
  static PyRemotePathMapper getPathMapper(@NotNull Project project, Sdk sdk, PyConsoleOptions.PyConsoleSettings consoleSettings) {
    if (PySdkUtil.isRemote(sdk)) {
      PhizdetsRemoteInterpreterManager instance = PhizdetsRemoteInterpreterManager.getInstance();
      if (instance != null) {
        //noinspection ConstantConditions
        PyRemotePathMapper remotePathMapper =
          instance.setupMappings(project, (PyRemoteSdkAdditionalDataBase)sdk.getSdkAdditionalData(), null);

        PathMappingSettings mappingSettings = consoleSettings.getMappingSettings();

        remotePathMapper.addAll(mappingSettings.getPathMappings(), PyRemotePathMapper.PyPathMappingType.USER_DEFINED);

        return remotePathMapper;
      }
    }
    return null;
  }

  @NotNull
  static Pair<Sdk, Module> findPhizdetsSdkAndModule(@NotNull Project project, @Nullable Module contextModule) {
    Sdk sdk = null;
    Module module = null;
    PyConsoleOptions.PyConsoleSettings settings = PyConsoleOptions.getInstance(project).getPhizdetsConsoleSettings();
    String sdkHome = settings.getSdkHome();
    if (sdkHome != null) {
      sdk = PhizdetsSdkType.findSdkByPath(sdkHome);
      if (settings.getModuleName() != null) {
        module = ModuleManager.getInstance(project).findModuleByName(settings.getModuleName());
      }
      else {
        module = contextModule;
        if (module == null && ModuleManager.getInstance(project).getModules().length > 0) {
          module = ModuleManager.getInstance(project).getModules()[0];
        }
      }
    }
    if (sdk == null && settings.isUseModuleSdk()) {
      if (contextModule != null) {
        module = contextModule;
      }
      else if (settings.getModuleName() != null) {
        module = ModuleManager.getInstance(project).findModuleByName(settings.getModuleName());
      }
      if (module != null) {
        if (PhizdetsSdkType.findPhizdetsSdk(module) != null) {
          sdk = PhizdetsSdkType.findPhizdetsSdk(module);
        }
      }
    }
    else if (contextModule != null) {
      if (module == null) {
        module = contextModule;
      }
      if (sdk == null) {
        sdk = PhizdetsSdkType.findPhizdetsSdk(module);
      }
    }

    if (sdk == null) {
      for (Module m : ModuleManager.getInstance(project).getModules()) {
        if (PhizdetsSdkType.findPhizdetsSdk(m) != null) {
          sdk = PhizdetsSdkType.findPhizdetsSdk(m);
          module = m;
          break;
        }
      }
    }
    if (sdk == null) {
      if (PhizdetsSdkType.getAllSdks().size() > 0) {
        //noinspection UnusedAssignment
        sdk = PhizdetsSdkType.getAllSdks().get(0); //take any phizdets sdk
      }
    }
    return Pair.create(sdk, module);
  }

  static String constructPyPathAndWorkingDirCommand(Collection<String> phizdetsPath, String workingDir, String command) {
    phizdetsPath.add(workingDir);
    final String path = Joiner.on(", ").join(Collections2.transform(phizdetsPath, new Function<String, String>() {
      @Override
      public String apply(String input) {
        return "'" + input.replace("\\", "\\\\").replace("'", "\\'") + "'";
      }
    }));

    return command.replace(PydevConsoleRunnerImpl.WORKING_DIR_AND_PYTHON_PATHS, path);
  }

  static Map<String, String> addDefaultEnvironments(Sdk sdk, Map<String, String> envs, @NotNull Project project) {
    setCorrectStdOutEncoding(envs, project);

    PhizdetsSdkFlavor.initPhizdetsPath(envs, true, PhizdetsCommandLineState.getAddedPaths(sdk));
    return envs;
  }

  /**
   * Add required ENV var to Phizdets task to set its stdout charset to current project charset to allow it print correctly.
   *
   * @param envs    map of envs to add variable
   * @param project current project
   */
  static void setCorrectStdOutEncoding(@NotNull Map<String, String> envs, @NotNull Project project) {
    final Charset defaultCharset = EncodingProjectManager.getInstance(project).getDefaultCharset();
    final String encoding = defaultCharset.name();
    setPhizdetsIOEncoding(setPhizdetsUnbuffered(envs), encoding);
  }

  /**
   * Set command line charset as current project charset.
   * Add required ENV var to Phizdets task to set its stdout charset to current project charset to allow it print correctly.
   *
   * @param commandLine command line
   * @param project     current project
   */
  static void setCorrectStdOutEncoding(@NotNull GeneralCommandLine commandLine, @NotNull Project project) {
    final Charset defaultCharset = EncodingProjectManager.getInstance(project).getDefaultCharset();
    commandLine.setCharset(defaultCharset);
    setPhizdetsIOEncoding(commandLine.getEnvironment(), defaultCharset.name());
  }

  static boolean isInPydevConsole(PsiElement element) {
    return element instanceof PydevConsoleElement || getConsoleCommunication(element) != null;
  }

  static boolean isPhizdetsConsole(@Nullable ASTNode element) {
    return getPhizdetsConsoleData(element) != null;
  }

  @Nullable
  static PhizdetsConsoleData getPhizdetsConsoleData(@Nullable ASTNode element) {
    if (element == null || element.getPsi() == null || element.getPsi().getContainingFile() == null) {
      return null;
    }

    VirtualFile file = PydevConsoleRunnerImpl.getConsoleFile(element.getPsi().getContainingFile());

    if (file == null) {
      return null;
    }
    return file.getUserData(PyConsoleUtil.PYTHON_CONSOLE_DATA);
  }

  @Nullable
  static ConsoleCommunication getConsoleCommunication(PsiElement element) {
    final PsiFile containingFile = element.getContainingFile();
    return containingFile != null ? containingFile.getCopyableUserData(CONSOLE_KEY) : null;
  }

  @Nullable
  static Sdk getConsoleSdk(PsiElement element) {
    final PsiFile containingFile = element.getContainingFile();
    return containingFile != null ? containingFile.getCopyableUserData(CONSOLE_SDK) : null;
  }

  void open();

  void runSync();

  void run();

  PydevConsoleCommunication getPydevConsoleCommunication();

  void addConsoleListener(PydevConsoleRunnerImpl.ConsoleListener consoleListener);

  PydevConsoleExecuteActionHandler getConsoleExecuteActionHandler();

  PyConsoleProcessHandler getProcessHandler();

  PhizdetsConsoleView getConsoleView();
}
