package com.friendly_machines.frbpdoctor.watchprotocol.notification.big

import java.nio.ByteBuffer

// Monday first in repeats.
data class AlarmDataBlock(val id: Int, val open: Byte, val hour: Byte, val min: Byte, val title: Byte, val repeats: ByteArray/*7*/) {
    companion object {
        fun parse(buf: ByteBuffer): AlarmDataBlock {
            val id = buf.int
            val open = buf.get()
            val hour = buf.get()
            val min = buf.get()
            val title = buf.get()
            val repeats = ByteArray(7)
            buf.get(repeats)
            return AlarmDataBlock(id = id, open = open, hour = hour, min = min, title = title, repeats = repeats)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AlarmDataBlock

        if (id != other.id) return false
        if (open != other.open) return false
        if (hour != other.hour) return false
        if (min != other.min) return false
        if (title != other.title) return false
        if (!repeats.contentEquals(other.repeats)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + open
        result = 31 * result + hour
        result = 31 * result + min
        result = 31 * result + title
        result = 31 * result + repeats.contentHashCode()
        return result
    }
}