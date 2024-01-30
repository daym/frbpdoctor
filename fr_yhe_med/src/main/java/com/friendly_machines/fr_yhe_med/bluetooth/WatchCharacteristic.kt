package com.friendly_machines.fr_yhe_med.bluetooth

import android.bluetooth.le.ScanFilter
import android.companion.BluetoothLeDeviceFilter
import android.companion.DeviceFilter
import android.os.ParcelUuid
import android.util.Log
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchMessageDecodingException
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchRawResponse
import com.friendly_machines.fr_yhe_med.Crc16
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

object WatchCharacteristic {
    internal val serviceUuid: ParcelUuid = ParcelUuid.fromString("0000FE51-0000-1000-8000-00805F9B34FB")
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
    private fun encodeVariableLengthInteger(input: Int): ByteArray { // protobuf and/or MIDI
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
    fun encodeFirstPacket(
        packetIndex: Int,
        packetBody: ByteArray,
        totalMessageLength: Int,
    ): ByteArray {
        assert(packetIndex == 0)
        val rawPacketIndex = encodeVariableLengthInteger(packetIndex)
        val rawTotalMessageLength = encodeVariableLengthInteger(totalMessageLength)
        val buf = ByteBuffer.allocate(rawPacketIndex.size + rawTotalMessageLength.size + 1 + packetBody.size).order(ByteOrder.BIG_ENDIAN)
        buf.put(rawPacketIndex)
        buf.put(rawTotalMessageLength)
        buf.put((4 * 16).toByte()) // FIXME 1
        buf.put(packetBody)
        return buf.array()
    }
    /**
     * packetIndex > 0 is required.
     */
    fun encodeMiddlePacket(
        packetIndex: Int,
        packetBody: ByteArray,
    ): ByteArray {
        val rawPacketIndex = encodeVariableLengthInteger(packetIndex)
        val buf = ByteBuffer.allocate(rawPacketIndex.size + packetBody.size).order(ByteOrder.BIG_ENDIAN)
        buf.put(rawPacketIndex)
        buf.put(packetBody)
        return buf.array()
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
        buffer = ByteBuffer.allocate(buffer.limit() + 2).order(ByteOrder.BIG_ENDIAN)
        buffer.put(rawBuffer0)
        buffer.putShort(crc)
        return buffer.array()
    }
    /** Decode message from buf. On error, throws an exception */
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
            if (oldCrc != newCrc) {
                throw WatchMessageDecodingException("CRC is incorrect (expected crc $oldCrc, calculated crc $newCrc, command $command, sequenceNumber $sequenceNumber, ackedSequenceNumber $ackedSequenceNumber, length $length)")
            }
            return WatchRawResponse(
                sequenceNumber, ackedSequenceNumber, command, rawContents
            )
        } else {
            throw WatchMessageDecodingException("unexpected EOF (command $command, sequenceNumber $sequenceNumber, ackedSequenceNumber $ackedSequenceNumber, length $length)")
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

        val oldCrc = buf.short
        buf.rewind()
        val bufBeforeCrc = ByteArray(length)
        buf.get(bufBeforeCrc)
        val newCrc = Crc16.crc16(bufBeforeCrc)
// TODO
//        if (newCrc != oldCrc) {
//            throw WatchMessageDecodingException("CRC is incorrect (expected crc $oldCrc, calculated crc $newCrc, command $command, sequenceNumber $sequenceNumber, length $length)")
//        }
        val r = buf.remaining()
        Log.e("BIG", "remaining $r")
        return WatchRawResponse(sequenceNumber, sequenceNumber, command, result)
    }
}