package com.friendly_machines.fr_yhe_api.commondata

import java.nio.ByteBuffer

data class WatchDialDataBlock(val id: Int, val blockNumber: Short, val canDelete: Boolean, val version: Short) {
    fun isCustomDial(): Boolean {
        return (id or 0xFF) == Int.MAX_VALUE
    }
    companion object {
        fun parsePro(buf: ByteBuffer): WatchDialDataBlock {
            return WatchDialDataBlock(id = buf.int, blockNumber = buf.short, canDelete = when (buf.get()) {
                1.toByte() -> true
                else -> false
            }, version = buf.short)
        }
    }

}