package io.hakaisecurity.beerusframework.core.functions.memoryDump

import android.annotation.SuppressLint
import android.content.Context
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
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
            echo "==== maps ====" && cat /proc/$PID/maps && \
            echo "\n==== stack ====" && cat /proc/$PID/stack && \
            echo "\n==== .so loaded ====" && cat /proc/$PID/maps | grep -oE '/[^ ]+\.so' | sort -u && \
            echo "\n==== envs ====" && tr '\0' '\n' < /proc/$PID/environ
        """.trimIndent()) { output ->
            runSuCommand("cat /proc/$PID/cmdline") { processName ->
                val safeProcessName = processName.trim()
                    .replace(Regex("[^a-zA-Z0-9._-]+"), "_")
                    .trim('_')

                val date = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())
                val baseDir = File(context.filesDir, "dumps").apply { mkdirs() }

                val quickFile = File(baseDir, "$date-$safeProcessName-quick-dump.txt")
                quickFile.writeText(output)

                val pid = PID.trim()
                val stringDumpDir = File(baseDir, "$date-$safeProcessName-string-dump")
                val tarFile = File(baseDir, "$date-$safeProcessName.tar.gz")

                runSuCommand("""
                    PAGE=4096
                    MAX_SIZE=$((20 * 1024 * 1024))
    
                    mkdir -p "${stringDumpDir.absolutePath}"
    
                    grep -E "rw.*(heap|anon|\.dex|\.odex|\.oat)" /proc/$pid/maps | \
                    while read -r line; do
                        RANGE=$(echo "${'$'}line" | awk '{print ${'$'}1}')
    
                        START_HEX=0x${'$'}{RANGE%-*}
                        END_HEX=0x${'$'}{RANGE#*-}
    
                        START=$(printf "%u" "${'$'}START_HEX")
                        END=$(printf "%u" "${'$'}END_HEX")
                        SIZE=$((END - START))
    
                        [ "${'$'}SIZE" -le 0 ] && continue
                        [ "${'$'}SIZE" -gt "${'$'}MAX_SIZE" ] && continue
    
                        SKIP=$((START / PAGE))
                        COUNT=$((SIZE / PAGE))
    
                        [ "${'$'}COUNT" -le 0 ] && continue
    
                        OUT_FILE="${stringDumpDir.absolutePath}/${'$'}RANGE"
    
                        dd if=/proc/$pid/mem \
                           bs=${'$'}PAGE \
                           skip=${'$'}SKIP \
                           count=${'$'}COUNT \
                           status=none 2>/dev/null | strings > "${'$'}OUT_FILE"
                    done
    
                    cd "${baseDir.absolutePath}" && \
                    tar -czf "${tarFile.absolutePath}" \
                        "${quickFile.name}" \
                        "${stringDumpDir.name}" && \
                    rm -rf "${quickFile.absolutePath}" "${stringDumpDir.absolutePath}"
                """.trimIndent()) {
                    if (!isUSB) {
                        sendFile(tarFile.absolutePath, server) {
                            runSuCommand("rm -f ${tarFile.absolutePath}") {
                                onComplete("OK")
                            }
                        }
                    } else {
                        runSuCommand("cp ${tarFile.absolutePath} /data/local/tmp") {
                            runSuCommand("rm -f ${tarFile.absolutePath}") {
                                onComplete("OK")
                            }
                        }
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

    fun verify(server: String, isUSB: Boolean, onComplete: (Boolean) -> Unit) {
        if (isUSB) {
            onComplete(true)
            return
        } else {
            val request = Request.Builder().url("$server/check").get().build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onComplete(false)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val jsonBody = response.body?.string()
                        val json = JSONObject(jsonBody)
                        if (json.has("app")) {
                            if (json.getString("app") == "Beerus Server") {
                                onComplete(true)
                                return
                            }
                        }
                        onComplete(false)
                        return
                    } else {
                        onComplete(false)
                        return
                    }
                }
            })
        }
    }
}
