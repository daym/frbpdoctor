package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer

data class SportDataBlock(val sportType: UByte, val avgHeartRate: UByte, val heat: Short, val runningDistance: Short, val duration: UShort, val speed: Short, val stepCount: Int, val timestamp: UInt) {
    companion object {
        fun parseMed(buf: ByteBuffer): SportDataBlock {
            val sportType = buf.get().toUByte()
            val avgHeartRate = buf.get().toUByte()
            val heat: Short = buf.short
            val runningDistance: Short = buf.short
            val duration: UShort = buf.short.toUShort()
            val speed: Short = buf.short
            val stepCount: Int = buf.int
            val timestamp: UInt = buf.int.toUInt()
            return SportDataBlock(sportType, avgHeartRate, heat, runningDistance, duration, speed, stepCount, timestamp)
        }
    }
}