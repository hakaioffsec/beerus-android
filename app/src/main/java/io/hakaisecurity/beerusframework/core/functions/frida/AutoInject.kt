package io.hakaisecurity.beerusframework.core.functions.frida

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class AutoInject {
    companion object {
        private val scriptCache = ConcurrentHashMap<String, String>()
        private var lastCacheUpdate = 0L
        private const val CACHE_EXPIRY_MS = 5000L

        fun injectFridaCore(context: Context, packageName: String, script: String) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
                context.startActivity(launchIntent)

                val scriptsFullPath = File(context.filesDir, "scripts").absolutePath + "/" + script

                try {
                    runSuCommand("sleep 5 && fridaCore \$(pidof $packageName) $scriptsFullPath") {}
                } catch (e: Exception) {}
            }
        }

        fun getScriptsContent(context: Context): Map<String, String> {
            val scriptsDir = File(context.filesDir, "scripts")
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastCacheUpdate < CACHE_EXPIRY_MS && scriptCache.isNotEmpty()) {
                return scriptCache.toMap()
            }

            scriptCache.clear()

            if (!scriptsDir.exists()) {
                scriptsDir.mkdir()
                return emptyMap()
            }

            if (scriptsDir.exists() && scriptsDir.isDirectory) {
                scriptsDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        try {
                            val content = file.readText()
                            scriptCache[file.name] = content
                        } catch (e: Exception) {
                            scriptCache[file.name] = "Error reading file: ${e.message}"
                        }
                    }
                }
            }

            lastCacheUpdate = currentTime
            return scriptCache.toMap()
        }

        fun getScriptContent(context: Context, scriptName: String): String? {
            if (scriptCache.containsKey(scriptName)) {
                return scriptCache[scriptName]
            }

            val scriptsFullPath = File(context.filesDir, "scripts").absolutePath + "/" + scriptName
            val file = File(scriptsFullPath)

            return if (file.exists()) {
                try {
                    val content = file.readText()
                    scriptCache[scriptName] = content
                    content
                } catch (e: Exception) {
                    "Error reading file: ${e.message}"
                }
            } else {
                null
            }
        }

        fun saveScript(context: Context, script: String, content: String) {
            try {
                val scriptsFullPath = File(context.filesDir, "scripts").absolutePath + "/" + script
                val file = File(scriptsFullPath)

                if (!file.exists()) {
                    file.createNewFile()
                }

                file.writeText(content)

                scriptCache[script] = content

            } catch (e: Exception) {
                println("Error saving script: ${e.message}")
            }
        }

        fun deleteScript(context: Context, script: String) {
            try {
                val scriptsFullPath = File(context.filesDir, "scripts").absolutePath + "/" + script
                val file = File(scriptsFullPath)

                if (file.exists()) {
                    file.delete()
                }

                scriptCache.remove(script)

            } catch (e: Exception) {
                println("Error deleting script: ${e.message}")
            }
        }

        fun getFileNameFromUri(context: Context, uri: Uri): String {
            var name = "uploaded_script.js"
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        name = it.getString(nameIndex)
                    }
                }
            }
            return name
        }
    }
}