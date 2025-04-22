package io.hakaisecurity.beerusframework.composables

import androidx.compose.foundation.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.hakaisecurity.beerusframework.core.functions.sandboxExfiltration.ApplicationInformation
import io.hakaisecurity.beerusframework.core.functions.sandboxExfiltration.SandboxExfiltration
import io.hakaisecurity.beerusframework.core.models.Application

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
    val sandboxExfiltration = remember { SandboxExfiltration() }


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
                            .clickable { selectedApp = app },
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

    selectedApp?.let { app ->
        AlertDialog(
            onDismissRequest = { selectedApp = null },
            confirmButton = {
                Button(onClick = {
                    exfiltrationResult = sandboxExfiltration.exfiltrateFile(context, app)
                    selectedApp = null
                }) {
                    Text("Exfiltrar Arquivos")
                }
            },
            dismissButton = {
                TextButton (onClick = { selectedApp = null }) {
                    Text("Cancelar")
                }
            },
            title = { Text(text = "Exfiltrar ${app.name}?") },
            text = {
                Column {
                    Text(text = "Pacote: ${app.identifier}")
                    Text(text = "Caminho: ${app.artifactPath}")
                }
            }
        )
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
}
