package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_profile")
data class DeviceProfile(
  @PrimaryKey val id: Int = 1,
  val name: String = "Lenovo Yogabook",
  val type: String = "Laptop",
  val batteryPercent: Int = 57,
  val isConnected: Boolean = true,
  val onboardingComplete: Boolean = false
)

@Entity(tableName = "workflows")
data class WorkflowEntity(
  @PrimaryKey val id: String,
  val name: String,
  val createdDate: String,
  val orderIndex: Int = 0
)

@Entity(tableName = "workflow_steps")
data class WorkflowStepEntity(
  @PrimaryKey val id: String,
  val workflowId: String,
  val orderIndex: Int,
  val title: String,
  val actionType: String,
  val badges: String, // comma-separated e.g. "GUI,R" or "Type Text"
  val payload: String = ""
)

@Entity(tableName = "remaps")
data class RemapEntity(
  @PrimaryKey val id: String,
  val name: String,
  val triggerKey: String,
  val targetKey: String,
  val isActive: Boolean = true,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transmissions")
data class TransmissionEntity(
  @PrimaryKey val id: String,
  val text: String,
  val timestamp: Long = System.currentTimeMillis()
)

data class WorkflowWithSteps(
  val workflow: WorkflowEntity,
  val steps: List<WorkflowStepEntity>
)
