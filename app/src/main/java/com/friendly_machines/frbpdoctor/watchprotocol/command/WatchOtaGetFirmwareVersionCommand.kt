package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

class WatchOtaGetFirmwareVersionCommand(type: WatchOtaFirmwareType): WatchCommand(WatchOperation.OtaGetFirmwareVersion, byteArrayOf(type.code))