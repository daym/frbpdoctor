package com.friendly_machines.frbpdoctor.ui.customization

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
import android.widget.Button
import android.widget.LinearLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.WatchChangeWatchDialAction
import com.friendly_machines.fr_yhe_api.commondata.WatchDialDataBlock
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.service.WatchCommunicationService
import kotlinx.coroutines.*

class WatchFaceFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    
    // Service connection and WatchFaceController
    private lateinit var serviceConnection: ServiceConnection
    private var binder: IWatchBinder? = null
    private var watchFaceController: WatchFaceController? = null
    private var disconnector: IWatchBinder? = null
    private var uploadJob: Job? = null
    
    // Upload progress UI elements
    private var uploadProgressLayout: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_watch_face, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI elements
        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        this.recyclerView = recyclerView
        
        uploadProgressLayout = view.findViewById(R.id.uploadProgressLayout)

        val addWatchDialButton = view.findViewById<FloatingActionButton>(R.id.addWatchDialButton)
        addWatchDialButton.setOnClickListener {
            val editWatchFaceDialog = EditWatchFaceDialog(WatchChangeWatchDialAction.Add)
            editWatchFaceDialog.addListener(object : EditWatchFaceDialog.OnWatchDialSetListener {
                override fun onWatchDialSet(watchface: WatchfaceCatalogItem) {
                    // Start upload process directly instead of launching separate fragment
                    startWatchfaceUpload(watchface)
                }
            })
            editWatchFaceDialog.show(childFragmentManager, "edit_watch_dial_dialog")
        }

        val chooseWatchDialButton = view.findViewById<Button>(R.id.chooseWatchDialButton)
        chooseWatchDialButton.setOnClickListener {
            if (recyclerView != null && recyclerView.adapter != null && watchFaceController != null) {
                val id = (recyclerView!!.adapter as WatchFaceAdapter).getSelectedItemId()
                id?.let { dialId ->
                    lifecycleScope.launch {
                        try {
                            watchFaceController?.selectWatchFace(dialId)
                        } catch (e: Exception) {
                            Log.e("WatchFaceFragment", "Failed to select watch face", e)
                        }
                    }
                }
            }
        }

        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            cancelUpload()
        }

        // Set up service connection
        setupServiceConnection()
    }

    fun setData(data: List<WatchDialDataBlock>) {
        val adapter = WatchFaceAdapter(data.sortedBy { it.id })
        recyclerView!!.adapter = adapter
        adapter.notifyDataSetChanged()
    }
    
    private fun setupServiceConnection() {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                binder = service as IWatchBinder
                
                // Android calls onServiceConnected multiple times if service crashes/restarts - clean up previous controller
                watchFaceController?.let { oldController ->
                    disconnector?.removeListener(oldController)
                }
                
                watchFaceController = WatchFaceController(service as IWatchBinder) { percentage, status ->
                    setProgress(percentage, status)
                }
                watchFaceController?.let { controller ->
                    disconnector = binder?.addListener(controller)
                }
                
                // Load initial watch face list
                loadWatchFaces()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                // Android calls this when service crashes unexpectedly, not on normal unbind
                // Multiple calls possible if service keeps crashing. Clean up safely.
                watchFaceController?.let { controller ->
                    disconnector?.removeListener(controller)
                }
                watchFaceController = null
                disconnector = null
                uploadJob?.cancel()
            }
        }
        
        val serviceIntent = Intent(context, WatchCommunicationService::class.java)
        context?.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    private fun loadWatchFaces() {
        lifecycleScope.launch {
            try {
                watchFaceController?.listWatchFaces()
                // Data will be provided via CustomizationActivity's setData() method
            } catch (e: Exception) {
                Log.e("WatchFaceFragment", "Failed to load watch faces", e)
            }
        }
    }
    
    private fun startWatchfaceUpload(watchface: WatchfaceCatalogItem) {
        // Load the .bin file for the selected watchface
        val binData = WatchFaceCatalogLoader.loadWatchfaceBinary(requireContext(), watchface)
        if (binData == null) {
            showError("Failed to load watch face data")
            return
        }
        
        // Show upload UI and disable controls
        setUploadMode(true)
        
        uploadJob = lifecycleScope.launch {
            try {
                val mtu = 235.toByte() // Standard BLE MTU - 9 bytes overhead = 235
                val dialPlateId = watchface.getDialPlateIdInt()
                val blockNumber = 0.toShort() // Default for new uploads
                val version = 1.toShort() // Default version
                
                watchFaceController?.downloadWatchface(mtu, dialPlateId, blockNumber, version, binData)
                watchFaceController?.listWatchFaces()
                watchFaceController?.selectWatchFace(dialPlateId)
                
                setProgress(100f, "Upload completed successfully!")
                
                // Hide upload UI after brief delay
                delay(2000)
                setUploadMode(false)
                
            } catch (e: Exception) {
                Log.e("WatchFaceFragment", "Watch face upload failed", e)
                showError("Upload failed: ${e.message}")
                setUploadMode(false)
            }
        }
    }
    
    private fun setUploadMode(uploading: Boolean) {
        requireActivity().runOnUiThread {
            uploadProgressLayout?.visibility = if (uploading) View.VISIBLE else View.GONE
            recyclerView?.isEnabled = !uploading
            view?.findViewById<FloatingActionButton>(R.id.addWatchDialButton)?.isEnabled = !uploading
            view?.findViewById<Button>(R.id.chooseWatchDialButton)?.isEnabled = !uploading
        }
    }
    
    private fun setProgress(text: String) {
        requireActivity().runOnUiThread {
            view?.findViewById<android.widget.TextView>(R.id.downloadStatus)?.text = text
        }
    }
    
    private fun setProgress(progress: Float, text: String) {
        requireActivity().runOnUiThread {
            view?.findViewById<android.widget.ProgressBar>(R.id.downloadProgressBar)?.progress = progress.toInt()
            view?.findViewById<android.widget.TextView>(R.id.downloadProgressText)?.text = "${progress.toInt()}%"
            view?.findViewById<android.widget.TextView>(R.id.downloadStatus)?.text = text
        }
    }
    
    private fun cancelUpload() {
        uploadJob?.cancel()
        setUploadMode(false)
    }
    
    private fun showError(message: String) {
        requireActivity().runOnUiThread {
            view?.findViewById<android.widget.TextView>(R.id.downloadStatus)?.text = message
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        watchFaceController?.let { controller ->
            disconnector?.removeListener(controller)
        }
        context?.unbindService(serviceConnection)
        uploadJob?.cancel()
        watchFaceController = null
        disconnector = null
    }
}