package io.hakaisecurity.beerusframework.composables

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
fun FridaScreen(modifier: Modifier, activity: Activity) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(60.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fridalogo),
            contentDescription = "Frida Logo"
        )

        Spacer(
            modifier = modifier.height(10.dp)
        )

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

            Canvas(modifier = modifier
                .size(25.dp)
                .padding(start = 5.dp)) {
                drawCircle(
                    color = when (fridaRunningState) {
                        "start" -> Color.Red
                        "stop" -> Color.Green
                        else -> Color.Yellow
                    }
                )
            }
        }

        Spacer(
            modifier = modifier.height(20.dp)
        )

        Row {
            Button(
                onClick = {
                    if (!animationStart) {
                        expanded = true
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
                    text = "Versions",
                    fontSize = 11.sp,
                    color = Color.Red,
                    modifier = modifier.padding(0.dp, 3.dp),
                    fontFamily = ibmFont
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (fridaVersions.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text(text = "No versions available", fontFamily = ibmFont) },
                        onClick = { expanded = false }
                    )
                } else {
                    fridaVersions.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text = item, fontFamily = ibmFont) },
                            onClick = { currentFridaVersionFromList = item; expanded = false }
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
                                    startFridaModule(activity, fridaVersions[0], fridaRunningState)
                                } else {
                                    startFridaModule(activity, it, fridaRunningState)
                                }
                            }
                        }
                    }
                },
                colors = when (fridaRunningState) {
                    "processing" -> ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFBDBDBD)
                    )

                    else -> ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                },
                shape = RoundedCornerShape(10.dp),
                modifier = modifier
                    .padding(start = 5.dp)
                    .width(140.dp)
            ) {
                Text(
                    text = when (fridaRunningState) {
                        "stop" -> "Stop Frida"
                        else -> "Start Frida"
                    },
                    fontSize = 11.sp,
                    color = when (fridaRunningState) {
                        "processing" -> Color(0xFFA10000)
                        else -> Color.Red
                    },
                    modifier = modifier.padding(1.dp, 3.dp),
                    fontFamily = ibmFont
                )
            }
        }
    }
}