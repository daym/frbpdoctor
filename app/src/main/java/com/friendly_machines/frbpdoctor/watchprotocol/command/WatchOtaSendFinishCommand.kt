package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

// Type: 1: ROM; otherwise: font
class WatchOtaSendFinishCommand(type: Byte): WatchCommand(WatchOperation.OtaSendFinish, byteArrayOf(type)) {
}