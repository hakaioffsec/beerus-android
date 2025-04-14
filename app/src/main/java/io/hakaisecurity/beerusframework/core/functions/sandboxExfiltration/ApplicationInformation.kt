package io.hakaisecurity.beerusframework.core.functions.sandboxExfiltration

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import android.util.Base64
import androidx.core.graphics.createBitmap

class ApplicationInformation(private val context: Context) {

    private fun getAppApkLocation(packageName: String): String? {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            applicationInfo.sourceDir
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getAppName(packageName: String): String? {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun getAppIconBitmap(packageName: String): Bitmap? {
        return try {
            val packageManager = context.packageManager
            val drawable = packageManager.getApplicationIcon(packageName)
            drawabletoBitmap(drawable)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawabletoBitmap(drawable: Drawable): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 64
            val heigh = drawable.intrinsicHeight.takeIf { it > 0 } ?: 64
            val bitmap = createBitmap(width, heigh)
            val canvas = Canvas(bitmap)

            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    suspend fun fetchApplications(path: String): List<Application> = withContext(Dispatchers.IO) {
        val apps = mutableListOf<Application>()

        try {
            val process = Runtime.getRuntime().exec("su")
            DataOutputStream(process.outputStream).use { outputStrem ->
                outputStrem.writeBytes("ls $path\n")
                outputStrem.writeBytes("exit\n")
                outputStrem.flush()
            }

            process.waitFor()

            BufferedReader(InputStreamReader(process.inputStream)).use { reader->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val packageName = line?.trim()
                    if (!packageName.isNullOrEmpty()) {
                        val apkPath = getAppApkLocation(packageName)
                        if (!apkPath.isNullOrEmpty()) {
                            apps.add(
                                Application(
                                    artifactPath = apkPath,
                                    icon = getAppIconBitmap(packageName),
                                    container = packageName,
                                    identifier = packageName,
                                    name = getAppName(packageName)
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

            return@withContext apps
    }
}

data class Application(
    val artifactPath: String,
    val icon: Bitmap?,
    val container: String?,
    val identifier: String,
    val name: String?
)

