package com.friendly_machines.frbpdoctor.watchprotocol.command

// Type: 1: ROM; otherwise: font
class WatchOtaFinishCommand(type: Byte): WatchCommand(5, byteArrayOf(type)) {
}