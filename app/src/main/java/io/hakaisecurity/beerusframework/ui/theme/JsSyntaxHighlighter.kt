package io.hakaisecurity.beerusframework.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.AnnotatedString.Range
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping

public object JsSyntaxHighlighter : VisualTransformation {
    private val keywords = setOf(
        "const","let","var","function","class","import","export",
        "if","else","for","while","return","try","catch",
        "async","await","new","null","undefined","true","false"
    )
    private val pattern = Regex("([a-zA-Z_][a-zA-Z0-9_]*|\\d+|\"[^\"]*\"|'[^']*'|\\s+|[^\\s])")

    override fun filter(text: AnnotatedString): TransformedText {
        val spans = buildSpans(text.text)
        val colored = AnnotatedString(
            text = text.text,
            spanStyles = spans
        )
        return TransformedText(colored, OffsetMapping.Identity)
    }

    private fun buildSpans(code: String): List<Range<SpanStyle>> {
        val result = mutableListOf<Range<SpanStyle>>()
        pattern.findAll(code).forEach { m ->
            val token = m.value
            val style = when {
                token in keywords -> SpanStyle(color = Color(0xFFF51D00), fontWeight = FontWeight.Bold)
                token.startsWith("\"") || token.startsWith("'") -> SpanStyle(color = Color(0xFFF5B600))
                token.all { it.isDigit() } -> SpanStyle(color = Color(0xFFF54700))
                else -> SpanStyle(color = Color.White)
            }
            result += Range(style, m.range.first, m.range.last + 1)
        }
        return result
    }
}