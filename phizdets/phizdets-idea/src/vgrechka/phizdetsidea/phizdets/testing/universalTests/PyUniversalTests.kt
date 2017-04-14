/*
 * Copyright 2000-2017 JetBrains s.r.o.
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


package vgrechka.phizdetsidea.phizdets.testing.universalTests

import com.google.gson.Gson
import com.intellij.execution.Location
import com.intellij.execution.PsiLocation
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.ConfigurationFromContext
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RefactoringListenerProvider
import com.intellij.execution.configurations.RuntimeConfigurationWarning
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.execution.testframework.sm.runner.SMTestLocator
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.impl.scopes.ModuleWithDependenciesScope
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMExternalizerUtil
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.QualifiedName
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.listeners.UndoRefactoringElementAdapter
import vgrechka.phizdetsidea.extenstions.toElement
import vgrechka.phizdetsidea.phizdets.PyBundle
import vgrechka.phizdetsidea.phizdets.psi.PyClass
import vgrechka.phizdetsidea.phizdets.psi.PyFile
import vgrechka.phizdetsidea.phizdets.psi.PyFunction
import vgrechka.phizdetsidea.phizdets.psi.PyQualifiedNameOwner
import vgrechka.phizdetsidea.phizdets.psi.stubs.PyFunctionNameIndex
import vgrechka.phizdetsidea.phizdets.psi.types.TypeEvalContext
import vgrechka.phizdetsidea.phizdets.run.AbstractPhizdetsRunConfiguration
import vgrechka.phizdetsidea.phizdets.run.CommandLinePatcher
import vgrechka.phizdetsidea.phizdets.run.PhizdetsConfigurationFactoryBase
import vgrechka.phizdetsidea.phizdets.testing.*
import vgrechka.phizdetsidea.reflection.DelegationProperty
import vgrechka.phizdetsidea.reflection.Properties
import vgrechka.phizdetsidea.reflection.Property
import vgrechka.phizdetsidea.reflection.getProperties
import org.jdom.Element
import java.io.File
import java.util.*
import javax.swing.JComponent


/**
 * New configuration factories
 */
val factories: Array<PhizdetsConfigurationFactoryBase> = arrayOf(PyUniversalUnitTestFactory,
                                                               PyUniversalPyTestFactory,
                                                               PyUniversalNoseTestFactory)

internal fun getAdditionalArgumentsPropertyName() = PyUniversalTestConfiguration::additionalArguments.name


/**
 * For cases like "module.class.test_name.subtest_name" situated somewhere deep in folder which is not package,
 * this function tries to resolve test_name using index.
 */
private fun findFunctionByPartialName(qualifiedName: QualifiedName, project: Project): PyFunction? {
  // TODO: Add to background if too slow
  val components = ArrayList(qualifiedName.components)
  components.reverse()
  components.forEach {
    for (function in PyFunctionNameIndex.find(it, project)) {
      val name = function.qualifiedName
      if (name != null && name.contains(qualifiedName.toString())) {
        return function
      }
    }
  }
  return null
}

/**
 * Since runners report names of tests as qualified name, no need to convert it to PSI and back to string.
 * We just save its name and provide it again to rerun
 */
private class PyTargetBasedPsiLocation(val target: ConfigurationTarget, element: PsiElement) : PsiLocation<PsiElement>(element) {
  override fun equals(other: Any?): Boolean {
    if (other is PyTargetBasedPsiLocation) {
      return target == other.target
    }
    return false
  }

  override fun hashCode(): Int {
    return target.hashCode()
  }
}


/**
 * @return factory chosen by user in "test runner" settings
 */
private fun findConfigurationFactoryFromSettings(module: Module): ConfigurationFactory {
  val name = TestRunnerService.getInstance(module).projectConfiguration
  val factories = PhizdetsTestConfigurationType.getInstance().configurationFactories
  val configurationFactory = factories.find { it.name == name }
  return configurationFactory ?: factories.first()
}


