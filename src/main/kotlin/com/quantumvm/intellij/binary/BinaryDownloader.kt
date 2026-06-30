package com.quantumvm.intellij.binary

import com.google.gson.JsonParser
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class GitHubRelease(
    val tagName: String,
    val assets: List<GitHubAsset>
)


data class GitHubAsset(
    val name: String,
    val browserDownloadUrl: String
)

class BinaryDownloader {
    companion object {
        private const val GITHUB_REPO = "wiji1/QuantumVM"
        private const val USER_AGENT = "QuantumVM-IntelliJ-Plugin"
        private const val API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"
        private const val MAX_REDIRECTS = 5
    }

    fun fetchLatestRelease(): GitHubRelease {
        val connection = URI(API_URL).toURL().openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", USER_AGENT)
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        try {
            if (connection.responseCode != 200) {
                throw RuntimeException("Failed to fetch release: HTTP ${connection.responseCode}")
            }

            val reader = InputStreamReader(connection.inputStream)
            val jsonObject = JsonParser.parseReader(reader).asJsonObject

            val tagName = jsonObject.get("tag_name").asString
            val assetsArray = jsonObject.getAsJsonArray("assets")

            val assets = assetsArray.map { assetElement ->
                val assetObj = assetElement.asJsonObject
                GitHubAsset(
                    name = assetObj.get("name").asString,
                    browserDownloadUrl = assetObj.get("browser_download_url").asString
                )
            }

            return GitHubRelease(tagName, assets)
        } finally {
            connection.disconnect()
        }
    }

    fun downloadBinary(
        url: String,
        destinationFile: File,
        title: String,
        onComplete: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(null, title, false) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    indicator.text = "Fetching download URL..."
                    indicator.isIndeterminate = false

                    val tmpFile = File(destinationFile.absolutePath + ".tmp")

                    destinationFile.parentFile?.mkdirs()

                    downloadWithRedirects(url, tmpFile, indicator, 0)

                    if (destinationFile.exists()) destinationFile.delete()
                    tmpFile.renameTo(destinationFile)

                    indicator.text = "Download complete"
                    indicator.fraction = 1.0

                    onComplete(destinationFile)
                } catch (e: Exception) {
                    onError(e)
                }
            }
        })
    }

    fun downloadAndExtractArchive(
        url: String,
        archiveFile: File,
        extractDir: File,
        title: String,
        onComplete: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(null, title, false) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    indicator.text = "Downloading archive..."
                    indicator.isIndeterminate = false

                    val tmpFile = File(archiveFile.absolutePath + ".tmp")
                    archiveFile.parentFile?.mkdirs()

                    downloadWithRedirects(url, tmpFile, indicator, 0)

                    if (archiveFile.exists()) archiveFile.delete()
                    tmpFile.renameTo(archiveFile)

                    indicator.text = "Extracting archive..."
                    indicator.fraction = 0.0

                    extractArchive(archiveFile, extractDir)

                    indicator.text = "Extraction complete"
                    indicator.fraction = 1.0

                    onComplete(extractDir)
                } catch (e: Exception) {
                    onError(e)
                }
            }
        })
    }

    private fun extractArchive(archiveFile: File, targetDir: File) {
        targetDir.mkdirs()

        when {
            archiveFile.name.endsWith(".tar.xz") -> extractTarXz(archiveFile, targetDir)
            archiveFile.name.endsWith(".zip") -> extractZip(archiveFile, targetDir)
            else -> throw RuntimeException("Unsupported archive format: ${archiveFile.name}")
        }
    }

    private fun extractTarXz(archiveFile: File, targetDir: File) {
        val process = ProcessBuilder("tar", "-xJf", archiveFile.absolutePath)
            .directory(targetDir)
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            val errorOutput = process.inputStream.bufferedReader().readText()
            throw RuntimeException("Failed to extract tar.xz: tar exited with code $exitCode\n$errorOutput")
        }

        // Archives produced by cargo-dist have a top-level directory named after the target triple.
        // Move any binaries from that subdirectory up to the target directory.
        val subDirs = targetDir.listFiles()?.filter { it.isDirectory } ?: emptyList()
        if (subDirs.size == 1) {
            val subDir = subDirs.first()
            subDir.listFiles()?.forEach { file ->
                val dest = File(targetDir, file.name)
                if (!dest.exists()) {
                    file.renameTo(dest)
                }
            }
            subDir.delete()
        }

        targetDir.listFiles()?.forEach { file ->
            if (!System.getProperty("os.name").lowercase().contains("win")) {
                file.setExecutable(true, false)
            }
        }
    }

    private fun extractZip(archiveFile: File, targetDir: File) {
        ZipInputStream(archiveFile.inputStream()).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val outputFile = File(targetDir, entry.name)

                if (entry.isDirectory) {
                    outputFile.mkdirs()
                } else {
                    outputFile.parentFile?.mkdirs()
                    FileOutputStream(outputFile).use { fos ->
                        zis.copyTo(fos)
                    }
                    if (!System.getProperty("os.name").lowercase().contains("win")) {
                        outputFile.setExecutable(true, false)
                    }
                }

                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private fun downloadWithRedirects(
        url: String,
        destinationFile: File,
        indicator: ProgressIndicator,
        redirectCount: Int
    ) {
        if (redirectCount >= MAX_REDIRECTS) throw RuntimeException("Too many redirects")

        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", USER_AGENT)
        connection.instanceFollowRedirects = false
        connection.connectTimeout = 30000
        connection.readTimeout = 30000

        try {
            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                val redirectUrl = connection.getHeaderField("Location")
                if (redirectUrl != null) {
                    downloadWithRedirects(redirectUrl, destinationFile, indicator, redirectCount + 1)
                    return
                }
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("Failed to download: HTTP $responseCode")
            }

            val contentLength = connection.contentLengthLong
            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(destinationFile)

            try {
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    if (contentLength > 0) {
                        val progress = (totalBytesRead.toDouble() / contentLength.toDouble())
                        indicator.fraction = progress
                        indicator.text = "Downloading... ${(progress * 100).toInt()}%"
                    } else {
                        indicator.text = "Downloading... ${totalBytesRead / 1024} KB"
                    }
                }
            } finally {
                inputStream.close()
                outputStream.close()
            }
        } finally {
            connection.disconnect()
        }
    }

    fun findAsset(release: GitHubRelease, assetName: String): GitHubAsset? {
        return release.assets.find { it.name == assetName }
    }

    fun showNotification(title: String, content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("QuantumVM Notifications")
            .createNotification(title, content, type)
            .notify(null)
    }
}
