package com.quantumvm.intellij.language

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object QasmFileType : LanguageFileType(QasmLanguage) {
    override fun getName(): String = "QASM"

    override fun getDescription(): String = "OpenQASM 3.0 file"

    override fun getDefaultExtension(): String = "qasm"

    override fun getIcon(): Icon = QasmIcons.FILE
}
