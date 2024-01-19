package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

// Type: 1: ROM; 4: font; otherwise pic
class WatchOtaSendStartCommand(type: Byte): WatchCommand(WatchOperation.OtaSendStart, byteArrayOf(type)) {
}