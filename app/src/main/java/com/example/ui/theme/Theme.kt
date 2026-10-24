package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PortageDarkColorScheme = darkColorScheme(
  primary = PortagePrimary,
  onPrimary = PortageText,
  primaryContainer = PortagePrimarySelectedBg,
  onPrimaryContainer = PortageText,
  secondary = PortageSurface1,
  onSecondary = PortageText,
  surface = PortageSurface2,
  onSurface = PortageText,
  surfaceVariant = PortageSurface3,
  onSurfaceVariant = PortageTextMuted,
  background = PortageBg,
  onBackground = PortageText,
  error = PortageDanger,
  onError = PortageText,
  outline = PortageBorder
)

@Composable
fun PortageTheme(
  content: @Composable () -> Unit
) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = PortageBg.toArgb()
        window.navigationBarColor = PortageBg.toArgb()
        WindowCompat.getInsetsController(window, view).apply {
          isAppearanceLightStatusBars = false
          isAppearanceLightNavigationBars = false
        }
      }
    }
  }

  MaterialTheme(
    colorScheme = PortageDarkColorScheme,
    typography = Typography,
    content = content
  )
}
