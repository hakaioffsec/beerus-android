package io.hakaisecurity.beerusframework.core.models

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.hakaisecurity.beerusframework.core.functions.Start.Companion.installBeerusModule

class StartModel: ViewModel() {
    companion object {
        var hasMagisk by mutableStateOf(false)
            private set

        var hasModule by mutableStateOf(false)
            private set

        var showMagiskModuleInstallerDialog by mutableStateOf(false)
            private set

        fun showsMagiskModuleInstallerDialog() {
            showMagiskModuleInstallerDialog = true
        }

        fun dismissMagiskModuleInstallerDialog() {
            showMagiskModuleInstallerDialog = false
        }

        fun confirmMagiskModuleInstallerDialog(context: Context) {
            showMagiskModuleInstallerDialog = false
            installBeerusModule(context)
        }

        fun updateHasMagisk(value: Boolean){
            hasMagisk = value
        }

        fun updateHasModule(value: Boolean){
            hasModule = value
        }
    }
}