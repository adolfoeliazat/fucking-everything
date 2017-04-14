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
package vgrechka.phizdetsidea.phizdets.run;

import com.google.common.collect.Lists;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Output;
import com.intellij.execution.OutputListener;
import com.intellij.execution.RunContentExecutor;
import com.intellij.execution.configurations.EncodingEnvironmentUtil;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParamsGroup;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.NotNullFunction;
import vgrechka.phizdetsidea.phizdets.HelperPackage;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.buildout.BuildoutFacet;
import vgrechka.phizdetsidea.phizdets.console.PydevConsoleRunner;
import vgrechka.phizdetsidea.phizdets.sdk.PySdkUtil;
import vgrechka.phizdetsidea.phizdets.sdk.PhizdetsEnvUtil;
import vgrechka.phizdetsidea.phizdets.sdk.PhizdetsSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO: Use {@link vgrechka.phizdetsidea.phizdets.run.PhizdetsRunner} instead of this class? At already supports rerun and other things
 * Base class for tasks which are run from PyCharm with results displayed in a toolwindow (manage.py, setup.py, Sphinx etc).
 *
 * @author yole
 */
public class PhizdetsTask {
  /**
   * Mils we wait to process to be stopped when "rerun" called
   */
  private static final long TIME_TO_WAIT_PROCESS_STOP = 2000L;
  private static final int TIMEOUT_TO_WAIT_FOR_TASK = 30000;
  protected final Module myModule;
  private final Sdk mySdk;
  private String myWorkingDirectory;
  private String myRunnerScript;
  private HelperPackage myHelper = null;

  private List<String> myParameters = new ArrayList<>();
  private final String myRunTabTitle;
  private String myHelpId;
  private Runnable myAfterCompletion;

  public PhizdetsTask(Module module, String runTabTitle) throws ExecutionException {
    this(module, runTabTitle, PhizdetsSdkType.findPhizdetsSdk(module));
  }

  @NotNull
  public static PhizdetsTask create(@NotNull final Module module,
                                  @NotNull final String runTabTitle,
                                  @NotNull final Sdk sdk) {
    // Ctor throws checked exception which is not good, so this wrapper saves user from dumb code
    try {
      return new PhizdetsTask(module, runTabTitle, sdk);
    }
    catch (final ExecutionException ignored) {
      throw new AssertionError("Exception thrown file should not be");
    }
  }

  public PhizdetsTask(final Module module, final String runTabTitle, @Nullable final Sdk sdk) throws ExecutionException {
    myModule = module;
    myRunTabTitle = runTabTitle;
    mySdk = sdk;
    if (mySdk == null) { // TODO: Get rid of such a weird contract
      throw new ExecutionException("Cannot find Phizdets interpreter for selected module");
    }
  }

  public String getWorkingDirectory() {
    return myWorkingDirectory;
  }

  public void setWorkingDirectory(String workingDirectory) {
    myWorkingDirectory = workingDirectory;
  }

  public void setRunnerScript(String script) {
    myRunnerScript = script;
  }

  public void setHelper(HelperPackage helper) {
    myHelper = helper;
  }

  public void setParameters(List<String> parameters) {
    myParameters = parameters;
  }

  public void setHelpId(String helpId) {
    myHelpId = helpId;
  }

  public void setAfterCompletion(Runnable afterCompletion) {
    myAfterCompletion = afterCompletion;
  }

  /**
   * @param env environment variables to be passed to process or null if nothing should be passed
   */
  public ProcessHandler createProcess(@Nullable final Map<String, String> env) throws ExecutionException {
    final GeneralCommandLine commandLine = createCommandLine();
    if (env != null) {
      commandLine.getEnvironment().putAll(env);
    }
    PydevConsoleRunner.setCorrectStdOutEncoding(commandLine, myModule.getProject()); // To support UTF-8 output

    ProcessHandler handler;
    if (PySdkUtil.isRemote(mySdk)) {
      assert mySdk != null;
      handler = new PyRemoteProcessStarter().startRemoteProcess(mySdk, commandLine, myModule.getProject(), null);
    }
    else {
      EncodingEnvironmentUtil.setLocaleEnvironmentIfMac(commandLine);
      handler = PhizdetsProcessRunner.createProcessHandlingCtrlC(commandLine);

      ProcessTerminatedListener.attach(handler);
    }
    return handler;
  }


  /**
   * Runs command using env vars from facet
   * @param consoleView console view to be used for command or null to create new
   * @throws ExecutionException failed to execute command
   */
  public void run(@Nullable final ConsoleView consoleView) throws ExecutionException {
    run(createCommandLine().getEnvironment(), consoleView);
  }

