package com.friendly_machines.fr_yhe_pro

object Crc16rev {
    fun crc16(bArr: ByteArray): Int {
        val i2: Int = bArr.size
        var s: Short = -1
        for (i3 in 0 until i2) {
            val s2 = ((((s.toInt() shl 8) and 0xFF00) or ((s.toInt() shr 8) and 255)).toShort().toInt() xor (bArr[i3].toInt() and 255)).toShort()
            val s3 = (s2.toInt() xor (s2.toInt() and 255 shr 4).toByte().toShort().toInt()).toShort()
            val s4 = (s3.toInt() xor (s3.toInt() shl 8 shl 4)).toShort()
            s = (s4.toInt() xor (s4.toInt() and 255 shl 4 shl 1)).toShort()
        }
        return s.toInt() and 0xFFFF
    }
}