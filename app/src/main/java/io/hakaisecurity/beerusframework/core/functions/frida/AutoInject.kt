package io.hakaisecurity.beerusframework.core.functions.frida

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

class AutoInject {
    companion object {
        private val scriptCache = ConcurrentHashMap<String, String>()
        private var lastCacheUpdate = 0L
        private const val CACHE_EXPIRY_MS = 5000L

        val consoleLogs = mutableStateListOf<String>()
        var isInjecting = mutableStateOf(false)
        private var fridaProcess: Process? = null

        fun injectFridaCore(context: Context, packageName: String, script: String) {
            stopFridaCore()

            val scriptsFullPath = File(context.filesDir, "scripts").absolutePath + "/" + script

            isInjecting.value = true
            consoleLogs.clear()
            consoleLogs.add("[*] Starting injection: $packageName")

            Thread {
                try {
                    // kill target app so spawn starts clean
                    val prep = Runtime.getRuntime().exec("su")
                    val prepOut = DataOutputStream(prep.outputStream)
                    prepOut.writeBytes("am force-stop $packageName\n")
                    prepOut.writeBytes("exit\n")
                    prepOut.flush()
                    prep.waitFor()

                    val process = Runtime.getRuntime().exec("su")
                    fridaProcess = process
                    val outputStream = DataOutputStream(process.outputStream)
                    val inputStream = BufferedReader(InputStreamReader(process.inputStream))
                    val errorStream = BufferedReader(InputStreamReader(process.errorStream))

                    outputStream.writeBytes("fridaCore $packageName '$scriptsFullPath'\n")
                    outputStream.flush()

                    val stdoutThread = Thread {
                        try {
                            var line: String?
                            while (inputStream.readLine().also { line = it } != null) {
                                line?.let { consoleLogs.add(it) }
                            }
                        } catch (_: Exception) {}
                    }

                    val stderrThread = Thread {
                        try {
                            var line: String?
                            while (errorStream.readLine().also { line = it } != null) {
                                line?.let { consoleLogs.add("[err] $it") }
                            }
                        } catch (_: Exception) {}
                    }

                    stdoutThread.start()
                    stderrThread.start()

                    process.waitFor()
                    stdoutThread.join()
                    stderrThread.join()

                    consoleLogs.add("[*] Process exited")
                } catch (e: Exception) {
                    consoleLogs.add("[err] ${e.message}")
                } finally {
                    isInjecting.value = false
                    fridaProcess = null
                }
            }.start()
        }

        fun stopFridaCore() {
            fridaProcess?.let { proc ->
                try {
                    val kill = Runtime.getRuntime().exec("su")
                    val out = DataOutputStream(kill.outputStream)
                    // SIGINT triggers graceful shutdown (script unload + session detach)
                    out.writeBytes("pkill -2 -f fridaCore\n")
                    out.writeBytes("sleep 2\n")
                    // force kill if still alive
                    out.writeBytes("pkill -9 -f fridaCore\n")
                    out.writeBytes("exit\n")
                    out.flush()
                    kill.waitFor()
                    proc.destroy()
                } catch (_: Exception) {}
                fridaProcess = null
                isInjecting.value = false
                consoleLogs.add("[*] Stopped")
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