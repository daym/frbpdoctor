package com.friendly_machines.fr_yhe_api.watchprotocol

import android.bluetooth.le.ScanRecord
import android.companion.DeviceFilter

interface IWatchDriver {
    val id: String
    val deviceFilter: DeviceFilter<*>
    fun compatibleWith(scanRecord: android.bluetooth.le.ScanRecord?): Boolean
    val communicator: IWatchCommunicator
    fun start(scanRecord: ScanRecord?, continuation: (key: ByteArray) -> Unit)
}