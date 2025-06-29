package io.hakaisecurity.beerusframework.core.utils

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

class CommandUtils {
    companion object {
        fun runSuCommand(command: String, callback: (String) -> Unit) {
            Thread {
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
            }.start()
        }
    }
}