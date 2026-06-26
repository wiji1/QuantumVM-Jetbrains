package com.quantumvm.intellij.language

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.plugins.textmate.api.TextMateBundleProvider
import java.io.File
import java.nio.file.Files

class QasmTextMateBundleProvider : TextMateBundleProvider {
    private val log = Logger.getInstance(QasmTextMateBundleProvider::class.java)

    override fun getBundles(): List<TextMateBundleProvider.PluginBundle> {
        try {
            val tempDir = Files.createTempDirectory("qasm-textmate-bundle").toFile()
            tempDir.deleteOnExit()

            val packageJsonResource = javaClass.classLoader.getResourceAsStream("textmate-bundle/package.json")
            if (packageJsonResource != null) {
                val packageJsonFile = File(tempDir, "package.json")
                packageJsonFile.deleteOnExit()
                FileUtil.copy(packageJsonResource, packageJsonFile.outputStream())
            }

            val syntaxesDir = File(tempDir, "syntaxes")
            syntaxesDir.mkdirs()
            syntaxesDir.deleteOnExit()

            val grammarResource = javaClass.classLoader.getResourceAsStream("textmate-bundle/syntaxes/qasm.tmLanguage.json")
            if (grammarResource != null) {
                val grammarFile = File(syntaxesDir, "qasm.tmLanguage.json")
                grammarFile.deleteOnExit()
                FileUtil.copy(grammarResource, grammarFile.outputStream())
            }

            return listOf(
                TextMateBundleProvider.PluginBundle("openqasm-textmate", tempDir.toPath())
            )
        } catch (e: Exception) {
            log.error("Failed to load TextMate bundle for OpenQASM", e)
            return emptyList()
        }
    }
}
