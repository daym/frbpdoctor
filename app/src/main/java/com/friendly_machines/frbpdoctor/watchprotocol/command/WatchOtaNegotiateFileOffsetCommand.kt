package com.friendly_machines.frbpdoctor.watchprotocol.command

// Type: 1: ROM; 4: font; otherwise pic
// Purpose: Probably to resume.
// TODO: We could also specify an offset here maybe.
class WatchNegotiateOtaFileOffsetCommand(type: Byte): WatchCommand(3, byteArrayOf(type)) {
}