package vgrechka.phizdetsidea

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.intellij.CommonBundle
import com.intellij.diagnostic.logging.LogConfigurationPanel
import com.intellij.execution.*
import com.intellij.execution.configuration.AbstractRunConfiguration
import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configuration.RunConfigurationExtensionsManager
import com.intellij.execution.configurations.*
import com.intellij.execution.console.ConsoleExecuteAction
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.filters.TextConsoleBuilder
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.filters.UrlFilter
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.GenericProgramRunner
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.ui.ConsoleView
import com.intellij.facet.FacetManager
import com.intellij.ide.DataManager
import com.intellij.lang.Language
import com.intellij.notification.Notification
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.SettingsEditorGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.roots.impl.libraries.LibraryImpl
import com.intellij.openapi.ui.ComponentWithBrowseButton
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.*
import com.intellij.openapi.util.io.FileSystemUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.CharFilter
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.encoding.EncodingProjectManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.listeners.RefactoringElementAdapter
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.reference.SoftReference
import com.intellij.remote.*
import com.intellij.remote.ext.CredentialsCase
import com.intellij.remote.ext.LanguageCaseCollector
import com.intellij.terminal.TerminalExecutionConsole
import com.intellij.ui.PanelWithAnchor
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.*
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.io.BaseDataReader
import com.intellij.util.io.BaseOutputReader
import com.intellij.util.text.CharSequenceReader
import org.jdom.Element
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import vgrechka.*
import java.awt.*
import java.awt.event.ActionListener
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.net.ServerSocket
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.IllegalCharsetNameException
import java.nio.charset.UnsupportedCharsetException
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.HyperlinkEvent
import kotlin.properties.Delegates.notNull


//class PhizdetsDebugRunner : GenericProgramRunner<PhizdetsRunnerSettings>() {
//    override fun canRun(executorId: String, profile: RunProfile): Boolean {
//        if (DefaultDebugExecutor.EXECUTOR_ID != executorId) {
//            // If not debug at all
//            return false
//        }
//
//        if (profile is WrappingRunConfiguration<*>) {
//            // If configuration is wrapper -- unwrap it and check
//            return isDebuggable(profile.peer)
//        }
//        return isDebuggable(profile)
//    }
//
//    private fun isDebuggable(profile: RunProfile): Boolean {
//        if (profile is PhizdetsRunConfiguration) {
//            return true
//        }
//        return false
//    }
//
//    override fun getRunnerId(): String {
//        return "PhizdetsDebugRunner"
//    }
//}

//class PhizdetsRunnerSettings : RunnerSettings {
//    override fun readExternal(element: Element?) {
//    }
//
//    override fun writeExternal(element: Element?) {
//    }
//}



