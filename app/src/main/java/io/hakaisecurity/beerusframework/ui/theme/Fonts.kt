package io.hakaisecurity.beerusframework.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import io.hakaisecurity.beerusframework.R

val ibmFont = FontFamily(
    Font(R.font.ibmplexmono_regular, FontWeight.Normal),
    Font(R.font.ibmplexmono_medium, FontWeight.Medium),
    Font(R.font.ibmplexmono_bold, FontWeight.Bold),
    Font(R.font.ibmplexmono_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.ibmplexmono_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.ibmplexmono_bolditalic, FontWeight.Bold, FontStyle.Italic)
)