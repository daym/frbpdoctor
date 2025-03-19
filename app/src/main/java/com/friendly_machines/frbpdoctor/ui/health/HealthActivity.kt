package com.friendly_machines.frbpdoctor.ui.health

import android.annotation.SuppressLint
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import androidx.health.connect.client.units.Pressure
import androidx.health.connect.client.units.Temperature
import androidx.navigation.findNavController
import com.friendly_machines.fr_yhe_api.commondata.BpDataBlock
import com.friendly_machines.fr_yhe_api.commondata.HeatDataBlock
import com.friendly_machines.fr_yhe_api.commondata.SleepDataBlock
import com.friendly_machines.fr_yhe_api.commondata.SportDataBlock
import com.friendly_machines.fr_yhe_api.commondata.decodeIntegerDouble
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseType
import com.friendly_machines.fr_yhe_pro.command.WatchHGetBloodHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetSleepHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetSportHistoryCommand
import com.friendly_machines.fr_yhe_pro.command.WatchHGetTemperatureHistoryCommand
import com.friendly_machines.fr_yhe_pro.indication.DInflatedBloodMeasurementResult
import com.friendly_machines.fr_yhe_pro.indication.DSleepReminder
import com.friendly_machines.fr_yhe_pro.indication.DSportMode
import com.friendly_machines.fr_yhe_pro.indication.DSportModeControl
import com.friendly_machines.frbpdoctor.BluetoothPermissionHandler
import com.friendly_machines.frbpdoctor.MedBigResponseBuffer
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand
import com.friendly_machines.frbpdoctor.databinding.ActivityHealthBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.Instant

class HealthActivity : AppCompatActivity(), IWatchListener, MedBigResponseBuffer.IBigResponseListener {
    companion object {
        const val TAG: String = "HealthActivity"
    }

    private lateinit var handler: Handler
    private lateinit var binding: ActivityHealthBinding
//    private var appBarConfiguration: AppBarConfiguration? = null

    private val BLUETOOTH_PERMISSION_REQUEST_CODE: Int = 0x100
    private var serviceConnection: ServiceConnection? = null
    private val healthClient by lazy { HealthConnectClient.getOrCreate(this) }
    private val healthConnectChannelScope = CoroutineScope(Dispatchers.Main + Job())

    // Channel for queuing health records that need to be inserted
    private val recordsChannel = Channel<List<Record>>(Channel.BUFFERED)

    override fun onStart() {
        super.onStart()
        BluetoothPermissionHandler.start(this, BLUETOOTH_PERMISSION_REQUEST_CODE) {
            this.serviceConnection = WatchCommunicationClientShorthand.bindPeriodic(handler, 10000, this, this) { binder ->
                (binding.viewPager.adapter as HealthViewPagerAdapter).requestData(binding.viewPager.currentItem, binder)
            }
        }
    }

    override fun onStop() {
        handler.removeCallbacksAndMessages(null)
        serviceConnection?.let {
            unbindService(it)
            serviceConnection = null
        }
        super.onStop()
    }

    override fun onDestroy() {
        recordsChannel.close()
        healthConnectChannelScope.cancel()
        handler.removeCallbacksAndMessages(null)
        // Just to make sure
        serviceConnection?.let {
            unbindService(it)
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

        val adapter = HealthViewPagerAdapter(this)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(
            binding.tabs, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int -> tab.text = adapter.getTabTitle(position) }.attach()

        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()

            BluetoothPermissionHandler.start(this, BLUETOOTH_PERMISSION_REQUEST_CODE) {
                // UNSAFE since we don't know the response. It will keep the listener alive forever.
                WatchCommunicationClientShorthand.bindExecOneCommandUnbind(this@HealthActivity, WatchResponseType.SetProfile) { binder ->
                    binder.setMainTheme(1)
                }
            }
        }

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

            is WatchHGetBloodHistoryCommand.Response -> {
                supportFragmentManager.fragments.filterIsInstance<BloodPressureFragment>().forEach { fragment ->
                    fragment.setData(response.items.map {
                        BpDataBlock(systolicPressure = it.bloodSystolicPressure, diastolicPressure = it.bloodDiastolicPressure, pulse = 0U, timestamp = it.bloodStartTime) // FIXME
                    }.toTypedArray())
                }
                try {
                    queueHealthRecordsInsertion(response.items.map {
                        BloodPressureRecord(time = instantFromUnix(it.bloodStartTime), zoneOffset = null, systolic = Pressure.millimetersOfMercury(it.bloodSystolicPressure.toDouble()), diastolic = Pressure.millimetersOfMercury(it.bloodDiastolicPressure.toDouble())) // FIXME zoneOffset, measurementLocation
                    })
                    // TODO delete from watch maybe
                } catch (e: RuntimeException) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                }
            }

