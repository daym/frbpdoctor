package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

class WatchOtaSendStartCommand(type: WatchOtaFirmwareType): WatchCommand(WatchOperation.OtaSendStart, byteArrayOf(type.code)) {
}