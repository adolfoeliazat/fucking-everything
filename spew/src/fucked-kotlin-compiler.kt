package vgrechka.spew

import org.jetbrains.kotlin.config.CompilerConfiguration
import com.google.common.base.Predicates.`in`
import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.ExitCode.*
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.jvm.PluginCliParser
import org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot
import org.jetbrains.kotlin.cli.jvm.config.addJavaSourceRoot
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.repl.ReplFromTerminal
import org.jetbrains.kotlin.codegen.CompilationException
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.PluginCliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.cliPluginUsageString
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.load.kotlin.JvmMetadataVersion
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCompilationComponents
import org.jetbrains.kotlin.script.KotlinScriptDefinitionFromAnnotatedTemplate
import org.jetbrains.kotlin.script.StandardScriptDefinition
import org.jetbrains.kotlin.util.PerformanceCounter
import org.jetbrains.kotlin.utils.KotlinPaths
import org.jetbrains.kotlin.utils.KotlinPathsFromHomeDir
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File
import java.lang.management.ManagementFactory
import java.net.URLClassLoader
import java.util.*
import java.util.concurrent.TimeUnit
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.JarUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiModificationTrackerImpl
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.asJava.FilteredJvmDiagnostics
import org.jetbrains.kotlin.backend.common.output.OutputFileCollection
import org.jetbrains.kotlin.backend.common.output.SimpleOutputFileCollection
import org.jetbrains.kotlin.cli.common.checkKotlinPackageUsage
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.common.output.outputUtils.writeAll
import org.jetbrains.kotlin.cli.jvm.compiler.*
import org.jetbrains.kotlin.cli.jvm.config.*
import org.jetbrains.kotlin.codegen.ClassBuilderFactories
import org.jetbrains.kotlin.codegen.CompilationErrorHandler
import org.jetbrains.kotlin.codegen.GeneratedClassLoader
import org.jetbrains.kotlin.codegen.KotlinCodegenFacade
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.codegen.state.GenerationStateEventCallback
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.addKotlinSourceRoots
import org.jetbrains.kotlin.fileClasses.JvmFileClassUtil
import org.jetbrains.kotlin.idea.MainFunctionDetector
import org.jetbrains.kotlin.load.kotlin.ModuleVisibilityManager
import org.jetbrains.kotlin.modules.Module
import org.jetbrains.kotlin.modules.TargetId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.progress.ProgressIndicatorAndCompilationCanceledStatus
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.utils.newLinkedHashMapWithExpectedSize
import org.jetbrains.kotlin.utils.tryConstructClassFromStringArgs
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.util.jar.Attributes


class EnoughFuckedCompiling(val analysisResult: AnalysisResult, val environment: KotlinCoreEnvironment) : RuntimeException() {
    companion object {
        var wasCreated = false
    }

    init {
        wasCreated = true
    }
}


class FuckedK2JVMCompiler : FuckedCLICompiler<K2JVMCompilerArguments>() {
    override fun doExecute(arguments: K2JVMCompilerArguments, configuration: CompilerConfiguration, rootDisposable: Disposable): ExitCode {
        val messageCollector = configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)

        val paths = if (arguments.kotlinHome != null)
            KotlinPathsFromHomeDir(File(arguments.kotlinHome))
        else
            PathUtil.getKotlinPathsForCompiler()

        messageCollector.report(CompilerMessageSeverity.LOGGING, "Using Kotlin home directory " + paths.homePath, CompilerMessageLocation.NO_LOCATION)
        PerformanceCounter.setTimeCounterEnabled(arguments.reportPerf)

        setupJdkClasspathRoots(arguments, configuration, messageCollector).let {
            if (it != OK) return it
        }

        try {
            PluginCliParser.loadPlugins(arguments, configuration)
        }
        catch (e: PluginCliOptionProcessingException) {
            val message = e.message + "\n\n" + cliPluginUsageString(e.pluginId, e.options)
            messageCollector.report(CompilerMessageSeverity.ERROR, message, CompilerMessageLocation.NO_LOCATION)
            return INTERNAL_ERROR
        }
        catch (e: CliOptionProcessingException) {
            messageCollector.report(CompilerMessageSeverity.ERROR, e.message!!, CompilerMessageLocation.NO_LOCATION)
            return INTERNAL_ERROR
        }
        catch (t: Throwable) {
            MessageCollectorUtil.reportException(messageCollector, t)
            return INTERNAL_ERROR
        }

