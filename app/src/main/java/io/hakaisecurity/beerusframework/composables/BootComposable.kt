package io.hakaisecurity.beerusframework.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.hakaisecurity.beerusframework.core.functions.bootOptions.bootFunctions.Companion.changeProperties
import io.hakaisecurity.beerusframework.core.functions.bootOptions.bootFunctions.Companion.getProperties
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import io.hakaisecurity.beerusframework.ui.theme.ibmFont

@Composable
fun BootScreen(modifier: Modifier) {
    var moduleCertsStatus by remember { mutableStateOf(false) }
    var moduleADBStatus by remember { mutableStateOf(false) }
    var moduleFridaStatus by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        getProperties { statusMap ->
            moduleCertsStatus = statusMap["systemTrustedCerts"] == "true"
            moduleADBStatus = statusMap["adbOverNetwork"] == "true"
            moduleFridaStatus = statusMap["frida"] == "true"
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(35.dp, 0.dp).fillMaxSize().verticalScroll(rememberScrollState())) {

        Text(
            text = "Reboot to apply",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            fontFamily = ibmFont
        )

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(0.dp, 5.dp)
                .background(Color(0xFF151515), shape = RoundedCornerShape(4.dp))
                .border(width = 2.dp, color = Color.White, shape = RoundedCornerShape(4.dp))
        ) {
            Column {
                Column (modifier.padding(16.dp, 8.dp)){
                    Row( modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(text = "Trusted System Certificates", textDecoration = TextDecoration.None, color =Color.White, fontWeight = FontWeight.Bold, fontFamily = ibmFont, fontSize =  16.sp, modifier = androidx.compose.ui.Modifier.weight(1f))
                        ToggleButton(modifier = Modifier, status = !moduleCertsStatus, onToggle = {
                            moduleCertsStatus = !moduleCertsStatus
                            changeProperties("systemTrustedCerts", moduleCertsStatus)
                        })
                    }
                }

                Column (modifier.padding(16.dp, 8.dp)){
                    Row( modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(text = "ADB Over Network", textDecoration = TextDecoration.None, color =Color.White, fontWeight = FontWeight.Bold, fontFamily = ibmFont, fontSize =  16.sp, modifier = androidx.compose.ui.Modifier.weight(1f))
                        ToggleButton(modifier = Modifier, status = !moduleADBStatus, onToggle = {
                            moduleADBStatus = !moduleADBStatus
                            changeProperties("adbOverNetwork", moduleADBStatus)
                        })
                    }
                }

                Column (modifier.padding(16.dp, 8.dp)){
                    Row( modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(text = "Frida", textDecoration = TextDecoration.None, color =Color.White, fontWeight = FontWeight.Bold, fontFamily = ibmFont, fontSize =  16.sp, modifier = androidx.compose.ui.Modifier.weight(1f))
                        ToggleButton(modifier = Modifier, status = !moduleFridaStatus, onToggle = {
                            moduleFridaStatus = !moduleFridaStatus
                            changeProperties("frida", moduleFridaStatus)
                        })
                    }
                }
            }
        }

        Button(
            onClick = { runSuCommand("reboot") {} },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Text(
                text = "Reboot",
                fontSize = 11.sp,
                color = Color.Red,
                modifier = modifier.padding(1.dp, 3.dp),
                fontFamily = ibmFont
            )
        }
    }
}