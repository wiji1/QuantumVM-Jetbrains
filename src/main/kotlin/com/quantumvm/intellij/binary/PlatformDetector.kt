package com.quantumvm.intellij.binary

import java.util.*

data class PlatformInfo(
    val platform: String,
    val architecture: String,
    val extension: String
) {
    fun getBinaryName(baseName: String): String {
        return "$baseName-$platform-$architecture$extension"
    }
}

object PlatformDetector {
    fun detectPlatform(): PlatformInfo {
        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
        val osArch = System.getProperty("os.arch").lowercase(Locale.getDefault())

        val platform = when {
            osName.contains("win") -> "windows"
            osName.contains("mac") || osName.contains("darwin") -> "macos"
            osName.contains("nux") || osName.contains("nix") -> "linux"
            else -> throw UnsupportedOperationException("Unsupported operating system: $osName")
        }

        val architecture = when {
            osArch.contains("aarch64") || osArch.contains("arm64") -> "aarch64"
            osArch.contains("x86_64") || osArch.contains("amd64") -> "x86_64"
            else -> throw UnsupportedOperationException("Unsupported architecture: $osArch")
        }

        val extension = if (platform == "windows") ".exe" else ""

        return PlatformInfo(platform, architecture, extension)
    }
}
