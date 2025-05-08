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

val Trash: ImageVector
    get() {
        if (_Trash != null) {
            return _Trash!!
        }
        _Trash = ImageVector.Builder(
            name = "Trash",
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
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(5.5f, 5.5f)
                arcTo(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6f, 6f)
                verticalLineToRelative(6f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1f, 0f)
                verticalLineTo(6f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.5f, -0.5f)
                moveToRelative(2.5f, 0f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.5f, 0.5f)
                verticalLineToRelative(6f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1f, 0f)
                verticalLineTo(6f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.5f, -0.5f)
                moveToRelative(3f, 0.5f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1f, 0f)
                verticalLineToRelative(6f)
                arcToRelative(0.5f, 0.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(14.5f, 3f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1f, 1f)
                horizontalLineTo(13f)
                verticalLineToRelative(9f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, 2f)
                horizontalLineTo(5f)
                arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2f, -2f)
                verticalLineTo(4f)
                horizontalLineToRelative(-0.5f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1f, -1f)
                verticalLineTo(2f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1f, -1f)
                horizontalLineTo(6f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1f, -1f)
                horizontalLineToRelative(2f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1f, 1f)
                horizontalLineToRelative(3.5f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1f, 1f)
                close()
                moveTo(4.118f, 4f)
                lineTo(4f, 4.059f)
                verticalLineTo(13f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, 1f)
                horizontalLineToRelative(6f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1f, -1f)
                verticalLineTo(4.059f)
                lineTo(11.882f, 4f)
                close()
                moveTo(2.5f, 3f)
                horizontalLineToRelative(11f)
                verticalLineTo(2f)
                horizontalLineToRelative(-11f)
                close()
            }
        }.build()
        return _Trash!!
    }

private var _Trash: ImageVector? = null

val RefreshCcwDot: ImageVector
    get() {
        if (_RefreshCcwDot != null) {
            return _RefreshCcwDot!!
        }
        _RefreshCcwDot = ImageVector.Builder(
            name = "RefreshCcwDot",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                fillAlpha = 1.0f,
                stroke = SolidColor(Color(0xFF000000)),
                strokeAlpha = 1.0f,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(3f, 2f)
                verticalLineToRelative(6f)
                horizontalLineToRelative(6f)
            }
            path(
                fill = null,
                fillAlpha = 1.0f,
                stroke = SolidColor(Color(0xFF000000)),
                strokeAlpha = 1.0f,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(21f, 12f)
                arcTo(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, 5.3f)
                lineTo(3f, 8f)
            }
            path(
                fill = null,
                fillAlpha = 1.0f,
                stroke = SolidColor(Color(0xFF000000)),
                strokeAlpha = 1.0f,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(21f, 22f)
                verticalLineToRelative(-6f)
                horizontalLineToRelative(-6f)
            }
            path(
                fill = null,
                fillAlpha = 1.0f,
                stroke = SolidColor(Color(0xFF000000)),
                strokeAlpha = 1.0f,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(3f, 12f)
                arcToRelative(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15f, 6.7f)
                lineToRelative(3f, -2.7f)
            }
            path(
                fill = null,
                fillAlpha = 1.0f,
                stroke = SolidColor(Color(0xFF000000)),
                strokeAlpha = 1.0f,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(13f, 12f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 12f, 13f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11f, 12f)
                arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13f, 12f)
                close()
            }
        }.build()
        return _RefreshCcwDot!!
    }

private var _RefreshCcwDot: ImageVector? = null

