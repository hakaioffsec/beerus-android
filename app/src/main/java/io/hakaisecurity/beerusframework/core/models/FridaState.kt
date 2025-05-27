package io.hakaisecurity.beerusframework.core.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.hakaisecurity.beerusframework.core.functions.frida.FridaSetup.Companion.checkFridaProcessState

class FridaState : ViewModel() {
    companion object {
        var fridaRunningState by mutableStateOf("start")
            private set

        var currentFridaVersionDownloaded by mutableStateOf<String>("None")
            private set

        var fridaVersions = mutableStateListOf<String>()
        var currentFridaVersionFromList by mutableStateOf<String?>(null)

        var packageName by mutableStateOf("")

        var inEditorMode by mutableStateOf(false)

        init {
            checkFridaProcessState { newState ->
                fridaRunningState = newState
            }
        }

        fun updateFridaState(newState: String) {
            fridaRunningState = newState
        }

        fun updateFridaDownloadedVersion(newVersion: String) {
            currentFridaVersionDownloaded = newVersion
        }

        fun updatePackageName(pkgName: String) {
            packageName = pkgName
        }
    }
}
