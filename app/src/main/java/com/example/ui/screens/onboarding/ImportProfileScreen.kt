package com.example.ui.screens.onboarding

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DEFAULT_IMPORTABLE_PROFILES
import com.example.data.model.ImportableProfile
import com.example.ui.components.HelpSheet
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.InstrumentSerifFontFamily
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageFontFamily
import com.example.ui.theme.PortagePrimary
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted

@Composable
fun ImportProfileScreen(
  onImportProfile: (ImportableProfile) -> Unit,
  onSkip: () -> Unit
) {
  val profiles = remember { DEFAULT_IMPORTABLE_PROFILES }
  var selectedProfileId by remember { mutableStateOf<String?>(profiles.firstOrNull()?.id) }
  var showHelpSheet by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current
  val scrollState = rememberScrollState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("import_profile_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)
    ) {
      // Scrollable content area taking all available space above buttons
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .verticalScroll(scrollState)
      ) {
        // Top header matching user screenshot: "Portage" centered
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Portage",
            color = PortageText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PortageFontFamily
          )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Heading: "Import your profile" in Instrument Serif
        Text(
          text = "Import your profile",
          color = PortageText,
          fontSize = 38.sp,
          fontWeight = FontWeight.Normal,
          fontFamily = InstrumentSerifFontFamily,
          lineHeight = 42.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Subtitle
        Text(
          text = "There are some existing profiles for other devices you can select any one to import their saved configurations.",
          color = PortageTextMuted,
          fontSize = 14.sp,
          fontFamily = GeistFontFamily,
          lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Existing Profile Cards
        Column(
          verticalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          profiles.forEach { profile ->
            val isSelected = profile.id == selectedProfileId

            ProfileCard(
              profile = profile,
              isSelected = isSelected,
              onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedProfileId = profile.id
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // Fixed Bottom Actions Section strictly pinned at the bottom
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp, bottom = 12.dp)
      ) {
        // Finish (Primary Blue Pill)
        Button(
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            val chosen = profiles.find { it.id == selectedProfileId } ?: profiles.firstOrNull()
            if (chosen != null) {
              Toast.makeText(context, "Importing ${chosen.name}…", Toast.LENGTH_SHORT).show()
              onImportProfile(chosen)
            } else {
              onSkip()
            }
          },
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(
            containerColor = PortagePrimary,
            contentColor = PortageText
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("import_finish_button")
        ) {
          Text(
            text = "Finish",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = GeistFontFamily
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Skip (Create a new profile) (Dark pill with subtle border)
        Button(
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onSkip()
          },
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1E1E1E),
            contentColor = PortageText
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.dp, PortageBorder, CircleShape)
            .testTag("import_skip_button")
        ) {
          Text(
            text = "Skip (Create a new profile)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = GeistFontFamily
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // "Stuck and need help?"
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Stuck and need help?",
            color = PortageTextMuted,
            fontSize = 13.sp,
            fontFamily = GeistFontFamily,
            modifier = Modifier
              .clickable { showHelpSheet = true }
              .padding(vertical = 4.dp, horizontal = 8.dp)
          )
        }
      }
    }

    if (showHelpSheet) {
      HelpSheet(onDismiss = { showHelpSheet = false })
    }
  }
}

@Composable
private fun ProfileCard(
  profile: ImportableProfile,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .background(PortageSurface2)
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = if (isSelected) PortagePrimary else Color(0xFF333333),
        shape = RoundedCornerShape(20.dp)
      )
      .clickable { onClick() }
      .padding(16.dp)
      .testTag("profile_card_${profile.id}")
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      // Thumbnail device visual matching screenshot
      DeviceGraphicThumbnail(type = profile.type)

      Spacer(modifier = Modifier.width(16.dp))

      // Metadata Column
      Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = "NAME",
          color = PortageTextMuted,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = GeistFontFamily,
          letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = profile.name,
          color = PortageText,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
          fontFamily = GeistFontFamily
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "DEVICE TYPE",
          color = PortageTextMuted,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = GeistFontFamily,
          letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = profile.type,
          color = PortageText,
          fontSize = 14.sp,
          fontWeight = FontWeight.Normal,
          fontFamily = GeistFontFamily
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "SAVED • ${profile.savedDate}",
          color = PortageTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          fontFamily = GeistFontFamily
        )
      }
    }
  }
}

/**
 * High-fidelity device graphic thumbnail matching the blue silk desktop wallpaper
 * on Laptop and Desktop screens shown in the user's mockup.
 */
