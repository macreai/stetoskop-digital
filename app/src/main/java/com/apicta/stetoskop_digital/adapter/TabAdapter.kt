package com.apicta.stetoskop_digital.adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.apicta.stetoskop_digital.view.patient.PairedDeviceFragment
import com.apicta.stetoskop_digital.view.patient.ScanDeviceFragment

class TabAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> {
                fragment = ScanDeviceFragment()
                Log.d("Tab Adapter", "createFragment: 1")
            }
            1 -> {
                fragment = PairedDeviceFragment()
                Log.d("Tab Adapter", "createFragment: 2")
            }
        }
        return fragment as Fragment
    }
}