package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer

data class SleepDataBlock(val startTimestamp: UInt, val endTimestamp: UInt, val quality: Byte) {
    companion object {
        fun parseMed(buf: ByteBuffer): SleepDataBlock {
            val startTimestamp: UInt = buf.int.toUInt()
            val endTimestamp: UInt = buf.int.toUInt()
            val quality = buf.get()
            return SleepDataBlock(startTimestamp, endTimestamp, quality)
        }
    }
}