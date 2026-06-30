package com.quantumvm.intellij.binary

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

@Service(Service.Level.APP)
class BinaryManager {
    companion object {
        private const val LSP_BINARY_NAME = "qasm-lsp"
        private const val VM_BINARY_NAME = "QuantumVM"
        private const val STORAGE_DIR_NAME = "quantumvm"
    }

    private val downloader = BinaryDownloader()
    private val platformInfo = PlatformDetector.detectPlatform()
    private val extractionLock = AtomicBoolean(false)
    private val extractionDone = AtomicBoolean(false)
    private val pendingCallbacks = CopyOnWriteArrayList<() -> Unit>()

    private fun getStorageDir(): File {
        val pluginsPath = PathManager.getPluginsPath()
        val storageDir = File(pluginsPath, STORAGE_DIR_NAME)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return storageDir
    }

    private fun getBinaryPath(binaryBaseName: String): Path {
        val binaryName = "$binaryBaseName${platformInfo.getBinaryExtension()}"
        return Paths.get(getStorageDir().absolutePath, binaryName)
    }

    fun ensureLspBinary(forceDownload: Boolean = false, onComplete: ((Path) -> Unit)? = null) {
        ensureBinariesInternal(forceDownload) {
            val path = getBinaryPath(LSP_BINARY_NAME)
            onComplete?.invoke(path)
        }
    }

    fun ensureVmBinary(forceDownload: Boolean = false, onComplete: ((Path) -> Unit)? = null) {
        ensureBinariesInternal(forceDownload) {
            val path = getBinaryPath(VM_BINARY_NAME)
            onComplete?.invoke(path)
        }
    }

    fun ensureBinaries(forceDownload: Boolean = false, onComplete: (() -> Unit)? = null) {
        ensureBinariesInternal(forceDownload) {
            onComplete?.invoke()
        }
    }

    fun getLspBinaryPath(): Path? {
        val path = getBinaryPath(LSP_BINARY_NAME)
        return if (path.toFile().exists()) path else null
    }

    fun getVmBinaryPath(): Path? {
        val path = getBinaryPath(VM_BINARY_NAME)
        return if (path.toFile().exists()) path else null
    }

    private fun ensureBinariesInternal(forceDownload: Boolean, onComplete: () -> Unit) {
        if (!forceDownload && extractionDone.get()) {
            setExecutableOnBinaries()
            onComplete()
            return
        }

        if (!extractionLock.compareAndSet(false, true)) {
            pendingCallbacks.add(onComplete)
            return
        }

        try {
            val lspPath = getBinaryPath(LSP_BINARY_NAME)
            val vmPath = getBinaryPath(VM_BINARY_NAME)

            if (!forceDownload && lspPath.toFile().exists() && vmPath.toFile().exists()) {
                extractionDone.set(true)
                setExecutableOnBinaries()
                firePendingCallbacks()
                onComplete()
                return
            }

            pendingCallbacks.add(onComplete)

            val release = downloader.fetchLatestRelease()
            val assetName = platformInfo.getArchiveAssetName()
            val asset = downloader.findAsset(release, assetName)

            if (asset == null) {
                val errorMsg = "No archive found for ${platformInfo.triple}"
                downloader.showNotification("Binary Download Failed", errorMsg, NotificationType.ERROR)
                return
            }

            val storageDir = getStorageDir()
            val archiveFile = File(storageDir, assetName)

            downloader.downloadAndExtractArchive(
                url = asset.browserDownloadUrl,
                archiveFile = archiveFile,
                extractDir = storageDir,
                title = "Downloading QuantumVM binaries",
                onComplete = {
                    extractionDone.set(true)
                    setExecutableOnBinaries()
                    downloader.showNotification(
                        "Download Complete",
                        "QuantumVM and qasm-lsp have been downloaded successfully",
                        NotificationType.INFORMATION
                    )
                    firePendingCallbacks()
                },
                onError = { error ->
                    pendingCallbacks.clear()
                    downloader.showNotification(
                        "Download Failed",
                        "Failed to download binaries: ${error.message}",
                        NotificationType.ERROR
                    )
                }
            )
        } catch (e: Exception) {
            pendingCallbacks.clear()
            downloader.showNotification(
                "Download Failed",
                "Failed to download binaries: ${e.message}",
                NotificationType.ERROR
            )
        }
    }

    private fun firePendingCallbacks() {
        val callbacks = pendingCallbacks.toList()
        pendingCallbacks.clear()
        callbacks.forEach { it() }
    }

    private fun setExecutableOnBinaries() {
        if (System.getProperty("os.name").lowercase().contains("win")) return
        getBinaryPath(LSP_BINARY_NAME).toFile().setExecutable(true, false)
        getBinaryPath(VM_BINARY_NAME).toFile().setExecutable(true, false)
    }
}