        if (arguments.script) {
            if (arguments.freeArgs.isEmpty()) {
                messageCollector.report(
                    CompilerMessageSeverity.ERROR, "Specify script source path to evaluate", CompilerMessageLocation.NO_LOCATION
                )
                return COMPILATION_ERROR
            }
            configuration.addKotlinSourceRoot(arguments.freeArgs[0])
        }
        else if (arguments.module == null) {
            for (arg in arguments.freeArgs) {
                configuration.addKotlinSourceRoot(arg)
                val file = File(arg)
                if (file.isDirectory) {
                    configuration.addJavaSourceRoot(file)
                }
            }
        }

        val classpath = getClasspath(paths, arguments)
        configuration.addJvmClasspathRoots(classpath)

        configuration.put(CommonConfigurationKeys.MODULE_NAME, arguments.moduleName ?: JvmAbi.DEFAULT_MODULE_NAME)

        if (arguments.module == null && arguments.freeArgs.isEmpty() && !arguments.version) {
            ReplFromTerminal.run(rootDisposable, configuration)
            return ExitCode.OK
        }

        if (arguments.skipMetadataVersionCheck) {
            JvmMetadataVersion.skipCheck = true
        }

        if (arguments.includeRuntime) {
            configuration.put(JVMConfigurationKeys.INCLUDE_RUNTIME, true)
        }
        val friendPaths = arguments.friendPaths?.toList()
        if (friendPaths != null) {
            configuration.put(JVMConfigurationKeys.FRIEND_PATHS, friendPaths)
        }

        if (arguments.jvmTarget != null) {
            val jvmTarget = JvmTarget.fromString(arguments.jvmTarget)
            if (jvmTarget != null) {
                configuration.put(JVMConfigurationKeys.JVM_TARGET, jvmTarget)
            }
            else {
                val errorMessage = "Unknown JVM target version: ${arguments.jvmTarget}\n" +
                    "Supported versions: ${JvmTarget.values().joinToString { it.description }}"
                messageCollector.report(CompilerMessageSeverity.ERROR, errorMessage, CompilerMessageLocation.NO_LOCATION)
            }
        }

        configuration.put(JVMConfigurationKeys.PARAMETERS_METADATA, arguments.javaParameters)

        putAdvancedOptions(configuration, arguments)

