package com.quantumvm.intellij.runner

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.quantumvm.intellij.binary.BinaryManager
import com.quantumvm.intellij.settings.QasmSettingsComponent
import java.io.File
import javax.swing.JComponent

class QasmRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<QasmRunConfigurationOptions>(project, factory, name) {

    override fun getOptionsClass(): Class<QasmRunConfigurationOptions> {
        return QasmRunConfigurationOptions::class.java
    }

    override fun getOptions(): QasmRunConfigurationOptions {
        return super.getOptions() as QasmRunConfigurationOptions
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return QasmRunConfigurationEditor()
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return QasmCommandLineState(environment, this)
    }

    var scriptPath: String
        get() = options.scriptPath
        set(value) {
            options.scriptPath = value
        }
}

class QasmRunConfigurationOptions : RunConfigurationOptions() {
    private val scriptPathProperty = string("").provideDelegate(this, "scriptPath")

    var scriptPath: String
        get() = scriptPathProperty.getValue(this) ?: ""
        set(value) {
            scriptPathProperty.setValue(this, value)
        }
}

class QasmRunConfigurationEditor : SettingsEditor<QasmRunConfiguration>() {
    private var scriptPath = ""

    override fun resetEditorFrom(config: QasmRunConfiguration) {
        scriptPath = config.scriptPath
    }

    override fun applyEditorTo(config: QasmRunConfiguration) {
        config.scriptPath = scriptPath
    }

    override fun createEditor(): JComponent {
        return panel {
            row("QASM file:") {
                textField()
                    .bindText(::scriptPath)
                    .comment("Path to the QASM file to execute")
            }
        }
    }
}

class QasmCommandLineState(
    environment: ExecutionEnvironment,
    private val config: QasmRunConfiguration
) : CommandLineState(environment) {

    override fun startProcess(): com.intellij.execution.process.ProcessHandler {
        val settings = ApplicationManager.getApplication().getService(QasmSettingsComponent::class.java)
        val binaryManager = ApplicationManager.getApplication().getService(BinaryManager::class.java)

        var vmPath: String? = settings.state.vmBinaryPath
        if (vmPath.isNullOrEmpty()) {
            val binaryPath = binaryManager.getVmBinaryPath()
            if (binaryPath != null) {
                vmPath = binaryPath.toString()
            } else {
                binaryManager.ensureVmBinary(forceDownload = false)
                repeat(60) {
                    val path = binaryManager.getVmBinaryPath()
                    if (path != null) {
                        vmPath = path.toString()
                        return@repeat
                    }
                    Thread.sleep(1000)
                }
                if (vmPath == null) {
                    throw RuntimeException(
                        "Failed to download QuantumVM binary. " +
                        "Check your internet connection and ensure the binary is not blocked."
                    )
                }
            }
        }

        val resolvedVmPath = vmPath ?: error("QuantumVM binary path not resolved")
        if (!File(resolvedVmPath).exists()) throw RuntimeException("QuantumVM binary not found at: $resolvedVmPath")

        val scriptPath = config.scriptPath
        if (scriptPath.isEmpty()) throw RuntimeException("No QASM file specified")

        if (!File(scriptPath).exists()) throw RuntimeException("QASM file not found: $scriptPath")

        val commandLine = GeneralCommandLine()
            .withExePath(resolvedVmPath)
            .withParameters(scriptPath)
            .withWorkDirectory(config.project.basePath ?: System.getProperty("user.dir"))

        val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(processHandler)

        return processHandler
    }
}
