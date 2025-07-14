package io.hakaisecurity.beerusframework.composables

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.hakaisecurity.beerusframework.core.functions.bootOptions.bootFunctions.Companion.changeProperties
import io.hakaisecurity.beerusframework.core.functions.bootOptions.bootFunctions.Companion.getProperties
import io.hakaisecurity.beerusframework.core.functions.proxyProfiles.ProxyProfiles.ProxyData
import io.hakaisecurity.beerusframework.core.functions.proxyProfiles.ProxyProfiles.addProfile
import io.hakaisecurity.beerusframework.core.functions.proxyProfiles.ProxyProfiles.deleteProxy
import io.hakaisecurity.beerusframework.core.functions.proxyProfiles.ProxyProfiles.editProfile
import io.hakaisecurity.beerusframework.core.functions.proxyProfiles.ProxyProfiles.getProfiles
import io.hakaisecurity.beerusframework.core.functions.proxyProfiles.ProxyProfiles.selectProfile
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.animationStart
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.updateanimationStartState
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.hasModule
import io.hakaisecurity.beerusframework.ui.theme.Edit
import io.hakaisecurity.beerusframework.ui.theme.Trash
import io.hakaisecurity.beerusframework.ui.theme.ibmFont

@Composable
fun ProxyScreen(modifier: Modifier, context: Context) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dec() * .40f

    var newProxyName by remember { mutableStateOf("") }
    var newProxyCon by remember { mutableStateOf("") }

    val proxiesState = remember { mutableStateOf(getProfiles(context)) }
    var selectedProxy by remember { mutableStateOf<String?>(null) }

    val proxies = proxiesState.value
    var showProfileDialog by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }
    var proxyToEdit by remember { mutableStateOf("") }
    var proxyEditValue by remember { mutableStateOf("") }

    fun refreshProxies() {
        proxiesState.value = getProfiles(context)
    }

    LaunchedEffect(Unit) {
        refreshProxies()

        if(hasModule) {
            getProperties { statusMap ->
                selectedProxy = statusMap["proxy"]
            }
        }else{
            selectedProxy = getProfiles(context).find { it.selected }?.conString
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(35.dp, 0.dp)
            .fillMaxSize()
    ) {
        Spacer(modifier = modifier.height(screenHeight.dp))

        Button(
            onClick = {
                if(!animationStart) {
                    showProfileDialog = true
                } else {
                    updateanimationStartState(false)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = modifier
                .padding(end = 5.dp)
                .width(140.dp)
        ) {
            Text(
                text = "Add profile",
                fontSize = 11.sp,
                color = Color.Red,
                modifier = modifier.padding(0.dp, 3.dp),
                fontFamily = ibmFont
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            proxies.forEach { proxy ->
                val isSelected = proxy.conString == selectedProxy

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 5.dp)
                        .background(Color(0xFF151515), shape = RoundedCornerShape(4.dp))
                        .border(width = 2.dp, color = Color.White)
                ) {
                    Column {
                        Column(Modifier.padding(16.dp, 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = proxy.name,
                                    textDecoration = TextDecoration.None,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = ibmFont,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                ToggleProfileButton(
                                    modifier = Modifier,
                                    status = isSelected,
                                    onToggle = {
                                        if(!animationStart) {
                                            if (selectedProxy == proxy.conString) {
                                                selectedProxy = null
                                                selectProfile(context, ":0")
                                                if (hasModule) changeProperties("proxy", ":0")
                                            } else {
                                                selectedProxy = proxy.conString
                                                selectProfile(context, proxy.conString)
                                                if (hasModule) changeProperties(
                                                    "proxy",
                                                    proxy.conString
                                                )
                                            }
                                        }else {
                                            updateanimationStartState(false)
                                        }
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = proxy.conString,
                                textDecoration = TextDecoration.None,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = ibmFont,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(
                            modifier = Modifier
                                .height(2.dp)
                                .fillMaxWidth()
                                .background(Color.White)
                        )

                        Row(
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(16.dp, 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Edit,
                                    contentDescription = "Edit",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(end = 5.dp)
                                )

                                Text(
                                    text = "Edit",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = ibmFont,
                                    fontSize = 14.sp,
                                    modifier = Modifier.clickable {
                                        if (!animationStart) {
                                            proxyToEdit = proxy.name
                                            proxyEditValue = proxy.conString
                                            showEditDialog = true
                                        } else {
                                            updateanimationStartState(false)
                                        }
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        if(!animationStart) {
                                            deleteProxy(context, proxy)
                                            refreshProxies()
                                            if (selectedProxy == proxy.conString) selectedProxy = null
                                        }else {
                                            updateanimationStartState(false)
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Remove",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = ibmFont,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = Trash,
                                    contentDescription = "Trash",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(start = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showProfileDialog) {
        val regex = Regex("""^(\d{1,3}\.){0,3}\d{0,3}(:\d{0,5})?$""")
        val regex2 = Regex("""^(\d{1,3}\.){3}\d{1,3}:\d{1,5}$""")

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Enter profile details") },
            text = {
                Column {
                    TextField(
                        value = newProxyName,
                        onValueChange = { newProxyName = it },
                        placeholder = { Text("Profile name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = newProxyCon,
                        onValueChange = {
                            if (it.isEmpty() || regex.matches(it)) {
                                newProxyCon = it
                            }
                        },
                        placeholder = { Text("0.0.0.0:4444") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if(regex2.matches(newProxyCon)){
                    showProfileDialog = false
                    addProfile(context, ProxyData(name = newProxyName, conString = newProxyCon, selected = false))
                    refreshProxies()
                        }
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog) {
        val regex = Regex("""^(\d{1,3}\.){0,3}\d{0,3}(:\d{0,5})?$""")
        val regex2 = Regex("""^(\d{1,3}\.){3}\d{1,3}:\d{1,5}$""")

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Enter profile details") },
            text = {
                Column {
                    TextField(
                        value = proxyEditValue,
                        onValueChange = {
                            if (it.isEmpty() || regex.matches(it)) {
                                proxyEditValue = it
                            }
                        },
                        placeholder = { Text("0.0.0.0:4444") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if(regex2.matches(proxyEditValue)){
                        editProfile(
                            context,
                            ProxyData(name = proxyToEdit, conString = proxies.find { it.name == proxyToEdit }?.conString ?: "", selected = false),
                            ProxyData(name = proxyToEdit, conString = proxyEditValue, selected = false)
                        )
                        showEditDialog = false
                        refreshProxies()
                    }
                }) {
                    Text("Edit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ToggleProfileButton(
    modifier: Modifier = Modifier,
    status: Boolean,
    onToggle: () -> Unit
) {
    val toggleWidth = 48.dp
    val toggleHeight = 24.dp
    val horizontalPadding = 0.dp
    val verticalPadding = 0.dp

    val dotSize = toggleHeight - verticalPadding * 2

    val backgroundColor by animateColorAsState(
        targetValue = if (!status) Color.White else Color(0xFFAB0100),
        label = "bgColor"
    )

    val horizontalOffset by animateDpAsState(
        targetValue = if (status) (toggleWidth - dotSize - horizontalPadding) else horizontalPadding,
        label = "dotPosition"
    )

    Box(
        modifier = modifier
            .width(toggleWidth)
            .height(toggleHeight)
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable { onToggle() }
    ) {
        Box(
            modifier = Modifier
                .offset(x = horizontalOffset, y = verticalPadding)
                .size(dotSize)
                .background(Color.Red, CircleShape)
        )
    }
}