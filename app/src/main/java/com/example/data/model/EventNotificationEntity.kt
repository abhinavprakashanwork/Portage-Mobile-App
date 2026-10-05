package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "event_notifications")
data class EventNotificationEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val title: String,
  val description: String,
  val type: String = "SYSTEM", // "WORKFLOW", "DEVICE", "TRANSMISSION", "REMAP", "RELAY"
  val timestamp: Long = System.currentTimeMillis(),
  val isRead: Boolean = false
)
