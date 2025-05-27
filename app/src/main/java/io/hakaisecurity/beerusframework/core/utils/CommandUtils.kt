package io.hakaisecurity.beerusframework.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.LocalSocket
import android.net.LocalSocketAddress
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter

class CommandUtils {
    companion object {
        private var hasDeamon: Boolean? = null

        fun runSuCommand(command: String, callback: (String) -> Unit) {
            Thread {
                when (hasDeamon) {
                    true -> runViaDaemon(command, callback)
                    false -> runViaShell(command, callback)
                    null -> {
                        hasDeamon = isDaemonAvailable()
                        runSuCommand(command, callback)
                    }
                }
            }.start()
        }

        private fun isDaemonAvailable(): Boolean {
            return try {
                val socket = LocalSocket()
                val address = LocalSocketAddress(
                    File(daemonSocketFile.parentFile, "beerusd").absolutePath,
                    LocalSocketAddress.Namespace.FILESYSTEM
                )
                socket.connect(address)
                socket.close()
                true
            } catch (e: IOException) {
                false
            }
        }

        private fun runViaDaemon(command: String, callback: (String) -> Unit) {
            val result = StringBuilder()
            try {
                val socket = LocalSocket()
                val address = LocalSocketAddress(
                    File(daemonSocketFile.parentFile, "beerusd").absolutePath,
                    LocalSocketAddress.Namespace.FILESYSTEM
                )
                socket.connect(address)

                val out = PrintWriter(socket.outputStream, true)
                out.println(command)

                val reader = BufferedReader(InputStreamReader(socket.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    result.append(line).append("\n")
                }

                socket.close()
            } catch (e: IOException) {
                result.append("Daemon error: ${e.message}")
            }

            callback(result.toString())
        }

        private fun runViaShell(command: String, callback: (String) -> Unit) {
            val result = StringBuilder()

            try {
                val process = Runtime.getRuntime().exec("su")
                val outputStream = DataOutputStream(process.outputStream)
                val inputStream = BufferedReader(InputStreamReader(process.inputStream))
                val errorStream = BufferedReader(InputStreamReader(process.errorStream))

                outputStream.writeBytes("$command\n")
                outputStream.writeBytes("exit\n")
                outputStream.flush()

                var line: String?
                while (inputStream.readLine().also { line = it } != null) {
                    result.append(line).append("\n")
                }

                while (errorStream.readLine().also { line = it } != null) {
                    result.append("Error: ").append(line).append("\n")
                }

                process.waitFor()
                outputStream.close()
                inputStream.close()
                errorStream.close()

            } catch (e: Exception) {
                result.append("Exception: ${e.message}")
            }

            callback(result.toString())
        }

        private val daemonSocketFile: File by lazy {
            File(getApplicationContext().filesDir.parentFile, "beerusd")
        }

        @SuppressLint("PrivateApi")
        private fun getApplicationContext(): Context {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentApplicationMethod = activityThreadClass.getMethod("currentApplication")
            return currentApplicationMethod.invoke(null) as Context
        }
    }
}