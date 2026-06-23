package io.hakaisecurity.beerusframework.core.functions.sandboxExfiltration

import io.hakaisecurity.beerusframework.core.models.Application
import io.hakaisecurity.beerusframework.core.network.grpc.BeerusGrpcUploader
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import io.hakaisecurity.beerusframework.grpc.ArtifactKind
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

    fun prepareFileToSend(destinationPath: String, server: String, isUSB: Boolean, packageName: String?, onComplete: (String) -> Unit) {
        runSuCommand("tar -czf $destinationPath.tar.gz $destinationPath/*") { tarResult ->
            if (tarResult.isBlank()) {
                onComplete("fail")
            } else {
                runSuCommand("chmod 655 $destinationPath.tar.gz && rm -rf $destinationPath") {
                    if (!isUSB) {
                        val tarPath = "$destinationPath.tar.gz"
                        if (server.trim().startsWith("grpc://")) {
                            BeerusGrpcUploader.uploadTarGz(
                                server = server,
                                filePath = tarPath,
                                kind = ArtifactKind.SANDBOX_EXFILTRATION_TAR_GZ,
                                packageName = packageName,
                                deviceId = null,
                                extractTarGz = true
                            ) { result ->
                                runSuCommand("rm -rf $destinationPath") {}
                                runSuCommand("rm -rf $tarPath") {}
                                onComplete(if (result != null && result.success) "success" else "fail")
                            }
                        } else {
                            sendFile(fileName = tarPath, server = server) { _ ->
                                runSuCommand("rm -rf $destinationPath") {}
                                runSuCommand("rm -rf $tarPath") {}
                                onComplete("success")
                            }
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
                        prepareFileToSend(destinationPath, server, isUSB, app.identifier) { status ->
                            onComplete(status)
                        }
                    }
                } else {
                    prepareFileToSend(destinationPath, server, isUSB, app.identifier) { status ->
                        onComplete(status)
                    }
                }
            }
        }
    }

    fun verify(server: String, isUSB: Boolean, onComplete: (Boolean) -> Unit) {
        if (isUSB) {
            onComplete(true)
            return
        } else {
            if (server.trim().startsWith("grpc://")) {
                BeerusGrpcUploader.check(server) { ok -> onComplete(ok) }
                return
            }
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