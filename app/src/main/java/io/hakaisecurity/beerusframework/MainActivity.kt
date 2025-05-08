package io.hakaisecurity.beerusframework

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import io.hakaisecurity.beerusframework.core.functions.Start.Companion.detectMagisk
import io.hakaisecurity.beerusframework.core.functions.Start.Companion.detectMagiskModuleInstalled
import io.hakaisecurity.beerusframework.core.functions.frida.FridaSetup.Companion.getFridaVersions
import io.hakaisecurity.beerusframework.core.functions.frida.FridaSetup.Companion.readFridaCurrentVersion
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.currentFridaVersionFromList
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.fridaVersions
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.inEditorMode
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.updateFridaDownloadedVersion
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.updateanimationStartState
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.confirmMagiskModuleInstallerDialog
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.dismissMagiskModuleInstallerDialog
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.showMagiskModuleInstallerDialog
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.showsMagiskModuleInstallerDialog
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.updateHasMagisk
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.updateHasModule
import io.hakaisecurity.beerusframework.ui.theme.ibmFont

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

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

                LaunchedEffect(Unit) {
                    detectMagisk { isMagisk ->
                        if (isMagisk) {
                            updateHasMagisk(true)

                            detectMagiskModuleInstalled { isModuleInstalled ->
                                if (!isModuleInstalled) {
                                    showsMagiskModuleInstallerDialog()
                                }else{
                                    updateHasModule(true)
                                }
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
                }

                NavigationFunc(context = context, modifier = Modifier)

                if (showMagiskModuleInstallerDialog) {
                    MagikModuleInstallDialog(
                        onDismiss = { dismissMagiskModuleInstallerDialog() },
                        onConfirm = { confirmMagiskModuleInstallerDialog(context) }
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