            is WatchHGetTemperatureHistoryCommand.Response -> {
                supportFragmentManager.fragments.filterIsInstance<HeatFragment>().forEach { fragment ->
                    fragment.setData(response.items.map {
                        HeatDataBlock(dayTimestamp = it.startTime, base = 0, sport = 0, walk = 0) // FIXME
                    }.toTypedArray())
                }
                try {
                    queueHealthRecordsInsertion(response.items.map {
                        BodyTemperatureRecord(time = instantFromUnix(it.startTime), zoneOffset = null, temperature = Temperature.celsius(decodeIntegerDouble(it.valueInt, it.valueFloat))) // FIXME zoneOffset, measurementLocation
                    })
                    // TODO delete from watch maybe
                } catch (e: RuntimeException) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                }
            }

            is WatchHGetSportHistoryCommand.Response -> {
                supportFragmentManager.fragments.filterIsInstance<SportFragment>().forEach { fragment ->
                    fragment.setData(response.items.map {
                        SportDataBlock(timestamp = it.startTime, sportType = 0U, avgHeartRate = 0U, heat = 0, runningDistance = it.distance, duration = (it.endTime - it.startTime).toUShort(), speed = 0, stepCount = it.steps.toInt()) // FIXME endTime, calories
                    }.toTypedArray())
                }
                try {
                    queueHealthRecordsInsertion(response.items.map {
                        // FIXME it.distance, it.steps, it.calories
                        ExerciseSessionRecord(startTime = instantFromUnix(it.startTime), endTime = instantFromUnix(it.endTime), exerciseRoute = null, startZoneOffset = null, endZoneOffset = null, exerciseType = 0) // FIXME zoneOffset, exerciseType, title, notes, segments, laps, exerciseRoute.
                    })
                    // TODO delete from watch maybe
                } catch (e: RuntimeException) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                }
            }

            is WatchHGetSleepHistoryCommand.Response -> {
                supportFragmentManager.fragments.filterIsInstance<SleepFragment>().forEach { fragment ->
                    fragment.setData(response.items.map {
                        SleepDataBlock(startTimestamp = 0U, endTimestamp = 0U, quality = 0) // FIXME !!!!
                    }.toTypedArray())
                }
//                    try {
//                    insertHealthRecords(response.items.map {
//                        // TODO: quality -> notes
//                        SleepSessionRecord(startTime = instantFromUnix(it.), endTime = instantFromUnix(it.), endZoneOffset = null, startZoneOffset = null) // TODO zone
//                    })
//                     TODO delete from watch maybe
//                } catch (e: RuntimeException) {
//            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
//        }
            }

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
        when (response) {
            is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetSleepData -> {
                supportFragmentManager.fragments.filterIsInstance<SleepFragment>().forEach { fragment ->
                    fragment.setData(response.data)
                }
            }

            is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetHeatData -> {
                supportFragmentManager.fragments.filterIsInstance<HeatFragment>().forEach { fragment ->
                    fragment.setData(response.data)
                }
            }

            is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetStepData -> {
                supportFragmentManager.fragments.filterIsInstance<StepsFragment>().forEach { fragment ->
                    fragment.setData(response.data)
                }
            }

            is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetBpData -> {
                supportFragmentManager.fragments.filterIsInstance<BloodPressureFragment>().forEach { fragment ->
                    fragment.setData(response.data)
                }
            }

            is com.friendly_machines.fr_yhe_med.WatchBigResponseMed.GetSportData -> {
                supportFragmentManager.fragments.filterIsInstance<SportFragment>().forEach { fragment ->
                    fragment.setData(response.data)
                }
            }

            else -> {

            }
        }
    }

    override fun onException(exception: Throwable) {
    }
}