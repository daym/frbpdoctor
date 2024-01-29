package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import java.nio.ByteBuffer

/*
control:
    1 play/pause
    2 previous song
    3 next song
    4 volume up
    5 volume down
 */
data class DMusicControl(val code: Short, val control: Byte)  : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x04 + WatchResponseFactory.D_RESPONSE_CODE_OFFSET).toShort()
        fun parse(buf: ByteBuffer): DMusicControl {
            return DMusicControl(code = WeCouldRespondCode, control = buf.get())
        }
    }
}