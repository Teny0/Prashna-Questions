package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = SaffronPrimary,
    secondary = AccentGold,
    tertiary = SaffronSecondary,
    background = DeepNavyBackground,
    surface = DeepNavyCard,
    onPrimary = Color.White,
    onSecondary = DeepNavyBackground,
    onBackground = TextSandLight,
    onSurface = TextSandLight,
    surfaceVariant = DeepNavyCard
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SaffronPrimary,
    secondary = DeepNavyBackground,
    tertiary = AccentGold,
    background = LightCreamBackground,
    surface = LightCreamCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextInkDark,
    onSurface = TextInkDark,
    surfaceVariant = LightCreamCard
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled to enforce our beautiful branded Modern Indian Minimalism theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
