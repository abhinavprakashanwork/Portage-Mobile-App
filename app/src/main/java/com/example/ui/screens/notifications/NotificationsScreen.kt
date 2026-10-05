package com.example.ui.screens.notifications

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventNotificationEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PortageTopBar
import com.example.ui.theme.GeistFontFamily
import com.example.ui.theme.InstrumentSerifFontFamily
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortagePrimary
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
  notifications: List<EventNotificationEntity>,
  onBackClick: () -> Unit,
  onClearAll: () -> Unit,
  onMarkAllRead: () -> Unit,
  onDismissNotification: (String) -> Unit = {}
) {
  val haptics = LocalHapticFeedback.current
  var selectedFilter by remember { mutableStateOf("All") }

  LaunchedEffect(Unit) {
    onMarkAllRead()
  }

  val filters = listOf("All", "Workflows", "Transmission", "Remaps", "Device")

  val filteredList = remember(notifications, selectedFilter) {
    when (selectedFilter) {
      "Workflows" -> notifications.filter { it.type.equals("WORKFLOW", ignoreCase = true) }
      "Transmission" -> notifications.filter { it.type.equals("TRANSMISSION", ignoreCase = true) }
      "Remaps" -> notifications.filter { it.type.equals("REMAP", ignoreCase = true) }
      "Device" -> notifications.filter { it.type.equals("DEVICE", ignoreCase = true) }
      else -> notifications
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("notifications_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp)
    ) {
      // Top bar with circular back button on left, centered Portage
      PortageTopBar(
        showBackButton = true,
        showNotificationButton = false,
        onBackClick = onBackClick
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Title & Subtitle matching image attachment
      Text(
        text = "Notifications",
        color = PortageText,
        fontSize = 36.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = InstrumentSerifFontFamily,
        letterSpacing = (-0.5).sp
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "History of all past events and actions",
        color = PortageTextMuted,
        fontSize = 14.sp,
        fontFamily = GeistFontFamily
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Filter chips row matching image attachment
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(filters) { filter ->
          val isSelected = selectedFilter == filter
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(if (isSelected) PortagePrimary else PortageSurface2)
              .border(
                width = 1.dp,
                color = if (isSelected) PortagePrimary else PortageBorder,
                shape = RoundedCornerShape(20.dp)
              )
              .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedFilter = filter
              }
              .padding(horizontal = 16.dp, vertical = 8.dp)
          ) {
            Text(
              text = filter,
              color = PortageText,
              fontSize = 13.sp,
              fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
              fontFamily = GeistFontFamily
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Notification Cards List
      if (filteredList.isEmpty()) {
        EmptyStateCard(
          message = "No notifications yet. Past events like workflow runs, transmissions, and device changes will appear here.",
          buttonText = "Back to Home",
          onButtonClick = onBackClick,
          modifier = Modifier.weight(1f),
          testTag = "empty_notifications_card"
        )
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredList, key = { it.id }) { item ->
            val currentItem by rememberUpdatedState(item)
            val dismissState = rememberSwipeToDismissBoxState(
              confirmValueChange = { dismissValue ->
                if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                  haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                  onDismissNotification(currentItem.id)
                  true
                } else {
                  false
                }
              }
            )

            SwipeToDismissBox(
              state = dismissState,
              modifier = Modifier
                .fillMaxWidth()
                .animateItem(),
              enableDismissFromStartToEnd = true,
              enableDismissFromEndToStart = true,
              backgroundContent = {
                val direction = dismissState.dismissDirection
                val isSwiping = direction == SwipeToDismissBoxValue.StartToEnd || direction == SwipeToDismissBoxValue.EndToStart
                val isSwipingRight = direction == SwipeToDismissBoxValue.StartToEnd

                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSwiping) Color(0xFF991B1B) else Color.Transparent)
                    .padding(horizontal = 20.dp),
                  contentAlignment = if (isSwipingRight) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                  if (isSwiping) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      if (!isSwipingRight) {
                        Text(
                          text = "Clear",
                          color = Color.White,
                          fontSize = 13.sp,
                          fontWeight = FontWeight.SemiBold,
                          fontFamily = GeistFontFamily
                        )
                      }
                      Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Notification",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                      )
                      if (isSwipingRight) {
                        Text(
                          text = "Clear",
                          color = Color.White,
                          fontSize = 13.sp,
                          fontWeight = FontWeight.SemiBold,
                          fontFamily = GeistFontFamily
                        )
                      }
                    }
                  }
                }
              }
            ) {
              NotificationCardItem(item = item)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      val canClear = notifications.isNotEmpty()

      // Bottom Sticky "Clear All" Red Pill Button (deactivated when all notifications are cleared)
      Button(
        onClick = {
          if (canClear) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClearAll()
          }
        },
        enabled = canClear,
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("clear_all_notifications_button"),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF991B1B), // Deep crimson red matching image
          contentColor = PortageText,
          disabledContainerColor = Color(0xFF261818), // Deactivated subtle dark crimson
          disabledContentColor = PortageTextMuted.copy(alpha = 0.45f)
        )
      ) {
        Text(
          text = "Clear All",
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
          fontFamily = GeistFontFamily
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun NotificationCardItem(item: EventNotificationEntity) {
  val (icon, iconTint) = when (item.type.uppercase()) {
    "WORKFLOW" -> Icons.Default.PlayArrow to PortagePrimary
    "TRANSMISSION" -> Icons.AutoMirrored.Filled.Send to Color(0xFF38BDF8)
    "REMAP" -> Icons.Default.SwapHoriz to Color(0xFFA855F7)
    "DEVICE" -> Icons.Default.Computer to Color(0xFF34D399)
    else -> Icons.Default.Widgets to PortageTextMuted
  }

  val formattedTime = remember(item.timestamp) {
    try {
      SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.timestamp))
    } catch (_: Exception) {
      ""
    }
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .background(PortageSurface2)
      .border(1.dp, PortageBorder, RoundedCornerShape(18.dp))
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(PortageSurface3),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon as ImageVector,
        contentDescription = item.type,
        tint = iconTint,
        modifier = Modifier.size(20.dp)
      )
    }

    Spacer(modifier = Modifier.width(14.dp))

    Column(modifier = Modifier.weight(1f)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = item.title,
          color = PortageText,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          fontFamily = GeistFontFamily
        )
        Text(
          text = formattedTime,
          color = PortageTextMuted,
          fontSize = 11.sp,
          fontFamily = GeistFontFamily
        )
      }
      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = item.description,
        color = PortageTextMuted,
        fontSize = 12.sp,
        fontFamily = GeistFontFamily
      )
    }
  }
}
