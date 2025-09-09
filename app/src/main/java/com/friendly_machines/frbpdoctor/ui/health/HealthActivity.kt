package com.friendly_machines.frbpdoctor.ui.health

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.friendly_machines.frbpdoctor.ui.BaseActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.navigation.findNavController
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseType
import com.friendly_machines.fr_yhe_pro.indication.DInflatedBloodMeasurementResult
import com.friendly_machines.fr_yhe_pro.indication.DSleepReminder
import com.friendly_machines.fr_yhe_pro.indication.DSportMode
import com.friendly_machines.fr_yhe_pro.indication.DSportModeControl
import com.friendly_machines.frbpdoctor.BluetoothPermissionHandler
import com.friendly_machines.frbpdoctor.MedBigResponseBuffer
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand
import com.friendly_machines.frbpdoctor.databinding.ActivityHealthBinding
import com.friendly_machines.frbpdoctor.service.WatchCommunicationService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.Instant

class HealthActivity : BaseActivity(), IWatchListener, MedBigResponseBuffer.IBigResponseListener {
    companion object {
        const val TAG: String = "HealthActivity"
    }

    private lateinit var handler: Handler
    private lateinit var binding: ActivityHealthBinding
//    private var appBarConfiguration: AppBarConfiguration? = null

    private val BLUETOOTH_PERMISSION_REQUEST_CODE: Int = 0x100
    private var serviceConnection: ServiceConnection? = null
    var watchBinder: IWatchBinder? = null
        private set
    private var disconnector: IWatchBinder? = null
    private val healthClient by lazy { HealthConnectClient.getOrCreate(this) }
    private val healthConnectChannelScope = CoroutineScope(Dispatchers.Main + Job())

    // Channel for queuing health records that need to be inserted
    private val recordsChannel = Channel<List<Record>>(Channel.BUFFERED)

    override fun onStart() {
        super.onStart()
        BluetoothPermissionHandler.start(this, BLUETOOTH_PERMISSION_REQUEST_CODE) {
            // Create ServiceConnection directly like WatchFaceDownloadingFragment
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    watchBinder = service as IWatchBinder
                    disconnector = watchBinder?.addListener(this@HealthActivity)
                    
                    // Data refresh is now handled by individual controllers
                    // No need for periodic refresh as controllers manage their own data
                }
                
                override fun onServiceDisconnected(name: ComponentName?) {
                    disconnector?.let { disc ->
                        disc.removeListener(this@HealthActivity)
                    }
                    disconnector = null
                    watchBinder = null
                    handler.removeCallbacksAndMessages(null)
                }
            }
            
