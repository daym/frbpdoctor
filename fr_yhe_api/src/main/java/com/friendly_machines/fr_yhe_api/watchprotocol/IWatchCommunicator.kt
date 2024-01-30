package com.friendly_machines.fr_yhe_api.watchprotocol

import com.polidea.rxandroidble3.RxBleDevice

interface IWatchCommunicator {
    val binder: IWatchBinder

    fun start(bleDevice: RxBleDevice, keyDigest: ByteArray)
    fun stop()

    // addListener result is untested.
    fun addListener(listener: IWatchListener): IWatchBinder
    fun removeListener(listener: IWatchListener)
}