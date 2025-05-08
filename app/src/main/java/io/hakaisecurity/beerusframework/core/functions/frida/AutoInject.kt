package io.hakaisecurity.beerusframework.core.functions.frida

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import java.io.File

class AutoInject {
    companion object {
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
            val result = mutableMapOf<String, String>()
            val scriptsDir = File(context.filesDir, "scripts")

            if (!scriptsDir.exists()) {
                scriptsDir.mkdir()
            }

            if (scriptsDir.exists() && scriptsDir.isDirectory) {
                scriptsDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        try {
                            result[file.name] = file.readText()
                        } catch (e: Exception) {
                            result[file.name] = "Error reading file: ${e.message}"
                        }
                    }
                }
            }

            return result
        }

        fun saveScript(context: Context, script: String, content: String) {
            try {
                val scriptsFullPath = File(context.filesDir, "scripts").absolutePath + "/" + script
                val file = File(scriptsFullPath)

                if (!file.exists()) {
                    file.createNewFile()
                }

                file.writeText(content)
            } catch (e: Exception) {
                println("Error saving script")
            }
        }

        fun deleteScript(context: Context, script: String){
            try {
                val scriptsFullPath = File(context.filesDir, "scripts").absolutePath + "/" + script
                val file = File(scriptsFullPath)

                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                println("Error deleting script")
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