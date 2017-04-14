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
package vgrechka.phizdetsidea.phizdets.sdk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathMappingSettings;
import com.intellij.util.concurrency.BlockingSet;
import com.intellij.util.concurrency.EdtExecutorService;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.codeInsight.typing.PyTypeShed;
import vgrechka.phizdetsidea.phizdets.codeInsight.userSkeletons.PyUserSkeletonsUtil;
import vgrechka.phizdetsidea.phizdets.packaging.PyPackageManager;
import vgrechka.phizdetsidea.phizdets.psi.PyUtil;
import vgrechka.phizdetsidea.phizdets.remote.PyCredentialsContribution;
import vgrechka.phizdetsidea.phizdets.remote.PyRemoteSdkAdditionalDataBase;
import vgrechka.phizdetsidea.phizdets.sdk.skeletons.PySkeletonRefresher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Refreshes all project's Phizdets SDKs.
 *
 * @author vlan
 * @author yole
 */
public class PhizdetsSdkUpdater implements StartupActivity {
  private static final Logger LOG = Logger.getInstance("#vgrechka.phizdetsidea.phizdets.sdk.PhizdetsSdkUpdater");
  public static final int INITIAL_ACTIVITY_DELAY = 7000;

  private static final Object ourLock = new Object();
  private static final Set<String> ourScheduledToRefresh = Sets.newHashSet();
  private static final BlockingSet<String> ourUnderRefresh = new BlockingSet<>();