        messageCollector.report(CompilerMessageSeverity.LOGGING, "Configuring the compilation environment", CompilerMessageLocation.NO_LOCATION)
        try {
            val destination = arguments.destination

            if (arguments.module != null) {
                val sanitizedCollector = FilteringMessageCollector(messageCollector, `in`(CompilerMessageSeverity.VERBOSE))
                val moduleScript = CompileEnvironmentUtil.loadModuleDescriptions(arguments.module, sanitizedCollector)

                if (destination != null) {
                    messageCollector.report(
                        CompilerMessageSeverity.STRONG_WARNING,
                        "The '-d' option with a directory destination is ignored because '-module' is specified",
                        CompilerMessageLocation.NO_LOCATION
                    )
                }

                val moduleFile = File(arguments.module)
                val directory = moduleFile.absoluteFile.parentFile

                FuckedKotlinToJVMBytecodeCompiler.configureSourceRoots(configuration, moduleScript.modules, directory)
                configuration.put(JVMConfigurationKeys.MODULE_XML_FILE, moduleFile)

                val environment = createEnvironmentWithScriptingSupport(rootDisposable, configuration, arguments, messageCollector)
                    ?: return COMPILATION_ERROR

                FuckedKotlinToJVMBytecodeCompiler.compileModules(environment, directory)
            }
            else if (arguments.script) {
                val scriptArgs = arguments.freeArgs.subList(1, arguments.freeArgs.size)

                configuration.put(JVMConfigurationKeys.RETAIN_OUTPUT_IN_MEMORY, true)

                val environment = createEnvironmentWithScriptingSupport(rootDisposable, configuration, arguments, messageCollector)
                    ?: return COMPILATION_ERROR

                return FuckedKotlinToJVMBytecodeCompiler.compileAndExecuteScript(environment, paths, scriptArgs)
            }
            else {
                if (destination != null) {
                    if (destination.endsWith(".jar")) {
                        configuration.put(JVMConfigurationKeys.OUTPUT_JAR, File(destination))
                    }
                    else {
                        configuration.put(JVMConfigurationKeys.OUTPUT_DIRECTORY, File(destination))
                    }
                }

                val environment = createEnvironmentWithScriptingSupport(rootDisposable, configuration, arguments, messageCollector)
                    ?: return COMPILATION_ERROR

                if (environment.getSourceFiles().isEmpty()) {
                    if (arguments.version) {
                        return OK
                    }
                    messageCollector.report(CompilerMessageSeverity.ERROR, "No source files", CompilerMessageLocation.NO_LOCATION)
                    return COMPILATION_ERROR
                }

                FuckedKotlinToJVMBytecodeCompiler.compileBunchOfSources(environment)
            }

            if (arguments.reportPerf) {
                reportGCTime(configuration)
                reportCompilationTime(configuration)
                PerformanceCounter.report { s -> reportPerf(configuration, s) }
            }
            return OK
        }
        catch (e: CompilationException) {
            messageCollector.report(
                CompilerMessageSeverity.EXCEPTION,
                OutputMessageUtil.renderException(e),
                MessageUtil.psiElementToMessageLocation(e.element)
            )
            return INTERNAL_ERROR
        }
    }

    private fun createEnvironmentWithScriptingSupport(rootDisposable: Disposable,
                                                      configuration: CompilerConfiguration,
                                                      arguments: K2JVMCompilerArguments,
                                                      messageCollector: MessageCollector
    ): KotlinCoreEnvironment? {

        val scriptResolverEnv = hashMapOf<String, Any?>()
        configureScriptDefinitions(arguments.scriptTemplates, configuration, messageCollector, scriptResolverEnv)
        if (!messageCollector.hasErrors()) {
            val environment = createCoreEnvironment(rootDisposable, configuration)
            if (!messageCollector.hasErrors()) {
                scriptResolverEnv.put("projectRoot", environment.project.run { basePath ?: baseDir?.canonicalPath }?.let(::File))
                return environment
            }
        }
        return null
    }

    private fun createCoreEnvironment(rootDisposable: Disposable, configuration: CompilerConfiguration): KotlinCoreEnvironment {
        val result = KotlinCoreEnvironment.createForProduction(rootDisposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)

        if (initStartNanos != 0L) {
            val initNanos = System.nanoTime() - initStartNanos
            reportPerf(configuration, "INIT: Compiler initialized in " + TimeUnit.NANOSECONDS.toMillis(initNanos) + " ms")
            initStartNanos = 0L
        }
        return result
    }

    override fun setupPlatformSpecificArgumentsAndServices(
        configuration: CompilerConfiguration, arguments: K2JVMCompilerArguments, services: Services
    ) {
        if (IncrementalCompilation.isEnabled()) {
            val components = services.get(IncrementalCompilationComponents::class.java)
            if (components != null) {
                configuration.put(JVMConfigurationKeys.INCREMENTAL_COMPILATION_COMPONENTS, components)
            }
        }
    }

    /**
     * Allow derived classes to add additional command line arguments
     */
    override fun createArguments(): K2JVMCompilerArguments {
        val result = K2JVMCompilerArguments()
        if (System.getenv("KOTLIN_REPORT_PERF") != null) {
            result.reportPerf = true
        }
        return result
    }

    companion object {
        private var initStartNanos = System.nanoTime()
        // allows to track GC time for each run when repeated compilation is used
        private val elapsedGCTime = hashMapOf<String, Long>()
        private var elapsedJITTime = 0L

        fun resetInitStartTime() {
            if (initStartNanos == 0L) {
                initStartNanos = System.nanoTime()
            }
        }

        @JvmStatic fun main(args: Array<String>) {
            FuckedCLICompiler.doMain(FuckedK2JVMCompiler(), args)
        }

        fun reportPerf(configuration: CompilerConfiguration, message: String) {
            if (!configuration.getBoolean(CLIConfigurationKeys.REPORT_PERF)) return

            val collector = configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)
            collector.report(CompilerMessageSeverity.INFO, "PERF: " + message, CompilerMessageLocation.NO_LOCATION)
        }

        fun reportGCTime(configuration: CompilerConfiguration) {
            ManagementFactory.getGarbageCollectorMXBeans().forEach {
                val currentTime = it.collectionTime
                val elapsedTime = elapsedGCTime.getOrElse(it.name) { 0 }
                val time = currentTime - elapsedTime
                reportPerf(configuration, "GC time for ${it.name} is $time ms")
                elapsedGCTime[it.name] = currentTime
            }
        }

        fun reportCompilationTime(configuration: CompilerConfiguration) {
            val bean = ManagementFactory.getCompilationMXBean() ?: return
            val currentTime = bean.totalCompilationTime
            reportPerf(configuration, "JIT time is ${currentTime - elapsedJITTime} ms")
            elapsedJITTime = currentTime
        }

        private fun putAdvancedOptions(configuration: CompilerConfiguration, arguments: K2JVMCompilerArguments) {
            configuration.put(JVMConfigurationKeys.DISABLE_CALL_ASSERTIONS, arguments.noCallAssertions)
            configuration.put(JVMConfigurationKeys.DISABLE_PARAM_ASSERTIONS, arguments.noParamAssertions)
            configuration.put(JVMConfigurationKeys.DISABLE_OPTIMIZATION, arguments.noOptimize)
            configuration.put(JVMConfigurationKeys.INHERIT_MULTIFILE_PARTS, arguments.inheritMultifileParts)
            configuration.put(JVMConfigurationKeys.SKIP_RUNTIME_VERSION_CHECK, arguments.skipRuntimeVersionCheck)
            configuration.put(CLIConfigurationKeys.ALLOW_KOTLIN_PACKAGE, arguments.allowKotlinPackage)
            configuration.put(CLIConfigurationKeys.REPORT_PERF, arguments.reportPerf)
            configuration.put(JVMConfigurationKeys.USE_SINGLE_MODULE, arguments.singleModule)
            configuration.put(JVMConfigurationKeys.ADD_BUILT_INS_FROM_COMPILER_TO_DEPENDENCIES, arguments.addCompilerBuiltIns)
            configuration.put(JVMConfigurationKeys.CREATE_BUILT_INS_FROM_MODULE_DEPENDENCIES, arguments.loadBuiltInsFromDependencies)

            arguments.declarationsOutputPath?.let { configuration.put(JVMConfigurationKeys.DECLARATIONS_JSON_PATH, it) }
        }

        private fun getClasspath(paths: KotlinPaths, arguments: K2JVMCompilerArguments): List<File> {
            val classpath = arrayListOf<File>()
            if (arguments.classpath != null) {
                classpath.addAll(arguments.classpath.split(File.pathSeparatorChar).map(::File))
            }
            if (!arguments.noStdlib) {
                classpath.add(paths.runtimePath)
                classpath.add(paths.scriptRuntimePath)
            }
            // "-no-stdlib" implies "-no-reflect": otherwise we would be able to transitively read stdlib classes through kotlin-reflect,
            // which is likely not what user wants since s/he manually provided "-no-stdlib"
            if (!arguments.noReflect && !arguments.noStdlib) {
                classpath.add(paths.reflectPath)
            }
            return classpath
        }

        private fun setupJdkClasspathRoots(arguments: K2JVMCompilerArguments, configuration: CompilerConfiguration, messageCollector: MessageCollector): ExitCode {
            try {
                if (!arguments.noJdk) {
                    if (arguments.jdkHome != null) {
                        messageCollector.report(CompilerMessageSeverity.LOGGING,
                                                "Using JDK home directory ${arguments.jdkHome}",
                                                CompilerMessageLocation.NO_LOCATION)
                        val classesRoots = PathUtil.getJdkClassesRoots(File(arguments.jdkHome))
                        if (classesRoots.isEmpty()) {
                            messageCollector.report(CompilerMessageSeverity.ERROR,
                                                    "No class roots are found in the JDK path: ${arguments.jdkHome}",
                                                    CompilerMessageLocation.NO_LOCATION)
                            return COMPILATION_ERROR
                        }
                        configuration.addJvmClasspathRoots(classesRoots)
                    }
                    else {
                        configuration.addJvmClasspathRoots(PathUtil.getJdkClassesRoots())
                    }
                }
                else {
                    if (arguments.jdkHome != null) {
                        messageCollector.report(CompilerMessageSeverity.STRONG_WARNING,
                                                "The '-jdk-home' option is ignored because '-no-jdk' is specified",
                                                CompilerMessageLocation.NO_LOCATION)
                    }
                }
            }
            catch (e: EnoughFuckedCompiling) {
                throw e
            }
            catch (t: Throwable) {
                MessageCollectorUtil.reportException(messageCollector, t)
                return INTERNAL_ERROR
            }
            return OK
        }

        fun configureScriptDefinitions(scriptTemplates: Array<String>?,
                                       configuration: CompilerConfiguration,
                                       messageCollector: MessageCollector,
                                       scriptResolverEnv: HashMap<String, Any?>) {
            val classpath = configuration.getList(JVMConfigurationKeys.CONTENT_ROOTS).filterIsInstance(JvmClasspathRoot::class.java).mapNotNull { it.file }
            // TODO: consider using escaping to allow kotlin escaped names in class names
            if (scriptTemplates != null && scriptTemplates.isNotEmpty()) {
                val classloader = URLClassLoader(classpath.map { it.toURI().toURL() }.toTypedArray(), Thread.currentThread().contextClassLoader)
                var hasErrors = false
                for (template in scriptTemplates) {
                    try {
                        val cls = classloader.loadClass(template)
                        val def = KotlinScriptDefinitionFromAnnotatedTemplate(cls.kotlin, null, null, scriptResolverEnv)
                        configuration.add(JVMConfigurationKeys.SCRIPT_DEFINITIONS, def)
                        messageCollector.report(
                            CompilerMessageSeverity.INFO,
                            "Added script definition $template to configuration: files pattern = \"${def.scriptFilePattern}\", resolver = ${def.resolver?.javaClass?.name}",
                            CompilerMessageLocation.NO_LOCATION
                        )
                    }
                    catch (ex: ClassNotFoundException) {
                        messageCollector.report(
                            CompilerMessageSeverity.ERROR, "Cannot find script definition template class $template", CompilerMessageLocation.NO_LOCATION
                        )
                        hasErrors = true
                    }
                    catch (e: EnoughFuckedCompiling) {
                        throw e
                    }
                    catch (ex: Exception) {
                        messageCollector.report(
                            CompilerMessageSeverity.ERROR, "Error processing script definition template $template: ${ex.message}", CompilerMessageLocation.NO_LOCATION
                        )
                        hasErrors = true
                        break
                    }
                }
                if (hasErrors) {
                    messageCollector.report(
                        CompilerMessageSeverity.LOGGING, "(Classpath used for templates loading: $classpath)", CompilerMessageLocation.NO_LOCATION
                    )
                    return
                }
            }
            configuration.add(JVMConfigurationKeys.SCRIPT_DEFINITIONS, StandardScriptDefinition)
        }
    }
}


