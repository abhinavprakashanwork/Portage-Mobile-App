package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.DeviceProfile
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.liverelay.LiveRelayScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.onboarding.ConnectDeviceScreen
import com.example.ui.screens.onboarding.DeviceInfoScreen
import com.example.ui.screens.onboarding.ImportProfileScreen
import com.example.ui.screens.onboarding.PairDeviceScreen
import com.example.ui.screens.onboarding.SplashScreen
import com.example.ui.screens.remap.RemapListScreen
import com.example.ui.screens.sendtext.SendTextScreen
import com.example.ui.screens.settings.DeviceSettingsScreen
import com.example.ui.screens.virtualcontrol.TrackpadScreen
import com.example.ui.screens.virtualcontrol.VirtualControlsScreen
import com.example.ui.screens.workflow.WorkflowDetailScreen
import com.example.ui.screens.workflow.WorkflowListScreen
import com.example.ui.theme.PortageBg
import com.example.ui.theme.PortageTheme
import com.example.ui.viewmodel.PortageViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    com.example.ui.components.IntelligenceAudioFeedback.init(applicationContext)
    com.example.service.PrivacyModeManager.init(applicationContext)
    setContent {
      PortageTheme {
        Surface(
          modifier = Modifier
            .fillMaxSize()
            .background(PortageBg),
          color = PortageBg
        ) {
          PortageApp()
        }
      }
    }
  }

  override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
    if (event.action == android.view.KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
      if (event.keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP) {
        com.example.service.VolumeKeyBus.emitKey("Vol +")
        return true
      } else if (event.keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN) {
        com.example.service.VolumeKeyBus.emitKey("Vol -")
        return true
      }
    }
    return super.dispatchKeyEvent(event)
  }
}

