package com.quantumvm.intellij.runner

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.DefaultProgramRunner

class QasmProgramRunner : DefaultProgramRunner() {

    override fun getRunnerId(): String = "QasmProgramRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return executorId == DefaultRunExecutor.EXECUTOR_ID && profile is QasmRunConfiguration
    }
}
