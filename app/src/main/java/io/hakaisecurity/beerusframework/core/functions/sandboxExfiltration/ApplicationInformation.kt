package io.hakaisecurity.beerusframework.core.functions.sandboxExfiltration

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import io.hakaisecurity.beerusframework.core.models.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

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
            return drawableToBitmap(drawable)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 64
            val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 64
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    suspend fun fetchApplications(path: String): List<Application> =
        withContext(Dispatchers.IO) {
            val apps = mutableListOf<Application>()
            val packagePattern = Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$")

            val bannedTerms = listOf(".auto_generated_")
            val bannedPatterns = listOf(
                Regex("com\\.android\\..*"),
                Regex("com\\.google\\..*")
            )

            try {
                val process = Runtime.getRuntime().exec("su")
                DataOutputStream(process.outputStream).use { outputStream ->
                    outputStream.writeBytes("ls $path\n")
                    outputStream.writeBytes("exit\n")
                    outputStream.flush()
                }

                process.waitFor()

                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line!!.isNotEmpty()) {
                            val packageName = line!!

                            val containsBannedTerm = bannedTerms.any { packageName.contains(it) }
                            val matchesBannedPattern =
                                bannedPatterns.any { it.matches(packageName) }

                            if (!containsBannedTerm && !matchesBannedPattern && packagePattern.matches(
                                    packageName
                                )
                            ) {
                                apps.add(
                                    Application(
                                        artifactPath = getAppApkLocation(packageName) ?: "",
                                        icon = getAppIconBitmap(packageName),
                                        container = packageName,
                                        identifier = packageName,
                                        name = getAppName(packageName) ?: "Unknown"
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
