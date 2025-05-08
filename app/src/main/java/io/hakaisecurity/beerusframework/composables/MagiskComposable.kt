package io.hakaisecurity.beerusframework.composables

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.hakaisecurity.beerusframework.R
import io.hakaisecurity.beerusframework.core.functions.magiskModuleManager.MagiskModule.Companion.getAllModules
import io.hakaisecurity.beerusframework.core.functions.magiskModuleManager.MagiskModule.Companion.getStatusModule
import io.hakaisecurity.beerusframework.core.functions.magiskModuleManager.MagiskModule.Companion.moduleOps
import io.hakaisecurity.beerusframework.core.functions.magiskModuleManager.MagiskModule.Companion.startModuleManager
import io.hakaisecurity.beerusframework.core.models.MagiskManager.Companion.confirmMagiskDialog
import io.hakaisecurity.beerusframework.core.models.MagiskManager.Companion.dismissMagiskDialog
import io.hakaisecurity.beerusframework.core.models.MagiskManager.Companion.showMagiskDialog
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import io.hakaisecurity.beerusframework.ui.theme.RefreshCcwDot
import io.hakaisecurity.beerusframework.ui.theme.Trash
import io.hakaisecurity.beerusframework.ui.theme.ibmFont

@Composable
fun MagiskScreen(modifier: Modifier, context: Context) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dec()
    val screenHeight = configuration.screenHeightDp.dec() * .15f

    val modulePropsList = remember { mutableStateListOf<String>() }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {startModuleManager(context, it)}
    }

    LaunchedEffect(Unit) {
        getAllModules(modulePropsList)
    }

    if (showMagiskDialog) {
        MagikRebootDialog(
            onDismiss = { dismissMagiskDialog(); modulePropsList.clear(); getAllModules(modulePropsList) },
            onConfirm = { confirmMagiskDialog() }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(25.dp, 0.dp)
    ) {
        Spacer(modifier = modifier.height(screenHeight.dp))

        Image(
            painter = painterResource(id = R.drawable.magisklogo),
            contentDescription = "Magisk Logo",
            modifier = modifier.size((screenWidth / 2).dp)
        )

        Spacer(
            modifier = modifier.height(20.dp)
        )

        Row (verticalAlignment = Alignment.CenterVertically){
            Button(onClick = { launcher.launch("application/zip") },
                colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = modifier.fillMaxWidth()
                ) {
                Text(
                    text = "Install Module",
                    fontSize = 11.sp,
                    color = Color.Red,
                    fontFamily = ibmFont
                )
            }
        }

        Spacer(
            modifier = modifier.height(10.dp)
        )

        Column(modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {
            modulePropsList.forEach { modulePath ->
                var moduleName by remember { mutableStateOf("") }
                var moduleVersion by remember { mutableStateOf("") }
                var moduleAuthor by remember { mutableStateOf("") }
                var moduleDescription by remember { mutableStateOf("") }

                var moduleStatus by remember { mutableStateOf(getStatusModule(modulePath, "disable")) }
                var deleteModuleStatus by remember { mutableStateOf(getStatusModule(modulePath, "remove")) }

                LaunchedEffect(modulePath) {
                    runSuCommand("cat $modulePath") { result ->
                        val lines = result.lines()
                        moduleName = lines.getOrNull(1)?.split("name=")?.get(1) ?: "Unknown Name"
                        moduleVersion = lines.getOrNull(2)?.split("version=")?.get(1) ?: "Unknown Version"
                        moduleAuthor = lines.getOrNull(4)?.split("author=")?.get(1) ?: "Unknown Author"
                        moduleDescription = lines.getOrNull(5)?.split("description=")?.get(1) ?: "No Description"
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 5.dp)
                        .background(Color(0xFF151515), shape = RoundedCornerShape(4.dp))
                        .border(width = 2.dp, color = if(!deleteModuleStatus) Color.White else Color(0xFF919191), shape = RoundedCornerShape(4.dp))
                ) {
                    Column {
                        Column (modifier.padding(16.dp, 8.dp)){
                            Row( modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Text(text = moduleName, textDecoration = if(!deleteModuleStatus) TextDecoration.None else TextDecoration.LineThrough , color = if(!deleteModuleStatus) Color.White else Color(0xFF919191), fontWeight = FontWeight.Bold, fontFamily = ibmFont, fontSize =  16.sp, modifier = Modifier.weight(1f))
                                ToggleButton(modifier = Modifier, status = moduleStatus, onToggle = {
                                    moduleStatus = !moduleStatus

                                    moduleOps(modulePath, moduleStatus, "disable")
                                })
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "$moduleVersion by $moduleAuthor", textDecoration = if(!deleteModuleStatus) TextDecoration.None else TextDecoration.LineThrough, color = if(!deleteModuleStatus) Color.White else Color(0xFF919191), fontStyle = FontStyle.Italic, fontFamily = ibmFont, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = moduleDescription, textDecoration = if(!deleteModuleStatus) TextDecoration.None else TextDecoration.LineThrough, color = if(!deleteModuleStatus) Color.White else Color(0xFF919191), fontWeight = FontWeight.SemiBold, fontFamily = ibmFont, fontSize =  12.sp)
                        }

                        Spacer(modifier = Modifier.height(2.dp).fillMaxWidth().background(if(!deleteModuleStatus) Color.White else Color(0xFF919191)))

                        Row(modifier = modifier.fillMaxWidth().padding(16.dp, 10.dp).clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            deleteModuleStatus = !deleteModuleStatus
                            moduleOps(modulePath, deleteModuleStatus, "remove")
                        },
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically){
                            Text(text = if(!deleteModuleStatus) "Remove" else "Restore", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = ibmFont, fontSize = 14.sp)
                            Icon(
                                imageVector = if(!deleteModuleStatus) Trash else RefreshCcwDot,
                                contentDescription = "Trash",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp).padding(start = 5.dp)
                            )

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleButton(
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
        targetValue = if (status) Color.White else Color(0xFFAB0100),
        label = "bgColor"
    )

    val horizontalOffset by animateDpAsState(
        targetValue = if (!status) (toggleWidth - dotSize - horizontalPadding) else horizontalPadding,
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

@Composable
fun MagikRebootDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reboot?", fontFamily = ibmFont) },
        text = { Text("Beerus need to reboot to perform module actions", fontFamily = ibmFont) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Reboot", fontFamily = ibmFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Do After", fontFamily = ibmFont)
            }
        }
    )
}