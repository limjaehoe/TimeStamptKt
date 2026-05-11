package com.androidkotlin.timestampkt.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Indigo80,
    onPrimary = Color(0xFF08205F),
    primaryContainer = Color(0xFF253B88),
    onPrimaryContainer = Color(0xFFE7EBFF),
    secondary = BlueGrey80,
    secondaryContainer = Color(0xFF3B465D),
    onSecondaryContainer = Color(0xFFE2E7F5),
    tertiary = Rose80,
    background = Color(0xFF111318),
    onBackground = Color(0xFFE4E6EE),
    surface = Color(0xFF191B22),
    onSurface = Color(0xFFE4E6EE),
    surfaceVariant = Color(0xFF2F333D),
    onSurfaceVariant = Color(0xFFC7CAD4),
    outline = Color(0xFF8E93A1),
    error = Color(0xFFFFB4AB)
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE4FF),
    onPrimaryContainer = Color(0xFF0B1B4D),
    secondary = BlueGrey40,
    secondaryContainer = Color(0xFFDDE4F3),
    onSecondaryContainer = Color(0xFF141B2C),
    tertiary = Rose40,
    background = Color(0xFFFAFAFD),
    onBackground = Color(0xFF191B22),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191B22),
    surfaceVariant = Color(0xFFE8EAF2),
    onSurfaceVariant = Color(0xFF4A4E59),
    outline = Color(0xFF767B87),
    error = Color(0xFFBA1A1A)

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun TimeStampKtTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
