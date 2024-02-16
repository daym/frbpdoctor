package com.friendly_machines.frbpdoctor.ui.health

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.frbpdoctor.ui.customization.AlarmFragment
import com.friendly_machines.frbpdoctor.ui.customization.WatchDialFragment

class HealthViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BloodPressureFragment()
            1 -> StepsFragment()
            2 -> HeatFragment()
            3 -> SleepFragment()
            4 -> SportFragment()
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
            else -> null
        }
    }

    fun requestData(currentItem: Int, binder: IWatchBinder) {
        when (currentItem) {
            0 -> binder.getBpData()
            1 -> binder.getStepData()
            2 -> binder.getHeatData()
            3 -> binder.getSleepData(1701730800, 1702162800) // FIXME
            4 -> binder.getSportData()
        }
    }

}