object FuckedKotlinToJVMBytecodeCompiler {

    private fun getAbsolutePaths(directory: File, module: Module): List<String> {
        return module.getSourceFiles().map { sourceFile ->
            var source = File(sourceFile)
            if (!source.isAbsolute) {
                source = File(directory, sourceFile)
            }
            source.absolutePath
        }
    }

    private fun writeOutput(
        configuration: CompilerConfiguration,
        outputFiles: OutputFileCollection,
        mainClass: FqName?
    ) {
        val jarPath = configuration.get(JVMConfigurationKeys.OUTPUT_JAR)
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        if (jarPath != null) {
            val includeRuntime = configuration.get(JVMConfigurationKeys.INCLUDE_RUNTIME, false)
            CompileEnvironmentUtil.writeToJar(jarPath, includeRuntime, mainClass, outputFiles)
            messageCollector.report(CompilerMessageSeverity.OUTPUT,
                                    OutputMessageUtil.formatOutputMessage(outputFiles.asList().flatMap { it.sourceFiles }.distinct(), jarPath), CompilerMessageLocation.NO_LOCATION)
            return
        }

        val outputDir = configuration.get(JVMConfigurationKeys.OUTPUT_DIRECTORY) ?: File(".")
        outputFiles.writeAll(outputDir, messageCollector)
    }

