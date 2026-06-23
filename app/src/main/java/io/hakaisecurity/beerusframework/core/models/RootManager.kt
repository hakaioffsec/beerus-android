package io.hakaisecurity.beerusframework.core.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.hakaisecurity.beerusframework.core.utils.CommandUtils

class RootManager : ViewModel() {
    companion object {
        var showRootDialog by mutableStateOf(false)
            private set

        fun showsRootDialog() {
            showRootDialog = true
        }

        fun dismissRootDialog() {
            showRootDialog = false
        }

        fun confirmRootDialog() {
            showRootDialog = false
            CommandUtils.runSuCommand("reboot") {}
        }
    }
}