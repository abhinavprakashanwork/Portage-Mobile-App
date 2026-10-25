package com.example.toothpaste

import android.os.Build
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPublicKeySpec
import java.security.interfaces.ECPublicKey
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Log

data class AesGcmCiphertext(
  val iv: ByteArray,
  val ciphertext: ByteArray,
  val tag: ByteArray
)

data class ToothKeyPair(
  val keyPair: KeyPair,
  val publicKey: ByteArray
)

object ToothCrypto {
  private const val P256_SIZE = 32
  private const val UNCOMPRESSED_SIZE = 65
  private const val COMPRESSED_SIZE = 33
  private val P = BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16)
  private val B = BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16)
  private val THREE = BigInteger.valueOf(3)

  fun generateKeyPair(): ToothKeyPair {
    val generator = KeyPairGenerator.getInstance("EC")
    generator.initialize(ECGenParameterSpec("secp256r1"))
    val keyPair = generator.generateKeyPair()
    val publicKey = keyPair.public as ECPublicKey
    return ToothKeyPair(keyPair, encodeUncompressed(publicKey.w.affineX, publicKey.w.affineY))
  }

  fun compressPublicKey(uncompressed: ByteArray): ByteArray {
    require(uncompressed.size == UNCOMPRESSED_SIZE && uncompressed[0] == 4.toByte()) {
      "Expected a 65-byte uncompressed P-256 public key"
    }
    val y = BigInteger(1, uncompressed.copyOfRange(33, 65))
    return byteArrayOf(if (y.testBit(0)) 3 else 2) + uncompressed.copyOfRange(1, 33)
  }

  fun decompressPublicKey(compressed: ByteArray): ByteArray {
    Log.d(
      "TP_PAIRING",
      "Decompress input length=${compressed.size} firstByte=${
        compressed.firstOrNull()?.toUByte()?.toString(16)?.padStart(2, '0') ?: "none"
      }"
    )
    require(compressed.size == COMPRESSED_SIZE && (compressed[0].toInt() == 2 || compressed[0].toInt() == 3)) {
      "Expected a 33-byte compressed P-256 public key"
    }
    val x = BigInteger(1, compressed.copyOfRange(1, COMPRESSED_SIZE))
    Log.d("TP_PAIRING", "Decompress X coordinate length=${P256_SIZE}")
    require(x < P) { "P-256 X coordinate is out of range" }

    val x3 = safeModPow(x, THREE, P)
    val threeX = THREE.multiply(x)
    val rawAlpha = x3.subtract(threeX).add(B)
    val alpha = rawAlpha.mod(P)

    val exponent = P.add(BigInteger.ONE).shiftRight(2)
    val y = safeModPow(alpha, exponent, P)
    val ySquaredModP = y.multiply(y).mod(P)

    Log.d(
      "TP_PAIRING",
      "API=${Build.VERSION.SDK_INT} " +
        "ySqEqAlpha=${ySquaredModP == alpha}"
    )

    var calculatedY = y
    require(ySquaredModP == alpha) { "Invalid P-256 public key point" }
    if (calculatedY.testBit(0) != (compressed[0].toInt() == 3)) calculatedY = P.subtract(calculatedY)
    return encodeUncompressed(x, calculatedY).also {
      Log.d(
        "TP_PAIRING",
        "Decompress output length=${it.size} firstByte=${
          it.firstOrNull()?.toUByte()?.toString(16)?.padStart(2, '0') ?: "none"
        }"
      )
    }
  }

  private fun safeModPow(base: BigInteger, exponent: BigInteger, m: BigInteger): BigInteger {
    var result = BigInteger.ONE
    val baseMod = base.mod(m)
    val bitLength = exponent.bitLength()
    for (i in bitLength - 1 downTo 0) {
      result = result.multiply(result).mod(m)
      if (exponent.testBit(i)) {
        result = result.multiply(baseMod).mod(m)
      }
    }
    return result
  }

  fun deriveSharedSecret(keyPair: KeyPair, peerUncompressed: ByteArray): ByteArray {
    require(peerUncompressed.size == UNCOMPRESSED_SIZE && peerUncompressed[0] == 4.toByte()) {
      "Expected a 65-byte uncompressed P-256 public key"
    }
    val ownPublic = keyPair.public as ECPublicKey
    val point = java.security.spec.ECPoint(
      BigInteger(1, peerUncompressed.copyOfRange(1, 33)),
      BigInteger(1, peerUncompressed.copyOfRange(33, 65))
    )
    Log.d(
      "TP_PAIRING",
      "EC point input length=${peerUncompressed.size} firstByte=${
        peerUncompressed.firstOrNull()?.toUByte()?.toString(16)?.padStart(2, '0') ?: "none"
      } xLength=${P256_SIZE} yLength=${P256_SIZE}"
    )
    val peerSpec = ECPublicKeySpec(point, ownPublic.params as ECParameterSpec)
    val peerKey = KeyFactory.getInstance("EC").generatePublic(peerSpec)
    val agreement = KeyAgreement.getInstance("ECDH")
    agreement.init(keyPair.private)
    agreement.doPhase(peerKey, true)
    return agreement.generateSecret().let { fixedSize(it, P256_SIZE) }
  }

  fun hkdfSha256(ikm: ByteArray, salt: ByteArray, info: ByteArray, length: Int = 32): ByteArray {
    require(length in 1..255 * 32)
    val actualSalt = if (salt.isEmpty()) ByteArray(32) else salt
    val extract = Mac.getInstance("HmacSHA256")
    extract.init(SecretKeySpec(actualSalt, "HmacSHA256"))
    val prk = extract.doFinal(ikm)
    val output = ByteArray(length)
    var previous = ByteArray(0)
    var offset = 0
    var counter = 1
    while (offset < length) {
      val expand = Mac.getInstance("HmacSHA256")
      expand.init(SecretKeySpec(prk, "HmacSHA256"))
      expand.update(previous)
      expand.update(info)
      expand.update(counter.toByte())
      previous = expand.doFinal()
      val copied = minOf(previous.size, length - offset)
      previous.copyInto(output, offset, 0, copied)
      offset += copied
      counter++
    }
    return output
  }

  fun aesGcmEncrypt(key: ByteArray, plaintext: ByteArray, iv: ByteArray = randomBytes(12)): AesGcmCiphertext {
    require(key.size == 32 && iv.size == 12)
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
    val combined = cipher.doFinal(plaintext)
    val split = combined.size - 16
    return AesGcmCiphertext(iv.copyOf(), combined.copyOfRange(0, split), combined.copyOfRange(split, combined.size))
  }

  fun aesGcmDecrypt(key: ByteArray, encrypted: AesGcmCiphertext): ByteArray {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, encrypted.iv))
    return cipher.doFinal(encrypted.ciphertext + encrypted.tag)
  }

  fun encodeBase64(bytes: ByteArray): String = Base64Codec.encode(bytes)

  fun decodeBase64(value: String): ByteArray = Base64Codec.decode(value)

  fun fixedSize(value: ByteArray, size: Int): ByteArray {
    require(value.size <= size) { "Value is larger than requested size" }
    return ByteArray(size).also { value.copyInto(it, size - value.size) }
  }

  fun randomBytes(size: Int): ByteArray = ByteArray(size).also { java.security.SecureRandom().nextBytes(it) }

  private fun encodeUncompressed(x: BigInteger, y: BigInteger): ByteArray {
    return byteArrayOf(4) + fixedSize(x.toByteArray().stripSignByte(), P256_SIZE) +
      fixedSize(y.toByteArray().stripSignByte(), P256_SIZE)
  }

  private fun ByteArray.stripSignByte(): ByteArray {
    return if (size == P256_SIZE + 1 && first() == 0.toByte()) copyOfRange(1, size) else this
  }
}