    private fun createOutputFilesFlushingCallbackIfPossible(configuration: CompilerConfiguration): GenerationStateEventCallback {
        if (configuration.get(JVMConfigurationKeys.OUTPUT_DIRECTORY) == null) {
            return GenerationStateEventCallback.DO_NOTHING
        }
        return GenerationStateEventCallback { state ->
            val currentOutput = SimpleOutputFileCollection(state.factory.currentOutput)
            writeOutput(configuration, currentOutput, mainClass = null)
            if (!configuration.get(JVMConfigurationKeys.RETAIN_OUTPUT_IN_MEMORY, false)) {
                state.factory.releaseGeneratedOutput()
            }
        }
    }

    fun compileModules(environment: KotlinCoreEnvironment, directory: File): Boolean {
        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        val moduleVisibilityManager = ModuleVisibilityManager.SERVICE.getInstance(environment.project)

        val projectConfiguration = environment.configuration
        val chunk = projectConfiguration.getNotNull(JVMConfigurationKeys.MODULES)
        for (module in chunk) {
            moduleVisibilityManager.addModule(module)
        }

        val friendPaths = environment.configuration.getList(JVMConfigurationKeys.FRIEND_PATHS)
        for (path in friendPaths) {
            moduleVisibilityManager.addFriendPath(path)
        }

        val targetDescription = "in targets [" + chunk.joinToString { input -> input.getModuleName() + "-" + input.getModuleType() } + "]"

        val result = repeatAnalysisIfNeeded(analyze(environment, targetDescription), environment, targetDescription)
        if (result == null || !result.shouldGenerateCode) return false

        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        result.throwIfError()

        val outputs = newLinkedHashMapWithExpectedSize<Module, GenerationState>(chunk.size)

        for (module in chunk) {
            ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()
            val ktFiles = CompileEnvironmentUtil.getKtFiles(
                environment.project, getAbsolutePaths(directory, module), projectConfiguration
            ) { path -> throw IllegalStateException("Should have been checked before: $path") }
            if (!checkKotlinPackageUsage(environment, ktFiles)) return false

            val moduleConfiguration = projectConfiguration.copy().apply {
                put(JVMConfigurationKeys.OUTPUT_DIRECTORY, File(module.getOutputDirectory()))
            }

            outputs[module] = generate(environment, moduleConfiguration, result, ktFiles, module)
        }

        try {
            for ((_, state) in outputs) {
                ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()
                writeOutput(state.configuration, state.factory, null)
            }
            return true
        }
        finally {
            outputs.values.forEach(GenerationState::destroy)
        }
    }

