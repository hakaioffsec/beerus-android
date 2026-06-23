package io.hakaisecurity.beerusframework

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import androidx.core.net.toUri
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import io.hakaisecurity.beerusframework.composables.ADBScreen
import io.hakaisecurity.beerusframework.composables.BootScreen
import io.hakaisecurity.beerusframework.composables.FridaScreen
import io.hakaisecurity.beerusframework.composables.FridaScriptsScreen
import io.hakaisecurity.beerusframework.composables.HomeScreen
import io.hakaisecurity.beerusframework.composables.ManifestScreen
import io.hakaisecurity.beerusframework.composables.MemDumpScreen
import io.hakaisecurity.beerusframework.composables.PropertiesScreen
import io.hakaisecurity.beerusframework.composables.ProxyScreen
import io.hakaisecurity.beerusframework.composables.RootScreen
import io.hakaisecurity.beerusframework.composables.SandboxScreen
import io.hakaisecurity.beerusframework.composables.UpdateScreen
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.inEditorMode
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.animationStart
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.animationStartOnDifferentSelectedItem
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.moduleName
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.updateNavigationState
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.updateanimationStartOnDifferentSelectedItemState
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.updateanimationStartState
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.confirmRootModuleInstallerDialog
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.hasModule
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.hasRoot
import io.hakaisecurity.beerusframework.ui.theme.Home
import io.hakaisecurity.beerusframework.ui.theme.ibmFont
import io.hakaisecurity.beerusframework.ui.theme.iconMemory
import io.hakaisecurity.beerusframework.ui.theme.iconPackage
import io.hakaisecurity.beerusframework.ui.theme.iconProxy
import io.hakaisecurity.beerusframework.ui.theme.restart_alt

