package com.friendly_machines.frbpdoctor.ui.health

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class LifestyleViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SportFragment()
            1 -> SportModeFragment()
            2 -> SleepFragment()
            else -> SportFragment()
        }
    }

    fun getTabTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Sport & Steps"
            1 -> "Sport Mode"
            2 -> "Sleep"
            else -> ""
        }
    }
}