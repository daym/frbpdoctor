package com.friendly_machines.frbpdoctor.watchprotocol.bluetooth

import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchRawResponse
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse

interface WatchListener {
    /** Called with small responses that are complete in one call */
    fun onWatchResponse(response: WatchResponse)
    /** Called with each chunk of a large transfer */
    fun onBigWatchRawResponse(rawResponse: WatchRawResponse) {

    }
    /** Called as soon as our SetMtu request got a responds (which we send as soon as we have connected) */
    fun onMtuResponse(mtu: Int) {

    }
    fun onException(exception: Throwable) {

    }
}