@Composable
private fun DeviceGraphicThumbnail(type: String) {
  Box(
    modifier = Modifier
      .size(width = 116.dp, height = 84.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF141414))
      .border(1.dp, Color(0xFF2B2B2B), RoundedCornerShape(12.dp)),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      if (type.equals("Laptop", ignoreCase = true)) {
        // Laptop screen
        val screenW = w * 0.76f
        val screenH = h * 0.58f
        val screenLeft = (w - screenW) / 2f
        val screenTop = h * 0.12f

        // Screen bezel
        drawRoundRect(
          color = Color(0xFF222222),
          topLeft = Offset(screenLeft - 2f, screenTop - 2f),
          size = Size(screenW + 4f, screenH + 4f),
          cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // Screen wallpaper (Blue gradient)
        val screenBrush = Brush.linearGradient(
          colors = listOf(
            Color(0xFF68B5FF),
            Color(0xFF1B64DA),
            Color(0xFF092965)
          ),
          start = Offset(screenLeft, screenTop),
          end = Offset(screenLeft + screenW, screenTop + screenH)
        )
        drawRoundRect(
          brush = screenBrush,
          topLeft = Offset(screenLeft, screenTop),
          size = Size(screenW, screenH),
          cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )

        // Soft curved silk wave inside screen
        val wavePath = Path().apply {
          moveTo(screenLeft, screenTop + screenH * 0.7f)
          cubicTo(
            screenLeft + screenW * 0.4f, screenTop + screenH * 0.2f,
            screenLeft + screenW * 0.6f, screenTop + screenH * 0.9f,
            screenLeft + screenW, screenTop + screenH * 0.4f
          )
          lineTo(screenLeft + screenW, screenTop + screenH)
          lineTo(screenLeft, screenTop + screenH)
          close()
        }
        drawPath(
          path = wavePath,
          color = Color(0xFF86C9FF).copy(alpha = 0.5f)
        )

        // Laptop base / keyboard deck
        val baseW = w * 0.86f
        val baseH = 6.dp.toPx()
        val baseLeft = (w - baseW) / 2f
        val baseTop = screenTop + screenH + 1f

        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFC7CBD1), Color(0xFF6F747D))
          ),
          topLeft = Offset(baseLeft, baseTop),
          size = Size(baseW, baseH),
          cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )

        // Trackpad notch
        drawRoundRect(
          color = Color(0xFF4A4E54),
          topLeft = Offset(w / 2f - 12.dp.toPx() / 2f, baseTop),
          size = Size(12.dp.toPx(), 2.dp.toPx()),
          cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )
      } else {
        // Desktop Monitor
        val monitorW = w * 0.72f
        val monitorH = h * 0.55f
        val monitorLeft = (w - monitorW) / 2f
        val monitorTop = h * 0.10f

        // Bezel
        drawRoundRect(
          color = Color(0xFF222222),
          topLeft = Offset(monitorLeft - 2f, monitorTop - 2f),
          size = Size(monitorW + 4f, monitorH + 4f),
          cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // Screen wallpaper (Blue gradient)
        val screenBrush = Brush.linearGradient(
          colors = listOf(
            Color(0xFF75BCFF),
            Color(0xFF1D6BE5),
            Color(0xFF092965)
          ),
          start = Offset(monitorLeft, monitorTop),
          end = Offset(monitorLeft + monitorW, monitorTop + monitorH)
        )
        drawRoundRect(
          brush = screenBrush,
          topLeft = Offset(monitorLeft, monitorTop),
          size = Size(monitorW, monitorH),
          cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )

        // Wave accent
        val wavePath = Path().apply {
          moveTo(monitorLeft, monitorTop + monitorH * 0.6f)
          cubicTo(
            monitorLeft + monitorW * 0.45f, monitorTop + monitorH * 0.1f,
            monitorLeft + monitorW * 0.6f, monitorTop + monitorH * 0.85f,
            monitorLeft + monitorW, monitorTop + monitorH * 0.35f
          )
          lineTo(monitorLeft + monitorW, monitorTop + monitorH)
          lineTo(monitorLeft, monitorTop + monitorH)
          close()
        }
        drawPath(
          path = wavePath,
          color = Color(0xFF90D0FF).copy(alpha = 0.5f)
        )

        // Stand stem
        val stemW = 8.dp.toPx()
        val stemH = 10.dp.toPx()
        val stemLeft = (w - stemW) / 2f
        val stemTop = monitorTop + monitorH + 2f

        drawRect(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFB5BAC1), Color(0xFF6F747D))
          ),
          topLeft = Offset(stemLeft, stemTop),
          size = Size(stemW, stemH)
        )

        // Stand base
        val baseW = 28.dp.toPx()
        val baseH = 3.dp.toPx()
        val baseLeft = (w - baseW) / 2f
        val baseTop = stemTop + stemH

        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFD6DBE2), Color(0xFF7B818A))
          ),
          topLeft = Offset(baseLeft, baseTop),
          size = Size(baseW, baseH),
          cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
        )
      }
    }
  }
}
