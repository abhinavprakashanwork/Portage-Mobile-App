package com.example.toothpaste

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.toothpaste.protocol.ResponsePacket
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.UUID

data class ToothBleConnection(
  val deviceId: String,
  val deviceName: String?,
  val gatt: BluetoothGatt,
  val packetCharacteristic: BluetoothGattCharacteristic,
  val pubKeyBytes: ByteArray?
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as ToothBleConnection
    if (deviceId != other.deviceId) return false
    if (deviceName != other.deviceName) return false
    if (gatt != other.gatt) return false
    if (packetCharacteristic != other.packetCharacteristic) return false
    if (pubKeyBytes != null) {
      if (other.pubKeyBytes == null) return false
      if (!pubKeyBytes.contentEquals(other.pubKeyBytes)) return false
    } else if (other.pubKeyBytes != null) return false
    return true
  }

  override fun hashCode(): Int {
    var result = deviceId.hashCode()
    result = 31 * result + (deviceName?.hashCode() ?: 0)
    result = 31 * result + gatt.hashCode()
    result = 31 * result + packetCharacteristic.hashCode()
    result = 31 * result + (pubKeyBytes?.contentHashCode() ?: 0)
    return result
  }
}

class BleTransport(private val context: Context) {
  companion object {
    const val SERVICE_UUID = "19b10000-e8f2-537e-4f6c-d104768a1214"
    const val PACKET_UUID = "6856e119-2c7b-455a-bf42-cf7ddd2c5907"
    const val RESPONSE_UUID = "6856e119-2c7b-455a-bf42-cf7ddd2c5908"
    const val MAC_UUID = "19b10002-e8f2-537e-4f6c-d104768a1214"
    const val PUBKEY_UUID = "19b10003-e8f2-537e-4f6c-d104768a1214"
    const val FIRMWARE_MINIMUM = "0.9.0"
    private const val TAG = "TP"
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
  }

  private val operationMutex = Mutex()
  private val responseEvents = MutableSharedFlow<ResponsePacket>(extraBufferCapacity = 16)
  private var gatt: BluetoothGatt? = null
  private var packetCharacteristic: BluetoothGattCharacteristic? = null
  private var connectionResult: CompletableDeferred<Int>? = null
  private var operationResult: CompletableDeferred<ByteArray?>? = null
  var onDisconnected: (() -> Unit)? = null

  val responses: Flow<ResponsePacket> = responseEvents.asSharedFlow()

  suspend fun connect(): ToothBleConnection = operationMutex.withLock {
    requireBluetoothPermissions()
    disconnect()
    val device = scanForDevice()
    val connectedGatt = connectGatt(device)
    Log.d(TAG, "Requesting MTU 247")
    awaitOperation { connectedGatt.requestMtu(247) }
    awaitOperation { connectedGatt.discoverServices() }
    val service = connectedGatt.getService(UUID.fromString(SERVICE_UUID))
      ?: error("ToothPaste service not found")
    val packet = service.getCharacteristic(UUID.fromString(PACKET_UUID))
      ?: error("ToothPaste packet characteristic not found")
    val response = service.getCharacteristic(UUID.fromString(RESPONSE_UUID))
      ?: error("ToothPaste response characteristic not found")
    val mac = service.getCharacteristic(UUID.fromString(MAC_UUID))
      ?: error("ToothPaste MAC characteristic not found")
    val pubKeyChar = service.getCharacteristic(UUID.fromString(PUBKEY_UUID))

    check(connectedGatt.setCharacteristicNotification(response, true)) {
      "Could not enable response notifications"
    }
    val cccd = response.getDescriptor(CCCD_UUID) ?: error("Response CCCD not found")
    cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
    awaitOperation { connectedGatt.writeDescriptor(cccd) }
    val macBytes = awaitOperation { connectedGatt.readCharacteristic(mac) }
      ?: error("MAC characteristic returned no data")
    val pubKeyBytes = pubKeyChar?.let { try { awaitOperation { connectedGatt.readCharacteristic(it) } } catch (_: Exception) { null } }
    val deviceId = macBytes.joinToString("") { "%02x".format(it) }
    Log.d(TAG, "Connected deviceId=$deviceId pubKeyBytes=${pubKeyBytes?.size}")
    gatt = connectedGatt
    packetCharacteristic = packet
    ToothBleConnection(deviceId, device.name, connectedGatt, packet, pubKeyBytes)
  }

