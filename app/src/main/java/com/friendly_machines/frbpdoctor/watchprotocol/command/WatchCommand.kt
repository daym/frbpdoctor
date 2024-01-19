package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

open class WatchCommand(val operation: WatchOperation, val arguments: ByteArray) {
    // TODO investigate 33

    // TODO 54 STEP_GOAL

    // TODO 67 RAW_BP_DATA (difficult)
}