            serviceConnection = connection
            val serviceIntent = Intent(this, WatchCommunicationService::class.java)
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        handler.removeCallbacksAndMessages(null)
        disconnector?.let { disc ->
            disc.removeListener(this@HealthActivity)
        }
        disconnector = null
        serviceConnection?.let { connection ->
            unbindService(connection)
            serviceConnection = null
        }
        watchBinder = null
        super.onStop()
    }

    override fun onDestroy() {
        recordsChannel.close()
        healthConnectChannelScope.cancel()
        handler.removeCallbacksAndMessages(null)
        // Just to make sure
        serviceConnection?.let { connection ->
            unbindService(connection)
            serviceConnection = null
        }

        super.onDestroy()
    }

    private val healthPermissions = setOf(
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getWritePermission(BloodPressureRecord::class),

        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),

        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),

        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getWritePermission(OxygenSaturationRecord::class),

        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        HealthPermission.getWritePermission(BloodGlucoseRecord::class),

        HealthPermission.getReadPermission(BodyTemperatureRecord::class),
        HealthPermission.getWritePermission(BodyTemperatureRecord::class),

        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getWritePermission(SleepSessionRecord::class),

        HealthPermission.getReadPermission(RespiratoryRateRecord::class),
        HealthPermission.getWritePermission(RespiratoryRateRecord::class),

        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),

        // TODO Fall
        // TODO Humidity
    )

    private suspend fun requestHealthPermissions(healthConnectClient: HealthConnectClient, permissions: Set<String>): Boolean {
        val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(healthPermissions)) {
            return true
        }
        return suspendCancellableCoroutine { continuation ->
            val launcher = registerForActivityResult(requestPermissionActivityContract) { granted ->
                continuation.resume(
                    //val shouldShowRationale = currentActivity.shouldShowRequestPermissionRationale(it)
                    //PermissionDenied(shouldShowRationale)
                    granted.containsAll(permissions)
                ) {
                    // FIXME cancellation cleanup
                }
            }
            launcher.launch(permissions)
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }

    private suspend fun checkAndRequestPermissions(): Boolean {
        // Check if we already have permissions
        val granted = healthClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(healthPermissions)) {
            return true
        }

        // If not, request them
        return suspendCancellableCoroutine { continuation ->
            val launcher = registerForActivityResult(
                PermissionController.createRequestPermissionResultContract()
            ) { permissions ->
                continuation.resume(permissions.containsAll(healthPermissions)) { // cancellation
                    cause -> // FIXME launcher.unregister()
                }
            }
            launcher.launch(healthPermissions)
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Start processing health records
        healthConnectChannelScope.launch {
            // First ensure we have permissions
            val granted = try {
                checkAndRequestPermissions()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get permissions", e)
                return@launch
            }

            if (!granted) {
                Log.e(TAG, "Permissions not granted")
                return@launch
            }

            // Then process records as they arrive
            for (records in recordsChannel) {
                try {
                    healthClient.insertRecords(records)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to insert records", e)
                }
            }
        }

        bigBuffers.listener = this

//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)

        binding = ActivityHealthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_health)
        //setupActionBarWithNavController(this, navController)
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
//        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        // Set up bottom navigation
        setupBottomNavigation()
        
        // Show vitals fragment by default
        showVitalsFragment()


        handler = Handler(Looper.getMainLooper())

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val availabilityStatus = HealthConnectClient.getSdkStatus(this)
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            return // early return as there is no viable integration
        }
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            // TODO Optionally redirect to package installer to find a provider
            return
        }
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            android.R.id.home -> {
//                // Navigate back to the previous activity
//                finish()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        // onBackPressed()
        return true
    }

    /** Insert RECORDS into the health database on the phone. */
    private fun queueHealthRecordsInsertion(records: List<Record>) {
        healthConnectChannelScope.launch {
            recordsChannel.send(records)
        }
    }

    private fun instantFromUnix(time: UInt): Instant = Instant.ofEpochSecond(time.toLong())
    @SuppressLint("RestrictedApi") // FIXME
    override fun onWatchResponse(response: WatchResponse) {
        super.onWatchResponse(response)
        when (response) {
            // = Pro =; TODO: add commondata interface

            // Blood pressure handling moved to BloodHistoryController

            // Temperature history handling moved to HeatHistoryController

            // Sport history handling moved to SportHistoryController

            // Sleep history handling moved to SleepHistoryController

            is DInflatedBloodMeasurementResult -> {
                // TODO
            }
            is DSleepReminder -> {
                // TODO
            }
            is DSportMode -> {
                // TODO
            }
            is DSportModeControl -> {
                // TODO
            }
        }
    }

    private val bigBuffers = MedBigResponseBuffer()
    override fun onBigWatchResponse(response: com.friendly_machines.fr_yhe_med.WatchBigResponseMed) {
        Log.d(TAG, "-> big decoded: $response")
    }

    override fun onException(exception: Throwable) {
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_vitals -> {
                    showVitalsFragment()
                    true
                }
                R.id.nav_lifestyle -> {
                    showLifestyleFragment()
                    true
                }
                R.id.nav_reports -> {
                    showReportsFragment()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun showVitalsFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_content, VitalsFragment())
            .commit()
    }
    
    private fun showLifestyleFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_content, LifestyleFragment())
            .commit()
    }
    
    private fun showReportsFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_content, ReportsFragment())
            .commit()
    }
}