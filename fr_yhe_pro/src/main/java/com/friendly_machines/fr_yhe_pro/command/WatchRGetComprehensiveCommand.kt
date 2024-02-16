package com.friendly_machines.fr_yhe_pro.command

import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.WatchOperation
import java.nio.ByteBuffer

class WatchRGetComprehensiveCommand : WatchCommand(WatchOperation.RComprehensive, byteArrayOf()) {
    data class Response(
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
            fun parse(buf: ByteBuffer): Response {
                return Response(
                    steps = buf.int,
                    distance = buf.short,
                    kcal = buf.short,
                    heartRate = buf.get(),
                    systolicPressure = buf.get(),
                    diastolicPressure = buf.get(),
                    bloodOxygen = buf.get(),
                    respirationRate = buf.get(),
                    temperatureInt = buf.get(),
                    temperatureFloat = buf.get(),
                    wearingState = buf.get(),
                    electricity = buf.get(),
                    ppi = buf.int
                )
            }
        }
    }
}
