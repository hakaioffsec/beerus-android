package io.hakaisecurity.beerusframework.core.models

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.hakaisecurity.beerusframework.core.functions.Start.Companion.installBeerusModule

class StartModel: ViewModel() {
    companion object {
        var hasRoot by mutableStateOf(false)
            private set

        var hasModule by mutableStateOf(false)
            private set

        var showRootModuleInstallerDialog by mutableStateOf(false)
            private set

        fun showsRootModuleInstallerDialog() {
            showRootModuleInstallerDialog = true
        }

        fun dismissRootModuleInstallerDialog() {
            showRootModuleInstallerDialog = false
        }

        fun confirmRootModuleInstallerDialog(context: Context) {
            showRootModuleInstallerDialog = false
            installBeerusModule(context)
        }

        fun updateHasRoot(value: Boolean){
            hasRoot = value
        }

        fun updateHasModule(value: Boolean){
            hasModule = value
        }
    }
}