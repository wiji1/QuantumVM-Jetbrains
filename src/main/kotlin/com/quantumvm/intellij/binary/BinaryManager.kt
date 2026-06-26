package com.quantumvm.intellij.binary

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.APP)
class BinaryManager {
    companion object {
        private const val LSP_BINARY_NAME = "qasm-lsp"
        private const val VM_BINARY_NAME = "QuantumVM"
        private const val STORAGE_DIR_NAME = "quantumvm"
    }

    private val downloader = BinaryDownloader()
    private val platformInfo = PlatformDetector.detectPlatform()
    private val downloadLocks = ConcurrentHashMap<String, Boolean>()

    private fun getStorageDir(): File {
        val pluginsPath = PathManager.getPluginsPath()
        val storageDir = File(pluginsPath, STORAGE_DIR_NAME)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return storageDir
    }

    private fun getBinaryPath(binaryBaseName: String): Path {
        val binaryName = platformInfo.getBinaryName(binaryBaseName)
        return Paths.get(getStorageDir().absolutePath, binaryName)
    }

    fun ensureLspBinary(forceDownload: Boolean = false, onComplete: ((Path) -> Unit)? = null) {
        ensureBinary(LSP_BINARY_NAME, "QASM Language Server", forceDownload, onComplete)
    }

    fun ensureVmBinary(forceDownload: Boolean = false, onComplete: ((Path) -> Unit)? = null) {
        ensureBinary(VM_BINARY_NAME, "QuantumVM", forceDownload, onComplete)
    }

    fun getLspBinaryPath(): Path? {
        val path = getBinaryPath(LSP_BINARY_NAME)
        return if (path.toFile().exists()) path else null
    }

    fun getVmBinaryPath(): Path? {
        val path = getBinaryPath(VM_BINARY_NAME)
        return if (path.toFile().exists()) path else null
    }

    private fun ensureBinary(
        binaryBaseName: String,
        title: String,
        forceDownload: Boolean,
        onComplete: ((Path) -> Unit)?
    ) {
        val binaryPath = getBinaryPath(binaryBaseName)
        val binaryFile = binaryPath.toFile()

        if (binaryFile.exists() && !forceDownload) {
            if (!System.getProperty("os.name").lowercase().contains("win")) binaryFile.setExecutable(true, false)
            onComplete?.invoke(binaryPath)
            return
        }

        val lockKey = "$binaryBaseName-$forceDownload"
        if (downloadLocks.putIfAbsent(lockKey, true) != null) return

        try {
            val release = downloader.fetchLatestRelease()
            val assetName = platformInfo.getBinaryName(binaryBaseName)
            val asset = downloader.findAsset(release, assetName)

            if (asset == null) {
                val errorMsg = "No binary found for ${platformInfo.platform}-${platformInfo.architecture}"
                downloader.showNotification("Binary Download Failed", errorMsg, NotificationType.ERROR)
                downloadLocks.remove(lockKey)
                return
            }

            downloader.downloadBinary(
                url = asset.browserDownloadUrl,
                destinationFile = binaryFile,
                title = "Downloading $title",
                onComplete = { file ->
                    downloadLocks.remove(lockKey)
                    downloader.showNotification(
                        "Download Complete",
                        "$title has been downloaded successfully",
                        NotificationType.INFORMATION
                    )
                    onComplete?.invoke(file.toPath())
                },
                onError = { error ->
                    downloadLocks.remove(lockKey)
                    downloader.showNotification(
                        "Download Failed",
                        "Failed to download $title: ${error.message}",
                        NotificationType.ERROR
                    )
                }
            )
        } catch (e: Exception) {
            downloadLocks.remove(lockKey)
            downloader.showNotification(
                "Download Failed",
                "Failed to download $title: ${e.message}",
                NotificationType.ERROR
            )
        }
    }
}
