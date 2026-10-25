package com.example.toothpaste

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class StoredToothKeys(
  val sharedSecret: ByteArray,
  val publicKey: ByteArray
)

class ToothKeyStore(context: Context) {
  private val preferences = context.getSharedPreferences("toothpaste_keys", Context.MODE_PRIVATE)
  private val keyAlias = "toothpaste-key-store"

  fun get(deviceId: String): StoredToothKeys? {
    val encoded = preferences.getString(recordKey(deviceId), null) ?: return null
    val stored = ToothCrypto.decodeBase64(encoded)
    require(stored.size > 12) { "Corrupt stored ToothPaste key record" }
    val plaintext = decrypt(stored.copyOfRange(0, 12), stored.copyOfRange(12, stored.size))
    val secretLength = plaintext.first().toInt() and 0xff
    require(secretLength == 32 && plaintext.size == 1 + secretLength + 65) {
      "Corrupt stored ToothPaste key lengths"
    }
    return StoredToothKeys(
      sharedSecret = plaintext.copyOfRange(1, 1 + secretLength),
      publicKey = plaintext.copyOfRange(1 + secretLength, plaintext.size)
    )
  }

  fun put(deviceId: String, keys: StoredToothKeys) {
    require(keys.sharedSecret.size == 32 && keys.publicKey.size == 65)
    val plaintext = byteArrayOf(keys.sharedSecret.size.toByte()) + keys.sharedSecret + keys.publicKey
    preferences.edit().putString(recordKey(deviceId), ToothCrypto.encodeBase64(encrypt(plaintext))).apply()
  }

  fun delete(deviceId: String) {
    preferences.edit().remove(recordKey(deviceId)).apply()
  }

  fun clearAll() {
    preferences.edit().clear().apply()
  }

  private fun recordKey(deviceId: String): String =
    "device_${deviceId.replace(Regex("[^A-Za-z0-9]"), "_")}"

  private fun secretKey(): SecretKey {
    val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    if (!keyStore.containsAlias(keyAlias)) {
      val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
      generator.init(
        KeyGenParameterSpec.Builder(
          keyAlias,
          KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
          .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
          .build()
      )
      generator.generateKey()
    }
    return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
  }

  private fun encrypt(plaintext: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey())
    val ciphertext = cipher.doFinal(plaintext)
    return cipher.iv + ciphertext
  }

  private fun decrypt(iv: ByteArray, encrypted: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
    return cipher.doFinal(encrypted)
  }
}
