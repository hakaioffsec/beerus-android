package io.hakaisecurity.beerusframework.core.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.hakaisecurity.beerusframework.core.functions.frida.FridaSetup.Companion.checkFridaProcessState

class FridaState : ViewModel() {
    companion object{
        var fridaRunningState by mutableStateOf("start")
            private set

        init {
            checkFridaProcessState { newState ->
                fridaRunningState = newState
            }
        }

        fun updateFridaState(newState: String) {
            fridaRunningState = newState
        }
    }
}