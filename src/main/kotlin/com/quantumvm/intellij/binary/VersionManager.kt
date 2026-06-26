package com.quantumvm.intellij.binary

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

data class VersionState(
    var lspVersion: String = "",
    var vmVersion: String = "",
    var lastUpdateCheck: Long = 0
)

@Service(Service.Level.APP)
@State(
    name = "QuantumVMVersions",
    storages = [Storage("quantumvm.xml")]
)
class VersionManager : PersistentStateComponent<VersionState> {
    private var state = VersionState()

    override fun getState(): VersionState = state

    override fun loadState(state: VersionState) {
        this.state = state
    }

    fun getLspVersion(): String = state.lspVersion

    fun getVmVersion(): String = state.vmVersion

    fun updateLastUpdateCheck() {
        state.lastUpdateCheck = System.currentTimeMillis()
    }

    fun shouldCheckForUpdates(): Boolean {
        val oneDayInMillis = 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()
        return (now - state.lastUpdateCheck) > oneDayInMillis
    }

    fun updateVersions(lspVersion: String, vmVersion: String) {
        state.lspVersion = lspVersion
        state.vmVersion = vmVersion
        updateLastUpdateCheck()
    }
}
