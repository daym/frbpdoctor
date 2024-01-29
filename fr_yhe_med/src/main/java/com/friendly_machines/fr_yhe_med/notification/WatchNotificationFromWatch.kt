package com.friendly_machines.fr_yhe_med.notification

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import java.nio.ByteBuffer

// This one can happen without us sending a command! So it's not really a response.
data class WatchNotificationFromWatch(val eventCode: Byte) : WatchResponse() {
    companion object {
        const val EVENT_CODE_ANSWER_PHONE_CALL: Byte = 0
        const val EVENT_CODE_RECONFIGURE_WATCH: Byte = 1 // (for example language; but not alarm)
        fun parse(buf: ByteBuffer): WatchNotificationFromWatch {
            val eventCode: Byte = buf.get()
            return WatchNotificationFromWatch(eventCode = eventCode)
        }
    }
}