package com.friendly_machines.frbpdoctor.watchprotocol.notification.big

import java.nio.ByteBuffer

data class SleepDataBlock(val startTimestamp: UInt, val endTimestamp: UInt, val quality: Byte) {
    companion object {
        fun parse(buf: ByteBuffer): SleepDataBlock {
            val startTimestamp: UInt = buf.int.toUInt()
            val endTimestamp: UInt = buf.int.toUInt()
            val quality = buf.get() // TODO
            return SleepDataBlock(startTimestamp, endTimestamp, quality)
        }
    }
}