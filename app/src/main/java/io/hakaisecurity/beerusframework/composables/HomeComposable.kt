package io.hakaisecurity.beerusframework.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import io.hakaisecurity.beerusframework.R
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.animationStart
import io.hakaisecurity.beerusframework.ui.theme.ibmFont

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val screenHeight by remember { mutableStateOf(configuration.screenHeightDp.dp) }
    val columnHeight = screenHeight * 0.65f
    val imageWidth = (configuration.screenWidthDp * 0.9f).dp
    val imageHeight = (imageWidth * (250f / 200f))

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.beerus20),
            contentDescription = null,
            modifier = Modifier
                .size(width = imageWidth, height = imageHeight)
                .align(Alignment.TopCenter)
                .offset(y = (-imageHeight / 10f))
                .zIndex(0f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(columnHeight)
                .shadow(
                    8.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .background(Color(0xFF151515))
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BEERUS\nframework",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 32.sp,
                lineHeight = 34.sp,
                fontFamily = ibmFont,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Developed by the Hakai Offensive Security Research Team, your all-in-one toolkit for mobile penetration testing ≧◡≦",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                fontFamily = ibmFont,
                fontWeight = FontWeight.Normal
            )

            Image(
                painter = painterResource(id = R.drawable.homelittlesymbols),
                contentDescription = null,
                modifier = Modifier
                    .size(width = imageWidth * .5f, height = imageHeight * .1f)

            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Attack to Protect!",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                fontFamily = ibmFont,
                fontWeight = FontWeight.Normal
            )
        }
    }
}