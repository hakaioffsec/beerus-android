package io.hakaisecurity.beerusframework.core.functions.Manifest

import android.app.Activity
import android.util.Log
import io.hakaisecurity.beerusframework.core.functions.Properties.Properties.PropertyData
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.util.Dictionary

class Manifest {


    data class ComponentInfo(
        val name: String,
        val exported: Boolean
    )

    data class Manifest(
        val userPermissions: List<String>,
        val components: Map<String, List<ComponentInfo>>,
        val General: Map<String, String>
    )

    fun parseManifest(androidManifest: String, callback: (Manifest) -> Unit) {
        val general = mutableMapOf<String, String>()
        val permissions = mutableListOf<String>()
        val components = mutableMapOf(
            "activities" to mutableListOf<ComponentInfo>(),
            "providers" to mutableListOf<ComponentInfo>(),
            "services" to mutableListOf<ComponentInfo>(),
            "receivers" to mutableListOf<ComponentInfo>()
        )

        val tagToKey = mapOf(
            "activity" to "activities",
            "provider" to "providers",
            "service" to "services",
            "receiver" to "receivers"
        )
        Regex(
            """<activity\b[^>]*android:name="([^"]+)"[^>]*>(?:(?!</activity>).)*?<intent-filter>(?:(?!</intent-filter>).)*?<action[^>]*android:name="android.intent.action.MAIN"[^>]*/>(?:(?!</intent-filter>).)*?<category[^>]*android:name="android.intent.category.LAUNCHER"[^>]*/>(?:(?!</intent-filter>).)*?</intent-filter>""",
            RegexOption.DOT_MATCHES_ALL
        ).find(androidManifest)?.let {
            general["Main Activity"] = it.groupValues[1]
        }

        Regex("""<manifest[^>]*package="([^"]+)""").find(androidManifest)?.let {
            general["Package Name"] = it.groupValues[1]
        }

        // 🔢 Version Name
        Regex("""android:versionName="([^"]+)"""").find(androidManifest)?.let {
            general["Version Name"] = it.groupValues[1]
        }

        Regex("""<uses-sdk[^>]*minSdkVersion="([^"]+)"""").find(androidManifest)?.let {
            general["Min SDK"] = it.groupValues[1]
        }
        Regex("""<uses-sdk[^>]*targetSdkVersion="([^"]+)"""").find(androidManifest)?.let {
            general["Target SDK"] = it.groupValues[1]
        }
        Regex("""<application[^>]*android:name="([^"]+)"""").find(androidManifest)?.let {
            general["Application"] = it.groupValues[1]
        }

        // Permissions
        var regex = Regex("""<uses-permission[^>]*android:name="([^"]+)"""")
        var matches = regex.findAll(androidManifest)
        for (i in matches) {
            var perm = i.groupValues[1]
            if (perm.startsWith("android.permission.")) {
                perm = perm.split("android.permission.", limit = 2)[1]
            }

            permissions.add(perm)
        }

        // Components
        for (tag in tagToKey.keys) {
            regex = Regex(
                """<$tag\b([^>]*)>(.*?)</$tag>""",
                RegexOption.DOT_MATCHES_ALL
            )
            matches = regex.findAll(androidManifest)
            Log.d("{OUTPUT}", "TAG: $tag")
            for (match in matches) {
                val attributes = match.groupValues[1]
                val innerContent = match.groupValues[2]

                val nameRegex = Regex("""android:name\s*=\s*"([^"]+)"""")
                val name = nameRegex.find(attributes)?.groupValues?.get(1) ?: continue

                val exportedRegex = Regex("""android:exported\s*=\s*"([^"]+)"""")
                val exportedRaw = exportedRegex.find(attributes)?.groupValues?.get(1)
                var exported = when (exportedRaw?.lowercase()) {
                    "true" -> true
                    "false" -> false
                    else -> null
                }

                if (exported != true && innerContent.contains("<intent-filter")) {
                    exported = true
                }

                val list = components.getValue(tagToKey[tag]!!)
                list.add(ComponentInfo(name, exported == true))
            }
        }

        callback(Manifest(permissions, components, general))
    }


    fun getManifest(artifactPath: String): Manifest? {
        val latch = java.util.concurrent.CountDownLatch(1)
        val apkFile = File(artifactPath)
        val parser = ApkFile(apkFile)
        val manifestXml = parser.manifestXml
        var ParsedManifest: Manifest? = null
        parseManifest(manifestXml) { result ->
            ParsedManifest = result
            latch.countDown()

        }

        latch.await()
        return ParsedManifest
    }
}