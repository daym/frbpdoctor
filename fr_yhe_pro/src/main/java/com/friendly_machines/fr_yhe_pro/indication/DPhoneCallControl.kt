package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import java.nio.ByteBuffer

data class DPhoneCallControl(val code: Short, val answer: Byte) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x02 + WatchResponseFactory.D_RESPONSE_CODE_OFFSET).toShort()
        fun parse(buf: ByteBuffer): DPhoneCallControl {
            return DPhoneCallControl(code = WeCouldRespondCode, answer = buf.get())
        }
    }
}