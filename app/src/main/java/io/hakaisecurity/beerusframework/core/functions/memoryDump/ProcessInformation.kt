package io.hakaisecurity.beerusframework.core.functions.memoryDump

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import io.hakaisecurity.beerusframework.core.models.Process
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProcessInformation (private val context: Context) {
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

    suspend fun fetchProcesses(): List<Process> =
        withContext(Dispatchers.IO) {
            val apps = mutableListOf<Process>()
            val result = CompletableDeferred<String>()

            runSuCommand("ps -A | grep u0_a", result::complete)
            val output = result.await()

            val packageInfoList = output
                .lines()
                .mapNotNull { line ->
                    val columns = line.trim().split(Regex("\\s+"))
                    val packageName = columns.getOrNull(columns.size - 1)
                    val pid = columns.getOrNull(1)?.toIntOrNull()
                    if (packageName != null && pid != null) packageName to pid else null
                }
                .distinctBy { it.first }

            println(packageInfoList.map { it.first })

            val packageManager = context.packageManager

            val bannedPatterns = listOf(
                Regex("com\\.android\\..*"),
                Regex("com\\.google\\..*")
            )
            val bannedTerms = listOf(".auto_generated_")

            for ((packageName, pid) in packageInfoList) {
                if (bannedPatterns.any { it.matches(packageName) } || bannedTerms.any { packageName.contains(it) }) continue
                try {
                    packageManager.getLaunchIntentForPackage(packageName) ?: continue
                    apps.add(
                        Process(
                            artifactPath = getAppApkLocation(packageName) ?: "",
                            icon = getAppIconBitmap(packageName),
                            container = packageName,
                            identifier = packageName,
                            pid = pid.toString(),
                            name = getAppName(packageName) ?: "Unknown"
                        )
                    )
                } catch (_: Exception) {}
            }

            return@withContext apps
        }
}
