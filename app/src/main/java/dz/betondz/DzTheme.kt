package dz.betondz

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// DZ 2026 shared design tokens (OpenPencil pass: betondz).
// design.html -> openpencil import -> lint accessibility 0 errors.

private val DzPrimaryLight = Color(0xFF006A60)
private val DzOnPrimaryLight = Color(0xFFFFFFFF)
private val DzSurfaceLight = Color(0xFFFBFDF9)
private val DzSurfaceLowLight = Color(0xFFF2F4EF)
private val DzErrorLight = Color(0xFFBA1A1A)
private val DzOutlineVariantLight = Color(0xFFC1C9BF)

private val DzPrimaryDark = Color(0xFF4DDAD0)
private val DzOnPrimaryDark = Color(0xFF003731)
private val DzSurfaceDark = Color(0xFF0F1513)
private val DzSurfaceLowDark = Color(0xFF1A2120)
private val DzErrorDark = Color(0xFFFFB4AB)
private val DzOutlineVariantDark = Color(0xFF3F4B45)

private val DzLightScheme = lightColorScheme(
    primary = DzPrimaryLight,
    onPrimary = DzOnPrimaryLight,
    surface = DzSurfaceLight,
    surfaceContainerLow = DzSurfaceLowLight,
    error = DzErrorLight,
    outlineVariant = DzOutlineVariantLight
)

private val DzDarkScheme = darkColorScheme(
    primary = DzPrimaryDark,
    onPrimary = DzOnPrimaryDark,
    surface = DzSurfaceDark,
    surfaceContainerLow = DzSurfaceLowDark,
    error = DzErrorDark,
    outlineVariant = DzOutlineVariantDark
)

@Composable
fun DzTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DzDarkScheme
        else -> DzLightScheme
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

@Composable
fun DzEmptyState(title: String, hint: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(hint, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
