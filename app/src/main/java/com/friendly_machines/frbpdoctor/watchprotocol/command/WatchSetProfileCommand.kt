package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

class WatchSetProfileCommand(height: Byte, weight: Byte, sex: Byte, age: Byte) : WatchCommand(WatchOperation.SetProfile, run {
    byteArrayOf(height, weight, sex, age)
})