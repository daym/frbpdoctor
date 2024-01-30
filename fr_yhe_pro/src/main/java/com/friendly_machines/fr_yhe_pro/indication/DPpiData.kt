package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import java.nio.ByteBuffer

data class DPpiData(val code: Short) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x12 + WatchResponseFactory.D_RESPONSE_CODE_OFFSET).toShort()
        fun parse(buf: ByteBuffer): DPpiData {
            return DPpiData(code = WeCouldRespondCode)
        }
    }
}