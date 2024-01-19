package com.friendly_machines.frbpdoctor.watchprotocol.notification

class WatchRawResponse(val sequenceNumber: Int, val ackedSequenceNumber: Int, val command: Short, val arguments: ByteArray) {
    override fun toString(): String {
        return String.format("%d: %s", command, arguments)
    }
}