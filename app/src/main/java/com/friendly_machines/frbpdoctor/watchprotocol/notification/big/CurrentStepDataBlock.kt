package com.friendly_machines.frbpdoctor.watchprotocol.notification.big

import java.nio.ByteBuffer

data class CurrentStepDataBlock(val currentStep: Int, val targetStep: Int, val dayTimestamp: UInt) {
    companion object {
        fun parse(buf: ByteBuffer): CurrentStepDataBlock {
            val currentStep: Int = buf.int
            val targetStep: Int = buf.int
            val dayTimestamp: UInt = buf.int.toUInt()
            return CurrentStepDataBlock(currentStep, targetStep, dayTimestamp)
        }
    }
}
