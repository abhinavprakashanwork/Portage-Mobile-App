package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.DeviceProfile
import com.example.data.model.RemapEntity
import com.example.data.model.TransmissionEntity
import com.example.data.model.WorkflowEntity
import com.example.data.model.WorkflowStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PortageDao {

  // Device Profile
  @Query("SELECT * FROM device_profile WHERE id = 1 LIMIT 1")
  fun getDeviceProfile(): Flow<DeviceProfile?>

  @Query("SELECT * FROM device_profile WHERE id = 1 LIMIT 1")
  suspend fun getDeviceProfileOnce(): DeviceProfile?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveDeviceProfile(profile: DeviceProfile)

  // Workflows
  @Query("SELECT * FROM workflows ORDER BY orderIndex ASC")
  fun getAllWorkflows(): Flow<List<WorkflowEntity>>

  @Query("SELECT * FROM workflows WHERE id = :id LIMIT 1")
  fun getWorkflowById(id: String): Flow<WorkflowEntity?>

  @Query("SELECT * FROM workflows WHERE id = :id LIMIT 1")
  suspend fun getWorkflowByIdOnce(id: String): WorkflowEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWorkflow(workflow: WorkflowEntity)

  @Query("UPDATE workflows SET name = :name WHERE id = :id")
  suspend fun updateWorkflowName(id: String, name: String)

  @Query("DELETE FROM workflows WHERE id = :id")
  suspend fun deleteWorkflowById(id: String)

  // Steps
  @Query("SELECT * FROM workflow_steps WHERE workflowId = :workflowId ORDER BY orderIndex ASC")
  fun getStepsForWorkflow(workflowId: String): Flow<List<WorkflowStepEntity>>

  @Query("SELECT * FROM workflow_steps WHERE workflowId = :workflowId ORDER BY orderIndex ASC")
  suspend fun getStepsForWorkflowOnce(workflowId: String): List<WorkflowStepEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStep(step: WorkflowStepEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSteps(steps: List<WorkflowStepEntity>)

  @Query("DELETE FROM workflow_steps WHERE id = :id")
  suspend fun deleteStepById(id: String)

  @Query("DELETE FROM workflow_steps WHERE workflowId = :workflowId")
  suspend fun deleteStepsForWorkflow(workflowId: String)

  // Remaps
  @Query("SELECT * FROM remaps ORDER BY createdAt DESC")
  fun getAllRemaps(): Flow<List<RemapEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRemap(remap: RemapEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRemaps(remaps: List<RemapEntity>)

  @Query("UPDATE remaps SET isActive = :isActive WHERE id = :id")
  suspend fun updateRemapActiveStatus(id: String, isActive: Boolean)

  @Query("SELECT * FROM remaps WHERE id = :id LIMIT 1")
  suspend fun getRemapById(id: String): RemapEntity?

  @Query("DELETE FROM remaps WHERE id = :id")
  suspend fun deleteRemapById(id: String)

  // Transmissions
  @Query("SELECT * FROM transmissions ORDER BY timestamp DESC")
  fun getAllTransmissions(): Flow<List<TransmissionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransmission(transmission: TransmissionEntity)

  // Notifications / Activity Events
  @Query("SELECT * FROM event_notifications ORDER BY timestamp DESC")
  fun getAllNotifications(): Flow<List<com.example.data.model.EventNotificationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotification(notification: com.example.data.model.EventNotificationEntity)

  @Query("DELETE FROM event_notifications")
  suspend fun clearAllNotifications()

  @Query("DELETE FROM event_notifications WHERE id = :id")
  suspend fun deleteNotificationById(id: String)

  @Query("UPDATE event_notifications SET isRead = 1")
  suspend fun markAllNotificationsAsRead()

  // Reset
  @Query("DELETE FROM device_profile")
  suspend fun clearDeviceProfile()

  @Query("DELETE FROM workflows")
  suspend fun clearWorkflows()

  @Query("DELETE FROM workflow_steps")
  suspend fun clearSteps()

  @Query("DELETE FROM remaps")
  suspend fun clearRemaps()

  @Query("DELETE FROM transmissions")
  suspend fun clearTransmissions()

  @Transaction
  suspend fun resetAll() {
    clearDeviceProfile()
    clearWorkflows()
    clearSteps()
    clearRemaps()
    clearTransmissions()
  }
}
