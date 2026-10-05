package com.example.device

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class DeviceEvent {
  data class TextSent(val text: String) : DeviceEvent()
  data class KeyRelayed(val key: String) : DeviceEvent()
  data class MouseMove(val dx: Float, val dy: Float) : DeviceEvent()
  data class MouseClick(val button: String) : DeviceEvent()
  data class ShortcutSent(val shortcut: String) : DeviceEvent()
  data class WorkflowStepStarted(val stepIndex: Int, val title: String) : DeviceEvent()
  object WorkflowCompleted : DeviceEvent()
  data class StatusMessage(val message: String) : DeviceEvent()
}

class DeviceService {

  private val _events = MutableSharedFlow<DeviceEvent>()
  val events: SharedFlow<DeviceEvent> = _events.asSharedFlow()

  suspend fun sendText(text: String): Boolean {
    delay(250) // simulate USB HID packet transmission
    _events.emit(DeviceEvent.TextSent(text))
    return true
  }

  suspend fun relayKey(key: String): Boolean {
    delay(50) // ultra low latency relay
    _events.emit(DeviceEvent.KeyRelayed(key))
    return true
  }

  suspend fun sendMouseMove(dx: Float, dy: Float): Boolean {
    _events.emit(DeviceEvent.MouseMove(dx, dy))
    return true
  }

  suspend fun sendMouseClick(button: String = "LEFT"): Boolean {
    _events.emit(DeviceEvent.MouseClick(button))
    return true
  }

  suspend fun sendShortcut(shortcut: String): Boolean {
    _events.emit(DeviceEvent.ShortcutSent(shortcut))
    return true
  }

  suspend fun runWorkflow(
    steps: List<com.example.data.model.WorkflowStepEntity>,
    onStepChange: (Int) -> Unit
  ) {
    for ((index, step) in steps.withIndex()) {
      onStepChange(index)
      _events.emit(DeviceEvent.WorkflowStepStarted(index + 1, step.title))

      when (step.actionType) {
        "WAIT" -> {
          val ms = step.payload.toLongOrNull() ?: 500L
          delay(ms.coerceIn(100L, 10000L))
        }
        "TYPE_TEXT" -> {
          delay(300)
        }
        "KEY_SHORTCUT" -> {
          delay(200)
        }
        else -> {
          delay(250)
        }
      }
    }
    _events.emit(DeviceEvent.WorkflowCompleted)
  }
}
