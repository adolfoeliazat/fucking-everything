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
package vgrechka.phizdetsidea.phizdets.run;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.filters.UrlFilter;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryImpl;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingProjectManager;
import com.intellij.remote.RemoteProcessControl;
import com.intellij.util.PlatformUtils;
import vgrechka.phizdetsidea.phizdets.PhizdetsHelpersLocator;
import vgrechka.phizdetsidea.phizdets.console.PyDebugConsoleBuilder;
import vgrechka.phizdetsidea.phizdets.debugger.PyDebugRunner;
import vgrechka.phizdetsidea.phizdets.debugger.PyDebuggerOptionsProvider;
import vgrechka.phizdetsidea.phizdets.facet.LibraryContributingFacet;
import vgrechka.phizdetsidea.phizdets.facet.PhizdetsPathContributingFacet;
import vgrechka.phizdetsidea.phizdets.library.PhizdetsLibraryType;
import vgrechka.phizdetsidea.phizdets.remote.PyRemotePathMapper;
import vgrechka.phizdetsidea.phizdets.sdk.PySdkUtil;
import vgrechka.phizdetsidea.phizdets.sdk.PhizdetsEnvUtil;
import vgrechka.phizdetsidea.phizdets.sdk.PhizdetsSdkAdditionalData;
import vgrechka.phizdetsidea.phizdets.sdk.PhizdetsSdkType;
import vgrechka.phizdetsidea.phizdets.sdk.flavors.JythonSdkFlavor;
import vgrechka.phizdetsidea.phizdets.sdk.flavors.PhizdetsSdkFlavor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author traff, Leonid Shalupov
 */
public abstract class PhizdetsCommandLineState extends CommandLineState {
  private static final Logger LOG = Logger.getInstance("#vgrechka.phizdetsidea.phizdets.run.PhizdetsCommandLineState");

  // command line has a number of fixed groups of parameters; patchers should only operate on them and not the raw list.

  public static final String GROUP_EXE_OPTIONS = "Exe Options";
  public static final String GROUP_DEBUGGER = "Debugger";
  public static final String GROUP_PROFILER = "Profiler";
  public static final String GROUP_COVERAGE = "Coverage";
  public static final String GROUP_SCRIPT = "Script";
  private final AbstractPhizdetsRunConfiguration myConfig;

  private Boolean myMultiprocessDebug = null;
  private boolean myRunWithPty = PtyCommandLine.isEnabled();

  public boolean isRunWithPty() {
    return myRunWithPty;
  }

  public boolean isDebug() {
    return PyDebugRunner.PY_DEBUG_RUNNER.equals(getEnvironment().getRunner().getRunnerId());
  }

  public static ServerSocket createServerSocket() throws ExecutionException {
    final ServerSocket serverSocket;
    try {
      //noinspection SocketOpenedButNotSafelyClosed
      serverSocket = new ServerSocket(0);
    }
    catch (IOException e) {
      throw new ExecutionException("Failed to find free socket port", e);
    }
    return serverSocket;
  }

  public PhizdetsCommandLineState(AbstractPhizdetsRunConfiguration runConfiguration, ExecutionEnvironment env) {
    super(env);
    myConfig = runConfiguration;
  }

  @Nullable
  public PhizdetsSdkFlavor getSdkFlavor() {
    return PhizdetsSdkFlavor.getFlavor(myConfig.getInterpreterPath());
  }

  @Nullable
  public Sdk getSdk() {
    return myConfig.getSdk();
  }

