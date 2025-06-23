package io.hakaisecurity.beerusframework.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.hakaisecurity.beerusframework.core.functions.Manifest.Manifest
import io.hakaisecurity.beerusframework.core.utils.ApplicationInformation
import io.hakaisecurity.beerusframework.core.models.Application
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.animationStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManifestScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val listHeight = screenHeight * 0.8f

    var applications by remember { mutableStateOf<List<Application>>(emptyList()) }
    var selectedApp by remember { mutableStateOf<Application?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var server by remember { mutableStateOf("") }

    var showInfos by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        applications = ApplicationInformation(context).fetchApplications()
    }

    val filteredApps = applications.filter {
        (it.name?.contains(searchQuery, ignoreCase = true) ?: false) ||
                it.identifier.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(listHeight)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFF151515))
                .padding(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                enabled = !animationStart,
                label = { Text("Buscar aplicativos", color = Color.White) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White,
                    disabledLabelColor = Color.Gray,
                    disabledBorderColor = Color.Gray
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredApps) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                selectedApp = app
                                showInfos = true
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        app.icon?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = app.name ?: "Unknown",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = app.identifier,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showInfos) {
        var manifestInfos = Manifest().getManifest(selectedApp!!.artifactPath)

        val screenHeightDp = LocalConfiguration.current.screenHeightDp // ← Int
        val targetHeight = (screenHeightDp * 8.0f) // ← Float.dp
        ModalBottomSheet (
            onDismissRequest = { showInfos = false },
            containerColor = Color.Black,
            contentColor = Color.Black,
            modifier = Modifier.fillMaxHeight(targetHeight)

        ) {


            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                selectedApp!!.icon?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .width(80.dp)
                            .height(80.dp)
                            .border(2.dp, Color.Red, CircleShape)
                    )
                }
            }


            Box(
                modifier = Modifier
                    .height(targetHeight.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (manifestInfos != null) {
                        AccordionMenu(title = "General") {
                            for (key in manifestInfos.General.keys) {
                                Row {
                                    Text(
                                        text = "$key: ",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${manifestInfos.General.get(key)}",
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        AccordionMenu(title = "Permissions") {
                            for (permission in manifestInfos.userPermissions) {
                                Text(
                                    text = "$permission",
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        AccordionMenu(title = "Activities") {
                            for (activity in manifestInfos.components.get("activities")!!) {
                                Column {
                                    Row {
                                        Text(
                                            text = "Activity: ",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${activity.name}",
                                            color = Color.White
                                        )
                                    }
                                    Row {
                                        Text(
                                            text = "Exported: ",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${activity.exported}",
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        AccordionMenu(title = "Providers") {
                            for (provider in manifestInfos.components.get("providers")!!) {
                                Column {
                                    Row {
                                        Text(
                                            text = "Provider: ",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${provider.name}",
                                            color = Color.White
                                        )
                                    }
                                    Row {
                                        Text(
                                            text = "Exported: ",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${provider.exported}",
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        AccordionMenu(title = "Services") {
                            for (Service in manifestInfos.components.get("services")!!) {
                                Column {
                                    Row {
                                        Text(
                                            text = "Service: ",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${Service.name}",
                                            color = Color.White
                                        )
                                    }
                                    Row {
                                        Text(
                                            text = "Exported: ",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${Service.exported}",
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        AccordionMenu(title = "Receivers") {
                            for (Receiver in manifestInfos.components.get("receivers")!!) {
                                Column {
                                    Row {
                                        Text(
                                            text = "Receiver: ",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${Receiver.name}",
                                            color = Color.White
                                        )
                                    }
                                    Row {
                                        Text(
                                            text = "Exported: ",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${Receiver.exported}",
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccordionMenu(
    title: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrowRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color(0xFF151515), shape = RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation),
                tint = Color.Gray
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}


