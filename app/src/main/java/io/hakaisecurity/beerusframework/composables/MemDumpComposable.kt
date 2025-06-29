package io.hakaisecurity.beerusframework.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.hakaisecurity.beerusframework.core.functions.memoryDump.MemoryDump.collectionTriagge
import io.hakaisecurity.beerusframework.core.functions.memoryDump.ProcessInformation
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.animationStart
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.updateanimationStartState
import io.hakaisecurity.beerusframework.core.models.Process
import io.hakaisecurity.beerusframework.ui.theme.iconWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemDumpScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val listHeight = screenHeight * 0.8f

    var applications by remember { mutableStateOf<List<Process>>(emptyList()) }
    var selectedApp by remember { mutableStateOf<Process?>(null) }
    var exfiltrationResult by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var server by remember { mutableStateOf("") }
    var isloading by remember { mutableStateOf(false) }
    var isUSB by remember { mutableStateOf(false) }

    var showSend by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var regexIsValid by remember { mutableStateOf(true) }
    val addressRegex = Regex("^(https?://)?((localhost)|(\\d{1,3}\\.){3}\\d{1,3}|([\\dA-Za-z-]+\\.)+[A-Za-z]{2,})(:(?:[1-9]\\d{0,3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5]))?(\\/([\\w %&,.~\\-]+)?)*\\/?\$")

    fun getServer(): String {
        return if (server.startsWith("http") || server.startsWith("https")) {
            server
        } else {
            "http://$server"
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            applications = ProcessInformation(context).fetchProcesses()
            kotlinx.coroutines.delay(2000)
        }
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

            Column {
                Row {
                    Text(
                        text = "App Processes",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                enabled = !animationStart,
                label = { Text("Search Processes", color = Color.White) },
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
                                if (!animationStart) {
                                    selectedApp = app
                                    showSend = true
                                } else {
                                    updateanimationStartState(false)
                                }
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

    exfiltrationResult?.let { result ->
        AlertDialog(
            onDismissRequest = { exfiltrationResult = null },
            confirmButton = {
                Button(onClick = { exfiltrationResult = null }) {
                    Text("OK")
                }
            },
            title = { Text(text = "Exfiltration Result") },
            text = { Text(text = result) }
        )
    }

    if (showSend) {
        ModalBottomSheet (
            onDismissRequest = { showSend = false },
            sheetState = sheetState,
            containerColor = Color.Black,
            contentColor = Color.Black
        ) {
            Column  (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
                    .background(color = Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                selectedApp?.let { app ->
                    Row {
                        app.icon?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(70.dp)
                                    .border(2.dp, Color.Red, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        app.name?.let {
                            Text(it,
                                color = Color.White,
                                fontSize = 20.sp

                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(1.dp))

                    Row {
                        app.identifier?.let {
                            Text(
                                it,
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.padding(horizontal = 50.dp)) {
                ToggleButtonTwoOptions("VPS", "USB", isUSB) {
                    if (!animationStart) {
                        isUSB = !isUSB
                    } else {
                        updateanimationStartState(false)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.padding(horizontal = 15.dp)) {
                OutlinedTextField(
                    value = if (!isUSB) server else "adb pull /data/local/tmp/{dump_file}.tar",
                    onValueChange = {
                        server = it
                        regexIsValid = addressRegex.matches(it)
                    },
                    isError = !regexIsValid,
                    supportingText = {
                        if (!regexIsValid) Text("Ex: 192.168.1.10:9032")
                    },
                    label = { Text(if (!isUSB) "VPS Host" else "Pc Command", color = if (!isUSB) Color.White else Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    singleLine = true,
                    enabled = !isUSB,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        errorTextColor = Color.White,
                        disabledBorderColor = Color.Gray,
                        disabledTextColor = Color.Gray,
                        disabledLabelColor = Color.Gray,
                        disabledPlaceholderColor = Color.Gray
                    )
                )
            }

            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp)
            ){
                Icon(
                    imageVector = iconWarning,
                    contentDescription = "Icon",
                    tint = Color.Yellow,
                    modifier = Modifier.size(35.dp)
                )

                Text(
                    text = "Warning: This can take sometime around 20 to 30 minutes... Please wait to stop loading",
                    color = Color.Yellow,
                    modifier = modifier.padding(start = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                if (!animationStart) {
                    selectedApp?.let { app ->
                        if (isUSB || regexIsValid) {
                            isloading = true
                            collectionTriagge(context, getServer(), isUSB, app.pid) { status ->
                                isloading = false
                                if (!isUSB) showSend = false
                            }
                        }
                    }
                } else {
                    updateanimationStartState(false)
                }
            },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().padding(start = 15.dp, top = 0.dp, bottom = 10.dp, end = 15.dp),
                enabled = !isloading,
                colors = ButtonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Red,
                    disabledContentColor = Color.White
                )
            ) {
                if (isloading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (!isUSB) "Send" else "Compact")
                }
            }
        }
    }
}
