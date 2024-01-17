package com.friendly_machines.frbpdoctor.ui.settings

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.MyApplication
import com.friendly_machines.frbpdoctor.R
import com.polidea.rxandroidble3.RxBleClient
import com.polidea.rxandroidble3.RxBleDevice
import com.polidea.rxandroidble3.scan.ScanFilter
import com.polidea.rxandroidble3.scan.ScanResult
import com.polidea.rxandroidble3.scan.ScanSettings
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable

/**
 * A fragment representing a list of Items.
 */
class ScannerFragment(private val resultListener: ScannerResultListener) : /* ListFragment */ DialogFragment(), MyScannerRecyclerViewAdapter.OnItemClickListener {
    private lateinit var adapter: MyScannerRecyclerViewAdapter
    private lateinit var scanResults: MutableList<ScanResult>
    private var columnCount = 1

    interface ScannerResultListener {
        fun onScanningUserSelectedDevice(scanResult: ScanResult)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanResults = mutableListOf()

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
        if (MyApplication.rxBleClient.isConnectRuntimePermissionGranted) {
            scan()
        } else {
            requestConnectionPermission(MyApplication.rxBleClient)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scanner_list, container, false)
        val that = this

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                that.adapter = MyScannerRecyclerViewAdapter(scanResults,  that)
                adapter = that.adapter
            }
        }
        return view
    }

    companion object {
        const val TAG = "ScannerFragment"

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int, resultListener: ScannerResultListener) =
            ScannerFragment(resultListener).apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

    override fun onItemClick(position: Int) {
        val scanResult = scanResults[position]
        resultListener.onScanningUserSelectedDevice(scanResult)
        dismiss()  // Close the dialog
    }

    //    private fun requestBluetoothPermission() {
//        ActivityCompat.requestPermissions(
//            this,
//            arrayOf(
//                Manifest.permission.BLUETOOTH_CONNECT,
//                Manifest.permission.BLUETOOTH_SCAN,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ),
//            BLUETOOTH_SCAN_PERMISSION_REQUEST_CODE
//        )
//    }

    private val REQUEST_PERMISSION_BLE_CONNECT = 102
    private fun requestConnectionPermission(client: RxBleClient) =
        ActivityCompat.requestPermissions(
            requireActivity(),
            /*
             * the below would cause a ArrayIndexOutOfBoundsException on API < 31. Yet it should not be called then as runtime
             * permissions are not needed and RxBleClient.isConnectRuntimePermissionGranted() returns `true`
             */
            arrayOf(client.recommendedConnectRuntimePermissions[0]),
            REQUEST_PERMISSION_BLE_CONNECT
        )

    private fun isConnectionPermissionGranted(requestCode: Int, grantResults: IntArray) =
        requestCode == REQUEST_PERMISSION_BLE_CONNECT && grantResults[0] == PackageManager.PERMISSION_GRANTED

    @Deprecated("Deprecated in Android")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isConnectionPermissionGranted(requestCode, grantResults)) {
            scan()
        }
    }

    private fun scan() {
        val rxBleClient = RxBleClient.create(requireContext())
        val scanFilter = ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString("0000fe51-0000-1000-8000-00805f9b34fb")).build()

        var scanSubscription = rxBleClient.scanBleDevices(
            ScanSettings.Builder()
                //.setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
                .build(),
            scanFilter
        )
            .subscribe(object : Observer<ScanResult> {
                override fun onSubscribe(d: Disposable) {
                    Log.d(TAG, "on subscribe")
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, "Scan onError", e)
                }

                override fun onComplete() {
                    Log.d(TAG, "on complete")
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
            }
            )
    }

}