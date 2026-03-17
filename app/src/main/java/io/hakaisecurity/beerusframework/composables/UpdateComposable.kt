package io.hakaisecurity.beerusframework.composables

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.hakaisecurity.beerusframework.core.functions.update.UpdateManager
import io.hakaisecurity.beerusframework.core.models.UpdateState
import io.hakaisecurity.beerusframework.ui.theme.ibmFont
import kotlinx.coroutines.launch

@Composable
fun UpdateScreen(modifier: Modifier, activity: Activity) {
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        if (UpdateState.currentVersion.isEmpty()) {
            UpdateState.updateCurrentVersion(UpdateManager.getCurrentVersion(activity))
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 35.dp, )
            .offset(x = (-5).dp, y = (-50).dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "App Update",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = ibmFont
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Current Version:",
                fontSize = 16.sp,
                color = Color.Gray,
                fontFamily = ibmFont
            )
            Text(
                text = UpdateState.currentVersion.ifEmpty { "..." },
                fontSize = 16.sp,
                color = Color.White,
                fontFamily = ibmFont
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Latest Version:",
                fontSize = 16.sp,
                color = Color.Gray,
                fontFamily = ibmFont
            )
            Text(
                text = when {
                    UpdateState.isChecking -> "Checking..."
                    UpdateState.latestVersion.isNotEmpty() -> UpdateState.latestVersion
                    else -> "Not checked"
                },
                fontSize = 16.sp,
                color = if (UpdateState.updateAvailable) Color(0xFF4CAF50) else Color.White,
                fontFamily = ibmFont
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Status:",
                fontSize = 16.sp,
                color = Color.Gray,
                fontFamily = ibmFont
            )
            Text(
                text = when {
                    UpdateState.isChecking -> "Checking for updates..."
                    UpdateState.isDownloading -> "Downloading..."
                    UpdateState.updateAvailable -> "Update available!"
                    UpdateState.latestVersion.isNotEmpty() -> "Up to date"
                    else -> "Unknown"
                },
                fontSize = 16.sp,
                color = when {
                    UpdateState.updateAvailable -> Color(0xFF4CAF50)
                    UpdateState.latestVersion.isNotEmpty() && !UpdateState.updateAvailable -> Color(0xFF2196F3)
                    else -> Color.White
                },
                fontFamily = ibmFont
            )
        }
        
        if (UpdateState.isDownloading) {
            Spacer(modifier = Modifier.height(24.dp))
            
            LinearProgressIndicator(
                progress = { UpdateState.downloadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color.Red,
                trackColor = Color.DarkGray,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${(UpdateState.downloadProgress * 100).toInt()}%",
                fontSize = 14.sp,
                color = Color.White,
                fontFamily = ibmFont
            )
        }
        

        UpdateState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                fontSize = 14.sp,
                color = Color(0xFFFF5252),
                fontFamily = ibmFont
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        UpdateManager.checkForUpdates(activity, showDialogIfAvailable = false)
                    }
                },
                enabled = !UpdateState.isChecking && !UpdateState.isDownloading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF333333),
                    disabledContainerColor = Color(0xFF222222)
                ),
                shape = RoundedCornerShape(8.dp),
//                modifier = Modifier.widthIn(max = 140.dp)
            ) {
                if (UpdateState.isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "Check Updates",
                    fontFamily = ibmFont,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            
            if (UpdateState.updateAvailable) {
                Button(
                    onClick = {
                        scope.launch {
                            UpdateManager.downloadAndInstall(activity)
                        }
                    },
                    enabled = !UpdateState.isDownloading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        disabledContainerColor = Color(0xFF662222)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.widthIn(min = 140.dp)
                ) {
                    if (UpdateState.isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (!UpdateState.isDownloading) "Update Now" else "Updating",
                        fontFamily = ibmFont,
                        color = Color.White,
                        maxLines = 1,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
