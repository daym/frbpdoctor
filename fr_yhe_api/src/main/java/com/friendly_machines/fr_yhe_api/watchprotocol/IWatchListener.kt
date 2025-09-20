package com.friendly_machines.fr_yhe_api.watchprotocol

import androidx.annotation.MainThread

// TODO add concrete on* things as well.
interface IWatchListener {
    /** Called with small responses that are complete in one call */
    @MainThread
    fun onWatchResponse(response: WatchResponse) {

    }

    /** Called with each chunk of a large transfer. Only on med. */
    @MainThread
    fun onBigWatchRawResponse(rawResponse: WatchRawResponse) {

    }

    /** Called as soon as our SetMtu request got a response (which we send as soon as we have connected) */
    @MainThread
    fun onMtuResponse(mtu: Int) {

    }

    @MainThread
    fun onException(exception: Throwable) {

    }

    @MainThread
    fun onWatchPhoneCallControl(answer: WatchPhoneCallControlAnswer): Boolean {
        return false
    }

    @MainThread
    fun onResetSequenceNumbers() {

    }

    @MainThread
    fun onWatchMusicControl(control: WatchMusicControlAnswer): Boolean {
        return false
    }

    @MainThread
    fun onWatchCameraControl(control: WatchCameraControlAnswer): Boolean {
        return false
    }

    @MainThread
    fun onWatchFindMobilePhone(): Boolean {
        return false
    }

    @MainThread
    fun onWatchHeartAlarm(): Boolean {
        return false
    }

    @MainThread
    fun onWatchRegularReminder(): Boolean {
        return false
    }

    @MainThread
    fun onWatchSleepReminder(): Boolean {
        return false
    }

    @MainThread
    fun onWatchAlarm(): Boolean {
        return false
    }

    @MainThread
    fun onWatchConnectOrDisconnect(): Boolean {
        return false
    }

    @MainThread
    fun onWatchDynamicCode(): Boolean {
        return false
    }

    @MainThread
    fun onWatchEndEcg(): Boolean {
        return false
    }

    @MainThread
    fun onWatchInflatedBloodMeasurementResult(): Boolean {
        return false
    }

    @MainThread
    fun onWatchLostReminder(): Boolean {
        return false
    }

    @MainThread
    fun onWatchMeasurementResult(): Boolean {
        return false
    }

    @MainThread
    fun onWatchMeasurementStatusAndResult(): Boolean {
        return false
    }

    @MainThread
    fun onWatchPpiData(): Boolean {
        return false
    }

    @MainThread
    fun onWatchSportMode(): Boolean {
        return false
    }

    @MainThread
    fun onWatchSportModeControl(): Boolean {
        return false
    }

    @MainThread
    fun onWatchSwitchDial(): Boolean {
        return false
    }

    @MainThread
    fun onWatchSyncContacts(): Boolean {
        return false
    }

    @MainThread
    fun onWatchUpgradeResult(): Boolean {
        return false
    }
}