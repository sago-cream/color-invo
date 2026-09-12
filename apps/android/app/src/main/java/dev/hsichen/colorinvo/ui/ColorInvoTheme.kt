package dev.hsichen.colorinvo.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Primary = Color(0xFF007EA8)
val PrimarySoft = Color(0xFFEAF8FF)
val Background = Color.White
val Surface = Color.White
val Success = Primary
val Warning = Color(0xFFB46214)
val Hairline = Color(0xFFD8E6EE)
val Ink = Color(0xFF111827)
val Muted = Color(0xFF52606A)

object EditorMetrics {
    val pageInset = 24.dp
    val sectionGap = 24.dp
    val controlGap = 12.dp
    val smallGap = 8.dp
    val controlHeight = 48.dp
    val controlRadius = 8.dp
    val widgetRadius = 24.dp
    val previewHeight = 220.dp
    val previewInset = 16.dp
    val swatchSize = 28.dp
}

private val EditorTypography = Typography(
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodySmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
)

@Composable
fun ColorInvoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Primary, onPrimary = Color.White,
            secondary = Primary, onSecondary = Color.White,
            primaryContainer = PrimarySoft, onPrimaryContainer = Primary,
            secondaryContainer = PrimarySoft, onSecondaryContainer = Primary,
            background = Background, onBackground = Ink,
            surface = Surface, onSurface = Ink, surfaceTint = Primary,
            surfaceContainer = Surface, surfaceContainerLow = Surface, surfaceContainerLowest = Surface,
            surfaceContainerHigh = Surface, surfaceContainerHighest = PrimarySoft,
            surfaceVariant = PrimarySoft, onSurfaceVariant = Muted,
            outline = Hairline, outlineVariant = Hairline,
            error = Warning,
        ),
        typography = EditorTypography,
        content = content,
    )
}
