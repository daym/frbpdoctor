package com.friendly_machines.fr_yhe_pro.bluetooth

import android.bluetooth.le.ScanFilter
import android.companion.BluetoothLeDeviceFilter
import android.companion.DeviceFilter
import android.os.ParcelUuid
import java.util.UUID

object WatchCharacteristic {
    //val serviceUuid: ParcelUuid = ParcelUuid.fromString("0000FE51-0000-1000-8000-00805F9B34FB")
    //val serviceUuid: ParcelUuid = ParcelUuid.fromString("be940000-7333-be46-b7ae-689e71722bd5")
    internal val serviceUuid: ParcelUuid = ParcelUuid.fromString("0000fee7-0000-1000-8000-00805f9b34fb")

    val writingPortCharacteristic: UUID = UUID.fromString("be940001-7333-be46-b7ae-689e71722bd5")
    val indicationPortCharacteristic: UUID = UUID.fromString("be940001-7333-be46-b7ae-689e71722bd5")
    val bigWritingPortCharacteristic: UUID = UUID.fromString("BE940002-7333-BE46-B7AE-689E71722BD5")
    val bigIndicationPortCharacteristic: UUID = UUID.fromString("BE940003-7333-BE46-B7AE-689E71722BD5")

    // FIXME service 0000ae00-0000-1000-8000-00805f9b34fb
}
