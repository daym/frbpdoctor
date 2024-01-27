package com.friendly_machines.frbpdoctor.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.MyApplication
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.watchprotocol.bluetooth.WatchCharacteristic
import com.polidea.rxandroidble3.RxBleClient
import com.polidea.rxandroidble3.RxBleDevice
import com.polidea.rxandroidble3.scan.ScanFilter
import com.polidea.rxandroidble3.scan.ScanResult
import com.polidea.rxandroidble3.scan.ScanSettings
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

class ScannerFragment(private val resultListener: ScannerResultListener) : /* ListFragment */ DialogFragment(), MyScannerRecyclerViewAdapter.ItemClickListener {
    private lateinit var adapter: MyScannerRecyclerViewAdapter
    private val scanResults: MutableList<ScanResult> = mutableListOf()

    interface ScannerResultListener {
        fun onScanningUserSelectedDevice(scanResult: ScanResult)
    }

    private fun withBluetoothPermissions(callback: () -> Unit) {
        if (MyApplication.rxBleClient.isConnectRuntimePermissionGranted) {
            callback()
        } else {
//                Manifest.permission.BLUETOOTH_CONNECT,
//                Manifest.permission.BLUETOOTH_SCAN,
//                Manifest.permission.ACCESS_FINE_LOCATION
            // TODO RequestMultiplePermissions ?
            val bluetoothPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isPermissionGranted ->
                if (isPermissionGranted) {
                    callback()
                } else {
                    // FIXME ?!
                }
            }
            bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN)
        }
    }

    private fun scan() {
        val rxBleClient = RxBleClient.create(requireContext())
        val scanFilter = ScanFilter.Builder().setServiceUuid(WatchCharacteristic.serviceUuid).build()
        rxBleClient.scanBleDevices(
            ScanSettings.Builder()
                //.setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
                .build(), scanFilter
        ).subscribe(object : Observer<ScanResult> {
            override fun onSubscribe(d: Disposable) {
                Log.d(TAG, "on subscribe")
            }

            override fun onError(e: Throwable) {
                Log.d(TAG, "Scan onError", e)
                Toast.makeText(requireContext(), "Error while scanning: $e", Toast.LENGTH_LONG).show()
            }

            override fun onComplete() {
                Log.d(TAG, "on complete")
                // TODO nicer thing
                Toast.makeText(requireContext(), "Scanning complete", Toast.LENGTH_SHORT).show()
            }

            override fun onNext(scanResult: ScanResult) {
                val device: RxBleDevice = scanResult.bleDevice
                Log.d(TAG, "Scan - " + device.name)
                val f = scanResults.find { it.bleDevice == device }
                if (f == null) {
                    scanResults.add(scanResult)
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scanner, container, false)
        val that = this
        val list = view.findViewById<RecyclerView>(R.id.list)
        with(list) {
            that.adapter = MyScannerRecyclerViewAdapter(scanResults, that)
            adapter = that.adapter
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        withBluetoothPermissions {
            scan()
        }
    }

    companion object {
        const val TAG = "ScannerFragment"
    }

    override fun onItemClick(position: Int) {
        val scanResult = scanResults[position]
        resultListener.onScanningUserSelectedDevice(scanResult)
        dismiss()  // Close the dialog
    }
}