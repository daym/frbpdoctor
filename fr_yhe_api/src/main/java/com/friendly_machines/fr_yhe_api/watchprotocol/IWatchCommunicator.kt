package com.friendly_machines.fr_yhe_api.watchprotocol

import androidx.annotation.MainThread
import com.polidea.rxandroidble3.RxBleDevice

interface IWatchCommunicator {
    val binder: IWatchBinder

    @MainThread
    fun start(bleDevice: RxBleDevice, keyDigest: ByteArray)

    @MainThread
    fun stop()

    // addListener result is untested.
    @MainThread
    fun addListener(listener: IWatchListener): IWatchBinder

    @MainThread
    fun removeListener(listener: IWatchListener)
}