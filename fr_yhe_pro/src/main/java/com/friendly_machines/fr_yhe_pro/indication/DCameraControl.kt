package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchCameraControlAnswer
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import java.nio.ByteBuffer

data class DCameraControl(val code: Short, val control: WatchCameraControlAnswer) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x03 + WatchResponseFactory.D_RESPONSE_CODE_OFFSET).toShort()
        private fun parseControl(b: Byte): WatchCameraControlAnswer = when (b) {
            0.toByte() -> WatchCameraControlAnswer.Exit
            1.toByte() -> WatchCameraControlAnswer.Prepare
            2.toByte() -> WatchCameraControlAnswer.Shoot
            else -> {
                WatchCameraControlAnswer.Unknown
            }
        }

        fun parse(buf: ByteBuffer): DCameraControl {
            return DCameraControl(code = WeCouldRespondCode, control = parseControl(buf.get()))
        }
    }
}