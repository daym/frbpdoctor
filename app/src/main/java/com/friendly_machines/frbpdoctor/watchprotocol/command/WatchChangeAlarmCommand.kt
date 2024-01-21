package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.AlarmTitle
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchChangeAlarmCommand(
    action: WatchChangeAlarmAction, id: Int, open: Byte, hour: Byte, min: Byte, title: AlarmTitle, repeats: BooleanArray/*7*/
) : WatchCommand(
    WatchOperation.SetAlarm, run {
        /**
         * repeats: multiselect {Mon, Tue, Wed, Thu, Fri, Sat, Sun}
         */
        val buf = ByteBuffer.allocate(1 + 4 + 1 + 1 + 1 + 1 + repeats.size).order(ByteOrder.BIG_ENDIAN)
        buf.put(action.code)
        buf.putInt(id)
        buf.put(open)
        buf.put(hour)
        buf.put(min)
        buf.put(title.code)
        buf.put(repeats.map { if (it) { 1.toByte() } else 0.toByte() }.toByteArray())
        buf.array()
    }
)