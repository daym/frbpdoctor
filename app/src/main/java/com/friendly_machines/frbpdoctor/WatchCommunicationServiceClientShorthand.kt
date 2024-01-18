package com.friendly_machines.frbpdoctor

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.friendly_machines.frbpdoctor.service.WatchCommunicationService
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchListener

object WatchCommunicationServiceClientShorthand {
    private const val TAG = "WatchCommunicationServiceClientShorthand"
    /** Note: It's mandatory that callback calls serviceConnection.addListener(), remembers the result and returns it */
    fun bind(context: Context, callback: (ServiceConnection, WatchCommunicationService.WatchCommunicationServiceBinder) -> WatchCommunicationService): ServiceConnection? {
        val serviceConnection = object : ServiceConnection {
            private var disconnector: WatchCommunicationService? = null
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as WatchCommunicationService.WatchCommunicationServiceBinder
                callback(this, binder).let {
                    assert(this.disconnector == null)
                    this.disconnector = it
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                //cleanup()
                disconnector?.let {
                    it.removeListener(it)
                }
            }
        }

        val serviceIntent = Intent(context, WatchCommunicationService::class.java)
        if (!context.bindService(
            serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE
        )) {
            Log.e(TAG, "Could not bind to WatchCommunicationService")
            return null
        }
        return serviceConnection
    }

    /**
     * Connect to the WatchCommunicationService, add listener, start a periodic task (on handler) that keeps calling callback every periodInMs ms. Uses up handler.
     * Return a handle that can be passed to unbindService.
     */
    fun bindPeriodic(handler: Handler, periodInMs: Long, context: Context, listener: WatchListener, callback: (WatchCommunicationService.WatchCommunicationServiceBinder) -> Unit): ServiceConnection? {
        val serviceConnection = object : ServiceConnection {
            private var disconnector: WatchCommunicationService? = null
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as WatchCommunicationService.WatchCommunicationServiceBinder
                disconnector = binder.addListener(listener)
                val periodicTask: Runnable = object : Runnable {
                    override fun run() {
                        callback(binder)
                        handler.postDelayed(this, periodInMs /* ms */)
                    }
                }
                handler.postDelayed(periodicTask, periodInMs /* ms */)
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                handler.removeCallbacksAndMessages(null)
                disconnector!!.removeListener(listener)
            }
        }
        val serviceIntent = Intent(context, WatchCommunicationService::class.java)
        if (!context.bindService(
                serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE
            )) {
            Log.e(TAG, "Could not bind to WatchCommunicationService")
            return null
        }
        return serviceConnection
    }
}