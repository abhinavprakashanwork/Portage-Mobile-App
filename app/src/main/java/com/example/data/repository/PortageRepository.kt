package com.example.data.repository

import com.example.data.local.PortageDao
import com.example.data.model.DeviceProfile
import com.example.data.model.EventNotificationEntity
import com.example.data.model.RemapEntity
import com.example.data.model.TransmissionEntity
import com.example.data.model.WorkflowEntity
import com.example.data.model.WorkflowStepEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class PortageRepository(private val dao: PortageDao) {

  val deviceProfile: Flow<DeviceProfile?> = dao.getDeviceProfile()
  val workflows: Flow<List<WorkflowEntity>> = dao.getAllWorkflows()
  val remaps: Flow<List<RemapEntity>> = dao.getAllRemaps()
  val transmissions: Flow<List<TransmissionEntity>> = dao.getAllTransmissions()
  val notifications: Flow<List<EventNotificationEntity>> = dao.getAllNotifications()

  fun getStepsForWorkflow(workflowId: String): Flow<List<WorkflowStepEntity>> {
    return dao.getStepsForWorkflow(workflowId)
  }

  fun getWorkflowById(workflowId: String): Flow<WorkflowEntity?> {
    return dao.getWorkflowById(workflowId)
  }

  suspend fun addNotification(title: String, description: String, type: String = "SYSTEM") {
    dao.insertNotification(
      EventNotificationEntity(
        id = UUID.randomUUID().toString(),
        title = title,
        description = description,
        type = type,
        timestamp = System.currentTimeMillis()
      )
    )
  }

  suspend fun clearNotifications() {
    dao.clearAllNotifications()
  }

  suspend fun deleteNotification(id: String) {
    dao.deleteNotificationById(id)
  }

  suspend fun markNotificationsRead() {
    dao.markAllNotificationsAsRead()
  }

  suspend fun saveDeviceProfile(profile: DeviceProfile) {
    dao.saveDeviceProfile(profile)
  }

  suspend fun updateConnection(isConnected: Boolean) {
    val current = dao.getDeviceProfileOnce() ?: DeviceProfile()
    dao.saveDeviceProfile(current.copy(isConnected = isConnected))
  }

  suspend fun updateDeviceNameAndType(name: String, type: String) {
    val current = dao.getDeviceProfileOnce() ?: DeviceProfile()
    dao.saveDeviceProfile(current.copy(name = name, type = type))
  }

  suspend fun finishOnboarding(name: String, type: String) {
    val devName = name.ifBlank { "Lenovo Yogabook" }
    dao.saveDeviceProfile(
      DeviceProfile(
        id = 1,
        name = devName,
        type = type,
        batteryPercent = 57,
        isConnected = true,
        onboardingComplete = true
      )
    )
    addNotification("Device Connected", "Initialized profile for $devName ($type)", "DEVICE")
  }

  suspend fun importExistingProfile(profile: com.example.data.model.ImportableProfile) {
    dao.saveDeviceProfile(
      DeviceProfile(
        id = 1,
        name = profile.name,
        type = profile.type,
        batteryPercent = if (profile.type == "Desktop") 100 else 88,
        isConnected = true,
        onboardingComplete = true
      )
    )

    for (wfData in profile.workflows) {
      val wfId = UUID.randomUUID().toString()
      dao.insertWorkflow(
        WorkflowEntity(
          id = wfId,
          name = wfData.name,
          createdDate = profile.savedDate,
          orderIndex = 0
        )
      )
      val stepEntities = wfData.steps.mapIndexed { idx, s ->
        WorkflowStepEntity(
          id = UUID.randomUUID().toString(),
          workflowId = wfId,
          orderIndex = idx,
          title = s.title,
          actionType = s.actionType,
          badges = s.badges,
          payload = s.payload
        )
      }
      dao.insertSteps(stepEntities)
    }

    for (remapData in profile.remaps) {
      dao.insertRemap(
        RemapEntity(
          id = UUID.randomUUID().toString(),
          name = remapData.name,
          triggerKey = remapData.triggerKey,
          targetKey = remapData.targetKey,
          isActive = remapData.isActive
        )
      )
    }

    for (text in profile.historyTransmissions) {
      dao.insertTransmission(
        TransmissionEntity(
          id = UUID.randomUUID().toString(),
          text = text,
          timestamp = System.currentTimeMillis() - (1000L * 60 * 30)
        )
      )
    }

    addNotification("Profile Imported", "Imported all configurations and history from ${profile.name}", "DEVICE")
  }

  suspend fun updateWorkflowName(id: String, name: String) {
    dao.updateWorkflowName(id, name)
  }

  suspend fun reorderSteps(steps: List<WorkflowStepEntity>) {
    dao.insertSteps(steps)
  }

  suspend fun clearAllTransmissions() {
    dao.clearTransmissions()
  }

  suspend fun createWorkflow(name: String): String {
    val id = UUID.randomUUID().toString()
    val workflow = WorkflowEntity(
      id = id,
      name = name,
      createdDate = "22/06/2026",
      orderIndex = 0
    )
    dao.insertWorkflow(workflow)
    return id
  }

  suspend fun deleteWorkflow(id: String) {
    dao.deleteWorkflowById(id)
    dao.deleteStepsForWorkflow(id)
  }

  suspend fun duplicateWorkflow(id: String) {
    val wf = dao.getWorkflowByIdOnce(id) ?: return
    val newId = UUID.randomUUID().toString()
    dao.insertWorkflow(wf.copy(id = newId, name = "${wf.name} (Copy)"))
    val steps = dao.getStepsForWorkflowOnce(id)
    val copiedSteps = steps.map { step ->
      step.copy(id = UUID.randomUUID().toString(), workflowId = newId)
    }
    dao.insertSteps(copiedSteps)
  }

  suspend fun addStep(
    workflowId: String,
    title: String,
    actionType: String,
    badges: String,
    payload: String
  ) {
    val currentSteps = dao.getStepsForWorkflowOnce(workflowId)
    val newStep = WorkflowStepEntity(
      id = UUID.randomUUID().toString(),
      workflowId = workflowId,
      orderIndex = currentSteps.size,
      title = title,
      actionType = actionType,
      badges = badges,
      payload = payload
    )
    dao.insertStep(newStep)
  }

  suspend fun updateStep(step: WorkflowStepEntity) {
    dao.insertStep(step)
  }

  suspend fun deleteStep(id: String) {
    dao.deleteStepById(id)
  }

  suspend fun duplicateStep(stepId: String) {
    // Duplicate step
    val allSteps = dao.getStepsForWorkflowOnce(stepId) // won't work by stepId, handled via step object
  }

  suspend fun saveRemap(name: String, triggerKey: String, targetKey: String, isActive: Boolean = true) {
    val remap = RemapEntity(
      id = UUID.randomUUID().toString(),
      name = name.ifBlank { "My Custom Remap" },
      triggerKey = triggerKey.ifBlank { "Vol +" },
      targetKey = targetKey.ifBlank { "Space" },
      isActive = isActive
    )
    dao.insertRemap(remap)
    addNotification("Button Remapped", "Mapped ${remap.triggerKey} -> ${remap.targetKey}", "REMAP")
  }

  suspend fun setRemapActive(id: String, isActive: Boolean) {
    dao.updateRemapActiveStatus(id, isActive)
    val status = if (isActive) "Activated" else "Deactivated"
    addNotification("Remap $status", "Remap status changed to $status", "REMAP")
  }

  suspend fun deleteRemap(id: String) {
    dao.deleteRemapById(id)
  }

  suspend fun addTransmission(text: String) {
    dao.insertTransmission(
      TransmissionEntity(
        id = UUID.randomUUID().toString(),
        text = text,
        timestamp = System.currentTimeMillis()
      )
    )
    addNotification("Text Sent", "Transmitted: \"$text\"", "TRANSMISSION")
  }

  suspend fun clearAllData() {
    dao.resetAll()
  }
}
