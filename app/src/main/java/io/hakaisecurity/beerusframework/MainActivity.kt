package io.hakaisecurity.beerusframework

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.hakaisecurity.beerusframework.core.functions.frida.FridaSetup
import io.hakaisecurity.beerusframework.core.functions.frida.FridaSetup.Companion.readFridaCurrentVersion
import io.hakaisecurity.beerusframework.core.functions.frida.FridaSetup.Companion.startFridaModule
import io.hakaisecurity.beerusframework.core.functions.magiskModuleManager.MagiskModule.Companion.deleteModule
import io.hakaisecurity.beerusframework.core.functions.magiskModuleManager.MagiskModule.Companion.getAllModules
import io.hakaisecurity.beerusframework.core.functions.magiskModuleManager.MagiskModule.Companion.startModuleManager
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.fridaRunningState
import io.hakaisecurity.beerusframework.core.models.MagiskManager.Companion.confirmDialog
import io.hakaisecurity.beerusframework.core.models.MagiskManager.Companion.dismissDialog
import io.hakaisecurity.beerusframework.core.models.MagiskManager.Companion.showMagiskDialog
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import io.hakaisecurity.beerusframework.ui.theme.Globe

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                HomeScreen(context = LocalContext.current, modifier = Modifier)
            }
        }
    }
}

@Composable
fun HomeScreen(context: Context, modifier: Modifier = Modifier) {
    val activity = context as Activity

    Column (
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row (verticalAlignment = Alignment.CenterVertically){
            Icon(imageVector = Globe,
                contentDescription = "Globe Icon",
                tint = Color.White,
                modifier = modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row (verticalAlignment = Alignment.CenterVertically){
            Text(
                text = "Hack the planet!",
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row (verticalAlignment = Alignment.CenterVertically){
            FridaModule(modifier, activity)
        }

        Row (verticalAlignment = Alignment.CenterVertically){
            MagiskManager(context)
        }
   }
}

@Composable
fun FridaModule(modifier: Modifier, activity: Activity) {
    val versions = remember { mutableStateListOf<String>() }
    val isLoading = remember { mutableStateOf(true) }
    var currentVersionFromList by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FridaSetup.getFridaVersions(
            onNewVersion = { version ->
                if (!versions.contains(version)) {
                    versions.add(version)
                }
            },
            onLoadingComplete = { isLoading.value = false; currentVersionFromList = readFridaCurrentVersion(activity) }
        )
    }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(58.dp, 0.dp, 0.dp, 0.dp)
        ) {
            Text(
                text = "frida version installed: ${readFridaCurrentVersion(activity)}",
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 12.sp,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { expanded = true }) {
                Text(text = "Select a version")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (versions.isEmpty() && isLoading.value) {
                    DropdownMenuItem(
                        text = { Text(text = "No versions available") }, onClick = { expanded = false }
                    )
                } else {
                    versions.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text = item) },
                            onClick = { currentVersionFromList = item; expanded = false}
                        )
                    }
                }
            }

            Button(
                onClick = {
                    currentVersionFromList?.let {
                        if(it == "None"){
                            startFridaModule(activity, versions[0], fridaRunningState)
                        }else{
                            startFridaModule(activity, it, fridaRunningState)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (fridaRunningState) {
                        "start" -> Color.Green
                        "stop" -> Color.Red
                        else -> Color.Magenta
                    }
                )
            ) {
                Text(
                    text = when (fridaRunningState) {
                        "start" -> "Run Frida"
                        "stop" -> "Stop Frida"
                        else -> "Downloading Frida"
                    },
                    color = Color.White
                )
            }
        }

        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun MagikRebootDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reboot?") },
        text = { Text("Beerus need to reboot to perform module actions") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Reboot")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Do After")
            }
        }
    )
}

@Composable
fun MagiskManager(context: Context){
    val modulePropsList = remember { mutableStateListOf<String>() }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {startModuleManager(context, it)}
    }

    LaunchedEffect(Unit) {
        getAllModules(modulePropsList)
    }

    if (showMagiskDialog) {
        MagikRebootDialog(
            onDismiss = { dismissDialog(); modulePropsList.clear(); getAllModules(modulePropsList) },
            onConfirm = { confirmDialog() }
        )
    }

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row (verticalAlignment = Alignment.CenterVertically){
            Button(onClick = { launcher.launch("application/zip") }) {
                Text("Select ZIP File")
            }
        }

        modulePropsList.forEach { modulePath ->
            var moduleName by remember { mutableStateOf("") }
            var moduleVersion by remember { mutableStateOf("") }
            var moduleAuthor by remember { mutableStateOf("") }
            var moduleDescription by remember { mutableStateOf("") }

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
                    .padding(8.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = moduleName, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$moduleVersion by $moduleAuthor", color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = moduleDescription, color = Color.White, fontStyle = FontStyle.Italic)
                    Button(onClick = { deleteModule(modulePath); modulePropsList.remove(modulePath) }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true, name = "Home Screen")
@Composable
fun HomeScreenPreview() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        HomeScreen(context = LocalContext.current, modifier = Modifier)
    }
}