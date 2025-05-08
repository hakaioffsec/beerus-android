package io.hakaisecurity.beerusframework.composables

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.hakaisecurity.beerusframework.R
import io.hakaisecurity.beerusframework.core.functions.adb.AdbOverNetwork.Companion.adbStart
import io.hakaisecurity.beerusframework.core.functions.adb.AdbOverNetwork.Companion.adbStop
import io.hakaisecurity.beerusframework.core.functions.adb.AdbOverNetwork.Companion.getIpAddr
import io.hakaisecurity.beerusframework.core.models.AdbState.Companion.adbRunningState
import io.hakaisecurity.beerusframework.ui.theme.ibmFont

@Composable
fun ADBScreen(modifier: Modifier, activity: Activity) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dec()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.adblogo),
            contentDescription = "ADB Logo",
            modifier = modifier.size((screenWidth / 2).dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Status: ",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = ibmFont
            )

            Text(
                text = if(adbRunningState) "Running" else "Stopped",
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = ibmFont
            )

            Canvas(modifier = modifier
                .size(25.dp)
                .padding(start = 5.dp)) {
                drawCircle(
                    color = if(adbRunningState) Color.Green else Color.Red
                )
            }
        }

        Spacer(
            modifier = modifier.height(1.dp)
        )

        if(adbRunningState) {
            Text(
                text = "Command:",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = ibmFont
            )

            Text(
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray)
                    .padding(4.dp),
                text = "adb connect " + getIpAddr().toString() + ":5555",
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = ibmFont
            )
        }

        Spacer(
            modifier = modifier.height(20.dp)
        )

        Row {
            Button(
                onClick = {
                    if(adbRunningState){
                        adbStop()
                    }else{
                        adbStart()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = modifier
                    .padding(start = 5.dp)
                    .width(280.dp)
            ) {
                Text(
                    text = if (!adbRunningState) "Start ADB" else "Stop ADB",
                    fontSize = 11.sp,
                    color = Color.Red,
                    modifier = modifier.padding(1.dp, 3.dp),
                    fontFamily = ibmFont
                )
            }
        }
    }
}