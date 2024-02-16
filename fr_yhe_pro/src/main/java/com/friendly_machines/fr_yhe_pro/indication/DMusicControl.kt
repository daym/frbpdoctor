package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchMusicControlAnswer
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import java.nio.ByteBuffer

data class DMusicControl(val code: Short, val control: WatchMusicControlAnswer) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x04 + WatchResponseFactory.D_RESPONSE_CODE_OFFSET).toShort()
        private fun parseControl(b: Byte): WatchMusicControlAnswer {
            return when (b) {
                1.toByte() -> WatchMusicControlAnswer.PlayPause
                2.toByte() -> WatchMusicControlAnswer.PreviousSong
                3.toByte() -> WatchMusicControlAnswer.NextSong
                4.toByte() -> WatchMusicControlAnswer.IncreaseVolume
                5.toByte() -> WatchMusicControlAnswer.DecreaseVolume
                else -> {
                    WatchMusicControlAnswer.Unknown
                }
            }
        }
        fun parse(buf: ByteBuffer): DMusicControl {
            return DMusicControl(code = WeCouldRespondCode, control = parseControl(buf.get()))
        }
    }
}