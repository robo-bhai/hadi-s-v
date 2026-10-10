package com.hadiscare.app.ui.theme

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

private val BabyLoveDarkColors = darkColorScheme(
    primary = BabyLoveBlush,
    onPrimary = BabyLoveDarkBg,
    primaryContainer = BabyLoveDeep,
    onPrimaryContainer = Color.White,
    secondary = BabyLovePeach,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF532435),
    onSecondaryContainer = BabyLoveMilk,
    tertiary = BabyLoveLullabyGold,
    onTertiary = Color.Black,
    background = BabyLoveDarkBg,
    onBackground = BabyLoveDarkText,
    surface = BabyLoveDarkBg,
    onSurface = BabyLoveDarkText,
    surfaceVariant = BabyLoveDarkCard,
    onSurfaceVariant = BabyLoveDarkSubtext,
    outline = Color(0xFF6B3A4D),
    error = BabyLoveHeart,
    errorContainer = Color(0xFF5B1028),
    onError = Color.White
)

private val BabyLoveLightColors = lightColorScheme(
    primary = BabyLovePrimary,
    onPrimary = Color.White,
    primaryContainer = BabyLoveMilk,
    onPrimaryContainer = BabyLoveDeep,
    secondary = BabyLoveRose,
    onSecondary = Color.White,
    secondaryContainer = BabyLoveLightCardTonal,
    onSecondaryContainer = BabyLoveDeep,
    tertiary = BabyLoveLullabyGold,
    onTertiary = Color.Black,
    background = BabyLoveLightBg,
    onBackground = BabyLoveLightText,
    surface = BabyLoveLightBg,
    onSurface = BabyLoveLightText,
    surfaceVariant = BabyLoveLightCard,
    onSurfaceVariant = BabyLoveLightSubtext,
    outline = BabyLoveBorder,
    error = BabyLoveHeart,
    errorContainer = BabyLoveMilk,
    onError = Color.White
)

@Composable
fun HadisCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve handcrafted signature "Baby Love" aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> BabyLoveDarkColors
        else -> BabyLoveLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
