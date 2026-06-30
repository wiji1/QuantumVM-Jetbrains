package com.quantumvm.intellij.binary

import java.util.*

data class PlatformInfo(
    val triple: String,
    val archiveExtension: String
) {
    fun getArchiveAssetName(): String {
        return "QuantumVM-$triple$archiveExtension"
    }

    fun getBinaryExtension(): String {
        return if (triple.contains("pc-windows")) ".exe" else ""
    }
}

object PlatformDetector {
    fun detectPlatform(): PlatformInfo {
        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
        val osArch = System.getProperty("os.arch").lowercase(Locale.getDefault())

        val arch = when {
            osArch.contains("aarch64") || osArch.contains("arm64") -> "aarch64"
            osArch.contains("x86_64") || osArch.contains("amd64") -> "x86_64"
            else -> throw UnsupportedOperationException("Unsupported architecture: $osArch")
        }

        val triple = when {
            osName.contains("win") -> "$arch-pc-windows-msvc"
            osName.contains("mac") || osName.contains("darwin") -> "$arch-apple-darwin"
            osName.contains("nux") || osName.contains("nix") -> "$arch-unknown-linux-gnu"
            else -> throw UnsupportedOperationException("Unsupported operating system: $osName")
        }

        val archiveExtension = if (osName.contains("win")) ".zip" else ".tar.xz"

        return PlatformInfo(triple, archiveExtension)
    }
}
