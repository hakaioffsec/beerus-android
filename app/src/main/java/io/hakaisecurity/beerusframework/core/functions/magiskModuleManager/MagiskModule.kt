package io.hakaisecurity.beerusframework.core.functions.magiskModuleManager

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import io.hakaisecurity.beerusframework.core.models.MagiskManager.Companion.showSDialog
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class MagiskModule {
    companion object{
        fun startModuleManager(context: Context, zipUri: Uri) {
            val zipFile = getFileNameFromUri(context, zipUri)
            val dirDestination = zipFile?.split(".")?.get(0)

            val cacheDir: File = context.cacheDir
            val cacheFile = zipFile?.let { File(cacheDir, it).absoluteFile }

            if (zipFile != null) {
                copyFileToCache(context, zipUri, zipFile)
            }

            println(dirDestination)

            runSuCommand("ls /data/adb/modules") { result ->
                if (!result.contains("No such file or directory")) {
                    runSuCommand("mkdir /data/adb/modules/${dirDestination}"){
                        runSuCommand("unzip -o $cacheFile -d /data/adb/modules/${dirDestination}") {
                            showSDialog()
                        }
                    }
                }
            }
        }

        fun getAllModules(modulePropsList: MutableList<String>){
            runSuCommand("find /data/adb/modules/ -type f -name \"module.prop\" -exec ls -l {} \\; | cut -d \" \" -f 8") { result ->
                result.split("\n").filter { it.isNotBlank() }.let { paths ->
                    modulePropsList.addAll(paths)
                }
            }
        }

        fun deleteModule(modulePath: String){
            val path = modulePath.replace("module.prop", "", ignoreCase = true)

            runSuCommand("rm -rf $path"){}
        }

        private fun getFileNameFromUri(context: Context, uri: Uri): String? {
            var result: String? = null
            if (uri.scheme == "content") {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            result = cursor.getString(nameIndex)
                        }
                    }
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result?.lastIndexOf('/')
                if (cut != null && cut != -1) {
                    result = result?.substring(cut + 1)
                }
            }
            return result
        }

        private fun copyFileToCache(context: Context, uri: Uri, filename: String) {
            try {
                val contentResolver: ContentResolver = context.contentResolver
                val inputStream: InputStream? = contentResolver.openInputStream(uri)

                inputStream?.let {
                    val cacheDir: File = context.cacheDir
                    val cacheFile = File(cacheDir, filename)

                    val outputStream: OutputStream = FileOutputStream(cacheFile)
                    val buffer = ByteArray(1024)
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }

                    outputStream.flush()
                    inputStream.close()
                    outputStream.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}