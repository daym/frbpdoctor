package com.friendly_machines.frbpdoctor

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseAnalysisResult
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseType
import com.friendly_machines.frbpdoctor.service.WatchCommunicationService
import com.friendly_machines.frbpdoctor.ui.settings.SettingsFragment

object WatchCommunicationClientShorthand {
    private const val TAG = "WatchCommunicationClientShorthand"

    /** Note: It's mandatory that callback calls serviceConnection.addListener(), remembers the result and returns it */
    private fun bind(context: Context, callback: (ServiceConnection, IWatchBinder) -> IWatchBinder): ServiceConnection? {
        val serviceConnection = object : ServiceConnection {
            private var disconnector: IWatchBinder? = null
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as IWatchBinder
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
            )
        ) {
            Log.e(TAG, "Could not bind to WatchCommunicationService")
            return null
        }
        return serviceConnection
    }

    /**
     * Bind to the watch communication service, send the given command, and wait until expectedResponse comes back.
     * Then unbind from the watch communication service again.
     *
     * Limitations: If you run this command after some other communication, some stray response could come and be confused for the response of the new command you sent (the latter of which is actually still pending).
     */
    fun bindExecOneCommandUnbind(context: Context, expectedResponseType: WatchResponseType, callback: (IWatchBinder) -> Unit) {
        bind(context) { _, binder ->
            val disconnector = binder.addListener(object : IWatchListener {
                override fun onWatchResponse(response: WatchResponse) {
                    when (binder.analyzeResponse(response, expectedResponseType)) {
                        WatchResponseAnalysisResult.Ok -> {
                            // FIXME context.unbindService(serviceConnection)
                            Log.d(SettingsFragment.TAG, "Command finished successfully with response $response")
                            return
                        }

                        WatchResponseAnalysisResult.Mismatch -> {
                            // Ignore the ones that have the wrong type, assuming that we will eventually get our response.
                        }

                        WatchResponseAnalysisResult.Err -> {
                            Log.e(SettingsFragment.TAG, "Command ended in unexpected response $response")
                            Toast.makeText(context, "Command ended in unexpected response $response", Toast.LENGTH_SHORT).show()
                        }
                        // FIXME context.unbindService(serviceConnection)
                    }
                }
            })
            callback(binder)
            disconnector
        }
    }

    /**
     * Connect to the WatchCommunicationService, add listener, start a periodic task (on handler) that keeps calling callback every periodInMs ms. Uses up handler.
     * Return a handle that can be passed to unbindService.
     */
    fun bindPeriodic(handler: Handler, periodInMs: Long, context: Context, listener: IWatchListener, callback: (IWatchBinder) -> Unit): ServiceConnection? {
        val serviceConnection = object : ServiceConnection {
            private var disconnector: IWatchBinder? = null
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as IWatchBinder
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
                // FIXME disconnector!!.removeListener(listener)
            }
        }
        val serviceIntent = Intent(context, WatchCommunicationService::class.java)
        if (!context.bindService(
                serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE
            )
        ) {
            Log.e(TAG, "Could not bind to WatchCommunicationService")
            return null
        }
        return serviceConnection
    }
}