import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.hakaisecurity.beerusframework"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.hakaisecurity.beerusframework"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

fun findLatestNdkBuild(): File {
    val os = System.getProperty("os.name").lowercase()
    val userHome = System.getProperty("user.home")
    val env = System.getenv()
    val sdkRoot = env["ANDROID_SDK_ROOT"] ?: env["ANDROID_HOME"] ?: "$userHome/Library/Android/sdk"
    val ndkDir = File(sdkRoot, "ndk")
    val ndkVersion = ndkDir
        .listFiles { file -> file.isDirectory }?.maxOfOrNull { it.name }
        ?: throw GradleException("No NDK versions found in $ndkDir")
    val ndkBuildName = if (os.contains("windows")) "ndk-build.cmd" else "ndk-build"
    val ndkBuildFile = File(ndkDir, "$ndkVersion/$ndkBuildName")
    if (!ndkBuildFile.exists()) {
        throw GradleException("ndk-build not found at: $ndkBuildFile")
    }
    return ndkBuildFile
}

val unzipFridaCoreLibs by tasks.registering {
    val archs = listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
    val baseDir = layout.projectDirectory.dir("../fridaCore/jni/libs")

    doLast {
        archs.forEach { arch ->
            val archDir = baseDir.dir(arch).asFile
            val zipFile = File(archDir, "libfrida-core.zip")

            if (zipFile.exists()) {
                println("Unzipping for architecture: $arch")
                project.copy {
                    from(project.zipTree(zipFile))
                    into(archDir)
                }
            }
        }
    }
}

val buildNativeFrida by tasks.registering(Exec::class) {
    dependsOn(unzipFridaCoreLibs)

    val ndkBuild = findLatestNdkBuild()
    workingDir = layout.projectDirectory.dir("../fridaCore").asFile
    commandLine = listOf(ndkBuild.absolutePath)
}

val zipFolderTaskRoot = tasks.register("zipModule") {
    val inputRootDir = layout.projectDirectory.dir("../rootModule")
    val outputRootZip = layout.buildDirectory.file("generated/beerusRootModule.zip")

    inputs.dir(inputRootDir)
    outputs.file(outputRootZip)

    doLast {
        val inputRoot: File = inputRootDir.asFile
        val outputRoot: File = outputRootZip.get().asFile
        outputRoot.parentFile.mkdirs()

        ZipOutputStream(outputRoot.outputStream()).use { zip ->
            inputRoot.walkTopDown().filter { it.isFile }.forEach { file ->
                val entryName = file.relativeTo(inputRoot).invariantSeparatorsPath
                zip.putNextEntry(ZipEntry(entryName))
                file.inputStream().copyTo(zip)
                zip.closeEntry()
            }
        }
    }
}

val zipFolderTaskFrida = tasks.register("zipFrida"){
    dependsOn(buildNativeFrida)

    val inputFridaDir = layout.projectDirectory.dir("../fridaCore/libs")
    val outputFridaZip = layout.buildDirectory.file("generated/fridaCore.zip")

    inputs.dir(inputFridaDir)
    outputs.file(outputFridaZip)

    doLast {
        val inputFrida: File = inputFridaDir.asFile
        val outputFrida: File = outputFridaZip.get().asFile
        outputFrida.parentFile.mkdirs()

        ZipOutputStream(outputFrida.outputStream()).use { zip ->
            inputFrida.walkTopDown().filter { it.isFile }.forEach { file ->
                val entryName = "libs/" + file.relativeTo(inputFrida).invariantSeparatorsPath
                zip.putNextEntry(ZipEntry(entryName))
                file.inputStream().copyTo(zip)
                zip.closeEntry()
            }
        }
    }
}

val buildNativeDbAgent by tasks.registering(Exec::class) {
    val ndkBuild = findLatestNdkBuild()
    workingDir = layout.projectDirectory.dir("../dbAgent").asFile
    commandLine = listOf(ndkBuild.absolutePath)
}

val zipFolderTaskDbAgent = tasks.register("zipDbAgent") {
    dependsOn(buildNativeDbAgent)

    val inputDbAgentDir = layout.projectDirectory.dir("../dbAgent/libs")
    val outputDbAgentZip = layout.buildDirectory.file("generated/dbAgent.zip")

    inputs.dir(inputDbAgentDir)
    outputs.file(outputDbAgentZip)

    doLast {
        val inputDbAgent: File = inputDbAgentDir.asFile
        val outputDbAgent: File = outputDbAgentZip.get().asFile
        outputDbAgent.parentFile.mkdirs()

        ZipOutputStream(outputDbAgent.outputStream()).use { zip ->
            inputDbAgent.walkTopDown().filter { it.isFile }.forEach { file ->
                val entryName = "libs/" + file.relativeTo(inputDbAgent).invariantSeparatorsPath
                zip.putNextEntry(ZipEntry(entryName))
                file.inputStream().copyTo(zip)
                zip.closeEntry()
            }
        }
    }
}

android.applicationVariants.all {
    val variant = this
    val variantName = variant.name.replaceFirstChar(Char::uppercaseChar)

    val copyZipsToAssets = tasks.register<Copy>("copyZipsToAssets$variantName") {
        dependsOn(zipFolderTaskRoot, zipFolderTaskFrida, zipFolderTaskDbAgent)

        val outputRootZip = layout.buildDirectory.file("generated/beerusRootModule.zip")
        val outputFridaZip = layout.buildDirectory.file("generated/fridaCore.zip")
        val outputDbAgentZip = layout.buildDirectory.file("generated/dbAgent.zip")
        from(outputRootZip) {
            rename { "beerusRootModule.zip" }
        }

        from(outputFridaZip) {
            rename { "fridaCore.zip" }
        }

        from(outputDbAgentZip) {
            rename { "dbAgent.zip" }
        }

        into(variant.mergeAssetsProvider.get().outputDir)
    }

    variant.mergeAssetsProvider.configure {
        dependsOn(copyZipsToAssets)
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.tukaani:xz:1.9")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("net.dongliu:apk-parser:2.6.10")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.testing)
    implementation(libs.androidx.ui.text.google.fonts)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
}