    fun configureSourceRoots(configuration: CompilerConfiguration, chunk: List<Module>, directory: File) {
        for (module in chunk) {
            configuration.addKotlinSourceRoots(getAbsolutePaths(directory, module))
        }

        for (module in chunk) {
            for (javaRootPath in module.getJavaSourceRoots()) {
                configuration.addJavaSourceRoot(File(javaRootPath.path), javaRootPath.packagePrefix)
            }
        }

        for (module in chunk) {
            for (classpathRoot in module.getClasspathRoots()) {
                configuration.addJvmClasspathRoot(File(classpathRoot))
            }
        }

        configuration.addAll(JVMConfigurationKeys.MODULES, chunk)
    }

    private fun findMainClass(generationState: GenerationState, files: List<KtFile>): FqName? {
        val mainFunctionDetector = MainFunctionDetector(generationState.bindingContext)
        return files.asSequence()
            .map { file ->
                if (mainFunctionDetector.hasMain(file.declarations))
                    JvmFileClassUtil.getFileClassInfoNoResolve(file).facadeClassFqName
                else
                    null
            }
            .singleOrNull { it != null }
    }

    fun compileBunchOfSources(environment: KotlinCoreEnvironment): Boolean {
        val moduleVisibilityManager = ModuleVisibilityManager.SERVICE.getInstance(environment.project)

        val friendPaths = environment.configuration.getList(JVMConfigurationKeys.FRIEND_PATHS)
        for (path in friendPaths) {
            moduleVisibilityManager.addFriendPath(path)
        }

        if (!checkKotlinPackageUsage(environment, environment.getSourceFiles())) return false

        val generationState = analyzeAndGenerate(environment) ?: return false

        val mainClass = findMainClass(generationState, environment.getSourceFiles())

        try {
            writeOutput(environment.configuration, generationState.factory, mainClass)
            return true
        }
        finally {
            generationState.destroy()
        }
    }

    fun compileAndExecuteScript(
        environment: KotlinCoreEnvironment,
        paths: KotlinPaths,
        scriptArgs: List<String>): ExitCode
    {
        val scriptClass = compileScript(environment, paths) ?: return ExitCode.COMPILATION_ERROR

        try {
            try {
                tryConstructClassFromStringArgs(scriptClass, scriptArgs)
                    ?: throw RuntimeException("unable to find appropriate constructor for class ${scriptClass.name} accepting arguments $scriptArgs\n")
            }
            finally {
                // NB: these lines are required (see KT-9546) but aren't covered by tests
                System.out.flush()
                System.err.flush()
            }
        }
        catch (e: Throwable) {
            reportExceptionFromScript(e)
            return ExitCode.SCRIPT_EXECUTION_ERROR
        }

        return ExitCode.OK
    }

    private fun repeatAnalysisIfNeeded(
        result: AnalysisResult?,
        environment: KotlinCoreEnvironment,
        targetDescription: String?
    ): AnalysisResult? {
        if (result is AnalysisResult.RetryWithAdditionalJavaRoots) {
            val configuration = environment.configuration

            val oldReadOnlyValue = configuration.isReadOnly
            configuration.isReadOnly = false
            configuration.addJavaSourceRoots(result.additionalJavaRoots)
            configuration.isReadOnly = oldReadOnlyValue

            if (result.addToEnvironment) {
                environment.updateClasspath(result.additionalJavaRoots.map { JavaSourceRoot(it, null) })
            }

            // Clear package caches (see KotlinJavaPsiFacade)
            ApplicationManager.getApplication().runWriteAction {
                (PsiManager.getInstance(environment.project).modificationTracker as? PsiModificationTrackerImpl)?.incCounter()
            }

            // Clear all diagnostic messages
            configuration[CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY]?.clear()

            // Repeat analysis with additional Java roots (kapt generated sources)
            return analyze(environment, targetDescription)
        }

        return result
    }

