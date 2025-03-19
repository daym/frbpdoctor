package com.friendly_machines.frbpdoctor.ui.customization

//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.service.WatchCommunicationService
import kotlinx.coroutines.*

// TODO: Maybe move to WatchFaceController magic to WatchFaceActivity
/*
class WatchFaceDownloadingFragment : Fragment() {

    private var listener: WatchFaceDownloaderListener? = null
    private var downloadJob: Job? = null
    private lateinit var watchFaceDownloader: WatchFaceDownloader

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is WatchFaceDownloaderListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement WatchFaceDownloaderListener")
        }
        if (context is WatchFaceSelectionActivity) {
            watchFaceDownloader = context.getWatchFaceDownloader()
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_watchface_downloading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get parameters from arguments
        val mtu = arguments?.getByte("MTU") ?: 0
        val dialPlateId = arguments?.getInt("DIAL_PLATE_ID") ?: 0
        val blockNumber = arguments?.getShort("BLOCK_NUMBER") ?: 0
        val version = arguments?.getShort("VERSION") ?: 0
        val body = arguments?.getByteArray("BODY") ?: byteArrayOf()

        downloadWatchface(mtu, dialPlateId, blockNumber, version, body)
    }

    private fun downloadWatchface(
        mtu: Byte, dialPlateId: Int, blockNumber: Short, version: Short, body: ByteArray
    ) {
        downloadJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                watchFaceDownloader.downloadWatchface(mtu, dialPlateId, blockNumber, version, body)
                binder?.selectWatchface(dialPlateId)
                // Notify the activity of successful download and selection
                listener?.onWatchFaceDownloaded(dialPlateId)
            } catch (e: Exception) {
                // Handle download failure
                Log.e("WatchFaceDownloadingFragment", "Watchface download failed", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel the download coroutine if the view is destroyed
        downloadJob?.cancel()
    }

    companion object {
        fun newInstance(mtu: Byte, dialPlateId: Int, blockNumber: Short, version: Short, body: ByteArray): WatchFaceDownloadingFragment {
            val fragment = WatchFaceDownloadingFragment()
            val args = Bundle().apply {
                putByte("MTU", mtu)
                putInt("DIAL_PLATE_ID", dialPlateId)
                putShort("BLOCK_NUMBER", blockNumber)
                putShort("VERSION", version)
                putByteArray("BODY", body)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
package com.friendly_machines.frbpdoctor.ui.watchface

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class WatchFaceSelectionActivity : AppCompatActivity(), WatchFaceDownloaderListener {

    private lateinit var watchFaceDownloader: WatchFaceDownloader
    private lateinit var serviceConnection: ServiceConnection
    private var binder: IWatchBinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watchface_selection)

        // Initialize ServiceConnection
        serviceConnection = object : ServiceConnection {
            private var disconnector: IWatchBinder? = null
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                binder = service as IWatchBinder
                disconnector = binder?.addListener(watchFaceDownloader)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                // Handle disconnection
            }
        }

        // Bind to the service
        val serviceIntent = Intent(this, WatchCommunicationService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        // Initialize WatchFaceDownloader
        watchFaceDownloader = WatchFaceDownloader(serviceConnection)

        // Example of adding the fragment
        val watchFace = WatchFace(mtu = 0x3F, dialPlateId = 1, blockNumber = 0, version = 1, body = byteArrayOf())
        val fragment = WatchFaceDownloadingFragment.newInstance(
            watchFace.mtu, watchFace.dialPlateId, watchFace.blockNumber, watchFace.version, watchFace.body
        )

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    fun getWatchFaceDownloader(): WatchFaceDownloader {
        return watchFaceDownloader
    }

    override suspend fun onWatchFaceDownloaded(dialPlateId: Int) {
        // Handle the watchface download completion
    }
}

 */
class WatchFaceDownloadingFragment : Fragment() {
    private lateinit var serviceConnection: ServiceConnection
    private var binder: IWatchBinder? = null
    private var downloadJob: Job? = null

    // Note: Auto-created onCreate() was deleted by me.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_watch_face_downloading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get parameters from arguments or hosting activity
        // FIXME: WTF kind of defaults are those? TODO: Ask the user.
        val mtu = arguments?.getByte("MTU") ?: 0
        val dialPlateId = arguments?.getInt("DIAL_PLATE_ID") ?: 0
        val blockNumber = arguments?.getShort("BLOCK_NUMBER") ?: 0
        val version = arguments?.getShort("VERSION") ?: 0
        val body = arguments?.getByteArray("BODY") ?: byteArrayOf()

        serviceConnection = object : ServiceConnection {
            private var disconnector: IWatchBinder? = null
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                binder = service as IWatchBinder
                val watchFaceController = WatchFaceController(service as IWatchBinder)
                disconnector = binder?.addListener(watchFaceController!!)
                downloadJob = CoroutineScope(Dispatchers.Main).launch {
                    try {
                        watchFaceController.downloadWatchface(mtu, dialPlateId, blockNumber, version, body)
                        watchFaceController.listWatchFaces()
                        watchFaceController.selectWatchFace(dialPlateId)
                        // TODO: Notify the user of successful download and selection
                    } catch (e: Exception) {
                        // TODO: Handle download failure
                        Log.e("WatchFaceDownloadingFragment", "Watchface download failed", e)
                    }
                }
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                // Cancel the download coroutine
                downloadJob?.cancel()
                // Handle disconnection
            }
        }
        val serviceIntent = Intent(context, WatchCommunicationService::class.java)
        context?.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        context?.unbindService(serviceConnection)
        // Cancel the download coroutine if the view is destroyed
        downloadJob?.cancel()
    }

    companion object {
        fun newInstance(mtu: Byte, dialPlateId: Int, blockNumber: Short, version: Short, body: ByteArray): WatchFaceDownloadingFragment {
            val fragment = WatchFaceDownloadingFragment()
            val args = Bundle().apply {
                putByte("MTU", mtu)
                putInt("DIAL_PLATE_ID", dialPlateId)
                putShort("BLOCK_NUMBER", blockNumber)
                putShort("VERSION", version)
                putByteArray("BODY", body)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
