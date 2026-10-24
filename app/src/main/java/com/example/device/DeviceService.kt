package com.example.device

import android.content.Context
import android.util.Log
import com.example.toothpaste.BleTransport
import com.example.toothpaste.StoredToothKeys
import com.example.toothpaste.ToothCrypto
import com.example.toothpaste.ToothKeyStore
import com.example.toothpaste.protocol.DataPacket
import com.example.toothpaste.protocol.EncryptedData
import com.example.toothpaste.protocol.KeyboardPacket
import com.example.toothpaste.protocol.ResponsePacket
import com.google.protobuf.ByteString
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

sealed class DeviceConnectionState {
  data object DISCONNECTED : DeviceConnectionState()
  data object SCANNING : DeviceConnectionState()
  data object CONNECTING : DeviceConnectionState()
  data object NEEDS_PAIRING : DeviceConnectionState()
  data object PAIRING : DeviceConnectionState()
  data object READY : DeviceConnectionState()
  data object UNSUPPORTED : DeviceConnectionState()
  data class ERROR(val reason: String) : DeviceConnectionState()
}

class DeviceService(context: Context) {

  companion object {
    private const val TAG = "TP"
    private const val AUTH_DELAY_MS = 5_000L
    private const val RESPONSE_TIMEOUT_MS = 10_000L
  }

  private val _events = MutableSharedFlow<DeviceEvent>()
  val events: SharedFlow<DeviceEvent> = _events.asSharedFlow()
  private val _connectionState = MutableStateFlow<DeviceConnectionState>(DeviceConnectionState.DISCONNECTED)
  val connectionState: StateFlow<DeviceConnectionState> = _connectionState.asStateFlow()

  private val transport = BleTransport(context.applicationContext)
  private val keyStore = ToothKeyStore(context.applicationContext)
  private val connectionMutex = Mutex()
  private var connection: com.example.toothpaste.ToothBleConnection? = null
  private var storedKeys: StoredToothKeys? = null
  private var sessionKey: ByteArray? = null

  init {
    transport.onDisconnected = {
      connection = null
      storedKeys = null
      sessionKey = null
      transition(DeviceConnectionState.DISCONNECTED)
    }
  }

  suspend fun sendText(text: String): Boolean {
    delay(250) // simulate USB HID packet transmission
    _events.emit(DeviceEvent.TextSent(text))
    return true
  }

  suspend fun connect(): Boolean = connectionMutex.withLock {
    if (_connectionState.value == DeviceConnectionState.READY) return@withLock true
    try {
      transition(DeviceConnectionState.SCANNING)
      val connected = transport.connect()
      connection = connected
      val keys = keyStore.get(connected.deviceId)
      storedKeys = keys
      if (keys == null) {
        transition(DeviceConnectionState.NEEDS_PAIRING)
      } else {
        authenticate(keys.publicKey)
      }
      _connectionState.value == DeviceConnectionState.READY ||
        _connectionState.value == DeviceConnectionState.NEEDS_PAIRING
    } catch (error: CancellationException) {
      transport.disconnect()
      connection = null
      storedKeys = null
      sessionKey = null
      transition(DeviceConnectionState.DISCONNECTED)
      throw error
    } catch (error: Exception) {
      transport.disconnect()
      connection = null
      storedKeys = null
      sessionKey = null
      transition(DeviceConnectionState.ERROR(error.message ?: "Connection failed"))
      false
    }
  }

  suspend fun pair(keyBase64: String): Boolean {
    return runCatching {
      val compressed = ToothCrypto.decodeBase64(keyBase64.trim())
      require(compressed.size == 33) { "Pairing key must decode to 33 bytes" }
      val peerPublicKey = ToothCrypto.decompressPublicKey(compressed)
      val generated = ToothCrypto.generateKeyPair()
      val secret = ToothCrypto.deriveSharedSecret(generated.keyPair, peerPublicKey)
      val connected = connection ?: error("Connect to a ToothPaste first")
      keyStore.put(connected.deviceId, StoredToothKeys(secret, generated.publicKey))
      storedKeys = StoredToothKeys(secret, generated.publicKey)
      transition(DeviceConnectionState.PAIRING)
      delay(AUTH_DELAY_MS)
      authenticate(generated.publicKey)
      true
    }.getOrElse { error ->
      transition(DeviceConnectionState.ERROR(error.message ?: "Pairing failed"))
      false
    }
  }

