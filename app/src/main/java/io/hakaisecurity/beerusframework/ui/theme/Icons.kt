package io.hakaisecurity.beerusframework.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Home: ImageVector
    get() {
        if (_Home != null) {
            return _Home!!
        }
        _Home = ImageVector.Builder(
            name = "Home",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(8.36f, 1.37f)
                lineToRelative(6.36f, 5.8f)
                lineToRelative(-0.71f, 0.71f)
                lineTo(13f, 6.964f)
                verticalLineToRelative(6.526f)
                lineToRelative(-0.5f, 0.5f)
                horizontalLineToRelative(-3f)
                lineToRelative(-0.5f, -0.5f)
                verticalLineToRelative(-3.5f)
                horizontalLineTo(7f)
                verticalLineToRelative(3.5f)
                lineToRelative(-0.5f, 0.5f)
                horizontalLineToRelative(-3f)
                lineToRelative(-0.5f, -0.5f)
                verticalLineTo(6.972f)
                lineTo(2f, 7.88f)
                lineToRelative(-0.71f, -0.71f)
                lineToRelative(6.35f, -5.8f)
                horizontalLineToRelative(0.72f)
                close()
                moveTo(4f, 6.063f)
                verticalLineToRelative(6.927f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(-3.5f)
                lineToRelative(0.5f, -0.5f)
                horizontalLineToRelative(3f)
                lineToRelative(0.5f, 0.5f)
                verticalLineToRelative(3.5f)
                horizontalLineToRelative(2f)
                verticalLineTo(6.057f)
                lineTo(8f, 2.43f)
                lineTo(4f, 6.063f)
                close()
            }
        }.build()
        return _Home!!
    }

private var _Home: ImageVector? = null