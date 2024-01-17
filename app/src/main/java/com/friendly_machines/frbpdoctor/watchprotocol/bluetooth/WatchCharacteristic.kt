package com.friendly_machines.frbpdoctor.watchprotocol.bluetooth

import java.util.UUID

class WatchCharacteristic {
    companion object {
        val writingPortCharacteristic: UUID = UUID.fromString("00000001-0000-1001-8001-00805F9B07D0")
        val notificationCharacteristic: UUID = UUID.fromString("00000002-0000-1001-8001-00805f9b07d0")
        val bigWritingPortCharacteristic: UUID = UUID.fromString("00000003-0000-1001-8001-00805F9B07D0")
        val bigNotificationCharacteristic: UUID = UUID.fromString("00000004-0000-1001-8001-00805F9B07D0")
    }
}