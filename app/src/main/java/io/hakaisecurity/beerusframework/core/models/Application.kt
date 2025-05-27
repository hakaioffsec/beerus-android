package io.hakaisecurity.beerusframework.core.models

import android.graphics.Bitmap

data class Application(
    val artifactPath: String,
    val icon: Bitmap?,
    val container: String?,
    val identifier: String,
    val name: String?
)