package io.hakaisecurity.beerusframework.core.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.hakaisecurity.beerusframework.core.utils.CommandUtils

class MagiskManager : ViewModel() {
    companion object {
        var showMagiskDialog by mutableStateOf(false)
            private set

        fun showsMagiskDialog() {
            showMagiskDialog = true
        }

        fun dismissMagiskDialog() {
            showMagiskDialog = false
        }

        fun confirmMagiskDialog() {
            showMagiskDialog = false
            CommandUtils.runSuCommand("reboot") {}
        }
    }
}