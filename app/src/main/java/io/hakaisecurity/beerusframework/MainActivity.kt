package io.hakaisecurity.beerusframework

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import io.hakaisecurity.beerusframework.ui.theme.Globe

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                HomeScreen(modifier = Modifier)
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.curcuit),
            contentDescription = null,
            modifier = Modifier
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .size(width = 250.dp, height = 16.dp)
                .align(Alignment.TopStart)
                .zIndex(-1f)
        )

        Image(
            painter = painterResource(id = R.drawable.circuit_right),
            contentDescription = null,
            modifier = Modifier
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .align(Alignment.TopEnd)
                .padding(end = 8.dp)
                .zIndex(-1f)
        )

        Image(
            painter = painterResource(id = R.drawable.circuit_left),
            contentDescription = null,
            modifier = Modifier
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .align(Alignment.TopStart)
                .padding(top = 8.dp)
                .zIndex(-1f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.safeDrawing.asPaddingValues())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
            ) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 0.dp),
                    modifier = modifier.padding(top = 8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.menu),
                        contentDescription = "Menu Button",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .wrapContentSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.beerushome),
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 212.dp, height = 154.dp)
                        .align(Alignment.Center)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color.Gray)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BEERUS\nframework",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed vel lorem ligula. Proin faucibus dolor erat, a ultricies ligula molestie scelerisque.",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "If you really know, you can hack.\nBSDaemon",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true, name = "Home Screen")
@Composable
fun HomeScreenPreview() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        HomeScreen(modifier = Modifier)
    }
}