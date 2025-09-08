package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer

data class BpDataBlockMed(val systolicPressure: UByte, val diastolicPressure: UByte, val pulse: UByte, val timestamp: UInt) {
    companion object {
        fun parseMed(buf: ByteBuffer): BpDataBlockMed {
            val systolicPressure = buf.get().toUByte()
            val diastolicPressure = buf.get().toUByte()
            val pulse = buf.get().toUByte()
            val timestamp: UInt = buf.int.toUInt()
            return BpDataBlockMed(systolicPressure, diastolicPressure, pulse, timestamp)
        }
    }
}