package io.hakaisecurity.beerusframework.core.functions.frida

import android.app.Activity
import android.content.Context
import android.os.Environment
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.updateFridaDownloadedVersion
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.updateFridaState
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.tukaani.xz.XZInputStream
import java.io.File
import java.net.URL

class FridaSetup {
    companion object {
        private var pidOfFrida = ""

        fun getFridaVersions(onNewVersion: (String) -> Unit, onLoadingComplete: () -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val fridaPages = scrappHttpContent("https://github.com/frida/frida/releases?page=1")

                if (fridaPages != null) {
                    val fridaVersions =
                        fridaPages.lines().filter { it.contains("/frida/frida/tree/") }

                    for (eachVersion in fridaVersions) {
                        onNewVersion(eachVersion.split("/frida/frida/tree/")[1].split("\"")[0])
                    }
                }

                withContext(Dispatchers.Main) {
                    onLoadingComplete()
                }
            }
        }

        fun startFridaModule(activity: Activity, fridaVersion: String, active: String) {
            val fridaCurrent = readFridaCurrentVersion(activity)

            val arch = when (System.getProperty("os.arch")) {
                "aarch64" -> "arm64"
                "armv7l" -> "arm"
                "x86_64" -> "x86_64"
                "x86" -> "x86"
                else -> "Unknow"
            }

            runSuCommand("ls /data/local/tmp/hiddenBin") { result ->
                if (result.contains("No such file or directory")) {
                    downloadFromHttp(
                        activity,
                        "https://github.com/frida/frida/releases/download/$fridaVersion/frida-server-$fridaVersion-android-$arch.xz",
                        active
                    )
                } else if (fridaVersion != fridaCurrent) {
                    deleteFridaBinary(activity)
                    downloadFromHttp(
                        activity,
                        "https://github.com/frida/frida/releases/download/$fridaVersion/frida-server-$fridaVersion-android-$arch.xz",
                        active
                    )
                } else {
                    runKillFrida(active)
                }
            }
        }

        private fun runKillFrida(active: String) {
            if (active == "start") {
                runSuCommand("chmod +x /data/local/tmp/hiddenBin && /data/local/tmp/hiddenBin &") { result ->
                    println(result)
                }

                updateFridaState("stop")
            } else if (active == "stop") {
                runSuCommand("ps -A | grep hidden | awk '{print \$2}'") { result ->
                    pidOfFrida = result

                    runSuCommand("kill -9 $pidOfFrida") { result2 ->
                        println("Command Output: $result2")
                    }

                    updateFridaState("start")
                }
            }
        }

        private fun deleteFridaBinary(context: Context) {
            runSuCommand("rm -rf /data/local/tmp/hiddenBin") { result ->
                println("Command Output: $result")
                deleteFridaCurrentVersion(context)
            }
        }

        fun checkFridaProcessState(callback: (String) -> Unit) {
            runSuCommand("ps -A | grep hidden") { result ->
                val state = if (result.contains("hidden")) "stop" else "start"
                callback(state)
            }
        }

        private fun saveFridaCurrentVersion(context: Context, data: String) {
            val file = File(context.filesDir, "@FridaVersion")
            file.writeText(data)

            updateFridaDownloadedVersion(data)
        }

        private fun deleteFridaCurrentVersion(context: Context) {
            val file = File(context.filesDir, "@FridaVersion")
            file.delete()
        }

        fun readFridaCurrentVersion(context: Context): String {
            val file = File(context.filesDir, "@FridaVersion")
            return if (file.exists()) file.readText() else "None"
        }

        private fun scrappHttpContent(url: String): String? = runBlocking {
            try {
                withContext(Dispatchers.IO) {
                    URL(url)
                        .openStream()
                        .bufferedReader()
                        .use { it.readText() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        private fun downloadFromHttp(context: Context, url: String, active: String) = runBlocking {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    updateFridaState("processing")

                    withContext(Dispatchers.IO) {
                        URL(url).openStream().use { input ->
                            File(
                                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                                url.split("/").last()
                            ).outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        println("file Downloaded $url")

                        return@withContext
                    }

                    withContext(Dispatchers.IO) {
                        unzipXZ(context, url.split("/").last(), active)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun unzipXZ(context: Context, file: String, active: String) {
            val buffer = ByteArray(1024)
            val inputFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), file)
            val outputFilename = file.dropLast(3)

            inputFile.inputStream().use { fis ->
                XZInputStream(fis).use { xzis ->
                    File(
                        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        file.dropLast(3)
                    ).outputStream().use { fos ->
                        var bytesRead: Int
                        while (xzis.read(buffer).also { bytesRead = it } != -1) {
                            fos.write(buffer, 0, bytesRead)
                        }
                    }
                }
            }

            val downloadFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            runSuCommand("cd $downloadFolder && mv $outputFilename /data/local/tmp/hiddenBin") { result2 ->
                println(result2)
                saveFridaCurrentVersion(
                    context,
                    outputFilename.split("frida-server-")[1].split("-android")[0]
                )
                runKillFrida(active)
            }

            inputFile.delete()
        }
    }
}