package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

/**
 * Long sitting reminder command.
 *
 * @param startHour1 Period 1 start hour (0-23)
 * @param startMinute1 Period 1 start minute (0-59) 
 * @param endHour1 Period 1 end hour (0-23)
 * @param endMinute1 Period 1 end minute (0-59)
 * @param startHour2 Period 2 start hour (0-23)
 * @param startMinute2 Period 2 start minute (0-59)
 * @param endHour2 Period 2 end hour (0-23) 
 * @param endMinute2 Period 2 end minute (0-59)
 * @param interval Reminder interval in minutes (typically 15, 30, 45, 60)
 * @param repeats Week repeat pattern: bit-encoded days after reversal
 *                Format: weekString("MTWTFSS"), enableBit, reverse(), binary2int
 *                Examples: 159=Mon-Fri enabled, 255=All days, 0=Disabled
 */
class WatchSSetLongSittingCommand(startHour1: Byte, startMinute1: Byte, endHour1: Byte, endMinute1: Byte, startHour2: Byte, startMinute2: Byte, endHour2: Byte, endMinute2: Byte, interval: Byte, repeats: UByte): WatchCommand(WatchOperation.SSetLongSitting, byteArrayOf(startHour1, startMinute1, endHour1, endMinute1, startHour2, startMinute2, endHour2, endMinute2, interval, repeats.toByte())) {
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