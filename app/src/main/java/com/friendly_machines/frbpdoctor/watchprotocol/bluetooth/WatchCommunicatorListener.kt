package com.friendly_machines.frbpdoctor.watchprotocol.bluetooth

import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchCommunicationRawResponse
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse

interface WatchCommunicatorListener {
    fun onWatchResponse(response: WatchResponse)
    fun onBigWatchRawResponse(response: WatchCommunicationRawResponse)
    fun onMtuResponse(mtu: Int)
}