package com.quantumvm.intellij.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.quantumvm.intellij.update.UpdateChecker

class CheckForUpdatesAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val updateChecker = ApplicationManager.getApplication().getService(UpdateChecker::class.java)
        updateChecker.checkForUpdates(manual = true)
    }
}
