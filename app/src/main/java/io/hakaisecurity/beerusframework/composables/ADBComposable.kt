package io.hakaisecurity.beerusframework.composables

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.hakaisecurity.beerusframework.R
import io.hakaisecurity.beerusframework.core.functions.adb.AdbOverNetwork.Companion.adbStart
import io.hakaisecurity.beerusframework.core.functions.adb.AdbOverNetwork.Companion.adbStop
import io.hakaisecurity.beerusframework.core.functions.adb.AdbOverNetwork.Companion.getIpAddr
import io.hakaisecurity.beerusframework.core.models.AdbState.Companion.adbRunningState
import io.hakaisecurity.beerusframework.ui.theme.ibmFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AdbWifiConnectionState {
    Idle,
    Connecting,
    Connected,
    Error
}

@Composable
fun ADBScreen(modifier: Modifier, activity: Activity) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dec()

    val scope = rememberCoroutineScope()

    var noConnection by remember { mutableStateOf(false) }
    var adbUiRunningState by remember { mutableStateOf(adbRunningState) }
    var isLoading by remember { mutableStateOf(false) }
    var connectionAnimationState by remember {
        mutableStateOf(
            if (adbRunningState) AdbWifiConnectionState.Connected
            else AdbWifiConnectionState.Idle
        )
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
    ) {
        AdbWifiConnectionAnimation(
            state = connectionAnimationState,
            size = (screenWidth / 2).dp
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Status: ",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = ibmFont
            )

            Text(
                text = if (adbUiRunningState) "Running" else "Stopped",
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = ibmFont
            )

            Canvas(modifier = modifier
                .size(25.dp)
                .padding(start = 5.dp)) {
                drawCircle(
                    color = if (adbUiRunningState) Color.Green else Color.Red
                )
            }
        }

        Spacer(
            modifier = modifier.height(1.dp)
        )

        if (adbUiRunningState) {
            Text(
                text = "Command:",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = ibmFont
            )

            Text(
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray)
                    .padding(4.dp),
                text = "adb connect " + getIpAddr().toString() + ":5555",
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = ibmFont
            )
        }

        Spacer(
            modifier = modifier.height(20.dp)
        )

        Row {
            Button(
                onClick = {
                    if (isLoading) return@Button

                    if (adbUiRunningState) {
                        isLoading = true
                        scope.launch {
                            withContext(Dispatchers.IO) { adbStop() }
                            adbUiRunningState = false
                            connectionAnimationState = AdbWifiConnectionState.Idle
                            isLoading = false
                        }
                    } else {
                        connectionAnimationState = AdbWifiConnectionState.Connecting
                        isLoading = true
                        scope.launch {
                            val ip = withContext(Dispatchers.IO) {
                                adbStart()
                                getIpAddr()
                            }

                            if (ip == null) {
                                withContext(Dispatchers.IO) { adbStop() }
                                adbUiRunningState = false
                                connectionAnimationState = AdbWifiConnectionState.Error
                                noConnection = true
                            } else {
                                adbUiRunningState = true
                                connectionAnimationState = AdbWifiConnectionState.Connected
                            }
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color(0xFFBDBDBD)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = modifier
                    .padding(start = 5.dp)
                    .width(200.dp)
            ) {
                Text(
                    text = if (!adbUiRunningState) "Start ADB" else "Stop ADB",
                    fontSize = 11.sp,
                    color = Color.Red,
                    modifier = modifier.padding(1.dp, 3.dp),
                    fontFamily = ibmFont
                )
            }
        }
    }

    if (noConnection) {
        AlertDialog(
            onDismissRequest = { noConnection = false },
            title = { Text("Warning") },
            text = { Text(text = "No internet connection found!", fontSize = 18.sp) },
            confirmButton = {
                Button(onClick = {
                    noConnection = false
                }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun AdbWifiConnectionAnimation(
    state: AdbWifiConnectionState,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val transition = rememberInfiniteTransition(label = "adbWifi")


    // Pulsos escalonados (dot -> inner -> middle -> outer) para Connecting e Connected.
    val dotWave = transition.pulse(periodMillis = 700, delayMillis = 0, label = "dotWave")
    val innerWave = transition.pulse(periodMillis = 700, delayMillis = 150, label = "innerWave")
    val middleWave = transition.pulse(periodMillis = 700, delayMillis = 300, label = "middleWave")
    val outerWave = transition.pulse(periodMillis = 700, delayMillis = 450, label = "outerWave")

    val subtlePulse = transition.pulse(
        periodMillis = 1200,
        delayMillis = 0,
        min = 0.92f,
        max = 1.05f,
        label = "subtlePulse"
    )

    val errorBlink = transition.pulse(
        periodMillis = 250,
        delayMillis = 0,
        min = 0.2f,
        max = 1f,
        label = "errorBlink"
    )

    val shake by transition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(80, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    val popScale = remember { Animatable(1f) }
    LaunchedEffect(state) {
        if (state == AdbWifiConnectionState.Connected) {
            popScale.snapTo(0.7f)
            popScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        } else {
            popScale.snapTo(1f)
        }
    }

    val arcColorFilter = when (state) {
        AdbWifiConnectionState.Connected -> ColorFilter.tint(Color(0xFF4CAF50))
        AdbWifiConnectionState.Error -> ColorFilter.tint(Color.Red)
        else -> null
    }
    val dotColorFilter = arcColorFilter

    fun connectedLayer(wave: Float): Pair<Float, Float> {
        val norm = ((wave - 0.2f) / 0.8f).coerceIn(0f, 1f)
        val alpha = 0.5f + norm * 0.5f
        val scale = (0.97f + norm * 0.06f) * popScale.value
        return alpha to scale
    }

    fun arcLayer(wave: Float): Pair<Float, Float> = when (state) {
        AdbWifiConnectionState.Idle -> 0.3f to 1f
        AdbWifiConnectionState.Connecting -> wave to (0.95f + wave * 0.1f)
        AdbWifiConnectionState.Connected -> connectedLayer(wave)
        AdbWifiConnectionState.Error -> 1f to 1f
    }

    val (innerAlpha, innerScale) = arcLayer(innerWave)
    val (middleAlpha, middleScale) = arcLayer(middleWave)
    val (outerAlpha, outerScale) = arcLayer(outerWave)

    val (dotAlpha, dotScale) = when (state) {
        AdbWifiConnectionState.Idle -> 0.6f to subtlePulse
        AdbWifiConnectionState.Connecting -> dotWave to (0.9f + dotWave * 0.15f)
        AdbWifiConnectionState.Connected -> connectedLayer(dotWave)
        AdbWifiConnectionState.Error -> errorBlink to 1f
    }

    val shakeOffset = if (state == AdbWifiConnectionState.Error) shake else 0f

    Box(
        modifier = modifier
            .size(size)
            .offset(x = shakeOffset.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.adb_wifi_android_head),
            contentDescription = null,
            modifier = Modifier.size(size)
        )

        WifiLayerImage(
            resId = R.drawable.adb_wifi_arc_outer,
            size = size,
            alpha = outerAlpha,
            scale = outerScale,
            colorFilter = arcColorFilter
        )

        WifiLayerImage(
            resId = R.drawable.adb_wifi_arc_middle,
            size = size,
            alpha = middleAlpha,
            scale = middleScale,
            colorFilter = arcColorFilter
        )

        WifiLayerImage(
            resId = R.drawable.adb_wifi_arc_inner,
            size = size,
            alpha = innerAlpha,
            scale = innerScale,
            colorFilter = arcColorFilter
        )

        WifiLayerImage(
            resId = R.drawable.adb_wifi_dot,
            size = size,
            alpha = dotAlpha,
            scale = dotScale,
            colorFilter = dotColorFilter
        )
    }
}

@Composable
private fun WifiLayerImage(
    resId: Int,
    size: Dp,
    alpha: Float,
    scale: Float,
    colorFilter: ColorFilter?
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = null,
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            },
        colorFilter = colorFilter
    )
}


@Composable
private fun InfiniteTransition.pulse(
    periodMillis: Int,
    delayMillis: Int,
    min: Float = 0.2f,
    max: Float = 1f,
    label: String
): Float {
    val value by animateFloat(
        initialValue = min,
        targetValue = max,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delayMillis)
        ),
        label = label
    )
    return value
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun AdbWifiConnectionAnimationPreview() {
    Column {
        AdbWifiConnectionAnimation(state = AdbWifiConnectionState.Idle, size = 160.dp)
        AdbWifiConnectionAnimation(state = AdbWifiConnectionState.Connecting, size = 160.dp)
        AdbWifiConnectionAnimation(state = AdbWifiConnectionState.Connected, size = 160.dp)
        AdbWifiConnectionAnimation(state = AdbWifiConnectionState.Error, size = 160.dp)
    }
}
