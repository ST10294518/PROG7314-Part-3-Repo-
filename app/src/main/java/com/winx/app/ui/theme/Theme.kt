package com.winx.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val WinxLightColorScheme = lightColorScheme(
    primary = WinxBlue,
    onPrimary = WinxWhite,

    secondary = WinxOrange,
    onSecondary = WinxBlack,

    tertiary = WinxPurple,
    onTertiary = WinxBlack,

    background = WinxPink,
    onBackground = WinxBlack,

    surface = WinxWhite,
    onSurface = WinxBlack
)

private val WinxDarkColorScheme = darkColorScheme(
    primary = WinxBlue,
    onPrimary = WinxWhite,

    secondary = WinxOrange,
    onSecondary = WinxBlack,

    tertiary = WinxPurple,
    onTertiary = WinxWhite,

    background = WinxDarkBlue,
    onBackground = WinxWhite,

    surface = WinxDarkBlue,
    onSurface = WinxWhite
)

@Composable
fun WinxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current

            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> WinxDarkColorScheme
        else -> WinxLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}