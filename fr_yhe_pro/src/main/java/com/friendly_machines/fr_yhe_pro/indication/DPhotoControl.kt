package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import java.nio.ByteBuffer

/*
control:
   0 for exiting camera
   1 for preparing shooting
   2 for shooting
 */
data class DPhotoControl(val code: Short, val control: Byte) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x03 + WatchResponseFactory.D_RESPONSE_CODE_OFFSET).toShort()
        fun parse(buf: ByteBuffer): DPhotoControl {
            return DPhotoControl(code = WeCouldRespondCode, control = buf.get())
        }
    }
}