@SuppressLint("NewApi")
@Composable
fun BaseNavigationComponent(context: Context, modifier: Modifier) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dec() * 0.55f

    var noRoot by remember { mutableStateOf(false) }
    var noModule by remember { mutableStateOf(false) }

    var selectedItem by remember { mutableStateOf("Home") }

    val iconFrida = ImageVector.vectorResource(id = R.drawable.frida)
    val iconCode = ImageVector.vectorResource(id = R.drawable.ic_code)
    val iconRoot = ImageVector.vectorResource(id = R.drawable.rooticon)
    val iconADB = ImageVector.vectorResource(id = R.drawable.adb)
    val iconProperty = ImageVector.vectorResource(id = R.drawable.propertyicon)

    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val items = remember {
        listOf("Home", "Frida Setup", "Frida Scripts", "Sandbox Exf/", "Memory Dump", "Manifest", "ADB O/ Network", "Proxy Profiles", "Root Manager", "Properties", "Boot Options", "Update")
    }
    val selectedIndex = items.indexOf(selectedItem).coerceIn(0, items.lastIndex)
    val itemHeightPx = with(density) { 84.dp.toPx() }
    val contentTopPx = with(density) { 60.dp.toPx() }
    val highlightHeightPx = with(density) { 64.dp.toPx() }
    val highlightLeftPx = with(density) { 20.dp.toPx() }
    val highlightWidthPx = with(density) { (screenWidth + 10).dp.toPx() }
    val whiteCircleCenterXPx = with(density) { 50.dp.toPx() }
    val whiteCircleRadiusPx = with(density) { 30.dp.toPx() }
    val targetOffsetInContent = selectedIndex * itemHeightPx
    val animatedOffsetInContent by animateFloatAsState(
        targetValue = targetOffsetInContent,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "highlightOffset"
    )
    var isScrolling by remember { mutableStateOf(false) }
    val scrollAlpha by animateFloatAsState(
        targetValue = if (isScrolling) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "scrollbarOpacity"
    )

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            kotlinx.coroutines.delay(1000)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 3.dp, end = 0.dp, bottom = 0.dp)
            .zIndex(0f)
            .drawWithContent {
                val layoutInfo = listState.layoutInfo
                val totalItems = layoutInfo.totalItemsCount
                val scrollOffsetPx = listState.firstVisibleItemIndex * itemHeightPx + listState.firstVisibleItemScrollOffset
                val highlightY = contentTopPx + animatedOffsetInContent - scrollOffsetPx
                val redHighlightTopY = highlightY + (itemHeightPx - highlightHeightPx) / 2f
                val cornerRadiusPx = highlightHeightPx / 2f
                drawRoundRect(
                    color = Color.Red,
                    topLeft = Offset(highlightLeftPx, redHighlightTopY),
                    size = Size(highlightWidthPx, highlightHeightPx),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )
                val whiteCircleCenterY = highlightY + itemHeightPx / 2f
                drawCircle(
                    color = Color.White,
                    radius = whiteCircleRadiusPx,
                    center = Offset(whiteCircleCenterXPx, whiteCircleCenterY)
                )
                drawContent()

                val visibleItems = layoutInfo.visibleItemsInfo
                val viewportHeight = size.height

                if (totalItems > 0 && visibleItems.isNotEmpty()) {
                    val firstItemHeight = visibleItems.first().size.toFloat()
                    val totalContentHeight = (totalItems+1) * firstItemHeight
                    val maxScroll = totalContentHeight - viewportHeight

                    val currentScroll =
                        listState.firstVisibleItemIndex * firstItemHeight + listState.firstVisibleItemScrollOffset
                    val proportion =
                        if (maxScroll > 0f) (currentScroll / maxScroll).coerceIn(0f, 1f) else 0f

                    val capsuleHeight = 24.dp.toPx()
                    val capsuleWidth = 6.dp.toPx()
                    val topPadding = 90.dp.toPx()
                    val bottomPadding = 30.dp.toPx()
                    val capsuleY =
                        topPadding + proportion * (viewportHeight - capsuleHeight - topPadding - bottomPadding)

                    drawRoundRect(
                        color = Color.Red.copy(alpha = scrollAlpha),
                        topLeft = Offset(2.dp.toPx(), capsuleY),
                        size = Size(capsuleWidth, capsuleHeight),
                        cornerRadius = CornerRadius(capsuleWidth / 2, capsuleWidth / 2)
                    )
                }
            },
        state = listState,
        contentPadding = PaddingValues(top = 60.dp)
    ) {
        val icons = mutableListOf(Home, iconFrida, iconCode, iconPackage, iconMemory, iconPackage, iconADB, iconProxy, iconRoot, iconProperty, restart_alt, restart_alt)

        itemsIndexed(items) { index, item ->
            Row(
                modifier = if (index == items.lastIndex)
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 80.dp)
                else
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
            ) {
                Button(
                    onClick = {
                        if (!hasRoot && (item == "Root Manager" || item == "Boot Options" || item == "Properties")) {
                            noRoot = true
                        } else if (!hasModule && (item == "Boot Options" || item == "Properties")) {
                            noModule = true
                        } else {
                            val isDifferent = item != selectedItem
                            updateanimationStartOnDifferentSelectedItemState(isDifferent)

                            if (isDifferent) {
                                updateNavigationState(item)
                                selectedItem = item
                            }

                            updateanimationStartState(false)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .padding(12.dp)
                        .defaultMinSize(minWidth = screenWidth.dp + 10.dp, minHeight = 1.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        Modifier.width(screenWidth.dp + 10.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconTintTarget = when {
                            (!hasRoot && (item == "Root Manager" || item == "Boot Options")) || (!hasModule && (item == "Boot Options" || item == "Properties")) -> Color(0xFF858585)
                            selectedItem == item -> Color.Red
                            else -> Color.White
                        }
                        val iconTint by animateColorAsState(
                            targetValue = iconTintTarget,
                            animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
                            label = "iconTint"
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.Transparent)
                                .padding(16.dp)
                                .zIndex(10f)
                        ) {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = "Icon",
                                tint = iconTint,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Text(
                            text = item,
                            modifier = Modifier.padding(8.dp, 0.dp, 0.dp, 0.dp),
                            textAlign = TextAlign.Right,
                            fontSize = 14.sp,
                            fontWeight = if(selectedItem == item) FontWeight.Bold else FontWeight.Normal,
                            color = if ((!hasRoot && (item == "Root Manager" || item == "Boot Options" || item == "Properties")) || (!hasModule && (item == "Boot Options" || item == "Properties"))) Color(0xFF858585) else Color.White,
                            fontFamily = ibmFont
                        )
                    }
                }
            }
        }
    }

    if (noRoot) {
        AlertDialog(
            onDismissRequest = { noRoot = false },
            title = { Text("Note") },
            text = {
                Text(
                    text = "Hey, if you want to use this feature you may install Magisk or KernelSU!",
                    fontSize = 18.sp
                )
            },
            confirmButton = {
                Button(onClick = {
                    noRoot = false
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, "https://topjohnwu.github.io/Magisk/".toUri())
                    )
                }) { Text("Magisk") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        noRoot = false
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, "https://kernelsu.org/".toUri())
                        )
                    }) { Text("KernelSU") }

                    TextButton(onClick = { noRoot = false }) {
                        Text("Dismiss", fontFamily = ibmFont)
                    }
                }
            }
        )
    }

    if (noModule) {
        AlertDialog(
            onDismissRequest = { noModule = false },
            title = { Text("Note") },
            text = { Text(text = "Hey, if you want to use this feature you may install our module!", fontSize = 18.sp) },
            confirmButton = {
                Button(onClick = {
                    noModule = false
                    confirmRootModuleInstallerDialog(context)
                }) {
                    Text("Install")
                }
            },
            dismissButton = {
                TextButton(onClick = { noModule = false }) {
                    Text("Do After", fontFamily = ibmFont)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing, delayMillis = if(!animationStartOnDifferentSelectedItem) 0 else 250),
        label = "transitionMenuAnimation"
    )

    val transition by animateFloatAsState(
        targetValue = if (animationStart) screenWidth else 0f,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing, delayMillis = if(!animationStartOnDifferentSelectedItem) 0 else 250),
        label = "transitionAnimation"
    )

    val rotation by animateFloatAsState(
        targetValue = if (animationStart) -20f else 0f,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing, delayMillis = if(!animationStartOnDifferentSelectedItem) 0 else 250),
        label = "rotationAnimation"
    )

    val scale by animateFloatAsState(
        targetValue = if (animationStart) 0.85f else 1f,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing, delayMillis = if(!animationStartOnDifferentSelectedItem) 0 else 250),
        label = "scaleAnimation"
    )

    val borderRadius by animateFloatAsState(
        targetValue = if (animationStart) 16f else 0f,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing, delayMillis = if(!animationStartOnDifferentSelectedItem) 0 else 250),
        label = "borderRadiusAnimation"
    )

    BaseNavigationComponent(context, modifier.graphicsLayer{
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
        .clipToBounds()
        .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            if (animationStart) {
                updateanimationStartState(false)
            }
        }
    ) {
        if (moduleName != "Home") {
            GlideImage(
                imageModel = { R.drawable.cyberpunklines_bg },
                modifier = Modifier.fillMaxSize(),
                imageOptions = ImageOptions(
                    contentScale = ContentScale.FillBounds
                )
            )
        } else {
            val imageWidth = (configuration.screenWidthDp * 0.9f).dp
            val imageHeight = (imageWidth * (250f / 200f))

            Image(
                painter = painterResource(id = R.drawable.ray),
                contentDescription = null,
                modifier = Modifier
                    .size(width = imageWidth * 0.9f, height = imageHeight)
                    .align(Alignment.TopStart)
                    .offset(x = (-65).dp, y = (-imageHeight / 4))
                    .graphicsLayer {
                        rotationZ = 180f
                        rotationX = 180f
                    }
                    .zIndex(-1f)
            )

            Image(
                painter = painterResource(id = R.drawable.ray),
                contentDescription = null,
                modifier = Modifier
                    .size(width = imageWidth * 0.9f, height = imageHeight)
                    .align(Alignment.TopEnd)
                    .offset(x = (65).dp, y = (-imageHeight / 33))
                    .graphicsLayer {
                        rotationX = 180f
                    }
                    .zIndex(-1f)
            )
        }

        Box(modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(top = 10.dp, start = 30.dp)) {
            Icon(imageVector = if (!animationStart) iconMenu else iconClose,
                contentDescription = "Icon",
                tint = Color.White,
                modifier = modifier
                    .size(22.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        if (!inEditorMode) updateanimationStartState(!animationStart)
                    }
            )
        }

        Box(modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(top = 10.dp)) {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (moduleName != "Home") moduleName else "",
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
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            when (moduleName) {
                "Home" -> HomeScreen(modifier)
                "Frida Setup" -> FridaScreen(modifier, activity)
                "Frida Scripts" -> FridaScriptsScreen(modifier, activity)
                "Sandbox Exf/" -> SandboxScreen(modifier)
                "Memory Dump" -> MemDumpScreen(modifier)
                "Proxy Profiles" -> ProxyScreen(modifier, activity)
                "Manifest" -> ManifestScreen(modifier)
                "ADB O/ Network" -> ADBScreen(modifier, activity)
                "Root Manager" -> RootScreen(modifier, activity)
                "Properties" -> PropertiesScreen(modifier, activity)
                "Boot Options" -> BootScreen(modifier)
                "Update" -> UpdateScreen(modifier, activity)
                else -> HomeScreen(modifier)
            }
        }
    }
}