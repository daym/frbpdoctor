package com.friendly_machines.frbpdoctor.ui.health

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ReportsViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllSensorFragment()
            1 -> ComprehensiveFragment()
            else -> AllSensorFragment()
        }
    }

    fun getTabTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "All Sensors"
            1 -> "Comprehensive"
            else -> ""
        }
    }
}