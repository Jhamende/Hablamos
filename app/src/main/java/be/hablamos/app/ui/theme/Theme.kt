package be.hablamos.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HablamosColors = lightColorScheme(
    primary = Color(0xFFE4572E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = Color(0xFF3A0A00),
    secondary = Color(0xFF2A9D8F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBDEDE7),
    background = Color(0xFFFFF8F1),
    surface = Color(0xFFFFFBF8),
    surfaceVariant = Color(0xFFF3E8DF),
    onSurface = Color(0xFF241A15)
)

@Composable
fun HablamosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HablamosColors,
        typography = Typography(),
        content = content
    )
}
