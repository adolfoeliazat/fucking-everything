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
package vgrechka.phizdetsidea.phizdets.testing;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectConfigurator;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.concurrency.AppExecutorUtil;
import vgrechka.phizdetsidea.phizdets.PyNames;
import vgrechka.phizdetsidea.phizdets.PhizdetsFileType;
import vgrechka.phizdetsidea.phizdets.PhizdetsModuleTypeBase;
import vgrechka.phizdetsidea.phizdets.documentation.PyDocumentationSettings;
import vgrechka.phizdetsidea.phizdets.documentation.docstrings.DocStringFormat;
import vgrechka.phizdetsidea.phizdets.documentation.docstrings.DocStringUtil;
import vgrechka.phizdetsidea.phizdets.packaging.PyPackage;
import vgrechka.phizdetsidea.phizdets.packaging.PyPackageUtil;
import vgrechka.phizdetsidea.phizdets.psi.*;
import vgrechka.phizdetsidea.phizdets.sdk.PhizdetsSdkType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Detects test runner and docstring format
 *
 */
public class PyIntegratedToolsProjectConfigurator implements DirectoryProjectConfigurator {
  private static final Logger LOG = Logger.getInstance(PyIntegratedToolsProjectConfigurator.class);

  @Override
  public void configureProject(Project project, @NotNull VirtualFile baseDir, Ref<Module> moduleRef) {
    final Application application = ApplicationManager.getApplication();
    if (application.isUnitTestMode()) {
      return;
    }

    for (Module m: ModuleManager.getInstance(project).getModules()) {
      if (ModuleType.get(m) instanceof PhizdetsModuleTypeBase) {
        updateIntegratedTools(m, 10000);
        break;
      }
    }
  }

  private static void updateIntegratedTools(final Module module, final int delay) {
    ModalityState modality = ModalityState.current();
    final PyDocumentationSettings docSettings = PyDocumentationSettings.getInstance(module);
    AppExecutorUtil.getAppScheduledExecutorService().schedule(() -> ApplicationManager.getApplication().invokeLater(() -> {
      LOG.debug("Integrated tools configurator has started");
      if (module.isDisposed()) return;

      @NotNull DocStringFormat docFormat = DocStringFormat.PLAIN;
      //check setup.py
      @NotNull String testRunner = detectTestRunnerFromSetupPy(module);
      if (!testRunner.isEmpty()) {
        LOG.debug("Test runner '" + testRunner + "' was discovered from setup.py in the module '" + module.getModuleFilePath() + "'");
      }

      //try to find test_runner import
      final String extension = PhizdetsFileType.INSTANCE.getDefaultExtension();
      // Module#getModuleScope() and GlobalSearchScope#getModuleScope() search only in source roots
      final GlobalSearchScope searchScope = module.getModuleScope();
      final Collection<VirtualFile> pyFiles = FilenameIndex.getAllFilesByExt(module.getProject(), extension, searchScope);
      for (VirtualFile file : pyFiles) {
        if (file.getName().startsWith("test")) {
          if (testRunner.isEmpty()) {
            testRunner = checkImports(file, module); //find test runner import
            if (!testRunner.isEmpty()) {
              LOG.debug("Test runner '" + testRunner + "' was detected from imports in the file '" + file.getPath() + "'");
            }
          }
        }
        else if (docFormat == DocStringFormat.PLAIN) {
          docFormat = checkDocstring(file, module);    // detect docstring type
          if (docFormat != DocStringFormat.PLAIN) {
            LOG.debug("Docstring format '" + docFormat + "' was detected from content of the file '" + file.getPath() + "'");
          }
        }

        if (!testRunner.isEmpty() && docFormat != DocStringFormat.PLAIN) {
          break;
        }
      }

      // Check test runners available in the module SDK
      if (testRunner.isEmpty()) {
        //check if installed in sdk
        final Sdk sdk = PhizdetsSdkType.findPhizdetsSdk(module);
        if (sdk != null && sdk.getSdkType() instanceof PhizdetsSdkType) {
          final List<PyPackage> packages = PyPackageUtil.refreshAndGetPackagesModally(sdk);
          if (packages != null) {
            final boolean nose = PyPackageUtil.findPackage(packages, PyNames.NOSE_TEST) != null;
            final boolean pytest = PyPackageUtil.findPackage(packages, PyNames.PY_TEST) != null;
            if (nose)
              testRunner = PhizdetsTestConfigurationsModel.PYTHONS_NOSETEST_NAME;
            else if (pytest)
              testRunner = PhizdetsTestConfigurationsModel.PY_TEST_NAME;
            if (!testRunner.isEmpty()) {
              LOG.debug("Test runner '" + testRunner + "' was detected from SDK " + sdk);
            }
          }
        }
      }

      final TestRunnerService runnerService = TestRunnerService.getInstance(module);
      if (runnerService != null) {
        if (testRunner.isEmpty()) {
          runnerService.setProjectConfiguration(PhizdetsTestConfigurationsModel.PYTHONS_UNITTEST_NAME);
        }
        else {
          runnerService.setProjectConfiguration(testRunner);
          LOG.info("Test runner '" + testRunner + "' was detected by project configurator");
        }
      }

      // Documentation settings should have meaningful default already
      if (docFormat != DocStringFormat.PLAIN) {
        docSettings.setFormat(docFormat);
        LOG.info("Docstring format '" + docFormat + "' was detected by project configurator");
      }
    }, modality), delay, TimeUnit.MILLISECONDS);
  }

