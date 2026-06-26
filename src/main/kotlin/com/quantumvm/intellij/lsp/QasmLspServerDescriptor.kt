package com.quantumvm.intellij.lsp

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.quantumvm.intellij.binary.BinaryManager
import com.quantumvm.intellij.settings.QasmSettingsComponent
import com.redhat.devtools.lsp4ij.LanguageServerFactory
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class QasmLspServerDescriptor : LanguageServerFactory {

    override fun createConnectionProvider(project: Project): StreamConnectionProvider {
        return QasmStreamConnectionProvider(project)
    }
}

class QasmStreamConnectionProvider(private val project: Project) : StreamConnectionProvider {
    private var process: Process? = null

    override fun start() {
        val settings = ApplicationManager.getApplication().getService(QasmSettingsComponent::class.java)
        val binaryManager = ApplicationManager.getApplication().getService(BinaryManager::class.java)

        println("[QASM-LSP] Starting LSP server...")

        var serverPath: String? = settings.state.lspServerPath

        if (!serverPath.isNullOrEmpty() && serverPath != "qasm-lsp") {
            if (!File(serverPath).exists()) serverPath = null
        } else serverPath = null

        if (serverPath == null) {
            val basePath = project.basePath
            if (basePath != null) {
                val localReleasePath = File(basePath, "target/release/qasm-lsp")
                val localDebugPath = File(basePath, "target/debug/qasm-lsp")

                when {
                    localReleasePath.exists() -> serverPath = localReleasePath.absolutePath
                    localDebugPath.exists() -> serverPath = localDebugPath.absolutePath
                }
            }
        }

        if (serverPath == null) {
            val binaryPath = binaryManager.getLspBinaryPath()
            if (binaryPath != null) {
                serverPath = binaryPath.toString()
            } else {
                binaryManager.ensureLspBinary(forceDownload = false)
                throw RuntimeException("QASM Language Server binary is being downloaded. Please wait and try again.")
            }
        }

        println("[QASM-LSP] Starting process: $serverPath")
        val processBuilder = ProcessBuilder(serverPath)
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)

        process = processBuilder.start()
        println("[QASM-LSP] LSP server started successfully, PID: ${process?.pid()}")
    }

    override fun getInputStream(): InputStream? {
        return process?.inputStream
    }

    override fun getOutputStream(): OutputStream? {
        return process?.outputStream
    }

    override fun stop() {
        process?.destroy()
        process = null
    }
}
