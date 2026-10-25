package com.example.toothpaste

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ToothCryptoTest {
  @Test
  fun hkdfMatchesRfc5869TestVectorOne() {
    val ikm = hex("0b".repeat(22))
    val salt = hex("000102030405060708090a0b0c")
    val info = hex("f0f1f2f3f4f5f6f7f8f9")
    val expected = hex(
      "3cb25f25faacd57a90434f64d0362f2a" +
        "2d2d0a90cf1a5a4c5db02d56ecc4c5bf" +
        "34007208d5b887185865"
    )

    assertArrayEquals(expected, ToothCrypto.hkdfSha256(ikm, salt, info, 42))
  }

  @Test
  fun aesGcmRoundTripSplitsJavaCiphertextAndTag() {
    val key = hex("7d70f1c988bf3c5aa77e367881853e79dbe61d2c1e92e27549ac6bad61755ab8")
    val iv = hex("1af38c2dc2b96ffdd8669409")
    val plaintext = "ToothPaste interoperability".toByteArray()

    val encrypted = ToothCrypto.aesGcmEncrypt(key, plaintext, iv)

    assertEquals(16, encrypted.tag.size)
    assertArrayEquals(
      hex("4258dc50fa8129a90a5ddc023dee3572742acef27505db339e56f4d7f9e48c54d843c1fca8c4eadb03b6ad"),
      encrypted.ciphertext + encrypted.tag
    )
    assertArrayEquals(plaintext, ToothCrypto.aesGcmDecrypt(key, encrypted))
  }

  @Test
  fun p256CompressionRoundTripsManyKeysIncludingLeadingZeroCoordinates() {
    var zeroCoordinateCases = 0
    repeat(2000) {
      val generated = ToothCrypto.generateKeyPair()
      val publicKey = generated.publicKey
      val compressed = ToothCrypto.compressPublicKey(publicKey)
      assertArrayEquals(publicKey, ToothCrypto.decompressPublicKey(compressed))
      if (publicKey[1] == 0.toByte() || publicKey[33] == 0.toByte()) zeroCoordinateCases++
    }
    assertTrue("Expected at least one leading-zero coordinate", zeroCoordinateCases > 0)
  }

  @Test
  fun ecdhAgreementMatchesForBothParties() {
    val alice = ToothCrypto.generateKeyPair()
    val bob = ToothCrypto.generateKeyPair()

    val aliceSecret = ToothCrypto.deriveSharedSecret(alice.keyPair, bob.publicKey)
    val bobSecret = ToothCrypto.deriveSharedSecret(bob.keyPair, alice.publicKey)

    assertArrayEquals(aliceSecret, bobSecret)
    assertEquals(32, aliceSecret.size)
  }

  @Test
  fun base64MatchesExpectedWebEncoding() {
    val publicKey = hex("02" + "00".repeat(32))
    assertEquals("AgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", ToothCrypto.encodeBase64(publicKey))
    assertArrayEquals(publicKey, ToothCrypto.decodeBase64(ToothCrypto.encodeBase64(publicKey)))
  }

  @Test
  fun hkdfMatchesNodeWebCryptoFixture() {
    val secret = hex("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f")
    val salt = hex("a0a1a2a3a4a5a6a7a8a9aaabacadaeaf")
    val expected = hex("7d70f1c988bf3c5aa77e367881853e79dbe61d2c1e92e27549ac6bad61755ab8")
    assertArrayEquals(expected, ToothCrypto.hkdfSha256(secret, salt, "aes-gcm-256".toByteArray()))
  }

  @Test
  fun esp32PairingKeyDecompressionTest() {
    val keyBase64 = "Avn3ddD0nOOOeMBICqFGY8jC565fNgcGMO4L25N3yKvi"
    val decoded = ToothCrypto.decodeBase64(keyBase64)
    assertEquals(33, decoded.size)
    assertEquals(2, decoded[0].toInt())
    val decompressed = ToothCrypto.decompressPublicKey(decoded)
    assertEquals(65, decompressed.size)
    assertEquals(4, decompressed[0].toInt())

    val alice = ToothCrypto.generateKeyPair()
    val secret = ToothCrypto.deriveSharedSecret(alice.keyPair, decompressed)
    assertEquals(32, secret.size)
  }

  private fun hex(value: String): ByteArray {
    return value.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
  }
}