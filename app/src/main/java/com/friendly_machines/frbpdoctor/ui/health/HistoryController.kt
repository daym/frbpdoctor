package com.friendly_machines.frbpdoctor.ui.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.frbpdoctor.MedBigResponseBuffer
import kotlinx.coroutines.channels.Channel
import kotlin.reflect.KClass

/**
 * Abstract base class for history data controllers.
 * Each controller handles a specific history command type and manages the bulk data collection process.
 *
 * Pattern: start command -> collect bulk responses -> confirm -> complete
 */
abstract class HistoryController<T>(
    protected val binder: IWatchBinder,
    protected val context: Context,
    protected val recyclerView: RecyclerView?,
    val onProgress: (current: Int, total: Int, message: String) -> Unit,
    val onComplete: () -> Unit,
    val onError: (error: Exception) -> Unit
) : IWatchListener, MedBigResponseBuffer.IBigResponseListener {
    protected val responseChannel = Channel<Any>(Channel.CONFLATED) // buffers only most recent message; replacing stuff
    protected val collectedItems = mutableListOf<T>()
    protected val healthClient by lazy { HealthConnectClient.getOrCreate(context) }

    protected abstract val responseTypes: Set<KClass<out WatchResponse>>
    protected abstract val medBigResponseTypes: Set<KClass<out com.friendly_machines.fr_yhe_med.WatchBigResponseMed>>
    public abstract suspend fun startCollection(): Unit
    public fun close() {
        // Here is the exact text from the official Kotlin documentation for the close(cause: Throwable? = null): Boolean function:
        //
        // "Closes this channel. This is an idempotent operation — subsequent invocations of this function have no effect and return false. A channel that was closed without a cause can be closed again with a cause.
        //
        // The result is true if the channel was closed by this invocation, false if it was already closed.
        //
        // A channel is closed when this function is called on it. All subsequent send attempts will throw ClosedSendChannelException.
        //
        // A channel is closed for receiving when it is closed and all its elements have been received. Subsequent receive attempts will throw ClosedReceiveChannelException.
        //
        // **This function wakes up all suspended senders and receivers on this channel with this exception.**
        responseChannel.close()
        // our derived classes do it: HistoryControllerRegistry.completeOperation(responseTypes, medBigResponseTypes)
    }

    override fun onWatchResponse(response: WatchResponse) {
        if (responseTypes.contains(response::class)) {
            val result = responseChannel.trySend(response)
            if (result.isFailure) {
                Log.e(TAG, "Failed to send WatchResponse ${response::class.simpleName}: ${result.exceptionOrNull()}")
                responseChannel.close()
            }
        }
    }

    override fun onBigWatchResponse(response: com.friendly_machines.fr_yhe_med.WatchBigResponseMed) {
        if (medBigResponseTypes.contains(response::class)) {
            val result = responseChannel.trySend(response)
            if (result.isFailure) {
                Log.e(TAG, "Failed to send WatchBigResponseMed ${response::class.simpleName}: ${result.exceptionOrNull()}")
                responseChannel.close()
            }
        }
    }

    companion object {
        private const val TAG = "HistoryController"
    }
}