@Composable
fun PortageApp(
  viewModel: PortageViewModel = viewModel()
) {
  val navController = rememberNavController()

  val deviceProfile by viewModel.deviceProfile.collectAsState()
  val workflows by viewModel.workflows.collectAsState()
  val remaps by viewModel.remaps.collectAsState()
  val transmissions by viewModel.transmissions.collectAsState()
  val notifications by viewModel.notifications.collectAsState()
  val stepCounts by viewModel.stepCounts.collectAsState()
  val activeSteps by viewModel.selectedWorkflowSteps.collectAsState()

  val hasUnreadNotifications = notifications.any { !it.isRead }
  val activeProfile = deviceProfile ?: DeviceProfile()

  NavHost(
    navController = navController,
    startDestination = "splash"
  ) {
    composable("splash") {
      SplashScreen(
        isAlreadyOnboarded = deviceProfile?.onboardingComplete == true,
        onStartClick = {
          navController.navigate("connect_device")
        },
        onAutoNavigateHome = {
          navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
          }
        }
      )
    }

    composable("connect_device") {
      ConnectDeviceScreen(
        connectionState = viewModel.connectionState.collectAsState().value,
        onConnect = { viewModel.connect() },
        onPair = { key -> viewModel.pair(key) },
        onConnected = {
          navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
          }
        }
      )
    }

    composable("pair_device") {
      PairDeviceScreen(
        onPaired = {
          navController.navigate("import_profile")
        },
        onBackClick = {
          navController.popBackStack()
        }
      )
    }

    composable("import_profile") {
      ImportProfileScreen(
        onImportProfile = { profile ->
          viewModel.importExistingProfile(profile)
          navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
          }
        },
        onSkip = {
          navController.navigate("device_info")
        }
      )
    }

    composable("device_info") {
      DeviceInfoScreen(
        onFinish = { name, type ->
          viewModel.finishOnboarding(name, type)
          navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
          }
        }
      )
    }

    composable("home") {
      HomeScreen(
        deviceProfile = activeProfile,
        hasUnreadNotifications = hasUnreadNotifications,
        onNavigateToSendText = { navController.navigate("send_text") },
        onNavigateToLiveRelay = { navController.navigate("live_relay") },
        onNavigateToRemap = { navController.navigate("remap_list") },
        onNavigateToWorkflows = { navController.navigate("workflow_list") },
        onNavigateToVirtualControl = { navController.navigate("virtual_controls") },
        onNavigateToSettings = { navController.navigate("device_settings") },
        onNotificationClick = { navController.navigate("notifications") }
      )
    }

    composable("send_text") {
      SendTextScreen(
        transmissions = transmissions,
        isConnected = activeProfile.isConnected,
        onBackClick = { navController.popBackStack() },
        onNotificationClick = { navController.navigate("notifications") },
        onSendText = { text -> viewModel.sendText(text) },
        onClearTransmissions = { viewModel.clearTransmissions() }
      )
    }

    composable("live_relay") {
      LiveRelayScreen(
        isConnected = activeProfile.isConnected,
        onBackClick = { navController.popBackStack() },
        onNotificationClick = { navController.navigate("notifications") },
        onRelayKey = { key -> viewModel.relayKey(key) }
      )
    }

    composable("remap_list") {
      RemapListScreen(
        remaps = remaps,
        hasUnreadNotifications = hasUnreadNotifications,
        onBackClick = { navController.popBackStack() },
        onNotificationClick = { navController.navigate("notifications") },
        onSaveRemap = { name, trigger, target, active ->
          viewModel.saveRemap(name, trigger, target, active)
        },
        onToggleActive = { id, active ->
          viewModel.toggleRemapActive(id, active)
        },
        onDeleteRemap = { id -> viewModel.deleteRemap(id) }
      )
    }

    composable("workflow_list") {
      WorkflowListScreen(
        workflows = workflows,
        stepCounts = stepCounts,
        hasUnreadNotifications = hasUnreadNotifications,
        onBackClick = { navController.popBackStack() },
        onNotificationClick = { navController.navigate("notifications") },
        onOpenWorkflow = { workflowId ->
          viewModel.loadStepsForWorkflow(workflowId)
          navController.navigate("workflow_detail/$workflowId")
        },
        onCreateWorkflow = {
          viewModel.createWorkflow { _ ->
            // Do not jump directly into it; stays on workflow list screen!
          }
        },
        onRunWorkflow = { workflowId ->
          viewModel.loadStepsForWorkflow(workflowId)
          viewModel.executeWorkflow(workflowId)
        }
      )
    }

    composable(
      route = "workflow_detail/{workflowId}",
      arguments = listOf(navArgument("workflowId") { type = NavType.StringType })
    ) { backStackEntry ->
      val workflowId = backStackEntry.arguments?.getString("workflowId") ?: ""
      val currentWorkflow = workflows.firstOrNull { it.id == workflowId }
        ?: com.example.data.model.WorkflowEntity(
          id = workflowId,
          name = "Workflow 1",
          createdDate = "22/06/2026"
        )

      WorkflowDetailScreen(
        workflow = currentWorkflow,
        steps = activeSteps,
        hasUnreadNotifications = hasUnreadNotifications,
        onBackClick = { navController.popBackStack() },
        onNotificationClick = { navController.navigate("notifications") },
        onAddStep = { title, actionType, badges, payload ->
          viewModel.addStep(workflowId, title, actionType, badges, payload)
        },
        onUpdateStep = { step -> viewModel.updateStep(step) },
        onDeleteStep = { stepId -> viewModel.deleteStep(stepId) },
        onDuplicateStep = { step -> viewModel.duplicateStep(step) },
        onReorderSteps = { steps -> viewModel.reorderSteps(steps) },
        onRenameWorkflow = { newName -> viewModel.renameWorkflow(workflowId, newName) },
        onRunWorkflow = { viewModel.executeWorkflow(workflowId) }
      )
    }

    composable("device_settings") {
      DeviceSettingsScreen(
        deviceProfile = activeProfile,
        onBackClick = { navController.popBackStack() },
        onUpdateDevice = { name, type -> viewModel.updateDevice(name, type) },
        onToggleConnection = { viewModel.toggleConnection() },
        onForgetDevice = {
          viewModel.forgetDevice {
            navController.navigate("splash") {
              popUpTo(0) { inclusive = true }
            }
          }
        },
        onDebugTest = if (BuildConfig.DEBUG) {
          { navController.navigate("debug_test") }
        } else null
      )
    }

    composable("debug_test") {
      DebugTestScreen(
        connectionState = viewModel.connectionState.collectAsState().value,
        onBackClick = { navController.popBackStack() },
        onSendTest = { text -> viewModel.sendTestString(text) }
      )
    }

    composable("notifications") {
      NotificationsScreen(
        notifications = notifications,
        onBackClick = { navController.popBackStack() },
        onClearAll = { viewModel.clearNotifications() },
        onMarkAllRead = { viewModel.markNotificationsRead() },
        onDismissNotification = { id -> viewModel.deleteNotification(id) }
      )
    }

    composable("virtual_controls") {
      VirtualControlsScreen(
        onBackClick = { navController.popBackStack() },
        onNavigateToTrackpad = { navController.navigate("trackpad") }
      )
    }

    composable("trackpad") {
      val scope = androidx.compose.runtime.rememberCoroutineScope()
      TrackpadScreen(
        onBackClick = { navController.popBackStack() },
        onMouseMove = { dx, dy -> viewModel.sendMouseMove(dx, dy) },
        onMouseClick = { button -> viewModel.sendMouseClick(button) },
        onSendText = { text ->
          scope.launch {
            viewModel.sendText(text)
          }
        },
        onSendShortcut = { shortcut -> viewModel.sendShortcut(shortcut) },
        onPlayAction = {
          workflows.firstOrNull()?.let { wf ->
            viewModel.loadStepsForWorkflow(wf.id)
            viewModel.executeWorkflow(wf.id)
          } ?: run {
            viewModel.sendMouseClick("LEFT")
          }
        }
      )
    }
  }
}