    private fun reportExceptionFromScript(exception: Throwable) {
        // expecting InvocationTargetException from constructor invocation with cause that describes the actual cause
        val stream = System.err
        val cause = exception.cause
        if (exception !is InvocationTargetException || cause == null) {
            exception.printStackTrace(stream)
            return
        }
        stream.println(cause)
        val fullTrace = cause.stackTrace
        val relevantEntries = fullTrace.size - exception.stackTrace.size
        for (i in 0..relevantEntries - 1) {
            stream.println("\tat " + fullTrace[i])
        }
    }

    fun compileScript(environment: KotlinCoreEnvironment, paths: KotlinPaths): Class<*>? =
        compileScript(environment,
                      {
                          val classPaths = arrayListOf(paths.runtimePath.toURI().toURL())
                          environment.configuration.jvmClasspathRoots.mapTo(classPaths) { it.toURI().toURL() }
                          URLClassLoader(classPaths.toTypedArray())
                      })

    fun compileScript(environment: KotlinCoreEnvironment, parentClassLoader: ClassLoader): Class<*>? = compileScript(environment, { parentClassLoader })

    private inline fun compileScript(
        environment: KotlinCoreEnvironment,
        makeParentClassLoader: () -> ClassLoader): Class<*>? {
        val state = analyzeAndGenerate(environment) ?: return null

        try {
            val classLoader = GeneratedClassLoader(state.factory, makeParentClassLoader())

            val script = environment.getSourceFiles()[0].script ?: error("Script must be parsed")
            return classLoader.loadClass(script.fqName.asString())
        }
        catch (e: Exception) {
            throw RuntimeException("Failed to evaluate script: " + e, e)
        }
    }

    fun analyzeAndGenerate(environment: KotlinCoreEnvironment): GenerationState? {
        val result = repeatAnalysisIfNeeded(analyze(environment, null), environment, null) ?: return null

        if (!result.shouldGenerateCode) return null

        result.throwIfError()

        return generate(environment, environment.configuration, result, environment.getSourceFiles(), null)
    }

    private fun analyze(environment: KotlinCoreEnvironment, targetDescription: String?): AnalysisResult? {
        val collector = environment.messageCollector

        val analysisStart = PerformanceCounter.currentTime()
        val analyzerWithCompilerReport = AnalyzerWithCompilerReport(collector)
        analyzerWithCompilerReport.analyzeAndReport(
            environment.getSourceFiles(), object : AnalyzerWithCompilerReport.Analyzer {
            override fun analyze(): AnalysisResult {
                val project = environment.project
                val moduleOutputs = environment.configuration.get(JVMConfigurationKeys.MODULES)?.mapNotNull { module ->
                    // Orig: environment.findLocalDirectory(module.getOutputDirectory())
                    val method = environment::class.java.getMethod("findLocalDirectory", String::class.java)
                    method.invoke(environment, module.getOutputDirectory()) as VirtualFile?
                }.orEmpty()
                val sourcesOnly = TopDownAnalyzerFacadeForJVM.newModuleSearchScope(project, environment.getSourceFiles())
                // To support partial and incremental compilation, we add the scope which contains binaries from output directories
                // of the compiled modules (.class) to the list of scopes of the source module
                val scope = if (moduleOutputs.isEmpty()) sourcesOnly else sourcesOnly.uniteWith(DirectoriesScope(project, moduleOutputs))
                return TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                    project,
                    environment.getSourceFiles(),
                    CliLightClassGenerationSupport.NoScopeRecordCliBindingTrace(),
                    environment.configuration,
                    { scope -> JvmPackagePartProvider(environment, scope) },
                    sourceModuleSearchScope = scope
                )
            }

            override fun reportEnvironmentErrors() {
                reportRuntimeConflicts(collector, environment.configuration.jvmClasspathRoots)
            }
        })

        val analysisNanos = PerformanceCounter.currentTime() - analysisStart

