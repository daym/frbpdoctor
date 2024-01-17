package com.friendly_machines.frbpdoctor.watchprotocol.bluetooth

import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchRawResponse
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse

interface WatchListener {
    fun onWatchResponse(response: WatchResponse)
    fun onBigWatchRawResponse(rawResponse: WatchRawResponse)
    fun onMtuResponse(mtu: Int)
}