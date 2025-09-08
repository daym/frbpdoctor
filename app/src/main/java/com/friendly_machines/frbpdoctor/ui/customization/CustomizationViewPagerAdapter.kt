package com.friendly_machines.frbpdoctor.ui.customization

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder

class CustomizationViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WatchFaceFragment()
            1 -> AlarmFragment()
            else -> WatchFaceFragment()
        }
    }

    fun getTabTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Watch Dials"
            1 -> "Alarms"
            else -> null
        }
    }

    fun requestData(currentItem: Int, binder: IWatchBinder) {
        when (currentItem) {
            0 -> binder.getWatchDial()
            1 -> binder.getAlarm()
        }
    }

}