package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val HonoursLightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = RoyalBlueContainer,
    onPrimaryContainer = NavyDeep,
    secondary = RoyalBlue,
    onSecondary = Color.White,
    secondaryContainer = RoyalBlueSoft,
    onSecondaryContainer = NavyDeep,
    tertiary = GoldAccent,
    onTertiary = Color.White,
    tertiaryContainer = GoldSoft,
    onTertiaryContainer = GoldDeep,
    background = IvoryBackground,
    onBackground = TextPrimaryLight,
    surface = SurfaceWhite,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    error = CrimsonError,
    onError = Color.White,
    errorContainer = CrimsonSoft,
    onErrorContainer = CrimsonError
)

private val HonoursDarkColorScheme = darkColorScheme(
    primary = RoyalBlueSoft,
    onPrimary = NavyDeep,
    primaryContainer = RoyalBlue,
    onPrimaryContainer = Color.White,
    secondary = GoldBright,
    onSecondary = NavyDeep,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = GoldSoft,
    tertiary = GoldBright,
    onTertiary = NavyDeep,
    tertiaryContainer = Color(0xFF3B2E08),
    onTertiaryContainer = GoldSoft,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkOutline,
    error = Color(0xFFF87171),
    onError = NavyDeep,
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = Color(0xFFFEE2E2)
)

val HonoursShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

enum class AppThemeMode(val labelBn: String) {
    SYSTEM("সিস্টেম ডিফল্ট"),
    LIGHT("লাইট মোড"),
    DARK("ডার্ক মোড")
}

@Composable
fun HonoursSolutionTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) HonoursDarkColorScheme else HonoursLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HonoursTypography,
        shapes = HonoursShapes,
        content = content
    )
}
