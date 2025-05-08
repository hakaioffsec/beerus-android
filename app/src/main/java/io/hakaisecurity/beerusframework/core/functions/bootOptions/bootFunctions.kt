package io.hakaisecurity.beerusframework.core.functions.bootOptions

import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand

class bootFunctions {
    companion object{
        fun changeProperties(propertyName: String, propertyValue: Boolean){
            runSuCommand("sed -i 's/^$propertyName=.*/$propertyName=$propertyValue/' /data/adb/modules/beerusMagiskModule/status"){}
        }

        fun getProperties(onResult: (Map<String, String>) -> Unit) {
            runSuCommand("cat /data/adb/modules/beerusMagiskModule/status") { output ->
                val statusMap = output
                    .split("\n")
                    .mapNotNull { line ->
                        val parts = line.split("=")
                        if (parts.size == 2) parts[0] to parts[1] else null
                    }
                    .toMap()

                onResult(statusMap)
            }
        }
    }
}