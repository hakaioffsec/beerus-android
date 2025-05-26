package io.hakaisecurity.beerusframework.composables

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.material3.AlertDialog
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.hakaisecurity.beerusframework.R
import io.hakaisecurity.beerusframework.core.functions.sandboxExfiltration.ApplicationInformation
import io.hakaisecurity.beerusframework.core.functions.sandboxExfiltration.SandboxExfiltration
import io.hakaisecurity.beerusframework.core.models.Application

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandboxScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val listHeight = screenHeight * 0.8f

    var applications by remember { mutableStateOf<List<Application>>(emptyList()) }
    var selectedApp by remember { mutableStateOf<Application?>(null) }
    var exfiltrationResult by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var server by remember { mutableStateOf("") }
    val sandboxExfiltration = remember { SandboxExfiltration() }
    var isloading by remember { mutableStateOf(false) }
    var isUSB by remember { mutableStateOf(false) }

    var showSend by remember { mutableStateOf(false) }
    var binarySend by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var regexIsValid by remember { mutableStateOf(true) }
    val addressRegex = Regex("^(https?://)?((localhost)|(\\d{1,3}\\.){3}\\d{1,3}|([\\dA-Za-z-]+\\.)+[A-Za-z]{2,})(:(?:[1-9]\\d{0,3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5]))?(\\/([\\w %&,.~\\-]+)?)*\\/?\$")



    fun getServer(): String {
        return if (server.startsWith("http") || server.startsWith("https")) {
            "$server"
        } else {
            "http://$server"
        }
    }

    LaunchedEffect(Unit) {
            applications = ApplicationInformation(context).fetchApplications("/data/data")
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

            Column() {
                Row {
                    Text(
                        text = "Data storage on",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Row {
                    Image(
                        painter = painterResource(id = R.drawable.returnarrow),
                        contentDescription = "Frida Logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "/data/data",
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
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
                    unfocusedTextColor = Color.White
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
                                showSend = true
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
            title = { Text(text = "Resultado da Exfiltração") },
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
                            )
                        }
                    }

                    Row {
                        app.name?.let {
                            Text(it,
                                color = Color.White,
                                fontSize = 20.sp

                            )
                        }
                    }

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

            Row {
                ToggleButtonTwoOptions("VPS", "USB", isUSB) {
                    isUSB = !isUSB
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row {
                OutlinedTextField(
                    value = if (!isUSB) server else "adb pull /data/local/tmp/{package_name}.tar.gz",
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
                modifier = Modifier.fillMaxWidth()
            ){
                Checkbox(
                    checked = binarySend,
                    onCheckedChange = { binarySend = it },
                    modifier = Modifier.width(20.dp),
                    colors = CheckboxColors(
                        checkedCheckmarkColor = Color.Gray,
                        uncheckedCheckmarkColor = Color.Gray,
                        checkedBoxColor = Color.Red,
                        uncheckedBoxColor = Color.Transparent,
                        disabledCheckedBoxColor = Color.Red,
                        disabledUncheckedBoxColor = Color.Red,
                        disabledIndeterminateBoxColor = Color.Red,
                        checkedBorderColor = Color.Red,
                        uncheckedBorderColor = Color.Red,
                        disabledBorderColor = Color.Red,
                        disabledUncheckedBorderColor = Color.Red,
                        disabledIndeterminateBorderColor = Color.Red
                    )
                )
                Text(
                    text = "Add app binary on upload",
                    color = Color.White,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Button(onClick = {
                    selectedApp?.let { app ->
                        if (isUSB || regexIsValid) {
                            isloading = true
                            sandboxExfiltration.exfiltrateFile(context, app, getServer(), binarySend, isUSB) { status ->
                                isloading = false
                                if (!isUSB) showSend = false
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
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

@Composable
fun ToggleButtonTwoOptions(
    option1: String = "Option A",
    option2: String = "Option B",
    selectedOption: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(if (selectedOption) Color.White else Color.Red)
                .clickable { onToggle(false) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = option1,
                color = if (selectedOption) Color.Black else Color.White
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .background(if (selectedOption) Color.Red else Color.White)
                .clickable { onToggle(true) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = option2,
                color = if (selectedOption) Color.White else Color.Black
            )
        }
    }
}
