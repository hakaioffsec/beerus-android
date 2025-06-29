package io.hakaisecurity.beerusframework.core.models

import android.graphics.Bitmap

data class Process(
    val artifactPath: String,
    val icon: Bitmap?,
    val container: String?,
    val identifier: String,
    val pid: String,
    val name: String?
)