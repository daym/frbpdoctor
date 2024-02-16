package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer

data class FileVerification2(val package_: Int, val size: Int, val crc: Int) {
    companion object {
        fun parsePro(buf: ByteBuffer): FileVerification2 {
            return FileVerification2(size = buf.int, package_ = buf.int, crc = buf.int)
        }
    }
}