package com.friendly_machines.fr_yhe_api.watchprotocol

import android.os.IBinder
import com.polidea.rxandroidble3.RxBleDevice

interface IWatchCommunicator {
    val binder: IWatchCommunication

    fun start(bleDevice: RxBleDevice, keyDigest: ByteArray)
    fun stop()
    // addListener result is untested.
    fun addListener(listener: IWatchListener): IWatchCommunication
    fun removeListener(listener: IWatchListener)
}