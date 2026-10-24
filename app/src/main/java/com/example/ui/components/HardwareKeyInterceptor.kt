package com.example.ui.components

import android.app.Activity
import android.view.KeyEvent
import android.view.ViewParent
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import com.example.service.VolumeKeyBus

/**
 * Intercepts physical hardware volume keys (KEYCODE_VOLUME_UP & KEYCODE_VOLUME_DOWN)
 * even when inside Dialogs or ModalBottomSheet windows, routing them to [VolumeKeyBus].
 * If [onVolumeKey] is provided, it is invoked only if not already handled by [VolumeKeyBus].
 */
@Composable
fun HardwareKeyInterceptor(
  enabled: Boolean = true,
  onVolumeKey: ((String) -> Unit)? = null
) {
  val view = LocalView.current
  DisposableEffect(view, enabled, onVolumeKey) {
    if (!enabled) {
      onDispose {}
    } else {
      var parent: ViewParent? = view.parent
      var dialogWindow: Window? = null
      while (parent != null) {
        if (parent is DialogWindowProvider) {
          dialogWindow = parent.window
          break
        }
        parent = parent.parent
      }

      val activity = view.context as? Activity
      val targetWindow = dialogWindow ?: activity?.window

      if (targetWindow != null) {
        val originalCallback = targetWindow.callback
        val customCallback = object : Window.Callback by originalCallback {
          override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
            if (event?.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
              when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                  val key = "Vol +"
                  val accepted = VolumeKeyBus.emitKey(key)
                  if (accepted) {
                    onVolumeKey?.invoke(key)
                  }
                  return true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                  val key = "Vol -"
                  val accepted = VolumeKeyBus.emitKey(key)
                  if (accepted) {
                    onVolumeKey?.invoke(key)
                  }
                  return true
                }
              }
            }
            return originalCallback.dispatchKeyEvent(event)
          }
        }

        targetWindow.callback = customCallback

        onDispose {
          if (targetWindow.callback === customCallback) {
            targetWindow.callback = originalCallback
          }
        }
      } else {
        onDispose {}
      }
    }
  }
}
