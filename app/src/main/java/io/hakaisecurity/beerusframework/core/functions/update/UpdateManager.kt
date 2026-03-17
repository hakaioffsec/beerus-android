package io.hakaisecurity.beerusframework.core.functions.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.FileProvider
import io.hakaisecurity.beerusframework.core.models.UpdateState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

object UpdateManager {

    private const val GITHUB_API_URL = "https://api.github.com/repos/hakaioffsec/beerus-android/releases/latest"
    private val client = OkHttpClient()

    fun getCurrentVersion(context: Context): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun calculateSHA256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        
        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val latestPart = latestParts.getOrElse(i) { 0 }
            val currentPart = currentParts.getOrElse(i) { 0 }
            
            if (latestPart > currentPart) return true
            if (latestPart < currentPart) return false
        }
        return false
    }
    
    suspend fun checkForUpdates(context: Context, showDialogIfAvailable: Boolean = true) {
        withContext(Dispatchers.IO) {
            try {
                UpdateState.updateIsChecking(true)
                UpdateState.updateError(null)

                val currentVersion = getCurrentVersion(context)
                UpdateState.updateCurrentVersion(currentVersion)

                val request = Request.Builder()
                    .url(GITHUB_API_URL)
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        UpdateState.updateError("Failed to check for updates: ${response.code}")
                        return@withContext
                    }

                    val json = JSONObject(response.body?.string() ?: "")
                    val tagName = json.optString("tag_name", "").removePrefix("v").removePrefix("V")
                    val assets = json.optJSONArray("assets")


                    var apkUrl = ""
                    var sha256Hash = ""

                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            val downloadUrl = asset.optString("browser_download_url", "")

                            if (name.endsWith(".apk")) {
                                apkUrl = downloadUrl
                                val digest = asset.optString("digest", "")
                                if (digest.startsWith("sha256:")) {
                                    sha256Hash = digest.removePrefix("sha256:")
                                }
                            }
                        }
                    }

                    UpdateState.updateLatestVersion(tagName)
                    UpdateState.updateDownloadUrl(apkUrl)
                    UpdateState.updateSha256Url(sha256Hash)

                    val currentClean = currentVersion.removePrefix("v").removePrefix("V")
                    val isUpdateAvailable = tagName.isNotEmpty() &&
                                           currentClean.isNotEmpty() &&
                                           isNewerVersion(tagName, currentClean) &&
                                           apkUrl.isNotEmpty()

                    UpdateState.updateUpdateAvailable(isUpdateAvailable)

                    if (isUpdateAvailable && showDialogIfAvailable) {
                        withContext(Dispatchers.Main) {
                            UpdateState.showDialog()
                        }
                    }
                }
            } catch (e: Exception) {
                UpdateState.updateError("Error: ${e.message}")
            } finally {
                UpdateState.updateIsChecking(false)
            }
        }
    }

    suspend fun downloadAndInstall(context: Context) {
        val downloadUrl = UpdateState.downloadUrl
        if (downloadUrl.isEmpty()) {
            UpdateState.updateError("No download URL available")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                UpdateState.updateIsDownloading(true)
                UpdateState.updateDownloadProgress(0f)
                UpdateState.updateError(null)

                val expectedSha256 = UpdateState.sha256Url.ifEmpty { null }

                val request = Request.Builder()
                    .url(downloadUrl)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        UpdateState.updateError("Download failed: ${response.code}")
                        return@withContext
                    }

                    val body = response.body ?: run {
                        UpdateState.updateError("Empty response")
                        return@withContext
                    }

                    val contentLength = body.contentLength()
                    val apkFile = File(context.cacheDir, "update.apk")

                    FileOutputStream(apkFile).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytesRead = 0L

                        body.byteStream().use { input ->
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead

                                if (contentLength > 0) {
                                    val progress = totalBytesRead.toFloat() / contentLength.toFloat()
                                    UpdateState.updateDownloadProgress(progress)
                                }
                            }
                        }
                    }

                    if (expectedSha256 != null) {
                        val actualSha256 = calculateSHA256(apkFile)
                        if (actualSha256.lowercase() != expectedSha256.lowercase()) {
                            apkFile.delete()
                            UpdateState.updateError("SHA256 verification failed! File may be corrupted or tampered.")
                            return@withContext
                        }
                    }

                    withContext(Dispatchers.Main) {
                        installApk(context, apkFile)
                    }
                }
            } catch (e: Exception) {
                UpdateState.updateError("Download error: ${e.message}")
            } finally {
                UpdateState.updateIsDownloading(false)
            }
        }
    }
    
    private fun installApk(context: Context, apkFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            UpdateState.updateError("Installation error: ${e.message}")
        }
    }
}
