package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import java.nio.ByteBuffer

data class DLostReminder(val code: Short) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x01 + WatchResponseFactory.D_RESPONSE_CODE_OFFSET).toShort()
        fun parse(buf: ByteBuffer): DLostReminder {
            return DLostReminder(code = WeCouldRespondCode)
        }
    }
}