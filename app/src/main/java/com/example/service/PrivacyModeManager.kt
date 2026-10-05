package com.example.service

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the global "Privacy Mode" state across the Portage applet.
 * Persists preference using SharedPreferences key "portage_privacy_mode".
 *
 * When active:
 * - Text input is masked as password dots.
 * - History transmissions are masked as "••••••••".
 * - Incoming live keystrokes are masked as "•" for printable characters (non-printables remain visible).
 * - Sent indicator at the bottom of the card is masked with "•".
 * - Canvas indicator displays "[EyeOff] Privacy Mode".
 * - Transmissions to the device continue accurately with real data.
 */
object PrivacyModeManager {
  private const val PREFS_NAME = "portage_prefs"
  const val KEY_PRIVACY_MODE = "portage_privacy_mode"

  private val _isPrivacyMode = MutableStateFlow(false)
  val isPrivacyMode: StateFlow<Boolean> = _isPrivacyMode.asStateFlow()

  @Volatile
  private var initialized = false

  fun init(context: Context) {
    if (!initialized) {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      _isPrivacyMode.value = prefs.getBoolean(KEY_PRIVACY_MODE, false)
      initialized = true
    }
  }

  fun toggle(context: Context): Boolean {
    init(context)
    val next = !_isPrivacyMode.value
    _isPrivacyMode.value = next
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putBoolean(KEY_PRIVACY_MODE, next)
      .apply()
    return next
  }

  fun set(context: Context, enabled: Boolean) {
    init(context)
    _isPrivacyMode.value = enabled
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putBoolean(KEY_PRIVACY_MODE, enabled)
      .apply()
  }

  /**
   * Formats a single key or shortcut for display under Privacy Mode.
   * Modifiers (CTRL, ALT, etc.), shortcuts (Ctrl+C, etc.), and printable characters are masked.
   * Only physical mouse events remain as their non-secret identifiers.
   */
  fun formatKey(key: String?, active: Boolean = _isPrivacyMode.value): String {
    if (key == null) return ""
    if (!active) return key

    val trimmed = key.trim()
    val upper = trimmed.uppercase()

    // Physical mouse click actions on trackpad
    if (upper == "LEFT" || upper == "RIGHT" || upper == "DOUBLE") {
      return key
    }

    // Modifier keys like Ctrl, Alt, Win, Shift, etc. are hidden under privacy mode
    val modifierOrControlKeys = setOf(
      "CTRL", "CONTROL", "ALT", "SHIFT", "WIN", "META", "CMD", "COMMAND", "OPTION",
      "ENTER", "ESC", "ESCAPE", "TAB", "BACKSPACE", "DELETE", "DEL",
      "CAPS LOCK", "CAPSLOCK", "PRINTSCREEN", "INSERT", "HOME", "END", "PAGEUP", "PAGEDOWN",
      "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12"
    )

    if (upper in modifierOrControlKeys) {
      return "••••"
    }

    // Any combination shortcut like "Ctrl + C", "Alt + F4", "Ctrl+V" is masked completely
    if (trimmed.contains("+")) {
      return "••••"
    }

    // Default for any printable single letter, symbol, number, or space
    return "•"
  }

  /**
   * Formats history / transmission preview text when Privacy Mode is active.
   */
  fun maskTransmissionText(text: String, active: Boolean = _isPrivacyMode.value): String {
    if (!active) return text
    if (text.isEmpty()) return ""
    return "••••••••"
  }
}
