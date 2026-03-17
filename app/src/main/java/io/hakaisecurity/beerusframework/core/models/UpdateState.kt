package io.hakaisecurity.beerusframework.core.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object UpdateState {
    var isChecking by mutableStateOf(false)
        private set
    
    var isDownloading by mutableStateOf(false)
        private set
    
    var downloadProgress by mutableStateOf(0f)
        private set
    
    var currentVersion by mutableStateOf("")
        private set
    
    var latestVersion by mutableStateOf("")
        private set
    
    var downloadUrl by mutableStateOf("")
        private set
    
    var sha256Url by mutableStateOf("")
        private set
    
    var updateAvailable by mutableStateOf(false)
        private set
    
    var showUpdateDialog by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    fun updateIsChecking(value: Boolean) {
        isChecking = value
    }
    
    fun updateIsDownloading(value: Boolean) {
        isDownloading = value
    }
    
    fun updateDownloadProgress(value: Float) {
        downloadProgress = value
    }
    
    fun updateCurrentVersion(value: String) {
        currentVersion = value
    }
    
    fun updateLatestVersion(value: String) {
        latestVersion = value
    }
    
    fun updateDownloadUrl(value: String) {
        downloadUrl = value
    }
    
    fun updateSha256Url(value: String) {
        sha256Url = value
    }
    
    fun updateUpdateAvailable(value: Boolean) {
        updateAvailable = value
    }
    
    fun showDialog() {
        showUpdateDialog = true
    }
    
    fun dismissDialog() {
        showUpdateDialog = false
    }
    
    fun updateError(message: String?) {
        errorMessage = message
    }
    
    fun reset() {
        isChecking = false
        isDownloading = false
        downloadProgress = 0f
        latestVersion = ""
        downloadUrl = ""
        sha256Url = ""
        updateAvailable = false
        errorMessage = null
    }
}
