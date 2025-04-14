package io.hakaisecurity.beerusframework.composables

import androidx.compose.foundation.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.hakaisecurity.beerusframework.core.functions.sandboxExfiltration.Application
import io.hakaisecurity.beerusframework.core.functions.sandboxExfiltration.ApplicationInformation
import io.hakaisecurity.beerusframework.core.functions.sandboxExfiltration.SandboxExfiltration

@Composable
fun SandboxScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val listHeight = screenHeight * 0.6f

    var applications by remember { mutableStateOf<List<Application>>(emptyList()) }
    var selectedApp by remember { mutableStateOf<Application?>(null) }
    var exfiltrationResult by remember { mutableStateOf<String?>(null) }
    val ApplicationInformation = remember { ApplicationInformation(context) }
    val sandboxExfiltration = remember { SandboxExfiltration() }

    LaunchedEffect(Unit) {
            applications = ApplicationInformation.fetchApplications("/data/data")
    }

    Column(
        modifier = modifier.fillMaxSize().padding(vertical = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.DarkGray)
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(applications) { app ->
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
