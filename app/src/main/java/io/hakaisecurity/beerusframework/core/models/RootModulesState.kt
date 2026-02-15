package io.hakaisecurity.beerusframework.core.models

import androidx.compose.runtime.mutableStateListOf

object RootModulesState {
    val rootModulePaths = mutableStateListOf<String>()

    fun setModulePaths(paths: List<String>) {
        rootModulePaths.clear()
        rootModulePaths.addAll(paths)
    }

    fun clear() {
        rootModulePaths.clear()
    }
}
