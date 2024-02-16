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
        val steps: Int,
        val calories: Short,
        val distance: Short,
        val sportsRealSteps: Int,
        val sportsRealCalories: Short,
        val sportsRealDistance: Short,
        val recordTime: Int,
        val ppi: Int
    ) : WatchResponse() {
        companion object {
            fun parse(buf: ByteBuffer): Response {
                return Response(
                    heartRate = buf.get(),
                    systolicPressure = buf.get(),
                    diastolicPressure = buf.get(),
                    bloodOxygen = buf.get(),
                    respirationRate = buf.get(),
                    temperatureInt = buf.get(),
                    temperatureFloat = buf.get(),
                    steps = buf.int,
                    calories = buf.short,
                    distance = buf.short,
                    sportsRealSteps = buf.int,
                    sportsRealCalories = buf.short,
                    sportsRealDistance = buf.short,
                    recordTime = buf.int,
                    ppi = buf.int
                )
            }
        }
    }
}
