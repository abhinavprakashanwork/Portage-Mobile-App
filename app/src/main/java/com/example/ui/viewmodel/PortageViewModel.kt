package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PortageDatabase
import com.example.data.model.DeviceProfile
import com.example.data.model.RemapEntity
import com.example.data.model.TransmissionEntity
import com.example.data.model.WorkflowEntity
import com.example.data.model.WorkflowStepEntity
import com.example.data.repository.PortageRepository
import com.example.device.DeviceService
import com.example.device.DeviceConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PortageViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: PortageRepository
  private val deviceService = DeviceService(application)

  val connectionState: StateFlow<DeviceConnectionState> = deviceService.connectionState

  val deviceProfile: StateFlow<DeviceProfile?>
  val workflows: StateFlow<List<WorkflowEntity>>
  val remaps: StateFlow<List<RemapEntity>>
  val transmissions: StateFlow<List<TransmissionEntity>>
  val notifications: StateFlow<List<com.example.data.model.EventNotificationEntity>>

  private val _selectedWorkflowSteps = MutableStateFlow<List<WorkflowStepEntity>>(emptyList())
  val selectedWorkflowSteps: StateFlow<List<WorkflowStepEntity>> = _selectedWorkflowSteps.asStateFlow()

  private val _stepCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
  val stepCounts: StateFlow<Map<String, Int>> = _stepCounts.asStateFlow()

  init {
    val dao = PortageDatabase.getDatabase(application).portageDao()
    repository = PortageRepository(dao)

    deviceProfile = repository.deviceProfile.stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = null
    )

    workflows = repository.workflows.stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

    remaps = repository.remaps.stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

    transmissions = repository.transmissions.stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

    notifications = repository.notifications.stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

    // Load step counts for all workflows
    viewModelScope.launch {
      workflows.collect { wfList ->
        val counts = mutableMapOf<String, Int>()
        for (wf in wfList) {
          repository.getStepsForWorkflow(wf.id).collect { steps ->
            counts[wf.id] = steps.size
            _stepCounts.value = counts.toMap()
          }
        }
      }
    }
  }

  fun loadStepsForWorkflow(workflowId: String) {
    viewModelScope.launch {
      repository.getStepsForWorkflow(workflowId).collect { steps ->
        _selectedWorkflowSteps.value = steps
      }
    }
  }

  fun finishOnboarding(name: String, type: String) {
    viewModelScope.launch {
      repository.finishOnboarding(name, type)
    }
  }

  fun connect() {
    viewModelScope.launch {
      deviceService.connect()
    }
  }

  fun pair(keyBase64: String) {
    viewModelScope.launch {
      deviceService.pair(keyBase64)
    }
  }

  fun sendTestString(text: String) {
    viewModelScope.launch {
      deviceService.sendTestString(text)
    }
  }

  fun disconnect() {
    deviceService.disconnect()
  }

  fun importExistingProfile(profile: com.example.data.model.ImportableProfile) {
    viewModelScope.launch {
      repository.importExistingProfile(profile)
    }
  }

  suspend fun sendText(text: String): Boolean {
    repository.addTransmission(text)
    return deviceService.sendText(text)
  }

  suspend fun relayKey(key: String): Boolean {
    return deviceService.relayKey(key)
  }

  suspend fun runWorkflow(workflowId: String) {
    val steps = _selectedWorkflowSteps.value
    deviceService.runWorkflow(steps) { /* progress */ }
  }

  fun executeWorkflow(workflowId: String) {
    viewModelScope.launch {
      val wf = workflows.value.firstOrNull { it.id == workflowId }
      val name = wf?.name ?: "Workflow"
      repository.addNotification("Workflow Executed", "Ran $name (${_selectedWorkflowSteps.value.size} steps)", "WORKFLOW")
      runWorkflow(workflowId)
    }
  }

  fun clearNotifications() {
    viewModelScope.launch {
      repository.clearNotifications()
    }
  }

  fun deleteNotification(id: String) {
    viewModelScope.launch {
      repository.deleteNotification(id)
    }
  }

  fun markNotificationsRead() {
    viewModelScope.launch {
      repository.markNotificationsRead()
    }
  }

  fun clearTransmissions() {
    viewModelScope.launch {
      repository.clearAllTransmissions()
    }
  }

  fun renameWorkflow(workflowId: String, newName: String) {
    viewModelScope.launch {
      repository.updateWorkflowName(workflowId, newName)
    }
  }

  fun reorderSteps(steps: List<WorkflowStepEntity>) {
    viewModelScope.launch {
      repository.reorderSteps(steps)
    }
  }

  fun createWorkflow(name: String = "Workflow ${workflows.value.size + 1}", onCreated: (String) -> Unit) {
    viewModelScope.launch {
      val id = repository.createWorkflow(name)
      onCreated(id)
    }
  }

  fun deleteWorkflow(id: String) {
    viewModelScope.launch {
      repository.deleteWorkflow(id)
    }
  }

  fun duplicateWorkflow(id: String) {
    viewModelScope.launch {
      repository.duplicateWorkflow(id)
    }
  }

  fun addStep(workflowId: String, title: String, actionType: String, badges: String, payload: String) {
    viewModelScope.launch {
      repository.addStep(workflowId, title, actionType, badges, payload)
    }
  }

  fun updateStep(step: WorkflowStepEntity) {
    viewModelScope.launch {
      repository.updateStep(step)
    }
  }

  fun deleteStep(stepId: String) {
    viewModelScope.launch {
      repository.deleteStep(stepId)
    }
  }

  fun duplicateStep(step: WorkflowStepEntity) {
    viewModelScope.launch {
      repository.addStep(
        workflowId = step.workflowId,
        title = "${step.title} (Copy)",
        actionType = step.actionType,
        badges = step.badges,
        payload = step.payload
      )
    }
  }

  fun saveRemap(name: String, triggerKey: String, targetKey: String, isActive: Boolean = true) {
    viewModelScope.launch {
      repository.saveRemap(name, triggerKey, targetKey, isActive)
    }
  }

  fun toggleRemapActive(id: String, isActive: Boolean) {
    viewModelScope.launch {
      repository.setRemapActive(id, isActive)
    }
  }

  fun deleteRemap(id: String) {
    viewModelScope.launch {
      repository.deleteRemap(id)
    }
  }

  fun updateDevice(name: String, type: String) {
    viewModelScope.launch {
      repository.updateDeviceNameAndType(name, type)
    }
  }

  fun toggleConnection() {
    if (connectionState.value == DeviceConnectionState.READY) disconnect() else connect()
  }

  fun sendMouseMove(dx: Float, dy: Float) {
    viewModelScope.launch {
      deviceService.sendMouseMove(dx, dy)
    }
  }

  fun sendMouseClick(button: String = "LEFT") {
    viewModelScope.launch {
      deviceService.sendMouseClick(button)
      repository.addTransmission("Click $button")
    }
  }

  fun sendShortcut(shortcut: String) {
    viewModelScope.launch {
      deviceService.sendShortcut(shortcut)
      repository.addTransmission("Shortcut: $shortcut")
    }
  }

  fun forgetDevice(onForgotten: () -> Unit) {
    viewModelScope.launch {
      deviceService.forgetDevice()
      repository.clearAllData()
      onForgotten()
    }
  }
}
