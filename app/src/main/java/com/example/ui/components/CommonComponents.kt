package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageBorderSubtle
import com.example.ui.theme.PortageChip
import com.example.ui.theme.PortageGrabHandle
import com.example.ui.theme.PortagePrimary
import com.example.ui.theme.PortagePrimaryGlow
import com.example.ui.theme.PortageSurface1
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted

@Composable
fun PortageTopBar(
  modifier: Modifier = Modifier,
  showBackButton: Boolean = true,
  onBackClick: () -> Unit = {},
  showNotificationButton: Boolean = true,
  hasUnreadNotifications: Boolean = false,
  onNotificationClick: () -> Unit = {}
) {
  val haptics = LocalHapticFeedback.current

  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(56.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Left slot (fixed 40dp width)
    if (showBackButton) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(PortageSurface2)
          .border(1.dp, PortageBorder, CircleShape)
          .clickable {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onBackClick()
          }
          .testTag("top_bar_back_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = PortageText,
          modifier = Modifier.size(18.dp)
        )
      }
    } else {
      Spacer(modifier = Modifier.size(40.dp))
    }

    // Centered Portage Brand Title
    Text(
      text = "Portage",
      color = PortageText,
      fontSize = 18.sp,
      fontWeight = FontWeight.SemiBold,
      fontFamily = com.example.ui.theme.OnestFontFamily,
      textAlign = TextAlign.Center
    )

    // Right slot (fixed 40dp width for rock-solid uniform positioning)
    if (showNotificationButton) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(PortageSurface2)
          .border(1.dp, PortageBorder, CircleShape)
          .clickable {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onNotificationClick()
          }
          .testTag("top_bar_notification_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Notifications,
          contentDescription = "Notifications",
          tint = PortageText,
          modifier = Modifier.size(18.dp)
        )
        if (hasUnreadNotifications) {
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(top = 8.dp, end = 8.dp)
              .size(7.dp)
              .clip(CircleShape)
              .background(PortagePrimary)
          )
        }
      }
    } else {
      Spacer(modifier = Modifier.size(40.dp))
    }
  }
}

@Composable
fun ScreenHeader(
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = title,
      color = PortageText,
      fontSize = 38.sp,
      fontWeight = FontWeight.Normal,
      fontFamily = com.example.ui.theme.InstrumentSerifFontFamily,
      letterSpacing = (-0.5).sp,
      lineHeight = 42.sp
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = subtitle,
      color = PortageTextMuted,
      fontSize = 14.sp,
      fontFamily = com.example.ui.theme.GeistFontFamily,
      lineHeight = 19.sp
    )
  }
}

@Composable
fun PortagePrimaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  height: Dp = 60.dp,
  enabled: Boolean = true,
  icon: ImageVector? = null,
  testTag: String = "primary_button"
) {
  val haptics = LocalHapticFeedback.current

  Button(
    onClick = {
      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
      onClick()
    },
    enabled = enabled,
    modifier = modifier
      .height(height)
      .testTag(testTag),
    shape = CircleShape,
    colors = ButtonDefaults.buttonColors(
      containerColor = PortagePrimary,
      contentColor = PortageText,
      disabledContainerColor = PortagePrimary.copy(alpha = 0.5f),
      disabledContentColor = PortageText.copy(alpha = 0.5f)
    ),
    contentPadding = PaddingValues(horizontal = 24.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = PortageText
      )
      if (icon != null) {
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = PortageText,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
fun PortageSecondaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  height: Dp = 60.dp,
  testTag: String = "secondary_button"
) {
  val haptics = LocalHapticFeedback.current

  Button(
    onClick = {
      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
      onClick()
    },
    modifier = modifier
      .height(height)
      .border(1.dp, PortageBorderSubtle, CircleShape)
      .testTag(testTag),
    shape = CircleShape,
    colors = ButtonDefaults.buttonColors(
      containerColor = PortageSurface1,
      contentColor = PortageText
    ),
    contentPadding = PaddingValues(horizontal = 24.dp)
  ) {
    Text(
      text = text,
      fontSize = 15.sp,
      fontWeight = FontWeight.SemiBold,
      color = PortageText
    )
  }
}

@Composable
fun PortageChip(
  text: String,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(CircleShape)
      .background(PortageChip)
      .padding(horizontal = 10.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = PortageText,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}

/**
 * Tactile physical keycap badge matching user's spec:
 * bg #4B4B4B, rounded 4dp, border white 10%, 2px subtle bottom shadow,
 * 11sp semibold text with 0.22sp tracking.
 */
@Composable
fun TactileKeyBadge(
  text: String,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(4.dp))
      .background(Color(0xFF4B4B4B))
      .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
      .padding(horizontal = 10.dp, vertical = 2.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = Color.White,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = 0.22.sp,
      fontFamily = com.example.ui.theme.GeistFontFamily
    )
  }
}

@Composable
fun EmptyStateCard(
  message: String,
  buttonText: String,
  onButtonClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "empty_state_action"
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, PortageBorder, RoundedCornerShape(28.dp))
      .padding(horizontal = 24.dp, vertical = 40.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = message,
        color = PortageTextMuted,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(0.85f)
      )
      Spacer(modifier = Modifier.height(24.dp))
      Button(
        onClick = onButtonClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = PortagePrimary,
          contentColor = PortageText
        ),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
        modifier = Modifier.testTag(testTag)
      ) {
        Text(
          text = buttonText,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}

@Composable
fun HelpLink(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) { onClick() }
      .padding(vertical = 12.dp)
      .testTag("help_link"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "Stuck and need help?",
      color = PortageTextMuted,
      fontSize = 14.sp,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
fun SelectDropdownField(
  label: String,
  value: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "select_dropdown"
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = label,
      color = PortageTextMuted,
      fontSize = 12.sp,
      modifier = Modifier.padding(bottom = 8.dp)
    )
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(PortageSurface3)
        .border(1.dp, PortageBorder, RoundedCornerShape(16.dp))
        .clickable { onClick() }
        .padding(horizontal = 16.dp)
        .testTag(testTag),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = value,
        color = PortageText,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold
      )
      Icon(
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = "Dropdown",
        tint = PortageText,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

@Composable
fun BottomSheetGlow(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(100.dp)
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color.Transparent,
            PortagePrimaryGlow.copy(alpha = 0.35f)
          )
        )
      )
  )
}

@Composable
fun SheetGrabHandle(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .width(40.dp)
      .height(5.dp)
      .clip(CircleShape)
      .background(PortageGrabHandle)
  )
}
