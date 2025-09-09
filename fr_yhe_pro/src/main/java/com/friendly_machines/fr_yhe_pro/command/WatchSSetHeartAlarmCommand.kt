package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

/**
 * Sets heart rate alarm configuration on the watch.
 *
 * @param enabled Alarm enable/disable: 1 = enabled, 0 = disabled
 * @param minValue UNKNOWN - Always 0 in usage (possibly reserved/unused)
 * @param maxValue Heart rate threshold in BPM (e.g., 130, 160)
 * 
 * Usage patterns:
 * - Enable alarm: settingHeartAlarm(1, 0, 130) = alarm at 130 BPM
 * - Disable alarm: settingHeartAlarm(0, 0, 0) = alarm disabled
 */
class WatchSSetHeartAlarmCommand(val enabled: Byte, val minValue: Byte, val maxValue: Byte) : WatchCommand(WatchOperation.SHeartAlarm, byteArrayOf(enabled, minValue, maxValue)) {
    data class Response(val status: Byte, val data: Byte?) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val status = buf.get() // FIXME
                val data = if (buf.hasRemaining()) buf.get() else null
                return Response(status = status, data = data)
            }
        }
    }
}