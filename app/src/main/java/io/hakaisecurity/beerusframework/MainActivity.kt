package io.hakaisecurity.beerusframework

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import io.hakaisecurity.beerusframework.core.functions.frida.FridaSetup
import io.hakaisecurity.beerusframework.core.functions.frida.FridaSetup.Companion.readFridaCurrentVersion
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.currentFridaVersionFromList
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.fridaVersions
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.updateFridaDownloadedVersion
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.updateanimationStartState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount >= 5) {
                                updateanimationStartState(true)
                            } else if (dragAmount < 0) {
                                updateanimationStartState(false)
                            }
                        }
                    },
                color = Color(0xFF1F1F22)
            ) {
                val context = LocalContext.current
                val activity = context as Activity

                LaunchedEffect(Unit) {
                    FridaSetup.getFridaVersions(
                        onNewVersion = { version ->
                            if (!fridaVersions.contains(version)) {
                                fridaVersions.add(version)
                            }
                        },
                        onLoadingComplete = {
                            currentFridaVersionFromList = readFridaCurrentVersion(activity)
                            updateFridaDownloadedVersion(readFridaCurrentVersion(activity))
                        }
                    )
                }

                NavigationFunc(context = context, modifier = Modifier)
            }
        }
    }
}