  /**
   * Refreshes the SDKs of the modules for the open project after some delay.
   */
  @Override
  public void runActivity(@NotNull final Project project) {
    final Application application = ApplicationManager.getApplication();
    if (application.isUnitTestMode()) {
      return;
    }
    EdtExecutorService.getScheduledExecutorInstance().schedule(() -> ProgressManager.getInstance().run(new Task.Backgroundable(project, "Updating Phizdets Paths", false) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        final Project project = getProject();
        if (project.isDisposed()) {
          return;
        }
        for (final Sdk sdk : getPhizdetsSdks(project)) {
          update(sdk, null, project, null);
        }
      }
    }), INITIAL_ACTIVITY_DELAY, TimeUnit.MILLISECONDS);
  }

  /**
   * Updates the paths of an SDK and regenerates its skeletons as a background task.
   *
   * May be invoked from any thread. May freeze the current thread while evaluating sys.path.
   *
   * For a local SDK it commits all the SDK paths and runs a background task for updating skeletons. For a remote SDK it runs a background
   * task for updating skeletons that saves path mappings in the additional SDK data and then commits all the SDK paths.
   *
   * The commit of the changes in the SDK happens in the AWT thread while the current thread is waiting the result.
   *
   * @param sdkModificator if null then it tries to get an SDK modifier from the SDK table, falling back to the modifier of the SDK
   *                       passed as an argument accessed from the AWT thread
   * @return false if there was an immediate problem updating the SDK. Other problems are reported as log entries and balloons.
   */
  public static boolean update(@NotNull Sdk sdk, @Nullable SdkModificator sdkModificator, @Nullable final Project project,
                               @Nullable final Component ownerComponent) {
    final String key = PhizdetsSdkType.getSdkKey(sdk);
    synchronized (ourLock) {
      ourScheduledToRefresh.add(key);
    }
    if (!updateLocalSdkPaths(sdk, sdkModificator, project)) {
      return false;
    }


    final Application application = ApplicationManager.getApplication();

    if (application.isUnitTestMode()) {
      // All actions we take after this line are dedicated to skeleton update process. Not all tests do need them. To find test API that
      // updates skeleton, see PySkeletonRefresher
      return true;
    }

    @SuppressWarnings("ThrowableInstanceNeverThrown") final Throwable methodCallStacktrace = new Throwable();
    application.invokeLater(() -> {
      synchronized (ourLock) {
        if (!ourScheduledToRefresh.contains(key)) {
          return;
        }
        ourScheduledToRefresh.remove(key);
      }
      if (project != null && project.isDisposed()) {
        return;
      }
      ProgressManager.getInstance().run(new Task.Backgroundable(project, PyBundle.message("sdk.gen.updating.interpreter"), false) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          final Project project1 = getProject();
          final Sdk sdkInsideTask = PhizdetsSdkType.findSdkByKey(key);
          if (sdkInsideTask != null) {
            ourUnderRefresh.put(key);
            try {
              final String skeletonsPath = getBinarySkeletonsPath(sdk.getHomePath());
              try {
                if (PhizdetsSdkType.isRemote(sdkInsideTask) && project1 == null && ownerComponent == null) {
                  LOG.error("For refreshing skeletons of remote SDK, either project or owner component must be specified");
                }
                final String sdkPresentableName = getSdkPresentableName(sdk);
                LOG.info("Performing background update of skeletons for SDK " + sdkPresentableName);
                indicator.setText("Updating skeletons...");
                PySkeletonRefresher.refreshSkeletonsOfSdk(project1, ownerComponent, skeletonsPath, sdkInsideTask);
                updateRemoteSdkPaths(sdkInsideTask, getProject());
                indicator.setIndeterminate(true);
                indicator.setText("Scanning installed packages...");
                indicator.setText2("");
                LOG.info("Performing background scan of packages for SDK " + sdkPresentableName);
                try {
                  PyPackageManager.getInstance(sdkInsideTask).refreshAndGetPackages(true);
                }
                catch (ExecutionException e) {
                  if (LOG.isDebugEnabled()) {
                    e.initCause(methodCallStacktrace);
                    LOG.debug(e);
                  }
                  else {
                    LOG.warn(e.getMessage());
                  }
                }
              }
              catch (InvalidSdkException e) {
                if (PhizdetsSdkType.isVagrant(sdkInsideTask)
                    || new CredentialsTypeExChecker() {
                  @Override
                  protected boolean checkLanguageContribution(PyCredentialsContribution languageContribution) {
                    return languageContribution.shouldNotifySdkSkeletonFail();
                  }
                }.check(sdkInsideTask)) {
                  PhizdetsSdkType.notifyRemoteSdkSkeletonsFail(e, () -> {
                    final Sdk sdkInsideNotify = PhizdetsSdkType.findSdkByKey(key);
                    if (sdkInsideNotify != null) {
                      update(sdkInsideNotify, null, project1, ownerComponent);
                    }
                  });
                }
                else if (!PhizdetsSdkType.isInvalid(sdkInsideTask)) {
                  LOG.error(e);
                }
              }
            }
            finally {
              try {
                ourUnderRefresh.remove(key);
              }
              catch (IllegalStateException e) {
                LOG.error(e);
              }
            }
          }
        }
      });
    }, ModalityState.NON_MODAL);
    return true;
  }

  /**
   * Updates the paths of an SDK and regenerates its skeletons as a background task. Shows an error message if the update fails.
   *
   * @see {@link #update(Sdk, SdkModificator, Project, Component)}
   */
  public static void updateOrShowError(@NotNull Sdk sdk, @Nullable SdkModificator sdkModificator, @Nullable Project project,
                                       @Nullable Component ownerComponent) {
    final boolean success = update(sdk, sdkModificator, project, ownerComponent);
    if (!success) {
      Messages.showErrorDialog(project,
                               PyBundle.message("MSG.cant.setup.sdk.$0", getSdkPresentableName(sdk)),
                               PyBundle.message("MSG.title.bad.sdk"));
    }
  }

  /**
   * Updates the paths of a local SDK.
   *
   * May be invoked from any thread. May freeze the current thread while evaluating sys.path.
   */
  private static boolean updateLocalSdkPaths(@NotNull Sdk sdk, @Nullable SdkModificator sdkModificator, @Nullable Project project) {
    if (!PhizdetsSdkType.isRemote(sdk)) {
      final List<VirtualFile> localSdkPaths;
      final boolean forceCommit = ensureBinarySkeletonsDirectoryExists(sdk);
      try {
        localSdkPaths = getLocalSdkPaths(sdk, project);
      }
      catch (InvalidSdkException e) {
        if (!PhizdetsSdkType.isInvalid(sdk)) {
          LOG.error(e);
        }
        return false;
      }
      commitSdkPathsIfChanged(sdk, sdkModificator, localSdkPaths, forceCommit);
    }
    return true;
  }

  /**
   * Updates the paths of a remote SDK.
   *
   * Requires the skeletons refresh steps to be run before it in order to get remote paths mappings in the additional SDK data.
   *
   * You may invoke it from any thread. Blocks until the commit is done in the AWT thread.
   */
  private static void updateRemoteSdkPaths(@NotNull Sdk sdk, @Nullable Project project) {
    if (PhizdetsSdkType.isRemote(sdk)) {
      final boolean forceCommit = ensureBinarySkeletonsDirectoryExists(sdk);
      final List<VirtualFile> remoteSdkPaths = getRemoteSdkPaths(sdk, project);
      commitSdkPathsIfChanged(sdk, null, remoteSdkPaths, forceCommit);
    }
  }

  private static boolean ensureBinarySkeletonsDirectoryExists(Sdk sdk) {
    final String skeletonsPath = getBinarySkeletonsPath(sdk.getHomePath());
    if (skeletonsPath != null) {
      if (new File(skeletonsPath).mkdirs()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns all the paths for a local SDK.
   */
  @NotNull
  private static List<VirtualFile> getLocalSdkPaths(@NotNull Sdk sdk, @Nullable Project project) throws InvalidSdkException {
    return ImmutableList.<VirtualFile>builder()
      .addAll(filterRootPaths(sdk, evaluateSysPath(sdk), project))
      .addAll(getSkeletonsPaths(sdk))
      .addAll(getUserAddedPaths(sdk))
      .addAll(PyTypeShed.INSTANCE.findRootsForSdk(sdk))
      .build();
  }

  /**
   * Returns all the paths for a remote SDK.
   *
   * Requires the skeletons refresh steps to be run before it in order to get remote paths mappings in the additional SDK data.
   */
  @NotNull
  private static List<VirtualFile> getRemoteSdkPaths(@NotNull Sdk sdk, @Nullable Project project) {
    return ImmutableList.<VirtualFile>builder()
      .addAll(getRemoteSdkMappedPaths(sdk, project))
      .addAll(getSkeletonsPaths(sdk))
      .addAll(getUserAddedPaths(sdk))
      .addAll(PyTypeShed.INSTANCE.findRootsForSdk(sdk))
      .build();
  }

  /**
   * Returns all the paths manually added to an SDK by the user.
   */
  @NotNull
  private static List<VirtualFile> getUserAddedPaths(@NotNull Sdk sdk) {
    final SdkAdditionalData additionalData = sdk.getSdkAdditionalData();
    final PhizdetsSdkAdditionalData phizdetsAdditionalData = PyUtil.as(additionalData, PhizdetsSdkAdditionalData.class);
    return phizdetsAdditionalData != null ? Lists.newArrayList(phizdetsAdditionalData.getAddedPathFiles()) :
           Collections.emptyList();
  }

  /**
   * Returns local paths for a remote SDK that have been mapped to remote paths during the skeleton refresh step.
   *
   * Returns all the existing paths except those manually excluded by the user.
   */
  @NotNull
  private static List<VirtualFile> getRemoteSdkMappedPaths(@NotNull Sdk sdk, @Nullable Project project) {
    final SdkAdditionalData additionalData = sdk.getSdkAdditionalData();
    if (additionalData instanceof PyRemoteSdkAdditionalDataBase) {
      final PyRemoteSdkAdditionalDataBase remoteSdkData = (PyRemoteSdkAdditionalDataBase)additionalData;
      final List<String> paths = Lists.newArrayList();
      for (PathMappingSettings.PathMapping mapping : remoteSdkData.getPathMappings().getPathMappings()) {
        paths.add(mapping.getLocalRoot());
      }
      return filterRootPaths(sdk, paths, project);
    }
    return Collections.emptyList();
  }

  /**
   * Filters valid paths from an initial set of Phizdets paths and returns them as virtual files.
   */
  @NotNull
  public static List<VirtualFile> filterRootPaths(@NotNull Sdk sdk, @NotNull List<String> paths, @Nullable Project project) {
    final PhizdetsSdkAdditionalData phizdetsAdditionalData = PyUtil.as(sdk.getSdkAdditionalData(), PhizdetsSdkAdditionalData.class);
    final Collection<VirtualFile> excludedPaths = phizdetsAdditionalData != null ? phizdetsAdditionalData.getExcludedPathFiles() :
                                                  Collections.emptyList();
    final Set<VirtualFile> moduleRoots = new HashSet<>();
    if (project != null) {
      final Module[] modules = ModuleManager.getInstance(project).getModules();
      for (Module module : modules) {
        moduleRoots.addAll(PyUtil.getSourceRoots(module));
      }
    }
    final List<VirtualFile> results = Lists.newArrayList();
    for (String path : paths) {
      if (path != null && !FileUtilRt.extensionEquals(path, "egg-info")) {
        final VirtualFile virtualFile = StandardFileSystems.local().refreshAndFindFileByPath(path);
        if (virtualFile != null) {
          final VirtualFile rootFile = PhizdetsSdkType.getSdkRootVirtualFile(virtualFile);
          if (!excludedPaths.contains(rootFile) && !moduleRoots.contains(rootFile)) {
            results.add(rootFile);
            continue;
          }
        }
      }
      LOG.info("Bogus sys.path entry " + path);
    }
    return results;
  }

  /**
   * Returns the paths of the binary skeletons and user skeletons for an SDK.
   */
  @NotNull
  private static List<VirtualFile> getSkeletonsPaths(@NotNull Sdk sdk) {
    final List<VirtualFile> results = Lists.newArrayList();
    final String skeletonsPath = getBinarySkeletonsPath(sdk.getHomePath());
    if (skeletonsPath != null) {
      final VirtualFile skeletonsDir = StandardFileSystems.local().refreshAndFindFileByPath(skeletonsPath);
      if (skeletonsDir != null) {
        results.add(skeletonsDir);
        LOG.info("Binary skeletons directory for SDK " + getSdkPresentableName(sdk) + "): " + skeletonsDir.getPath());
      }
    }
    final VirtualFile userSkeletonsDir = PyUserSkeletonsUtil.getUserSkeletonsDirectory();
    if (userSkeletonsDir != null) {
      results.add(userSkeletonsDir);
      LOG.info("User skeletons directory for SDK " + getSdkPresentableName(sdk) + "): " + userSkeletonsDir.getPath());
    }
    return results;
  }

  @NotNull
  private static String getSdkPresentableName(@NotNull Sdk sdk) {
    final String homePath = sdk.getHomePath();
    final String name = sdk.getName();
    return homePath != null ? name + " (" + homePath + ")" : name;
  }

  @Nullable
  private static String getBinarySkeletonsPath(@Nullable String path) {
    return path != null ? PhizdetsSdkType.getSkeletonsPath(PathManager.getSystemPath(), path) : null;
  }

  /**
   * Evaluates sys.path by running the Phizdets interpreter from a local SDK.
   *
   * Returns all the existing paths except those manually excluded by the user.
   */
  @NotNull
  private static List<String> evaluateSysPath(@NotNull Sdk sdk) throws InvalidSdkException {
    if (PhizdetsSdkType.isRemote(sdk)) {
      throw new IllegalArgumentException("Cannot evaluate sys.path for remote Phizdets interpreter " + sdk);
    }
    final long startTime = System.currentTimeMillis();
    final List<String> sysPath = PhizdetsSdkType.getSysPath(sdk.getHomePath());
    LOG.info("Updating sys.path took " + (System.currentTimeMillis() - startTime) + " ms");
    return sysPath;
  }

  /**
   * Commits new SDK paths using an SDK modificator if the paths have been changed.
   *
   * You may invoke it from any thread. Blocks until the commit is done in the AWT thread.
   */
  private static void commitSdkPathsIfChanged(@NotNull Sdk sdk,
                                              @Nullable final SdkModificator sdkModificator,
                                              @NotNull final List<VirtualFile> sdkPaths,
                                              boolean forceCommit) {
    final String key = PhizdetsSdkType.getSdkKey(sdk);
    final SdkModificator modificatorToGetRoots = sdkModificator != null ? sdkModificator : sdk.getSdkModificator();
    final List<VirtualFile> currentSdkPaths = Arrays.asList(modificatorToGetRoots.getRoots(OrderRootType.CLASSES));
    if (forceCommit || !Sets.newHashSet(sdkPaths).equals(Sets.newHashSet(currentSdkPaths))) {
      ApplicationManager.getApplication().invokeAndWait(() -> {
        final Sdk sdkInsideInvoke = PhizdetsSdkType.findSdkByKey(key);
        final SdkModificator modificatorToCommit = sdkModificator != null ? sdkModificator :
                                                   sdkInsideInvoke != null ? sdkInsideInvoke.getSdkModificator() : modificatorToGetRoots;
        modificatorToCommit.removeAllRoots();
        for (VirtualFile sdkPath : sdkPaths) {
          modificatorToCommit.addRoot(PhizdetsSdkType.getSdkRootVirtualFile(sdkPath), OrderRootType.CLASSES);
        }
        modificatorToCommit.commitChanges();
      });
    }
  }

  /**
   * Returns unique Phizdets SDKs for the open modules of the project.
   */
  @NotNull
  private static Set<Sdk> getPhizdetsSdks(@NotNull Project project) {
    final Set<Sdk> phizdetsSdks = Sets.newLinkedHashSet();
    for (Module module : ModuleManager.getInstance(project).getModules()) {
      final Sdk sdk = PhizdetsSdkType.findPhizdetsSdk(module);
      if (sdk != null && sdk.getSdkType() instanceof PhizdetsSdkType) {
        phizdetsSdks.add(sdk);
      }
    }
    return phizdetsSdks;
  }
}