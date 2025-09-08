package com.friendly_machines.frbpdoctor.ui.health

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class VitalsViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BloodPressureFragment()
            1 -> HeartRateFragment() 
            2 -> BloodOxygenFragment()
            3 -> HeatFragment()
            else -> BloodPressureFragment()
        }
    }

    fun getTabTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Blood Pressure"
            1 -> "Heart Rate"
            2 -> "Blood Oxygen" 
            3 -> "Temperature"
            else -> ""
        }
    }
}