val Upload: ImageVector
    get() {
        if (_Upload != null) {
            return _Upload!!
        }
        _Upload = ImageVector.Builder(
            name = "Upload",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(440f, 640f)
                verticalLineToRelative(-326f)
                lineTo(336f, 418f)
                lineToRelative(-56f, -58f)
                lineToRelative(200f, -200f)
                lineToRelative(200f, 200f)
                lineToRelative(-56f, 58f)
                lineToRelative(-104f, -104f)
                verticalLineToRelative(326f)
                close()
                moveTo(240f, 800f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(160f, 720f)
                verticalLineToRelative(-120f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(120f)
                horizontalLineToRelative(480f)
                verticalLineToRelative(-120f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(120f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(720f, 800f)
                close()
            }
        }.build()
        return _Upload!!
    }

private var _Upload: ImageVector? = null

val Arrow_back: ImageVector
    get() {
        if (_Arrow_back != null) {
            return _Arrow_back!!
        }
        _Arrow_back = ImageVector.Builder(
            name = "Arrow_back",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(313f, 520f)
                lineToRelative(224f, 224f)
                lineToRelative(-57f, 56f)
                lineToRelative(-320f, -320f)
                lineToRelative(320f, -320f)
                lineToRelative(57f, 56f)
                lineToRelative(-224f, 224f)
                horizontalLineToRelative(487f)
                verticalLineToRelative(80f)
                close()
            }
        }.build()
        return _Arrow_back!!
    }

private var _Arrow_back: ImageVector? = null

val Add: ImageVector
    get() {
        if (_add != null) {
            return _add!!
        }
        _add = ImageVector.Builder(
            name = "Add",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(440f, 520f)
                horizontalLineTo(200f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(240f)
                verticalLineToRelative(-240f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(240f)
                horizontalLineToRelative(240f)
                verticalLineToRelative(80f)
                horizontalLineTo(520f)
                verticalLineToRelative(240f)
                horizontalLineToRelative(-80f)
                close()
            }
        }.build()
        return _add!!
    }

private var _add: ImageVector? = null

val apps: ImageVector
    get() {
        if (_apps != null) {
            return _apps!!
        }
        _apps = ImageVector.Builder(
            name = "Apps",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(240f, 800f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(160f, 720f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(240f, 640f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(320f, 720f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(240f, 800f)
                moveToRelative(240f, 0f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(400f, 720f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(480f, 640f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(560f, 720f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(480f, 800f)
                moveToRelative(240f, 0f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(640f, 720f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(720f, 640f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(800f, 720f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(720f, 800f)
                moveTo(240f, 560f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(160f, 480f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(240f, 400f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(320f, 480f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(240f, 560f)
                moveToRelative(240f, 0f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(400f, 480f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(480f, 400f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(560f, 480f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(480f, 560f)
                moveToRelative(240f, 0f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(640f, 480f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(720f, 400f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(800f, 480f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(720f, 560f)
                moveTo(240f, 320f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(160f, 240f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(240f, 160f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(320f, 240f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(240f, 320f)
                moveToRelative(240f, 0f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(400f, 240f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(480f, 160f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(560f, 240f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(480f, 320f)
                moveToRelative(240f, 0f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(640f, 240f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(720f, 160f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(800f, 240f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(720f, 320f)
            }
        }.build()
        return _apps!!
    }

private var _apps: ImageVector? = null

val restart_alt: ImageVector
    get() {
        if (_restart_alt != null) {
            return _restart_alt!!
        }
        _restart_alt = ImageVector.Builder(
            name = "Restart_alt",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(440f, 838f)
                quadToRelative(-121f, -15f, -200.5f, -105.5f)
                reflectiveQuadTo(160f, 520f)
                quadToRelative(0f, -66f, 26f, -126.5f)
                reflectiveQuadTo(260f, 288f)
                lineToRelative(57f, 57f)
                quadToRelative(-38f, 34f, -57.5f, 79f)
                reflectiveQuadTo(240f, 520f)
                quadToRelative(0f, 88f, 56f, 155.5f)
                reflectiveQuadTo(440f, 758f)
                close()
                moveToRelative(80f, 0f)
                verticalLineToRelative(-80f)
                quadToRelative(87f, -16f, 143.5f, -83f)
                reflectiveQuadTo(720f, 520f)
                quadToRelative(0f, -100f, -70f, -170f)
                reflectiveQuadToRelative(-170f, -70f)
                horizontalLineToRelative(-3f)
                lineToRelative(44f, 44f)
                lineToRelative(-56f, 56f)
                lineToRelative(-140f, -140f)
                lineToRelative(140f, -140f)
                lineToRelative(56f, 56f)
                lineToRelative(-44f, 44f)
                horizontalLineToRelative(3f)
                quadToRelative(134f, 0f, 227f, 93f)
                reflectiveQuadToRelative(93f, 227f)
                quadToRelative(0f, 121f, -79.5f, 211.5f)
                reflectiveQuadTo(520f, 838f)
            }
        }.build()
        return _restart_alt!!
    }

private var _restart_alt: ImageVector? = null
