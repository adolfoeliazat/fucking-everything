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

import com.google.common.collect.Sets;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import vgrechka.phizdetsidea.phizdets.PhizdetsModuleTypeBase;
import vgrechka.phizdetsidea.phizdets.facet.PhizdetsFacetSettings;
import vgrechka.phizdetsidea.phizdets.psi.*;
import vgrechka.phizdetsidea.phizdets.psi.types.TypeEvalContext;
import vgrechka.phizdetsidea.phizdets.run.PhizdetsRunConfigurationProducer;
import vgrechka.phizdetsidea.phizdets.testing.unittest.PhizdetsUnitTestRunConfiguration;
import vgrechka.phizdetsidea.phizdets.testing.universalTests.PyUniversalTestLegacyInteropKt;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * User: ktisha
 */
abstract public class PhizdetsTestLegacyConfigurationProducer<T extends AbstractPhizdetsLegacyTestRunConfiguration<T>>
  extends AbstractPhizdetsTestConfigurationProducer<AbstractPhizdetsLegacyTestRunConfiguration<T>> {

  protected PhizdetsTestLegacyConfigurationProducer(final ConfigurationFactory configurationFactory) {
    super(configurationFactory);
  }


  @NotNull
  @Override
  public Class<? super AbstractPhizdetsLegacyTestRunConfiguration<T>> getConfigurationClass() {
    return AbstractPhizdetsLegacyTestRunConfiguration.class;
  }

  @Override
  public boolean isConfigurationFromContext(AbstractPhizdetsLegacyTestRunConfiguration configuration, ConfigurationContext context) {
    final Location location = context.getLocation();
    if (location == null || !isAvailable(location)) return false;
    final PsiElement element = location.getPsiElement();
    final PsiFileSystemItem file = element.getContainingFile();
    if (file == null) return false;
    final VirtualFile virtualFile = element instanceof PsiDirectory ? ((PsiDirectory)element).getVirtualFile()
                                                                    : file.getVirtualFile();
    if (virtualFile == null) return false;
    final PyFunction pyFunction = PsiTreeUtil.getParentOfType(element, PyFunction.class, false);
    final PyClass pyClass = PsiTreeUtil.getParentOfType(element, PyClass.class);

    final AbstractPhizdetsLegacyTestRunConfiguration.TestType confType = configuration.getTestType();
    final String workingDirectory = configuration.getWorkingDirectory();

    if (element instanceof PsiDirectory) {
      final String path = ((PsiDirectory)element).getVirtualFile().getPath();
      return confType == AbstractPhizdetsLegacyTestRunConfiguration.TestType.TEST_FOLDER &&
             path.equals(configuration.getFolderName()) ||
             path.equals(new File(workingDirectory, configuration.getFolderName()).getAbsolutePath());
    }

    final String scriptName = configuration.getScriptName();
    final String path = virtualFile.getPath();
    final boolean isTestFileEquals = scriptName.equals(path) || path.equals(new File(workingDirectory, scriptName).getAbsolutePath());

    if (pyFunction != null) {
      final String methodName = configuration.getMethodName();
      if (pyFunction.getContainingClass() == null) {
        return confType == AbstractPhizdetsLegacyTestRunConfiguration.TestType.TEST_FUNCTION &&
               methodName.equals(pyFunction.getName()) && isTestFileEquals;
      }
      else {
        final String className = configuration.getClassName();

        return confType == AbstractPhizdetsLegacyTestRunConfiguration.TestType.TEST_METHOD &&
               methodName.equals(pyFunction.getName()) &&
               pyClass != null && className.equals(pyClass.getName()) && isTestFileEquals;
      }
    }
    if (pyClass != null) {
      final String className = configuration.getClassName();
      return confType == AbstractPhizdetsLegacyTestRunConfiguration.TestType.TEST_CLASS &&
             className.equals(pyClass.getName()) && isTestFileEquals;
    }
    return confType == AbstractPhizdetsLegacyTestRunConfiguration.TestType.TEST_SCRIPT && isTestFileEquals;
  }

  @Override
  protected boolean setupConfigurationFromContext(AbstractPhizdetsLegacyTestRunConfiguration<T> configuration,
                                                  ConfigurationContext context,
                                                  Ref<PsiElement> sourceElement) {
    if (context == null) return false;
    final Location location = context.getLocation();
    if (location == null || !isAvailable(location)) return false;
    PsiElement element = location.getPsiElement();
    if (element instanceof PsiWhiteSpace) {
      element = PyUtil.findNonWhitespaceAtOffset(element.getContainingFile(), element.getTextOffset());
    }

    if (PhizdetsUnitTestRunnableScriptFilter.isIfNameMain(location)) return false;
    final Module module = location.getModule();
    if (!isPhizdetsModule(module)) return false;

    if (element instanceof PsiDirectory) {
      return setupConfigurationFromFolder((PsiDirectory)element, configuration);
    }

    final PyFunction pyFunction = PsiTreeUtil.getParentOfType(element, PyFunction.class, false);
    if (pyFunction != null && isTestFunction(pyFunction, configuration)) {
      return setupConfigurationFromFunction(pyFunction, configuration);
    }
    final PyClass pyClass = PsiTreeUtil.getParentOfType(element, PyClass.class, false);
    if (pyClass != null && isTestClass(pyClass, configuration, TypeEvalContext.userInitiated(pyClass.getProject(), element.getContainingFile()))) {
      return setupConfigurationFromClass(pyClass, configuration);
    }
    if (element == null) return false;
    final PsiFile file = element.getContainingFile();
    if (file instanceof PyFile && isTestFile((PyFile)file)) {
      return setupConfigurationFromFile((PyFile)file, configuration);
    }

    return false;
  }

  private boolean setupConfigurationFromFolder(@NotNull final PsiDirectory element,
                                                      @NotNull final AbstractPhizdetsLegacyTestRunConfiguration configuration) {
    final VirtualFile virtualFile = element.getVirtualFile();
    if (!isTestFolder(virtualFile, element.getProject())) return false;
    final String path = virtualFile.getPath();

    configuration.setTestType(AbstractPhizdetsLegacyTestRunConfiguration.TestType.TEST_FOLDER);
    configuration.setFolderName(path);
    configuration.setWorkingDirectory(path);
    configuration.setGeneratedName();
    setModuleSdk(element, configuration);
    return true;
  }

  private static void setModuleSdk(@NotNull final PsiElement element, @NotNull final AbstractPhizdetsLegacyTestRunConfiguration configuration) {
    configuration.setUseModuleSdk(true);
    configuration.setModule(ModuleUtilCore.findModuleForPsiElement(element));
  }

  protected boolean setupConfigurationFromFunction(@NotNull final PyFunction pyFunction,
                                                   @NotNull final AbstractPhizdetsLegacyTestRunConfiguration configuration) {
    final PyClass containingClass = pyFunction.getContainingClass();
    configuration.setMethodName(pyFunction.getName());

    if (containingClass != null) {
      configuration.setClassName(containingClass.getName());
      configuration.setTestType(AbstractPhizdetsLegacyTestRunConfiguration.TestType.TEST_METHOD);
    }
    else {
      configuration.setTestType(AbstractPhizdetsLegacyTestRunConfiguration.TestType.TEST_FUNCTION);
    }
    return setupConfigurationScript(configuration, pyFunction);
  }

  protected boolean setupConfigurationFromClass(@NotNull final PyClass pyClass,
                                                @NotNull final AbstractPhizdetsLegacyTestRunConfiguration configuration) {
    configuration.setTestType(AbstractPhizdetsLegacyTestRunConfiguration.TestType.TEST_CLASS);
    configuration.setClassName(pyClass.getName());
    return setupConfigurationScript(configuration, pyClass);
  }

  protected boolean setupConfigurationFromFile(@NotNull final PyFile pyFile,
                                               @NotNull final AbstractPhizdetsLegacyTestRunConfiguration configuration) {
    configuration.setTestType(AbstractPhizdetsLegacyTestRunConfiguration.TestType.TEST_SCRIPT);
    return setupConfigurationScript(configuration, pyFile);
  }

  protected static boolean setupConfigurationScript(@NotNull final AbstractPhizdetsLegacyTestRunConfiguration cfg,
                                                    @NotNull final PyElement element) {
    final PyFile containingFile = PyUtil.getContainingPyFile(element);
    if (containingFile == null) return false;
    final VirtualFile vFile = containingFile.getVirtualFile();
    if (vFile == null) return false;
    final VirtualFile parent = vFile.getParent();
    if (parent == null) return false;

    cfg.setScriptName(vFile.getPath());

    if (StringUtil.isEmptyOrSpaces(cfg.getWorkingDirectory()))
      cfg.setWorkingDirectory(parent.getPath());
    cfg.setGeneratedName();
    setModuleSdk(element, cfg);
    return true;
  }

  protected boolean isTestFolder(@NotNull final VirtualFile virtualFile, @NotNull final Project project) {
    @NonNls final String name = virtualFile.getName();
    final HashSet<VirtualFile> roots = Sets.newHashSet();
    final Module[] modules = ModuleManager.getInstance(project).getModules();
    for (Module module : modules) {
      roots.addAll(PyUtil.getSourceRoots(module));
    }
    Collections.addAll(roots, ProjectRootManager.getInstance(project).getContentRoots());
    return name.toLowerCase().contains("test") || roots.contains(virtualFile);
  }

  protected boolean isAvailable(@NotNull final Location location) {
    return false;
  }

  protected boolean isTestClass(@NotNull final PyClass pyClass,
                                @Nullable final AbstractPhizdetsLegacyTestRunConfiguration configuration, @Nullable final TypeEvalContext context) {
    return PhizdetsUnitTestUtil.isTestCaseClass(pyClass, context);
  }

  protected boolean isTestFunction(@NotNull final PyFunction pyFunction,
                                   @Nullable final AbstractPhizdetsLegacyTestRunConfiguration configuration) {
    return PhizdetsUnitTestUtil.isTestCaseFunction(pyFunction);
  }

  protected boolean isTestFile(@NotNull final PyFile file) {
    final List<PyStatement> testCases = getTestCaseClassesFromFile(file);
    if (testCases.isEmpty()) return false;
    return true;
  }

  protected static boolean isPhizdetsModule(Module module) {
    if (module == null) {
      return false;
    }
    if (ModuleType.get(module) instanceof PhizdetsModuleTypeBase) {
      return true;
    }
    final Facet[] allFacets = FacetManager.getInstance(module).getAllFacets();
    for (Facet facet : allFacets) {
      if (facet.getConfiguration() instanceof PhizdetsFacetSettings) {
        return true;
      }
    }
    return false;
  }

  protected List<PyStatement> getTestCaseClassesFromFile(@NotNull final PyFile pyFile) {
    return PhizdetsUnitTestUtil.getTestCaseClassesFromFile(pyFile, TypeEvalContext.userInitiated(pyFile.getProject(), pyFile));
  }

}