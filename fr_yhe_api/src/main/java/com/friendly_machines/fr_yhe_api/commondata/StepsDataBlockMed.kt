package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer

data class StepsDataBlockMed(val currentSteps: Int, val targetSteps: Int, val dayTimestamp: UInt) {
    companion object {
        fun parseMed(buf: ByteBuffer): StepsDataBlockMed {
            val currentSteps: Int = buf.int
            val targetSteps: Int = buf.int
            val dayTimestamp: UInt = buf.int.toUInt()
            return StepsDataBlockMed(currentSteps, targetSteps, dayTimestamp)
        }
    }
}
