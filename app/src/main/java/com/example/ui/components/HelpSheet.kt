package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InstrumentSerifFontFamily
import com.example.ui.theme.PortageBorderSubtle
import com.example.ui.theme.PortageChip
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSheet(
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = PortageSurface2,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    dragHandle = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
      ) {
        SheetGrabHandle()
      }
    }
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      BottomSheetGlow(modifier = Modifier.align(Alignment.BottomCenter))

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp)
          .padding(bottom = 36.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Need help?",
            color = PortageText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = InstrumentSerifFontFamily,
            modifier = Modifier.weight(1f)
          )
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(PortageSurface3)
              .clickable { onDismiss() },
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

        HorizontalDivider(color = PortageBorderSubtle, thickness = 1.dp)

        Spacer(modifier = Modifier.height(20.dp))

        val checklist = listOf(
          "Use a data-capable USB cable.",
          "Try a different USB port on your machine.",
          "Make sure the Portage device is powered on.",
          "Unplug and plug it back in, then tap Connect."
        )

        checklist.forEachIndexed { index, item ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.Top
          ) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(PortageChip),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${index + 1}",
                color = PortageText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
              text = item,
              color = PortageText,
              fontSize = 14.sp,
              lineHeight = 20.sp,
              modifier = Modifier.weight(1f)
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PortageSecondaryButton(
          text = "Close",
          onClick = onDismiss,
          height = 50.dp,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}
