package com.friendly_machines.frbpdoctor.watchprotocol.command

import com.friendly_machines.frbpdoctor.watchprotocol.WatchOperation

// See <https://developer.tuya.com/en/docs/iot-device-dev/OTA_BLE> for a possible workflow.
// Type: 1: ROM; 4: font; otherwise pic
// Purpose: Probably to resume.
// TODO: We could also specify an offset here maybe.
class WatchOtaNegotiateFileOffsetCommand(type: Byte): WatchCommand(WatchOperation.OtaNegotiateFileOffset, byteArrayOf(type)) {
}