package com.example.service

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Global bus for forwarding physical hardware volume key events
 * to active screens (Live Relay, Remap, etc.)
 * Includes debounce to prevent hardware key bounces or multi-listener duplicate registrations.
 */
object VolumeKeyBus {
  private val _keyEvents = MutableSharedFlow<String>(extraBufferCapacity = 64)
  val keyEvents: SharedFlow<String> = _keyEvents.asSharedFlow()

  @Volatile
  private var lastEmitTime: Long = 0L

  @Volatile
  private var lastKey: String? = null

  fun emitKey(key: String): Boolean {
    val now = SystemClock.uptimeMillis()
    // Debounce duplicate events within 200ms
    if (key == lastKey && (now - lastEmitTime) < 200L) {
      return false
    }
    lastKey = key
    lastEmitTime = now
    return _keyEvents.tryEmit(key)
  }
}

