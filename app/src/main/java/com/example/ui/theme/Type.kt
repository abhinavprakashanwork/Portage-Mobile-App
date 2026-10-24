package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// 1. Onest font: for "Portage" branding
val OnestFontFamily = FontFamily(
  Font(R.font.onest, FontWeight.Normal),
  Font(R.font.onest, FontWeight.Medium),
  Font(R.font.onest, FontWeight.SemiBold),
  Font(R.font.onest, FontWeight.Bold)
)

// 2. Instrument Serif font: for main headings and titles
val InstrumentSerifFontFamily = FontFamily(
  Font(R.font.instrument_serif, FontWeight.Normal),
  Font(R.font.instrument_serif, FontWeight.Medium),
  Font(R.font.instrument_serif, FontWeight.SemiBold),
  Font(R.font.instrument_serif, FontWeight.Bold)
)

// 3. Geist font: for body text and everything else
val GeistFontFamily = FontFamily(
  Font(R.font.geist, FontWeight.Normal),
  Font(R.font.geist, FontWeight.Medium),
  Font(R.font.geist, FontWeight.SemiBold),
  Font(R.font.geist, FontWeight.Bold)
)

val HeadingFontFamily = InstrumentSerifFontFamily
val BodyFontFamily = GeistFontFamily
val PortageFontFamily = OnestFontFamily

val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = InstrumentSerifFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 48.sp,
    letterSpacing = (-0.5).sp,
    color = PortageText
  ),
  headlineLarge = TextStyle(
    fontFamily = InstrumentSerifFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 36.sp,
    letterSpacing = (-0.5).sp,
    color = PortageText
  ),
  headlineMedium = TextStyle(
    fontFamily = InstrumentSerifFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    letterSpacing = (-0.3).sp,
    color = PortageText
  ),
  titleLarge = TextStyle(
    fontFamily = InstrumentSerifFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    letterSpacing = (-0.2).sp,
    color = PortageText
  ),
  titleMedium = TextStyle(
    fontFamily = GeistFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    color = PortageText
  ),
  titleSmall = TextStyle(
    fontFamily = GeistFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    color = PortageText
  ),
  bodyLarge = TextStyle(
    fontFamily = GeistFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    color = PortageText
  ),
  bodyMedium = TextStyle(
    fontFamily = GeistFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 19.sp,
    color = PortageTextMuted
  ),
  bodySmall = TextStyle(
    fontFamily = GeistFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    color = PortageTextMuted
  ),
  labelLarge = TextStyle(
    fontFamily = GeistFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
    color = PortageText
  ),
  labelMedium = TextStyle(
    fontFamily = GeistFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    letterSpacing = 0.5.sp,
    color = PortageTextMuted
  ),
  labelSmall = TextStyle(
    fontFamily = GeistFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    letterSpacing = 0.8.sp,
    color = PortageTextMuted
  )
)