private object Base64Codec {
  private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

  fun encode(bytes: ByteArray): String {
    val result = StringBuilder((bytes.size + 2) / 3 * 4)
    var index = 0
    while (index < bytes.size) {
      val first = bytes[index++].toInt() and 0xff
      val second = if (index < bytes.size) bytes[index++].toInt() and 0xff else -1
      val third = if (index < bytes.size) bytes[index++].toInt() and 0xff else -1
      result.append(ALPHABET[first ushr 2])
      result.append(ALPHABET[((first and 3) shl 4) or if (second >= 0) second ushr 4 else 0])
      result.append(if (second >= 0) ALPHABET[((second and 15) shl 2) or if (third >= 0) third ushr 6 else 0] else '=')
      result.append(if (third >= 0) ALPHABET[third and 63] else '=')
    }
    return result.toString()
  }

  fun decode(value: String): ByteArray {
    val normalized = value.trim()
    Log.d(
      "TP_PAIRING",
      "Base64 input length=${value.length} normalizedLength=${normalized.length} containsWhitespace=${value.any(Char::isWhitespace)}"
    )
    require(normalized.isNotEmpty() && normalized.length % 4 == 0) { "Invalid base64 length" }
    val result = ArrayList<Byte>(normalized.length * 3 / 4)
    var index = 0
    while (index < normalized.length) {
      val a = valueOf(normalized[index++])
      val b = valueOf(normalized[index++])
      val cChar = normalized[index++]
      val dChar = normalized[index++]
      val c = if (cChar == '=') 0 else valueOf(cChar)
      val d = if (dChar == '=') 0 else valueOf(dChar)
      result.add(((a shl 2) or (b ushr 4)).toByte())
      if (cChar != '=') result.add((((b and 15) shl 4) or (c ushr 2)).toByte())
      if (dChar != '=') result.add((((c and 3) shl 6) or d).toByte())
    }
    return result.toByteArray()
  }

  private fun valueOf(char: Char): Int {
    val value = ALPHABET.indexOf(char)
    require(value >= 0) { "Invalid base64 character: $char" }
    return value
  }
}