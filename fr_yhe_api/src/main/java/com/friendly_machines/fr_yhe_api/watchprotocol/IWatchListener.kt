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

    fun onWatchPhoneCallControl(answer: WatchPhoneCallControlAnswer) {
    }

    fun onResetSequenceNumbers() {

    }

    fun onWatchMusicControl(control: WatchMusicControlAnswer) {

    }

    fun onWatchCameraControl(control: WatchCameraControlAnswer) {

    }

    fun onWatchFindMobilePhone() {

    }

    fun onWatchHeartAlarm() {

    }

    fun onWatchRegularReminder() {

    }

    fun onWatchSleepReminder() {

    }
}