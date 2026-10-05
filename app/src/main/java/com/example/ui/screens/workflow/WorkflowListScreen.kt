package com.example.ui.screens.workflow

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkflowEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PortagePrimaryButton
import com.example.ui.components.PortageTopBar
import com.example.ui.components.ScreenHeader
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted
import kotlinx.coroutines.launch

@Composable
fun WorkflowListScreen(
  workflows: List<WorkflowEntity>,
  stepCounts: Map<String, Int>,
  hasUnreadNotifications: Boolean = false,
  onBackClick: () -> Unit,
  onNotificationClick: () -> Unit,
  onOpenWorkflow: (workflowId: String) -> Unit,
  onCreateWorkflow: () -> Unit,
  onRunWorkflow: suspend (workflowId: String) -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val haptics = LocalHapticFeedback.current
  var runningWorkflowId by remember { mutableStateOf<String?>(null) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("workflows_list_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp)
    ) {
      // Top bar with back button + notification button on opposite side
      PortageTopBar(
        showBackButton = true,
        showNotificationButton = true,
        hasUnreadNotifications = hasUnreadNotifications,
        onBackClick = onBackClick,
        onNotificationClick = onNotificationClick
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Header
      ScreenHeader(
        title = "Workflows",
        subtitle = "Automate repeatable actions"
      )

      Spacer(modifier = Modifier.height(24.dp))

      if (workflows.isEmpty()) {
        EmptyStateCard(
          message = "There are no workflows created. Click button below to create one now.",
          buttonText = "Create Workflow",
          onButtonClick = onCreateWorkflow,
          modifier = Modifier.weight(1f),
          testTag = "create_workflow_empty_button"
        )
        Spacer(modifier = Modifier.height(24.dp))
      } else {
        // Redesigned cleaner workflow list matching user's image
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(workflows, key = { it.id }) { workflow ->
            val count = stepCounts[workflow.id] ?: 0
            val isRunning = runningWorkflowId == workflow.id

            WorkflowCardItem(
              workflow = workflow,
              stepCount = count,
              isRunning = isRunning,
              onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onOpenWorkflow(workflow.id)
              },
              onRun = {
                if (runningWorkflowId == null) {
                  haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                  runningWorkflowId = workflow.id
                  scope.launch {
                    val total = if (count > 0) count else 1
                    for (step in 1..total) {
                      Toast.makeText(context, "Running ${workflow.name}: Step $step/$total", Toast.LENGTH_SHORT).show()
                      kotlinx.coroutines.delay(650)
                    }
                    onRunWorkflow(workflow.id)
                    runningWorkflowId = null
                    Toast.makeText(context, "Workflow '${workflow.name}' completed", Toast.LENGTH_SHORT).show()
                  }
                }
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pinned Create New Workflow button (h=50)
        PortagePrimaryButton(
          text = "Create New Workflow",
          onClick = onCreateWorkflow,
          height = 50.dp,
          modifier = Modifier.fillMaxWidth(),
          testTag = "create_new_workflow_button"
        )

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

/**
 * Clean, single-row workflow card matching panel 1 in user's image:
 * "Workflow 1  ·  5 STEPS" on left, 44x44 rounded square Play button on right.
 */
@Composable
private fun WorkflowCardItem(
  workflow: WorkflowEntity,
  stepCount: Int,
  isRunning: Boolean,
  onClick: () -> Unit,
  onRun: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(72.dp)
      .clip(RoundedCornerShape(18.dp))
      .background(com.example.ui.theme.PortageSurface2)
      .border(1.dp, PortageBorder, RoundedCornerShape(18.dp))
      .clickable { onClick() }
      .padding(horizontal = 18.dp, vertical = 12.dp)
      .testTag("workflow_card_${workflow.name}"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // "Workflow 1  ·  5 STEPS"
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
      Text(
        text = workflow.name,
        color = PortageText,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f, fill = false)
      )

      Text(
        text = "  ·  ",
        color = PortageTextMuted,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = "$stepCount STEPS",
        color = PortageTextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    // 44x44 rounded-square button with outlined Play icon
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(Color(0xFF383838))
        .clickable { onRun() }
        .testTag("run_workflow_button"),
      contentAlignment = Alignment.Center
    ) {
      if (isRunning) {
        CircularProgressIndicator(
          color = PortageText,
          strokeWidth = 2.dp,
          modifier = Modifier.size(18.dp)
        )
      } else {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = "Run",
          tint = PortageText,
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}
