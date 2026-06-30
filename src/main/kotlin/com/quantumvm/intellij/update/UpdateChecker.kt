package com.quantumvm.intellij.update

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.quantumvm.intellij.binary.BinaryDownloader
import com.quantumvm.intellij.binary.BinaryManager
import com.quantumvm.intellij.binary.VersionManager
import com.quantumvm.intellij.settings.QasmSettingsComponent

@Service(Service.Level.APP)
class UpdateChecker {
    private val downloader = BinaryDownloader()
    private val binaryManager = ApplicationManager.getApplication().getService(BinaryManager::class.java)
    private val versionManager = ApplicationManager.getApplication().getService(VersionManager::class.java)
    private val settings = ApplicationManager.getApplication().getService(QasmSettingsComponent::class.java)

    fun checkForUpdates(manual: Boolean = false) {
        if (!manual && !settings.state.autoUpdate) return

        if (!manual && !versionManager.shouldCheckForUpdates()) return

        try {
            val release = downloader.fetchLatestRelease()
            val latestVersion = release.tagName

            val currentLspVersion = versionManager.getLspVersion()
            val currentVmVersion = versionManager.getVmVersion()

            if (currentLspVersion.isEmpty() || currentVmVersion.isEmpty()) {
                versionManager.updateVersions(latestVersion, latestVersion)
                return
            }

            val updateAvailable = latestVersion != currentLspVersion || latestVersion != currentVmVersion

            versionManager.updateLastUpdateCheck()

            if (updateAvailable) showUpdateNotification(latestVersion, currentLspVersion)
            else if (manual) {
                showNotification(
                    "No Updates Available",
                    "You are using the latest version of qasm-lsp and QuantumVM ($latestVersion)",
                    NotificationType.INFORMATION
                )
            }
        } catch (e: Exception) {
            if (manual) {
                showNotification(
                    "Update Check Failed",
                    "Failed to check for updates: ${e.message}",
                    NotificationType.ERROR
                )
            }
        }
    }

    private fun showUpdateNotification(latestVersion: String, currentVersion: String) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("QuantumVM Notifications")
            .createNotification(
                "QuantumVM Update Available",
                "New version $latestVersion is available (current: $currentVersion)",
                NotificationType.INFORMATION
            )

        notification.addAction(NotificationAction.createSimple("Update Now") {
            performUpdate(latestVersion)
        })

        notification.addAction(NotificationAction.createSimple("Dismiss") {
            notification.expire()
        })

        notification.notify(null)
    }

    fun performUpdate(version: String) {
        showNotification(
            "Updating Binaries",
            "Downloading qasm-lsp and QuantumVM version $version...",
            NotificationType.INFORMATION
        )

        binaryManager.ensureBinaries(forceDownload = true) {
            versionManager.updateVersions(version, version)
            showNotification(
                "Update Complete",
                "Successfully updated to version $version. Please restart the IDE for changes to take effect.",
                NotificationType.INFORMATION
            )
        }
    }

    private fun showNotification(title: String, content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("QuantumVM Notifications")
            .createNotification(title, content, type)
            .notify(null)
    }
}
