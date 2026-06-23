package io.hakaisecurity.beerusframework.composables

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.hakaisecurity.beerusframework.R
import io.hakaisecurity.beerusframework.core.functions.frida.FridaSetup.Companion.startFridaModule
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.currentFridaVersionDownloaded
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.currentFridaVersionFromList
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.fridaRunningState
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.fridaVersions
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.animationStart
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.updateanimationStartState
import io.hakaisecurity.beerusframework.ui.theme.ibmFont

@ExperimentalLayoutApi
@Composable
fun FridaScreen(modifier: Modifier, activity: Activity) {
    var expanded by remember { mutableStateOf(false) }
    var showAddVersionDialog by remember { mutableStateOf(false) }
    var newVersionText by remember { mutableStateOf("") }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dec() * .30f

    val borderRadius by animateFloatAsState(
        targetValue = if (animationStart) 16f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "borderRadiusAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize().padding(horizontal = 30.dp)
    ) {
        Spacer(modifier = modifier.height(screenHeight.dp))

        Image(
            painter = painterResource(id = R.drawable.fridalogo),
            contentDescription = "Frida Logo"
        )

        Spacer(modifier = modifier.height(10.dp))

        Row {
            Text(
                text = "Version: ",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = ibmFont
            )

            Text(
                text = currentFridaVersionDownloaded,
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = ibmFont
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Status: ",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = ibmFont
            )

            Text(
                text = when (fridaRunningState) {
                    "start" -> "Stopped"
                    "stop" -> "Running"
                    else -> "Downloading"
                },
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = ibmFont
            )

            androidx.compose.foundation.Canvas(
                modifier = modifier
                    .size(25.dp)
                    .padding(start = 5.dp)
            ) {
                drawCircle(
                    color = when (fridaRunningState) {
                        "start" -> Color.Red
                        "stop" -> Color.Green
                        else -> Color.Yellow
                    }
                )
            }
        }

        Spacer(modifier = modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = {
                    if (!animationStart) {
                        expanded = true
                    } else {
                        updateanimationStartState(false)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Versions",
                    fontSize = 12.sp,
                    color = Color.Red,
                    fontFamily = ibmFont
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Add version manually", fontFamily = ibmFont) },
                    onClick = {
                        expanded = false
                        showAddVersionDialog = true
                    }
                )
                if (fridaVersions.isEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "No versions available",
                                fontFamily = ibmFont
                            )
                        },
                        onClick = { expanded = false }
                    )
                } else {
                    fridaVersions.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = item,
                                    fontFamily = ibmFont,
                                    color = if (item == currentFridaVersionFromList) Color.White else Color.Black
                                )
                            },
                            onClick = {
                                currentFridaVersionFromList = item
                                expanded = false
                            },
                            modifier = Modifier.background(color = if (item == currentFridaVersionFromList) Color.Red else Color.White)
                        )
                    }
                }
            }

            Button(
                onClick = {
                    when (fridaRunningState) {
                        "processing" -> {}
                        else -> {
                            currentFridaVersionFromList?.let {
                                if (it == "None") {
                                    startFridaModule(
                                        activity,
                                        fridaVersions[0],
                                        fridaRunningState
                                    )
                                } else {
                                    startFridaModule(
                                        activity,
                                        it,
                                        fridaRunningState
                                    )
                                }
                            }
                        }
                    }
                },
                colors = when (fridaRunningState) {
                    "processing" -> ButtonDefaults.buttonColors(containerColor = Color(0xFFBDBDBD))
                    else -> ButtonDefaults.buttonColors(containerColor = Color.White)
                },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = when (fridaRunningState) {
                        "stop" -> "Stop Frida"
                        else -> "Start Frida"
                    },
                    fontSize = 12.sp,
                    color = when (fridaRunningState) {
                        "processing" -> Color(0xFFA10000)
                        else -> Color.Red
                    },
                    fontFamily = ibmFont
                )
            }
        }
    }

    if (showAddVersionDialog) {
        AlertDialog(
            onDismissRequest = { showAddVersionDialog = false },
            title = { Text("Add Version") },
            text = {
                TextField(
                    value = newVersionText,
                    onValueChange = { newVersionText = it },
                    placeholder = { Text("e.g., 16.1.2") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newVersionText.isNotBlank()) {
                        fridaVersions.add(newVersionText.trim())
                        currentFridaVersionFromList = newVersionText.trim()
                        showAddVersionDialog = false
                        newVersionText = ""
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddVersionDialog = false
                    newVersionText = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
