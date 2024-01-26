package com.friendly_machines.frbpdoctor.ui.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse
import java.util.Calendar

/**
 * A simple [Fragment] subclass.
 * Use the [WeatherFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WeatherFragment : Fragment() {
    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weather, container, false)
        val weatherTypeTextNumber = view.findViewById<EditText>(R.id.weatherTypeTextNumber)
        val weatherTempTextNumber = view.findViewById<EditText>(R.id.weatherTempTextNumber)
        val weatherMaxTempTextNumber = view.findViewById<EditText>(R.id.weatherMaxTempTextNumber)
        val weatherMinTempTextNumber = view.findViewById<EditText>(R.id.weatherMinTempTextNumber)
        val weatherDummyTextNumber = view.findViewById<EditText>(R.id.weatherDummyTextNumber)
        val weatherMonthTextNumber = view.findViewById<EditText>(R.id.weatherMonthTextNumber)
        val weatherDayOfMonthTextNumber = view.findViewById<EditText>(R.id.weatherDayOfMonthTextNumber)
        val weatherDayOfWeekMondayBasedTextNumber = view.findViewById<EditText>(R.id.weatherDayOfWeekMondayBasedTextNumber)
        val weatherLocationEditView = view.findViewById<EditText>(R.id.weatherLocation)
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        weatherMonthTextNumber.setText(month.toString())
        weatherDayOfMonthTextNumber.setText(dayOfMonth.toString())
        weatherDayOfWeekMondayBasedTextNumber.setText(dayOfWeek.toString())
        view.findViewById<Button>(R.id.setWeatherButton).setOnClickListener {
            val weatherType = Integer.parseUnsignedInt(weatherTypeTextNumber.text.toString()).toShort()
            val temp = Integer.parseUnsignedInt(weatherTempTextNumber.text.toString()).toByte()
            val weatherMaxTemp = Integer.parseUnsignedInt(weatherMaxTempTextNumber.text.toString()).toByte()
            val weatherMinTemp = Integer.parseUnsignedInt(weatherMinTempTextNumber.text.toString()).toByte()
            val dummy = Integer.parseUnsignedInt(weatherDummyTextNumber.text.toString()).toByte()
            val weatherMonth = Integer.parseUnsignedInt(weatherMonthTextNumber.text.toString()).toByte()
            val weatherDayOfMonth = Integer.parseUnsignedInt(weatherDayOfMonthTextNumber.text.toString()).toByte()
            val weatherDayOfWeekMondayBased = Integer.parseUnsignedInt(weatherDayOfWeekMondayBasedTextNumber.text.toString()).toByte()
            val weatherLocation = weatherLocationEditView.text.toString()
            WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponse.SetWeather(0)) { binder ->
                binder.setWeather(weatherType, temp, weatherMaxTemp, weatherMinTemp, dummy, weatherMonth, weatherDayOfMonth, weatherDayOfWeekMondayBased, weatherLocation)
            }
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WeatherFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            WeatherFragment().apply {
                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
                }
            }
    }
}