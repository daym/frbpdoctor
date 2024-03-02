package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer

data class FileVerification(val package_: Int, val size: Int, val crc: Short) {
    companion object {
        const val SIZE: Int = (4 + 4 + 2) // B

        fun parsePro(buf: ByteBuffer): FileVerification {
            return FileVerification(package_ = buf.int, size = buf.int, crc = buf.short)
        }
    }
}