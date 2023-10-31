package com.apicta.stetoskop_digital.view.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.apicta.stetoskop_digital.R
import com.apicta.stetoskop_digital.databinding.FragmentHomePatientBinding
import com.apicta.stetoskop_digital.databinding.FragmentLoginBinding
import com.apicta.stetoskop_digital.databinding.FragmentPatientMainBinding

class HomePatientFragment : Fragment() {

    private var _binding: FragmentHomePatientBinding? = null
    private val binding get()= _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomePatientBinding.inflate(inflater, container, false)
        val view = binding.root

        initUi()

        return view
    }

    private fun initUi() {
        binding.connect.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_homePatientFragment_to_listDeviceFragment)
        }
    }
}