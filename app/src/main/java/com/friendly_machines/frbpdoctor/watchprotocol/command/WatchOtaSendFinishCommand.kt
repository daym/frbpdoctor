package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

class WatchOtaSendFinishCommand(type: WatchOtaFirmwareType): WatchCommand(WatchOperation.OtaSendFinish, byteArrayOf(type.code))