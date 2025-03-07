package io.hakaisecurity.beerusframework

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

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
    val configuration = LocalConfiguration.current
    val screenHeight by remember { mutableStateOf(configuration.screenHeightDp.dp) }
    val columnHeight = screenHeight * 0.65f
    val ibmFont = FontFamily(
        Font(R.font.ibmplexmono_regular, FontWeight.Normal),
        Font(R.font.ibmplexmono_medium, FontWeight.Medium),
        Font(R.font.ibmplexmono_bold, FontWeight.Bold),
        Font(R.font.ibmplexmono_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.ibmplexmono_mediumitalic, FontWeight.Medium, FontStyle.Italic),
        Font(R.font.ibmplexmono_bolditalic, FontWeight.Bold, FontStyle.Italic)
    )

    Box(
        modifier = modifier.fillMaxSize()
            .paint(
                painterResource(id = R.drawable.cyberpunklines_bg),
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(Color.Red)
            )
    ) {

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
                        .zIndex(2f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(columnHeight)
                    .shadow(8.dp,shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), ambientColor = Color.Black)
                    .background(Color(0xFF151515))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BEERUS\nframework",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp,
                    fontFamily = ibmFont,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed vel lorem ligula. Proin faucibus dolor erat, a ultricies ligula molestie scelerisque.",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontFamily = ibmFont,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "If you really know, you can hack.\nBSDaemon",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontFamily = ibmFont,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Home Screen")
@Composable
fun HomeScreenPreview() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        HomeScreen(modifier = Modifier)
    }
}