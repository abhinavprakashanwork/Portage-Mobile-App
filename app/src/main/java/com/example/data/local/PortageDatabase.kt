package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DeviceProfile
import com.example.data.model.EventNotificationEntity
import com.example.data.model.RemapEntity
import com.example.data.model.TransmissionEntity
import com.example.data.model.WorkflowEntity
import com.example.data.model.WorkflowStepEntity

@Database(
  entities = [
    DeviceProfile::class,
    WorkflowEntity::class,
    WorkflowStepEntity::class,
    RemapEntity::class,
    TransmissionEntity::class,
    EventNotificationEntity::class
  ],
  version = 3,
  exportSchema = false
)
abstract class PortageDatabase : RoomDatabase() {
  abstract fun portageDao(): PortageDao

  companion object {
    @Volatile
    private var INSTANCE: PortageDatabase? = null

    fun getDatabase(context: Context): PortageDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          PortageDatabase::class.java,
          "portage_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