  public GeneralCommandLine createCommandLine() {
    GeneralCommandLine cmd = new GeneralCommandLine();

    if (myWorkingDirectory != null) {
      cmd.setWorkDirectory(myWorkingDirectory);
    }

    String homePath = mySdk.getHomePath();
    if (homePath != null) {
      homePath = FileUtil.toSystemDependentName(homePath);
    }

    PhizdetsCommandLineState.createStandardGroups(cmd);
    ParamsGroup scriptParams = cmd.getParametersList().getParamsGroup(PhizdetsCommandLineState.GROUP_SCRIPT);
    assert scriptParams != null;

    Map<String, String> env = cmd.getEnvironment();
    if (!SystemInfo.isWindows && !PySdkUtil.isRemote(mySdk)) {
      cmd.setExePath("bash");
      ParamsGroup bashParams = cmd.getParametersList().addParamsGroupAt(0, "Bash");
      bashParams.addParameter("-cl");

      NotNullFunction<String, String> escaperFunction = StringUtil.escaper(false, "|>$\"'& ");
      StringBuilder paramString;
      if (myHelper != null) {
        paramString = new StringBuilder(escaperFunction.fun(homePath) + " " + escaperFunction.fun(myHelper.asParamString()));
        myHelper.addToPhizdetsPath(cmd.getEnvironment());
      }
      else {
        paramString = new StringBuilder(escaperFunction.fun(homePath) + " " + escaperFunction.fun(myRunnerScript));
      }
      for (String p : myParameters) {
        paramString.append(" ").append(p);
      }
      bashParams.addParameter(paramString.toString());
    }
    else {
      cmd.setExePath(homePath);
      if (myHelper != null) {
        myHelper.addToGroup(scriptParams, cmd);
      }
      else {
        scriptParams.addParameter(myRunnerScript);
      }
      scriptParams.addParameters(myParameters.stream().filter( o -> o != null).collect(Collectors.toList()));
    }

    PhizdetsEnvUtil.setPhizdetsUnbuffered(env);
    if (homePath != null) {
      PhizdetsEnvUtil.resetHomePathChanges(homePath, env);
    }

    List<String> phizdetsPath = setupPhizdetsPath();
    PhizdetsCommandLineState.initPhizdetsPath(cmd, true, phizdetsPath, homePath);

    BuildoutFacet facet = BuildoutFacet.getInstance(myModule);
    if (facet != null) {
      facet.patchCommandLineForBuildout(cmd);
    }

    return cmd;
  }

  protected List<String> setupPhizdetsPath() {
    return setupPhizdetsPath(true, true);
  }

  protected List<String> setupPhizdetsPath(final boolean addContent, final boolean addSource) {
    final List<String> phizdetsPath = Lists.newArrayList(PhizdetsCommandLineState.getAddedPaths(mySdk));
    phizdetsPath.addAll(PhizdetsCommandLineState.collectPhizdetsPath(myModule, addContent, addSource));
    return phizdetsPath;
  }

  /**
   * @param env         environment variables to be passed to process or null if nothing should be passed
   * @param consoleView console to run this task on. New console will be used if no console provided.
   */
  public void run(@Nullable final Map<String, String> env, @Nullable final ConsoleView consoleView) throws ExecutionException {
    final ProcessHandler process = createProcess(env);
    final Project project = myModule.getProject();
    new RunContentExecutor(project, process)
      .withFilter(new PhizdetsTracebackFilter(project))
      .withConsole(consoleView)
      .withTitle(myRunTabTitle)
      .withRerun(() -> {
        try {
          process.destroyProcess(); // Stop process before rerunning it
          if (process.waitFor(TIME_TO_WAIT_PROCESS_STOP)) {
            this.run(env, consoleView);
          }
          else {
            Messages.showErrorDialog(PyBundle.message("unable.to.stop"), myRunTabTitle);
          }
        }
        catch (ExecutionException e) {
          Messages.showErrorDialog(e.getMessage(), myRunTabTitle);
        }
      })
      .withStop(() -> process.destroyProcess(), () -> !process.isProcessTerminated()
      )
      .withAfterCompletion(myAfterCompletion)
      .withHelpId(myHelpId)
      .run();
  }


  /**
   * Runs task with out console
   * @return stdout
   * @throws ExecutionException in case of error. Consider using {@link com.intellij.execution.util.ExecutionErrorDialog}
   */
  @NotNull
  public final String runNoConsole() throws ExecutionException {

    final ProcessHandler process = createProcess(new HashMap<>());
    final OutputListener listener = new OutputListener();
    process.addProcessListener(listener);
    process.startNotify();
    process.waitFor(TIMEOUT_TO_WAIT_FOR_TASK);
    final Output output = listener.getOutput();
    final int exitCode = output.getExitCode();
    if (exitCode == 0) {
      return output.getStdout();
    }
    throw new ExecutionException(String.format("Error on phizdets side. " +
                                               "Exit code: %s, err: %s out: %s", exitCode, output.getStderr(), output.getStdout()));
  }
}