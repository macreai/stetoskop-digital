package com.apicta.stetoskop_digital.view.patient

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.apicta.stetoskop_digital.R
import com.apicta.stetoskop_digital.adapter.TabAdapter
import com.apicta.stetoskop_digital.databinding.FragmentDeviceBinding
import com.apicta.stetoskop_digital.databinding.FragmentHomePatientBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.psp.bluetoothlibrary.Bluetooth

class DeviceFragment : Fragment() {

    private var _binding: FragmentDeviceBinding? = null
    private val binding get()= _binding!!

    private lateinit var bluetooth: Bluetooth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        val view = binding.root


        bluetooth = Bluetooth(requireContext())
        bluetooth.turnOnWithPermission(requireActivity())

        val adapter = TabAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager){ tab, position ->
            tab.text = when (position){
                0 -> "Scan Device"
                1 -> "Paired Device"
                else -> null
            }
        }.attach()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}