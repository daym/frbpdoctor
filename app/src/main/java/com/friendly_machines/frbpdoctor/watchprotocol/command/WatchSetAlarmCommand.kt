package com.friendly_machines.frbpdoctor.watchprotocol.command

import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSetAlarmCommand(
    action: Byte, id: Int, open: Byte, hour: Byte, min: Byte, title: Byte, repeats: ByteArray
) : WatchCommand(
    55, run {
        /**
         * command;
         * action: 1=add; 0=?;
         * title: possibilities: wakeup|work|anniversary|meeting|medicine|date
         * repeats: multiselect {Mo, Tu, Fr, Sat, Sun}
         */
        val buf = ByteBuffer.allocate(1 + 4 + 1 + 1 + 1 + 1 + repeats.size).order(ByteOrder.BIG_ENDIAN)
        buf.put(action)
        buf.putInt(id)
        buf.put(open)
        buf.put(hour)
        buf.put(min)
        buf.put(title)
        buf.put(repeats)
        buf.array()
    }
)