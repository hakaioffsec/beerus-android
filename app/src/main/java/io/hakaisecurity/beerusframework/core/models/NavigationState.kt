package io.hakaisecurity.beerusframework.core.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class NavigationState : ViewModel() {
    companion object {
        var animationStart by mutableStateOf(false)
            private set

        var moduleName by mutableStateOf("Home")
            private set

        fun updateNavigationState(newState: String) {
            moduleName = newState
        }

        fun updateanimationStartState(newState: Boolean) {
            animationStart = newState
        }
    }
}