package com.friendly_machines.frbpdoctor.watchprotocol.command

// Type: 1: ROM; otherwise: font; probably progress
class WatchOtaRequestCommand(type: Byte): WatchCommand(1, byteArrayOf(type)) {
}