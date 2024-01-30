package com.friendly_machines.fr_yhe_med.bluetooth

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanRecord
import android.companion.BluetoothLeDeviceFilter
import android.companion.DeviceFilter
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchDriver

class WatchDriver : IWatchDriver {
    override val id: String = WatchCommunicator.javaClass.canonicalName.removeSuffix(".Companion") // FIXME
    override val deviceFilter: DeviceFilter<*> = BluetoothLeDeviceFilter.Builder().setScanFilter(ScanFilter.Builder().setServiceUuid(WatchCharacteristic.serviceUuid).build()).build()
    override fun isCompatibleWith(scanRecord: android.bluetooth.le.ScanRecord?): Boolean {
        return if (scanRecord != null)
            scanRecord.serviceUuids.find { it == WatchCharacteristic.serviceUuid } != null
        else
            false
    }

    override val communicator = WatchCommunicator()

    // Returns: Key
    override fun createCommunicator(scanRecord: ScanRecord?, continuation: (key: ByteArray) -> Unit) {

    }
}