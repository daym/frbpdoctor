package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer

data class StepsDataBlock(val currentSteps: Int, val targetSteps: Int, val dayTimestamp: UInt) {
    companion object {
        fun parseMed(buf: ByteBuffer): StepsDataBlock {
            val currentSteps: Int = buf.int
            val targetSteps: Int = buf.int
            val dayTimestamp: UInt = buf.int.toUInt()
            return StepsDataBlock(currentSteps, targetSteps, dayTimestamp)
        }
    }
}
