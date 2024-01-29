package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer

data class CurrentStepDataBlock(val currentStep: Int, val targetStep: Int, val dayTimestamp: UInt) {
    companion object {
        fun parseMed(buf: ByteBuffer): CurrentStepDataBlock {
            val currentStep: Int = buf.int
            val targetStep: Int = buf.int
            val dayTimestamp: UInt = buf.int.toUInt()
            return CurrentStepDataBlock(currentStep, targetStep, dayTimestamp)
        }
    }
}
