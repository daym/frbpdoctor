package com.friendly_machines.frbpdoctor.ui.health

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class HealthViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> return BloodPressureFragment()
            1 -> return StepsFragment()
            2 -> return HeatFragment()
            3 -> return SleepFragment()
            4 -> return AlarmFragment()
            else -> return SleepFragment()
        }
    }

    fun getTabTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> return "Blood Pressure"
            1 -> return "Steps"
            2 -> return "Heat"
            3 -> return "Sleep"
            4 -> return "Alarm"
            else -> return "Sleep"
        }
    }
}