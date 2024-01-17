package com.friendly_machines.frbpdoctor.watchprotocol.notification.big

import java.nio.ByteBuffer

data class BpDataBlock(val systolicPressure: UByte, val diastolicPressure: UByte, val pulse: UByte, val timestamp: UInt) {
    companion object {
        fun parse(buf: ByteBuffer): BpDataBlock {
            val systolicPressure = buf.get().toUByte()
            val diastolicPressure = buf.get().toUByte()
            val pulse = buf.get().toUByte()
            val timestamp: UInt = buf.int.toUInt()
            return BpDataBlock(systolicPressure, diastolicPressure, pulse, timestamp)
        }
    }
}