  @NotNull
  @Override
  public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
    return execute(executor, (CommandLinePatcher[])null);
  }

  public ExecutionResult execute(Executor executor, CommandLinePatcher... patchers) throws ExecutionException {
    return execute(executor, getDefaultPhizdetsProcessStarter(), patchers);
  }

  public ExecutionResult execute(Executor executor,
                                 PhizdetsProcessStarter processStarter,
                                 CommandLinePatcher... patchers) throws ExecutionException {
    final ProcessHandler processHandler = startProcess(processStarter, patchers);
    final ConsoleView console = createAndAttachConsole(myConfig.getProject(), processHandler, executor);
    return new DefaultExecutionResult(console, processHandler, createActions(console, processHandler));
  }

  @NotNull
  protected ConsoleView createAndAttachConsole(Project project, ProcessHandler processHandler, Executor executor)
    throws ExecutionException {
    final ConsoleView consoleView = createConsoleBuilder(project).getConsole();
    consoleView.addMessageFilter(createUrlFilter(processHandler));

    addTracebackFilter(project, consoleView, processHandler);

    consoleView.attachToProcess(processHandler);
    return consoleView;
  }

  protected void addTracebackFilter(Project project, ConsoleView consoleView, ProcessHandler processHandler) {
    if (PySdkUtil.isRemote(myConfig.getSdk())) {
      assert processHandler instanceof RemoteProcessControl;
      consoleView
        .addMessageFilter(new PyRemoteTracebackFilter(project, myConfig.getWorkingDirectory(), (RemoteProcessControl)processHandler));
    }
    else {
      consoleView.addMessageFilter(new PhizdetsTracebackFilter(project, myConfig.getWorkingDirectorySafe()));
    }
    consoleView.addMessageFilter(createUrlFilter(processHandler)); // Url filter is always nice to have
  }

  private TextConsoleBuilder createConsoleBuilder(Project project) {
    if (isDebug()) {
      return new PyDebugConsoleBuilder(project, PhizdetsSdkType.findSdkByPath(myConfig.getInterpreterPath()));
    }
    else {
      return TextConsoleBuilderFactory.getInstance().createBuilder(project);
    }
  }

  @Override
  @NotNull
  protected ProcessHandler startProcess() throws ExecutionException {
    return startProcess(getDefaultPhizdetsProcessStarter());
  }

  /**
   * Patches the command line parameters applying patchers from first to last, and then runs it.
   *
   * @param patchers any number of patchers; any patcher may be null, and the whole argument may be null.
   * @return handler of the started process
   * @throws ExecutionException
   * @deprecated use {@link #startProcess(PhizdetsProcessStarter, CommandLinePatcher...)} instead
   */
  @Deprecated
  @NotNull
  protected ProcessHandler startProcess(CommandLinePatcher... patchers) throws ExecutionException {
    return startProcess(getDefaultPhizdetsProcessStarter(), patchers);
  }

  /**
   * Patches the command line parameters applying patchers from first to last, and then runs it.
   *
   * @param processStarter
   * @param patchers any number of patchers; any patcher may be null, and the whole argument may be null.
   * @return handler of the started process
   * @throws ExecutionException
   */
  @NotNull
  protected ProcessHandler startProcess(PhizdetsProcessStarter processStarter, CommandLinePatcher... patchers) throws ExecutionException {
    GeneralCommandLine commandLine = generateCommandLine(patchers);

    // Extend command line
    PhizdetsRunConfigurationExtensionsManager.getInstance()
      .patchCommandLine(myConfig, getRunnerSettings(), commandLine, getEnvironment().getRunner().getRunnerId());

    ProcessHandler processHandler = processStarter.start(myConfig, commandLine);

    // attach extensions
    PhizdetsRunConfigurationExtensionsManager.getInstance().attachExtensionsToProcess(myConfig, processHandler, getRunnerSettings());

    return processHandler;
  }

  @NotNull
  protected final PhizdetsProcessStarter getDefaultPhizdetsProcessStarter() {
    return (config, commandLine) -> {
      Sdk sdk = PhizdetsSdkType.findSdkByPath(myConfig.getInterpreterPath());
      final ProcessHandler processHandler;
      if (PySdkUtil.isRemote(sdk)) {
        PyRemotePathMapper pathMapper = createRemotePathMapper();
        processHandler = createRemoteProcessStarter().startRemoteProcess(sdk, commandLine, myConfig.getProject(), pathMapper);
      }
      else {
        EncodingEnvironmentUtil.setLocaleEnvironmentIfMac(commandLine);
        processHandler = doCreateProcess(commandLine);
        ProcessTerminatedListener.attach(processHandler);
      }
      return processHandler;
    };
  }

  @Nullable
  private PyRemotePathMapper createRemotePathMapper() {
    if (myConfig.getMappingSettings() == null) {
      return null;
    }
    else {
      return PyRemotePathMapper.fromSettings(myConfig.getMappingSettings(), PyRemotePathMapper.PyPathMappingType.USER_DEFINED);
    }
  }

  protected PyRemoteProcessStarter createRemoteProcessStarter() {
    return new PyRemoteProcessStarter();
  }


  public GeneralCommandLine generateCommandLine(CommandLinePatcher[] patchers) {
    GeneralCommandLine commandLine = generateCommandLine();
    if (patchers != null) {
      for (CommandLinePatcher patcher : patchers) {
        if (patcher != null) patcher.patchCommandLine(commandLine);
      }
    }
    return commandLine;
  }

  protected ProcessHandler doCreateProcess(GeneralCommandLine commandLine) throws ExecutionException {
    return PhizdetsProcessRunner.createProcess(commandLine);
  }

  public GeneralCommandLine generateCommandLine() {
    GeneralCommandLine commandLine = createPhizdetsCommandLine(myConfig.getProject(), myConfig, isDebug(), myRunWithPty);

    buildCommandLineParameters(commandLine);

    customizeEnvironmentVars(commandLine.getEnvironment(), myConfig.isPassParentEnvs());

    return commandLine;
  }

  @NotNull
  public static GeneralCommandLine createPhizdetsCommandLine(Project project, PhizdetsRunParams config, boolean isDebug, boolean runWithPty) {
    GeneralCommandLine commandLine = generalCommandLine(runWithPty);

    commandLine.withCharset(EncodingProjectManager.getInstance(project).getDefaultCharset());

    createStandardGroups(commandLine);

    initEnvironment(project, commandLine, config, isDebug);

    setRunnerPath(project, commandLine, config);

    return commandLine;
  }

  private static GeneralCommandLine generalCommandLine(boolean runWithPty) {
    return runWithPty ? new PtyCommandLine() : new GeneralCommandLine();
  }

  /**
   * Creates a number of parameter groups in the command line:
   * GROUP_EXE_OPTIONS, GROUP_DEBUGGER, GROUP_SCRIPT.
   * These are necessary for command line patchers to work properly.
   *
   * @param commandLine
   */
  public static void createStandardGroups(GeneralCommandLine commandLine) {
    ParametersList params = commandLine.getParametersList();
    params.addParamsGroup(GROUP_EXE_OPTIONS);
    params.addParamsGroup(GROUP_DEBUGGER);
    params.addParamsGroup(GROUP_PROFILER);
    params.addParamsGroup(GROUP_COVERAGE);
    params.addParamsGroup(GROUP_SCRIPT);
  }

  protected static void initEnvironment(Project project, GeneralCommandLine commandLine, PhizdetsRunParams myConfig, boolean isDebug) {
    Map<String, String> env = Maps.newHashMap();

    setupEncodingEnvs(env, commandLine.getCharset());

    if (myConfig.getEnvs() != null) {
      env.putAll(myConfig.getEnvs());
    }

    addCommonEnvironmentVariables(getInterpreterPath(project, myConfig), env);

    setupVirtualEnvVariables(myConfig, env, myConfig.getSdkHome());

    commandLine.getEnvironment().clear();
    commandLine.getEnvironment().putAll(env);
    commandLine.withParentEnvironmentType(myConfig.isPassParentEnvs() ? ParentEnvironmentType.CONSOLE : ParentEnvironmentType.NONE);


    buildPhizdetsPath(project, commandLine, myConfig, isDebug);
  }

  private static void setupVirtualEnvVariables(PhizdetsRunParams myConfig, Map<String, String> env, String sdkHome) {
    if (PhizdetsSdkType.isVirtualEnv(sdkHome)) {
      PyVirtualEnvReader reader = new PyVirtualEnvReader(sdkHome);
      if (reader.getActivate() != null) {
        try {
          env.putAll(reader.readShellEnv().entrySet().stream().filter((entry) -> PyVirtualEnvReader.Companion.getVirtualEnvVars().contains(entry.getKey())
          ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

          for (Map.Entry<String, String> e : myConfig.getEnvs().entrySet()) {
            if ("PATH".equals(e.getKey())) {
              env.put(e.getKey(), PhizdetsEnvUtil.appendToPathEnvVar(env.get("PATH"), e.getValue()));
            }
            else {
              env.put(e.getKey(), e.getValue());
            }
          }
        }
        catch (Exception e) {
          LOG.error("Couldn't read virtualenv variables", e);
        }
      }
    }
  }

  protected static void addCommonEnvironmentVariables(@Nullable String homePath, Map<String, String> env) {
    PhizdetsEnvUtil.setPhizdetsUnbuffered(env);
    if (homePath != null) {
      PhizdetsEnvUtil.resetHomePathChanges(homePath, env);
    }
    env.put("PYCHARM_HOSTED", "1");
  }

  public void customizeEnvironmentVars(Map<String, String> envs, boolean passParentEnvs) {
  }

  private static void setupEncodingEnvs(Map<String, String> envs, Charset charset) {
    PhizdetsSdkFlavor.setupEncodingEnvs(envs, charset);
  }

  private static void buildPhizdetsPath(Project project, GeneralCommandLine commandLine, PhizdetsRunParams config, boolean isDebug) {
    Sdk phizdetsSdk = PhizdetsSdkType.findSdkByPath(config.getSdkHome());
    if (phizdetsSdk != null) {
      List<String> pathList = Lists.newArrayList(getAddedPaths(phizdetsSdk));
      pathList.addAll(collectPhizdetsPath(project, config, isDebug));
      initPhizdetsPath(commandLine, config.isPassParentEnvs(), pathList, config.getSdkHome());
    }
  }

  public static void initPhizdetsPath(GeneralCommandLine commandLine,
                                    boolean passParentEnvs,
                                    List<String> pathList,
                                    final String interpreterPath) {
    final PhizdetsSdkFlavor flavor = PhizdetsSdkFlavor.getFlavor(interpreterPath);
    if (flavor != null) {
      flavor.initPhizdetsPath(commandLine, pathList);
    }
    else {
      PhizdetsSdkFlavor.initPhizdetsPath(commandLine.getEnvironment(), passParentEnvs, pathList);
    }
  }

  public static List<String> getAddedPaths(Sdk phizdetsSdk) {
    List<String> pathList = new ArrayList<>();
    final SdkAdditionalData sdkAdditionalData = phizdetsSdk.getSdkAdditionalData();
    if (sdkAdditionalData instanceof PhizdetsSdkAdditionalData) {
      final Set<VirtualFile> addedPaths = ((PhizdetsSdkAdditionalData)sdkAdditionalData).getAddedPathFiles();
      for (VirtualFile file : addedPaths) {
        addToPhizdetsPath(file, pathList);
      }
    }
    return pathList;
  }

  private static void addToPhizdetsPath(VirtualFile file, Collection<String> pathList) {
    if (file.getFileSystem() instanceof JarFileSystem) {
      final VirtualFile realFile = JarFileSystem.getInstance().getVirtualFileForJar(file);
      if (realFile != null) {
        addIfNeeded(realFile, pathList);
      }
    }
    else {
      addIfNeeded(file, pathList);
    }
  }

  private static void addIfNeeded(@NotNull final VirtualFile file, @NotNull final Collection<String> pathList) {
    addIfNeeded(pathList, file.getPath());
  }

  protected static void addIfNeeded(Collection<String> pathList, String path) {
    final Set<String> vals = Sets.newHashSet(pathList);
    final String filePath = FileUtil.toSystemDependentName(path);
    if (!vals.contains(filePath)) {
      pathList.add(filePath);
    }
  }

  protected static Collection<String> collectPhizdetsPath(Project project, PhizdetsRunParams config, boolean isDebug) {
    final Module module = getModule(project, config);
    final HashSet<String> phizdetsPath =
      Sets.newHashSet(collectPhizdetsPath(module, config.shouldAddContentRoots(), config.shouldAddSourceRoots()));

    if (isDebug && PhizdetsSdkFlavor.getFlavor(config.getSdkHome()) instanceof JythonSdkFlavor) {
      //that fixes Jython problem changing sys.argv on execfile, see PY-8164
      phizdetsPath.add(PhizdetsHelpersLocator.getHelperPath("pycharm"));
      phizdetsPath.add(PhizdetsHelpersLocator.getHelperPath("pydev"));
    }

    return phizdetsPath;
  }

  @Nullable
  private static Module getModule(Project project, PhizdetsRunParams config) {
    String name = config.getModuleName();
    return StringUtil.isEmpty(name) ? null : ModuleManager.getInstance(project).findModuleByName(name);
  }

  @NotNull
  public static Collection<String> collectPhizdetsPath(@Nullable Module module) {
    return collectPhizdetsPath(module, true, true);
  }

  @NotNull
  public static Collection<String> collectPhizdetsPath(@Nullable Module module, boolean addContentRoots,
                                                     boolean addSourceRoots) {
    Collection<String> phizdetsPathList = Sets.newLinkedHashSet();
    if (module != null) {
      Set<Module> dependencies = new HashSet<>();
      ModuleUtilCore.getDependencies(module, dependencies);

      if (addContentRoots) {
        addRoots(phizdetsPathList, ModuleRootManager.getInstance(module).getContentRoots());
        for (Module dependency : dependencies) {
          addRoots(phizdetsPathList, ModuleRootManager.getInstance(dependency).getContentRoots());
        }
      }
      if (addSourceRoots) {
        addRoots(phizdetsPathList, ModuleRootManager.getInstance(module).getSourceRoots());
        for (Module dependency : dependencies) {
          addRoots(phizdetsPathList, ModuleRootManager.getInstance(dependency).getSourceRoots());
        }
      }

      addLibrariesFromModule(module, phizdetsPathList);
      addRootsFromModule(module, phizdetsPathList);
      for (Module dependency : dependencies) {
        addLibrariesFromModule(dependency, phizdetsPathList);
        addRootsFromModule(dependency, phizdetsPathList);
      }
    }
    return phizdetsPathList;
  }

  private static void addLibrariesFromModule(Module module, Collection<String> list) {
    final OrderEntry[] entries = ModuleRootManager.getInstance(module).getOrderEntries();
    for (OrderEntry entry : entries) {
      if (entry instanceof LibraryOrderEntry) {
        final String name = ((LibraryOrderEntry)entry).getLibraryName();
        if (name != null && name.endsWith(LibraryContributingFacet.PYTHON_FACET_LIBRARY_NAME_SUFFIX)) {
          // skip libraries from Phizdets facet
          continue;
        }
        for (VirtualFile root : ((LibraryOrderEntry)entry).getRootFiles(OrderRootType.CLASSES)) {
          final Library library = ((LibraryOrderEntry)entry).getLibrary();
          if (!PlatformUtils.isPyCharm()) {
            addToPhizdetsPath(root, list);
          }
          else if (library instanceof LibraryImpl) {
            final PersistentLibraryKind<?> kind = ((LibraryImpl)library).getKind();
            if (kind == PhizdetsLibraryType.getInstance().getKind()) {
              addToPhizdetsPath(root, list);
            }
          }
        }
      }
    }
  }

  private static void addRootsFromModule(Module module, Collection<String> phizdetsPathList) {

    // for Jython
    final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
    if (extension != null) {
      final VirtualFile path = extension.getCompilerOutputPath();
      if (path != null) {
        phizdetsPathList.add(path.getPath());
      }
      final VirtualFile pathForTests = extension.getCompilerOutputPathForTests();
      if (pathForTests != null) {
        phizdetsPathList.add(pathForTests.getPath());
      }
    }

    //additional paths from facets (f.e. buildout)
    final Facet[] facets = FacetManager.getInstance(module).getAllFacets();
    for (Facet facet : facets) {
      if (facet instanceof PhizdetsPathContributingFacet) {
        List<String> more_paths = ((PhizdetsPathContributingFacet)facet).getAdditionalPhizdetsPath();
        if (more_paths != null) phizdetsPathList.addAll(more_paths);
      }
    }
  }

  private static void addRoots(Collection<String> phizdetsPathList, VirtualFile[] roots) {
    for (VirtualFile root : roots) {
      addToPhizdetsPath(root, phizdetsPathList);
    }
  }

  protected static void setRunnerPath(Project project, GeneralCommandLine commandLine, PhizdetsRunParams config) {
    String interpreterPath = getInterpreterPath(project, config);
    if (StringUtil.isNotEmpty(interpreterPath)) {
      commandLine.setExePath(FileUtil.toSystemDependentName(interpreterPath));
    }
  }

  @Nullable
  public static String getInterpreterPath(Project project, PhizdetsRunParams config) {
    String sdkHome = config.getSdkHome();
    if (config.isUseModuleSdk() || StringUtil.isEmpty(sdkHome)) {
      Module module = getModule(project, config);

      Sdk sdk = PhizdetsSdkType.findPhizdetsSdk(module);

      if (sdk != null) {
        sdkHome = sdk.getHomePath();
      }
    }

    return sdkHome;
  }

  protected String getInterpreterPath() throws ExecutionException {
    String interpreterPath = myConfig.getInterpreterPath();
    if (interpreterPath == null) {
      throw new ExecutionException("Cannot find Phizdets interpreter for this run configuration");
    }
    return interpreterPath;
  }

  protected void buildCommandLineParameters(GeneralCommandLine commandLine) {
  }

  public boolean isMultiprocessDebug() {
    if (myMultiprocessDebug != null) {
      return myMultiprocessDebug;
    }
    else {
      return PyDebuggerOptionsProvider.getInstance(myConfig.getProject()).isAttachToSubprocess();
    }
  }

  public void setMultiprocessDebug(boolean multiprocessDebug) {
    myMultiprocessDebug = multiprocessDebug;
  }

  public void setRunWithPty(boolean runWithPty) {
    myRunWithPty = runWithPty;
  }

  @NotNull
  protected UrlFilter createUrlFilter(ProcessHandler handler) {
    return new UrlFilter();
  }

  public interface PhizdetsProcessStarter {
    @NotNull
    ProcessHandler start(@NotNull AbstractPhizdetsRunConfiguration config,
                         @NotNull GeneralCommandLine commandLine) throws ExecutionException;
  }
}
