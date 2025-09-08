package com.friendly_machines.frbpdoctor.ui.health

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder

class HealthViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 9
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BloodPressureFragment()
            1 -> HeatFragment()
            2 -> SleepFragment()
            3 -> SportFragment()
            4 -> HeartRateFragment()
            5 -> AllSensorFragment()
            6 -> SportModeFragment()
            7 -> BloodOxygenFragment()
            8 -> ComprehensiveFragment()
            else -> BloodPressureFragment()
        }
    }

    fun getTabTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Blood Pressure"
            1 -> "Heat"
            2 -> "Sleep"
            3 -> "Sport & Steps"
            4 -> "Heart Rate"
            5 -> "All Sensor"
            6 -> "Sport Mode"
            7 -> "Blood Oxygen"
            8 -> "Comprehensive"
            else -> null
        }
    }

    fun requestData(currentItem: Int, binder: IWatchBinder) {
        when (currentItem) {
            0 -> binder.getBpData()
            1 -> binder.getHeatData()
            2 -> binder.getSleepData(1701730800, 1702162800) // FIXME
            3 -> {
                binder.getSportData()
                binder.getStepData() // Also get step data for the combined Sport & Steps tab
            }
            4 -> binder.getHeartHistoryData()
            5 -> binder.getAllHistoryData()
            6 -> binder.getSportModeHistoryData()
            7 -> binder.getBloodOxygenHistoryData()
            8 -> binder.getComprehensiveHistoryData()
        }
    }

}