package com.quantumvm.intellij.runner

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project
import com.quantumvm.intellij.language.QasmIcons
import javax.swing.Icon

class QasmRunConfigurationType : ConfigurationType {
    override fun getDisplayName(): String = "QASM"

    override fun getConfigurationTypeDescription(): String =
        "Run configuration for OpenQASM 3.0 files"

    override fun getIcon(): Icon = QasmIcons.FILE

    override fun getId(): String = "QasmRunConfiguration"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
        arrayOf(QasmConfigurationFactory(this))
}

class QasmConfigurationFactory(type: QasmRunConfigurationType) : ConfigurationFactory(type) {
    override fun getId(): String = "QASM"

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        QasmRunConfiguration(project, this, "QASM")

    override fun getName(): String = "QASM"
}
