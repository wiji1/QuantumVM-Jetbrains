package com.quantumvm.intellij.runner

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.quantumvm.intellij.language.QasmFileType

class QasmRunConfigurationProducer : LazyRunConfigurationProducer<QasmRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory {
        return QasmConfigurationFactory(QasmRunConfigurationType())
    }

    override fun isConfigurationFromContext(
        configuration: QasmRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val location = context.location ?: return false
        val file = location.virtualFile ?: return false

        if (file.fileType != QasmFileType) return false

        return configuration.scriptPath == file.path
    }

    override fun setupConfigurationFromContext(
        configuration: QasmRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val location = context.location ?: return false
        val file = location.virtualFile ?: return false

        if (file.fileType != QasmFileType) return false

        configuration.scriptPath = file.path
        configuration.name = file.nameWithoutExtension

        return true
    }
}
