package com.quantumvm.intellij.actions

import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.quantumvm.intellij.language.QasmFileType
import com.quantumvm.intellij.runner.QasmRunConfiguration
import com.quantumvm.intellij.runner.QasmRunConfigurationType

class RunQasmFileAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file?.fileType == QasmFileType
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        if (file.fileType != QasmFileType) return

        val runManager = RunManager.getInstance(project)
        val configurationType = QasmRunConfigurationType()
        val factory = configurationType.configurationFactories[0]

        val existingConfig = runManager.allSettings.find { settings ->
            val config = settings.configuration
            config is QasmRunConfiguration && config.scriptPath == file.path
        }

        val runnerAndConfigurationSettings = if (existingConfig != null) {
            existingConfig
        } else {
            val settings = runManager.createConfiguration(
                file.nameWithoutExtension,
                factory
            )
            val config = settings.configuration as QasmRunConfiguration
            config.scriptPath = file.path
            runManager.addConfiguration(settings)
            settings
        }

        runManager.selectedConfiguration = runnerAndConfigurationSettings

        val executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID)
        if (executor != null) ProgramRunnerUtil.executeConfiguration(runnerAndConfigurationSettings, executor)
    }
}
