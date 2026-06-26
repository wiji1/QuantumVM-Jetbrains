package com.quantumvm.intellij.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(
    name = "QasmSettings",
    storages = [Storage("quantumvm.xml")]
)
class QasmSettingsComponent : PersistentStateComponent<QasmSettings> {
    private var myState = QasmSettings()

    @get:JvmName("getSettings")
    val state: QasmSettings
        get() = myState

    override fun getState(): QasmSettings = myState

    override fun loadState(state: QasmSettings) {
        this.myState = state
    }
}
