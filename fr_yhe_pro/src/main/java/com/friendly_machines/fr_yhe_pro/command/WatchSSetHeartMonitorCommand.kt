package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

/**
 * Sets heart rate monitoring configuration on the watch.
 * 
 * @param type Command type: ALWAYS 1 (command identifier, not enable/disable)
 * @param interval Monitoring interval in minutes: 0 = disabled, >0 = enabled with that interval
 * 
 * Usage patterns:
 * - Enable monitoring: settingHeartMonitor(1, 10) = monitor every 10 minutes
 * - Enable monitoring: settingHeartMonitor(1, 30) = monitor every 30 minutes  
 * - Disable monitoring: settingHeartMonitor(1, 0) = monitoring disabled
 * 
 * NOTE: First parameter is ALWAYS 1.  Enable/disable is controlled by interval=0.
 */
class WatchSSetHeartMonitorCommand(val type: Byte, val interval: Byte): WatchCommand(WatchOperation.SHeartMonitor, byteArrayOf(type, interval)) {
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