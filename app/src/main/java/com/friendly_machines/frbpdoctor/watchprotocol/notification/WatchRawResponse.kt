package com.friendly_machines.frbpdoctor.watchprotocol.notification

class WatchRawResponse(var sequenceNumber: Int, var ackedSequenceNumber: Int, var command: Short, var arguments: ByteArray) {
    override fun toString(): String {
        return String.format("%d: %s", command, arguments)
    }
}