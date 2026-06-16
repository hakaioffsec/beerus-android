package io.hakaisecurity.beerusframework.core.functions.proxyProfiles

import android.content.Context
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

object ProxyProfiles {
    enum class ProxyMode { HTTP, IPTABLES }

    data class ProxyData(
        val name: String,
        val conString: String,
        val selected: Boolean,
        val mode: ProxyMode = ProxyMode.HTTP
    )

    fun addProfile(context: Context, profile: ProxyData) {
        val fileName = "proxyLists.json"
        val file = File(context.filesDir, fileName)

        try {
            val jsonObject = if (file.exists()) {
                val content = file.readText()
                JSONObject(content)
            } else {
                JSONObject().apply { put("connections", JSONArray()) }
            }

            val connectionsArray = jsonObject.getJSONArray("connections")

            val newProfileJson = JSONObject().apply {
                put("name", profile.name)
                put("conString", profile.conString)
                put("selected", profile.selected)
                put("mode", profile.mode.name)
            }

            connectionsArray.put(newProfileJson)
            file.writeText(jsonObject.toString(4))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getProfiles(context: Context): List<ProxyData> {
        val fileName = "proxyLists.json"
        val file = File(context.filesDir, fileName)

        return try {
            if (file.exists()) {
                val content = file.readText()
                val jsonObject = JSONObject(content)
                val connectionsArray = jsonObject.getJSONArray("connections")

                val profiles = mutableListOf<ProxyData>()
                for (i in 0 until connectionsArray.length()) {
                    val obj = connectionsArray.getJSONObject(i)
                    val name = obj.getString("name")
                    val conString = obj.getString("conString")
                    val selected = obj.getBoolean("selected")
                    val mode = try {
                        ProxyMode.valueOf(obj.getString("mode"))
                    } catch (_: Exception) {
                        ProxyMode.HTTP
                    }
                    profiles.add(ProxyData(name, conString, selected, mode))
                }
                profiles
            } else {
                emptyList()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun deleteProxy(context: Context, profile: ProxyData) {
        val fileName = "proxyLists.json"
        val file = File(context.filesDir, fileName)

        try {
            if (file.exists()) {
                val content = file.readText()
                val jsonObject = JSONObject(content)

                val connectionsArray = jsonObject.getJSONArray("connections")

                for (i in 0 until connectionsArray.length()) {
                    val obj = connectionsArray.getJSONObject(i)
                    if (obj.getString("name") == profile.name &&
                        obj.getString("conString") == profile.conString) {
                        connectionsArray.remove(i)
                        break
                    }
                }

                file.writeText(jsonObject.toString(4))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun selectProfile(context: Context, conString: String, mode: ProxyMode = ProxyMode.HTTP) {
        val fileName = "proxyLists.json"
        val file = File(context.filesDir, fileName)

        try {
            if (file.exists()) {
                val jsonObject = JSONObject(file.readText())
                val connectionsArray = jsonObject.getJSONArray("connections")

                for (i in 0 until connectionsArray.length()) {
                    val obj = connectionsArray.getJSONObject(i)
                    val selected = obj.getString("conString") == conString
                    obj.put("selected", selected)
                }

                file.writeText(jsonObject.toString(4))

                when (mode) {
                    ProxyMode.HTTP -> {
                        runSuCommand("iptables -t nat -F") { }
                        runSuCommand("runcon u:r:shell:s0 sh -c 'settings put global http_proxy $conString'") { }
                    }
                    ProxyMode.IPTABLES -> {
                        runSuCommand("runcon u:r:shell:s0 sh -c 'settings put global http_proxy :0'") { }
                        runSuCommand(
                            "iptables -t nat -F" +
                            " && iptables -t nat -A OUTPUT -p tcp --dport 80 -j DNAT --to-destination $conString" +
                            " && iptables -t nat -A OUTPUT -p tcp --dport 443 -j DNAT --to-destination $conString" +
                            " && iptables -t nat -A POSTROUTING -p tcp --dport 443 -j MASQUERADE" +
                            " && iptables -t nat -A POSTROUTING -p tcp --dport 80 -j MASQUERADE"
                        ) { }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun deselectProfile(context: Context) {
        val fileName = "proxyLists.json"
        val file = File(context.filesDir, fileName)

        try {
            if (file.exists()) {
                val jsonObject = JSONObject(file.readText())
                val connectionsArray = jsonObject.getJSONArray("connections")

                for (i in 0 until connectionsArray.length()) {
                    connectionsArray.getJSONObject(i).put("selected", false)
                }

                file.writeText(jsonObject.toString(4))
            }
        } catch (_: IOException) { }

        runSuCommand("runcon u:r:shell:s0 sh -c 'settings put global http_proxy :0'") { }
        runSuCommand("iptables -t nat -F") { }
    }

    fun updateProfileMode(context: Context, profileName: String, newMode: ProxyMode) {
        val fileName = "proxyLists.json"
        val file = File(context.filesDir, fileName)

        try {
            if (file.exists()) {
                val jsonObject = JSONObject(file.readText())
                val connectionsArray = jsonObject.getJSONArray("connections")

                for (i in 0 until connectionsArray.length()) {
                    val obj = connectionsArray.getJSONObject(i)
                    if (obj.getString("name") == profileName) {
                        obj.put("mode", newMode.name)
                        break
                    }
                }

                file.writeText(jsonObject.toString(4))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun editProfile(context: Context, oldProfile: ProxyData, newProfile: ProxyData) {
        val fileName = "proxyLists.json"
        val file = File(context.filesDir, fileName)

        try {
            if (file.exists()) {
                val jsonObject = JSONObject(file.readText())
                val connectionsArray = jsonObject.getJSONArray("connections")

                for (i in 0 until connectionsArray.length()) {
                    val obj = connectionsArray.getJSONObject(i)
                    if (obj.getString("name") == oldProfile.name &&
                        obj.getString("conString") == oldProfile.conString) {

                        obj.put("name", newProfile.name)
                        obj.put("conString", newProfile.conString)
                        obj.put("selected", newProfile.selected)
                        obj.put("mode", newProfile.mode.name)
                        break
                    }
                }

                file.writeText(jsonObject.toString(4))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}