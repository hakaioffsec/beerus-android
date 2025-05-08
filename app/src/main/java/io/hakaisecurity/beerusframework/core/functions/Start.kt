package io.hakaisecurity.beerusframework.core.functions

import android.content.Context
import android.os.Build
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import java.io.File

class Start {
    companion object{
        fun detectMagiskModuleInstalled(callback: (Boolean) -> Unit) {
            val cmd = """
                if { [ -f /system/lib/libzygisk.so ] || [ -f /system/lib64/libzygisk.so ] || \
                      grep -q "magisk" /sbin/* 2>/dev/null; } && \
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
                   grep -q "magisk" /sbin/* 2>/dev/null; then
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

            val assetZipNameDeamon = "beerusd.zip"
            val zipDestPathDeamon = "$binPath/beerusd.zip"

            val tempZipMagisk = File(context.cacheDir, assetZipNameMagisk)
            val tempZipFrida = File(context.cacheDir, assetZipNameFrida)
            val tempZipDeamon = File(context.cacheDir, assetZipNameDeamon)

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

            context.assets.open(assetZipNameDeamon).use { input ->
                tempZipDeamon.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            runSuCommand("mkdir -p $modulePathMagisk && cp ${tempZipMagisk.absolutePath} $zipDestPathMagisk" +
                    "&& cd $modulePathMagisk && unzip $assetZipNameMagisk && rm -rf $assetZipNameMagisk && rm -rf $binPath/dummy" +
                    "&& cd $binPath && cp ${tempZipFrida.absolutePath} $zipDestPathFrida && unzip $assetZipNameFrida"){
                val arch = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
                installBinsForBeerusModule(arch, binPath, zipDestPathFrida,tempZipDeamon, zipDestPathDeamon, assetZipNameDeamon)
            }
        }

        fun installBinsForBeerusModule(arch: String, binPath: String, zipDestPathFrida: String, tempZipDeamon: File, zipDestPathDeamon: String, assetZipNameDeamon: String){
            runSuCommand("mv $binPath/libs/$arch/fridaCore $binPath && rm -rf $binPath/libs && rm -rf $zipDestPathFrida" +
                    "&& cd $binPath && cp ${tempZipDeamon.absolutePath} $zipDestPathDeamon && unzip $assetZipNameDeamon" +
                    "&& mv $binPath/libs/$arch/beerusd $binPath && rm -rf $binPath/libs && rm -rf $zipDestPathDeamon" +
                    "&& reboot"){}
        }
    }
}