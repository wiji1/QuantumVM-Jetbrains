package com.quantumvm.intellij.runner

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.quantumvm.intellij.language.QasmFileType

class QasmRunLineMarkerContributor : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): Info? {
        val psiFile = element.containingFile ?: return null
        if (psiFile.fileType != QasmFileType) return null

        if (element.parent != psiFile || element.prevSibling != null) return null

        return Info(
            AllIcons.RunConfigurations.TestState.Run,
            { "Run QASM file" },
            *ExecutorAction.getActions(0)
        )
    }
}
