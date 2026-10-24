package com.example.ui.screens.workflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BottomSheetGlow
import com.example.ui.components.SheetGrabHandle
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageFontFamily
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted

data class ActionItem(
  val id: String,
  val name: String,
  val group: String,
  val icon: ImageVector,
  val actionType: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActionSheet(
  onSelectAction: (actionType: String, actionName: String) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var searchQuery by remember { mutableStateOf("") }

  val allActions = listOf(
    ActionItem("type_text", "Type text", "KEYBOARD", Icons.Default.Keyboard, "TYPE_TEXT"),
    ActionItem("press_key", "Press key", "KEYBOARD", Icons.Default.VpnKey, "KEY_SHORTCUT"),
    ActionItem("shortcut", "Shortcut", "KEYBOARD", Icons.Default.Widgets, "KEY_SHORTCUT"),
    ActionItem("click", "Click", "MOUSE", Icons.Default.NearMe, "CLICK"),
    ActionItem("move", "Move", "MOUSE", Icons.Default.Mouse, "MOVE"),
    ActionItem("scroll", "Scroll", "MOUSE", Icons.Default.UnfoldMore, "SCROLL"),
    ActionItem("wait", "Wait", "SYSTEM", Icons.Default.Schedule, "WAIT")
  )

  val filteredActions = allActions.filter {
    it.name.contains(searchQuery, ignoreCase = true) ||
      it.group.contains(searchQuery, ignoreCase = true)
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = PortageSurface2,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    dragHandle = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center
      ) {
        SheetGrabHandle()
      }
    }
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
    ) {
      BottomSheetGlow(modifier = Modifier.align(Alignment.BottomCenter))

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight()
          .verticalScroll(rememberScrollState())
          .navigationBarsPadding()
          .padding(horizontal = 22.dp)
          .padding(bottom = 16.dp)
      ) {
        // Sheet Header: Add action + XCircle
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Add action",
            color = PortageText,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
          )

          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(PortageSurface3)
              .clickable { onDismiss() }
              .testTag("close_add_action_sheet"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = PortageText,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Input (radius 16)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PortageSurface3)
            .border(1.dp, PortageBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = PortageTextMuted,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          BasicTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            textStyle = TextStyle(
              color = PortageText,
              fontSize = 14.sp,
              fontFamily = PortageFontFamily
            ),
            cursorBrush = SolidColor(PortageText),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("action_search_input"),
            decorationBox = { innerTextField ->
              if (searchQuery.isEmpty()) {
                Text(
                  text = "Search actions…",
                  color = PortageTextMuted,
                  fontSize = 14.sp
                )
              }
              innerTextField()
            }
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Grouped Items
        val groups = listOf("KEYBOARD", "MOUSE", "SYSTEM")

        groups.forEach { group ->
          val groupItems = filteredActions.filter { it.group == group }
          if (groupItems.isNotEmpty()) {
            Text(
              text = group,
              color = PortageTextMuted,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 1.sp,
              modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              groupItems.forEach { item ->
                ActionRowItem(
                  item = item,
                  onClick = {
                    onSelectAction(item.actionType, item.name)
                  }
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun ActionRowItem(
  item: ActionItem,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(46.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(PortageSurface3)
      .border(1.dp, PortageBorder, RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .padding(horizontal = 14.dp)
      .testTag("action_row_${item.id}"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = item.icon,
        contentDescription = null,
        tint = PortageText,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = item.name,
        color = PortageText,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
      )
    }

    Icon(
      imageVector = Icons.Default.KeyboardArrowDown,
      contentDescription = null,
      tint = PortageTextMuted,
      modifier = Modifier.size(18.dp)
    )
  }
}
