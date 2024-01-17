package com.friendly_machines.frbpdoctor.watchprotocol.bluetooth

import android.util.Log
import com.friendly_machines.frbpdoctor.watchprotocol.Crc16
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchRawResponse
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import kotlin.RuntimeException

object WatchCharacteristic {
    val writingPortCharacteristic: UUID = UUID.fromString("00000001-0000-1001-8001-00805F9B07D0")
    val notificationCharacteristic: UUID = UUID.fromString("00000002-0000-1001-8001-00805F9B07D0")
    val bigWritingPortCharacteristic: UUID = UUID.fromString("00000003-0000-1001-8001-00805F9B07D0")
    val bigNotificationCharacteristic: UUID = UUID.fromString("00000004-0000-1001-8001-00805F9B07D0")
    fun encodeWatchString(input: String): ByteArray {
        val inputChars = input.toCharArray()
        val buf = ByteBuffer.allocate(inputChars.size * 2).order(ByteOrder.BIG_ENDIAN)
        inputChars.forEach {
            buf.putChar(it)
        }
        return buf.array()
    }
    // decode var length integer; result: (decoded value, length of raw data)
    fun decodeVariableLengthInteger(buf: ByteBuffer): Int {
        var result = 0
        var basis = 1
        for (i in 0 until 4) {
            val chunk = buf.get().toInt()
            result += (chunk and 0x7F) * basis
            basis *= 0x80
            if ((chunk and 0x80) == 0) { // EOF
                return result
            }
        }
        return result
    }
    fun encodeVariableLengthInteger(input: Int): ByteArray { // protobuf and/or MIDI
        var input = input
        val buf = ByteBuffer.allocate(
            if (input < 0x80) 1
            else if (input < 0x4000) 2
            else if (input < 0x20_0000) 3
            else 4
        ) // less than 7, 14, 21 bits of payloads need differing output length
        do {
            var chunk = input % 0x80 // take lowest 7 bits
            input /= 128 // shift by 7 bits
            if (input > 0) { // still more bits left
                chunk = chunk or 0x80 // set top bit as a "continue" flag
            }
            buf.put(chunk.toByte())
        } while (input > 0)
        return buf.array()
    }
    /**
     * If packetIndex == 0, it's the first packet. Otherwise, packetIndex > 0 is requires.
     * totalMessageLen is only used if packetIndex == 0
     */
    fun encodePacket(
        packetIndex: Int,
        packetBody: ByteArray,
        totalMessageLength: Int,
    ): ByteArray {
        val rawPacketIndex = encodeVariableLengthInteger(packetIndex)
        return if (packetIndex == 0) { // first chunk
            val rawTotalMessageLength = encodeVariableLengthInteger(totalMessageLength)
            val buf = ByteBuffer.allocate(rawPacketIndex.size + rawTotalMessageLength.size + 1 + packetBody.size).order(ByteOrder.BIG_ENDIAN)
            buf.put(rawPacketIndex)
            buf.put(rawTotalMessageLength)
            buf.put((4 * 16).toByte()) // FIXME 1
            buf.put(packetBody)
            buf.array()
        } else {
            val buf = ByteBuffer.allocate(rawPacketIndex.size + packetBody.size).order(ByteOrder.BIG_ENDIAN)
            buf.put(rawPacketIndex)
            buf.put(packetBody)
            buf.array()
        }
    }
    // TODO: check
    fun encodeInternal3(
        body: ByteArray, sendingSequenceNumber: Int, type: Byte
    ): ByteArray {
        val buf = ByteBuffer.allocate(4 + 1 + 2 + 2 + body.size).order(ByteOrder.BIG_ENDIAN)
        buf.putInt(sendingSequenceNumber)
        buf.put(type)
        buf.putShort(0) // junk
        buf.putShort(body.size.toShort())
        buf.put(body)

        // Put CRC

        val buf2 = ByteBuffer.allocate(buf.limit() + 2).order(ByteOrder.BIG_ENDIAN)
        buf2.put(buf.array())
        buf2.putShort(Crc16.crc16(buf.array()))
        return buf2.array()
    }
    /** Encode the given body into a watch message body */
    fun encodeMessage(
        body: ByteArray, sendingSequenceNumber: Int, command: Short
    ): ByteArray {
        var buffer = ByteBuffer.allocate(4 + 4 + 2 + 2 + body.size).order(ByteOrder.BIG_ENDIAN)
        buffer.putInt(sendingSequenceNumber)
        buffer.putInt(0) // junk
        buffer.putShort(command)
        buffer.putShort(body.size.toShort())
        buffer.put(body)
        val rawBuffer0 = buffer.array()
        val crc = Crc16.crc16(rawBuffer0)
        buffer = ByteBuffer.allocate(buffer.limit() + 2)
        buffer.put(rawBuffer0)
        buffer.putShort(crc)
        return buffer.array()
    }
    fun decodeMessage(buf: ByteBuffer): WatchRawResponse {
        buf.order(ByteOrder.BIG_ENDIAN)
        val sequenceNumber = buf.int // generated by watch
        val ackedSequenceNumber = buf.int // That's the sequenceNumber of the package that we had sent to the watch
        val command = buf.short
        val length = buf.short.toInt()
        if (buf.hasRemaining()) {
            val rawContents = ByteArray(length)
            buf.get(rawContents)
            val lengthUntilCrcField = buf.position()
            buf.rewind()
            val everythingButCrc = ByteArray(lengthUntilCrcField)
            buf.get(everythingButCrc)
            val oldCrc = buf.short
            val newCrc = Crc16.crc16(everythingButCrc)
            if (oldCrc == newCrc) {
                Log.i(WatchCommunicator.TAG, "decode crc ok")
            } else {
                Log.e(WatchCommunicator.TAG, "decode crc mistake")
                throw RuntimeException("CRC is incorrect")
            }
            return WatchRawResponse(
                sequenceNumber, ackedSequenceNumber, command, rawContents
            )
        } else {
            throw RuntimeException("unexpected EOF")
        }
    }
    fun decodeBigMessage(buf: ByteBuffer): WatchRawResponse {
        buf.order(ByteOrder.BIG_ENDIAN)
        val sequenceNumber = buf.int // generated by watch
        buf.get() // TODO
        val command = buf.short
        val length = buf.short.toInt()
        val result = ByteArray(length)
        buf.get(result)
        // padding
        //val r = buf.remaining()
        //Log.e(TAG, "remaining $r")
        return WatchRawResponse(sequenceNumber, sequenceNumber, command, result)
    }
}