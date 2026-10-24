package com.example.ui.screens.workflow

import android.widget.Toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.model.WorkflowEntity
import com.example.data.model.WorkflowStepEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PortageChip
import com.example.ui.components.PortagePrimaryButton
import com.example.ui.components.PortageTopBar
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageBorder
import com.example.ui.theme.PortageDanger
import com.example.ui.theme.PortageFontFamily
import com.example.ui.theme.PortagePrimary
import com.example.ui.theme.PortagePrimaryHover
import com.example.ui.theme.PortageSurface2
import com.example.ui.theme.PortageSurface3
import com.example.ui.theme.PortageText
import com.example.ui.theme.PortageTextMuted
import kotlin.math.roundToInt

@Composable
fun WorkflowDetailScreen(
  workflow: WorkflowEntity,
  steps: List<WorkflowStepEntity>,
  hasUnreadNotifications: Boolean = false,
  onBackClick: () -> Unit,
  onNotificationClick: () -> Unit = {},
  onAddStep: (title: String, actionType: String, badges: String, payload: String) -> Unit,
  onUpdateStep: (WorkflowStepEntity) -> Unit,
  onDeleteStep: (stepId: String) -> Unit,
  onDuplicateStep: (WorkflowStepEntity) -> Unit,
  onReorderSteps: (List<WorkflowStepEntity>) -> Unit,
  onRenameWorkflow: (name: String) -> Unit,
  onRunWorkflow: () -> Unit
) {
  var showAddActionSheet by remember { mutableStateOf(false) }
  var pendingActionType by remember { mutableStateOf<String?>(null) }
  var pendingActionName by remember { mutableStateOf<String?>(null) }
  var editingStep by remember { mutableStateOf<WorkflowStepEntity?>(null) }
  var showRenameDialog by remember { mutableStateOf(false) }
  var renameInput by remember { mutableStateOf(workflow.name) }

  // Drag and drop tracking state
  var draggedIndex by remember { mutableStateOf<Int?>(null) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }
  var activeExecutingStepIndex by remember { mutableStateOf<Int?>(null) }

  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current
  val scope = androidx.compose.runtime.rememberCoroutineScope()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PortageBg)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("workflow_detail_screen")
  ) {
    // Backdrop behavior: when a sheet is open, show card container effect
    val sheetIsOpen = showAddActionSheet || pendingActionType != null || editingStep != null
    val rootModifier = if (sheetIsOpen) {
      Modifier
        .fillMaxSize()
        .padding(horizontal = 14.dp, vertical = 8.dp)
        .border(1.dp, PortageBorder, RoundedCornerShape(28.dp))
        .clip(RoundedCornerShape(28.dp))
        .background(PortageBg.copy(alpha = 0.85f))
        .padding(12.dp)
    } else {
      Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp)
    }

    Column(modifier = rootModifier) {
      // Top bar with back button + notification button on opposite side
      PortageTopBar(
        showBackButton = true,
        showNotificationButton = true,
        hasUnreadNotifications = hasUnreadNotifications,
        onBackClick = onBackClick,
        onNotificationClick = onNotificationClick
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Header row: Title + Edit pencil + Circle Play Button next to title
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(
          modifier = Modifier
            .weight(1f)
            .padding(end = 16.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = workflow.name,
              color = PortageText,
              fontSize = 28.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.5).sp,
              lineHeight = 34.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier
                .weight(1f, fill = false)
                .widthIn(max = 130.dp)
                .clickable {
                  renameInput = workflow.name
                  showRenameDialog = true
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PortageSurface3)
                .clickable {
                  haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                  renameInput = workflow.name
                  showRenameDialog = true
                }
                .testTag("edit_workflow_name_button"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Rename workflow",
                tint = PortageTextMuted,
                modifier = Modifier.size(16.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (steps.isEmpty()) "No steps added yet" else "${steps.size} Steps",
            color = PortageTextMuted,
            fontSize = 14.sp
          )
        }

        // Circle Play Button next to workflow title
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(PortagePrimary)
            .clickable {
              if (activeExecutingStepIndex == null && steps.isNotEmpty()) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch {
                  for (i in steps.indices) {
                    activeExecutingStepIndex = i
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    kotlinx.coroutines.delay(700)
                  }
                  activeExecutingStepIndex = null
                  Toast.makeText(context, "'${workflow.name}' completed", Toast.LENGTH_SHORT).show()
                  onRunWorkflow()
                }
              }
            }
            .testTag("circle_play_workflow_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Run Workflow",
            tint = PortageText,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      if (steps.isEmpty()) {
        EmptyStateCard(
          message = "There are no steps created. Click button below to create one now.",
          buttonText = "New Step",
          onButtonClick = { showAddActionSheet = true },
          modifier = Modifier.weight(1f),
          testTag = "create_step_empty_button"
        )
        Spacer(modifier = Modifier.height(24.dp))
      } else {
        // Redesigned spacious step cards (h=78dp) with intuitive drag-and-drop reorder
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          itemsIndexed(steps, key = { _, step -> step.id }) { index, step ->
            val isBeingDragged = draggedIndex == index
            val animatedScale by animateFloatAsState(targetValue = if (isBeingDragged) 1.03f else 1.0f)

            WorkflowStepRowItem(
              stepIndex = index + 1,
              step = step,
              isBeingDragged = isBeingDragged,
              isExecuting = activeExecutingStepIndex == index,
              dragOffsetY = if (isBeingDragged) dragOffsetY else 0f,
              modifier = Modifier
                .scale(animatedScale)
                .zIndex(if (isBeingDragged) 10f else 1f)
                .pointerInput(steps) {
                  // Long-press anywhere on card to drag
                  detectDragGesturesAfterLongPress(
                    onDragStart = {
                      draggedIndex = index
                      dragOffsetY = 0f
                      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDragEnd = {
                      draggedIndex = null
                      dragOffsetY = 0f
                    },
                    onDragCancel = {
                      draggedIndex = null
                      dragOffsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                      change.consume()
                      dragOffsetY += dragAmount.y
                      val itemThreshold = 78.dp.toPx()
                      if (dragOffsetY > itemThreshold && index < steps.size - 1) {
                        val mutable = steps.toMutableList()
                        val temp = mutable[index]
                        mutable[index] = mutable[index + 1]
                        mutable[index + 1] = temp
                        onReorderSteps(mutable.mapIndexed { i, s -> s.copy(orderIndex = i) })
                        dragOffsetY = 0f
                        draggedIndex = index + 1
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                      } else if (dragOffsetY < -itemThreshold && index > 0) {
                        val mutable = steps.toMutableList()
                        val temp = mutable[index]
                        mutable[index] = mutable[index - 1]
                        mutable[index - 1] = temp
                        onReorderSteps(mutable.mapIndexed { i, s -> s.copy(orderIndex = i) })
                        dragOffsetY = 0f
                        draggedIndex = index - 1
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                      }
                    }
                  )
                },
              onHandleDragModifier = {
                // Immediate drag on handle touch
                Modifier.pointerInput(steps) {
                  detectDragGestures(
                    onDragStart = {
                      draggedIndex = index
                      dragOffsetY = 0f
                      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDragEnd = {
                      draggedIndex = null
                      dragOffsetY = 0f
                    },
                    onDragCancel = {
                      draggedIndex = null
                      dragOffsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                      change.consume()
                      dragOffsetY += dragAmount.y
                      val itemThreshold = 78.dp.toPx()
                      if (dragOffsetY > itemThreshold && index < steps.size - 1) {
                        val mutable = steps.toMutableList()
                        val temp = mutable[index]
                        mutable[index] = mutable[index + 1]
                        mutable[index + 1] = temp
                        onReorderSteps(mutable.mapIndexed { i, s -> s.copy(orderIndex = i) })
                        dragOffsetY = 0f
                        draggedIndex = index + 1
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                      } else if (dragOffsetY < -itemThreshold && index > 0) {
                        val mutable = steps.toMutableList()
                        val temp = mutable[index]
                        mutable[index] = mutable[index - 1]
                        mutable[index - 1] = temp
                        onReorderSteps(mutable.mapIndexed { i, s -> s.copy(orderIndex = i) })
                        dragOffsetY = 0f
                        draggedIndex = index - 1
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                      }
                    }
                  )
                }
              },
              onClick = {
                editingStep = step
                pendingActionType = step.actionType
                pendingActionName = when (step.actionType) {
                  "TYPE_TEXT" -> "Type text"
                  "WAIT" -> "Wait"
                  "KEY_SHORTCUT" -> "Key or Shortcut"
                  "CLICK" -> "Click"
                  "MOVE" -> "Move"
                  "SCROLL" -> "Scroll"
                  else -> "Action"
                }
              },
              onDelete = { onDeleteStep(step.id) },
              onDuplicate = { onDuplicateStep(step) }
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pinned New Step button (h=50)
        PortagePrimaryButton(
          text = "New Step",
          onClick = {
            editingStep = null
            showAddActionSheet = true
          },
          height = 50.dp,
          modifier = Modifier.fillMaxWidth(),
          testTag = "new_step_button"
        )

        Spacer(modifier = Modifier.height(24.dp))
      }
    }

    // Sheet 1: Add Action Sheet (Search & categories)
    if (showAddActionSheet) {
      AddActionSheet(
        onSelectAction = { actionType, actionName ->
          showAddActionSheet = false
          pendingActionType = actionType
          pendingActionName = actionName
          editingStep = null
        },
        onDismiss = { showAddActionSheet = false }
      )
    }

    // Sheet 2: Step Configuration Sheet (supports both New and Edit modes)
    if (pendingActionType != null && pendingActionName != null) {
      StepConfigSheet(
        stepNumber = if (editingStep != null) steps.indexOfFirst { it.id == editingStep!!.id } + 1 else steps.size + 1,
        actionType = pendingActionType!!,
        actionName = pendingActionName!!,
        initialTitle = editingStep?.title,
        initialPayload = editingStep?.payload,
        initialBadges = editingStep?.badges,
        onSaveStep = { title, actionType, badges, payload ->
          if (editingStep != null) {
            onUpdateStep(
              editingStep!!.copy(
                title = title,
                actionType = actionType,
                badges = badges,
                payload = payload
              )
            )
            Toast.makeText(context, "Step updated", Toast.LENGTH_SHORT).show()
          } else {
            onAddStep(title, actionType, badges, payload)
          }
          pendingActionType = null
          pendingActionName = null
          editingStep = null
        },
        onDismiss = {
          pendingActionType = null
          pendingActionName = null
          editingStep = null
        }
      )
    }

    // Rename Dialog
    if (showRenameDialog) {
      AlertDialog(
        onDismissRequest = { showRenameDialog = false },
        containerColor = PortageSurface2,
        title = {
          Text(text = "Rename Workflow", color = PortageText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        },
        text = {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(PortageSurface3)
              .border(1.dp, PortageBorder, RoundedCornerShape(12.dp))
              .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            BasicTextField(
              value = renameInput,
              onValueChange = { renameInput = it },
              textStyle = TextStyle(
                color = PortageText,
                fontSize = 15.sp,
                fontFamily = PortageFontFamily
              ),
              cursorBrush = SolidColor(PortageText),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          TextButton(
            onClick = {
              if (renameInput.isNotBlank()) {
                onRenameWorkflow(renameInput.trim())
                showRenameDialog = false
              }
            }
          ) {
            Text("Save", color = PortagePrimary, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showRenameDialog = false }) {
            Text("Cancel", color = PortageTextMuted)
          }
        }
      )
    }
  }
}

/**
 * Spacious, airy step card with increased height (78dp) and drag & drop reorder
 */
@Composable
fun WorkflowStepRowItem(
  stepIndex: Int,
  step: WorkflowStepEntity,
  isBeingDragged: Boolean,
  isExecuting: Boolean = false,
  dragOffsetY: Float,
  modifier: Modifier = Modifier,
  onHandleDragModifier: () -> Modifier = { Modifier },
  onClick: () -> Unit,
  onDelete: () -> Unit,
  onDuplicate: () -> Unit
) {
  var isMenuOpen by remember { mutableStateOf(false) }
  val haptics = LocalHapticFeedback.current

  Row(
    modifier = modifier
      .fillMaxWidth()
      .offset { IntOffset(0, dragOffsetY.roundToInt()) }
      .height(78.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(PortageSurface2)
      .border(
        width = if (isExecuting) 2.5.dp else if (isBeingDragged) 1.5.dp else 1.dp,
        color = if (isExecuting) PortagePrimaryHover else if (isBeingDragged) PortagePrimary else PortageBorder,
        shape = RoundedCornerShape(20.dp)
      )
      .clickable {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        onClick()
      }
      .padding(horizontal = 18.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Subtle drag indicator handle with direct touch tracking
    Box(
      modifier = Modifier
        .then(onHandleDragModifier())
        .padding(end = 14.dp)
        .size(28.dp),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.DragHandle,
        contentDescription = "Drag to reorder",
        tint = if (isBeingDragged) PortagePrimary else PortageTextMuted.copy(alpha = 0.5f),
        modifier = Modifier.size(20.dp)
      )
    }

    // Left: STEP N and title (airy, clean hierarchy)
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = "STEP $stepIndex",
        color = PortageTextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = step.title,
        color = PortageText,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    // Right: Badge chip(s) + Kebab
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      val badgeList = remember(step.badges) {
        step.badges.split(",").map { it.trim() }.filter { it.isNotEmpty() }
      }

      badgeList.forEach { badge ->
        PortageChip(text = badge)
      }

      // Options kebab
      Box {
        Icon(
          imageVector = Icons.Default.MoreVert,
          contentDescription = "Step Options",
          tint = PortageTextMuted,
          modifier = Modifier
            .size(22.dp)
            .clickable { isMenuOpen = true }
        )

        DropdownMenu(
          expanded = isMenuOpen,
          onDismissRequest = { isMenuOpen = false },
          modifier = Modifier
            .background(PortageSurface2)
            .border(1.dp, PortageBorder, RoundedCornerShape(12.dp))
        ) {
          DropdownMenuItem(
            text = { Text("Edit Step", color = PortageText) },
            onClick = {
              isMenuOpen = false
              onClick()
            }
          )
          DropdownMenuItem(
            text = { Text("Duplicate", color = PortageText) },
            onClick = {
              isMenuOpen = false
              onDuplicate()
            }
          )
          DropdownMenuItem(
            text = { Text("Delete", color = PortageDanger) },
            onClick = {
              isMenuOpen = false
              onDelete()
            },
            colors = MenuDefaults.itemColors(
              textColor = PortageDanger
            )
          )
        }
      }
    }
  }
}
