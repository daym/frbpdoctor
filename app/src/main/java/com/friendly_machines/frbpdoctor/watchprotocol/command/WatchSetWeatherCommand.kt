package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WatchSetWeatherCommand(
    weatherType: Short, temp: Byte, maxTemp: Byte, minTemp: Byte, dummy: Byte/*0*/, month: Byte, dayOfMonth: Byte, dayOfWeekMondayBased: Byte, location: ByteArray
) : WatchCommand(
    WatchOperation.SetWeather, run {
        /// TITLE is the big endian short list... ish
        val buf = ByteBuffer.allocate(2 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + location.size).order(ByteOrder.BIG_ENDIAN)
        buf.putShort(weatherType)
        buf.put(temp)
        buf.put(maxTemp)
        buf.put(minTemp)
        buf.put(dummy)
        buf.put(month)
        buf.put(dayOfMonth)
        buf.put(dayOfWeekMondayBased) // FIXME make more abstract
        buf.put(location)
        buf.array()
    })