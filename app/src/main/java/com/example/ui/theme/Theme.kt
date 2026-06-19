package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CapeSandGold,
    secondary = CapeSoftGoldGreen,
    tertiary = CapeOlive,
    background = CapeSlateDark,
    surface = CapeSlateGray,
    onPrimary = CapeSlateDark,
    onSecondary = CapeSlateDark,
    onBackground = CapeOffWhite,
    onSurface = CapeOffWhite
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CapeOceanGreen,
    secondary = CapeSandGold,
    tertiary = CapeOlive,
    background = CapeOffWhite,
    surface = androidx.compose.ui.graphics.Color.White,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = CapeSlateDark,
    onBackground = CapeSlateDark,
    onSurface = CapeSlateDark
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to ensure consistent brand identity visual representation
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