private object PyUniversalTestsLocator : SMTestLocator {
  override fun getLocation(protocol: String, path: String, project: Project, scope: GlobalSearchScope): List<Location<out PsiElement>> {
    if (scope !is ModuleWithDependenciesScope) {
      return listOf()
    }
    val qualifiedName = QualifiedName.fromDottedString(path)
    // Assume qname id good and resolve it directly
    var element = qualifiedName.toElement(scope.module,
                                          TypeEvalContext.codeAnalysis(project, null))
    if (element == null) {
      // If no luck then resolve it using heuristic
      element = findFunctionByPartialName(qualifiedName, project)
    }
    if (element != null) {
      // Path is qualified name of phizdets test according to runners protocol
      // Parentheses are part of generators / parametrized tests
      // Until https://github.com/JetBrains/teamcity-messages/issues/121 they are disabled,
      // so we cut them out of path not to provide unsupported targets to runners
      val pathNoParentheses = QualifiedName.fromComponents(qualifiedName.components.filter { !it.contains('(') }).toString()
      return listOf(PyTargetBasedPsiLocation(ConfigurationTarget(pathNoParentheses, TestTargetType.PYTHON), element))
    }
    else {
      return listOf()
    }
  }
}

abstract class PyUniversalTestExecutionEnvironment<T : PyUniversalTestConfiguration>(configuration: T, environment: ExecutionEnvironment)
  : PhizdetsTestCommandLineStateBase<T>(configuration, environment) {

  override fun getTestLocator(): SMTestLocator = PyUniversalTestsLocator

  override fun getTestSpecs(): MutableList<String> = ArrayList(configuration.getTestSpec())

  override fun generateCommandLine(patchers: Array<out CommandLinePatcher>?): GeneralCommandLine {
    val line = super.generateCommandLine(patchers)
    line.workDirectory = File(configuration.workingDirectorySafe)
    return line
  }
}


abstract class PyUniversalTestSettingsEditor(private val form: PyUniversalTestForm)
  : SettingsEditor<PyUniversalTestConfiguration>() {


  override fun resetEditorFrom(s: PyUniversalTestConfiguration) {
    // usePojoProperties is true because we know that Form is java-based
    AbstractPhizdetsRunConfiguration.copyParams(s, form.optionsForm)
    s.copyTo(getProperties(form, usePojoProperties = true))
  }

  override fun applyEditorTo(s: PyUniversalTestConfiguration) {
    AbstractPhizdetsRunConfiguration.copyParams(form.optionsForm, s)
    s.copyFrom(getProperties(form, usePojoProperties = true))
  }

  override fun createEditor(): JComponent = form.panel
}

enum class TestTargetType(val optionName: String) {
  PYTHON("--target"), PATH("--path"), CUSTOM("")
}


/**
 * Target depends on target type. It could be path to file/folder or phizdets target
 */
data class ConfigurationTarget(@ConfigField var target: String, @ConfigField var targetType: TestTargetType) {
  fun copyTo(dst: ConfigurationTarget) {
    // TODO:  do we have such method it in Kotlin?
    dst.target = target
    dst.targetType = targetType
  }

  /**
   * Validates configuration and throws exception if target is invalid
   */
  fun checkValid() {
    if (targetType != TestTargetType.CUSTOM && target.isEmpty()) {
      throw RuntimeConfigurationWarning("Target should be set for anything but custom")
    }
  }

  /**
   * Converts target to PSI element if possible
   */
  fun asPsiElement(module: Module, context: TypeEvalContext): PsiElement? {
    if (targetType == TestTargetType.PYTHON) {
      return QualifiedName.fromDottedString(target).toElement(module, context)
    }
    return null
  }

  /**
   * Converts target to file if possible
   */
  fun asVirtualFile(fileSystem: VirtualFileSystem): VirtualFile? {
    if (targetType == TestTargetType.PATH) {
      return fileSystem.findFileByPath(target)
    }
    return null
  }
}


/**
 * To prevent legacy configuration options  from clashing with new names, we add prefix
 * to use for writing/reading xml
 */
private val Property.prefixedName: String
  get() = "_new_" + this.getName()

