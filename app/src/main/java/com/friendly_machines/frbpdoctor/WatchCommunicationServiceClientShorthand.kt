package com.friendly_machines.frbpdoctor

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.friendly_machines.frbpdoctor.service.NotificationListener
import com.friendly_machines.frbpdoctor.service.WatchCommunicationService
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchListener

object WatchCommunicationServiceClientShorthand {
    private const val TAG = "WatchCommunicationServiceClientShorthand"
    fun bind(context: Context, callback: (WatchCommunicationService.WatchCommunicationServiceBinder) -> Unit): Boolean {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as WatchCommunicationService.WatchCommunicationServiceBinder
                callback(binder)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                //cleanup()
            }
        }

        val serviceIntent = Intent(context, WatchCommunicationService::class.java)
        if (!context.bindService(
            serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE
        )) {
            Log.e(TAG, "Could not bind to WatchCommunicationService")
            return false
        }
        return true
    }

    fun bindPeriodic(handler: Handler, periodMs: Long, context: Context, listener: WatchListener, callback: (WatchCommunicationService.WatchCommunicationServiceBinder) -> Unit): ServiceConnection? {
        val serviceConnection = object : ServiceConnection {
            private var disconnector: WatchCommunicationService? = null
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as WatchCommunicationService.WatchCommunicationServiceBinder
                disconnector = binder.addListener(listener)
                val periodicTask: Runnable = object : Runnable {
                    override fun run() {
                        callback(binder)

                        handler.postDelayed(this, periodMs /* ms */)
                    }
                }
                handler.postDelayed(periodicTask, periodMs /* ms */)
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
            return serviceConnection
        }
        return null
    }
}