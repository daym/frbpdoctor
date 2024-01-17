package com.friendly_machines.frbpdoctor.watchprotocol.command

import java.nio.ByteBuffer
import java.nio.ByteOrder

class SetWeatherCommand(
    weatherType: Short, temp: Byte, maxTemp: Byte, minTemp: Byte, dummy: Byte/*0*/, month: Byte, dayOfMonth: Byte, dayOfWeekMondayBased: Byte, title: ByteArray
) : WatchCommand(
    44, run {
        /// TITLE is the big endian short list... ish
        val buf = ByteBuffer.allocate(2 + 1 + 1 + 1 + 1 + 1 + 1 + 1 + title.size).order(ByteOrder.BIG_ENDIAN)
        buf.putShort(weatherType)
        buf.put(temp)
        buf.put(maxTemp)
        buf.put(minTemp)
        buf.put(dummy)
        buf.put(month)
        buf.put(dayOfMonth)
        buf.put(dayOfWeekMondayBased) // FIXME make more abstract
        buf.put(title)
        buf.array()
    })