/**
 * Parent of all new test configurations.
 * All config-specific fields are implemented as properties. They are saved/restored automatically and passed to GUI form.
 *
 * @param runBareFunctions if config supports running functions directly in modules or only class methods
 */
abstract class PyUniversalTestConfiguration(project: Project,
                                            configurationFactory: ConfigurationFactory,
                                            private val runBareFunctions: Boolean = true)
  : AbstractPhizdetsTestRunConfiguration<PyUniversalTestConfiguration>(project, configurationFactory), PyRerunAwareConfiguration,
    RefactoringListenerProvider {
  @DelegationProperty
  val target = ConfigurationTarget(".", TestTargetType.PATH)
  @ConfigField
  var additionalArguments = ""

  val testFrameworkName = configurationFactory.name!!

  @Suppress("LeakingThis") // Legacy adapter is used to support legacy configs. Leak is ok here since everything takes place in one thread
  @DelegationProperty
  val legacyConfigurationAdapter = PyUniversalTestLegacyConfigurationAdapter(this)

  /**
   * Renames working directory if folder physically renamed
   */
  private open inner class PyConfigurationRenamer(private val workingDirectoryFile: VirtualFile?) : UndoRefactoringElementAdapter() {
    override fun refactored(element: PsiElement, oldQualifiedName: String?) {
      if (workingDirectoryFile != null) {
        workingDirectory = workingDirectoryFile.path
      }
    }
  }

  /**
   * Renames phizdets target if phizdets symbol, module or folder renamed
   */
  private inner class PyElementTargetRenamer(private val originalElement: PsiElement, workingDirectoryFile: VirtualFile?) :
    PyConfigurationRenamer(workingDirectoryFile) {
    override fun refactored(element: PsiElement, oldQualifiedName: String?) {
      super.refactored(element, oldQualifiedName)
      if (originalElement is PyQualifiedNameOwner) {
        target.target = originalElement.qualifiedName ?: return
      }
      else if (originalElement is PsiNamedElement) {
        target.target = originalElement.name ?: return
      }
    }
  }

  /**
   * Renames folder target if file or folder really renamed
   */
  private inner class PyVirtualFileRenamer(private val virtualFile: VirtualFile, workingDirectoryFile: VirtualFile?) :
    PyConfigurationRenamer(workingDirectoryFile) {
    override fun refactored(element: PsiElement, oldQualifiedName: String?) {
      super.refactored(element, oldQualifiedName)
      target.target = virtualFile.path
    }
  }

  override fun getRefactoringElementListener(element: PsiElement?): RefactoringElementListener? {
    val myModule = module
    val targetElement: PsiElement?
    if (myModule != null) {
      targetElement = target.asPsiElement(myModule, TypeEvalContext.userInitiated(project, null))
    }
    else {
      targetElement = null
    }
    val targetFile = target.asVirtualFile(LocalFileSystem.getInstance())

    val workingDirectoryFile = if (workingDirectory.isNotEmpty()) {
      LocalFileSystem.getInstance().findFileByPath(workingDirectory)
    }
    else {
      null
    }

    if (targetElement != null && PsiTreeUtil.isAncestor(element, targetElement, false)) {
      return PyElementTargetRenamer(targetElement, workingDirectoryFile)
    }
    if (targetFile != null && element is PsiFileSystemItem && VfsUtil.isAncestor(element.virtualFile, targetFile, false)) {
      return PyVirtualFileRenamer(targetFile, workingDirectoryFile)
    }
    return null
  }

  override fun checkConfiguration() {
    super.checkConfiguration()
    if (!isFrameworkInstalled()) {
      throw RuntimeConfigurationWarning(PyBundle.message("runcfg.testing.no.test.framework", testFrameworkName))
    }
    target.checkValid()
  }

  /**
   * Check if framework is available on SDK
   */
  abstract fun isFrameworkInstalled(): Boolean


  override fun isTestBased() = true

  private fun getTestSpecForPhizdetsTarget(location: Location<*>): List<String> {

    if (location is PyTargetBasedPsiLocation) {
      return listOf(location.target.targetType.optionName, location.target.target)
    }

    if (location !is PsiLocation) {
      return emptyList()
    }
    if (location.psiElement !is PyQualifiedNameOwner) {
      return emptyList()
    }
    val qualifiedName = (location.psiElement as PyQualifiedNameOwner).qualifiedName ?: return emptyList()
    return listOf(TestTargetType.PYTHON.optionName, qualifiedName)
  }

  override fun getTestSpec(location: Location<*>, failedTest: AbstractTestProxy): String? {
    val list = getTestSpecForPhizdetsTarget(location)
    if (list.isEmpty()) {
      return null
    }
    else {
      return list.joinToString(" ")
    }
  }

  override fun getTestSpecsForRerun(scope: GlobalSearchScope,
                                    locations: MutableList<Pair<Location<*>, AbstractTestProxy>>): List<String> {
    val result = ArrayList<String>()
    // Set used to remove duplicate targets
    locations.map { it.first }.toSet().map { getTestSpecForPhizdetsTarget(it) }.filterNotNull().forEach { result.addAll(it) }
    return result + generateRawArguments()
  }

  fun getTestSpec(): List<String> {
    // For custom we only need to provide additional (raw) args
    // Provide target otherwise
    if (target.targetType == TestTargetType.CUSTOM) {
      return generateRawArguments()
    }
    return listOf(target.targetType.optionName, target.target) + generateRawArguments()
  }

  /**
   * raw arguments to be added after "--" and passed to runner directly
   */
  private fun generateRawArguments(): List<String> {
    val rawArguments = additionalArguments + " " + getCustomRawArgumentsString()
    if (rawArguments.isNotBlank()) {
      return listOf("--") + rawArguments.trim().split(" ")
    }
    return emptyList()
  }

  override fun suggestedName() =
    when (target.targetType) {
      TestTargetType.PATH -> {
        val name = target.asVirtualFile(LocalFileSystem.getInstance())?.name
        "$testFrameworkName in " + (name ?: target.target)
      }
      TestTargetType.PYTHON -> {
        "$testFrameworkName for " + target.target
      }
      else -> {
        testFrameworkName
      }
    }


  /**
   * @return configuration-specific arguments
   */
  protected open fun getCustomRawArgumentsString() = ""

  fun reset() {
    target.target = "."
    target.targetType = TestTargetType.PATH
    additionalArguments = ""
  }

  fun copyFrom(src: Properties) {
    src.copyTo(getConfigFields())
  }

  fun copyTo(dst: Properties) {
    getConfigFields().copyTo(dst)
  }


  override fun writeExternal(element: Element) {
    // Write legacy config to preserve it
    legacyConfigurationAdapter.writeExternal(element)
    // Super is called after to overwrite legacy settings with new one
    super.writeExternal(element)

    val gson = Gson()

    getConfigFields().properties.forEach {
      val value = it.get()
      if (value != null) {
        // No need to write null since null is default value
        JDOMExternalizerUtil.writeField(element, it.prefixedName, gson.toJson(value))
      }
    }
  }

  override fun readExternal(element: Element) {
    super.readExternal(element)

    val gson = Gson()

    getConfigFields().properties.forEach {
      val fromJson: Any? = gson.fromJson(JDOMExternalizerUtil.readField(element, it.prefixedName), it.getType())
      if (fromJson != null) {
        it.set(fromJson)
      }
    }
    legacyConfigurationAdapter.readExternal(element)
  }


  private fun getConfigFields() = getProperties(this, ConfigField::class.java)

  /**
   * Checks if element could be test target for this config.
   * Function is used to create tests by context.
   *
   * If yes, and element is [PsiElement] then it is [TestTargetType.PYTHON].
   * If file then [TestTargetType.PATH]
   */
  fun couldBeTestTarget(element: PsiElement) =
    // TODO: PhizdetsUnitTestUtil logic is weak. We should give user ability to launch test on symbol since user knows better if folder
    // contains tests etc
    when (element) {
      is PyFile -> isTestFile(element)
      is PsiDirectory -> element.children.any { it is PyFile && isTestFile(it) }
      is PyFunction -> PhizdetsUnitTestUtil.isTestCaseFunction(element, runBareFunctions)
      is PyClass -> PhizdetsUnitTestUtil.isTestCaseClass(element, TypeEvalContext.userInitiated(element.project, element.containingFile))
      else -> false
    }
}

