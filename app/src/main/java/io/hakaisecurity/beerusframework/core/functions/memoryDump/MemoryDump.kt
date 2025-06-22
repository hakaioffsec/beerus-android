package io.hakaisecurity.beerusframework.core.functions.memoryDump

import android.annotation.SuppressLint
import android.content.Context
import io.hakaisecurity.beerusframework.core.models.Process
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

object MemoryDump {
    fun collectionTriagge(context: Context, server:String, isUSB: Boolean, selectionData: String, onComplete: (String) -> Unit) {
        val dir = File(context.filesDir, "dumps")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        quickDump(context, server, isUSB, selectionData, onComplete)
    }

    @SuppressLint("SimpleDateFormat")
    private fun quickDump(context: Context, server: String, isUSB: Boolean, PID: String, onComplete: (String) -> Unit) {
        runSuCommand("""
            echo -e "==== maps ====" && cat /proc/$PID/maps && \
            echo -e "\n==== stack ====" && cat /proc/$PID/stack && \
            echo -e "\n==== .so loaded ====" && cat /proc/$PID/maps | grep -oE '/[^ ]+\.so' | sort -u && \
            echo -e "\n==== envs ====" && cat /proc/$PID/environ
        """.trimIndent()) { output ->
            runSuCommand("cat /proc/$PID/cmdline") { processName ->
                val safeProcessName = processName.trim()
                    .replace(Regex("[^a-zA-Z0-9._-]+"), "_")
                    .removePrefix("_")
                    .removeSuffix("_")
                val date = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())
                val dir = File(context.filesDir, "dumps").apply { mkdirs() }
                val file = File(dir, "$date-$safeProcessName-quick-dump.txt")
                file.writeText(output)

                val pid = PID.trim()
                val stringDumpDirName = File(dir, "$date-$safeProcessName-string-dump")
                val tarFile = File(dir, "$date-$safeProcessName.tar.gz")
                runSuCommand("""
                    mkdir -p ${stringDumpDirName.absolutePath} && \
                    while IFS= read -r line; do \
                        RANGE=$(echo "${'$'}line" | awk '{print ${'$'}1}'); \
                        PERMS=$(echo "${'$'}line" | awk '{print ${'$'}2}'); \
                        if [[ "${'$'}PERMS" == *"r"* ]]; then \
                            START_HEX=0x${'$'}{RANGE%-*}; \
                            END_HEX=0x${'$'}{RANGE#*-}; \
                            START=$(printf "%u" ${'$'}START_HEX); \
                            END=$(printf "%u" ${'$'}END_HEX); \
                            SIZE=${'$'}((END - START)); \
                            FILE="${stringDumpDirName.absolutePath}/${'$'}RANGE"; \
                            dd if=/proc/$pid/mem bs=1 skip=${'$'}START count=${'$'}SIZE status=none 2>/dev/null | strings > "${'$'}FILE"; \
                        fi; \
                    done < /proc/$pid/maps && \
                    cd ${dir.absolutePath} && \
                    tar -czf ${tarFile.absolutePath} $date-$safeProcessName-quick-dump.txt $date-$safeProcessName-string-dump && \
                    rm -rf ${file.absolutePath} ${stringDumpDirName.absolutePath}
                """.trimIndent()) {
                    if (!isUSB) {
                        sendFile(tarFile.absolutePath, server) { R ->
                            runSuCommand("rm -rf ${tarFile.absolutePath}") {
                                onComplete("OK")
                            }
                        }
                    } else {
                        runSuCommand("mv ${tarFile.absolutePath} /data/local/tmp") { onComplete("OK") }
                    }
                }
            }
        }
    }

    private val client = OkHttpClient()

    private fun sendFile(fileName: String, server:String, onComplete: (String) -> Unit) {
        val sourceFile = File(fileName)
        if (!sourceFile.exists()) {
            onComplete("Compressed file not found: $fileName")
        }

        val fileBody = sourceFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
        var body = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("file", sourceFile.name, fileBody).build()
        val request = Request.Builder().url(server).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onComplete("ERROR: Failed to send the file")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    onComplete("SUCCESS: File sent successfully")
                } else {
                    onComplete("ERROR: Failed to send the file")
                }
            }
        })
    }
}