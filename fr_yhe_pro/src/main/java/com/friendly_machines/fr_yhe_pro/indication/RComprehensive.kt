package com.friendly_machines.fr_yhe_pro.indication

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.indication.WatchResponseFactory.R_RESPONSE_CODE_OFFSET
import java.nio.ByteBuffer

// Note: Request should use WatchAGetRealData(sensorType = 10, measureType, duration)
// FIXME: Real time response
data class RComprehensive(
    val code: Short,
    val steps: Int,
    val distance: Short,
    val kcal: Short,
    val heartRate: Byte,
    val systolicPressure: Byte,
    val diastolicPressure: Byte,
    val bloodOxygen: Byte,
    val respirationRate: Byte,
    val temperatureInt: Byte,
    val temperatureFloat: Byte,
    val wearingState: Byte,
    val electricity: Byte,
    val ppi: Int
) : WatchResponse() {
    companion object {
        private const val WeCouldRespondCode: Short = (0x0A + R_RESPONSE_CODE_OFFSET).toShort()
        
        private fun read24BitLeInt(buf: ByteBuffer): Int {
            val byte1 = buf.get().toInt() and 0xFF
            val byte2 = buf.get().toInt() and 0xFF
            val byte3 = buf.get().toInt() and 0xFF
            return (byte3 shl 16) or (byte2 shl 8) or byte1
        }

        fun parse(buf: ByteBuffer): RComprehensive {
            val steps = read24BitLeInt(buf)
            val distance = buf.short
            val kcal = buf.short
            val heartRate = buf.get()
            val systolicPressure = buf.get()
            val diastolicPressure = buf.get()
            val bloodOxygen = buf.get()
            val respirationRate = buf.get()
            val temperatureInt = buf.get()
            val temperatureFloat = buf.get()
            val wearingState = buf.get()
            val electricity = buf.get()
            val ppi = buf.int
            
            return RComprehensive(
                code = WeCouldRespondCode,
                steps = steps, 
                distance = distance, 
                kcal = kcal, 
                heartRate = heartRate, 
                systolicPressure = systolicPressure, 
                diastolicPressure = diastolicPressure,
                bloodOxygen = bloodOxygen, 
                respirationRate = respirationRate, 
                temperatureInt = temperatureInt, 
                temperatureFloat = temperatureFloat,
                wearingState = wearingState, 
                electricity = electricity, 
                ppi = ppi
            )
        }
    }
}