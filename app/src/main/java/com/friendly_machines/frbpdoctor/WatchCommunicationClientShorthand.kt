package com.friendly_machines.frbpdoctor

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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

    /**
     * Bind to the watch communication service, send the given command, and wait until expectedResponse comes back.
     * Then unbind from the watch communication service again.
     *
     * Limitations: If you run this command after some other communication, some stray response could come and be confused for the response of the new command you sent (the latter of which is actually still pending).
     * Precondition: Needs BLUETOOTH_CONNECT permission. See BluetoothPermissionHandler for help.
     */
    fun bindExecOneCommandUnbind(context: Context, expectedResponseType: WatchResponseType, callback: (IWatchBinder) -> Unit) {
        val serviceConnection = object : ServiceConnection {
            private var disconnector: IWatchBinder? = null
            private var currentListener: IWatchListener? = null
            private val timeoutHandler = Handler(Looper.getMainLooper())
            private var timeoutRunnable: Runnable? = null
            private var commandSent = false
            
            private fun cleanup() {
                // Cancel timeout
                timeoutRunnable?.let { runnable ->
                    timeoutHandler.removeCallbacks(runnable)
                }
                timeoutRunnable = null
                
                // Remove listener
                currentListener?.let { listener ->
                    disconnector?.removeListener(listener)
                }
                currentListener = null
                disconnector = null
                
                // Unbind service
                try {
                    context.unbindService(this)
                } catch (e: Exception) {
                    Log.e(TAG, "Error unbinding service: ${e.message}")
                }
            }
            
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as IWatchBinder
                
                // Android calls onServiceConnected multiple times if service crashes/restarts - clean up previous listener
                currentListener?.let { oldListener ->
                    disconnector?.removeListener(oldListener)
                }
                
                val listener = object : IWatchListener {
                    override fun onWatchResponse(response: WatchResponse) {
                        // Only process responses if we've actually sent a command
                        if (!commandSent) return
                        
                        when (binder.analyzeResponse(response, expectedResponseType)) {
                            WatchResponseAnalysisResult.Ok -> {
                                cleanup()
                                Log.d(SettingsFragment.TAG, "Command finished successfully with response $response")
                                return
                            }

                            WatchResponseAnalysisResult.Mismatch -> {
                                // Ignore the ones that have the wrong type, assuming that we will eventually get our response.
                                Log.d(TAG, "Ignoring mismatched response: $response, expected type: $expectedResponseType")
                            }

                            WatchResponseAnalysisResult.Err -> {
                                cleanup()
                                Log.e(SettingsFragment.TAG, "Command ended in unexpected response $response")
                                Toast.makeText(context, "Command ended in unexpected response $response", Toast.LENGTH_SHORT).show()
                                return
                            }
                        }
                    }
                    
                    override fun onException(exception: Throwable) {
                        if (commandSent) {
                            Log.e(TAG, "Exception during command execution: ${exception.message}")
                            cleanup()
                        }
                    }
                }
                currentListener = listener
                disconnector = binder.addListener(listener)
                
                // Set up timeout
                timeoutRunnable = Runnable {
                    if (commandSent) {
                        cleanup()
                        Log.e(TAG, "Command timed out, unbinding service")
                        Toast.makeText(context, "Watch command timed out", Toast.LENGTH_SHORT).show()
                    }
                }
                timeoutHandler.postDelayed(timeoutRunnable!!, 2000)
                
                // Mark that we've sent the command
                commandSent = true
                callback(binder)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                // Android only calls this when service crashes unexpectedly, not on normal unbind.
                // Multiple calls possible if service keeps crashing. Clean up safely.
                timeoutRunnable?.let { runnable ->
                    timeoutHandler.removeCallbacks(runnable)
                }
                timeoutRunnable = null
                currentListener?.let { listener ->
                    disconnector?.removeListener(listener)
                }
                currentListener = null
                disconnector = null
            }
        }

        val serviceIntent = Intent(context, WatchCommunicationService::class.java)
        if (!context.bindService(
                serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE
            )
        ) {
            Log.e(TAG, "Could not bind to WatchCommunicationService")
            return
        }
    }

    /**
     * Connect to the WatchCommunicationService, add listener, start a periodic task (on handler) that keeps calling callback every periodInMs ms. Uses up handler.
     * Return a handle that can be passed to unbindService.
     * Precondition: Needs BLUETOOTH_CONNECT permission. See BluetoothPermissionHandler for help.
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