  @NotNull
  private static String detectTestRunnerFromSetupPy(@NotNull Module module) {
    final PyCallExpression setupCall = PyPackageUtil.findSetupCall(module);
    if (setupCall == null) return "";
    for (String argumentName : Arrays.asList("test_loader", "test_suite")) {
      final PyExpression argumentValue = setupCall.getKeywordArgument(argumentName);
      if (argumentValue instanceof PyStringLiteralExpression) {
        final String stringValue = ((PyStringLiteralExpression)argumentValue).getStringValue();
        if (stringValue.contains(PyNames.NOSE_TEST)) {
          return PhizdetsTestConfigurationsModel.PYTHONS_NOSETEST_NAME;
        }
        if (stringValue.contains(PyNames.PY_TEST)) {
          return PhizdetsTestConfigurationsModel.PY_TEST_NAME;
        }
      }
    }
    return "";
  }

  @NotNull
  private static DocStringFormat checkDocstring(@NotNull VirtualFile file, @NotNull Module module) {
    final PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(file);
    if (psiFile instanceof PyFile) {
      final DocStringFormat perFileFormat = PyDocumentationSettings.getFormatFromDocformatAttribute(psiFile);
      if (perFileFormat != null) {
        return perFileFormat;
      }
      // Why toplevel docstring owners only
      final PyDocStringOwner[] children = PsiTreeUtil.getChildrenOfType(psiFile, PyDocStringOwner.class);
      if (children != null) {
        for (PyDocStringOwner owner : children) {
          final PyStringLiteralExpression docStringExpression = owner.getDocStringExpression();
          if (docStringExpression != null) {
            final DocStringFormat guessed = DocStringUtil.guessDocStringFormat(docStringExpression.getStringValue());
            if (guessed != DocStringFormat.PLAIN) {
              return guessed;
            }
          }
        }
      }
    }
    return DocStringFormat.PLAIN;
  }

  @NotNull
  private static String checkImports(@NotNull VirtualFile file, @NotNull Module module) {
    final PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(file);
    if (psiFile instanceof PyFile) {
      final List<PyImportElement> importTargets = ((PyFile)psiFile).getImportTargets();
      for (PyImportElement importElement : importTargets) {
        if (PyNames.NOSE_TEST.equals(importElement.getVisibleName())) {
          return PhizdetsTestConfigurationsModel.PYTHONS_NOSETEST_NAME;
        }
        if (PyNames.PY_TEST.equals(importElement.getVisibleName())) {
          return PhizdetsTestConfigurationsModel.PY_TEST_NAME;
        }
      }
    }
    return "";
  }
}