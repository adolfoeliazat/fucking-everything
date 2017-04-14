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
package vgrechka.phizdetsidea.phizdets.console;

import com.google.common.collect.Maps;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.PathMapper;
import vgrechka.phizdetsidea.phizdets.buildout.BuildoutFacet;
import vgrechka.phizdetsidea.phizdets.run.PhizdetsCommandLineState;
import vgrechka.phizdetsidea.phizdets.sdk.PhizdetsEnvUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author traff
 */
public class PydevConsoleRunnerFactory extends PhizdetsConsoleRunnerFactory {
  @Override
  @NotNull
  public PydevConsoleRunnerImpl createConsoleRunner(@NotNull Project project,
                                                    @Nullable Module contextModule) {
    Pair<Sdk, Module> sdkAndModule = PydevConsoleRunner.findPhizdetsSdkAndModule(project, contextModule);

    Module module = sdkAndModule.second;
    Sdk sdk = sdkAndModule.first;

    assert sdk != null;

    PyConsoleOptions.PyConsoleSettings settingsProvider = PyConsoleOptions.getInstance(project).getPhizdetsConsoleSettings();

    PathMapper pathMapper = PydevConsoleRunner.getPathMapper(project, sdk, settingsProvider);

    String[] setupFragment;

    Collection<String> phizdetsPath = PhizdetsCommandLineState.collectPhizdetsPath(module, settingsProvider.shouldAddContentRoots(),
                                                                             settingsProvider.shouldAddSourceRoots());

    if (pathMapper != null) {
      phizdetsPath = pathMapper.convertToRemote(phizdetsPath);
    }

    String customStartScript = settingsProvider.getCustomStartScript();

    if (customStartScript.trim().length() > 0) {
      customStartScript = "\n" + customStartScript;
    }

    String workingDir = settingsProvider.getWorkingDirectory();
    if (StringUtil.isEmpty(workingDir)) {
      if (module != null && ModuleRootManager.getInstance(module).getContentRoots().length > 0) {
        workingDir = ModuleRootManager.getInstance(module).getContentRoots()[0].getPath();
      }
      else {
        if (ModuleManager.getInstance(project).getModules().length > 0) {
          VirtualFile[] roots = ModuleRootManager.getInstance(ModuleManager.getInstance(project).getModules()[0]).getContentRoots();
          if (roots.length > 0) {
            workingDir = roots[0].getPath();
          }
        }
      }
    }

    if (pathMapper != null && workingDir != null) {
      workingDir = pathMapper.convertToRemote(workingDir);
    }

    String selfPathAppend = PydevConsoleRunner.constructPyPathAndWorkingDirCommand(phizdetsPath, workingDir, customStartScript);

    BuildoutFacet facet = null;
    if (module != null) {
      facet = BuildoutFacet.getInstance(module);
    }

    if (facet != null) {
      List<String> path = facet.getAdditionalPhizdetsPath();
      if (pathMapper != null) {
        path = pathMapper.convertToRemote(path);
      }
      String prependStatement = facet.getPathPrependStatement(path);
      setupFragment = new String[]{prependStatement, selfPathAppend};
    }
    else {
      setupFragment = new String[]{selfPathAppend};
    }

    Map<String, String> envs = Maps.newHashMap(settingsProvider.getEnvs());
    putIPhizdetsEnvFlag(project, envs);

    Consumer<String> rerunAction = title -> {
      PydevConsoleRunnerImpl runner = createConsoleRunner(project, module);
      runner.setConsoleTitle(title);
      runner.run();
    };

    return createConsoleRunner(project, sdk, workingDir, envs, PyConsoleType.PYTHON, settingsProvider, rerunAction, setupFragment);
  }

  public static void putIPhizdetsEnvFlag(@NotNull Project project, Map<String, String> envs) {
    String iphizdetsEnabled = PyConsoleOptions.getInstance(project).isIphizdetsEnabled() ? "True" : "False";
    envs.put(PhizdetsEnvUtil.IPYTHONENABLE, iphizdetsEnabled);
  }

  @NotNull
  protected PydevConsoleRunnerImpl createConsoleRunner(Project project,
                                                       Sdk sdk,
                                                       String workingDir,
                                                       Map<String, String> envs,
                                                       PyConsoleType consoleType,
                                                       PyConsoleOptions.PyConsoleSettings settingsProvider,
                                                       Consumer<String> rerunAction, String... setupFragment) {
    return new PydevConsoleRunnerImpl(project, sdk, consoleType, workingDir, envs, settingsProvider, rerunAction, setupFragment);
  }
}
