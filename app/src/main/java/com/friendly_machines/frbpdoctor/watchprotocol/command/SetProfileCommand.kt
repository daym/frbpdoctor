package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchCommand

class SetProfileCommand(height: Byte, weight: Byte, sex: Byte, age: Byte) : WatchCommand(53, run {
    byteArrayOf(height, weight, sex, age)
})