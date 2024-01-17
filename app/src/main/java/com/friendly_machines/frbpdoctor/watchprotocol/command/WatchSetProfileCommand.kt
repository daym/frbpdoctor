package com.friendly_machines.frbpdoctor.watchprotocol.command

class WatchSetProfileCommand(height: Byte, weight: Byte, sex: Byte, age: Byte) : WatchCommand(53, run {
    byteArrayOf(height, weight, sex, age)
})