  suspend fun sendTestString(text: String): Boolean {
    return runCatching {
      check(connectionState.value == DeviceConnectionState.READY) { "ToothPaste is not ready" }
      val key = sessionKey ?: error("No active ToothPaste session")
      val keyboard = KeyboardPacket.newBuilder()
        .setMessage(text)
        .setLength(text.length)
        .build()
      val encryptedData = EncryptedData.newBuilder()
        .setPacketType(EncryptedData.PacketType.KEYBOARD_STRING)
        .setKeyboardPacket(keyboard)
        .build()
      val encrypted = ToothCrypto.aesGcmEncrypt(key, encryptedData.toByteArray())
      val packet = DataPacket.newBuilder()
        .setPacketID(DataPacket.PacketID.DATA_PACKET)
        .setPacketNumber(1)
        .setTotalPackets(1)
        .setSlowMode(true)
        .setIv(ByteString.copyFrom(encrypted.iv))
        .setDataLen(encrypted.ciphertext.size)
        .setEncryptedData(ByteString.copyFrom(encrypted.ciphertext))
        .setTag(ByteString.copyFrom(encrypted.tag))
        .build()
      val bytes = packet.toByteArray()
      Log.d(TAG, "Test string packet bytes=${bytes.size} payload=${encrypted.ciphertext.size}")
      transport.writeWithoutResponse(bytes)
      true
    }.getOrElse { error ->
      Log.e(TAG, "Test string failed", error)
      false
    }
  }

  fun disconnect() {
    transport.disconnect()
    connection = null
    storedKeys = null
    sessionKey = null
    transition(DeviceConnectionState.DISCONNECTED)
  }

  fun forgetDevice() {
    disconnect()
    keyStore.clearAll()
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

  private suspend fun authenticate(publicKey: ByteArray) {
    val response = awaitResponseAfter {
      val encodedPublicKey = ToothCrypto.encodeBase64(publicKey).toByteArray(Charsets.UTF_8)
      val packet = DataPacket.newBuilder()
        .setPacketID(DataPacket.PacketID.AUTH_PACKET)
        .setPacketNumber(1)
        .setSlowMode(true)
        .setIv(ByteString.copyFrom(ByteArray(12)))
        .setDataLen(encodedPublicKey.size)
        .setEncryptedData(ByteString.copyFrom(encodedPublicKey))
        .setTag(ByteString.copyFrom(ByteArray(16)))
        .build()
      val bytes = packet.toByteArray()
      Log.d(TAG, "AUTH packet bytes=${bytes.size} payload=${encodedPublicKey.size}")
      transport.writeWithoutResponse(bytes)
    }
    val firmware = response.firmwareVersion
    if (!isFirmwareCompatible(firmware)) {
      transition(DeviceConnectionState.UNSUPPORTED)
      return
    }
    when (response.responseType) {
      ResponsePacket.ResponseType.CHALLENGE -> {
        val keys = storedKeys ?: error("No stored keys for challenge")
        sessionKey = ToothCrypto.hkdfSha256(keys.sharedSecret, response.challengeData.toByteArray(), "aes-gcm-256".toByteArray())
        transition(DeviceConnectionState.READY)
      }
      ResponsePacket.ResponseType.PEER_KNOWN -> {
        transition(DeviceConnectionState.READY)
      }
      ResponsePacket.ResponseType.PEER_UNKNOWN -> {
        val deviceId = connection?.deviceId
        if (deviceId != null) keyStore.delete(deviceId)
        storedKeys = null
        sessionKey = null
        transition(DeviceConnectionState.NEEDS_PAIRING)
      }
      else -> error("Unexpected ToothPaste response: ${response.responseType}")
    }
  }

  private suspend fun awaitResponseAfter(send: suspend () -> Unit): ResponsePacket {
    return coroutineScope {
      val response = async {
        withTimeout(RESPONSE_TIMEOUT_MS) {
          transport.responses.first { it.responseType != ResponsePacket.ResponseType.KEEPALIVE }
        }
      }
      send()
      response.await()
    }
  }

  private fun isFirmwareCompatible(version: String): Boolean {
    val actual = version.split('.', limit = 3).mapNotNull { it.takeWhile(Char::isDigit).toIntOrNull() }
    val minimum = listOf(0, 9, 0)
    return actual.size == 3 && actual.zip(minimum).firstOrNull { it.first != it.second }?.let { it.first > it.second } ?: (actual.size == 3)
  }

  private fun transition(state: DeviceConnectionState) {
    Log.d(TAG, "State ${_connectionState.value} -> $state")
    _connectionState.value = state
  }
}
