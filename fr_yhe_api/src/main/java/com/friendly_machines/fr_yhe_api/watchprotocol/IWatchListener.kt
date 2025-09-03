package com.friendly_machines.fr_yhe_api.watchprotocol

// TODO add concrete on* things as well.
interface IWatchListener {
    /** Called with small responses that are complete in one call */
    fun onWatchResponse(response: WatchResponse) {

    }

    /** Called with each chunk of a large transfer. Only on med. */
    fun onBigWatchRawResponse(rawResponse: WatchRawResponse) {

    }

    /** Called as soon as our SetMtu request got a response (which we send as soon as we have connected) */
    fun onMtuResponse(mtu: Int) {

    }

    fun onException(exception: Throwable) {

    }

    fun onWatchPhoneCallControl(answer: WatchPhoneCallControlAnswer): Boolean {
        return false
    }

    fun onResetSequenceNumbers() {

    }

    fun onWatchMusicControl(control: WatchMusicControlAnswer): Boolean {
        return false
    }

    fun onWatchCameraControl(control: WatchCameraControlAnswer): Boolean {
        return false
    }

    fun onWatchFindMobilePhone(): Boolean {
        return false
    }

    fun onWatchHeartAlarm(): Boolean {
        return false
    }

    fun onWatchRegularReminder(): Boolean {
        return false
    }

    fun onWatchSleepReminder(): Boolean {
        return false
    }

    fun onConnected() {

    }
}