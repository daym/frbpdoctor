package com.friendly_machines.frbpdoctor.watchprotocol.command

enum class WatchOtaFirmwareType(val code: Byte) {
    Rom(1), Font(4); // TODO: otherwise pic
}
