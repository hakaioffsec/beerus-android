package io.hakaisecurity.beerusframework.core.functions.Properties

import android.util.Log
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import java.io.IOException

object Properties {
    var systemPropPath = "/data/adb/modules/beerusMagiskModule/system.prop"

    data class PropertyData(
        val name: String,
        val value: String
    )

    fun listProperties(): List<PropertyData> {
        val latch = java.util.concurrent.CountDownLatch(1)

        return try {
            val properties = mutableListOf<PropertyData>()
            runSuCommand("cat $systemPropPath") { content ->
                val contents = content.split("\n")
                for (c in contents) {
                    if (c != "") {
                        val d = c.split("=", limit=2)
                        val name = d[0]
                        val value = d[1]
                        properties.add(PropertyData(name, value))
                    }
                }
                latch.countDown()
            }
            latch.await()
            properties
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun addProperty(name: String, value: String) {
        val latch = java.util.concurrent.CountDownLatch(1)
        val name = name.replace("\"", "\\\"").replace("'", "\\'")
        val value = value.replace("\"", "\\\"").replace("'", "\\'")

        runSuCommand("echo \"$name=$value\" >> $systemPropPath") { result ->
            latch.countDown()
        }
        latch.await()
    }

    fun removeProperty(name: String) {
        val latch = java.util.concurrent.CountDownLatch(1)
        val name = name.replace("\"", "\\\"").replace("'", "\\'")

        runSuCommand("prop=\"$name\"; sed -i \"/^\${prop}=/d\" $systemPropPath") { result ->
            latch.countDown()
        }
        latch.await()
    }
}