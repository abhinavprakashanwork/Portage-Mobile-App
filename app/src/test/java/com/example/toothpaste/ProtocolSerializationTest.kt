package com.example.toothpaste

import com.example.toothpaste.protocol.DataPacket
import com.google.protobuf.ByteString
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class ProtocolSerializationTest {
  @Test
  fun authPacketMatchesWebClientSerialization() {
    val publicKey = "AgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=".toByteArray(Charsets.UTF_8)
    val packet = DataPacket.newBuilder()
      .setPacketID(DataPacket.PacketID.AUTH_PACKET)
      .setPacketNumber(1)
      .setSlowMode(true)
      .setIv(ByteString.copyFrom(ByteArray(12)))
      .setDataLen(publicKey.size)
      .setEncryptedData(ByteString.copyFrom(publicKey))
      .setTag(ByteString.copyFrom(ByteArray(16)))
      .build()

    assertArrayEquals(
      hex("0801100120012a0c000000000000000000000000302c3a2c416741414141414141414141414141414141414141414141414141414141414141414141414141414141413d421000000000000000000000000000000000"),
      packet.toByteArray()
    )
  }

  private fun hex(value: String): ByteArray =
    value.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}