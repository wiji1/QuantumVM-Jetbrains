package com.quantumvm.intellij

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.quantumvm.intellij.binary.BinaryManager
import com.quantumvm.intellij.settings.QasmSettingsComponent
import com.quantumvm.intellij.update.UpdateChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QasmPlugin : ProjectActivity {

    override suspend fun execute(project: Project) {
        withContext(Dispatchers.IO) {
            val settings = ApplicationManager.getApplication().getService(QasmSettingsComponent::class.java)
            val binaryManager = ApplicationManager.getApplication().getService(BinaryManager::class.java)
            val updateChecker = ApplicationManager.getApplication().getService(UpdateChecker::class.java)

            try {
                binaryManager.ensureLspBinary(forceDownload = false)
                binaryManager.ensureVmBinary(forceDownload = false)
            } catch (ignored: Exception) { }

            if (settings.state.autoUpdate) {
                try {
                    updateChecker.checkForUpdates(manual = false)
                } catch (ignored: Exception) { }
            }
        }
    }
}
