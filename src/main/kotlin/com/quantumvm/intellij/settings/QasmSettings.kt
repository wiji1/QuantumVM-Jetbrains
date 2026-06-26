package com.quantumvm.intellij.settings

enum class LspTraceLevel {
    OFF,
    MESSAGES,
    VERBOSE
}

data class QasmSettings(
    var lspServerPath: String = "qasm-lsp",
    var lspTraceLevel: LspTraceLevel = LspTraceLevel.OFF,
    var autoUpdate: Boolean = true,
    var vmBinaryPath: String = ""
)