//class PhizdetsConfigurationType : ConfigurationType {
//
//    val factory = PhizdetsConfigurationFactory(this)
//
//    class PhizdetsConfigurationFactory constructor(configurationType: ConfigurationType) : PhizdetsConfigurationFactoryBase(configurationType) {
//
//        override fun createTemplateConfiguration(project: Project): RunConfiguration {
//            return PhizdetsRunConfiguration(project, this)
//        }
//    }
//
//    override fun getDisplayName(): String {
//        return "Phizdets"
//    }
//
//    override fun getConfigurationTypeDescription(): String {
//        return "Phizdets run configuration"
//    }
//
//    override fun getIcon(): Icon {
//        return PhizdetsIcons.Phizdets.Phizdets
//    }
//
//    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
//        return arrayOf<ConfigurationFactory>(factory)
//    }
//
//    @NonNls
//    override fun getId(): String {
//        return "PhizdetsConfigurationType"
//    }
//
//    companion object {
//
//        val instance: PhizdetsConfigurationType
//            get() = ConfigurationTypeUtil.findConfigurationType(PhizdetsConfigurationType::class.java)
//    }
//}
//
//abstract class PhizdetsConfigurationFactoryBase constructor(type: ConfigurationType) : ConfigurationFactory(type) {
//    override fun isApplicable(project: Project): Boolean {
//        return true
//    }
//}
//
//class PhizdetsRunConfiguration constructor(project: Project, configurationFactory: ConfigurationFactory) : AbstractPhizdetsRunConfiguration<PhizdetsRunConfiguration>(project, configurationFactory), AbstractPhizdetsRunConfigurationParams, PhizdetsRunConfigurationParams, RefactoringListenerProvider {
//    override var scriptName by notNull<String>()
//    override var scriptParameters by notNull<String>()
//    private var myShowCommandLineAfterwards = false
//    private var myEmulateTerminal = false
//
//    init {
//        setUnbufferedEnv()
//    }
//
//    override fun createConfigurationEditor(): SettingsEditor<PhizdetsRunConfiguration> {
//        return PhizdetsRunConfigurationEditor(this)
//    }
//
//    override fun getState(executor: Executor, env: ExecutionEnvironment): RunProfileState {
//        return PhizdetsScriptCommandLineState(this, env)
//    }
//
//    override fun checkConfiguration() {
//        super.checkConfiguration()
//
//        if (StringUtil.isEmptyOrSpaces(scriptName)) {
//            throw RuntimeConfigurationException(PhiBundle.message("runcfg.unittest.no_script_name"))
//        }
//    }
//
//    override fun suggestedName(): String? {
//        val scriptName = scriptName ?: return null
//        val name = File(scriptName).name
//        if (name.endsWith(".php")) {
//            return name.substring(0, name.length - 3)
//        }
//        return name
//    }
//
//    override fun showCommandLineAfterwards(): Boolean {
//        return myShowCommandLineAfterwards
//    }
//
//    override fun setShowCommandLineAfterwards(showCommandLineAfterwards: Boolean) {
//        myShowCommandLineAfterwards = showCommandLineAfterwards
//    }
//
//    override fun emulateTerminal(): Boolean {
//        return myEmulateTerminal
//    }
//
//    override fun setEmulateTerminal(emulateTerminal: Boolean) {
//        myEmulateTerminal = emulateTerminal
//    }
//
//    override fun readExternal(element: Element) {
//        super.readExternal(element)
//        scriptName = JDOMExternalizerUtil.readField(element, SCRIPT_NAME)!!
//        scriptParameters = JDOMExternalizerUtil.readField(element, PARAMETERS)!!
//        myShowCommandLineAfterwards = java.lang.Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, SHOW_COMMAND_LINE, "false"))
//        myEmulateTerminal = java.lang.Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, EMULATE_TERMINAL, "false"))
//    }
//
//    override fun writeExternal(element: Element) {
//        super.writeExternal(element)
//        JDOMExternalizerUtil.writeField(element, SCRIPT_NAME, scriptName)
//        JDOMExternalizerUtil.writeField(element, PARAMETERS, scriptParameters)
//        JDOMExternalizerUtil.writeField(element, SHOW_COMMAND_LINE, java.lang.Boolean.toString(myShowCommandLineAfterwards))
//        JDOMExternalizerUtil.writeField(element, EMULATE_TERMINAL, java.lang.Boolean.toString(myEmulateTerminal))
//    }
//
//    override val baseParams: AbstractPhizdetsRunConfigurationParams
//        get() = this
//
//    override fun getRefactoringElementListener(element: PsiElement): RefactoringElementListener? {
//        if (element is PsiFile) {
//            val virtualFile = element.virtualFile
//            if (virtualFile != null && Comparing.equal(File(virtualFile.path).absolutePath,
//                                                       File(scriptName!!).absolutePath)) {
//                return object : RefactoringElementAdapter() {
//                    public override fun elementRenamedOrMoved(newElement: PsiElement) {
//                        val virtualFile = (newElement as PsiFile).virtualFile
//                        if (virtualFile != null) {
//                            updateScriptName(virtualFile.path)
//                        }
//                    }
//
//                    override fun undoElementMovedOrRenamed(newElement: PsiElement, oldQualifiedName: String) {
//                        updateScriptName(oldQualifiedName)
//                    }
//
//                    private fun updateScriptName(path: String) {
//                        scriptName = FileUtil.toSystemDependentName(path)
//                    }
//                }
//            }
//        }
//        return null
//    }
//
//    companion object {
//        val SCRIPT_NAME = "SCRIPT_NAME"
//        val PARAMETERS = "PARAMETERS"
//        val MULTIPROCESS = "MULTIPROCESS"
//        val SHOW_COMMAND_LINE = "SHOW_COMMAND_LINE"
//        val EMULATE_TERMINAL = "EMULATE_TERMINAL"
//
//        fun copyParams(source: PhizdetsRunConfigurationParams, target: PhizdetsRunConfigurationParams) {
//            AbstractPhizdetsRunConfiguration.copyParams(source.baseParams, target.baseParams)
//            target.scriptName = source.scriptName
//            target.scriptParameters = source.scriptParameters
//            target.setShowCommandLineAfterwards(source.showCommandLineAfterwards())
//            target.setEmulateTerminal(source.emulateTerminal())
//        }
//    }
//}
//
//interface PhizdetsRunParams {
//    var interpreterOptions: String
//
//    var workingDirectory: String
//
//    var sdkHome: String?
//
//    fun setModule(module: Module)
//
//    val moduleName: String
//
//    var isUseModuleSdk: Boolean
//
//    fun isPassParentEnvs(): Boolean
//    fun setPassParentEnvs(x: Boolean)
//
//    fun getEnvs(): Map<String, String>
//    fun setEnvs(x: Map<String, String>)
//
//    var mappingSettings: PathMappingSettings?
//
//    fun shouldAddContentRoots(): Boolean
//
//    fun shouldAddSourceRoots(): Boolean
//
//    fun setAddContentRoots(flag: Boolean)
//
//    fun setAddSourceRoots(flag: Boolean)
//}
//
//interface AbstractPhizdetsRunConfigurationParams : PhizdetsRunParams {
//    val module: Module
//}
//
//interface CommandLinePatcher {
//    fun patchCommandLine(commandLine: GeneralCommandLine)
//}
//
//interface PhiCommonOptionsFormData {
//    val project: Project
//    val validModules: List<Module>
//    fun showConfigureInterpretersLink(): Boolean
//}
//
//fun isPhizda() = true
//
//abstract class AbstractPhizdetsRunConfiguration<T : AbstractPhizdetsRunConfiguration<T>>(project: Project, factory: ConfigurationFactory) : AbstractRunConfiguration(project, factory), AbstractPhizdetsRunConfigurationParams, CommandLinePatcher {
//    override var interpreterOptions = ""
//    override var workingDirectory = ""
//    private var mySdkHome = ""
//    override var isUseModuleSdk: Boolean = false
//    private var myAddContentRoots = true
//    private var myAddSourceRoots = true
//
//    override var mappingSettings: PathMappingSettings? = null
//    /**
//     * To prevent "double module saving" child may enable this flag
//     * and no module info would be saved
//     */
//    protected var mySkipModuleSerialization: Boolean = false
//
//    init {
//        configurationModule.init()
//    }
//
//    override fun getValidModules(): List<Module> {
//        return getValidModules(project)
//    }
//
//    val isTestBased: Boolean
//        get() = false
//
//    val commonOptionsFormData: PhiCommonOptionsFormData
//        get() = object : PhiCommonOptionsFormData {
//            override val project: Project
//                get() = this@AbstractPhizdetsRunConfiguration.project
//
//            override val validModules: List<Module>
//                get() = this@AbstractPhizdetsRunConfiguration.validModules
//
//            override fun showConfigureInterpretersLink(): Boolean {
//                return false
//            }
//        }
//
//    override fun getConfigurationEditor(): SettingsEditor<T> {
//        val runConfigurationEditor = PhizdetsExtendedConfigurationEditor.create(createConfigurationEditor())
//
//        val group = SettingsEditorGroup<T>()
//
//        // run configuration settings tab:
//        group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), runConfigurationEditor)
//
//        // tabs provided by extensions:
//
//        PhizdetsRunConfigurationExtensionsManager.instance.appendEditors(this, group as SettingsEditorGroup<*>)
//        group.addEditor(ExecutionBundle.message("logs.tab.title"), LogConfigurationPanel<T>())
//
//        return group
//    }
//
//    protected abstract fun createConfigurationEditor(): SettingsEditor<T>
//
//    @Throws(RuntimeConfigurationException::class)
//    override fun checkConfiguration() {
//        super.checkConfiguration()
//
//        checkSdk()
//
//        checkExtensions()
//    }
//
//    @Throws(RuntimeConfigurationException::class)
//    private fun checkExtensions() {
//        try {
//            PhizdetsRunConfigurationExtensionsManager.instance.validateConfiguration(this, false)
//        } catch (e: RuntimeConfigurationException) {
//            throw e
//        } catch (ee: Exception) {
//            throw RuntimeConfigurationException(ee.message)
//        }
//
//    }
//
//    @Throws(RuntimeConfigurationError::class)
//    private fun checkSdk() {
//        if (isPhizda()) {
//            val path = interpreterPath
//            if (StringUtil.isEmptyOrSpaces(path)) {
//                throw RuntimeConfigurationError("Please select a valid Phizdets interpreter")
//            }
//        } else {
//            if (!isUseModuleSdk) {
//                if (StringUtil.isEmptyOrSpaces(sdkHome)) {
//                    val projectSdk = ProjectRootManager.getInstance(project).projectSdk
//                    if (projectSdk == null || projectSdk.sdkType !is PhizdetsSdkType) {
//                        throw RuntimeConfigurationError(PhiBundle.message("runcfg.unittest.no_sdk"))
//                    }
//                } else if (!PhizdetsSdkType.getInstance().isValidSdkHome(sdkHome)) {
//                    throw RuntimeConfigurationError(PhiBundle.message("runcfg.unittest.no_valid_sdk"))
//                }
//            } else {
//                val sdk = PhizdetsSdkType.findPhizdetsSdk(module) ?: throw RuntimeConfigurationError(PhiBundle.message("runcfg.unittest.no_module_sdk"))
//            }
//        }
//    }
//
//    override var sdkHome: String?
//        get() {
//            var sdkHome: String? = mySdkHome
//            if (StringUtil.isEmptyOrSpaces(mySdkHome)) {
//                val projectJdk = PhizdetsSdkType.findPhizdetsSdk(module)
//                if (projectJdk != null) {
//                    sdkHome = projectJdk.getHomePath()
//                }
//            }
//            return sdkHome
//        }
//        set(sdkHome) {
//            mySdkHome = sdkHome!!
//        }
//
//    val interpreterPath: String?
//        get() {
//            val sdkHome: String?
//            if (isUseModuleSdk) {
//                val sdk = PhizdetsSdkType.findPhizdetsSdk(module) ?: return null
//                sdkHome = sdk.getHomePath()
//            } else {
//                sdkHome = this.sdkHome
//            }
//            return sdkHome
//        }
//
//    val sdk: Sdk?
//        get() {
//            if (isUseModuleSdk) {
//                return PhizdetsSdkType.findPhizdetsSdk(module)
//            } else {
//                return PhizdetsSdkType.findSdkByPath(sdkHome)
//            }
//        }
//
//    @Throws(InvalidDataException::class)
//    override fun readExternal(element: Element) {
//        super.readExternal(element)
//        interpreterOptions = JDOMExternalizerUtil.readField(element, "INTERPRETER_OPTIONS")!!
//        readEnvs(element)
//        mySdkHome = JDOMExternalizerUtil.readField(element, "SDK_HOME")!!
//        workingDirectory = JDOMExternalizerUtil.readField(element, "WORKING_DIRECTORY")!!
//        isUseModuleSdk = java.lang.Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "IS_MODULE_SDK"))
//        val addContentRoots = JDOMExternalizerUtil.readField(element, "ADD_CONTENT_ROOTS")
//        myAddContentRoots = addContentRoots == null || java.lang.Boolean.parseBoolean(addContentRoots)
//        val addSourceRoots = JDOMExternalizerUtil.readField(element, "ADD_SOURCE_ROOTS")
//        myAddSourceRoots = addSourceRoots == null || java.lang.Boolean.parseBoolean(addSourceRoots)
//        if (!mySkipModuleSerialization) {
//            configurationModule.readExternal(element)
//        }
//
//        mappingSettings = PathMappingSettings.readExternal(element)
//        // extension settings:
//        PhizdetsRunConfigurationExtensionsManager.instance.readExternal(this, element)
//    }
//
//    protected fun readEnvs(element: Element) {
//        val parentEnvs = JDOMExternalizerUtil.readField(element, "PARENT_ENVS")
//        if (parentEnvs != null) {
//            isPassParentEnvs = java.lang.Boolean.parseBoolean(parentEnvs)
//        }
//        EnvironmentVariablesComponent.readExternal(element, envs)
//    }
//
//    @Throws(WriteExternalException::class)
//    override fun writeExternal(element: Element) {
//        super.writeExternal(element)
//        JDOMExternalizerUtil.writeField(element, "INTERPRETER_OPTIONS", interpreterOptions)
//        writeEnvs(element)
//        JDOMExternalizerUtil.writeField(element, "SDK_HOME", mySdkHome)
//        JDOMExternalizerUtil.writeField(element, "WORKING_DIRECTORY", workingDirectory)
//        JDOMExternalizerUtil.writeField(element, "IS_MODULE_SDK", java.lang.Boolean.toString(isUseModuleSdk))
//        JDOMExternalizerUtil.writeField(element, "ADD_CONTENT_ROOTS", java.lang.Boolean.toString(myAddContentRoots))
//        JDOMExternalizerUtil.writeField(element, "ADD_SOURCE_ROOTS", java.lang.Boolean.toString(myAddSourceRoots))
//        if (!mySkipModuleSerialization) {
//            configurationModule.writeExternal(element)
//        }
//
//        // extension settings:
//        PhizdetsRunConfigurationExtensionsManager.instance.writeExternal(this, element)
//
//        PathMappingSettings.writeExternal(element, mappingSettings)
//    }
//
//    protected fun writeEnvs(element: Element) {
//        JDOMExternalizerUtil.writeField(element, "PARENT_ENVS", java.lang.Boolean.toString(isPassParentEnvs))
//        EnvironmentVariablesComponent.writeExternal(element, envs)
//    }
//
//    override val module: Module
//        get() = configurationModule.module!!
//
//    override fun shouldAddContentRoots(): Boolean {
//        return myAddContentRoots
//    }
//
//    override fun shouldAddSourceRoots(): Boolean {
//        return myAddSourceRoots
//    }
//
//    override fun setAddSourceRoots(flag: Boolean) {
//        myAddSourceRoots = flag
//    }
//
//    override fun setAddContentRoots(flag: Boolean) {
//        myAddContentRoots = flag
//    }
//
//    /**
//     * Some setups (e.g. virtualenv) provide a script that alters environment variables before running a Phizdets interpreter or other tools.
//     * Such settings are not directly stored but applied right before running using this method.
//
//     * @param commandLine what to patch
//     */
//    override fun patchCommandLine(commandLine: GeneralCommandLine) {
//        val interpreterPath = interpreterPath
//        val sdk = sdk
//        if (sdk != null && interpreterPath != null) {
//            patchCommandLineFirst(commandLine, interpreterPath)
//            patchCommandLineForVirtualenv(commandLine, interpreterPath)
//            patchCommandLineForBuildout(commandLine, interpreterPath)
//            patchCommandLineLast(commandLine, interpreterPath)
//        }
//    }
//
//    /**
//     * Patches command line before virtualenv and buildout patchers.
//     * Default implementation does nothing.
//
//     * @param commandLine
//     * *
//     * @param sdkHome
//     */
//    protected fun patchCommandLineFirst(commandLine: GeneralCommandLine, sdkHome: String) {
//        // override
//    }
//
//    /**
//     * Patches command line after virtualenv and buildout patchers.
//     * Default implementation does nothing.
//
//     * @param commandLine
//     * *
//     * @param sdkHome
//     */
//    protected fun patchCommandLineLast(commandLine: GeneralCommandLine, sdkHome: String) {
//        // override
//    }
//
//    /**
//     * Gets called after [.patchCommandLineForVirtualenv]
//     * Does nothing here, real implementations should use alter running script name or use engulfer.
//
//     * @param commandLine
//     * *
//     * @param sdkHome
//     */
//    protected fun patchCommandLineForBuildout(commandLine: GeneralCommandLine, sdkHome: String) {
//    }
//
//    /**
//     * Alters PATH so that a virtualenv is activated, if present.
//
//     * @param commandLine
//     * *
//     * @param sdkHome
//     */
//    protected fun patchCommandLineForVirtualenv(commandLine: GeneralCommandLine, sdkHome: String) {
//        wtf("2017f3cc-0a2b-414d-9413-7061364d14a2")
//    }
//
//    protected fun setUnbufferedEnv() {
//        wtf("b8646009-815b-4b72-9225-7e9410c2e40a")
////        val envs = envs
////        // unbuffered I/O is easier for IDE to handle
////        PhizdetsEnvUtil.setPhizdetsUnbuffered(envs)
//    }
//
//    override fun excludeCompileBeforeLaunchOption(): Boolean {
//        return false
////        val module = module
////        return if (module != null) ModuleType.get(module) is PhizdetsModuleTypeBase else true
//    }
//
//    fun canRunWithCoverage(): Boolean {
//        return true
//    }
//
//
//    /**
//     * Note to inheritors: Always check [.getWorkingDirectory] first. You should return it, if it is not empty since
//     * user should be able to set dir explicitly. Then, do your guess and return super as last resort.
//
//     * @return working directory to run, never null, does its best to guess which dir to use.
//     * * Unlike [.getWorkingDirectory] it does not simply take directory from config.
//     */
//    val workingDirectorySafe: String
//        get() {
//            val result = if (StringUtil.isEmpty(workingDirectory)) project.basePath else workingDirectory
//            if (result != null) {
//                return result
//            }
//
//            val firstModuleRoot = firstModuleRoot
//            if (firstModuleRoot != null) {
//                return firstModuleRoot
//            }
//            return File(".").absolutePath
//        }
//
//    private val firstModuleRoot: String?
//        get() {
//            val module = module ?: return null
//            val roots = ModuleRootManager.getInstance(module).contentRoots
//            return if (roots.size > 0) roots[0].path else null
//        }
//
//    override val moduleName: String
//        get() {
//            val module = module
//            return module?.name!!
//        }
//
//    override fun isCompileBeforeLaunchAddedByDefault(): Boolean {
//        return false
//    }
//
//    /**
//     * Adds test specs (like method, class, script, etc) to list of runner parameters.
//     */
//    fun addTestSpecsAsParameters(paramsGroup: ParamsGroup, testSpecs: List<String>) {
//        // By default we simply add them as arguments
//        paramsGroup.addParameters(testSpecs)
//    }
//
//    companion object {
//
//        fun getValidModules(project: Project): List<Module> {
//            val modules = ModuleManager.getInstance(project).modules
//            val result = Lists.newArrayList<Module>()
//            for (module in modules) {
//                if (PhizdetsSdkType.findPhizdetsSdk(module) != null) {
//                    result.add(module)
//                }
//            }
//            return result
//        }
//
//        fun copyParams(source: AbstractPhizdetsRunConfigurationParams, target: AbstractPhizdetsRunConfigurationParams) {
//            target.setEnvs(HashMap(source.getEnvs()))
//            target.interpreterOptions = source.interpreterOptions
//            target.setPassParentEnvs(source.isPassParentEnvs())
//            target.sdkHome = source.sdkHome
//            target.workingDirectory = source.workingDirectory
//            target.setModule(source.module)
//            target.isUseModuleSdk = source.isUseModuleSdk
//            target.mappingSettings = source.mappingSettings
//            target.setAddContentRoots(source.shouldAddContentRoots())
//            target.setAddSourceRoots(source.shouldAddSourceRoots())
//        }
//    }
//}
//
//interface PhizdetsRunConfigurationParams {
//    val baseParams: AbstractPhizdetsRunConfigurationParams
//
//    var scriptName: String
//
//    var scriptParameters: String
//
//    fun showCommandLineAfterwards(): Boolean
//    fun setShowCommandLineAfterwards(showCommandLineAfterwards: Boolean)
//
//    fun emulateTerminal(): Boolean
//    fun setEmulateTerminal(emulateTerminal: Boolean)
//}
//
//object PhizdetsIcons {
//    private fun load(path: String): Icon {
//        return IconLoader.getIcon(path, PhizdetsIcons::class.java)
//    }
//
//    object Phizdets {
//
//        object Buildout {
//            val Buildout = load("/icons/vgrechka/phizdets/buildout/buildout.png") // 16x16
//
//        }
//
//        val DataView = load("/icons/vgrechka/phizdets/DataView.png") // 13x13
//
//        object Debug {
//            val CommandLine = load("/icons/vgrechka/phizdets/debug/commandLine.png") // 16x16
//            val SpecialVar = load("/icons/vgrechka/phizdets/debug/specialVar.png") // 16x16
//            val StepIntoMyCode = load("/icons/vgrechka/phizdets/debug/StepIntoMyCode.png") // 16x16
//
//        }
//
//        val Dotnet = load("/icons/vgrechka/phizdets/dotnet.png") // 16x16
//        val Function = load("/icons/vgrechka/phizdets/function.png") // 16x16
//        val InterpreterGear = load("/icons/vgrechka/phizdets/interpreterGear.png") // 16x16
//        val Jython = load("/icons/vgrechka/phizdets/jython.png") // 16x16
//
//        object Nodes {
//            val Cyan_dot = load("/icons/vgrechka/phizdets/nodes/cyan-dot.png") // 16x16
//            val Lock = load("/icons/vgrechka/phizdets/nodes/lock.png") // 16x16
//            val Red_inv_triangle = load("/icons/vgrechka/phizdets/nodes/red-inv-triangle.png") // 16x16
//
//        }
//
//        val PropertyDeleter = load("/icons/vgrechka/phizdets/propertyDeleter.png") // 16x16
//        val PropertyGetter = load("/icons/vgrechka/phizdets/propertyGetter.png") // 16x16
//        val PropertySetter = load("/icons/vgrechka/phizdets/propertySetter.png") // 16x16
//        val Phizdets_logo = load("/icons/vgrechka/phizdets/phizdets-logo.png") // 16x16
//        val Phizdets = load("/icons/vgrechka/phizdets/phizdets.png") // 16x16
//        val Phizdets_24 = load("/icons/vgrechka/phizdets/phizdets_24.png") // 24x24
//        val PhizdetsClosed = load("/icons/vgrechka/phizdets/phizdetsClosed.png") // 16x16
//        val PhizdetsConsole = load("/icons/vgrechka/phizdets/phizdetsConsole.png") // 16x16
//        val PhizdetsConsoleToolWindow = load("/icons/vgrechka/phizdets/phizdetsConsoleToolWindow.png") // 13x13
//        val PhizdetsTests = load("/icons/vgrechka/phizdets/phizdetsTests.png") // 16x16
//        val RemoteInterpreter = load("/icons/vgrechka/phizdets/RemoteInterpreter.png") // 16x16
//        val TemplateRoot = load("/icons/vgrechka/phizdets/templateRoot.png") // 16x16
//        val Virtualenv = load("/icons/vgrechka/phizdets/virtualenv.png") // 16x16
//
//    }
//}
//
//class PhizdetsRunConfigurationEditor(configuration: PhizdetsRunConfiguration) : SettingsEditor<PhizdetsRunConfiguration>() {
//    private var myForm: PhizdetsRunConfigurationForm? = PhizdetsRunConfigurationForm(configuration)
//
//    override fun resetEditorFrom(config: PhizdetsRunConfiguration) {
//        PhizdetsRunConfiguration.copyParams(config, myForm!!)
//    }
//
//    @Throws(ConfigurationException::class)
//    override fun applyEditorTo(config: PhizdetsRunConfiguration) {
//        PhizdetsRunConfiguration.copyParams(myForm!!, config)
//    }
//
//    override fun createEditor(): JComponent {
//        return myForm!!.panel
//    }
//
//    override fun disposeEditor() {
//        myForm = null
//    }
//}
//
//class PhizdetsRunConfigurationForm(configuration: PhizdetsRunConfiguration) : PhizdetsRunConfigurationParams, PanelWithAnchor {
//    private val myRootPanel: JPanel? = null
//    private val myScriptTextField: TextFieldWithBrowseButton? = null
//    private val myScriptParametersTextField: RawCommandLineEditor? = null
//    private val myCommonOptionsPlaceholder: JPanel? = null
//    private val myScriptParametersLabel: JBLabel? = null
//    private val myCommonOptionsForm: AbstractPhiCommonOptionsForm
//    private var anchor: JComponent? = null
//    private val myProject: Project
//    private val myShowCommandLineCheckbox: JBCheckBox? = null
//    private val myEmulateTerminalCheckbox: JBCheckBox? = null
//
//    init {
//        myCommonOptionsForm = PhiCommonOptionsFormFactory.instance.createForm(configuration.commonOptionsFormData)
//        myCommonOptionsForm.addInterpreterModeListener(java.util.function.Consumer<Boolean> {isRemoteInterpreter -> emulateTerminalEnabled(!isRemoteInterpreter)})
//        myCommonOptionsPlaceholder!!.add(myCommonOptionsForm.mainPanel, BorderLayout.CENTER)
//
//        myProject = configuration.getProject()
//
//        val chooserDescriptor = object : FileChooserDescriptor(true, false, false, false, false, false) {
//            override fun isFileVisible(file: VirtualFile, showHiddenFiles: Boolean): Boolean {
//                return file.isDirectory || file.extension == null || Comparing.equal(file.extension, "php")
//            }
//        }
//        //chooserDescriptor.setRoot(s.getProject().getBaseDir());
//
//        val listener = object : ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>("Select Script", "", myScriptTextField, myProject,
//                                                                                                 chooserDescriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT) {
//
//            override fun onFileChosen(chosenFile: VirtualFile) {
//                super.onFileChosen(chosenFile)
//                myCommonOptionsForm.workingDirectory = chosenFile.parent.path
//            }
//        }
//
//        myScriptTextField!!.addActionListener(listener)
//
//        if (SystemInfo.isWindows) {
//            //TODO: enable it on Windows when it works there
//            emulateTerminalEnabled(false)
//        }
//
//        myEmulateTerminalCheckbox!!.isSelected = false
//
//        myEmulateTerminalCheckbox.addChangeListener {updateShowCommandLineEnabled()}
//
//        setAnchor(myCommonOptionsForm.getAnchor())
//    }
//
//    private fun updateShowCommandLineEnabled() {
//        myShowCommandLineCheckbox!!.isEnabled = !myEmulateTerminalCheckbox!!.isVisible || !myEmulateTerminalCheckbox.isSelected
//    }
//
//    private fun emulateTerminalEnabled(flag: Boolean) {
//        myEmulateTerminalCheckbox!!.isVisible = flag
//        updateShowCommandLineEnabled()
//    }
//
//    val panel: JComponent
//        get() = myRootPanel!!
//
//    override val baseParams: AbstractPhizdetsRunConfigurationParams
//        get() = myCommonOptionsForm
//
//    override var scriptName: String
//        get() = FileUtil.toSystemIndependentName(myScriptTextField!!.text.trim {it <= ' '})
//        set(scriptName) = myScriptTextField!!.setText(if (scriptName == null) "" else FileUtil.toSystemDependentName(scriptName))
//
//    override var scriptParameters: String
//        get() = myScriptParametersTextField!!.text.trim {it <= ' '}
//        set(scriptParameters) = myScriptParametersTextField!!.setText(scriptParameters)
//
//    override fun showCommandLineAfterwards(): Boolean {
//        return myShowCommandLineCheckbox!!.isSelected
//    }
//
//    override fun setShowCommandLineAfterwards(showCommandLineAfterwards: Boolean) {
//        myShowCommandLineCheckbox!!.isSelected = showCommandLineAfterwards
//    }
//
//    override fun emulateTerminal(): Boolean {
//        return myEmulateTerminalCheckbox!!.isSelected
//    }
//
//    override fun setEmulateTerminal(emulateTerminal: Boolean) {
//        myEmulateTerminalCheckbox!!.isSelected = emulateTerminal
//    }
//
//    override fun getAnchor(): JComponent {
//        return anchor!!
//    }
//
//    var isMultiprocessMode: Boolean
//        get() = false // PhiDebuggerOptionsProvider.getInstance(myProject).isAttachToSubprocess()
//        set(multiprocess) {}
//
//    override fun setAnchor(anchor: JComponent?) {
//        this.anchor = anchor
//        myScriptParametersLabel!!.anchor = anchor
//        myCommonOptionsForm.setAnchor(anchor)
//    }
//}
//
//class PhizdetsScriptCommandLineState(private val myConfig: PhizdetsRunConfiguration, env: ExecutionEnvironment) : PhizdetsCommandLineState(myConfig, env) {
//
//    fun execute(executor: Executor,
//                         processStarter: PhizdetsProcessStarter,
//                         vararg patchers: CommandLinePatcher): ExecutionResult {
//        if (myConfig.showCommandLineAfterwards() && !myConfig.emulateTerminal()) {
//            if (executor.id === DefaultDebugExecutor.EXECUTOR_ID) {
//                return super.execute(executor, processStarter, ArrayUtil.append(patchers, object : CommandLinePatcher() {
//                    fun patchCommandLine(commandLine: GeneralCommandLine) {
//                        commandLine.parametersList.getParamsGroup(PhizdetsCommandLineState.GROUP_DEBUGGER)!!.addParameterAt(1, "--cmd-line")
//                    }
//                }))
//            }
//
//            val runner = PhizdetsScriptWithConsoleRunner(myConfig.getProject(), myConfig.getSdk(), PhiConsoleType.Phizdets, myConfig.getWorkingDirectory(),
//                                                       myConfig.getEnvs(), patchers,
//                                                       PhiConsoleOptions.getInstance(myConfig.getProject()).getPhizdetsConsoleSettings())
//
//            runner.setEnableAfterConnection(false)
//            runner.runSync()
//            // runner.getProcessHandler() would be null if execution error occurred
//            if (runner.getProcessHandler() == null) {
//                return null
//            }
//            runner.getPhidevConsoleCommunication().setConsoleView(runner.getConsoleView())
//            val actions = Lists.newArrayList(createActions(runner.getConsoleView(), runner.getProcessHandler()))
//            actions.add(ShowVarsAction(runner.getConsoleView(), runner.getPhidevConsoleCommunication()))
//
//            return DefaultExecutionResult(runner.getConsoleView(), runner.getProcessHandler(), actions.toTypedArray())
//        } else if (myConfig.emulateTerminal()) {
//            setRunWithPty(true)
//
//            val processHandler = startProcess(processStarter, patchers)
//
//            val executeConsole = TerminalExecutionConsole(myConfig.getProject(), processHandler)
//
//            executeConsole.addMessageFilter(myConfig.getProject(), PhizdetsTracebackFilter(myConfig.getProject()))
//            executeConsole.addMessageFilter(myConfig.getProject(), UrlFilter())
//
//            processHandler.startNotify()
//
//            return DefaultExecutionResult(executeConsole, processHandler, *AnAction.EMPTY_ARRAY)
//        } else {
//            return super.execute(executor, processStarter, patchers)
//        }
//    }
//
//    fun customizeEnvironmentVars(envs: MutableMap<String, String>, passParentEnvs: Boolean) {
//        if (myConfig.emulateTerminal()) {
//            if (!SystemInfo.isWindows) {
//                envs.put("TERM", "xterm-256color")
//            }
//        }
//    }
//
//    @Throws(ExecutionException::class)
//    protected fun doCreateProcess(commandLine: GeneralCommandLine): ProcessHandler {
//        if (myConfig.emulateTerminal()) {
//            return object : OSProcessHandler(commandLine) {
//                override fun readerOptions(): BaseOutputReader.Options {
//                    return object : BaseOutputReader.Options() {
//                        override fun policy(): BaseDataReader.SleepingPolicy {
//                            return BaseDataReader.SleepingPolicy.BLOCKING
//                        }
//
//                        override fun splitToLines(): Boolean {
//                            return false
//                        }
//
//                        override fun withSeparators(): Boolean {
//                            return true
//                        }
//                    }
//                }
//            }
//        } else {
//            return super.doCreateProcess(commandLine)
//        }
//    }
//
//    protected fun buildCommandLineParameters(commandLine: GeneralCommandLine) {
//        val parametersList = commandLine.parametersList
//        val exe_options = parametersList.getParamsGroup(GROUP_EXE_OPTIONS)!!
//        exe_options.addParametersString(myConfig.getInterpreterOptions())
//
//        val script_parameters = parametersList.getParamsGroup(GROUP_SCRIPT)!!
//        if (!StringUtil.isEmptyOrSpaces(myConfig.getScriptName())) {
//            script_parameters.addParameter(myConfig.getScriptName())
//        }
//
//        val script_options_string = myConfig.getScriptParameters()
//        if (script_options_string != null) script_parameters.addParametersString(script_options_string!!)
//
//        if (!StringUtil.isEmptyOrSpaces(myConfig.getWorkingDirectory())) {
//            commandLine.setWorkDirectory(myConfig.getWorkingDirectory())
//        }
//    }
//
//    /**
//     * @author traff
//     */
//    inner class PhizdetsScriptWithConsoleRunner(project: Project,
//                                              sdk: Sdk,
//                                              consoleType: PhiConsoleType,
//                                              workingDir: String?,
//                                              environmentVariables: Map<String, String>,
//                                              private val myPatchers: Array<CommandLinePatcher>,
//                                              consoleSettings: PhiConsoleOptions.PhiConsoleSettings,
//                                              vararg statementsToExecute: String) : PhidevConsoleRunnerImpl(project, sdk, consoleType, workingDir, environmentVariables, consoleSettings, {s ->}, statementsToExecute) {
//        protected val runnerFileFromHelpers = "pydev/pydev_run_in_console.py"
//
//        protected fun createContentDescriptorAndActions() {
//            val a = ConsoleExecuteAction(super.getConsoleView(), myConsoleExecuteActionHandler,
//                                         myConsoleExecuteActionHandler.getEmptyExecuteAction(), myConsoleExecuteActionHandler)
//            registerActionShortcuts(Lists.newArrayList<AnAction>(a), getConsoleView().getConsoleEditor().getComponent())
//        }
//
//        protected fun createCommandLine(sdk: Sdk,
//                                        environmentVariables: Map<String, String>,
//                                        workingDir: String, ports: IntArray): GeneralCommandLine {
//            val consoleCmdLine = doCreateConsoleCmdLine(sdk, environmentVariables, workingDir, ports, PhizdetsHelper.RUN_IN_CONSOLE)
//
//            val cmd = generateCommandLine(myPatchers)
//
//            val group = consoleCmdLine.getParametersList().getParamsGroup(PhizdetsCommandLineState.GROUP_SCRIPT)!!
//            group!!.addParameters(cmd.getParametersList().getList())
//
//            PhizdetsEnvUtil.mergePhizdetsPath(consoleCmdLine.getEnvironment(), cmd.getEnvironment())
//
//            consoleCmdLine.getEnvironment().putAll(cmd.getEnvironment())
//
//            return consoleCmdLine
//        }
//    }
//}
//
//abstract class PhizdetsCommandLineState(private val myConfig: AbstractPhizdetsRunConfiguration, env: ExecutionEnvironment) : CommandLineState(env) {
//
//    private var myMultiprocessDebug: Boolean? = null
//    var isRunWithPty = PtyCommandLine.isEnabled()
//
//    val isDebug: Boolean
//        get() = PhiDebugRunner.PHI_DEBUG_RUNNER.equals(environment.runner.runnerId)
//
//    val sdkFlavor: PhizdetsSdkFlavor?
//        get() = PhizdetsSdkFlavor.getFlavor(myConfig.getInterpreterPath())
//
//    val sdk: Sdk?
//        get() = myConfig.getSdk()
//
//    @Throws(ExecutionException::class)
//    override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
//        return execute(executor, *null as Array<CommandLinePatcher>?)
//    }
//
//    @Throws(ExecutionException::class)
//    fun execute(executor: Executor, vararg patchers: CommandLinePatcher): ExecutionResult {
//        return execute(executor, defaultPhizdetsProcessStarter, *patchers)
//    }
//
//    @Throws(ExecutionException::class)
//    fun execute(executor: Executor,
//                processStarter: PhizdetsProcessStarter,
//                vararg patchers: CommandLinePatcher): ExecutionResult {
//        val processHandler = startProcess(processStarter, *patchers)
//        val console = createAndAttachConsole(myConfig.getProject(), processHandler, executor)
//        return DefaultExecutionResult(console, processHandler, *createActions(console, processHandler))
//    }
//
//    @Throws(ExecutionException::class)
//    protected fun createAndAttachConsole(project: Project, processHandler: ProcessHandler, executor: Executor): ConsoleView {
//        val consoleView = createConsoleBuilder(project).console
//        consoleView.addMessageFilter(createUrlFilter(processHandler))
//
//        addTracebackFilter(project, consoleView, processHandler)
//
//        consoleView.attachToProcess(processHandler)
//        return consoleView
//    }
//
//    protected fun addTracebackFilter(project: Project, consoleView: ConsoleView, processHandler: ProcessHandler) {
//        if (PhiSdkUtil.isRemote(myConfig.getSdk())) {
//            assert(processHandler is RemoteProcessControl)
//            consoleView
//                .addMessageFilter(PhiRemoteTracebackFilter(project, myConfig.getWorkingDirectory(), processHandler as RemoteProcessControl))
//        } else {
//            consoleView.addMessageFilter(PhizdetsTracebackFilter(project, myConfig.getWorkingDirectorySafe()))
//        }
//        consoleView.addMessageFilter(createUrlFilter(processHandler)) // Url filter is always nice to have
//    }
//
//    private fun createConsoleBuilder(project: Project): TextConsoleBuilder {
//        if (isDebug) {
//            return PhiDebugConsoleBuilder(project, PhizdetsSdkType.findSdkByPath(myConfig.getInterpreterPath()))
//        } else {
//            return TextConsoleBuilderFactory.getInstance().createBuilder(project)
//        }
//    }
//
//    @Throws(ExecutionException::class)
//    override fun startProcess(): ProcessHandler {
//        return startProcess(defaultPhizdetsProcessStarter)
//    }
//
//    /**
//     * Patches the command line parameters applying patchers from first to last, and then runs it.
//
//     * @param patchers any number of patchers; any patcher may be null, and the whole argument may be null.
//     * *
//     * @return handler of the started process
//     * *
//     * @throws ExecutionException
//     * *
//     */
//    @Deprecated("")
//    @Deprecated("use {@link #startProcess(PhizdetsProcessStarter, CommandLinePatcher...)} instead")
//    @Throws(ExecutionException::class)
//    protected fun startProcess(vararg patchers: CommandLinePatcher): ProcessHandler {
//        return startProcess(defaultPhizdetsProcessStarter, *patchers)
//    }
//
//    /**
//     * Patches the command line parameters applying patchers from first to last, and then runs it.
//
//     * @param processStarter
//     * *
//     * @param patchers any number of patchers; any patcher may be null, and the whole argument may be null.
//     * *
//     * @return handler of the started process
//     * *
//     * @throws ExecutionException
//     */
//    @Throws(ExecutionException::class)
//    protected fun startProcess(processStarter: PhizdetsProcessStarter, vararg patchers: CommandLinePatcher): ProcessHandler {
//        val commandLine = generateCommandLine(patchers)
//
//        // Extend command line
//        PhizdetsRunConfigurationExtensionsManager.getInstance()
//            .patchCommandLine(myConfig, runnerSettings, commandLine, environment.runner.runnerId)
//
//        val processHandler = processStarter.start(myConfig, commandLine)
//
//        // attach extensions
//        PhizdetsRunConfigurationExtensionsManager.getInstance().attachExtensionsToProcess(myConfig, processHandler, runnerSettings)
//
//        return processHandler
//    }
//
//    protected val defaultPhizdetsProcessStarter: PhizdetsProcessStarter
//        get() = {config, commandLine ->
//            val sdk = PhizdetsSdkType.findSdkByPath(myConfig.getInterpreterPath())
//            val processHandler: ProcessHandler
//            if (PhiSdkUtil.isRemote(sdk)) {
//                val pathMapper = createRemotePathMapper()
//                processHandler = createRemoteProcessStarter().startRemoteProcess(sdk, commandLine, myConfig.getProject(), pathMapper)
//            } else {
//                EncodingEnvironmentUtil.setLocaleEnvironmentIfMac(commandLine)
//                processHandler = doCreateProcess(commandLine)
//                ProcessTerminatedListener.attach(processHandler)
//            }
//            processHandler
//        }
//
//    private fun createRemotePathMapper(): PhiRemotePathMapper? {
//        if (myConfig.getMappingSettings() == null) {
//            return null
//        } else {
//            return PhiRemotePathMapper.fromSettings(myConfig.getMappingSettings(), PhiRemotePathMapper.PhiPathMappingType.USER_DEFINED)
//        }
//    }
//
//    protected fun createRemoteProcessStarter(): PhiRemoteProcessStarter {
//        return PhiRemoteProcessStarter()
//    }
//
//
//    fun generateCommandLine(patchers: Array<CommandLinePatcher>?): GeneralCommandLine {
//        val commandLine = generateCommandLine()
//        if (patchers != null) {
//            for (patcher in patchers) {
//                patcher?.patchCommandLine(commandLine)
//            }
//        }
//        return commandLine
//    }
//
//    @Throws(ExecutionException::class)
//    protected fun doCreateProcess(commandLine: GeneralCommandLine): ProcessHandler {
//        return PhizdetsProcessRunner.createProcess(commandLine)
//    }
//
//    fun generateCommandLine(): GeneralCommandLine {
//        val commandLine = createPhizdetsCommandLine(myConfig.getProject(), myConfig, isDebug, isRunWithPty)
//
//        buildCommandLineParameters(commandLine)
//
//        customizeEnvironmentVars(commandLine.environment, myConfig.isPassParentEnvs())
//
//        return commandLine
//    }
//
//    fun customizeEnvironmentVars(envs: Map<String, String>, passParentEnvs: Boolean) {}
//
//    protected val interpreterPath: String
//        @Throws(ExecutionException::class)
//        get() {
//            val interpreterPath = myConfig.getInterpreterPath() ?: throw ExecutionException("Cannot find Phizdets interpreter for this run configuration")
//            return interpreterPath
//        }
//
//    protected fun buildCommandLineParameters(commandLine: GeneralCommandLine) {}
//
//    var isMultiprocessDebug: Boolean
//        get() {
//            if (myMultiprocessDebug != null) {
//                return myMultiprocessDebug!!
//            } else {
//                return PhiDebuggerOptionsProvider.getInstance(myConfig.getProject()).isAttachToSubprocess()
//            }
//        }
//        set(multiprocessDebug) {
//            myMultiprocessDebug = multiprocessDebug
//        }
//
//    protected fun createUrlFilter(handler: ProcessHandler): UrlFilter {
//        return UrlFilter()
//    }
//
//    interface PhizdetsProcessStarter {
//        @Throws(ExecutionException::class)
//        fun start(config: AbstractPhizdetsRunConfiguration,
//                  commandLine: GeneralCommandLine): ProcessHandler
//    }
//
//    companion object {
//        private val LOG = Logger.getInstance("#vgrechka.phizdetsidea.PhizdetsCommandLineState")
//
//        // command line has a number of fixed groups of parameters; patchers should only operate on them and not the raw list.
//
//        val GROUP_EXE_OPTIONS = "Exe Options"
//        val GROUP_DEBUGGER = "Debugger"
//        val GROUP_PROFILER = "Profiler"
//        val GROUP_COVERAGE = "Coverage"
//        val GROUP_SCRIPT = "Script"
//
//        @Throws(ExecutionException::class)
//        fun createServerSocket(): ServerSocket {
//            val serverSocket: ServerSocket
//            try {
//
//                serverSocket = ServerSocket(0)
//            } catch (e: IOException) {
//                throw ExecutionException("Failed to find free socket port", e)
//            }
//
//            return serverSocket
//        }
//
//        fun createPhizdetsCommandLine(project: Project, config: PhizdetsRunParams, isDebug: Boolean, runWithPty: Boolean): GeneralCommandLine {
//            val commandLine = generalCommandLine(runWithPty)
//
//            commandLine.withCharset(EncodingProjectManager.getInstance(project).defaultCharset)
//
//            createStandardGroups(commandLine)
//
//            initEnvironment(project, commandLine, config, isDebug)
//
//            setRunnerPath(project, commandLine, config)
//
//            return commandLine
//        }
//
//        private fun generalCommandLine(runWithPty: Boolean): GeneralCommandLine {
//            return if (runWithPty) PtyCommandLine() else GeneralCommandLine()
//        }
//
//        /**
//         * Creates a number of parameter groups in the command line:
//         * GROUP_EXE_OPTIONS, GROUP_DEBUGGER, GROUP_SCRIPT.
//         * These are necessary for command line patchers to work properly.
//
//         * @param commandLine
//         */
//        fun createStandardGroups(commandLine: GeneralCommandLine) {
//            val params = commandLine.parametersList
//            params.addParamsGroup(GROUP_EXE_OPTIONS)
//            params.addParamsGroup(GROUP_DEBUGGER)
//            params.addParamsGroup(GROUP_PROFILER)
//            params.addParamsGroup(GROUP_COVERAGE)
//            params.addParamsGroup(GROUP_SCRIPT)
//        }
//
//        protected fun initEnvironment(project: Project, commandLine: GeneralCommandLine, myConfig: PhizdetsRunParams, isDebug: Boolean) {
//            val env = Maps.newHashMap<String, String>()
//
//            setupEncodingEnvs(env, commandLine.charset)
//
//            if (myConfig.getEnvs() != null) {
//                env.putAll(myConfig.getEnvs())
//            }
//
//            addCommonEnvironmentVariables(getInterpreterPath(project, myConfig), env)
//
//            setupVirtualEnvVariables(myConfig, env, myConfig.getSdkHome())
//
//            commandLine.environment.clear()
//            commandLine.environment.putAll(env)
//            commandLine.withParentEnvironmentType(if (myConfig.isPassParentEnvs()) GeneralCommandLine.ParentEnvironmentType.CONSOLE else GeneralCommandLine.ParentEnvironmentType.NONE)
//
//
//            buildPhizdetsPath(project, commandLine, myConfig, isDebug)
//        }
//
//        private fun setupVirtualEnvVariables(myConfig: PhizdetsRunParams, env: MutableMap<String, String>, sdkHome: String) {
//            if (PhizdetsSdkType.isVirtualEnv(sdkHome)) {
//                val reader = PhiVirtualEnvReader(sdkHome)
//                if (reader.getActivate() != null) {
//                    try {
//                        env.putAll(reader.readShellEnv().entrySet().stream().filter({entry -> PhiVirtualEnvReader.Companion.getVirtualEnvVars().contains(entry.getKey())}
//                        ).collect(Collectors.toMap<T, K, U>(Function<T, K> {it.key}, Function<T, U> {it.value})))
//
//                        for (e in myConfig.getEnvs().entrySet()) {
//                            if ("PATH" == e.key) {
//                                env.put(e.key, PhizdetsEnvUtil.appendToPathEnvVar(env["PATH"], e.value))
//                            } else {
//                                env.put(e.key, e.value)
//                            }
//                        }
//                    } catch (e: Exception) {
//                        LOG.error("Couldn't read virtualenv variables", e)
//                    }
//
//                }
//            }
//        }
//
//        protected fun addCommonEnvironmentVariables(homePath: String?, env: MutableMap<String, String>) {
//            PhizdetsEnvUtil.setPhizdetsUnbuffered(env)
//            if (homePath != null) {
//                PhizdetsEnvUtil.resetHomePathChanges(homePath, env)
//            }
//            env.put("PHIZDA_HOSTED", "1")
//        }
//
//        private fun setupEncodingEnvs(envs: Map<String, String>, charset: Charset) {
//            PhizdetsSdkFlavor.setupEncodingEnvs(envs, charset)
//        }
//
//        private fun buildPhizdetsPath(project: Project, commandLine: GeneralCommandLine, config: PhizdetsRunParams, isDebug: Boolean) {
//            val phizdetsSdk = PhizdetsSdkType.findSdkByPath(config.getSdkHome())
//            if (phizdetsSdk != null) {
//                val pathList = Lists.newArrayList(getAddedPaths(phizdetsSdk))
//                pathList.addAll(collectPhizdetsPath(project, config, isDebug))
//                initPhizdetsPath(commandLine, config.isPassParentEnvs(), pathList, config.getSdkHome())
//            }
//        }
//
//        fun initPhizdetsPath(commandLine: GeneralCommandLine,
//                           passParentEnvs: Boolean,
//                           pathList: List<String>,
//                           interpreterPath: String) {
//            val flavor = PhizdetsSdkFlavor.getFlavor(interpreterPath)
//            if (flavor != null) {
//                flavor!!.initPhizdetsPath(commandLine, pathList)
//            } else {
//                PhizdetsSdkFlavor.initPhizdetsPath(commandLine.environment, passParentEnvs, pathList)
//            }
//        }
//
//        fun getAddedPaths(phizdetsSdk: Sdk): List<String> {
//            val pathList = ArrayList<String>()
//            val sdkAdditionalData = phizdetsSdk.sdkAdditionalData
//            if (sdkAdditionalData is PhizdetsSdkAdditionalData) {
//                val addedPaths = (sdkAdditionalData as PhizdetsSdkAdditionalData).getAddedPathFiles()
//                for (file in addedPaths) {
//                    addToPhizdetsPath(file, pathList)
//                }
//            }
//            return pathList
//        }
//
//        private fun addToPhizdetsPath(file: VirtualFile, pathList: MutableCollection<String>) {
//            if (file.fileSystem is JarFileSystem) {
//                val realFile = JarFileSystem.getInstance().getVirtualFileForJar(file)
//                if (realFile != null) {
//                    addIfNeeded(realFile, pathList)
//                }
//            } else {
//                addIfNeeded(file, pathList)
//            }
//        }
//
//        private fun addIfNeeded(file: VirtualFile, pathList: MutableCollection<String>) {
//            addIfNeeded(pathList, file.path)
//        }
//
//        protected fun addIfNeeded(pathList: MutableCollection<String>, path: String) {
//            val vals = Sets.newHashSet(pathList)
//            val filePath = FileUtil.toSystemDependentName(path)
//            if (!vals.contains(filePath)) {
//                pathList.add(filePath)
//            }
//        }
//
//        protected fun collectPhizdetsPath(project: Project, config: PhizdetsRunParams, isDebug: Boolean): Collection<String> {
//            val module = getModule(project, config)
//            val phizdetsPath = Sets.newHashSet(collectPhizdetsPath(module, config.shouldAddContentRoots(), config.shouldAddSourceRoots()))
//
//            if (isDebug && PhizdetsSdkFlavor.getFlavor(config.getSdkHome()) is JythonSdkFlavor) {
//                phizdetsPath.add(PhizdetsHelpersLocator.getHelperPath("phizda"))
//                phizdetsPath.add(PhizdetsHelpersLocator.getHelperPath("phizdev"))
//            }
//
//            return phizdetsPath
//        }
//
//        private fun getModule(project: Project, config: PhizdetsRunParams): Module? {
//            val name = config.getModuleName()
//            return if (StringUtil.isEmpty(name)) null else ModuleManager.getInstance(project).findModuleByName(name)
//        }
//
//        @JvmOverloads fun collectPhizdetsPath(module: Module?, addContentRoots: Boolean = true,
//                                            addSourceRoots: Boolean = true): Collection<String> {
//            val phizdetsPathList = Sets.newLinkedHashSet<String>()
//            if (module != null) {
//                val dependencies = HashSet<Module>()
//                ModuleUtilCore.getDependencies(module, dependencies)
//
//                if (addContentRoots) {
//                    addRoots(phizdetsPathList, ModuleRootManager.getInstance(module).contentRoots)
//                    for (dependency in dependencies) {
//                        addRoots(phizdetsPathList, ModuleRootManager.getInstance(dependency).contentRoots)
//                    }
//                }
//                if (addSourceRoots) {
//                    addRoots(phizdetsPathList, ModuleRootManager.getInstance(module).sourceRoots)
//                    for (dependency in dependencies) {
//                        addRoots(phizdetsPathList, ModuleRootManager.getInstance(dependency).sourceRoots)
//                    }
//                }
//
//                addLibrariesFromModule(module, phizdetsPathList)
//                addRootsFromModule(module, phizdetsPathList)
//                for (dependency in dependencies) {
//                    addLibrariesFromModule(dependency, phizdetsPathList)
//                    addRootsFromModule(dependency, phizdetsPathList)
//                }
//            }
//            return phizdetsPathList
//        }
//
//        private fun addLibrariesFromModule(module: Module, list: MutableCollection<String>) {
//            val entries = ModuleRootManager.getInstance(module).orderEntries
//            for (entry in entries) {
//                if (entry is LibraryOrderEntry) {
//                    val name = entry.libraryName
//                    if (name != null && name.endsWith(LibraryContributingFacet.PHIZDETS_FACET_LIBRARY_NAME_SUFFIX)) {
//                        // skip libraries from Phizdets facet
//                        continue
//                    }
//                    for (root in entry.getRootFiles(OrderRootType.CLASSES)) {
//                        val library = entry.library
//                        if (!PlatformUtils.isPhizda()) {
//                            addToPhizdetsPath(root, list)
//                        } else if (library is LibraryImpl) {
//                            val kind = library.kind
//                            if (kind === PhizdetsLibraryType.getInstance().getKind()) {
//                                addToPhizdetsPath(root, list)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        private fun addRootsFromModule(module: Module, phizdetsPathList: MutableCollection<String>) {
//
//            // for Jython
//            val extension = CompilerModuleExtension.getInstance(module)
//            if (extension != null) {
//                val path = extension.compilerOutputPath
//                if (path != null) {
//                    phizdetsPathList.add(path.path)
//                }
//                val pathForTests = extension.compilerOutputPathForTests
//                if (pathForTests != null) {
//                    phizdetsPathList.add(pathForTests.path)
//                }
//            }
//
//            //additional paths from facets (f.e. buildout)
//            val facets = FacetManager.getInstance(module).allFacets
//            for (facet in facets) {
//                if (facet is PhizdetsPathContributingFacet) {
//                    val more_paths = (facet as PhizdetsPathContributingFacet).getAdditionalPhizdetsPath()
//                    if (more_paths != null) phizdetsPathList.addAll(more_paths!!)
//                }
//            }
//        }
//
//        private fun addRoots(phizdetsPathList: MutableCollection<String>, roots: Array<VirtualFile>) {
//            for (root in roots) {
//                addToPhizdetsPath(root, phizdetsPathList)
//            }
//        }
//
//        protected fun setRunnerPath(project: Project, commandLine: GeneralCommandLine, config: PhizdetsRunParams) {
//            val interpreterPath = getInterpreterPath(project, config)
//            if (StringUtil.isNotEmpty(interpreterPath)) {
//                commandLine.exePath = FileUtil.toSystemDependentName(interpreterPath!!)
//            }
//        }
//
//        fun getInterpreterPath(project: Project, config: PhizdetsRunParams): String? {
//            var sdkHome = config.getSdkHome()
//            if (config.isUseModuleSdk() || StringUtil.isEmpty(sdkHome)) {
//                val module = getModule(project, config)
//
//                val sdk = PhizdetsSdkType.findPhizdetsSdk(module)
//
//                if (sdk != null) {
//                    sdkHome = sdk!!.getHomePath()
//                }
//            }
//
//            return sdkHome
//        }
//    }
//}
//
//object PhiBundle {
//
//    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
//        return CommonBundle.message(bundle, key, *params)
//    }
//
//    fun messageOfNull(@PropertyKey(resourceBundle = BUNDLE) key: String,
//                      vararg params: Any): String? {
//        return CommonBundle.messageOfNull(bundle, key, *params)
//    }
//
//    private var ourBundle: Reference<ResourceBundle>? = null
//    @NonNls
//    private const val BUNDLE = "vgrechka.phizdetsidea.PhiBundle"
//
//    // Cached loading
//    private val bundle: ResourceBundle
//        get() {
//            var bundle = SoftReference.dereference(ourBundle)
//            if (bundle == null) {
//                bundle = ResourceBundle.getBundle(BUNDLE)
//                ourBundle = SoftReference<ResourceBundle>(bundle)
//            }
//            return bundle!!
//        }
//}
//
//class PhizdetsExtendedConfigurationEditor<T : AbstractPhizdetsRunConfiguration<T>>(private val myMainSettingsEditor: SettingsEditor<T>) : SettingsEditor<T>() {
//
//    private var myCurrentEditor: PyRunConfigurationEditorExtension? = null
//    private var myCurrentSettingsEditor: SettingsEditor<AbstractPhizdetsRunConfiguration>? = null
//    private var myCurrentSettingsEditorComponent: JComponent? = null
//
//    private var mySettingsPlaceholder: JPanel? = null
//
//    init {
//
//        Disposer.register(this, myMainSettingsEditor)
//    }
//
//    override fun resetEditorFrom(s: T) {
//        myMainSettingsEditor.resetFrom(s)
//        updateCurrentEditor(s)
//        if (myCurrentSettingsEditor != null) {
//            myCurrentSettingsEditor!!.resetFrom(s)
//        }
//    }
//
//    @Throws(ConfigurationException::class)
//    override fun applyEditorTo(s: T) {
//        myMainSettingsEditor.applyTo(s)
//        val updated = updateCurrentEditor(s)
//        if (myCurrentSettingsEditor != null) {
//            if (updated) {
//                myCurrentSettingsEditor!!.resetFrom(s)
//            } else {
//                myCurrentSettingsEditor!!.applyTo(s)
//            }
//        }
//    }
//
//    private fun updateCurrentEditor(s: T): Boolean {
//        val newEditor = PyRunConfigurationEditorExtension.Factory.getExtension(s)
//        if (myCurrentEditor !== newEditor) {
//            // discard previous
//            if (myCurrentSettingsEditorComponent != null) {
//                mySettingsPlaceholder!!.remove(myCurrentSettingsEditorComponent!!)
//            }
//            if (myCurrentSettingsEditor != null) {
//                Disposer.dispose(myCurrentSettingsEditor!!)
//            }
//            // set current editor
//            myCurrentEditor = newEditor
//            myCurrentSettingsEditor = null
//            // add new
//            if (newEditor != null) {
//                myCurrentSettingsEditor = newEditor!!.createEditor(s)
//                myCurrentSettingsEditorComponent = myCurrentSettingsEditor!!.getComponent()
//                val constraints = GridBagConstraints()
//                constraints.gridy = 1
//                constraints.fill = GridBagConstraints.BOTH
//                constraints.weightx = 1.0
//                constraints.insets.top = 10
//                mySettingsPlaceholder!!.add(myCurrentSettingsEditorComponent!!, constraints)
//
//                Disposer.register(this, myCurrentSettingsEditor!!)
//            }
//            return true
//        }
//        return false
//    }
//
//    override fun createEditor(): JComponent {
//        val mainEditorComponent = myMainSettingsEditor.component
//        mySettingsPlaceholder = JPanel(GridBagLayout())
//        val constraints = GridBagConstraints()
//        constraints.fill = GridBagConstraints.BOTH
//        constraints.weightx = 1.0
//        mySettingsPlaceholder!!.add(mainEditorComponent, constraints)
//        return mySettingsPlaceholder
//    }
//
//    companion object {
//
//        fun <T : AbstractPhizdetsRunConfiguration<T>> create(editor: SettingsEditor<T>): PhizdetsExtendedConfigurationEditor<T> {
//            return PhizdetsExtendedConfigurationEditor(editor)
//        }
//    }
//}
//
//
//class PhizdetsRunConfigurationExtensionsManager : RunConfigurationExtensionsManager<AbstractPhizdetsRunConfiguration, PhizdetsRunConfigurationExtension>(PhizdetsRunConfigurationExtension.EP_NAME) {
//    companion object {
//
//        val instance: PhizdetsRunConfigurationExtensionsManager
//            get() = ServiceManager.getService(PhizdetsRunConfigurationExtensionsManager::class.java)
//    }
//}
//
//class PhizdetsSdkType private constructor() : SdkType("Phizdets SDK") {
//
//    companion object {
//        fun getInstance(): PhizdetsSdkType {
//            return SdkType.findInstance<PhizdetsSdkType>(PhizdetsSdkType::class.java)
//        }
//
//        fun findPhizdetsSdk(module: Module?): Sdk? {
//            if (module == null) return null
//            val sdk = ModuleRootManager.getInstance(module).sdk
//            if (sdk != null && sdk.sdkType is PhizdetsSdkType) return sdk
//            val facets = FacetManager.getInstance(module).allFacets
//            for (facet in facets) {
//                val configuration = facet.configuration
//                if (configuration is PhizdetsFacetSettings) {
//                    return (configuration as PhizdetsFacetSettings).getSdk()
//                }
//            }
//            return null
//        }
//
//        fun findSdkByPath(path: String?): Sdk? {
//            if (path != null) {
//                return findSdkByPath(getAllSdks(), path)
//            }
//            return null
//        }
//
//        fun findSdkByPath(sdkList: List<Sdk>, path: String?): Sdk? {
//            if (path != null) {
//                for (sdk in sdkList) {
//                    if (sdk != null && FileUtil.pathsEqual(path, sdk.homePath)) {
//                        return sdk
//                    }
//                }
//            }
//            return null
//        }
//
//        fun getAllSdks(): List<Sdk> {
//            return ProjectJdkTable.getInstance().getSdksOfType(getInstance())
//        }
//    }
//
//    override fun getIcon(): Icon {
//        return PhizdetsIcons.Phizdets.Phizdets
//    }
//
//    override fun getHelpTopic(): String {
//        return "reference.project.structure.sdk.phizdets"
//    }
//
//    override fun getIconForAddAction(): Icon {
//        return PhizdetsIcons.Phizdets.Phizdets
////        return PhizdetsFileType.INSTANCE.getIcon()
//    }
//
//    @NonNls
//    override fun suggestHomePath(): String? {
//        imf("254be4c2-bab9-4e61-afad-87dfd6718c59")
//    }
//
//    override fun suggestHomePaths(): Collection<String> {
//        imf("63203029-7bc3-49cc-8991-1612a39e6b3e")
//    }
//
//    override fun isValidSdkHome(path: String?): Boolean {
//        imf("624c1f37-2e04-45c7-94b0-7ec024ec987a")
//    }
//
//    override fun getHomeChooserDescriptor(): FileChooserDescriptor {
//        imf("cb1ddcd4-87aa-4870-97bd-905248a01688")
//    }
//
//    override fun supportsCustomCreateUI(): Boolean {
//        return true
//    }
//
//    override fun showCustomCreateUI(sdkModel: SdkModel,
//                                    parentComponent: JComponent,
//                                    sdkCreatedCallback: Consumer<Sdk>) {
//        imf("4922ea46-4081-43e3-b1a3-bd024ea05ce6")
//    }
//
//    override fun suggestSdkName(currentSdkName: String, sdkHome: String): String {
//        imf("efd38007-940e-473e-af90-157c3ae016bc")
//    }
//
//    override fun createAdditionalDataConfigurable(sdkModel: SdkModel,
//                                                  sdkModificator: SdkModificator): AdditionalDataConfigurable? {
//        return null
//    }
//
//    override fun saveAdditionalData(additionalData: SdkAdditionalData, additional: Element) {
//        imf("29d6443d-4e47-4a05-a359-49d023482567")
//    }
//
//    override fun loadAdditionalData(currentSdk: Sdk, additional: Element): SdkAdditionalData? {
//        imf("4704bb43-c715-4b7a-9073-4fb1255889ab")
//    }
//
//    @NonNls
//    override fun getPresentableName(): String {
//        return "Phizdets SDK"
//    }
//
//    override fun sdkPath(homePath: VirtualFile): String {
//        imf("dd1f3cf6-ff34-4a69-8457-70a1755a188f")
//    }
//
//    override fun setupSdkPaths(sdk: Sdk) {
//        imf("213abc20-7d0a-485c-8d57-b85727ffb4b9")
//    }
//
//    override fun setupSdkPaths(sdk: Sdk, sdkModel: SdkModel): Boolean {
//        return true  // run setupSdkPaths only once (from PhizdetsSdkDetailsStep). Skip this from showCustomCreateUI
//    }
//
//    override fun getVersionString(sdk: Sdk): String? {
//        imf("4ff535f3-5350-4a92-b0e8-a22adbd32ada")
//    }
//
//    override fun getVersionString(sdkHome: String?): String? {
//        imf("90ad37dd-d406-4dad-9ef9-d76a030aeb22")
//    }
//
//    override fun isRootTypeApplicable(type: OrderRootType): Boolean {
//        imf("8b6649e2-7924-4cd0-b3eb-d0977b85c620")
//    }
//
//    override fun sdkHasValidPath(sdk: Sdk): Boolean {
//        imf("eb441932-8ba5-4649-b2f7-67ccbc61587a")
//    }
//
//}
//
//object PhizdetsEnvUtil {
////    val PYTHONPATH = "PYTHONPATH"
////    val PYTHONUNBUFFERED = "PYTHONUNBUFFERED"
////    val PYTHONIOENCODING = "PYTHONIOENCODING"
////    val IPYTHONENABLE = "IPYTHONENABLE"
////    val PYTHONDONTWRITEBYTECODE = "PYTHONDONTWRITEBYTECODE"
////    val PYVENV_LAUNCHER = "__PYVENV_LAUNCHER__"
////
////    fun setPhizdetsUnbuffered(env: MutableMap<String, String>): Map<String, String> {
////        env.put(PYTHONUNBUFFERED, "1")
////        return env
////    }
////
////    fun setPhizdetsIOEncoding(env: MutableMap<String, String>, encoding: String): Map<String, String> {
////        env.put(PYTHONIOENCODING, encoding)
////        return env
////    }
////
////    /**
////     * Resets the environment variables that affect the way the Phizdets interpreter searches for its settings and libraries.
////     */
////    fun resetHomePathChanges(homePath: String, env: MutableMap<String, String>): Map<String, String> {
////        if (System.getenv(PYVENV_LAUNCHER) != null || EnvironmentUtil.getEnvironmentMap().containsKey(PYVENV_LAUNCHER)) {
////            env.put(PYVENV_LAUNCHER, homePath)
////        }
////        return env
////    }
////
////    /**
////     * Appends a value to the end os a path-like environment variable, using system-dependent path separator.
////
////     * @param source path-like string to append to
////     * *
////     * @param value  what to append
////     * *
////     * @return modified path-like string
////     */
////    fun appendToPathEnvVar(source: String?, value: String): String {
////        if (StringUtil.isEmpty(source)) return value
////        val paths = Sets.newHashSet(*source!!.split(File.pathSeparator.toRegex()).dropLastWhile {it.isEmpty()}.toTypedArray())
////        return if (!paths.contains(value)) source + File.pathSeparator + value else source
////    }
////
////    fun addPathsToEnv(env: MutableMap<String, String>, key: String, values: Collection<String>) {
////        for (`val` in values) {
////            addPathToEnv(env, key, `val`)
////        }
////    }
////
////    fun addPathToEnv(env: MutableMap<String, String>, key: String, value: String) {
////        if (!StringUtil.isEmpty(value)) {
////            if (env.containsKey(key)) {
////                env.put(key, appendToPathEnvVar(env[key], value))
////            } else {
////                env.put(key, value)
////            }
////        }
////    }
////
////    fun addToPhizdetsPath(env: MutableMap<String, String>, values: Collection<String>) {
////        addPathsToEnv(env, PYTHONPATH, values)
////    }
////
////    fun addToPhizdetsPath(env: MutableMap<String, String>, value: String) {
////        addPathToEnv(env, PYTHONPATH, value)
////    }
////
////    fun mergePhizdetsPath(from: Map<String, String>, to: MutableMap<String, String>) {
////        val value = from[PYTHONPATH]
////        if (value != null) {
////            val paths = Sets.newHashSet(*value.split(File.pathSeparator.toRegex()).dropLastWhile {it.isEmpty()}.toTypedArray())
////            addToPhizdetsPath(to, paths)
////        }
////    }
////
////    fun setPhizdetsDontWriteBytecode(env: MutableMap<String, String>): Map<String, String> {
////        env.put(PYTHONDONTWRITEBYTECODE, "1")
////        return env
////    }
//}
//
//interface AbstractPhiCommonOptionsForm : AbstractPhizdetsRunConfigurationParams, PanelWithAnchor {
//
//    val mainPanel: JComponent
//
//    fun subscribe()
//
//    fun addInterpreterComboBoxActionListener(listener: ActionListener)
//
//    fun removeInterpreterComboBoxActionListener(listener: ActionListener)
//
//    fun addInterpreterModeListener(listener: java.util.function.Consumer<Boolean>)
//
//    companion object {
//        @NonNls val EXPAND_PROPERTY_KEY = "ExpandEnvironmentPanel"
//    }
//}
//
//abstract class PhiCommonOptionsFormFactory {
//
//    abstract fun createForm(data: PhiCommonOptionsFormData): AbstractPhiCommonOptionsForm
//
//    companion object {
//        val instance: PhiCommonOptionsFormFactory
//            get() = ServiceManager.getService(PhiCommonOptionsFormFactory::class.java)
//    }
//}