  suspend fun writeWithoutResponse(bytes: ByteArray) = operationMutex.withLock {
    val currentGatt = gatt ?: error("ToothPaste is not connected")
    val characteristic = packetCharacteristic ?: error("Packet characteristic unavailable")
    check(bytes.size < 253) { "DataPacket exceeds BLE payload limit" }
    Log.d(TAG, "Writing packet bytes=${bytes.size}")
    characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
    characteristic.value = bytes
    awaitOperation { currentGatt.writeCharacteristic(characteristic) }
  }

  fun disconnect() {
    val connectedGatt = gatt
    gatt = null
    packetCharacteristic = null
    connectedGatt?.disconnect()
    connectedGatt?.close()
  }

  private suspend fun scanForDevice(): BluetoothDevice = withContext(Dispatchers.Main.immediate) {
    val scanner = bluetoothAdapter().bluetoothLeScanner ?: error("Bluetooth LE scanner unavailable")
    val result = CompletableDeferred<BluetoothDevice>()
    val callback = object : ScanCallback() {
      override fun onScanResult(callbackType: Int, scanResult: ScanResult) {
        if (!result.isCompleted) result.complete(scanResult.device)
      }

      override fun onScanFailed(errorCode: Int) {
        if (!result.isCompleted) result.completeExceptionally(Exception("BLE scan failed: $errorCode"))
      }
    }
    val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
    val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(SERVICE_UUID)).build()
    scanner.startScan(listOf(filter), settings, callback)
    try {
      withTimeout(10_000) { result.await() }
    } finally {
      scanner.stopScan(callback)
    }
  }

  private suspend fun connectGatt(device: BluetoothDevice): BluetoothGatt {
    val connection = CompletableDeferred<Int>()
    connectionResult = connection
    val connectedGatt = device.connectGatt(context, false, callback(), BluetoothDevice.TRANSPORT_LE)
      ?: error("Could not create GATT connection")
    gatt = connectedGatt
    return try {
      withTimeout(10_000) { connection.await() }
      connectedGatt
    } catch (error: Throwable) {
      connectedGatt.close()
      throw error
    } finally {
      connectionResult = null
    }
  }

  private fun callback() = object : BluetoothGattCallback() {
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
      if (this@BleTransport.gatt !== gatt) return
      if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
        Log.d(TAG, "GATT connected")
        connectionResult?.complete(status)
      } else {
        connectionResult?.completeExceptionally(Exception("GATT connection failed: status=$status state=$newState"))
        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
          this@BleTransport.gatt = null
          packetCharacteristic = null
          gatt.close()
          onDisconnected?.invoke()
        }
      }
    }

    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
      Log.d(TAG, "MTU callback mtu=$mtu status=$status")
      completeOperation(status, null)
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) = completeOperation(status, null)

    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
      completeOperation(status, if (status == BluetoothGatt.GATT_SUCCESS) characteristic.value else null)
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
      completeOperation(status, null)
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
      completeOperation(status, null)
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
      if (characteristic.uuid != UUID.fromString(RESPONSE_UUID)) return
      runCatching { ResponsePacket.parseFrom(characteristic.value) }
        .onSuccess { response ->
          Log.d(TAG, "Notify response=${response.responseType} firmware=${response.firmwareVersion}")
          responseEvents.tryEmit(response)
        }
        .onFailure { Log.e(TAG, "Invalid ResponsePacket", it) }
    }
  }

  private suspend fun awaitOperation(operation: () -> Boolean): ByteArray? {
    val result = CompletableDeferred<ByteArray?>()
    operationResult = result
    check(operation()) { "GATT operation could not start" }
    return try {
      withTimeout(5_000) { result.await() }
    } finally {
      if (operationResult === result) operationResult = null
    }
  }

  private fun completeOperation(status: Int, value: ByteArray?) {
    val result = operationResult ?: return
    if (status == BluetoothGatt.GATT_SUCCESS) result.complete(value)
    else result.completeExceptionally(Exception("GATT operation failed: status=$status"))
  }

  private fun bluetoothAdapter(): BluetoothAdapter {
    val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    return manager.adapter ?: error("Bluetooth adapter unavailable")
  }

  private fun requireBluetoothPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      check(ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
        "Bluetooth scan permission is required. Please grant Bluetooth permissions."
      }
      check(ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
        "Bluetooth connect permission is required. Please grant Bluetooth permissions."
      }
    } else {
      check(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        "Location permission is required for Bluetooth scanning."
      }
    }
  }
}
