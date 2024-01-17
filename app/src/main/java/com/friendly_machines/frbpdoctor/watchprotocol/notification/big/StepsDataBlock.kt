package com.friendly_machines.frbpdoctor.watchprotocol.notification.big

import java.nio.ByteBuffer

data class StepsDataBlock(val currentSteps: Int, val targetSteps: Int, val dayTimestamp: UInt) {
    companion object {
        fun parse(buf: ByteBuffer): StepsDataBlock {
            val currentSteps: Int = buf.int
            val targetSteps: Int = buf.int
            val dayTimestamp: UInt = buf.int.toUInt()
            return StepsDataBlock(currentSteps, targetSteps, dayTimestamp)
        }
    }
}
