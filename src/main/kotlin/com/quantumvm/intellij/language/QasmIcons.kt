package com.quantumvm.intellij.language

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object QasmIcons {
    @JvmField
    val FILE: Icon = IconLoader.getIcon("/icons/qasm.svg", QasmIcons::class.java)

    @JvmField
    val PLUGIN: Icon = IconLoader.getIcon("/META-INF/pluginIcon.svg", QasmIcons::class.java)
}
