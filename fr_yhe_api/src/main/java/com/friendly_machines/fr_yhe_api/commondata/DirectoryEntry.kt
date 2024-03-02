package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer

data class DirectoryEntry(val name: String, val size: Int, val checksum: Int) {
    companion object {
        val SIZE: Int = (16 + 4 + 4) // B

        fun parsePro(buf: ByteBuffer): DirectoryEntry {
            val name = ByteArray(16)
            buf.get(name)
            val size = buf.int
            val checksum = buf.int
            return DirectoryEntry(name = String(name), size = size, checksum = checksum)
        }
    }
}