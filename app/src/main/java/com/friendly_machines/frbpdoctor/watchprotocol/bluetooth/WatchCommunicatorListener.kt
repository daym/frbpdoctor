package com.friendly_machines.frbpdoctor.watchprotocol.bluetooth

import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchCommunicationRawResponse
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse

interface WatchCommunicatorListener {
    //fun connectToDevice(deviceAddress: String)
    //fun sendData(data: ByteArray)
    // Other methods for Bluetooth action?
    fun onWatchResponse(response: WatchResponse)
    fun onBigWatchRawResponse(response: WatchCommunicationRawResponse)
    fun onMtuResponse(mtu: Int)
}