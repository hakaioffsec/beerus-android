package io.hakaisecurity.beerusframework.core.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.hakaisecurity.beerusframework.core.functions.adb.AdbOverNetwork.Companion.adbStatus

class AdbState : ViewModel() {
    companion object {
        var adbRunningState by mutableStateOf(false)
            private set

        init {
            adbStatus{ state ->
                adbRunningState = state == "5555"
            }
        }

        fun updateAdbState(status: Boolean) {
            adbRunningState = status
        }
    }
}