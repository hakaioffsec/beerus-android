package io.hakaisecurity.beerusframework

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import io.hakaisecurity.beerusframework.core.functions.Start.Companion.detectKernelSu
import io.hakaisecurity.beerusframework.core.functions.Start.Companion.detectMagisk
import io.hakaisecurity.beerusframework.core.functions.Start.Companion.detectRootModuleInstalled
import io.hakaisecurity.beerusframework.core.functions.frida.FridaSetup.Companion.getFridaVersions
import io.hakaisecurity.beerusframework.core.functions.frida.FridaSetup.Companion.readFridaCurrentVersion
import io.hakaisecurity.beerusframework.core.functions.rootModuleManager.RootModule.Companion.getAllModules
import io.hakaisecurity.beerusframework.core.functions.update.UpdateManager
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.currentFridaVersionFromList
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.fridaVersions
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.inEditorMode
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.updateFridaDownloadedVersion
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.updateanimationStartState
import io.hakaisecurity.beerusframework.core.models.RootModulesState
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.confirmRootModuleInstallerDialog
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.dismissRootModuleInstallerDialog
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.showRootModuleInstallerDialog
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.showsRootModuleInstallerDialog
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.updateHasModule
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.updateHasRoot
import io.hakaisecurity.beerusframework.core.models.UpdateState
import io.hakaisecurity.beerusframework.ui.theme.ibmFont
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount >= 5) {
                                if(!inEditorMode) {
                                    updateanimationStartState(true)
                                }
                            } else if (dragAmount < 0) {
                                updateanimationStartState(false)
                            }
                        }
                    },
                color = Color(0xFF1F1F22)
            ) {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                val mainHandler = Handler(Looper.getMainLooper())
                LaunchedEffect(Unit) {
                    detectMagisk { isMagisk ->
                        if (isMagisk) {
                            updateHasRoot(true)
                            getAllModules { paths ->
                                mainHandler.post { RootModulesState.setModulePaths(paths) }
                            }
                            detectRootModuleInstalled { isModuleInstalled ->
                                if (!isModuleInstalled) {
                                    showsRootModuleInstallerDialog()
                                }else{
                                    updateHasModule(true)
                                }
                            }
                        } else {
                            try {
                                detectKernelSu{ isKernelSu ->
                                    if (isKernelSu){
                                        updateHasRoot(true)
                                        getAllModules { paths ->
                                            mainHandler.post { RootModulesState.setModulePaths(paths) }
                                        }
                                        detectRootModuleInstalled { isModuleInstalled ->
                                            if (!isModuleInstalled) {
                                                showsRootModuleInstallerDialog()
                                            }else{
                                                updateHasModule(true)
                                            }
                                        }
                                    }
                                }
                            } catch (t: Throwable) {
                                throw t
                            }
                        }
                    }

                    getFridaVersions(
                        onNewVersion = { version ->
                            if (!fridaVersions.contains(version)) {
                                fridaVersions.add(version)
                            }
                        },
                        onLoadingComplete = {
                            currentFridaVersionFromList = readFridaCurrentVersion(context)
                            updateFridaDownloadedVersion(readFridaCurrentVersion(context))
                        }
                    )
                    
                    UpdateManager.checkForUpdates(context, showDialogIfAvailable = true)
                }

                NavigationFunc(context = context, modifier = Modifier)

                if (showRootModuleInstallerDialog) {
                    MagikModuleInstallDialog(
                        onDismiss = { dismissRootModuleInstallerDialog() },
                        onConfirm = { confirmRootModuleInstallerDialog(context) }
                    )
                }
                
                if (UpdateState.showUpdateDialog) {
                    UpdateAvailableDialog(
                        onDismiss = { UpdateState.dismissDialog() },
                        onConfirm = {
                            UpdateState.dismissDialog()
                            scope.launch {
                                UpdateManager.downloadAndInstall(context)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MagikModuleInstallDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Note") },
        text = { Text(text = "For the best experience with this framework, please consider installing our module.", fontSize = 18.sp) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Install", fontFamily = ibmFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Do After", fontFamily = ibmFont)
            }
        }
    )
}

@Composable
fun UpdateAvailableDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Available") },
        text = { 
            Text(
                text = "A new version (${UpdateState.latestVersion}) is available. Current version: ${UpdateState.currentVersion}. Would you like to update now?",
                fontSize = 16.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Update", fontFamily = ibmFont)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later", fontFamily = ibmFont)
            }
        }
    )
}