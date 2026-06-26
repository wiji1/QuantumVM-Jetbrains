package com.quantumvm.intellij.language

import com.intellij.lang.Language

object QasmLanguage : Language("qasm") {
    override fun getDisplayName(): String = "OpenQASM"

    override fun isCaseSensitive(): Boolean = true
}
