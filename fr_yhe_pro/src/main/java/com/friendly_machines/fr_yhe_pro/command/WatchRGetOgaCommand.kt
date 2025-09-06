package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchRGetOgaCommand : WatchCommand(WatchOperation.ROga, byteArrayOf()) {
    data class Response(
        val heartRate: Byte,
        val systolicPressure: Byte,
        val diastolicPressure: Byte,
        val bloodOxygen: Byte,
        val respirationRate: Byte,
        val temperatureInt: Byte,
        val temperatureFloat: Byte,
        val realSteps: Int,
        val realCalories: Short,
        val realDistance: Short,
        val sportsRealSteps: Int,
        val sportsRealCalories: Short,
        val sportsRealDistance: Short,
        val recordTime: Int?,
        val ppi: Int?
    ) : WatchResponse() {
        companion object {
            private fun read24BitLeInt(buf: ByteBuffer): Int {
                val byte1 = buf.get().toInt() and 0xFF
                val byte2 = buf.get().toInt() and 0xFF
                val byte3 = buf.get().toInt() and 0xFF
                return (byte3 shl 16) or (byte2 shl 8) or byte1
            }

            fun parse(buf: ByteBuffer): Response {
                val heartRate = buf.get()
                val systolicPressure = buf.get()
                val diastolicPressure = buf.get()
                val bloodOxygen = buf.get()
                val respirationRate = buf.get()
                val temperatureInt = buf.get()
                val temperatureFloat = buf.get()
                val realSteps = read24BitLeInt(buf)
                val realCalories = buf.short
                val realDistance = buf.short
                val sportsRealSteps = read24BitLeInt(buf)
                val sportsRealCalories = buf.short
                val sportsRealDistance = buf.short
                
                // Optional fields based on remaining length
                val recordTime = if (buf.remaining() >= 4) buf.int else null
                val ppi = if (buf.remaining() >= 4) buf.int else null
                
                return Response(
                    heartRate, systolicPressure, diastolicPressure, bloodOxygen, respirationRate,
                    temperatureInt, temperatureFloat, realSteps, realCalories, realDistance,
                    sportsRealSteps, sportsRealCalories, sportsRealDistance, recordTime, ppi
                )
            }
        }
    }
}