private fun isTestFile(file: PyFile): Boolean {
  return PhizdetsUnitTestUtil.isUnitTestFile(file) ||
  PhizdetsUnitTestUtil.getTestCaseClassesFromFile(file, TypeEvalContext.userInitiated(file.project, file)).isNotEmpty()
}

abstract class PyUniversalTestFactory<out CONF_T : PyUniversalTestConfiguration> : PhizdetsConfigurationFactoryBase(
  PhizdetsTestConfigurationType.getInstance()) {
  override abstract fun createTemplateConfiguration(project: Project): CONF_T
}

/**
 * Only one producer is registered with EP, but it uses factory configured by user to prdouce different configs
 */
object PyUniversalTestsConfigurationProducer : AbstractPhizdetsTestConfigurationProducer<PyUniversalTestConfiguration>(
  PhizdetsTestConfigurationType.getInstance()) {

  override val configurationClass = PyUniversalTestConfiguration::class.java

  override fun cloneTemplateConfiguration(context: ConfigurationContext): RunnerAndConfigurationSettings {
    return cloneTemplateConfigurationStatic(context, findConfigurationFactoryFromSettings(context.module))
  }

  override fun createConfigurationFromContext(context: ConfigurationContext?): ConfigurationFromContext? {
    // Since we need module, no need to even try to create config with out of it
    context?.module ?: return null
    return super.createConfigurationFromContext(context)
  }

  override fun findOrCreateConfigurationFromContext(context: ConfigurationContext?): ConfigurationFromContext? {
    if (!isNewTestsModeEnabled()) {
      return null
    }
    return super.findOrCreateConfigurationFromContext(context)
  }

  override fun setupConfigurationFromContext(configuration: PyUniversalTestConfiguration?,
                                             context: ConfigurationContext?,
                                             sourceElement: Ref<PsiElement>?): Boolean {

    if (sourceElement == null || configuration == null) {
      return false
    }

    val location = context?.location
    if (location is PyTargetBasedPsiLocation) {
      location.target.copyTo(configuration.target)
    }
    else {
      val targetForConfig = getTargetForConfig(configuration, sourceElement.get()) ?: return false
      targetForConfig.copyTo(configuration.target)
    }
    configuration.setGeneratedName()
    return true
  }


  /**
   * Find concrete element to be used as test target.
   * @return configuration name and its target
   */
  private fun getTargetForConfig(configuration: PyUniversalTestConfiguration,
                                 baseElement: PsiElement): ConfigurationTarget? {
    var element = baseElement
    // Go up until we reach top of the file
    // asking configuration about each element if it is supported or not
    // If element is supported -- set it as configuration target
    do {
      if (configuration.couldBeTestTarget(element)) {
        when (element) {
          is PyQualifiedNameOwner -> { // Function, class, method
            val qualifiedName = element.qualifiedName
            if (qualifiedName == null) {
              Logger.getInstance(PyUniversalTestConfiguration::class.java).warn("$element has no qualified name")
              return null
            }
            return ConfigurationTarget(qualifiedName, TestTargetType.PYTHON)
          }
          is PsiFileSystemItem -> return ConfigurationTarget(element.virtualFile.path, TestTargetType.PATH)
        }
      }
      element = element.parent
    }
    while (element !is PsiDirectory) // if parent is folder, then we are at file level
    return null
  }


  override fun isConfigurationFromContext(configuration: PyUniversalTestConfiguration?, context: ConfigurationContext?): Boolean {
    val psiElement = context?.psiLocation ?: return false
    val targetForConfig = getTargetForConfig(configuration!!, psiElement) ?: return false
    return configuration.target == targetForConfig
  }
}


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
/**
 * Mark run configuration field with it to enable saving, resotring and form iteraction
 */
annotation class ConfigField
