package io.hakaisecurity.beerusframework.core.functions

import android.content.Context
import android.os.Build
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import java.io.File

class Start {
    companion object{
        fun detectMagiskModuleInstalled(callback: (Boolean) -> Unit) {
            val cmd = """
                if ( [ -f /system/lib/libzygisk.so ] || [ -f /system/lib64/libzygisk.so ] || \
                     [ -f /system/bin/magisk ] || [ -f /sbin/magisk ] ) && \
                   [ -d /data/adb/modules/beerusMagiskModule ]; then
                    echo true
                else
                    echo false
                fi
            """.trimIndent()

            runSuCommand(cmd) {
                callback(it.trim() == "true")
            }
        }

        fun detectMagisk(callback: (Boolean) -> Unit) {
            val cmd = """
                if [ -f /system/lib/libzygisk.so ] || [ -f /system/lib64/libzygisk.so ] || \
                   [ -f /system/bin/magisk ] || [ -f /sbin/magisk ]; then
                    echo true
                else
                    echo false
                fi
            """.trimIndent()

            runSuCommand(cmd) {
                callback(it.trim() == "true")
            }
        }

        fun installBeerusModule(context: Context){
            val assetZipNameMagisk = "beerusMagiskModule.zip"
            val modulePathMagisk = "/data/adb/modules/beerusMagiskModule"
            val zipDestPathMagisk = "$modulePathMagisk/beerusMagiskModule.zip"

            val binPath = "/data/adb/modules/beerusMagiskModule/system/bin"

            val assetZipNameFrida = "fridaCore.zip"
            val zipDestPathFrida = "$binPath/fridaCore.zip"

            val assetZipNameDB = "dbAgent.zip"
            val zipDestPathDB = "$binPath/dbAgent.zip"

            val tempZipMagisk = File(context.cacheDir, assetZipNameMagisk)
            val tempZipFrida = File(context.cacheDir, assetZipNameFrida)
            val tempZipDB = File(context.cacheDir, assetZipNameDB)

            context.assets.open(assetZipNameMagisk).use { input ->
                tempZipMagisk.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            context.assets.open(assetZipNameFrida).use { input ->
                tempZipFrida.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            context.assets.open(assetZipNameDB).use { input ->
                tempZipDB.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            runSuCommand("""
                mkdir -p $modulePathMagisk &&
                cp ${tempZipMagisk.absolutePath} $zipDestPathMagisk &&
                cd $modulePathMagisk &&
                unzip $assetZipNameMagisk &&
                rm -rf $assetZipNameMagisk &&
                rm -rf $binPath/dummy &&
                cp ${tempZipFrida.absolutePath} $zipDestPathFrida &&
                cd $binPath &&
                unzip $assetZipNameFrida
            """.trimIndent()) {
                val arch = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
                installBinsForBeerusModule(arch, binPath, zipDestPathFrida, tempZipDB, assetZipNameDB, zipDestPathDB)
            }
        }

        fun installBinsForBeerusModule(arch: String, binPath: String, zipDestPathFrida: String, tempZipDB: File, assetZipNameDB: String, zipDestPathDB: String){
            runSuCommand("""
                mv $binPath/libs/$arch/fridaCore $binPath &&
                rm -rf $binPath/libs &&
                rm -rf $zipDestPathFrida &&
                cp ${tempZipDB.absolutePath} $zipDestPathDB &&
                cd $binPath &&
                unzip $assetZipNameDB
                mv $binPath/libs/$arch/dbAgent $binPath &&
                rm -rf $binPath/libs &&
                rm -rf $zipDestPathDB &&
                reboot
            """.trimIndent()) {}
        }
    }
}