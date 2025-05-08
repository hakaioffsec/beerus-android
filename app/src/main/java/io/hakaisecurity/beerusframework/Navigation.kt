package io.hakaisecurity.beerusframework

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import io.hakaisecurity.beerusframework.composables.ADBScreen
import io.hakaisecurity.beerusframework.composables.BootScreen
import io.hakaisecurity.beerusframework.composables.FridaScreen
import io.hakaisecurity.beerusframework.composables.HomeScreen
import io.hakaisecurity.beerusframework.composables.MagiskScreen
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.inEditorMode
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.animationStart
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.moduleName
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.updateNavigationState
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.updateanimationStartState
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.hasMagisk
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.hasModule
import io.hakaisecurity.beerusframework.ui.theme.Home
import io.hakaisecurity.beerusframework.ui.theme.ibmFont
import io.hakaisecurity.beerusframework.ui.theme.restart_alt

@SuppressLint("NewApi")
@Composable
fun BaseNavigationComponent(modifier: Modifier) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dec() * 0.55f

    var noMagisk by remember { mutableStateOf(false) }
    var noModule by remember { mutableStateOf(false) }

    var selectedItem by remember { mutableStateOf("Home") }

    Column(
        modifier
            .fillMaxSize()
            .padding(5.dp, 50.dp, 0.dp, 0.dp)
            .zIndex(0f)
    ) {
        val iconFrida = ImageVector.vectorResource(id = R.drawable.frida)
        val iconMagisk = ImageVector.vectorResource(id = R.drawable.magiskicon)
        val iconADB = ImageVector.vectorResource(id = R.drawable.adb)

        val items = mutableListOf("Home", "Frida Setup", "ADB O/ Network", "Magisk Manager", "Boot Options")
        val icons = mutableListOf(Home, iconFrida, iconADB, iconMagisk, restart_alt)
        var iconIndex = 0

        items.forEach { item ->
            Row(Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (!hasMagisk && (item == "Magisk Manager" || item == "Boot Options")){
                            noMagisk = true
                        }else{
                            if(!hasModule && item == "Boot Options"){
                                noModule = true
                            }else{
                                updateNavigationState(item); updateanimationStartState(false); selectedItem = item
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if ((!hasMagisk && (item == "Magisk Manager" || item == "Boot Options")) || (!hasModule && item == "Boot Options")){
                                Color.Transparent
                            }else{
                                if (selectedItem == item){
                                    Color.Red
                                } else {
                                    Color.Transparent
                                }
                            }
                    ),
                    modifier = Modifier
                        .padding(12.dp)
                        .defaultMinSize(minWidth = screenWidth.dp, minHeight = 1.dp),
                    contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
                ) {
                    Row(
                        Modifier.width(screenWidth.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(if (selectedItem == item) Color.White else Color.Transparent)
                                .padding(16.dp)
                                .zIndex(10f)
                        ) {
                            Icon(
                                imageVector = icons[iconIndex],
                                contentDescription = "Icon",
                                tint = if ((!hasMagisk && (item == "Magisk Manager" || item == "Boot Options")) || (!hasModule && item == "Boot Options")){
                                    Color(0xFF858585)
                                }else{
                                    if (selectedItem == item){
                                        Color.Red
                                    } else {
                                        Color.White
                                    }
                                },
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Spacer(modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp))

                        Text(
                            text = item,
                            textAlign = TextAlign.Right,
                            fontSize = 17.sp,
                            color = if ((!hasMagisk && (item == "Magisk Manager" || item == "Boot Options")) || (!hasModule && item == "Boot Options")) Color(0xFF858585) else Color.White,
                            fontFamily = ibmFont
                        )
                    }
                }
            }

            iconIndex++
        }
    }

    if (noMagisk) {
        AlertDialog(
            onDismissRequest = { noMagisk = false },
            title = { Text("Note") },
            text = { Text(text = "Hey, if you want to use this feature you may install Magisk!", fontSize = 18.sp) },
            confirmButton = {
                Button(onClick = { noMagisk = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (noModule) {
        AlertDialog(
            onDismissRequest = { noModule = false },
            title = { Text("Note") },
            text = { Text(text = "Hey, if you want to use this feature you may install our module on startup!", fontSize = 18.sp) },
            confirmButton = {
                Button(onClick = { noModule = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@SuppressLint("NewApi")
@Composable
fun NavigationFunc(context: Context, modifier: Modifier = Modifier) {
    val activity = context as Activity

    val configuration = LocalConfiguration.current
    val screenDensity = LocalDensity.current
    val screenWidth = with(screenDensity) { configuration.screenWidthDp.dp.toPx() / 1.75f }

    val iconMenu = ImageVector.vectorResource(id = R.drawable.menu)
    val iconClose = ImageVector.vectorResource(id = R.drawable.close)

    val transitionMenu by animateFloatAsState(
        targetValue = if (animationStart) 0f else -configuration.screenWidthDp.dp.value * 2,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "transitionMenuAnimation"
    )

    val transition by animateFloatAsState(
        targetValue = if (animationStart) screenWidth else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "transitionAnimation"
    )

    val rotation by animateFloatAsState(
        targetValue = if (animationStart) -20f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "rotationAnimation"
    )

    val scale by animateFloatAsState(
        targetValue = if (animationStart) 0.85f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "scaleAnimation"
    )

    val borderRadius by animateFloatAsState(
        targetValue = if (animationStart) 16f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "borderRadiusAnimation"
    )

    val transitionColor = animateColorAsState(
        targetValue = Color.Red,
        animationSpec = tween(durationMillis = 500),
        label = "colorAnimation"
    )

    BaseNavigationComponent(modifier.graphicsLayer{
        translationX = transitionMenu
    })

    Box(modifier
        .fillMaxSize()
        .zIndex(1f)
        .graphicsLayer {
            translationX = transition
            scaleX = scale
            scaleY = scale
            rotationY = rotation
            cameraDistance = 10 * density
        }
        .drawWithContent {
            val radiusPx = borderRadius.dp.toPx()
            drawRoundRect(
                color = Color(0xFF0B0A0B),
                size = size,
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
            drawContent()
            drawRoundRect(
                color = Color(0xFF0B0A0B),
                cornerRadius = CornerRadius(radiusPx, radiusPx),
                size = size,
                style = Stroke(width = 4.dp.toPx())
            )
        }
        .paint(
            painterResource(id = R.drawable.cyberpunklines_bg),
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(transitionColor.value)
        )
        .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            if (animationStart) {
                updateanimationStartState(false)
            }
        }
    ) {
        Box(modifier = modifier
            .fillMaxSize()
            .padding(top = 30.dp, start = 30.dp)) {
            Icon(imageVector = if (!animationStart) iconMenu else iconClose,
                contentDescription = "Icon",
                tint = Color.White,
                modifier = modifier
                    .size(22.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        if(!inEditorMode) updateanimationStartState(!animationStart)
                    }
            )
        }

        Box(modifier = modifier
            .fillMaxSize()
            .padding(top = 30.dp)) {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = moduleName,
                    fontFamily = ibmFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            when (moduleName) {
                "Home" -> HomeScreen(modifier)
                "Frida Setup" -> FridaScreen(modifier, activity)
                "Magisk Manager" -> MagiskScreen(modifier, activity)
                "ADB O/ Network" -> ADBScreen(modifier, activity)
                "Boot Options" -> BootScreen(modifier)
                else -> HomeScreen(modifier)
            }
        }
    }
}