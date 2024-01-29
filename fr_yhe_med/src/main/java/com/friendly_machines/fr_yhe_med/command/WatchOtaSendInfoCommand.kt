package com.friendly_machines.fr_yhe_med.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_med.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

// Type: 1: ROM; 4: font; otherwise pic
/*
 * Contents:
 * - type==1(rom) -> 1: u8, soc: u32?, romVersion: u32, file length encoded int, md5nothing, empty16
 * - type==4(font) -> 1: u8, file length encoded int, encode be int(0x0100_0000), md5 byte[16] no hex, empty array 16
 * - otherwise(pic) -> byte[64], file length encoded int
 */
class WatchOtaSendInfoCommand(type: WatchOtaFirmwareType, contents: ByteArray, crc32: Int): WatchCommand(WatchOperation.OtaSendInfo, byteArrayOf(type.code) + contents + ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(crc32).array())
{
    data class Response(val flag: Byte, val romVersion: Int, val maxSize: Short): WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                val flag: Byte = buf.get()

                // TODO The following not for fonts.

                val romVersion = buf.int
                val maxSize = buf.short
                return Response(flag = flag, romVersion = romVersion, maxSize = maxSize)
            }
        }
    }
}