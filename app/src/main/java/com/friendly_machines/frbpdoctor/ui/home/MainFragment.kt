package com.friendly_machines.frbpdoctor.ui.home

import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.friendly_machines.fr_yhe_api.commondata.RealDataSensorType
import com.friendly_machines.fr_yhe_api.commondata.RealDataMeasureType
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchListener
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchRawResponse
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponse
import com.friendly_machines.fr_yhe_pro.indication.RBloodOxygen
import com.friendly_machines.fr_yhe_pro.indication.RBloodPressure
import com.friendly_machines.fr_yhe_pro.indication.RComprehensive
import com.friendly_machines.fr_yhe_pro.indication.RHeart
import com.friendly_machines.fr_yhe_pro.indication.ROga
import com.friendly_machines.fr_yhe_pro.indication.RRespiration
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand
import com.google.android.flexbox.FlexboxLayout
import java.io.IOException
import java.io.InputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment(), IWatchListener {
    private lateinit var svgImageView: ImageView
    private lateinit var measurementsContainer: FlexboxLayout
    private lateinit var handler: Handler
    private var serviceConnection: ServiceConnection? = null
    private val activeMeasurements = mutableSetOf<RealDataSensorType>()
    private val measurementViews = mutableMapOf<RealDataSensorType, TextView>()

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    data class MeasurementData(
        val name: String,
        val unit: String,
        val sensorType: RealDataSensorType,
        val icon: Int = R.drawable.ic_measurement_default
    )

    private val measurements = listOf(
        MeasurementData("Sport", "steps", RealDataSensorType.SPORT, R.drawable.ic_sensor_sport),
        MeasurementData("Heart Rate", "bpm", RealDataSensorType.HEART, R.drawable.ic_sensor_heart),
        MeasurementData("Blood Oxygen", "%", RealDataSensorType.BLOOD_OXYGEN, R.drawable.ic_sensor_blood_oxygen),
        MeasurementData("Blood Pressure", "mmHg", RealDataSensorType.BLOOD_PRESSURE, R.drawable.ic_sensor_blood_pressure),
        MeasurementData("PPG", "signal", RealDataSensorType.PPG, R.drawable.ic_sensor_ppg),
        MeasurementData("ECG", "signal", RealDataSensorType.ECG, R.drawable.ic_sensor_ecg),
        MeasurementData("Run", "steps", RealDataSensorType.RUN, R.drawable.ic_sensor_run),
        MeasurementData("Respiration", "rpm", RealDataSensorType.RESPIRATION, R.drawable.ic_sensor_respiration),
        MeasurementData("Sensor", "data", RealDataSensorType.SENSOR, R.drawable.ic_sensor_sensor),
        MeasurementData("Ambient Light", "lux", RealDataSensorType.AMBIENT_LIGHT, R.drawable.ic_sensor_ambient_light),
        MeasurementData("Comprehensive", "health", RealDataSensorType.COMPREHENSIVE, R.drawable.ic_sensor_comprehensive),
        MeasurementData("Schedule", "event", RealDataSensorType.SCHEDULE, R.drawable.ic_sensor_schedule),
        MeasurementData("Event Reminder", "alert", RealDataSensorType.EVENT_REMINDER, R.drawable.ic_sensor_event_reminder),
        MeasurementData("All Data", "data", RealDataSensorType.OGA, R.drawable.ic_sensor_oga),
        MeasurementData("Inflated BP", "mmHg", RealDataSensorType.INFLATED_BLOOD, R.drawable.ic_sensor_inflated_blood),
        MeasurementData("Multi Photo", "optical", RealDataSensorType.MUL_PHOTOELECTRIC, R.drawable.ic_sensor_mul_photoelectric)
    )
    private fun loadAndDisplaySVG(context: Context) {
        try {
            val inputStream: InputStream = requireContext().assets.open("clock.svg")
            val svg = SVG.getFromInputStream(inputStream)
//            val bitmap = Bitmap.createBitmap(svg.documentWidth.toInt(), svg.documentHeight.toInt(), Bitmap.Config.ARGB_8888)
//            val canvas = Canvas(bitmap)
//            svg.renderToCanvas(canvas)
//            svgImageView.setImageBitmap(bitmap)
            svgImageView.setImageDrawable(SvgDrawable(svg, svgImageView.width, svgImageView.height))

        } catch (e: SVGParseException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun createMeasurementDisplays() {
        measurementsContainer.removeAllViews()
        
        measurements.forEach { measurement ->
            val measurementView = LayoutInflater.from(requireContext())
                .inflate(R.layout.measurement_item, measurementsContainer, false)

            val checkbox = measurementView.findViewById<CheckBox>(R.id.measurementCheckbox)
            val icon = measurementView.findViewById<ImageView>(R.id.measurementIcon)
            val value = measurementView.findViewById<TextView>(R.id.measurementValue)
            val unit = measurementView.findViewById<TextView>(R.id.measurementUnit)

            icon.setImageResource(measurement.icon)
            value.text = "--"
            unit.text = measurement.unit

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    activeMeasurements.add(measurement.sensorType)
                    value.text = "..."
                } else {
                    activeMeasurements.remove(measurement.sensorType)
                    value.text = "--"
                }
            }

            // Store reference to the value TextView for updates
            measurementViews[measurement.sensorType] = value
            measurementsContainer.addView(measurementView)
        }
    }

    private fun startPeriodicMeasurements() {
        handler = Handler(Looper.getMainLooper())
        serviceConnection = WatchCommunicationClientShorthand.bindPeriodic(
            handler, 
            2000, // 2 second period
            requireContext(),
            this
        ) { binder ->
            // For each active measurement, trigger getRealData
            activeMeasurements.forEach { sensorType ->
                binder.getRealData(sensorType, RealDataMeasureType.SEQUENCE_1)
            }
        }
    }

    private fun stopPeriodicMeasurements() {
        serviceConnection?.let {
            requireContext().unbindService(it)
            serviceConnection = null
        }
        handler.removeCallbacksAndMessages(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        svgImageView = view.findViewById(R.id.clockView)
        measurementsContainer = view.findViewById(R.id.measurementsContainer)

        // Create measurement displays
        createMeasurementDisplays()

        // Use OnPreDrawListener to wait until the ImageView is measured
        svgImageView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                // Remove the listener to avoid multiple calls
                svgImageView.viewTreeObserver.removeOnPreDrawListener(this)

                loadAndDisplaySVG(requireContext())
                return true
            }
        })
        
        startPeriodicMeasurements()
    }

    override fun onDestroyView() {
        stopPeriodicMeasurements()
        super.onDestroyView()
    }

    override fun onWatchResponse(response: WatchResponse) {
        super.onWatchResponse(response)
        
        // Handle different R* response types and update corresponding measurement displays
        when (response) {
            is RHeart -> updateMeasurementValue(RealDataSensorType.HEART, "${response.heart}")
            is RBloodOxygen -> updateMeasurementValue(RealDataSensorType.BLOOD_OXYGEN, "${response.bloodOxygen}")
            is RBloodPressure -> updateMeasurementValue(RealDataSensorType.BLOOD_PRESSURE, "${response.systolicPressure};${response.diastolicPressure}")
            //is RSport -> updateMeasurementValue(RealDataSensorType.SPORT, "${response.steps}")
            //is RRun -> updateMeasurementValue(RealDataSensorType.RUN, "${response.steps}")
            //is RPpg -> updateMeasurementValue(RealDataSensorType.PPG, "${response.ppgValue}")
            //is REcg -> updateMeasurementValue(RealDataSensorType.ECG, "${response.ecgValue}")
            is RRespiration -> updateMeasurementValue(RealDataSensorType.RESPIRATION, "${response.respirationRate}")
            //is RSensor -> updateMeasurementValue(RealDataSensorType.SENSOR, "${response.sensorValue}")
            //is RAmbientLight -> updateMeasurementValue(RealDataSensorType.AMBIENT_LIGHT, "${response.lightValue}")
            is RComprehensive -> updateMeasurementValue(RealDataSensorType.COMPREHENSIVE, "OK")
            //is RSchedule -> updateMeasurementValue(RealDataSensorType.SCHEDULE, "${response.scheduleCount}")
            //is REventReminder -> updateMeasurementValue(RealDataSensorType.EVENT_REMINDER, "${response.reminderCount}")
            is ROga -> updateMeasurementValue(RealDataSensorType.OGA, "Data")
            //is RInflatedBlood -> updateMeasurementValue(RealDataSensorType.INFLATED_BLOOD, "${response.systolicPressure}/${response.diastolicPressure}")
            //is RUploadMulPhotoelectricWaveform -> updateMeasurementValue(RealDataSensorType.MUL_PHOTOELECTRIC, "${response.waveformData.size}")
        }
    }
    
    private fun updateMeasurementValue(sensorType: RealDataSensorType, value: String) {
        // Update measurement display on UI thread
        activity?.runOnUiThread {
            measurementViews[sensorType]?.text = value
        }
    }

    // IWatchListener implementation
    override fun onBigWatchRawResponse(rawResponse: WatchRawResponse) {
        // Handle big responses if needed
    }

    override fun onException(exception: Throwable) {
        // Handle exceptions
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}