package com.friendly_machines.frbpdoctor.watchprotocol.notification.big

import java.nio.ByteBuffer

data class SleepDataBlock(val startTimestamp: UInt, val endTimestamp: UInt, val flag: Byte) {
    companion object {
        fun parse(buf: ByteBuffer): SleepDataBlock {
            val startTimestamp: UInt = buf.int.toUInt()
            val endTimestamp: UInt = buf.int.toUInt()
            val flag = buf.get() // TODO
            return SleepDataBlock(startTimestamp, endTimestamp, flag)
        }
    }
}