package com.quantumvm.intellij

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.TypedActionHandler
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager

class QasmTypedHandler(private val originalHandler: TypedActionHandler) : TypedActionHandler {
    companion object {
        private val LOG = Logger.getInstance(QasmTypedHandler::class.java)
        private val PAIRS = mapOf('(' to ')', '{' to '}', '[' to ']')

        fun install() {
            val typedAction = TypedAction.getInstance()
            val currentHandler = typedAction.handler
            LOG.info("Installing QasmTypedHandler. Current handler: ${currentHandler::class.java.name}")
            if (currentHandler !is QasmTypedHandler) {
                typedAction.setupHandler(QasmTypedHandler(currentHandler))
                LOG.info("QasmTypedHandler installed successfully")
            } else {
                LOG.info("QasmTypedHandler already installed, skipping")
            }
        }
    }

    init {
        LOG.info("QasmTypedHandler constructed, original=${originalHandler::class.java.name}")
    }

    override fun execute(editor: Editor, charTyped: Char, dataContext: DataContext) {
        if (charTyped in PAIRS) {
            val virtualFile = FileDocumentManager.getInstance().getFile(editor.document)
            val ext = virtualFile?.extension

            if (ext == "qasm") {
                handleAutoClose(editor, charTyped)
                return
            }
        }

        originalHandler.execute(editor, charTyped, dataContext)
    }

    private fun handleAutoClose(editor: Editor, charTyped: Char) {
        val closing = PAIRS[charTyped] ?: return
        val document = editor.document
        val offset = editor.caretModel.offset
        val text = document.text

        if (offset < text.length && text[offset] == closing) {
            editor.caretModel.moveToOffset(offset + 1)
            return
        }

        val project = editor.project
        if (project == null) {
            originalHandler.execute(editor, charTyped, DataContext.EMPTY_CONTEXT)
            return
        }

        WriteCommandAction.runWriteCommandAction(project) {
            document.insertString(offset, "$charTyped$closing")
            editor.caretModel.moveToOffset(offset + 1)
        }
    }
}
