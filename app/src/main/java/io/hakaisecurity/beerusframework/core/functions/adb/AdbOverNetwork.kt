package io.hakaisecurity.beerusframework.core.functions.adb

import io.hakaisecurity.beerusframework.core.models.AdbState.Companion.updateAdbState
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import java.net.Inet4Address
import java.net.NetworkInterface

class AdbOverNetwork {
    companion object{
        fun adbStart(){
            runSuCommand("setprop service.adb.tcp.port 5555 | stop adbd"){}
            updateAdbState(true)

            runSuCommand("getprop init.svc.adbd") { output ->
                if(output != "running"){
                    runSuCommand("start adbd"){}
                }
            }
        }

        fun adbStop(){
            runSuCommand("setprop service.adb.tcp.port \"\" | stop adbd") { }
            updateAdbState(false)

            runSuCommand("getprop init.svc.adbd") { output ->
                if(output != "running"){
                    runSuCommand("start adbd"){}
                }
            }
        }

        fun adbStatus(callback: (String) -> Unit) {
            runSuCommand("getprop service.adb.tcp.port") { result ->
                callback(result.replace("\n", ""))
            }
        }

        fun getIpAddr(): String? {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                for (intf in interfaces) {
                    val addrs = intf.inetAddresses
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress && addr is Inet4Address) {
                            return addr.hostAddress
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return null
        }

    }
}