        val sourceLinesOfCode = environment.sourceLinesOfCode
        val numberOfFiles = environment.getSourceFiles().size
        val time = TimeUnit.NANOSECONDS.toMillis(analysisNanos)
        val speed = sourceLinesOfCode.toFloat() * 1000 / time

        val message = "ANALYZE: $numberOfFiles files ($sourceLinesOfCode lines) ${targetDescription ?: ""}" +
            "in $time ms - ${"%.3f".format(speed)} loc/s"

        FuckedK2JVMCompiler.reportPerf(environment.configuration, message)

        val analysisResult = analyzerWithCompilerReport.analysisResult
        throw EnoughFuckedCompiling(analysisResult, environment)

        return if (!analyzerWithCompilerReport.hasErrors() || analysisResult is AnalysisResult.RetryWithAdditionalJavaRoots)
            analysisResult
        else
            null
    }

    class DirectoriesScope(
        project: Project, private val directories: List<VirtualFile>
    ) : DelegatingGlobalSearchScope(GlobalSearchScope.allScope(project)) {
        // TODO: optimize somehow?
        override fun contains(file: VirtualFile) =
            directories.any { directory -> VfsUtilCore.isAncestor(directory, file, false) }

        override fun toString() = "All files under: $directories"
    }

    private fun generate(
        environment: KotlinCoreEnvironment,
        configuration: CompilerConfiguration,
        result: AnalysisResult,
        sourceFiles: List<KtFile>,
        module: Module?
    ): GenerationState {
        val isKapt2Enabled = environment.project.getUserData(IS_KAPT2_ENABLED_KEY) ?: false
        val generationState = GenerationState(
            environment.project,
            ClassBuilderFactories.binaries(isKapt2Enabled),
            result.moduleDescriptor,
            result.bindingContext,
            sourceFiles,
            configuration,
            GenerationState.GenerateClassFilter.GENERATE_ALL,
            module?.let(::TargetId),
            module?.let(Module::getModuleName),
            module?.let { File(it.getOutputDirectory()) },
            createOutputFilesFlushingCallbackIfPossible(configuration)
        )
        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        val generationStart = PerformanceCounter.currentTime()

        KotlinCodegenFacade.compileCorrectFiles(generationState, CompilationErrorHandler.THROW_EXCEPTION)

        val generationNanos = PerformanceCounter.currentTime() - generationStart
        val desc = if (module != null) "target " + module.getModuleName() + "-" + module.getModuleType() + " " else ""
        val numberOfSourceFiles = sourceFiles.size
        val numberOfLines = environment.countLinesOfCode(sourceFiles)
        val time = TimeUnit.NANOSECONDS.toMillis(generationNanos)
        val speed = numberOfLines.toFloat() * 1000 / time
        val message = "GENERATE: $numberOfSourceFiles files ($numberOfLines lines) ${desc}in $time ms - ${"%.3f".format(speed)} loc/s"

        FuckedK2JVMCompiler.reportPerf(environment.configuration, message)
        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        AnalyzerWithCompilerReport.reportDiagnostics(
            FilteredJvmDiagnostics(
                generationState.collectedExtraJvmDiagnostics,
                result.bindingContext.diagnostics
            ),
            environment.messageCollector
        )

        AnalyzerWithCompilerReport.reportBytecodeVersionErrors(
            generationState.extraJvmDiagnosticsTrace.bindingContext, environment.messageCollector
        )

        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()
        return generationState
    }

    private val KotlinCoreEnvironment.messageCollector: MessageCollector
        get() = configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)

    private fun reportRuntimeConflicts(messageCollector: MessageCollector, jvmClasspathRoots: List<File>) {
        fun String.removeIdeaVersionSuffix(): String {
            val versionIndex = indexOfAny(arrayListOf("-IJ", "-Idea"))
            return if (versionIndex >= 0) substring(0, versionIndex) else this
        }

        val runtimes = jvmClasspathRoots.map {
            try {
                it.canonicalFile
            }
            catch (e: IOException) {
                it
            }
        }.filter { it.name == PathUtil.KOTLIN_JAVA_RUNTIME_JAR && it.exists() }

        val runtimeVersions = runtimes.map {
            JarUtil.getJarAttribute(it, Attributes.Name.IMPLEMENTATION_VERSION).orEmpty().removeIdeaVersionSuffix()
        }

        if (runtimeVersions.toSet().size > 1) {
            messageCollector.report(CompilerMessageSeverity.ERROR,
                                    "Conflicting versions of Kotlin runtime on classpath: " + runtimes.joinToString { it.path },
                                    CompilerMessageLocation.NO_LOCATION)
        }
    }
}

