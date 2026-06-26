package com.quantumvm.intellij.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.quantumvm.intellij.language.QasmFileType
import com.redhat.devtools.lsp4ij.LanguageServerFactory

class QasmLspServerSupportProvider : LanguageServerFactory {

    override fun createConnectionProvider(project: Project) =
        QasmLspServerDescriptor().createConnectionProvider(project)

    companion object {
        fun isLspEnabledForFile(file: VirtualFile): Boolean {
            return file.fileType == QasmFileType
        }
    }
}
