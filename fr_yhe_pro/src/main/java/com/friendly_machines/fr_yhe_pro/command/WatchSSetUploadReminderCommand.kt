package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

/**
 * Sets the upload reminder configuration on the watch.
 *
 * @param enabled Whether upload reminders are enabled
 */
class WatchSSetUploadReminderCommand(enabled: Boolean, parameter2: Byte) : WatchCommand(WatchOperation.SSetUploadReminder, byteArrayOf(if (enabled) 1.toByte() else 0.toByte(), parameter2)) {
    data class Response(val status: Byte, val data: Byte?) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get()
                val data = if (buf.hasRemaining()) buf.get() else null
                return Response(status = status, data = data)
            }
        }
    }
}