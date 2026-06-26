package com.quantumvm.intellij.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*

class QasmSettingsConfigurable : BoundConfigurable("OpenQASM") {
    private val settings = ApplicationManager.getApplication().getService(QasmSettingsComponent::class.java)

    override fun createPanel(): DialogPanel {
        return panel {
            group("Language Server") {
                row("LSP Server Path:") {
                    textFieldWithBrowseButton(
                        FileChooserDescriptorFactory.createSingleFileDescriptor()
                            .withTitle("Select QASM LSP Binary")
                    )
                        .bindText(settings.state::lspServerPath)
                        .comment("Path to qasm-lsp binary. Leave as 'qasm-lsp' to use auto-downloaded binary.")
                        .align(AlignX.FILL)
                }

                row("Trace Level:") {
                    comboBox(LspTraceLevel.entries)
                        .bindItem(settings.state::lspTraceLevel.toNullableProperty())
                        .comment("Verbosity of LSP communication logs")
                }
            }

            group("Runtime") {
                row("QuantumVM Binary Path:") {
                    textFieldWithBrowseButton(
                        FileChooserDescriptorFactory.createSingleFileDescriptor()
                            .withTitle("Select QuantumVM Binary")
                    )
                        .bindText(settings.state::vmBinaryPath)
                        .comment("Custom path to QuantumVM binary. Leave empty to use auto-downloaded binary.")
                        .align(AlignX.FILL)
                }
            }

            group("Updates") {
                row {
                    checkBox("Automatically check for binary updates")
                        .bindSelected(settings.state::autoUpdate)
                        .comment("Check for new qasm-lsp and QuantumVM releases on startup")
                }
            }
        }
    }
}
