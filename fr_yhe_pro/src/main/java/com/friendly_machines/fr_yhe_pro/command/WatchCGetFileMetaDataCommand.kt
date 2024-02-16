package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.commondata.FileVerification
import com.friendly_machines.fr_yhe_api.commondata.FileVerification2
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchCGetFileMetaDataCommand(val name: String, val x: Int) : WatchCommand(WatchOperation.CGetFileMetaData, run {
    // name: max 16 chars in utf-8
    val nameBytes = name.toByteArray(Charsets.UTF_8)
    val buf = ByteBuffer.allocate(16 + 4).order(ByteOrder.LITTLE_ENDIAN)
    buf.put(nameBytes)
    buf.position(16)
    buf.putInt(x)
    buf.array()
}) {

    data class Response(val entries: List<FileVerification2>) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val entries = mutableListOf<FileVerification2>()
                while (buf.remaining() >= 4 + 4 + 2) {
                    entries.add(FileVerification2.parsePro(buf))
                }
                return Response(entries = entries)
            }
        }
    }

}