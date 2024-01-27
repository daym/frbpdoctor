package com.friendly_machines.frbpdoctor.ui.health

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.friendly_machines.frbpdoctor.service.WatchCommunicationService

class HealthViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 6
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BloodPressureFragment()
            1 -> StepsFragment()
            2 -> HeatFragment()
            3 -> SleepFragment()
            4 -> SportFragment()
            5 -> AlarmFragment()
            else -> SleepFragment()
        }
    }

    fun getTabTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Blood Pressure"
            1 -> "Steps"
            2 -> "Heat"
            3 -> "Sleep"
            4 -> "Sport"
            5 -> "Alarm"
            else -> null
        }
    }

    fun requestData(currentItem: Int, binder: WatchCommunicationService.WatchCommunicationServiceBinder) {
        when (currentItem) {
            0 -> binder.getBpData()
            1 -> binder.getStepData()
            2 -> binder.getHeatData()
            3 -> binder.getSleepData(1701730800, 1702162800)
            4 -> binder.getSportData()
            5 -> binder.getAlarm()
        }
    }

}