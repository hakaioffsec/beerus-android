package io.hakaisecurity.beerusframework.core.network.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import io.hakaisecurity.beerusframework.grpc.ArtifactKind
import io.hakaisecurity.beerusframework.grpc.BeerusTransferGrpc
import io.hakaisecurity.beerusframework.grpc.CheckRequest
import io.hakaisecurity.beerusframework.grpc.UploadChunk
import io.hakaisecurity.beerusframework.grpc.UploadMetadata
import io.hakaisecurity.beerusframework.grpc.UploadResult
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit

object BeerusGrpcUploader {
    data class Endpoint(val host: String, val port: Int) {
        companion object {
            fun parse(server: String, defaultPort: Int = 6525): Endpoint? {
                val s = server.trim()
                if (!s.startsWith("grpc://")) return null
                val hostPort = s.removePrefix("grpc://").trim().trimEnd('/')
                if (hostPort.isBlank()) return null
                val parts = hostPort.split(":", limit = 2)
                val host = parts[0].trim()
                val port = parts.getOrNull(1)?.toIntOrNull() ?: defaultPort
                if (host.isBlank()) return null
                return Endpoint(host = host, port = port)
            }
        }
    }

    private fun channel(endpoint: Endpoint): ManagedChannel =
        ManagedChannelBuilder.forAddress(endpoint.host, endpoint.port)
            .usePlaintext()
            .build()

    fun check(server: String, onComplete: (Boolean) -> Unit) {
        val endpoint = Endpoint.parse(server) ?: run {
            onComplete(false)
            return
        }

        val ch = channel(endpoint)
        try {
            val stub = BeerusTransferGrpc.newBlockingStub(ch).withDeadlineAfter(3, TimeUnit.SECONDS)
            val resp = stub.check(CheckRequest.newBuilder().build())
            onComplete(resp.app == "Beerus gRPC Server")
        } catch (_: Exception) {
            onComplete(false)
        } finally {
            ch.shutdownNow()
        }
    }

    fun uploadTarGz(
        server: String,
        filePath: String,
        kind: ArtifactKind,
        packageName: String?,
        deviceId: String?,
        extractTarGz: Boolean = true,
        onComplete: (UploadResult?) -> Unit
    ) {
        val endpoint = Endpoint.parse(server) ?: run {
            onComplete(null)
            return
        }

        val file = File(filePath)
        if (!file.exists()) {
            onComplete(null)
            return
        }

        val ch = channel(endpoint)
        val stub = BeerusTransferGrpc.newStub(ch)

        val responseObserver = object : StreamObserver<UploadResult> {
            override fun onNext(value: UploadResult) = onComplete(value)
            override fun onError(t: Throwable) {
                onComplete(null)
                ch.shutdownNow()
            }

            override fun onCompleted() {
                ch.shutdown()
            }
        }

        val requestObserver = stub.upload(responseObserver)

        try {
            var seq = 0L
            val meta = UploadMetadata.newBuilder()
                .setKind(kind)
                .setFilename(file.name)
                .setExtractTarGz(extractTarGz)
                .apply {
                    if (!packageName.isNullOrBlank()) setPackageName(packageName)
                    if (!deviceId.isNullOrBlank()) setDeviceId(deviceId)
                }
                .build()

            requestObserver.onNext(
                UploadChunk.newBuilder()
                    .setSeq(seq++)
                    .setMeta(meta)
                    .build()
            )

            FileInputStream(file).use { fis ->
                val buf = ByteArray(64 * 1024)
                while (true) {
                    val n = fis.read(buf)
                    if (n <= 0) break
                    requestObserver.onNext(
                        UploadChunk.newBuilder()
                            .setSeq(seq++)
                            .setData(com.google.protobuf.ByteString.copyFrom(buf, 0, n))
                            .build()
                    )
                }
            }

            requestObserver.onCompleted()
        } catch (_: Exception) {
            try {
                requestObserver.onError(RuntimeException("upload failed"))
            } catch (_: Exception) {}
            ch.shutdownNow()
        }
    }
}

