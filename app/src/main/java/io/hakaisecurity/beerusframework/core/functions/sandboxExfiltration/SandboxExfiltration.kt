package io.hakaisecurity.beerusframework.core.functions.sandboxExfiltration

import java.io.File
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import android.content.Context

class SandboxExfiltration {

    fun exfiltrateFile(context: Context, app: Application): String {
        val sourceFile = File(app.artifactPath)
        if (!sourceFile.exists()) {
            return "Arquivo de origem não encontrado: ${app.artifactPath}"
        }

        val destinationDir = "/data/local/tmp/${app.identifier}"
        val destinationPath = "$destinationDir/${sourceFile.name}"

        var resultMessage = ""

        runSuCommand("mkdir -p $destinationDir") { mkdirResult ->
            if (mkdirResult.isBlank()) {
                resultMessage = "Erro ao criar diretório de destino: $mkdirResult"
                return@runSuCommand
            }

            runSuCommand("cp -r ${sourceFile.absolutePath} $destinationPath") { copyResult ->
                if (copyResult.isBlank()) {
                    runSuCommand("ls $destinationPath") { check ->
                        resultMessage = if (check.isNotBlank()) {
                            "Arquivos exfiltrados com sucesso para: $destinationPath"
                        } else {
                            "Falha ao validar os arquivos copiados."
                        }
                    }
                } else {
                    resultMessage = "Erro ao copiar: $copyResult"
                }
            }
        }

        return resultMessage
    }
}